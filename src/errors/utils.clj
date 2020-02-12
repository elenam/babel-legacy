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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;; Predicates for handling fn ;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn same-position
  "Takes two spec problems and returns true if their 'in' is exactly the same
   and false otherwise"
  [p1 p2]
  (= (:in p1) (:in p2)))

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

(defn fn-multi-arity?
  "Takes a value of a failing spec and returns true if the fn has
  more than one arity and false otherwise"
  [value]
  (let [named? (fn-named? value)
        n (count value)]
       (or (and named? (> n 2) (every? seq? (rest value)))
           (and (not named?) (> n 1) (every? seq? value)))))

(defn fn-has-amp?
  "Takes a value of a failing spec and the 'in' index and returns true if the
  value at the index has & in it and false otherwise"
  [value in]
  (and (vector? (nth value in)) (not (empty? (filter #(= % (symbol '&)) (nth value in))))))

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

(defn missing-vector-message
  [grouped-probs value]
  (if (= 1 (count grouped-probs))
      (let [no-vectors? (empty? (filter vector? value))
            val (:val (first (first (vals grouped-probs))))] ; replace by specter?
            (if no-vectors?
                (str "A function definition requires a vector of parameters, but was given "
                     (if (nil? val) "nil" (d/print-macro-arg val)) " instead.")
                ;; Perhaps should report the failing argument
                "fn is missing a vector of parameters or it is misplaced."))
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
            "fn is missing a vector of parameters or it is misplaced.")))

(defn- not-names->seq
  [val]
  (cond
    (seq? val) (filter #(not (symbol? %)) val)
    (nil? val) '(nil)
    :else '()))

(defn- not-names->str
  [val]
  (let [s (not-names->seq val)
        names-str (s/join ", " (map #(if (nil? %) "nil" (d/print-macro-arg %)) s))]
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
        has-vector? (some vector? before-n)
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
              (= pred 'clojure.core/vector?)
                 (str clause-str
                      "A function definition requires a vector of parameters, but was given "
                      (d/print-macro-arg val)
                      " instead.")
              :else (str clause-str
                         (d/print-macro-arg val)
                         " cannot be outside of a function body."))))

(defn clause-number
  "Takes a vector of failed 'in' entries from a spec error and returns the max one.
   If none available, returns 0."
  [ins]
  (let [valid-ins (filter number? (map first ins))]
       (if (empty? valid-ins) 0 (apply max valid-ins))))

(defn label-vect-maps
  "Takes a vector of maps and returns it with an extra key/val pair added to each entry:
  :n and the index in the original vector.
   For instance, given [{:a 1} {:b 0} {:c 5}] it returns
   [{:a 1, :n 0} {:b 0, :n 1} {:c 5, :n 2}].
   Helpful for subsequent sorting since it preserves the index in the original vector."
  [v-maps]
  (mapv #(assoc %1 :n %2) v-maps (range)))

(defn cmp-spec
  "A comparison function for two spec fails. Compares first by the arity clause,
  then by depth (in both cases higher values first), then by the position in
  the 'problems' vector (higher numbers last, i.e. preserving the given order)."
  [p1 p2]
  (let [in1 (:in p1)
        in2 (:in p2)
        clause1 (or (first in1) -1)
        clause2 (or (first in2) -1)
        depth1 (if (sequential? in1) (count in1) 0)
        depth2 (if (sequential? in2) (count in2) 0)]
        (cond
          (not (= depth1 depth2)) (- depth2 depth1) ; Depth first, as it is more precise.
          (not (= clause1 clause2)) (- clause2 clause1)
          :else (- (:n p1) (:n p2)))))

(defn sort-by-clause
  "Sorts spec-failures according to cmp-spec."
  [probs]
  (sort cmp-spec probs))


(defn prefix-position
  "Takes two spec problems and returns true if the 'in' of the first one is a
   proper prefix of the second one and false otherwise."
  [p1 p2]
  (let [in1 (:in p1)
        in2 (:in p2)]
       (v-prefix? in1 in2)))

(defn arity-n?
  [{:keys [path]}]
  (v-prefix=? [:fn-tail :arity-n :params] path))

;; Can be used to tell whether a clause is falsely identified,
;; in particular confused with a function call.
(defn clause?
  "Takes an element of value and returns true if it is a function clause
   and false otherwise."
  [s]
  (and (seq? s) (or (empty? s) (vector? (first s)))))

(defn pred-vector?
  "Takes a predicate of a spec failure and returns true if it is clojure.core/vector?
  and false otherwise."
  [pred]
  (and (symbol? pred) (= #'clojure.core/vector? (resolve pred))))

(defn pick-vector-fail
  "Takes a list of spec failures and returns the message and the value to
   report for a failed parameter vector for fn"
  [probs]
  (let [vector-fails (filter #(and (pred-vector? (:pred %))
                                   (v-prefix=? [:fn-tail :arity-1 :params] (:path %))) probs)]
       (first vector-fails)))

(defn multi-clause
  [probs]
  (let [vector-probs (pick-vector-fail probs)
        val (:val vector-probs)]
       (cond (clause? val) (str "Detected multi-clause fn; issue with " (d/print-macro-arg val))
             (#{"fn*" "quote"} (str (first val))) (str "A function definition requires a vector of parameters, but was given " (d/print-macro-arg val) " instead.")
             :else (str "A function definition requires a vector of parameters, but was given " (d/print-macro-arg val "(" ")") " instead."))))

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
