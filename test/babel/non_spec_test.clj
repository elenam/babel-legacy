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
(expect nil (log/add-log
              (do
                (def file-name "this file")
                (:file (meta #'file-name)))))

(expect "Tried to divide by zero" (log/babel-test-message "(/ 70 0)"))

(expect "Tried to divide by zero" (log/babel-test-message "(/ 70 8 0)"))

;(expect "Expected a number, but a sequence was given instead." (log/babel-test-message "(defn greater-than-zero [x] (> x 0)) (take (range) (range))"))

;(expect "The second argument of (map f f) was expected to be a sequence but is a function f instead." (log/babel-test-message "(defn f [x] (+ x 2)) (map f f)"))

;(expect "Tried to divide by zero" (log/babel-test-message "(defn not-divide-zero [x] (/ x 0))"))

;(expect "Expected a number, but a string was given instead." (log/babel-test-message "(defn not-divide-zero [x] (/ x \"a\"))"))
