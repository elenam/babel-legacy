(ns errors.utils
  (:require [clojure.string :as s]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;; Utilities for handling macro specs ;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn with-space-if-needed
  [val-str]
  (if (= val-str "") "" (str " " val-str)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;; Predicates for handling fn ;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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
  "Takes a value of a failing spec and returns true if the value has
  & in it and false otherwise"
  [value]
  (and (vector? (first value)) (not (empty? (filter #(= % (symbol '&)) (first value))))))

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
   []{:a 1, :n 0} {:b 0, :n 1} {:c 5, :n 2}].
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

;; TODO: generalize to multiple args?
(defn same-position
  "Takes two spec problems and returns true if their 'in' is exactly the same
   and false otherwise"
  [p1 p2]
  (= (:in p1) (:in p2)))

(defn prefix-position
  "Takes two spec problems and returns true if the 'in' of the first one is a
   proper prefix of the second one and false otherwise."
  [p1 p2]
  (let [in1 (:in p1)
        in2 (:in p2)]
       (and (< (count in1) (count in2)) (reduce #(and %1 %2) (map = in1 in2)))))



; (defn max-depth-fail
;   "Takes a vector of failed specs and returns the number of the spec
;    with the largest depth"
;   [problems]
;   (second (first (reverse (sort #(< (first %1) (count (:in (first %2))))) (seq (zipmap problems (range))))))
