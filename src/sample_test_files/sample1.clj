(ns sample-test-files.sample1
  (:require
   [corefns.corefns]))

;; #####################################################
;; ########### Test functions for location info ########
;; #### DO NOT MOVE: it would change line numbers ######
;; #####################################################

(defn div0-test
  []
  (/ 5 0))

(defn take-test
  []
  (take 4 5))

(defn null-ptr-test
  []
  ((first [])))

(defn take-lazy-test
  []
  (take (range) (range)))

(defn map-spec-test
  []
  (map map map))

(defn f
  [s]
  (f (str (repeat 1000 s) (repeat 1000 s))))

(defn out-of-memory-test
  []
  (f "There goes memory!!!"))

(defn compare-char-test
  []
  (compare \a "a"))

(defn f1
  [x y]
  [x y])

(defn arity-defn-test
  []
  (f1 2))

(defn div-0-in-map-test
  []
  (map #(/ 6 %) (reverse (range 3))))

(defn spec-in-filter-test
  []
  (filter even? "strawberry"))
