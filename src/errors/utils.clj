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
  and false otherwise"
  [value]
  (and (seq? value) (simple-symbol? (first value))))

(defn fn-multi-arity?
  "Takes a value of a failing spec and returns true if the fn has
  more than one arity and false otherwise"
  [value]
  (let [named? (fn-named? value)
        n (count value)]
       (or (and named? (> n 2)) (and (not named?) (> n 1)))))

(defn fn-has-amp?
  "Takes a value of a failing spec and returns true if the value has
  & in it and false otherwise"
  [value]
  (and (vector? (first value)) (not (empty? (filter #(= % (symbol '&)) (first value))))))
