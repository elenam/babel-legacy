(ns babel.processor
 (:require [clojure.string :as s]
           [com.rpl.specter :as sp]
           [errors.prettify-exception :as p-exc]
           [errors.utils :as u]
           [errors.dictionaries :as d]
           [clojure.core.specs.alpha]))

;;an atom that record original error response
(def recorder (atom {:msg [] :detail []}))

(defn reset-recorder
  "This function reset the recorder atom"
  []
  (reset! recorder {:msg [] :detail []}))

(defn update-recorder-msg
  "takes an unfixed error message, and put it into the recorder"
  [inp-message]
  (swap! recorder update-in [:msg] conj inp-message))
  ;(swap! recorder assoc :msg inp-message))

(defn update-recorder-detail
  "takes error message details, and put them into the recorder"
  [inp-message]
  (swap! recorder update-in [:detail] conj inp-message))

(defn process-message
  "Takes a type and a message and returns a string based on the match found in error
  dictionary"
  [t m]
  (p-exc/process-errors t m))

(defn macro-spec?
  "Takes an exception cause and via. Returns a true value
   if it's a spec error for a macro, a false value otherwise."
  [cause via]
   (and (= :macro-syntax-check (:clojure.error/phase (:data (first via))))
        (re-matches #"Call to (.*) did not conform to spec." cause)))

(defn invalid-signature?
  "Takes an exception cause and via. Returns a true value
   if it's an invalid signature error, a false value otherwise."
  [cause via]
   (and (= :macro-syntax-check (:clojure.error/phase (:data (first via))))
        (re-matches #"Invalid signature (.*) should be a (.*)" cause)))

(def spec-ref {:number "a number", :collection "a sequence", :string "a string", :coll "a sequence",
                :map-arg "a two-element-vector", :function "a function", :ratio "a ratio", :future "a future", :key "a key", :map-or-vector "a map-or-vector",
                :regex "a regular expression", :num-non-zero "a number that's not zero", :num "a number", :lazy "a lazy sequence"
                :wrong-path "of correct type and length", :sequence "a sequence of vectors with only 2 elements or a map with key-value pairs",
                :number-greater-than-zero "a number that's greater than zero",
                :collection-map "a sequence" :only-collection "a collection"})

(def length-ref {:b-length-one "one argument", :b-length-two "two arguments", :b-length-three "three arguments", :b-length-zero-or-greater "zero or more arguments",
                 :b-length-greater-zero "one or more arguments", :b-length-greater-one "two or more arguments", :b-length-greater-two "three or more arguments",
                 :b-length-zero-to-one "zero or one arguments", :b-length-one-to-two "one or two arguments", :b-length-two-to-three "two or three arguments",
                 :b-length-two-to-four "two or up to four arguments", :b-length-one-to-three "one or up to three arguments", :b-length-zero-to-three "zero or up to three arguments"})

;; TO-DO: check if this is used!
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
  [{:keys [path]}]
  (.contains path :clojure.spec.alpha/nil))

(defn filter-extra-spec-errors
   "problem-maps looks like [{:path [:a :b ...] ~~} {:path [] ~~} ...]
   Filters through problem-maps removing any map that contains :clojure.spec.apha/nil in :path or :reason"
   [problem-maps]
   (if (> (count problem-maps) 1)
       (->> problem-maps
            (filter #(not (has-alpha-nil? %)))
            (filter #(not (contains? % :reason))))
       problem-maps))

(defn babel-spec-message
  "Takes ex-info data of our babel spec error, returns a modified message as a string"
  [ex-data]
  (let [{problem-list :clojure.spec.alpha/problems fn-full-name :clojure.spec.alpha/fn args-val :clojure.spec.alpha/args} ex-data
        {:keys [path pred val via in]} (-> problem-list
                                           filter-extra-spec-errors
                                           first)
        fn-name (d/get-function-name (str fn-full-name))
        function-args-val (s/join " " (map d/non-macro-spec-arg->str args-val))
        arg-number (first in)
        [print-type print-val] (d/type-and-val val)]
    (if (re-matches #"corefns\.corefns/b-length(.*)" (str pred))
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
        {:keys [path pred val via in]} (-> problem-list
                                           filter-extra-spec-errors
                                           first)
         fn-name (d/get-function-name (str fn-full-name))
         function-args-val (s/join " " (map d/non-macro-spec-arg->str args-val))
         arg-number (first in)
         [print-type print-val] (d/type-and-val val)]
     (cond
       (= (:reason (first problem-list)) "Extra input")
          (str
            "Extra input: 'In the "
            fn-name
            "call ("
            fn-name
            " "
            function-args-val
            ") there were extra arguments'")
       (= (:reason (first problem-list)) "Insufficient input")
          (str
            "Insufficient input: 'In the "
            fn-name
            "call ("
            fn-name
            " "
            function-args-val
            ") there were insufficient arguments'")
       :else ;; "In (my-test-fn 3 4) the second argument 4 fails a requirement: clojure.core/string?"
          (str
            "In ("
            fn-name
            " "
            function-args-val
            ") the "
            (d/arg-str arg-number)
            " "
            print-val
            " fails a requirement: "
            pred))))

(def BABEL-NS ":corefns.corefns")

(defn- babel-fn-spec?
  "Takes a list of spec problems, returns true if any of the :via or :pred
   starts with :corefns.corefns"
  [probs]
  (let [p (sp/select [sp/ALL (sp/multi-path :via :pred)] probs)]
        (sp/selected-any? [sp/ALL
                           ;; Can handle a vector or a list of predicates or a single predicate:
                           (sp/if-path seqable? sp/ALL sp/STAY)
                           #(s/starts-with? (str %) BABEL-NS)]
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
        (and (not (empty? via-lasts)) (every? #(or (re-find #"param-list" %) (re-find #"param+body" %)) via-lasts))))

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
