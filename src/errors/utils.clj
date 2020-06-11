(ns errors.utils
  (:require [clojure.string :as s]
            [com.rpl.specter :as sp]
            [errors.dictionaries :as d]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;; Utilities for handling macro specs ;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn with-space-if-needed
  [val-str]
  (if (= val-str "") "" (str " " val-str)))

(defn label-vect-maps
  "Takes a vector of maps and returns it with an extra key/val pair added to each entry:
  :n and the index in the original vector.
   For instance, given [{:a 1} {:b 0} {:c 5}] it returns
   [{:a 1, :n 0} {:b 0, :n 1} {:c 5, :n 2}].
   Helpful for subsequent sorting since it preserves the index in the original vector."
  [v-maps]
  (mapv #(assoc %1 :n %2) v-maps (range)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;; Predicates for handling fn ;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- v-prefix=?
  "Returns true if one vector is a prefix of the other one or
  the two are exactly the same"
  [v1 v2]
  (and (<= (count v1) (count v2)) (every? #{true} (map = v1 v2))))

(defn- v-prefix?
  "Returns true if one vector is a strict prefix of the other one"
  [v1 v2]
  (and (< (count v1) (count v2)) (every? #{true} (map = v1 v2))))

(defn fn-named?
  "Takes a value of a failing spec and returns true if the fn has a name
  and false otherwise."
  [value]
  (and (seq? value) (simple-symbol? (first value))))

(defn fn-has-amp?
  "Takes a value of a failing spec and the 'in' index and returns true if the
  value at the index has & in it and false otherwise"
  [value in]
  (and (vector? (nth value in)) (not (empty? (filter #(= % (symbol '&)) (nth value in))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;; Functions for selecting specs ;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn key-vals-match
  "Takes a map of keys and values and returns a function that matches
  a spec problem based on these key/vals."
  [m]
  (fn [p] (= m (sp/select-one (sp/submap (keys m)) p))))

(defn key-vals-match-by-prefix
  "Takes a map of keys and values and returns a function that matches
  a spec problem with given keys whose values are either exactly
  equal to given values or start with a given vector."
  [m]
  ;; split both m & p into vector/non-vector values, check vectors using prefix=?, check non-vectors by =
  (let [not-vector? (complement vector?)
        f-vector (sp/filterer #(vector? (second %)))
        f-not-vector (sp/filterer #(not-vector? (second %)))
        [m-vect m-not-vect] (sp/select (sp/multi-path f-vector f-not-vector) m)]
        (fn [p] (let [[p-vect p-not-vect] (sp/select
                                              (sp/multi-path f-vector f-not-vector)
                                              (sp/select-one (sp/submap (keys m)) p))]
           (and (= m-not-vect p-not-vect) (every? true? (map #(v-prefix=? (second %1) (second %2)) m-vect p-vect)))))))

(defn get-match
  "Takes spec failures grouped by 'in' and a map of key/val pairs and returns
  a vector of matching spec problems."
  [grouped-probs m]
  (sp/select [sp/MAP-VALS sp/ALL (key-vals-match m)] grouped-probs))

(defn get-match-by-prefix
  "Takes spec failures grouped by 'in' and a map of key/val pairs and returns
  a vector of spec problems with given keys whose values are either exactly
  equal to given values or start with a given vector."
  [grouped-probs m]
  (sp/select [sp/MAP-VALS sp/ALL (key-vals-match-by-prefix m)] grouped-probs))

(defn has-match?
  "Takes spec failures grouped by 'in' and a map of key/val pairs and returns
  true if any matches were found and false otherwise."
  [grouped-probs m]
  (not (empty? (get-match grouped-probs m))))

(defn has-match-by-prefix?
  "Takes spec failures grouped by 'in' and a map of key/val pairs and returns
  true if there is a vector of spec problems with given keys whose values are
  either exactly equal to given values or start with a given vector."
  [grouped-probs m]
  (not (empty? (get-match-by-prefix grouped-probs m))))

(defn has-every-match?
  "Takes spec failures grouped by 'in' and a sequence of maps of key/val pairs
  and returns true if each map was matched by some spec and false otherwise."
  [grouped-probs ms]
  (every? true? (map #(has-match? grouped-probs %) ms)))

(defn all-match?
  "Takes spec failures grouped by 'in' and a map of key/val pairs and returns
  true if any matches were found and false otherwise."
  [grouped-probs m]
  (= (get-match grouped-probs m) (sp/select [sp/MAP-VALS sp/ALL] grouped-probs)))

(defn doesnt-have-keys?
  "Takes a spec failure and sequence of keys and returns
  true if none of these keys are present in the spec
  and false otherwise."
  [prob ks]
  (= [] (sp/select [sp/MAP-KEYS (into #{} ks)] prob)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;; Various utils for handling specs ;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- not-names->seq
  [val]
  (cond
    (seq? val) (filter #(not (simple-symbol? %)) val)
    (nil? val) '(nil)
    :else '()))

(defn- print-with-nil-and-seq
  [val]
  (if (and (seq? val) (not (#{"fn*" "quote"} (str (first val)))))
      (d/print-macro-arg val :nil)
      (d/print-macro-arg val :no-parens :nil)))

(defn- not-names->str
  [val]
  (let [s (not-names->seq val)
        n (count s)
        names-str (s/join ", " (map print-with-nil-and-seq s))]
        (cond (and (= n 1) (symbol? (first s)))
                   ;; case of a qualified name, e.g. a/b
                   (str names-str " is not a valid name.")
              (and (> n 1) (some symbol? s))
                   ;; some are qualified name
                   (str names-str " are not valid names.")
              (= n 1)
                   (str names-str " is not a name.")
              :else (str names-str " are not names."))))


(defn- vector-amp-issues
  "Takes a vector and returns the part of the vector after the ampersand
   if the vector has an invalid use of amepersand, or nil if there
   are no ampersand issues. Is applied recursively to subvectors until
   one is found."
  [v]
  (let [[before-amp amp-and-after] (split-with (complement #{'&}) v)
         has-amp? (not (empty? amp-and-after))
         all-names-before-amp? (every? symbol? before-amp)
         length-issue-after-amp? (not= (count amp-and-after) 2) ;; Need exactly one name after &
         not-name-after-amp? (or (not (symbol? (second amp-and-after)))
                                 (= '& (second amp-and-after)))]
         (if (and has-amp? all-names-before-amp?
                  (or length-issue-after-amp? not-name-after-amp?))
             (rest amp-and-after)
             (->> v
                 (filter vector?)
                 (map vector-amp-issues)
                 (filter some?) ;; Filter out nil values
                 first))))

(defn- ampersand-issues
  [value in]
  (let [val-in (first (sp/select [(apply sp/nthpath (drop-last in))] value))]
       (vector-amp-issues val-in)))

(defn missing-name
  "Takes a value that's reported when defn is missing a name,
   returns the message string with the printed value."
  [val]
  (str "Missing a function name"
       (cond (nil? val) ", given nil instead."
             (= val '()) "."
             :else (str ", given " (d/print-macro-arg val) " instead."))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;; Handling specific spec cases ;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn missing-vector-message
  [grouped-probs value]
  (if (= 1 (count grouped-probs))
      (let [{:keys [val in pred]} (first (second (first grouped-probs)))
            clause-n (first in)
            before-n (take clause-n value)
            named? (symbol? (first value))
            start-clause (if named? 1 0)
            no-vectors? (empty? (filter vector? value))]
            (cond
               (and (= pred 'clojure.core/vector?) (> clause-n start-clause))
                    (str "A function definition requires a vector of parameters, but was given "
                          (d/print-macro-arg (seq (sp/select-one (sp/srange start-clause (inc clause-n)) value)) :no-parens)
                          " instead.")
               no-vectors?
                    (str "A function definition requires a vector of parameters, but was given "
                         (d/print-macro-arg val :no-parens)
                         " instead.")
                ;; Perhaps should report the failing argument
                :else "The function definition is missing a vector of parameters or it is misplaced."))
      "Need to handle this case"))

(defn missing-vector-message-seq
  "Takes a failing spec with a value that's a sequence and returns its
  error message for a missing vector"
  [prob value]
  (let [val (:val prob)
        no-vectors? (empty? (filter vector? value))]
        (if no-vectors?
                (str "A function definition requires a vector of parameters, but was given "
                     (d/print-macro-arg val)
                     " instead.")
            ;; Perhaps should report the failing argument
            "The function definition is missing a vector of parameters or it is misplaced.")))

(defn parameters-not-names
  [prob value]
  (let [{:keys [val in]} prob
        amp-issues (ampersand-issues value in)]
        (cond
          (nil? amp-issues) ;; No issues with &
              (str "Parameter vector must consist of names, but "
                   (not-names->str val))
          (empty? amp-issues) ; Nothing after &
              (str "Missing a name after &.")
          :else
              (str "& must be followed by exactly one name, but is followed by "
                   (d/print-macro-arg amp-issues :no-parens)
                   " instead."))))

(defn multi-clause-fn?
  "Takes a value of fn and returns true if it has multiple clauses
   and false otherwise."
  [value]
  (cond (empty? value) false
        ;; check if a named fn:
        (symbol? (first value)) (every? seq? (rest value))
        :else (every? seq? value)))

(defn err-clause-str
  [value [n & _]]
  (let [k (if (symbol? (first value)) (dec n) n)
        clause (d/position-0-based->word k)]
       (str "The issue is in the "
            clause
            " clause.\n")))

(defn multi-clause-defn?
  "Takes a value of defn and returns true if it has multiple clauses
   and false otherwise."
  [[_ & s]]
  ;; The first of value is a name, ignore it
  ;; The second may be a doc-string
  (let [clauses-or-maps (if (string? (first s))
                            (rest s)
                            s)]
       ;; the first and the last are allowed to be attribute maps
       (or (every? seq? clauses-or-maps)
           (and (map? (first clauses-or-maps))
                (every? seq? (rest clauses-or-maps)))
           (and (map? (first clauses-or-maps))
                (map? (last clauses-or-maps))
                (every? seq? (drop-last (rest clauses-or-maps)))))))

(defn clause-single-spec
  [prob value]
  (let [{:keys [reason val pred in path]} prob
        clause-n (first in)
        before-n (take clause-n value)
        clause-val (nth value clause-n)
        named? (symbol? (first value))
        start-clause (if named? 1 0)
        has-vector? (some vector? before-n)
        amp-issues (ampersand-issues value in)]
        (cond has-vector?
                "fn needs a vector of parameters and a body, but has something else instead."
              (and (= "Extra input" reason) (not (nil? amp-issues)))
                  (str "& must be followed by exactly one name, but is followed by "
                       (d/print-macro-arg amp-issues :no-parens)
                       " instead.")
              (= "Extra input" reason)
                 (str "Parameter vector must consist of names, but "
                      (not-names->str val))
              (and (= pred 'clojure.core/vector?) (vector? clause-val))
                 (str "A function clause must be enclosed in parentheses, but is a vector "
                      (d/print-macro-arg clause-val)
                      " instead.")
              (and (= pred 'clojure.core/vector?) (#{"fn*" "quote"} (str val)))
                 (str (d/print-macro-arg clause-val)
                      " cannot be outside of a function body.")
              (and (= pred 'clojure.core/vector?) (> clause-n start-clause) (not (multi-clause-fn? value)))
                 (str "A function definition requires a vector of parameters, but was given "
                      (d/print-macro-arg (sp/select-one (sp/srange start-clause (inc clause-n)) value))
                      " instead.")
              (= pred 'clojure.core/vector?)
                 (str "A function definition requires a vector of parameters, but was given "
                      (d/print-macro-arg val)
                      " instead.")
              :else (str (d/print-macro-arg val)
                         " cannot be outside of a function body."))))

(defn process-nested-error
  "Takes grouped problems of a spec with all 'Extra input' conditions, returns a message based
   on the less nested problem"
  [gp]
  (let [ins (keys gp)
        less-nested-groups (if (and (> (count ins) 1) (v-prefix? (second ins) (first ins)))
                               (gp (second ins))
                               (gp (first ins)))
        prob (first less-nested-groups)
        val (:val prob)]
        (str "Function parameters must be a vector of names, but "
             (d/print-macro-arg val :no-parens)
             " was given instead.")))

;; ########################################################
;; ### Utils for getting location info from stacktrace ####
;; ########################################################

(def excluded-ns-for-location #{"clojure.core" "clojure.string" "clojure.lang"
                   "clojure.main" "nrepl.middleware" "java.lang"
                   "java.util"})

(defn allowed-ns?
  "Takes a stacktrace element of an exception
   and returns true if its first element doesn't start
   with any of the excluded namespaces, and false otherwise"
  [s]
  (let [ns-element (str (first s))]
       (every? #(not (s/starts-with? ns-element %)) excluded-ns-for-location)))

(defn allowed-ns-invoke-static?
  [s]
  "Takes a stacktrace element of an exception
   and returns true if its method is \"invokeStatic\"
   and its first element doesn't start
   with any of the excluded namespaces, and false otherwise"
  (let [[ne m] s
        ns-element (str ne)
        method (str m)]
        (and (= method "invokeStatic")
             (every? #(not (s/starts-with? ns-element %)) excluded-ns-for-location))))

;; ########################################################
;; ########## Utils for getting error location ############
;; ########################################################

(defn get-line-info
  "Takes the 'via' list of nested exceptions and attempts to find
   the line/column/source info given direrctly in via elements.
   Returns a map (possibly partially) filled in
   with this info. Non-found fields are mapped to nil."
  [via]
  (let [v (sp/select [sp/ALL :data
                             (sp/submap [:clojure.error/line :clojure.error/column :clojure.error/source])]
                      via)
        {line :clojure.error/line
         column :clojure.error/column
         source :clojure.error/source} (sp/select-first [sp/ALL #(not (empty? %))] v)
         phase (sp/select-first [:data :clojure.error/phase] (first via))]
         (cond source
                  {:source source :line line :column column}
               (= :read-source phase)
                  {:source phase :line line :column column}
               :else
                  {:source nil :line line :column column})))

(defn get-line-info-from-at
  "Takes the 'via' list of nested exceptions and attempts to find
   the line/source info in the 'at' looking for a first occurence of
   a non-excluded namespace.
   Returns a map (possibly partially) filled in
   with this info. Non-found fields are mapped to nil."
  [via]
  (let [all-at (sp/select [sp/ALL (sp/submap [:at]) sp/MAP-VALS] via)
        [_ _ source line] (sp/select-first [sp/ALL allowed-ns?] all-at)]
        {:line line :source source}))

(defn get-line-info-from-stacktrace
  "Takes a stacktrace and returns the first element with invokeStatic
   that's not in the excluded namespaces.
   Returns a map (possibly partially) filled in
   with this info. Non-found fields are mapped to nil."
  [tr]
  (let [[_ _ source line] (sp/select-first [sp/ALL allowed-ns-invoke-static?] tr)]
       {:line line :source source}))

(defn- or-empty-str
  [x]
  (or x ""))

(defn- fn-name-trim
  [f]
  (let [[_ name _] (re-matches #"(\w*)__(.+)" f)]
       (or name f)))

(defn get-name-from-tr-element
  "Takes a function name as it appears in a stack trace
   and returns the actual function name"
  [tr-fn]
  (-> tr-fn
      str
      (s/split #"\$")
      second
      or-empty-str
      d/fn-name-or-anonymous
      fn-name-trim
      d/replace-special-symbols))

(defn get-namespace-from-tr-element
  "Takes a function name as it appears in a stack trace
   and returns its namespace"
  [tr-fn]
  (-> tr-fn
      (s/split #"\$")
      first
      or-empty-str))

(defn- calling-fn?
  [tr-elt]
  (let [tr-s (str (first tr-elt))]
       (or (not (s/starts-with? tr-s "clojure.lang"))
           (s/starts-with? tr-s "clojure.lang.Numbers"))))

(defn get-fname-from-stacktrace
  "Takes the stacktrace and attempts to find the function name
   in the first element that's not clojure.lang"
   [tr]
   (let [tr1 (sp/select-first [sp/ALL calling-fn?] tr)]
        (if (= (str (first tr1)) "clojure.lang.Numbers")
            (str (second tr1))
            (get-name-from-tr-element (first tr1)))))

(defn- handle-temp-name
  [f]
  (if (re-matches #"form-init(\d+)\.clj" f)
      "Clojure interactive session"
      (str "file " f)))

(defn- file-name
  "Takes a file name that may be an absolute path as a string and returns
   the file name without the path."
  [f]
  (->> f
       clojure.java.io/file
       .getName
       handle-temp-name))

(defn- location-format
  [s]
  (-> s
      s/trim
      (str ".")))

(defmulti location->str
  (fn [{:keys [source]}] source))

(defmethod location->str :read-source
   [{:keys [line column]}]
    (if (or line column)
        (location-format (str "Found while reading position "
                         column
                         " of line "
                         line
                         " in a dynamic expression"))
        "."))

(defmethod location->str nil
   [{:keys [line column]}]
   (if (or line column)
       (location-format (str "Found while reading position "
                        column
                        " of line "
                        line
                        " in a dynamic expression"))
       "."))

(defmethod location->str :default
   [{:keys [source line column]}]
   (let [f (str "In " (file-name source) " ")
         l (if line (str "on line " line " ") "")
         c (if column (str "at position " column) "")]
        (location-format (str f l c))))

;; ########################################################
;; ########## Utils for stacktrace processing #############
;; ########################################################

(defn- s->pattern
  "Takes a string and returns a string that represents the corresponding
   regex pattern: replaces * by (.*) and quotes all other strings."
  [s]
  (cond (= "*" s) "(.*)"
        :else (java.util.regex.Pattern/quote s)))

;; Might add more regex encoding later
(defn- ns->regex
  "Takes a string that describes a regex. Creates the corresponding
   regex, replacing * by (.*), and . by \\."
  [s]
  (->> s
       (into [])
       (partition-by #(= % \*))
       (map #(apply str %))
       (map s->pattern)
       (apply str)
       re-pattern))

;; Encoded regexes for stacktrace filtering. For convenience
;; . is taken literally, but * denotes (.*)
(def excluded-ns-for-stacktrace #{
    "nrepl.middleware.*" "clojure.spec.*" "clojure.core.protocols*"
    "clojure.core$transduce*" "*.reflect.*" "clojure.core$read"
    "clojure.main$repl$*" "clojure.core$cast"
    "clojure.core$print_sequential" "clojure.core$pr_on"
    ; map appears in the stacktrace of :print-eval-result errors:
    "clojure.core$map$fn__*$fn__*"
    ;; Java:
    "java.lang.*" "java.util.*" "java.io.*"
    ;; Our own:
    "babel.processor*" "errors.*" "babel.middleware$interceptor*"})

(def excluded-ns-regex-from-strings (sp/transform [sp/ALL] ns->regex excluded-ns-for-stacktrace))

(def excluded-ns-regex-explicit #{#"(.*)\$eval(\d+)" #"clojure\.core\$fn__(\d+)"
                                  ;; Excluding all Clojure.lang except clojue.lang.Numbers,
                                  ;; in two steps for easier understanding:
                                  ;; those that don't start with "N" and those that start with "Na":
                                  #"clojure\.lang\.[^\\N](.*)" #"clojure\.lang\.Na(.*)"})

(def excluded-ns-regex (clojure.set/union excluded-ns-regex-from-strings
                                          excluded-ns-regex-explicit))

(def excluded-ns-after-compile (sp/transform [sp/ALL] ns->regex
                                             #{"clojure.core$eval"}))

(def excluded-ns-after-compiler (clojure.set/union excluded-ns-regex
                                                   excluded-ns-after-compile))

(defn- before-compiler?
  [tr-elt]
  (not= "clojure.lang.Compiler" (str (first tr-elt))))

(defn- trace-elt-included?
  "Takes a trace element and returns true if it is included into
   a filtered stacktrace and false otherwise."
  [excl-set tr-elt]
  (not-any? #(re-matches % (str (first tr-elt))) excl-set))

(defn- duplicates?
  "Takes two stacktrace elements, returns true if they are duplicates
   (i.e. have the same name, the same file, and differ only in invoke
   vs invokeStatic and possibly in a line number)"
   [[name1 m1 file1 line1] [name2 m2 file2 line2]]
   (let [n1 (str name1)
         n2 (str name2)]
         (and (or (s/starts-with? n1 n2) (s/starts-with? n2 n1))
              (= file1 file2)
              (or (not= m1 m2) (not= line1 line2)))))

(defn remove-duplicates
  "Takes the stack trace and removes multiple different references to
   the same function in a row. Keeps the exact same references to preserve stacktrace
   for infinite recursion."
  [trace]
  (if (empty? trace)
      []
      (loop [tr (rest trace) result [(first trace)]]
            (cond (= (count tr) 0) result
                  (duplicates? (first tr) (last result))
                      (recur (rest tr) result)
                  :else (recur (rest tr) (conj result (first tr)))))))

(defn- filter-out-assert-fdecl
  "Takes an exception stacktrace and removes everything that follows
   assert_valid_fdecl"
  [trace]
  (first (split-with #(not= "assert_valid_fdecl" (get-name-from-tr-element %))
                     trace)))

(defn filter-stacktrace
  "Takes an exception stacktrace and filters out unneeded elements."
  [trace]
  (let [[tr1 tr2] (split-with before-compiler? trace)]
       (->> (into (sp/select [sp/ALL (partial trace-elt-included? excluded-ns-regex)] tr1)
                  (sp/select [sp/ALL (partial trace-elt-included? excluded-ns-after-compiler)] tr2))
             filter-out-assert-fdecl
             remove-duplicates)))

(defn- format-tr-element
  "Takes a stack trace element with the function name and the namespace
   already extracted and all fields passed as strings.
   Returns the string as it will be printed to the user."
  [[name nspace method file line]]
  (cond (and (= "anonymous function" name) (= "Clojure interactive session" file))
             (str "[An anonymous function called dynamically]")
        (= name "anonymous function")
             (str "[An anonymous function called in " file " on line " line "]")
        (= name "repl")
             (str "[Clojure interactive session (repl)]")
        (= "Clojure interactive session" file)
             (str "[" name "(ns:" nspace ") called dynamically]")
        (= "clojure.lang.Numbers" nspace)
             ;; For clojure.lang.Numbers the function name is in the method part,
             ;; e.g. [clojure.lang.Numbers inc "Numbers.java" 137]
             (str "[" method " (ns:" nspace ") called in " file " on line " line "]")
        :else (str "[" name " (ns:" nspace ") called in " file " on line " line "]")))

(defn- stacktr-or-empty
  [s]
  (if (= s "") "" (str "Call sequence:\n" s)))

(defn format-stacktrace
  "Takes a (filtered) stacktrace, returns it as a string to be printed"
  [trace]
  (->> trace
      (sp/transform [sp/ALL (sp/collect sp/FIRST)]
                    #(into [(first %1)] %2))
      (sp/transform [sp/ALL sp/ALL] str)
      (sp/multi-transform [sp/ALL (sp/multi-path [(sp/nthpath 0) (sp/terminal get-name-from-tr-element)]
                                                 [(sp/nthpath 1) (sp/terminal get-namespace-from-tr-element)]
                                                 [(sp/nthpath 3) (sp/terminal handle-temp-name)])])
      (sp/transform [sp/ALL] format-tr-element)
      (interpose "\n")
      (apply str)
      stacktr-or-empty))
