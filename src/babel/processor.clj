(ns babel.processor
 (:require [clojure.string :as s]
           [errors.messageobj :as m-obj]
           [errors.prettify-exception :as p-exc]
           [errors.dictionaries :as d]))

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
  "takes a Java Throwable object, and returns the adjusted message as a string."
  [err]
  (let [errmap (Throwable->map err)
        throwvia (:via errmap)
        viacount (count throwvia)
        errclass (str (:type (first throwvia)))
        errdata (:data errmap)]
    (if (and (= "clojure.lang.ExceptionInfo" errclass) (= viacount 1))
        (p-exc/process-spec-errors (str (.getMessage err)) errdata true)
        (if (= "clojure.lang.Compiler$CompilerException" errclass)
        ;; TO-DO: refactor this function and get rid of its uses on ExceptionInfo
          "";(p-exc/process-macro-errors err errclass (ex-data err))
          (if (and (= "clojure.lang.ExceptionInfo" errclass) (> viacount 1))
            (str
              (->> throwvia
                   reverse
                   first
                   :message
                   (str (:type (first (reverse throwvia))) " ")
                   p-exc/process-errors
                   :msg-info-obj
                   m-obj/get-all-text)
              (p-exc/process-stacktrace err))
            (str
              (->> err
                   .getMessage
                   (str errclass " ")
                   p-exc/process-errors
                   :msg-info-obj
                   m-obj/get-all-text)
              (p-exc/process-stacktrace err)))))))


(defn macro-spec?
  "Takes an exception object. Returns a true value if it's a spec error for a macro,
   a false value otherwise."
  [exc]
  (and (> (count (:via (Throwable->map exc))) 1)
       (= :macro-syntax-check (:clojure.error/phase (:data (first (:via (Throwable->map exc))))))))

(def spec-ref {:number "a number", :collection "a sequence", :string "a string", :coll "a sequence",
                :map-arg "a two-element-vector", :function "a function", :ratio "a ratio", :future "a future", :key "a key", :map-or-vector "a map-or-vector",
                :regex "a regular expression", :num-non-zero "a number that's not zero", :arg-one "not wrong" :num "a number" :lazy "a lazy sequence"
                :wrong-path "of correct type and length", :sequence "a sequence of vectors with only 2 elements or a map with key-value pairs" :number-greater-than-zero "a number that's greater than zero"})

(def length-ref {:b-length-one "one argument", :b-length-two "two arguments", :b-length-three "three arguments", :b-length-zero-or-greater "zero or more arguments",
                 :b-length-greater-zero "one or more arguments", :b-length-greater-one "two or more arguments", :b-length-greater-two "three or more arguments",
                 :b-length-zero-to-one "zero or one arguments", :b-length-one-to-two "one or two arguments", :b-length-two-to-three "two or three arguments",
                 :b-length-two-to-four "two or up to four arguments", :b-length-one-to-three "one or up to three arguments", :b-length-zero-to-three "zero or up to three arguments"})

(defn stringify
  "If there's only one item inside of path, it will use it's name via spec-ref and return a string.
   If there's two or more then it will only take the second item in the path because there's usually only three items."
  [vector-of-keywords]
  (if (= (count vector-of-keywords) 1) (name (spec-ref (first vector-of-keywords))) (name (spec-ref (second vector-of-keywords)))))

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
        wrong-num-args-msg "Wrong number of arguments, expected in (%s %s): the function %s expects %s but was given %s arguments"
        general-err-msg "The %s of (%s %s) was expected to be %s but is %s%s instead.\n"
        fn-name (d/get-function-name (str fn-full-name))
        function-args-val (apply str (interpose " " (map d/anonymous? (map #(second (d/type-and-val %)) args-val))))
        arg-number (first in)
        [print-type print-val] (d/type-and-val val)]
    (if (re-matches #"corefns\.corefns/b-length(.*)" (str pred))
        (format wrong-num-args-msg fn-name
                                   function-args-val
                                   fn-name
                                   (length-ref (keyword (d/get-function-name (str (first via))))) ;num-expected-args
                                   (if (nil? val) 0 (count val))) ;num-given-args
        (format general-err-msg (d/arg-str arg-number) ;index of incorrect argument
                                fn-name
                                function-args-val
                                (stringify path) ;correct type
                                print-type
                                print-val))))

(defn unknown-spec
  "determines if the spec function is ours or someone's else"
  [unknown-ex-data]
  (let [{problem-list :clojure.spec.alpha/problems fn-full-name :clojure.spec.alpha/fn args-val :clojure.spec.alpha/args} unknown-ex-data
        {:keys [path pred val via in]} (-> problem-list
                                           filter-extra-spec-errors
                                           first)
         fail "Fails a predicate: 'The %s argument of (%s %s) fails a requirement: must be a %s'"
         extra "Extra input: 'In the %s call (%s %s) there were extra arguments'"
         insufficient "Insufficient input: 'In the %s call (%s %s) there were insufficient arguments'"
         fn-name (d/get-function-name (str fn-full-name))
         function-args-val (apply str (interpose " " (map d/anonymous? (map #(second (d/type-and-val %)) args-val))))
         arg-number (first in)
         [print-type print-val] (d/type-and-val val)]
     (cond
       (= (:reason (first problem-list)) "Extra input") (format extra fn-name
                                                                      fn-name
                                                                      function-args-val)
      (= (:reason (first problem-list)) "Insufficient input") (format insufficient fn-name
                                                                                   fn-name
                                                                                   function-args-val)
      :else (format fail arg-number
                         fn-name
                         function-args-val
                         pred))))

(defn spec-message
 "uses babel-spec-message"
 [exception]
 (let [{problem-list :clojure.spec.alpha/problems} exception
       {:keys [pred]} (-> problem-list
                                          filter-extra-spec-errors
                                          first)]
 (if (or (re-matches #"clojure.core(.*)" (str pred)) (re-matches #"corefns\.corefns(.*)" (str pred))) (babel-spec-message exception) (unknown-spec exception))))


 (defn build-the-anonymous
   "switches the anonymous symbols with readable strings"
   [anon-func]
   (cond
     (and (re-matches #"p(\d)__(.*)" (str anon-func)) (vector? anon-func)) ""
     (string? anon-func) (str "\"" anon-func "\"")
     (= "fn*" (str anon-func)) "#"
     (re-matches #"p(\d)__(.*)" (str anon-func)) (s/replace (subs (str anon-func) 0 2) #"p" "%")
     :else (str anon-func)))


 (defn- print-macro-arg-rec
   "Takes an argument that fails a spec condition for a macro and returns
    a user-readable representation of this argument as a string"
   [val]
   (cond
     (not (seqable? val)) (build-the-anonymous val)
     (empty? val) ")"
     (seqable? (first val)) (str "(" (print-macro-arg-rec (first val)) " " (print-macro-arg-rec (rest val)))
     (and (not (empty? (rest val))) (= "fn*" (str (first val))) (seqable? val)) (str (print-macro-arg-rec (first val)) " " (print-macro-arg-rec (rest (rest val))))
     :else (str (build-the-anonymous (first val)) " " (print-macro-arg-rec (rest val)))))

(defn- print-macro-arg
  [val]
  (not (seqable? val)) (build-the-anonymous val)
  :else (str "(" (print-macro-arg-rec (first val)) (print-macro-arg-rec (rest val))))

;; Predicates are mapped to a pair of a position and a beginner-friendly
;; name. Negativr positions are later discarded
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
           (str "In place of " (print-macro-arg val) " the following are allowed:" (print-failed-predicates probs) "\n")
           "")))

(defn- process-paths-macro
  "Takes the 'problems' part of a spec for a macro and returns a description
   of the problems as a string"
  [problems]
  (let [grouped (group-by :val (map #(select-keys % [:pred :val :reason]) problems))
        num-groups (count grouped)]
       (apply str (map process-group grouped))))

(defn- invalid-macro-params?
  "Takes the 'problems' part of a spect for a macro and returns true
   if all problems refer to the parameters and false otherwise"
   [problems]
   (let [via-lasts (distinct (map str (map last (map :via problems))))]
        (and (not (empty? via-lasts)) (every? #(or (re-find #"param-list" %) (re-find #"param+body" %)) via-lasts))))

(defn spec-macro-message
  "Takes an exception of a macro spec failure and returns the description of
   the problem as a string"
  [ex]
  (let [exc-map (Throwable->map ex)
        {:keys [cause data]} exc-map
        fn-name (d/get-function-name (nth (re-matches #"Call to (.*) did not conform to spec." cause) 1))
        {problems :clojure.spec.alpha/problems value :clojure.spec.alpha/value args :clojure.spec.alpha/args} data
        val-str (d/macro-args->str value) ; need to be consistent between val and value
        n (count problems)]
        (cond (and (= n 1) (= "Insufficient input" (:reason (first problems)))) (str fn-name " requires more parts than given here: (" fn-name val-str ")\n")
              ;; should we report the extra parts?
              (and (= n 1) (= "Extra input" (:reason (first problems)))) (str fn-name " has too many parts here: (" fn-name val-str ")" (d/extra-macro-args-info (first problems)) "\n")
              ;; case of :data containing only :arg Example: (defn f ([+] 5 6) 9)
              (or (= val-str " ") (= val-str "")) (str "The parameters are invalid in (" fn-name (d/macro-args->str args)  ")\n")
              (and (= n 1) (= (resolve (:pred (first problems))) #'clojure.core.specs.alpha/even-number-of-forms?))
                   (str fn-name " requires pairs of a name and an expression, but in (" fn-name val-str ") one element doesn't have a match.\n")
              (and (= n 1) (= (resolve (:pred (first problems))) #'clojure.core/vector?))
                   (str fn-name " requires a vector of name/expression pairs, but is given " (:val (first problems)) " instead.\n")
              (invalid-macro-params? problems) (str "The parameters are invalid in (" fn-name val-str ")\n")
              :else (str "Syntax problems with (" fn-name val-str "):\n" (process-paths-macro problems)))))

(println "babel.processor loaded")
