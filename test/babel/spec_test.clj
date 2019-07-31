(ns babel.spec-test
  (:require
    [logs.utils :as log]
    [expectations :refer :all]))

;############################################
;### Tests for functions that have specs  ###
;############################################

;; TO RUN tests, make sure you have repl started in a separate terminal

(expect "The second argument of (map map map) was expected to be a sequence but is a function map instead."
        (log/babel-test-message "(map map map)"))
