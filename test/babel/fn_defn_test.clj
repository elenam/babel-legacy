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
(log/babel-test-message "(fn a b [])"))

(expect "Syntax problems with (fn 4 []):
fn is missing a vector of parameters or it is misplaced."
(log/babel-test-message "(fn 4 [])"))

(expect "Syntax problems with (fn a #(= %5) [] #\"hi\" ['(8 9)]):
fn is missing a vector of parameters or it is misplaced."
(log/babel-test-message "(fn a #(= %5) [] #\"hi\" ['(8 9)])"))

(expect "Syntax problems with (fn &):
fn is missing a vector of parameters."
(log/babel-test-message "(fn &)"))

; & is a symbol, so this may be misleading:
(expect "Syntax problems with (fn & x):
A function definition requires a vector of parameters, but was given x instead."
(log/babel-test-message "(fn & x)"))

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

;; This is actually somewhat misleading since this would've been
;; correct as destructuring with keywords and names as map
;; elements
(expect "Syntax problems with (fn [{a :a 5 6}] [a x]):
Parameter vector must consist of names, but {a :a 5 6} is not a name."
(log/babel-test-message "(fn [{a :a 5 6}] [a x])"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; fn: nested parameter vector errors ;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect "Syntax problems with (fn [[5]] 6):
???"
(log/babel-test-message "(fn [[5]] 6)"))

(expect "Syntax problems with (fn [[[5]]] 6):
???"
(log/babel-test-message "(fn [[[5]]] 6)"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;; fn: map binding errors ;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect "Syntax problems with (fn [{a :a 5 :b}] [a x]):
Parameter vector must consist of names, but {a :a 5 :b} is not a name."
(log/babel-test-message "(fn [{a :a 5 :b}] [a x])"))

(expect "Syntax problems with (fn [[{a :a 5 x}]] [a x]):
???"
(log/babel-test-message "(fn [[{a :a 5 x}]] [a x])"))

(expect "Syntax problems with (fn [[[{a :a 5 x}]]] [a x]):
???"
(log/babel-test-message "(fn [[[{a :a 5 x}]]] [a x])"))

(expect "Syntax problems with (fn [[[{a :a 5 x}] 5]] [a x]):
???"
(log/babel-test-message "(fn [[[{a :a 5 x}] 5]] [a x])"))

(expect "Syntax problems with (fn [[[{a :a 5 x}] z]] [a x]):
???"
(log/babel-test-message "(fn [[[{a :a 5 x}] z]] [a x])"))

(expect "Syntax problems with (fn [x {:a 5}] 7):
Parameter vector must consist of names, but {:a 5} is not a name."
(log/babel-test-message "(fn [x {:a 5}] 7)"))

(expect "Syntax problems with (fn [[x] {:a 5}] 7):
Var-arg fn message, will look into later."
(log/babel-test-message "(fn [[x] {:a 5}] 7)"))

(expect "Syntax problems with (fn [[x] 6] 5):
Var-arg fn message, will look into later."
(log/babel-test-message "(fn [[x] 6] 5)"))

(expect "Syntax problems with (fn ([[x] 6])):
A function definition requires a vector of parameters, but was given [[x] 6] instead."
(log/babel-test-message "(fn ([[x] 6]))"))

(expect "Syntax problems with (fn a \"abc\"):
A function definition requires a vector of parameters, but was given \"abc\" instead."
(log/babel-test-message "(fn a \"abc\")"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;; fails spec for let, not fn ;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect "Syntax problems with (fn [[x] {:a 5}] [[y] 7]):
Fails let spec; might be fixed in spec2."
(log/babel-test-message "(fn [[x] {:a 5}] [[y] 7])"))

(expect "Syntax problems with (fn [[x] 6 z] [[x] 7]):
Fails let spec; might be fixed in spec2."
(log/babel-test-message "(fn [[x] 6 z] [[x] 7])"))

(expect "Syntax problems with (fn [[x] 6] [[x] 7]):
Fails let spec; might be fixed in spec2."
(log/babel-test-message "(fn [[x] 6] [[x] 7])"))

(expect "Syntax problems with (fn [[[x]] #(+ %1 %2)] [[x] 7]):
Fails let spec; might be fixed in spec2."
(log/babel-test-message "(fn [[[x]] #(+ %1 %2)] [[x] 7])"))

;; The spec failure doesn't contain the function name a
(expect "Syntax problems with (fn a [[[x]] :a] [[x] 7]):
Fails let spec; might be fixed in spec2."
(log/babel-test-message "(fn a [[[x]] :a] [[x] 7])"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; fn: more than one argument after & ;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(expect "Syntax problems with (fn [x & y z] 8):
???"
(log/babel-test-message "(fn [x & y z] 8)"))

(expect "Syntax problems with (fn [& y z] 8):
???"
(log/babel-test-message "(fn [& y z] 8)"))

(expect "Syntax problems with (fn [x & y 5] 8):
???"
(log/babel-test-message "(fn [x & y 5] 8)"))

(expect "Syntax problems with (fn [x & y & z] 8):
???"
(log/babel-test-message "(fn [x & y & z] 8)"))

(expect "Syntax problems with (fn [x & y & z & u] 8):
???"
(log/babel-test-message "(fn [x & y & z & u] 8)"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;; fn with & ;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect "Syntax problems with (fn [2 & y] 8):
Parameter vector must consist of names, but 2 is not a name."
(log/babel-test-message "(fn [2 & y] 8)"))

(expect "Syntax problems with (fn [x & 7] 8):
???"
(log/babel-test-message "(fn [x & 7] 8)"))

(expect "Syntax problems with (fn [5 & 7] 8):
Parameter vector must consist of names, but 5, 7 are not names."
(log/babel-test-message "(fn [5 & 7] 8)"))

(expect "Syntax problems with (fn [x & [5]] 2 3):
???"
(log/babel-test-message "(fn [x & [5]] 2 3)"))

(expect "Syntax problems with (fn [[x] & [5]] 2 3):
???"
(log/babel-test-message "(fn [[x] & [5]] 2 3)"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;; var-arg fn ;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect "Syntax problems with (fn ([x & y] 2 3) 5):
???"
(log/babel-test-message "(fn ([x & y] 2 3) 5)"))

(expect "Syntax problems with (fn ([x & y] 2 3) [5]):
???"
(log/babel-test-message "(fn ([x & y] 2 3) [5])"))

(expect "Syntax problems with (fn ([x & y] 2 3) [x]):
???"
(log/babel-test-message "(fn ([x & y] 2 3) [x])"))

(expect "Syntax problems with (fn ([x & y] 2 3) (x 3)):
???"
(log/babel-test-message "(fn ([x & y] 2 3) (x 3))"))

(expect "Syntax problems with (fn ([x & y] 2 3) ([5] 3)):
Parameter vector must consist of names, but 5 is not a name."
(log/babel-test-message "(fn ([x & y] 2 3) ([5] 3))"))

(expect "Syntax problems with (fn ([x & y] 2 3) ([& x y] 3)):
???"
(log/babel-test-message "(fn ([x & y] 2 3) ([& x y] 3))"))

(expect "Syntax problems with (fn ([x] 2 3) ([& x y] 3)):
???"
(log/babel-test-message "(fn ([x] 2 3) ([& x y] 3))"))

(expect "Syntax problems with (fn ([x] 2 3) ([& x y] 3)):
???"
(log/babel-test-message "(fn ([x] 2 3) ([& x y] 3))"))

(expect "Syntax problems with (fn a ([x] 2 3) ([] 8) ([& x y] 3)):
???"
(log/babel-test-message "(fn a ([x] 2 3) ([] 8) ([& x y] 3))"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;; fn non-spec error   ;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect #"(?s)Syntax error compiling at (.*)Name x is undefined\."
(log/babel-test-message "(fn [{a x}] [a])"))

(expect #"You have a key that's missing a value; a hashmap must consist of key/value pairs\.(.*)"
(log/babel-test-message "(fn [{a :a x}] [a x])"))

(expect #"The fn definition has two cases with the same number of arguments; only one case is allowed\.(.*)"
(log/babel-test-message "(fn ([x] 2 3) ([y] 3))"))

(expect #"The fn definition has two cases with the same number of arguments; only one case is allowed\.(.*)"
(log/babel-test-message "(fn ([x] 2 3) ([y] 3) ([x] 8))"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;; fn valid definitions (maps) ;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect nil (log/babel-test-message "(fn [{a :a x :b}] [a x])"))

(expect nil (log/babel-test-message "(fn [{a :a x 5}] [a x])"))

(expect nil (log/babel-test-message "(fn [{a :a x #(+ %)}] [a x])"))

(expect nil (log/babel-test-message "(fn [[x] {y 5}] [[y] 7])"))

;; TODO: check how it works with multiple arities of fn
