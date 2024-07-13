(ns babel.fn-defn-test
  (:require
   [expectations :refer [expect]]
   [logs.utils :as log]
   [babel.non-spec-test]
   [babel.utils-for-testing :as t]))

;#############################################
;### Tests for fn and defn (specced macros)###
;#############################################

;; TO RUN tests, make sure you have repl started in a separate terminal

(expect #(not= % nil) (log/set-log babel.non-spec-test/to-log?))

(expect nil (log/add-log
              (do
                (def file-name "this file")
                (:file (meta #'file-name)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;; fn: missing a vector  ;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect (t/make-pattern "Syntax problems with (fn a):
fn is missing a vector of parameters.")
(log/babel-test-message "(fn a)"))

(expect (t/make-pattern "Syntax problems with (fn):
fn is missing a vector of parameters.")
(log/babel-test-message "(fn)"))

(expect (t/make-pattern "Syntax problems with (fn nil):
Parameter vector must consist of names, but nil is not a name.")
(log/babel-test-message "(fn nil)"))

(expect (t/make-pattern "Syntax problems with (fn a [a a.b/c 5]):
Parameter vector must consist of names, but a.b/c, 5 are not valid names.")
(log/babel-test-message "(fn a [a a.b/c 5])"))

(expect (t/make-pattern "Syntax problems with (fn a [a.b/c]):
Parameter vector must consist of names, but a.b/c is not a valid name.")
(log/babel-test-message "(fn a [a.b/c])"))

(expect (t/make-pattern
"Syntax problems with (fn 5 6):
A function definition requires a vector of parameters, but was given 5 instead."
#"(.*)"
#"In (.+) on line (\d+) at position (\d+)")
(log/babel-test-message "(fn 5 6)"))

(expect (t/make-pattern "Syntax problems with (fn a 6):
A function definition requires a vector of parameters, but was given 6 instead.")
(log/babel-test-message "(fn a 6)"))

;; Note: the name is optional, so the message may be misleading
(expect (t/make-pattern "Syntax problems with (fn a b):
A function definition requires a vector of parameters, but was given b instead.")
(log/babel-test-message "(fn a b)"))

(expect (t/make-pattern "Syntax problems with (fn {x y}):
A function definition requires a vector of parameters, but was given {x y} instead.")
(log/babel-test-message "(fn {x y})"))

(expect (t/make-pattern "Syntax problems with (fn '(x y)):
A function definition requires a vector of parameters, but was given '(x y) instead.")
(log/babel-test-message "(fn '(x y))"))

(expect (t/make-pattern "Syntax problems with (fn \\a 5):
A function definition requires a vector of parameters, but was given \\a instead.")
(log/babel-test-message "(fn \\a 5)"))

(expect (t/make-pattern "Syntax problems with (fn a '([])):
A function definition requires a vector of parameters, but was given '([]) instead.")
(log/babel-test-message "(fn a '([]))"))

(expect (t/make-pattern "Syntax problems with (fn a b []):
The function definition is missing a vector of parameters or it is misplaced.")
(log/babel-test-message "(fn a b [])"))

(expect (t/make-pattern "Syntax problems with (fn 4 []):
The function definition is missing a vector of parameters or it is misplaced.")
(log/babel-test-message "(fn 4 [])"))

(expect (t/make-pattern "Syntax problems with (fn a #(= %5) [] #\"hi\" ['(8 9)]):
The function definition is missing a vector of parameters or it is misplaced.")
(log/babel-test-message "(fn a #(= %5) [] #\"hi\" ['(8 9)])"))

(expect (t/make-pattern "Syntax problems with (fn &):
fn is missing a vector of parameters.")
(log/babel-test-message "(fn &)"))

; & is a symbol, so this may be misleading:
(expect (t/make-pattern "Syntax problems with (fn & x):
A function definition requires a vector of parameters, but was given x instead.")
(log/babel-test-message "(fn & x)"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; fn: parameter vector has non-names ;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect (t/make-pattern "Syntax problems with (fn [5] {7 \"hello\"}):
Parameter vector must consist of names, but 5 is not a name.")
(log/babel-test-message "(fn [5] {7 \"hello\"})"))

(expect (t/make-pattern "Syntax problems with (fn [5 x] {7 \"hello\"}):
Parameter vector must consist of names, but 5 is not a name.")
(log/babel-test-message "(fn [5 x] {7 \"hello\"})"))

(expect (t/make-pattern "Syntax problems with (fn [x 5 y] {7 \"hello\"}):
Parameter vector must consist of names, but 5 is not a name.")
(log/babel-test-message "(fn [x 5 y] {7 \"hello\"})"))

(expect (t/make-pattern "Syntax problems with (fn [x #(+ %1) y] 2):
Parameter vector must consist of names, but #(+ %1) is not a name.")
(log/babel-test-message "(fn [x #(+ %) y] 2)"))

(expect (t/make-pattern "Syntax problems with (fn [x #(+ %1) 3] 2):
Parameter vector must consist of names, but #(+ %1), 3 are not names.")
(log/babel-test-message "(fn [x #(+ %) 3] 2)"))

(expect (t/make-pattern "Syntax problems with (fn ['(2 3) 5]):
Parameter vector must consist of names, but '(2 3), 5 are not names.")
(log/babel-test-message "(fn ['(2 3) 5])"))

;; This is actually somewhat misleading since this would've been
;; correct as destructuring with keywords and names as map
;; elements
(expect (t/make-pattern "Syntax problems with (fn [{a :a, 5 6}] [a x]):
Parameter vector must consist of names, but {a :a, 5 6} is not a name.")
(log/babel-test-message "(fn [{a :a 5 6}] [a x])"))

(expect (t/make-pattern "Syntax problems with (fn a [nil]):
Parameter vector must consist of names, but nil is not a name.")
(log/babel-test-message "(fn a [nil])"))

(expect (t/make-pattern "Syntax problems with (fn a [(count 5)]):
Parameter vector must consist of names, but (count 5) is not a name.")
(log/babel-test-message "(fn a [(count 5)])"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; fn: nested parameter vector errors ;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect (t/make-pattern "Syntax problems with (fn [[5]] 6):
Function parameters must be a vector of names, but [5] was given instead.")
(log/babel-test-message "(fn [[5]] 6)"))

(expect (t/make-pattern "Syntax problems with (fn [[[5]]] 6):
Function parameters must be a vector of names, but [[5]] was given instead.")
(log/babel-test-message "(fn [[[5]]] 6)"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;; fn: map binding errors ;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect (t/make-pattern "Syntax problems with (fn [{a :a, 5 :b}] [a x]):
Parameter vector must consist of names, but {a :a, 5 :b} is not a name.")
(log/babel-test-message "(fn [{a :a 5 :b}] [a x])"))

(expect (t/make-pattern "Syntax problems with (fn [[{a :a, 5 x}]] [a x]):
Function parameters must be a vector of names, but [{a :a, 5 x}] was given instead.")
(log/babel-test-message "(fn [[{a :a 5 x}]] [a x])"))

(expect (t/make-pattern "Syntax problems with (fn [[[{a :a, 5 x}]]] [a x]):
Function parameters must be a vector of names, but [[{a :a, 5 x}]] was given instead.")
(log/babel-test-message "(fn [[[{a :a 5 x}]]] [a x])"))

(expect (t/make-pattern "Syntax problems with (fn [[[{a :a, 5 x}] 5]] [a x]):
Function parameters must be a vector of names, but [[{a :a, 5 x}] 5] was given instead.")
(log/babel-test-message "(fn [[[{a :a 5 x}] 5]] [a x])"))

(expect (t/make-pattern "Syntax problems with (fn [[[{a :a, 5 x}] z]] [a x]):
Function parameters must be a vector of names, but [[{a :a, 5 x}] z] was given instead.")
(log/babel-test-message "(fn [[[{a :a 5 x}] z]] [a x])"))

(expect (t/make-pattern "Syntax problems with (fn [x {:a 5}] 7):
Parameter vector must consist of names, but {:a 5} is not a name.")
(log/babel-test-message "(fn [x {:a 5}] 7)"))

(expect (t/make-pattern "Syntax problems with (fn [[x] {:a 5}] 7):
fn needs a vector of parameters and a body, but has something else instead.")
(log/babel-test-message "(fn [[x] {:a 5}] 7)"))

(expect (t/make-pattern "Syntax problems with (fn [[x] 6] 5):
fn needs a vector of parameters and a body, but has something else instead.")
(log/babel-test-message "(fn [[x] 6] 5)"))

(expect (t/make-pattern "Syntax problems with (fn a \"abc\"):
A function definition requires a vector of parameters, but was given \"abc\" instead.")
(log/babel-test-message "(fn a \"abc\")"))

(expect (t/make-pattern "Syntax problems with (fn a (count [])):
A function definition requires a vector of parameters, but was given (count []) instead.")
(log/babel-test-message "(fn a (count []))"))

(expect (t/make-pattern "Syntax problems with (fn a #{8}):
A function definition requires a vector of parameters, but was given #{8} instead.")
(log/babel-test-message "(fn a #{8})"))

(expect (t/make-pattern "Syntax problems with (fn a nil):
Parameter vector must consist of names, but nil is not a name.")
(log/babel-test-message "(fn a nil)"))

(expect (t/make-pattern "Syntax problems with (fn [#(+ %&)]):
Parameter vector must consist of names, but #(+ %&) is not a name.")
(log/babel-test-message "(fn [#(+ %&)])"))

(expect (t/make-pattern "Syntax problems with (fn [#(+ %&) [#(%&)]]):
Parameter vector must consist of names, but #(+ %&), [#(%&)] are not names.")
(log/babel-test-message "(fn [#(+ %&) [#(%&)]])"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;; fails spec for let, not fn ;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; (expect (t/make-pattern "Syntax problems with (fn [[x] {:a 5}] [[y] 7]):
;; Fails let spec; might be fixed in spec2.")
;; (log/babel-test-message "(fn [[x] {:a 5}] [[y] 7])"))

;; (expect (t/make-pattern "Syntax problems with (fn [[x] 6 z] [[x] 7]):
;; Fails let spec; might be fixed in spec2.")
;; (log/babel-test-message "(fn [[x] 6 z] [[x] 7])"))

;; (expect (t/make-pattern "Syntax problems with (fn [[x] 6] [[x] 7]):
;; Fails let spec; might be fixed in spec2.")
;; (log/babel-test-message "(fn [[x] 6] [[x] 7])"))

;; (expect (t/make-pattern "Syntax problems with (fn [[[x]] #(+ %1 %2)] [[x] 7]):
;; Fails let spec; might be fixed in spec2.")
;; (log/babel-test-message "(fn [[[x]] #(+ %1 %2)] [[x] 7])"))

;; The spec failure doesn't contain the function name a
;; (expect (t/make-pattern "Syntax problems with (fn a [[[x]] :a] [[x] 7]):
;; Fails let spec; might be fixed in spec2.")
;; (log/babel-test-message "(fn a [[[x]] :a] [[x] 7])"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; fn: more than one argument after & ;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(expect (t/make-pattern "Syntax problems with (fn [x & y z] 8):
& must be followed by exactly one name, but is followed by y z instead.")
(log/babel-test-message "(fn [x & y z] 8)"))

(expect (t/make-pattern "Syntax problems with (fn [& y z] 8):
& must be followed by exactly one name, but is followed by y z instead.")
(log/babel-test-message "(fn [& y z] 8)"))

(expect (t/make-pattern "Syntax problems with (fn [x & y 5] 8):
& must be followed by exactly one name, but is followed by y 5 instead.")
(log/babel-test-message "(fn [x & y 5] 8)"))

(expect (t/make-pattern "Syntax problems with (fn [x & y & z] 8):
& must be followed by exactly one name, but is followed by y & z instead.")
(log/babel-test-message "(fn [x & y & z] 8)"))

(expect (t/make-pattern "Syntax problems with (fn [x & y & z & u] 8):
& must be followed by exactly one name, but is followed by y & z & u instead.")
(log/babel-test-message "(fn [x & y & z & u] 8)"))

(expect (t/make-pattern "Syntax problems with (fn [x & #(+ %1)] 8):
& must be followed by exactly one name, but is followed by #(+ %1) instead.")
(log/babel-test-message "(fn [x & #(+ %)] 8)"))

(expect (t/make-pattern "Syntax problems with (fn [x &] 8):
fn is missing a name after &.")
(log/babel-test-message "(fn [x &] 8)"))

(expect (t/make-pattern "Syntax problems with (fn [& nil] 0):
& must be followed by exactly one name, but is followed by nil instead.")
(log/babel-test-message "(fn [& nil] 0)"))

(expect (t/make-pattern "Syntax problems with (fn [& nil nil] 0):
& must be followed by exactly one name, but is followed by nil nil instead.")
(log/babel-test-message "(fn [& nil nil] 0)"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;; fn with & ;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; FAILED AFTER THE CHANGE
(expect (t/make-pattern "Syntax problems with (fn [2 & y] 8):
Parameter vector must consist of names, but 2 is not a name.")
(log/babel-test-message "(fn [2 & y] 8)"))

(expect (t/make-pattern "Syntax problems with (fn [x & 7] 8):
& must be followed by exactly one name, but is followed by 7 instead.")
(log/babel-test-message "(fn [x & 7] 8)"))

(expect (t/make-pattern "Syntax problems with (fn [& 7] 8):
& must be followed by exactly one name, but is followed by 7 instead.")
(log/babel-test-message "(fn [& 7] 8)"))

(expect (t/make-pattern "Syntax problems with (fn [&] 8):
fn is missing a name after &.")
(log/babel-test-message "(fn [&] 8)"))

(expect (t/make-pattern "Syntax problems with (fn a [[x] &] 8):
fn needs a vector of parameters and a body, but has something else instead.")
(log/babel-test-message "(fn a [[x] &] 8)"))

(expect (t/make-pattern "Syntax problems with (fn [x &] 8):
fn is missing a name after &.")
(log/babel-test-message "(fn [x &] 8)"))

(expect (t/make-pattern "Syntax problems with (fn [[x] &] 8):
fn needs a vector of parameters and a body, but has something else instead.")
(log/babel-test-message "(fn [[x] &] 8)"))

(expect (t/make-pattern "Syntax problems with (fn [5 & 7] 8):
Parameter vector must consist of names, but 5, 7 are not names.")
(log/babel-test-message "(fn [5 & 7] 8)"))

(expect (t/make-pattern "Syntax problems with (fn [x & [5]] 2 3):
& must be followed by exactly one name, but is followed by [5] instead.")
(log/babel-test-message "(fn [x & [5]] 2 3)"))

(expect (t/make-pattern "Syntax problems with (fn [[x] & [5]] 2 3):
fn needs a vector of parameters and a body, but has something else instead.")
(log/babel-test-message "(fn [[x] & [5]] 2 3)"))

;; Nested vector with &:

(expect (t/make-pattern "Syntax problems with (fn [[x & nil]] 8):
& must be followed by exactly one name, but is followed by nil instead.")
(log/babel-test-message "(fn [[x & nil]] 8)"))

(expect (t/make-pattern "Syntax problems with (fn [[& nil]] 8):
& must be followed by exactly one name, but is followed by nil instead.")
(log/babel-test-message "(fn [[& nil]] 8)"))

(expect (t/make-pattern "Syntax problems with (fn [[& 6 & 6]] 8):
& must be followed by exactly one name, but is followed by 6 & 6 instead.")
(log/babel-test-message "(fn [[& 6 & 6]] 8)"))

(expect (t/make-pattern "Syntax problems with (fn [[x &]] 8):
fn is missing a name after &.")
(log/babel-test-message "(fn [[x &]] 8)"))

(expect (t/make-pattern "Syntax problems with (fn [x u [[a [&]]]] 8):
Missing a name after &.")
(log/babel-test-message "(fn [x u [[a [&]]]] 8)"))

;; Nested vector with & after a variable:

(expect (t/make-pattern "Syntax problems with (fn [x [& 6 & 6]] 8):
& must be followed by exactly one name, but is followed by 6 & 6 instead.")
(log/babel-test-message "(fn [x [& 6 & 6]] 8)"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;; a nested seq fn ;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect (t/make-pattern "Syntax problems with (fn ([[x] 6])):
The issue is in the first clause.
Parameter vector must consist of names, but 6 is not a name.")
(log/babel-test-message "(fn ([[x] 6]))"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;; var-arg fn ;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect (t/make-pattern "Syntax problems with (fn ([x & y] 2 3) 5):
5 cannot be outside of a function body.")
(log/babel-test-message "(fn ([x & y] 2 3) 5)"))

(expect (t/make-pattern "Syntax problems with (fn ([x & y] 2 3) [5]):
A function clause must be enclosed in parentheses, but is a vector [5] instead.")
(log/babel-test-message "(fn ([x & y] 2 3) [5])"))

(expect (t/make-pattern "Syntax problems with (fn ([x & y] 2 3) [x]):
A function clause must be enclosed in parentheses, but is a vector [x] instead.")
(log/babel-test-message "(fn ([x & y] 2 3) [x])"))

(expect (t/make-pattern "Syntax problems with (fn ([x & y] 2 3) [#(+ %1)]):
A function clause must be enclosed in parentheses, but is a vector [#(+ %1)] instead.")
(log/babel-test-message "(fn ([x & y] 2 3) [#(+ %)])"))

(expect (t/make-pattern "Syntax problems with (fn ([x & y] 2 3) (x 3)):
The issue is in the second clause.
A function definition requires a vector of parameters, but was given x instead.")
(log/babel-test-message "(fn ([x & y] 2 3) (x 3))"))

(expect (t/make-pattern "Syntax problems with (fn ([x & y] 2 3) '(x 3)):
The issue is in the second clause.
'(x 3) cannot be outside of a function body.")
(log/babel-test-message "(fn ([x & y] 2 3) '(x 3))"))

(expect (t/make-pattern "Syntax problems with (fn ([x & y] 2 3) #(+ %1)):
The issue is in the second clause.
#(+ %1) cannot be outside of a function body.")
(log/babel-test-message "(fn ([x & y] 2 3) #(+ %))"))

(expect (t/make-pattern "Syntax problems with (fn ([x & y] 2 3) ([5] 3)):
The issue is in the second clause.
Parameter vector must consist of names, but 5 is not a name.")
(log/babel-test-message "(fn ([x & y] 2 3) ([5] 3))"))

(expect (t/make-pattern "Syntax problems with (fn ([x & y] 2 3) ([& x y] 3)):
The issue is in the second clause.
& must be followed by exactly one name, but is followed by x y instead.")
(log/babel-test-message "(fn ([x & y] 2 3) ([& x y] 3))"))

(expect (t/make-pattern "Syntax problems with (fn ([x] 2 3) ([& x y] 3)):
The issue is in the second clause.
& must be followed by exactly one name, but is followed by x y instead.")
(log/babel-test-message "(fn ([x] 2 3) ([& x y] 3))"))

(expect (t/make-pattern "Syntax problems with (fn a ([x] 2 3) ([] 8) ([& x y] 3)):
The issue is in the third clause.
& must be followed by exactly one name, but is followed by x y instead.")
(log/babel-test-message "(fn a ([x] 2 3) ([] 8) ([& x y] 3))"))

(expect (t/make-pattern "Syntax problems with (fn ([x 5] 2 3) ([x y] 3)):
The issue is in the first clause.
Parameter vector must consist of names, but 5 is not a name.")
(log/babel-test-message "(fn ([x 5] 2 3) ([x y] 3))"))

(expect (t/make-pattern "Syntax problems with (fn ([x] 5) ([6 & 7] 8)):
The issue is in the second clause.
Parameter vector must consist of names, but 6, 7 are not names.")
(log/babel-test-message "(fn ([x] 5) ([6 & 7] 8))"))

;; Because of parentheses, this needs the clause number
(expect (t/make-pattern "Syntax problems with (fn ([x {6 7}])):
The issue is in the first clause.
Parameter vector must consist of names, but {6 7} is not a name.")
(log/babel-test-message "(fn ([x {6 7}]))"))

(expect (t/make-pattern "Syntax problems with (fn ([[x] #(+ %3 %2)]) (8 9)):
The issue is in the first clause.
Parameter vector must consist of names, but #(+ %3 %2) is not a name.")
(log/babel-test-message "(fn ([[x] #(+ %3 %2)]) (8 9))"))

(expect (t/make-pattern "Syntax problems with (fn ([x] 5) ([x & y z] 8)):
The issue is in the second clause.
& must be followed by exactly one name, but is followed by y z instead.")
(log/babel-test-message "(fn ([x] 5) ([x & y z] 8))"))

(expect (t/make-pattern "Syntax problems with (fn [x u [& z y]] 8):
& must be followed by exactly one name, but is followed by z y instead.")
(log/babel-test-message "(fn [x u [& z y]] 8)"))

(expect (t/make-pattern "Syntax problems with (fn [x & u [z y]] 8):
& must be followed by exactly one name, but is followed by u [z y] instead.")
(log/babel-test-message "(fn [x & u [z y]] 8)"))

;; This shouldn't report the clause:
(expect (t/make-pattern "Syntax problems with (fn a ([x y] 7) ([x] 7) 8):
8 cannot be outside of a function body.")
(log/babel-test-message "(fn a ([x y] 7) ([x] 7) 8)"))

;; This shouldn't report the clause:
(expect (t/make-pattern "Syntax problems with (fn ([x & y] 2 3) #{6}):
#{6} cannot be outside of a function body.")
(log/babel-test-message "(fn ([x & y] 2 3) #{6})"))

;; This shouldn't report the clause:
(expect (t/make-pattern "Syntax problems with (fn ([x & y] 2 3) {:a :b}):
{:a :b} cannot be outside of a function body.")
(log/babel-test-message "(fn ([x & y] 2 3) {:a :b})"))

;; Reports the clause because functions are represented as sequences
(expect (t/make-pattern "Syntax problems with (fn ([x & y] 2 3) #(+ %1 %1)):
The issue is in the second clause.
#(+ %1 %1) cannot be outside of a function body.")
(log/babel-test-message "(fn ([x & y] 2 3) #(+ % %))"))

(expect (t/make-pattern "Syntax problems with (fn ([x] 2 3) ([2 & x] 3)):
The issue is in the second clause.
Parameter vector must consist of names, but 2 is not a name.")
(log/babel-test-message "(fn ([x] 2 3) ([2 & x] 3))"))

(expect (t/make-pattern "Syntax problems with (fn a ([x] 2 3) ([2 & 7] 3)):
The issue is in the second clause.
Parameter vector must consist of names, but 2, 7 are not names.")
(log/babel-test-message "(fn a ([x] 2 3) ([2 & 7] 3))"))

(expect (t/make-pattern "Syntax problems with (fn a ([x] 2 3) ([x &] 3)):
The issue is in the second clause.
fn is missing a name after &.")
(log/babel-test-message "(fn a ([x] 2 3) ([x &] 3))"))

(expect (t/make-pattern "Syntax problems with (fn a ([x &] 3) ([x] 2 3)):
The issue is in the first clause.
fn is missing a name after &.")
(log/babel-test-message "(fn a ([x &] 3) ([x] 2 3))"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;; defn missing name ;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect (t/make-pattern "Syntax problems with (defn):
Missing a function name.")
(log/babel-test-message "(defn)"))

(expect (t/make-pattern "Syntax problems with (defn 4 5):
Missing a function name, given 4 instead.")
(log/babel-test-message "(defn 4 5)"))

(expect (t/make-pattern "Syntax problems with (defn [x y] 7):
Missing a function name, given [x y] instead.")
(log/babel-test-message "(defn [x y] 7)"))

(expect (t/make-pattern "Syntax problems with (defn {7 8} 7):
Missing a function name, given {7 8} instead.")
(log/babel-test-message "(defn {7 8} 7)"))

(expect (t/make-pattern "Syntax problems with (defn #(even? %1) #(odd? %1)):
Missing a function name, given #(even? %1) instead.")
(log/babel-test-message "(defn #(even? %) #(odd? %))"))

(expect (t/make-pattern "Syntax problems with (defn '(7 8) 7):
Missing a function name, given '(7 8) instead.")
(log/babel-test-message "(defn '(7 8) 7)"))

(expect (t/make-pattern "Syntax problems with (defn nil 7):
Missing a function name, given nil instead.")
(log/babel-test-message "(defn nil 7)"))

(expect (t/make-pattern "Syntax problems with (defn a [x &] 7):
defn is missing a name after &.")
(log/babel-test-message "(defn a [x &] 7)"))

(expect (t/make-pattern "Syntax problems with (defn a [&] 7):
defn is missing a name after &.")
(log/babel-test-message "(defn a [&] 7)"))

(expect (t/make-pattern "Syntax problems with (defn a [x 3 y z] 7):
Parameter vector must consist of names, but 3 is not a name.")
(log/babel-test-message "(defn a [x 3 y z] 7)"))

(expect (t/make-pattern "Syntax problems with (defn a [a b/c]):
Parameter vector must consist of names, but b/c is not a valid name.")
(log/babel-test-message "(defn a [a b/c])"))

(expect (t/make-pattern "Syntax problems with (defn a [a b/c 5]):
Parameter vector must consist of names, but b/c, 5 are not valid names.")
(log/babel-test-message "(defn a [a b/c 5])"))

(expect (t/make-pattern "Syntax problems with (defn a [x 3 y #(+ 7 %1)] 7):
Parameter vector must consist of names, but 3, #(+ 7 %1) are not names.")
(log/babel-test-message "(defn a [x 3 y #(+ 7 %)] 7)"))

(expect (t/make-pattern "Syntax problems with (defn a [x & y z] 7):
& must be followed by exactly one name, but is followed by y z instead.")
(log/babel-test-message "(defn a [x & y z] 7)"))

(expect (t/make-pattern "Syntax problems with (defn- a [x & y z] 7):
& must be followed by exactly one name, but is followed by y z instead.")
(log/babel-test-message "(defn- a [x & y z] 7)"))

(expect (t/make-pattern "Syntax problems with (defn a [[x [6]] &] 7):
Function parameters must be a vector of names, but [x [6]] & was given instead.")
(log/babel-test-message "(defn a [[x [6]] &] 7)"))

(expect (t/make-pattern "Syntax problems with (defn a [(count 5)]):
Parameter vector must consist of names, but (count 5) is not a name.")
(log/babel-test-message "(defn a [(count 5)])"))

(expect (t/make-pattern "Syntax problems with (defn a 7):
A function definition requires a vector of parameters, but was given 7 instead.")
(log/babel-test-message "(defn a 7)"))

(expect (t/make-pattern "Syntax problems with (defn a #\"abc\" 1 2 3):
A function definition requires a vector of parameters, but was given #\"abc\" instead.")
(log/babel-test-message "(defn a #\"abc\" 1 2 3)"))

(expect (t/make-pattern "Syntax problems with (defn a (count 6)):
A function definition requires a vector of parameters, but was given (count 6) instead.")
(log/babel-test-message "(defn a (count 6))"))

(expect (t/make-pattern "Syntax problems with (defn a b [x]):
The function definition is missing a vector of parameters or it is misplaced.")
(log/babel-test-message "(defn a b [x])"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; defn special cases with a string or a map ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Special case because "abc" is allowed as a doc-string
(expect (t/make-pattern "Syntax problems with (defn a \"abc\"):
defn is missing a vector of parameters.")
(log/babel-test-message "(defn a \"abc\")"))

;; Special case because {x y} is allowed as a pre-, post-
;; conditions map
(expect (t/make-pattern "Syntax problems with (defn a {x y}):
defn is missing a vector of parameters.")
(log/babel-test-message "(defn a {x y})"))

(expect (t/make-pattern "Syntax problems with (defn a \"doc string\" 7):
A function definition requires a vector of parameters, but was given \"doc string\" 7 instead.")
(log/babel-test-message "(defn a \"doc string\" 7)"))

(expect (t/make-pattern "Syntax problems with (defn a {:pre x} {:a :m} 7):
A function definition requires a vector of parameters, but was given {:pre x} {:a :m} instead.")
(log/babel-test-message "(defn a {:pre x} {:a :m} 7)"))

(expect (t/make-pattern "Syntax problems with (defn a [x & [5]] 2 3):
& must be followed by exactly one name, but is followed by [5] instead.")
(log/babel-test-message "(defn a [x & [5]] 2 3)"))

(expect (t/make-pattern "Syntax problems with (defn a [& 7] 8):
& must be followed by exactly one name, but is followed by 7 instead.")
(log/babel-test-message "(defn a [& 7] 8)"))

(expect (t/make-pattern "Syntax problems with (defn f [[& nil]] 6):
& must be followed by exactly one name, but is followed by nil instead.")
(log/babel-test-message "(defn f [[& nil]] 6)"))

(expect (t/make-pattern "Syntax problems with (defn f [[x & #{9}]] 6):
& must be followed by exactly one name, but is followed by #{9} instead.")
(log/babel-test-message "(defn f [[x & #{9}]] 6)"))

(expect (t/make-pattern "Syntax problems with (defn f [[x & #{}]] 6):
& must be followed by exactly one name, but is followed by #{} instead.")
(log/babel-test-message "(defn f [[x & #{}]] 6)"))

(expect (t/make-pattern "Syntax problems with (defn a [x u [[a [&]]]] 8):
Missing a name after &.")
(log/babel-test-message "(defn a [x u [[a [&]]]] 8)"))

(expect (t/make-pattern "Syntax problems with (defn f \"doc string\" [[x & #{}]] 6):
& must be followed by exactly one name, but is followed by #{} instead.")
(log/babel-test-message "(defn f \"doc string\" [[x & #{}]] 6)"))

(expect (t/make-pattern "Syntax problems with (defn a \"doc string\" [x & 7] 8):
& must be followed by exactly one name, but is followed by 7 instead.")
(log/babel-test-message "(defn a \"doc string\" [x & 7] 8)"))

(expect (t/make-pattern "Syntax problems with (defn a {:pre x} [x & 7] 8):
& must be followed by exactly one name, but is followed by 7 instead.")
(log/babel-test-message "(defn a {:pre x} [x & 7] 8)"))

(expect (t/make-pattern "Syntax problems with (defn f {} ([+] 5 6) 7):
Unexpected element(s) outside of the first clause: 7")
(log/babel-test-message "(defn f {} ([+] 5 6) 7)"))

(expect (t/make-pattern "Syntax problems with (defn f \"doc string\" ([x] 5 6) #{8} 8):
Unexpected element(s) outside of the first clause: #{8} 8")
(log/babel-test-message "(defn f \"doc string\" ([x] 5 6) #{8} 8)"))

(expect (t/make-pattern "Syntax problems with (defn f \"doc string\" ([x] 5 6) #{8} (count '())):
Unexpected element(s) outside of the first clause: #{8} (count '())")
(log/babel-test-message "(defn f \"doc string\" ([x] 5 6) #{8} (count '()))"))

;; (expect (t/make-pattern "Syntax problems with (defn f ([x] 6) ([9] 0)):
;; Unexpected element(s) outside of the first clause: ([9] 0)")
;; (log/babel-test-message "(defn f ([x] 6) ([9] 0))"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;; defn invalid signature ;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect (t/make-pattern "Syntax problems in defn: instead of [[x] 9] you need a list.")
(log/babel-test-message "(defn f ([x] 9) [[x] 9] ([] 0))"))

(expect (t/make-pattern "Syntax problems in defn: instead of [[]] you need a list.")
(log/babel-test-message "(defn f ([x] 9) [[]] ([] 0))"))

(expect (t/make-pattern "Syntax problems in defn: instead of [[] nil] you need a list.")
(log/babel-test-message "(defn f ([x] 9) [[] nil] ([] 0))"))

(expect (t/make-pattern "Syntax problems in defn: instead of [[] a] you need a list.")
(log/babel-test-message "(defn f ([x] 9) [[] a] ([] 0))"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;; fn non-spec error   ;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect (t/make-pattern "Name x is undefined.")
(log/babel-test-message "(fn [{a x}] [a])"))

(expect #"You have a key that's missing a value; a hashmap must consist of key/value pairs\.(.*)"
(log/babel-test-message "(fn [{a :a x}] [a x])"))

(expect #"The function definition has two cases with the same number of arguments; only one case is allowed\.(.*)"
(log/babel-test-message "(fn ([x] 2 3) ([y] 3))"))

(expect #"The function definition has two cases with the same number of arguments; only one case is allowed\.(.*)"
(log/babel-test-message "(fn ([x] 2 3) ([y] 3) ([x] 8))"))

(expect #"The function definition has two cases with the same number of arguments; only one case is allowed\.(.*)"
(log/babel-test-message "(defn f ([x] 2 3) ([y] 3))"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;; fn valid definitions (maps) ;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect nil (log/babel-test-message "(fn [{a :a x :b}] [a x])"))

(expect nil (log/babel-test-message "(fn [{a :a x 5}] [a x])"))

(expect nil (log/babel-test-message "(fn [{a :a x #(+ %)}] [a x])"))

(expect nil (log/babel-test-message "(fn [[x] {y 5}] [[y] 7])"))

;; TODO: check how it works with multiple arities of fn
