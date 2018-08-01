(ns babel.error-tests
  (:require
   [expectations :refer :all])
  (:use
   [loggings.loggingtool :only [get-error start-log add-log]]))

;;you need to have launched a nREPL server in babel for these to work.
;;this must be the same port specified in project.clj

;;start logging
(start-log)
(expect nil (add-log
              (do
                (def file-name "this file")
                (:file (meta #'file-name)))))


;;test non erroring commands
(expect  nil (get-error "(+ 5 8)"))
(expect  nil (get-error "(prn \"error\")"))
(expect  nil (get-error "(take 5 (filter #(> 8 %) (repeatedly #(rand-int 10))))"))

;;arithmetic-exception-divide-by-zero
(expect "Tried to divide by zero\n" (get-error "(/ 70 0)"))

(expect "Tried to divide by zero\n" (get-error "(/ 70 8 0)"))

;;compiler-exception-cannot-resolve-symbol
(expect "Name smoked-cod is undefined.\n" (get-error "(smoked-cod)"))
(expect "Name Ebeneezer is undefined.\n" (get-error "(Ebeneezer)"))

;;class-cast-exception
;(expect "Attempted to use a string, but a number was expected" (get-error "(+ 8 \"seventeen\")"));;will not work until we write specs for core functions

;; NullPointerException with an object given
;; This might not be what we want (might need to process the object), but that's what it currently is
(expect "An attempt to access a non-existing object:   java.util.regex.Pattern.<init> (Pattern.java:1350)\r\n (NullPointerException).\n"
        (get-error "(re-pattern nil)"))
