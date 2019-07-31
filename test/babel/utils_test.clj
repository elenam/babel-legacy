(ns babel.utils-test
  (:require
    [errors.dictionaries :refer :all]
    [logs.utils :as log]
    [expectations :refer :all]))

;#########################################
;### Tests for utilties functions      ###
;### for error processing              ###
;#########################################

;; TO RUN tests, make sure you have repl started in a separate terminal
(expect "true" (log/babel-test-message "(greater-than-zero 9)"))
