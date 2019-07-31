(ns babel.spec-test
  (:require
    [logs.utils :as log]
    [expectations :refer :all]))

;############################################
;### Tests for functions that have specs  ###
;############################################

;; TO RUN tests, make sure you have repl started in a separate terminal
(expect "The second argument of (take 9 8) was expected to be a sequence but is a number 9 instead."
(log/babel-test-message "(take 9 9)"))
