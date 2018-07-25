(ns babel.dictionary_tests
  (:require
    [errors.dictionaries :refer :all]
    [expectations :refer :all]))

;#########################################
;### Tests for supplementary functions ###
;### in errors.dictionaries            ###
;#########################################

(expect "inc" (remove-inliner "inc--inliner--5258"))
(expect "inc" (remove-inliner "inc--inliner"))
(expect "inc" (remove-inliner "inc--5258"))
(expect "inc5258" (remove-inliner "inc5258"))
(expect "inc-5258" (remove-inliner "inc-5258"))

(expect "days" (get-function-name "happy$days"))
(expect "days" (get-function-name "happy/days"))
(expect "first" (get-function-name "clojure.lang.RT.first"))
(expect "first" (get-function-name "clojure.lang.RT.first--5678"))
(expect "somethingElse" (get-function-name "somethingElse"))
