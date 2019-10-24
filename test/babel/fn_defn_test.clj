(ns babel.fn-defn-test
  (:require
   [expectations :refer :all]
   [logs.utils :as log]))

;#############################################
;### Tests for fn and defn (specced macros)###
;#############################################

;; TO RUN tests, make sure you have repl started in a separate terminal

;start logging
(log/start-log)

(def to-log? true)

(expect #(not= % nil) (log/set-log to-log?))

(expect nil (log/add-log
              (do
                (def file-name "this file")
                (:file (meta #'file-name)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;; fn: missing a vector  ;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect "Syntax problems with (fn a):
fn is missing a vector of parameters."
(log/babel-test-message "(fn a)"))

(expect "Syntax problems with (fn):
fn is missing a vector of parameters."
(log/babel-test-message "(fn)"))

(expect "Syntax problems with (fn 5 6):
A function definition requires a vector of parameters, but was given 5 instead."
(log/babel-test-message "(fn 5 6)"))

(expect "Syntax problems with (fn a 6):
A function definition requires a vector of parameters, but is given 6 instead."
(log/babel-test-message "(fn a 6)"))

;; Note: the name is optional, so the message may be misleading
(expect "Syntax problems with (fn a b):
A function definition requires a vector of parameters, but is given b instead."
(log/babel-test-message "(fn a b)"))

(expect "Syntax problems with (fn {x y}):
A function definition requires a vector of parameters, but is given {x y} instead."
(log/babel-test-message "(fn {x y})"))

(expect "Syntax problems with (fn '(x y)):
A function definition requires a vector of parameters, but is given '(x y) instead."
(log/babel-test-message "(fn '(x y))"))

(expect "Syntax problems with (fn 4 []):
NOT SURE WHAT THIS SHOULD BE: it has a vector, but 4 isn't a name."
(log/babel-test-message "(fn 4 [])"))

(expect "Syntax problems with (fn [5] {7 \"hello\"}):
parameter vector must consist of names, but 5 is not a name."
(log/babel-test-message "(fn [5] {7 \"hello\"})"))

(expect "Syntax problems with (fn [5 x] {7 \"hello\"}):
parameter vector must consist of names, but 5 is not a name."
(log/babel-test-message "(fn [5 x] {7 \"hello\"})"))

(expect "Syntax problems with (fn [x 5 y] {7 \"hello\"}):
parameter vector must consist of names, but 5 y has elements other than names."
(log/babel-test-message "(fn [x 5 y] {7 \"hello\"})"))

(expect "Syntax problems with  (fn a b []):
fn is missing a vector of parameters or it is misplaced."
(log/babel-test-message " (fn a b [])"))
