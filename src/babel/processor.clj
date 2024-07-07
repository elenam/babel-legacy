(ns babel.processor
 (:require [clojure.string :as s]
           [com.rpl.specter :as sp]
           [errors.prettify-exception :as p-exc]
           [errors.utils :as u]
           [errors.dictionaries :as d]
           [clojure.core.specs.alpha]))

;; TODO: Look into removing this
(def recorder 
  "An atom to record unmodified error messages and their details."
  (atom {:msg [] :detail []}))

(defn reset-recorder!
  "Resets the recorder atom."
  []
  (reset! recorder {:msg [] :detail []}))

(defn update-recorder-msg!
  "Takes an unmodified error message, and appends it to the recorder's message contents."
  [input-message]
  (swap! recorder update-in [:msg] conj input-message))

(defn update-recorder-detail!
  "Takes details of an unmodified error, and appends it to the recorder's detail contents."
  [inp-message]
  (swap! recorder update-in [:detail] conj inp-message))

;; TODO: Remove this and call p-exc/process-message directly where it is used,
;; also rename the function to this in p-exc
(defn process-message
  "Takes a type and a message and returns a string based on the match found in error
  dictionary"
  [t m]
  (p-exc/process-errors t m))

(defn macro-spec?
  "Takes a \"cause\" string and a \"via\" vector from exception data. Returns 
   true if the exception is a macro spec error."
  [cause via]
   (and (= :macro-syntax-check (:clojure.error/phase (:data (first via))))
        (re-matches #"Call to (.*) did not conform to spec." cause)))

(defn invalid-signature?
  "Takes a \"cause\" string and a \"via\" vector from exception data. Returns
   true if the exception is an invalid signature error."
  [cause via]
   (and (= :macro-syntax-check (:clojure.error/phase (:data (first via))))
        (re-matches #"Invalid signature (.*) should be a (.*)" cause)))

;; For use in the stringify function.
;; These keywords are extracted from the :path of a spec failure 
;; (keywords assoc.d with specs in s/or, s/cat, etc.)
;; NOTE: We could probably condense this quite a bit with better conventions for kw'ing specs.
;; Also, maybe this could be in a separate file?
(def spec-ref {:number "a number", :num-non-zero "a number that's not zero"
               :number-greater-than-zero "a number that's greater than zero"
               :string "a string", :vector "a vector", :map "a map"
               :collection "a sequence" :coll "a sequence" :collection-map "a sequence"
               :map-arg "a two-element-vector"
               :map-or-vector "a map or a vector"
               :function "a function", :ratio "a ratio", :future "a future", :key "a key"
               :regex "a regular expression",  :lazy "a lazy sequence"
               :wrong-path "of correct type and length"
               :sequence "a sequence of vectors with only 2 elements or a map with key-value pairs"
               :only-collection "a collection"})

;; Unqualified names for arity (argument count) specs are translated to readable string representations here.
(def length-ref {:one "one argument", :two "two arguments", :three "three arguments",
                 :zero-or-greater "zero or more arguments" :greater-than-zero "one or more arguments"
                 :greater-than-one "two or more arguments", :greater-than-two "three or more arguments"
                 :zero-to-one "zero or one arguments", :one-to-two "one or two arguments"
                 :two-to-three "two or three arguments" :two-to-four "two or up to four arguments"
                 :one-to-three "one or up to three arguments", :zero-to-three "zero or up to three arguments"})

(defn stringify
  "Takes a vector of keywords of failed predicates. If there is
  only one, returns the result of looking it up in spec-ref.
  Otherwise returns the first result of looking up the rest of
  the keywords in spec-ref, as a string.
  Returns an empty string if no matches are found"
  [vector-of-keywords]
  (if (= (count vector-of-keywords) 1)
      (or (spec-ref (first vector-of-keywords)) "unknown condition")
      (or (->> (rest vector-of-keywords)
               (map #(spec-ref %))
               (filter #(not (nil? %)))
               first)
           "unknown condition")))

(defn has-alpha-nil?
  "Takes a map of a key to a vector of paths,
   returns true if :nil is present in the paths."
  ;; Destructured map argument names the keyword ":keys", but this data is usually
  ;; coming from the ":path" binding... would it be helpful to rename this?
  [{:keys [path]}]
  (.contains path :clojure.spec.alpha/nil))

(defn filter-extra-spec-errors
   "Takes a sequence of problem-maps
    (as associated with :clojure.spec.alpha/problems 
    in a map of exception data),
    returns a new sequence, filtering out any map that extraneously (TODO: why?) associates 
    :clojure.spec.alpha/nil with either :path or :reason (if any).

    TODO: Figure out and explain why filtering out maps with a :reason key is necessary.
    
    Note that each problem-map looks like {:path [...], :pred ..., etc.}"
   [problem-maps]
   (if (> (count problem-maps) 1)
     (->> problem-maps
          (filter #(not (has-alpha-nil? %))) ;; ? I'd like to see an example
            ;; where the ex-data of a failing spec might contain :clojure.spec.alpha/nil in its path.
          (filter #(not (contains? % :reason)))) ;; QUESTION: Why can't a problem-map contain a :reason key? Example?
     ;; :reason keys just contain references to further-nested exceptions, in a way
     ;; but you should look more into this by reading more ex-maps
     problem-maps))

(defn- multi-spec-fails->str
  "Takes a list of spec failure problems and the :in path and returns
   a string of failed predicates."
  [probs in]
  (->> probs
       ;; This code works. Some VS Code extensions might complain about sp/ALL
       ;; being an "unresolved var" for whatever reason, likely due to the
       ;; naming conventions used in the specter import.
       ;; QUESTION: what to do about this? maybe look this up
       #_{:clj-kondo/ignore [:unresolved-var]}
       (sp/select [sp/ALL (sp/pred #(= (:in %) in)) :pred])
       (s/join " or ")))


(defn babel-spec-message
  "Takes data representation of a Babel spec error 
   (i.e. as returned by Throwable->map), 
   and returns a modified error message as a string.  
   The new message has the form 'Wrong number of arguments ...' 
   if the failing spec is a :babel/length spec, or 'The 
   (nth) argument of (fn-name) was expected to be ...' otherwise."
  [ex-data]
  ;; From ex-data, extract problems, fn (failing function call) and 
  ;; args (that were passed to fn).
  (let [{problem-list :clojure.spec.alpha/problems,
         fn-full-name :clojure.spec.alpha/fn,
         args-val :clojure.spec.alpha/args}
        ex-data,
        ;; See: https://clojuredocs.org/clojure.spec.alpha/explain-data
        ;; The problem-list generated by a failing spec may contain a 
        ;; sequence of "problem maps", all of which may contain some of the 
        ;; keys named below. This maps the first "problem map" without a nil path
        ;; to a vector, as this is the one we will report on.
        {:keys [path pred val via in reason]} (-> problem-list
                                                  filter-extra-spec-errors
                                                  first),
        ;; Processed form of ...alpha/fn
        fn-name
        (d/get-function-name (str fn-full-name)), 
        ;; Processed form of ...alpha/args
        function-args-val
        (s/join " " (map d/non-macro-spec-arg->str args-val)), 
        ;; Number of the arg that caused the spec failure, as reported with the :in key
        arg-number
        (first in),
        ;; Only used for evaluation in the :else case below.
        [print-type print-val] (d/type-and-val val)] 
    
    (cond reason "babel specs are inconsistent, sorry",
    ;;    ^^^ If the only spec contains :reason, this means that babel 
    ;; specs for arity aren't set up right... i.e. we shouldn't be here.
    ;; TODO: Look into trying to trigger this case, creating an inconsistent spec for arity
          ;; Case: the spec relates to a babel/length predicate. In this case, the user
          ;; has entered the wrong number of arguments for the function.
          ;; TODO: update the regex here to match namespace correctly. also, look into a better function
          (re-matches #"corefns\.corefns/b-length(.*)" (str pred))
          (str "Wrong number of arguments in (" 
               fn-name 
               " " 
               function-args-val 
               "): the function " 
               fn-name 
               " expects "
               (length-ref (keyword (d/get-function-name (str (first via)))))
               " but was given "
               (if (or (nil? val) (= (count val) 0)) "no" (d/number-word (count val)))
               (if (= (count val) 1) " argument." " arguments.")) 
          
          :else
          (str "The " 
               (d/arg-str arg-number) 
               " of (" 
               fn-name 
               " " 
               function-args-val 
               ") was expected to be "
               (stringify path)
               " but is "
               print-type
               (d/anon-fn-handling print-val)
               " instead."))))

(defn third-party-spec
  "Handles spec that's not from babel: takes the exc-data
  and returns the message as a string."
  [ex-data]
  (let [{problem-list :clojure.spec.alpha/problems fn-full-name :clojure.spec.alpha/fn args-val :clojure.spec.alpha/args} ex-data
         filtered-probs (filter-extra-spec-errors problem-list)
         {:keys [_path pred val _via in]} (first filtered-probs)
         fn-name (d/get-function-name (str fn-full-name))
         function-args-val (s/join " " (map d/non-macro-spec-arg->str args-val))
         arg-number (first in)
         [print-type print-val] (d/type-and-val val)]
     (cond
       (= (:reason (first problem-list)) "Extra input")
          (str
            "Wrong number of arguments in ("
            fn-name
            " "
            function-args-val
            "): the function "
            fn-name
            " requires fewer than "
            (d/number-word (count args-val))
            " arguments.")
       (and (= (:reason (first problem-list)) "Insufficient input") (> (count args-val) 0))
          (str
            "Wrong number of arguments in ("
            fn-name
            " "
            function-args-val
            "): the function "
            fn-name
            " requires more than "
            (d/number-word (count args-val))
            " arguments.")
      (= (:reason (first problem-list)) "Insufficient input") ;; (= (count args-val) 0)
         (str
           "Wrong number of arguments in ("
           fn-name
           " "
           function-args-val
           "): the function "
           fn-name
           " cannot be called with no arguments.")
       (= 1 (count filtered-probs))
          (str
            "In ("
            fn-name
            " "
            function-args-val
            ") the "
            (d/arg-str arg-number)
            ", which is "
            print-type
            (d/anon-fn-handling print-val)
            ", fails a requirement: "
            pred)
         ;; if there are more errors with the same :in, pull all the pred names
         :else
           (str
             "In ("
             fn-name
             " "
             function-args-val
             ") the "
             (d/arg-str arg-number)
             ", which is "
             print-type
             (d/anon-fn-handling print-val)
             ", fails a requirement: "
             (multi-spec-fails->str filtered-probs in)))))

(def babel-ns ":babel")

(defn- babel-fn-spec?
  "Takes a list of spec problems, returns true if any of the :via or :pred
   starts with :babel"
  [probs]
  #_{:clj-kondo/ignore [:unresolved-var]}
  ;; This code works. Some VS Code extensions might complain about sp/ALL
  ;; being an "unresolved var" for whatever reason, likely due to the 
  ;; naming conventions used in the specter import.
  (let [p (sp/select [sp/ALL (sp/multi-path :via :pred)] probs)] 
    (sp/selected-any? [sp/ALL
                       ;; Can handle a vector or a list of predicates or a single predicate:
                       (sp/if-path seqable? sp/ALL sp/STAY)
                       #(s/starts-with? (str %) babel-ns)]
                      p)))


(defn spec-message
 "Takes exception data and calls either babel spec processing or third-party spec
  processing."
 [data]
 (let [{probs :clojure.spec.alpha/problems} data]
 (if (babel-fn-spec? probs)
     (babel-spec-message data)
     (third-party-spec data))))


;; Predicates are mapped to a pair of a position and a beginner-friendly
;; name. Negative positions are later discarded
(def macro-predicates {#'clojure.core/simple-symbol? [0 " a name"],
  #'clojure.core/vector? [1 " a vector"], #'clojure.core/map? [2 " a hashmap"],
  #'clojure.core/qualified-keyword? [-1 " a keyword"],
  #'clojure.core/sequential? [1 " a vector"]}) ; while other sequential constructs are possible, for beginners "a vector" is sufficient

(defn- predicate-name
  "Takes a failed predicate from a macro spec, returns a vector
   of its name and position"
  [p]
  (cond (symbol? p) (or (macro-predicates (resolve p)) [10 " unknown type"]) ; for debugging purposes
        (set? p) [-1 " one of specific keywords"]
        (= (str p) "(clojure.core/fn [%] (clojure.core/not= (quote &) %))") [-1 " not &"]
        (and (seq? p) (re-find #"clojure.core/sequential\?" (apply str (flatten p))))
             (macro-predicates #'clojure.core/sequential?)
        :else  [10 (str " unknown type " p)]))

(defn- print-failed-predicates
  "Takes a vector of hashmaps of failed predicates and returns a string
   that describes them for beginners"
  [probs]
  (->> probs
       (filter #(nil? (:reason %))) ; eliminate "Extra input" and "Insufficient input"
       (map :pred) ; get the failed predicates
       (distinct) ; eliminate duplicates
       (map #(predicate-name %)) ; get position/name pairs
       (sort #(< (first %1) (first %2))) ; sort by the position
       (filter #(>= (first %) 0)); remove negative positions
       (map second) ; take names only
       (distinct) ; eliminate duplicates
       (s/join " or"))) ; join into a string with " or" as a separator

(defn- process-group
  "Takes a vector of a value and hashmaps of predicates it failed and returns
   a string describing the problems"
  [[val probs]]
  (let [printed-group (print-failed-predicates probs)]
       (if (not= printed-group "")
           (str "In place of "
                (d/print-macro-arg val :nil)
                " the following are allowed:"
                (print-failed-predicates probs) "\n")
           "")))

(defn- process-paths-macro
  "Takes the 'problems' part of a spec for a macro and returns a description
   of the problems as a string"
  [problems]
  (let [grouped (group-by :val (map #(select-keys % [:pred :val :reason]) problems))]
       (apply str (map process-group grouped))))

(defn- invalid-macro-params?
  "Takes the 'problems' part of a spect for a macro and returns true
   if all problems refer to the parameters and false otherwise"
   [problems]
   (let [via-lasts (distinct (map str (map last (map :via problems))))]
     ;; Usage of (seq? %) here is equivalent to (not (empty? %))
     (and (seq via-lasts) (every? #(or (re-find #"param-list" %) (re-find #"param+body" %)) via-lasts))))

(defn- let-macros
  "Takes parts of the spec message for let and related macros and returns an error message as a string"
  [fn-name value problems]
  (str "Syntax problems with ("
        fn-name
        " "
        (d/print-macro-arg (first value))
        ;; The 'if' is needed so that there is a space before the args, but no space when there
        ;; are no args:
        (if (= (count (rest value)) 0)
            ""
            (str " " (d/print-macro-arg (rest value) :no-parens)))
        "):\n"
        (process-paths-macro problems)))

(defn- defn-macros
  "Takes parts of the spec message for defn and defn- and returns an error message as a string"
  [fn-name value problems]
  (let [n (count problems)
        val-str (d/print-macro-arg value :no-parens)
        probs-labeled (u/label-vect-maps problems) ; each spec fail is labeled with its position in 'problems'
        probs-grouped (group-by :in probs-labeled)
        error-name (str "Syntax problems with (" fn-name (u/with-space-if-needed val-str) "):\n")]
        (cond (u/has-match? probs-grouped {:path [:fn-name]})
                   (str error-name  (u/missing-name (:val (first problems))))
              ;; Multi-arity defn fails with a non-informtive spec failure
              (= n 0) (str error-name
                           "Unexpected element(s) outside of the first clause: "
                           (d/print-macro-arg (rest (drop-while #(not (seq? %)) value)) :no-parens))
              ;; Special case for defn since a string could be a doc-string and a map
              ;; could be a pre/post-conditions map:
              (and (= n 1) (u/has-match? probs-grouped {:path [:fn-tail] :reason "Insufficient input"}))
                   (str error-name fn-name " is missing a vector of parameters.")
              (u/has-match? probs-grouped {:reason "Insufficient input", :pred :clojure.core.specs.alpha/binding-form})
                   (str error-name fn-name " is missing a name after &.")
              (u/has-every-match? probs-grouped
                   [{:pred 'clojure.core/vector?}
                    {:pred '(clojure.core/fn [%] (clojure.core/or (clojure.core/nil? %) (clojure.core/sequential? %)))}])
                   (str error-name (u/missing-vector-message probs-grouped value))
              (and (> n 1) (u/all-match? probs-grouped {:reason "Extra input"}))
                   (str error-name (u/process-nested-error probs-grouped))
              (u/has-every-match? probs-grouped
                  [{:pred 'clojure.core/vector?, :path [:fn-tail :arity-1 :params]}
                   {:pred 'clojure.core/vector?, :path [:fn-tail :arity-n :bodies :params]}])
                  (str error-name (u/missing-vector-message-seq
                                    (first (u/get-match probs-grouped
                                                 {:pred 'clojure.core/vector?, :path [:fn-tail :arity-1 :params]}))
                                       value))
              (u/has-every-match? probs-grouped
                   [{:reason "Extra input", :path [:fn-tail :arity-1 :params]}
                    {:pred 'clojure.core/vector?, :path [:fn-tail :arity-n :bodies :params]}])
                   (str error-name (u/parameters-not-names
                                     (first (u/get-match probs-grouped
                                                 {:reason "Extra input", :path [:fn-tail :arity-1 :params]}))
                                      value))
              (u/has-every-match? probs-grouped
                   [{:reason "Extra input", :path [:fn-tail :arity-1 :params]}
                    {:path [:fn-tail :arity-n :bodies :params :var-params :var-form :local-symbol]}])
                   (str error-name (u/parameters-not-names
                                     (first (u/get-match probs-grouped
                                                 {:path [:fn-tail :arity-n :bodies :params :var-params :var-form :local-symbol]}))
                                      value))
              (u/has-every-match? probs-grouped
                   [{:path [:fn-tail :arity-1 :params :var-params :var-form :local-symbol]}
                    {:path [:fn-tail :arity-1 :params :var-params :var-form :seq-destructure]}
                    {:path [:fn-tail :arity-1 :params :var-params :var-form :map-destructure]}
                    {:pred 'clojure.core/vector?, :path [:fn-tail :arity-n :bodies :params]}])
                  (str error-name (u/parameters-not-names
                                     (first (u/get-match probs-grouped
                                                  {:path [:fn-tail :arity-1 :params :var-params :var-form :local-symbol]}))
                                     value))
              :else "Placeholder message for defn")))

(defn- fn-macros
  "Takes parts of the spec message for fn and returns an error message as a string"
  [fn-name value problems]
  (let [n (count problems)
       val-str (d/print-macro-arg value :no-parens)
       probs-labeled (u/label-vect-maps problems) ; each spec fail is labeled with its position in 'problems'
       probs-grouped (group-by :in probs-labeled)
       error-name (str "Syntax problems with (" fn-name (u/with-space-if-needed val-str) "):\n")
       multi-clause? (u/multi-clause-fn? value)]
       (cond (and (= n 1) ((u/key-vals-match {:reason "Insufficient input", :path [:fn-tail]}) (first problems)))
                  (str error-name "fn is missing a vector of parameters.")
             (u/has-match? probs-grouped {:reason "Insufficient input", :pred :clojure.core.specs.alpha/binding-form})
                  (str error-name
                       (if multi-clause?
                           (u/err-clause-str value
                                             (:in (first problems)))
                            "")
                       "fn is missing a name after &.")
             (u/has-every-match? probs-grouped
                  [{:pred 'clojure.core/vector?}
                   {:pred '(clojure.core/fn [%] (clojure.core/or (clojure.core/nil? %) (clojure.core/sequential? %)))}])
                   (str error-name (u/missing-vector-message probs-grouped value))
             (and (> n 1) (u/all-match? probs-grouped {:reason "Extra input"}))
                  (str error-name (u/process-nested-error probs-grouped))
             (u/has-every-match? probs-grouped
                  [{:pred 'clojure.core/vector?, :path [:fn-tail :arity-1 :params]}
                   {:pred 'clojure.core/vector?, :path [:fn-tail :arity-n :params]}])
                  (str error-name (u/missing-vector-message-seq
                                    (first (u/get-match probs-grouped
                                                 {:pred 'clojure.core/vector?, :path [:fn-tail :arity-1 :params]}))
                                     value))
             (u/has-every-match? probs-grouped
                  [{:reason "Extra input", :path [:fn-tail :arity-1 :params]}
                   {:pred 'clojure.core/vector?, :path [:fn-tail :arity-n :params]}])
                  (str error-name (u/parameters-not-names
                                    (first (u/get-match probs-grouped
                                                 {:reason "Extra input", :path [:fn-tail :arity-1 :params]}))
                                    value))
              ;; multi-arity case, first clause
             (u/has-every-match? probs-grouped
                 [{:reason "Extra input", :path [:fn-tail :arity-n :params]}
                  {:pred 'clojure.core/vector?, :path [:fn-tail :arity-1 :params]}])
                 (let [prob1 (first (u/get-match probs-grouped
                              {:reason "Extra input", :path [:fn-tail :arity-n :params]}))]
                       (str error-name
                            (if multi-clause?
                                (u/err-clause-str value
                                                  (:in prob1))
                                "")
                            (u/parameters-not-names prob1
                                                    value)))
             (u/has-every-match? probs-grouped
                 [{:reason "Extra input", :path [:fn-tail :arity-1 :params]}
                  {:path [:fn-tail :arity-n :params :var-params :var-form :local-symbol]}])
                 (str error-name (u/parameters-not-names
                                   (first (u/get-match probs-grouped
                                                {:path [:fn-tail :arity-n :params :var-params :var-form :local-symbol]}))
                                   value))
             (u/has-every-match? probs-grouped
                 [{:pred 'clojure.core/vector?, :path [:fn-tail :arity-1 :params]}
                  {:reason "Insufficient input", :path [:fn-tail :arity-n :params]}])
                 (str error-name (u/parameters-not-names
                                   (first (u/get-match probs-grouped
                                                {:pred 'clojure.core/vector?, :path [:fn-tail :arity-1 :params]}))
                                   value))
              (u/has-every-match? probs-grouped
                   [{:path [:fn-tail :arity-1 :params :var-params :var-form :local-symbol]}
                    {:path [:fn-tail :arity-1 :params :var-params :var-form :seq-destructure]}
                    {:path [:fn-tail :arity-1 :params :var-params :var-form :map-destructure]}
                    {:pred 'clojure.core/vector?, :path [:fn-tail :arity-n :params]}])
                  (str error-name (u/parameters-not-names
                                     (first (u/get-match probs-grouped
                                                  {:path [:fn-tail :arity-1 :params :var-params :var-form :local-symbol]}))
                                     value))
              (and (= n 1) (u/has-match-by-prefix? probs-grouped {:path [:fn-tail :arity-n]}))
                   (str error-name
                        (if multi-clause?
                            (u/err-clause-str value
                                              (:in (first problems)))
                            "")
                        (u/clause-single-spec (first problems) ; n=1, so there is only one prob
                                              value))
              :else (str error-name "Placeholder for a message for fn"))))


(defn spec-macro-message
  "Takes the cause and data of a macro spec failure and returns the description of
   the problem as a string"
  [cause data]
  (let [fn-name-match (nth (re-matches #"Call to (.*) did not conform to spec." cause) 1)
        fn-name (if (= (str fn-name-match) "clojure.core/fn") "fn" (d/get-function-name fn-name-match))
        {problems :clojure.spec.alpha/problems value :clojure.spec.alpha/value args :clojure.spec.alpha/args} data
        val-str (d/print-macro-arg args :no-parens) ; args is present in cases when there is no value (e.g. multi-arity defn)
        n (count problems)]
        (cond (#{"fn"} fn-name) (fn-macros fn-name args problems)
              (#{"defn" "defn-"} fn-name) (defn-macros fn-name args problems)
              (and (= n 1) (= "Insufficient input" (:reason (first problems))))
                   (str fn-name
                        " requires more parts than given here: ("
                        fn-name
                        val-str
                        ")\n")
              ;; should we report the extra parts?
              (and (= n 1) (= "Extra input" (:reason (first problems))))
                   (str fn-name
                        " has too many parts here: ("
                        fn-name
                        " "
                        val-str
                        ")"
                        (d/extra-macro-args-info (first problems))
                        "\n")
              (and (= n 1) (= (resolve (:pred (first problems))) #'clojure.core.specs.alpha/even-number-of-forms?))
                   (str fn-name
                        " requires pairs of a name and an expression, but in ("
                        fn-name
                        val-str
                        ") one element doesn't have a match.\n")
              (and (= n 1) (= (resolve (:pred (first problems))) #'clojure.core/vector?))
                   (str fn-name
                        " requires a vector of name/expression pairs, but is given "
                        (d/print-macro-arg (:val (first problems)) :nil)
                        " instead.\n")
              (invalid-macro-params? problems)
                    (str "The parameters are invalid in ("
                         fn-name
                         " "
                         val-str
                         ")\n")
              (and (#{"let" "if-let"} fn-name) (seqable? value))
                   (let-macros fn-name value problems)
              :else (str "Syntax problems with ("
                         fn-name
                         " "
                         val-str
                         "):\n"
                         (process-paths-macro problems)))))

 (defn invalid-sig-message
   "Takes the cause and symbol of an invalid signature macroexpansion error and
    returns the description of the problem as a string"
   [cause s]
   (let [[_ what should-be] (re-matches #"Invalid signature \"(.*)\" should be a (.*)" cause)]
        (str "Syntax problems in "
              s
              ": instead of "
              what
              " you need a " ;; Encountered cases are: list, vector. Later we may need "an" article for some cases
              should-be
              ".")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;; Location and stacktrace ;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn location-macro-spec
  "Takes the first element of via for a macro spec, returns a string
   with the location of the error."
  [[via1]]
  (let [{source :clojure.error/source
         line :clojure.error/line
         column :clojure.error/column} (:data via1)]
         (u/location->str {:source source :line line :column column})))

 (defn location-function-spec
   "Takes data of a function spec, returns a string with the location
   of the error."
   [data]
   (let [{:keys [source file line]} (:clojure.spec.test.alpha/caller data)
         src (or source file)]
         (u/location->str {:source src :line line})))

(defn location-non-spec
  "Takes the via list of an exception and its stacktrace and returns the location
   of the error as a string."
  [via trace]
  (let [loc-via (u/location->str (u/get-line-info via))
        ;; The result of u/location->str always has a . at the end
        loc-at (if (= loc-via ".") (u/location->str (u/get-line-info-from-at via)) loc-via)
        loc (if (= loc-at ".") (u/location->str (u/get-line-info-from-stacktrace trace)) loc-at)]
        (if (= loc ".") "" loc)))

(defn location-print-phase-spec
  "Takes the data of a spec error for a print-eval phase and returns
   the location of the error as a string."
  [data]
  (let [{:keys [var-scope]} (:clojure.spec.test.alpha/caller data)
        fname (d/get-function-name (str var-scope))]
        (if (= fname "anonymous function")
            (str "Called from an anonymous function; location unknown.")
            (str "Called from the function: "
                 fname
                 "; location unknown."))))

(defn location-print-phase
  "Takes the via and the trace of an exception and returns available
   info about the error location, as a string.
   Assumes that the phase of the exception is :print-eval-result"
  [via trace]
  (let [f (->> via
               last
               :at
               first)
         f1 (u/get-name-from-tr-element f)
         fname (if (= f1 "") (u/get-fname-from-stacktrace trace) f1)]
         (if (= fname "anonymous function")
             "In an anonymous function; location unknown."
             (str "In function: " fname "; location unknown."))))

(defn print-stacktrace
  "Takes an exception and returns its filtered and formatted stacktrace
   as a string (with newlines)"
  [exc]
  (->> exc
       Throwable->map
       :trace
       u/filter-stacktrace
       (take 20)
       u/format-stacktrace))

(println "babel.processor loaded")
