(ns babel.non-spec-test
  (:require
   [expectations :refer :all]
   [logs.utils :as log]))

;#########################################
;### Tests for errors that aren't      ###
;### spec failures                     ###
;#########################################

;; TO RUN tests, make sure you have repl started in a separate terminal

(expect "Tried to divide by zero" (log/babel-test-message "(/ 70 0)"))

(expect "Tried to divide by zero" (log/babel-test-message "(/ 70 8 0)"))

(expect "Tried to divide by zero" (log/babel-test-message "(map map map)"))
