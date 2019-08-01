(ns babel.spec-test
  (:require
    [logs.utils :as log]
    [expectations :refer :all]))

;############################################
;### Tests for functions that have specs  ###
;############################################

;; TO RUN tests, make sure you have repl started in a separate terminal
; (expect "The second argument of (take 9 8) was expected to be a sequence but is a number 9 instead."
; (log/babel-test-message "(take 9 9)"))

(expect nil (log/add-log
              (do
                (def file-name "this file")
                (:file (meta #'file-name)))))

(expect "The second argument of (map map map) was expected to be a sequence but is a function map instead."
(log/babel-test-message "(map map map)"))

(expect "The second argument of (conj {} \"a\") was expected to be a sequence of vectors with only 2 elements or a map with key-value pairs but is a string \"a\" instead."
(log/babel-test-message "(conj {} \"a\")"))

(expect "The second argument of (conj {} []) was expected to be a sequence of vectors with only 2 elements or a map with key-value pairs but is a vector [] instead."
(log/babel-test-message "(conj {} [])"))

;returns null pointer exception for some reason (conj "lijk" "jlksdfj")
;same thing (conj 1 "a")
;(into [] {} "a") gives null pointer
;(take "apple" "banana") gives null pointer

(expect "The first argument of (conj 1 \"a\") was expected to be a map but is a number 1 instead." (log/babel-test-message "(conj 1 \"a\")" ))

(expect "The second argument of (partition 1 1 1 1) was expected to be a sequence but is a number 1 instead." (log/babel-test-message "(partition 1 1 1 1)"))

(expect "The second argument of (partition 1 1 1) was expected to be a sequence but is a number 1 instead." (log/babel-test-message "(partition 1 1 1)"))

;(expect "The first argument of (into 1 clojure.spec.test.alpha$spec_checking_fn$fn__3026@4b0e572e) was expected to be a sequence but is a number 1 instead." (log/babel-test-message "(into 1 even?)"))

(expect "The first argument of (contains? :a :a) was expected to be a sequence but is a keyword :a instead." (log/babel-test-message "(contains? :a :a)"))

(expect "Wrong number of arguments, expected in (contains? {} \"a\" #{}): the function contains? expects two arguments but was given 3 arguments" (log/babel-test-message "(contains? {} \"a\" #{})"))

(expect "Expected a function, but a number was given instead." (log/babel-test-message "(drop 3 (1 2 3))")) ;fails

(expect "The second argument of (drop 1 :a) was expected to be a sequence but is a keyword :a instead." (log/babel-test-message "(drop 1 :a)"))

(expect "The second argument of (get-in [1] \"a\") was expected to be a sequence but is a string \"a\" instead." (log/babel-test-message "(get-in [1] \"a\")"))

(expect "Expected a number, but a sequence was given instead." (log/babel-test-message "(mod (range 5) (range 10))"))

(expect "The first argument of (reduce 4 \"strawberry\") was expected to be a function but is a number 4 instead." (log/babel-test-message "(reduce 4 \"strawberry\")"))

(expect "Expected a number, but a character was given instead." (log/babel-test-message "(map #(> % 5) \"strawberry\")"))

; (expect "" (log/babel-test-message ""))
;
; (expect "" (log/babel-test-message ""))
;
; (expect "" (log/babel-test-message ""))
