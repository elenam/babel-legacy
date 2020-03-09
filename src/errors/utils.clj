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
    (seq? val) (filter #(not (symbol? %)) val)
    (nil? val) '(nil)
    :else '()))

(defn- print-with-nil-and-seq
  [val]
  (cond
    (nil? val) "nil"
    (and (seq? val) (not (#{"fn*" "quote"} (str (first val))))) (d/print-macro-arg val "(" ")")
    :else (d/print-macro-arg val)))

(defn- not-names->str
  [val]
  (let [s (not-names->seq val)
        names-str (s/join ", " (map print-with-nil-and-seq s))]
        (if (= 1 (count s))
            (str names-str " is not a name.")
            (str names-str " are not names."))))

(defn- ampersand-issues
  [value in]
  (let [val-in (first (sp/select [(apply sp/nthpath (drop-last in))] value))
        [before-amp amp-and-after] (split-with (complement #{'&}) val-in)
        has-amp? (not (empty? amp-and-after))
        not-names-before-amp (filter #(not (symbol? %)) before-amp)
        count-after-amp (dec (count amp-and-after))
        length-issue-after-amp? (not= count-after-amp 1)
        not-allowed-after-amp (or (not (symbol? (second amp-and-after))) (= '& (second amp-and-after)))]
        (if (and has-amp? (empty? not-names-before-amp) (or length-issue-after-amp? not-allowed-after-amp))
            (rest amp-and-after)
            '())))

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
                          (d/print-macro-arg (seq (sp/select-one (sp/srange start-clause (inc clause-n)) value)))
                          " instead.")
               no-vectors?
                    (str "A function definition requires a vector of parameters, but was given "
                         (if (nil? val) "nil" (d/print-macro-arg val)) " instead.")
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
            (if (#{"fn*" "quote"} (str (first val)))
                (str "A function definition requires a vector of parameters, but was given " (d/print-macro-arg val) " instead.")
                (str "A function definition requires a vector of parameters, but was given " (d/print-macro-arg val "(" ")") " instead."))
            ;; Perhaps should report the failing argument
            "The function definition is missing a vector of parameters or it is misplaced.")))

(defn parameters-not-names
  [prob value]
  (let [{:keys [val in]} prob
        amp-issues (ampersand-issues value in)]
        (cond (not (empty? amp-issues))
                (str "& must be followed by exactly one name, but is followed by "
                     (d/print-macro-arg amp-issues)
                     " instead.")
          :else (str "Parameter vector must consist of names, but "
                (not-names->str val)))))

(defn clause-single-spec
  [prob value]
  (let [{:keys [reason val pred in path]} prob
        clause-n (first in)
        before-n (take clause-n value)
        clause-val (nth value clause-n)
        named? (symbol? (first value))
        start-clause (if named? 1 0)
        has-vector? (some vector? before-n)
        multi-clause? (every? sequential? (drop start-clause before-n))
        clause-to-report (if named? (dec clause-n) clause-n)
        clause-str (if (> clause-to-report 0)
                       (str "The issue is in the " (d/position-0-based->word clause-to-report) " clause.\n")
                       "")
        amp-issues (ampersand-issues value in)]
        (cond has-vector? "fn needs a vector of parameters and a body, but has something else instead."
              (and (= "Extra input" reason) (not (empty? amp-issues)))
                  (str clause-str
                       "& must be followed by exactly one name, but is followed by "
                       (d/print-macro-arg amp-issues)
                       " instead.")
              (= "Extra input" reason)
                 (str clause-str
                      "Parameter vector must consist of names, but "
                      (not-names->str val))
              (and (= pred 'clojure.core/vector?) (vector? clause-val))
                 (str clause-str
                      "A function clause must be enclosed in parentheses, but is a vector "
                      (d/print-macro-arg clause-val "[" "]")
                      " instead.")
              (and (= pred 'clojure.core/vector?) (#{"fn*" "quote"} (str val)))
                 (str clause-str
                      (d/print-macro-arg clause-val)
                      " cannot be outside of a function body.")
              (and (= pred 'clojure.core/vector?) (> clause-n start-clause) (not multi-clause?))
                 (str clause-str
                      "A function definition requires a vector of parameters, but was given "
                      (d/print-macro-arg (sp/select-one (sp/srange start-clause (inc clause-n)) value))
                      " instead.")
              (= pred 'clojure.core/vector?)
                 (str clause-str
                      "A function definition requires a vector of parameters, but was given "
                      (d/print-macro-arg val)
                      " instead.")
              :else (str clause-str
                         (d/print-macro-arg val)
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
        (str "Function parameters must be a vector of names, but " (d/print-macro-arg val) " was given instead.")))

;; ########################################################
;; ######## Utils for stack trace processing ##############
;; ########################################################

(def excluded-ns #{"clojure.core" "clojure.string" "clojure.lang"
                   "clojure.main" "nrepl.middleware"})

(defn allowed-ns?
  "Takes a stacktrace element of an exception
   and returns true if its first element doesn't start
   with any of the excluded namespaces, and false otherwise"
  [s]
  (let [ns-element (str (first s))]
       (every? #(not (s/starts-with? ns-element %)) excluded-ns)))

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
             (every? #(not (s/starts-with? ns-element %)) excluded-ns))))

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
         source :clojure.error/source} (sp/select-first [sp/ALL #(not (empty? %))] v)]
        {:line line :column column :source source}))

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

(defn location->str
  "Takes a map of :source, :line, :column. Returns the string to be
   printed for error location"
  [{:keys [source line column]}]
  (let [s (if source (str "In file " source  " ") "")
        l (if line (str "on line " line " ") "")
        c (if column (str "at position " column) "")]
        (-> (str s l c)
             s/capitalize
             s/trim
             (str "."))))
