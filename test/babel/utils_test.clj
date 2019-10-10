(ns babel.utils-test
  (:require
    [errors.dictionaries :refer :all]
    [logs.utils :as log]
    [babel.processor :refer :all]
    [expectations :refer :all]))

;#########################################
;### Tests for utilties functions      ###
;### for error processing              ###
;#########################################

;; TO RUN tests, make sure you have repl started in a separate terminal

(expect "5" (print-macro-arg 5))
(expect map (print-macro-arg 'map))
(expect "\"abc\"" (print-macro-arg '"abc"))
(expect "\"a\"" (print-macro-arg '("a")))
(expect "+ 2 3 4 5" (print-macro-arg '(+ 2 3 4 5)))
(expect "[2 3 4 5]" (print-macro-arg '(2 3 4 5) "[" "]"))
(expect "+ (* 2 3) 9 (/ 8 9)" (print-macro-arg '(+ (* 2 3) 9 (/ 8 9))))
(expect "(+ 7 8) (* 2 3) 9" (print-macro-arg '((+ 7 8) (* 2 3) 9)))
(expect "((+ 7 8))" (print-macro-arg '(((+ 7 8)))))
