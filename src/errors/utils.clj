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
  "Takes a vector of maps and returns it as a sequence with an extra
   key/val pair added to each entry: :n and the index in the original vector.
   For instance, given [{:a 1} {:b 0} {:c 5}] it returns
   ({:a 1, :n 0} {:b 0, :n 1} {:c 5, :n 2}).
   Helpful for subsequent sorting since it preserves the index in the original vector."
  [v-maps]
  (map #(assoc %1 :n %2) v-maps (range)))

;; Not quite what I want, perhaps group-by? Also need to deal with the nil values
;; Need to select largest depth and highest clause (and handle nil)
(defn sort-by-clause
  "TODO"
  [probs]
  (sort #(> (first (:in %1)) (first (:in %2))) (filter #(number? (:in %)) probs)))

; (defn max-depth-fail
;   "Takes a vector of failed specs and returns the number of the spec
;    with the largest depth"
;   [problems]
;   (second (first (reverse (sort #(< (first %1) (count (:in (first %2))))) (seq (zipmap problems (range))))))
