(ns babel.fn-defn-test
  (:require
   [expectations :refer :all]
   [logs.utils :as log]))

;#############################################
;### Tests for fn and defn (specced macros)###
;#############################################

;; TO RUN tests, make sure you have repl started in a separate terminal

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
A function definition requires a vector of parameters, but was given 6 instead."
(log/babel-test-message "(fn a 6)"))

;; Note: the name is optional, so the message may be misleading
(expect "Syntax problems with (fn a b):
A function definition requires a vector of parameters, but was given b instead."
(log/babel-test-message "(fn a b)"))

(expect "Syntax problems with (fn {x y}):
A function definition requires a vector of parameters, but was given {x y} instead."
(log/babel-test-message "(fn {x y})"))

(expect "Syntax problems with (fn '(x y)):
A function definition requires a vector of parameters, but was given '(x y) instead."
(log/babel-test-message "(fn '(x y))"))

(expect "Syntax problems with (fn a '([])):
A function definition requires a vector of parameters, but was given '([]) instead."
(log/babel-test-message "(fn a '([]))"))

(expect "Syntax problems with (fn a b []):
fn is missing a vector of parameters or it is misplaced."
(log/babel-test-message " (fn a b [])"))

(expect "Syntax problems with (fn 4 []):
fn is missing a vector of parameters or it is misplaced."
(log/babel-test-message "(fn 4 [])"))

(expect "Syntax problems with (fn a #(= %5) [] #\"hi\" ['(8 9)]):
fn is missing a vector of parameters or it is misplaced."
(log/babel-test-message "(fn a #(= %5) [] #\"hi\" ['(8 9)])"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; fn: parameter vector has non-names ;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect "Syntax problems with (fn [5] {7 \"hello\"}):
Parameter vector must consist of names, but 5 is not a name."
(log/babel-test-message "(fn [5] {7 \"hello\"})"))

(expect "Syntax problems with (fn [5 x] {7 \"hello\"}):
Parameter vector must consist of names, but 5 is not a name."
(log/babel-test-message "(fn [5 x] {7 \"hello\"})"))

(expect "Syntax problems with (fn [x 5 y] {7 \"hello\"}):
Parameter vector must consist of names, but 5 is not a name."
(log/babel-test-message "(fn [x 5 y] {7 \"hello\"})"))

(expect "Syntax problems with (fn [x #(+ %1) y] 2):
Parameter vector must consist of names, but #(+ %1) is not a name."
(log/babel-test-message "(fn [x #(+ %) y] 2)"))

(expect "Syntax problems with (fn [x #(+ %1) 3] 2):
Parameter vector must consist of names, but #(+ %1), 3 are not names."
(log/babel-test-message "(fn [x #(+ %) 3] 2)"))

(expect "Syntax problems with (fn ['(2 3) 5]):
Parameter vector must consist of names, but '(2 3), 5 are not names."
(log/babel-test-message "(fn ['(2 3) 5])"))

;; TODO: check how it works with multiple arities of fn
