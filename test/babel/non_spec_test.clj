(ns babel.non-spec-test
  (:require
   [expectations :refer :all]
   [logs.utils :as log]))

;#########################################
;### Tests for errors that aren't      ###
;### spec failures                     ###
;#########################################

;; TO RUN tests, make sure you have repl started in a separate terminal

;start logging
(log/start-log)

(def to-log? true)

(expect #(not= % nil) (log/set-log to-log?))

(expect nil (log/add-log
              (do
                (def file-name "this file")
                (:file (meta #'file-name)))))

(expect "Tried to divide by zero" (log/babel-test-message "(/ 70 0)"))

(expect "Tried to divide by zero" (log/babel-test-message "(/ 70 8 0)"))

(expect "" (log/babel-test-message "(* 3 0)"))

(expect "" (log/babel-test-message "#(+ 1)"))

(expect "" (log/babel-test-message "(- 2)"))

;; 3-argument usage of 'into' has a transducer as its second argument. {} is a function
;; so it passes the type check, but throws a null pointer exception when applied.
(expect "An attempt to access a non-existing object (NullPointerException)." (log/babel-test-message "(into [] {} \"a\")"))

;(expect "Expected a number, but a sequence was given instead." (log/babel-test-message "(defn greater-than-zero [x] (> x 0)) (take (range) (range))"))

;(expect "The second argument of (map f f) was expected to be a sequence but is a function f instead." (log/babel-test-message "(defn f [x] (+ x 2)) (map f f)"))

;(expect "Tried to divide by zero" (log/babel-test-message "(defn not-divide-zero [x] (/ x 0))"))

;(expect "Expected a number, but a string was given instead." (log/babel-test-message "(defn not-divide-zero [x] (/ x \"a\"))"))

(expect #"(?s)There is an unmatched delimiter ]\.(.*)" (log/babel-test-message "(+ (])"))

(expect #"(?s)You have a key that's missing a value; a hashmap must consist of key/value pairs\.(.*)" (log/babel-test-message "{9 8 7}"))

(expect "The format of the number 8.5.1 is invalid." (log/babel-test-message "8.5.1"))

(expect "Wrong number of args (1) passed to: anonymous function" (log/babel-test-message "(map #(7) [0])"))

(expect "Wrong number of args (2) passed to: anonymous function" (log/babel-test-message "(map #(+ %1) [9] [0])"))

(expect "Wrong number of args (2) passed to: f" (log/babel-test-message "(defn f[x] (inc x)) (f 5 6)"))
