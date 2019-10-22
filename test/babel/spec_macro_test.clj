(ns babel.spec-macro-test
  (:require
   [logs.utils :as log]
   [babel.non-spec-test :refer [to-log?]]
   [expectations :refer :all]))

;#########################################
;### Tests for specced macros          ###
;#########################################

;; TO RUN tests, make sure you have repl started in a separate terminal

(expect #(not= % nil) (log/set-log babel.non-spec-test/to-log?))

(expect nil (log/add-log
              (do
                (def file-name "this file")
                (:file (meta #'file-name)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;Syntax Problems;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;; let, if-let, when-let ;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect "let requires a vector of name/expression pairs, but is given 5 instead."
(log/babel-test-message "(let 5 5)"))

(expect "let requires a vector of name/expression pairs, but is given #(8 7) instead."
(log/babel-test-message "(let #(8 7) 5)"))

(expect "let requires a vector of name/expression pairs, but is given {8 7} instead."
(log/babel-test-message "(let {8 7} 5)"))

(expect "let requires a vector of name/expression pairs, but is given {8 7 9 0} instead."
(log/babel-test-message "(let {8 7 9 0} 5)"))

(expect "let requires a vector of name/expression pairs, but is given {8 7 #(+ %1) 7} instead."
(log/babel-test-message "(let {8 7 #(+ %) 7} 8)"))

(expect "let requires a vector of name/expression pairs, but is given (+ 8 7 #(+ %1) 7) instead."
(log/babel-test-message "(let (+ 8 7 #(+ %) 7) 5)"))

(expect "let requires a vector of name/expression pairs, but is given \"a\" instead."
(log/babel-test-message "(let \"a\" 5)"))

(expect "let requires a vector of name/expression pairs, but is given #{7 8} instead."
(log/babel-test-message "(let #{7 8} 9)"))

(expect "if-let requires a vector of name/expression pairs, but is given \"a\" instead."
(log/babel-test-message "(if-let \"a\" 6)"))

(expect "if-let requires a vector of name/expression pairs, but is given {8 9 5 4} instead."
(log/babel-test-message "(if-let {8 9 5 4} 6)"))

(expect "when-let requires a vector of name/expression pairs, but is given 6 instead."
(log/babel-test-message "(when-let 6)"))

(expect "let requires a vector of name/expression pairs, but is given '(8) instead."
(log/babel-test-message "(let '(8))"))

(expect "let requires a vector of name/expression pairs, but is given '(1 2) instead."
(log/babel-test-message "(let '(1 2))"))

;; TO-DO:
;; This wording is different from the wording above, and there is no space before the argument of 'let':
(expect "let requires pairs of a name and an expression, but in (let[[8 _]] 9) one element doesn't have a match."
(log/babel-test-message "(let [[8 _]] 9)"))

(expect "Syntax problems with (let [(+ #(* %1 3) 2) g] 7):
In place of + #(* %1 3) 2 the following are allowed: a name or a vector or a hashmap"
(log/babel-test-message "(let [(+ #(* %1 3) 2) g] 7)"))

(expect "Syntax problems with (let [(+ #(* %1 3) 2) g] 7):
In place of + #(* %1 3) 2 the following are allowed: a name or a vector or a hashmap"
(log/babel-test-message "(let [(+ #(* % 3) 2) g] 7)"))

(expect "Syntax problems with (let [{:a 1} g] 7):
In place of {:a 1} the following are allowed: a name or a vector
In place of :a the following are allowed: a name or a vector or a hashmap
In place of 1 the following are allowed: a vector"
(log/babel-test-message " (let [{:a 1} g] 7)"))

(expect "Syntax problems with (let [(+ x 1) g] g):
In place of + x 1 the following are allowed: a name or a vector or a hashmap"
(log/babel-test-message "(defn macro [s] (let [(+ x 1) g] g))"))

(expect "Syntax problems with (let [(+ 1 2) (+ 1 2)] (+ 1 2)):
In place of + 1 2 the following are allowed: a name or a vector or a hashmap" (log/babel-test-message "(let [(+ 1 2) (+ 1 2)] (+ 1 2))"))

(expect "Syntax problems with (let [[[3 4] [5 6]] y]):
In place of [3 4] [5 6] the following are allowed: a name or a hashmap" (log/babel-test-message "(let [[[3 4] [5 6]] y])"))

(expect "Syntax problems with (let [5 x]):
In place of 5 the following are allowed: a name or a vector or a hashmap"
(log/babel-test-message "(let [5 x])"))

(expect "Syntax problems with (let [#{\"chromosome\" [\"x\"] \"y\"} z]):\nIn place of \"chromosome\" [\"x\"] \"y\" the following are allowed: a name or a vector or a hashmap"
(log/babel-test-message "(let [#{\"chromosome\" [\"x\"] \"y\"} z])"))

(expect "Syntax problems with (let [(let [x 5] #(+ %1)) 9] 8):
In place of let [x 5] #(+ %1) the following are allowed: a name or a vector or a hashmap"
(log/babel-test-message "(let [(let [x 5] #(+ %1)) 9] 8)"))

(expect "Syntax problems with (let [#() 1] 5):
In place of #() the following are allowed: a name or a vector or a hashmap"
(log/babel-test-message "(let [#() 1] 5)"))

(expect "Syntax problems with (let [{8 7} 1] 0):
In place of {8 7} the following are allowed: a name or a vector
In place of 8 the following are allowed: a name or a vector or a hashmap
In place of 7 the following are allowed: a vector" (log/babel-test-message "(let [{8 7} 1] 0)"))

(expect "Syntax problems with (let [{8 7 #(+ %1) 7} 1] 8):
In place of {8 7 #(+ %1) 7} the following are allowed: a name or a vector
In place of 8 the following are allowed: a name or a vector or a hashmap
In place of 7 the following are allowed: a vector
In place of #(+ %1) the following are allowed: a name or a vector or a hashmap"
(log/babel-test-message "(let [{8 7 #(+ %) 7} 1] 8)"))

(expect "Syntax problems with (let [\"(let [7 8] 1)\" 0] 5):
In place of \"(let [7 8] 1)\" the following are allowed: a name or a vector or a hashmap"
(log/babel-test-message "(let [\"(let [7 8] 1)\" 0] 5)"))

(expect "Syntax problems with (let [#\"regex\" 5] 0):
In place of #\"regex\" the following are allowed: a name or a vector or a hashmap"
(log/babel-test-message "(let [#\"regex\" 5] 0)"))

(expect "Syntax problems with (let [#\"(let [5 6] 7)\" 5] 0):
In place of #\"(let [5 6] 7)\" the following are allowed: a name or a vector or a hashmap"
(log/babel-test-message "(let [#\"(let [5 6] 7)\" 5] 0)"))

(expect "Syntax problems with (let [& 5] 8):
In place of & the following are allowed: a vector or a hashmap"
(log/babel-test-message "(let [& 5] 8)"))

(expect "Syntax problems with (let [{& 5} 6] 9):
In place of {& 5} the following are allowed: a name or a vector
In place of & the following are allowed: a vector or a hashmap
In place of 5 the following are allowed: a vector"
(log/babel-test-message "(let [{& 5} 6] 9)"))

(expect "Syntax problems with (let ['(1 2) 6]):
In place of '(1 2) the following are allowed: a name or a vector or a hashmap"
(log/babel-test-message "(let ['(1 2) 6])"))

(expect "Syntax problems with (let ['(1 2) '(3 4)] #(+ %)):
In place of '(1 2) the following are allowed: a name or a vector or a hashmap"
(log/babel-test-message "(let ['(1 2) '(3 4)] #(+ %))"))

(expect "Syntax problems with (let ['(1 2) '(3 4)] '(1 2 3 4)):
In place of '(1 2) the following are allowed: a name or a vector or a hashmap"
(log/babel-test-message "(let ['(1 2) '(3 4)] '(1 2 3 4))"))

(expect "Syntax problems with (when-let [7 8]):
In place of 7 the following are allowed: a name or a vector or a hashmap"
(log/babel-test-message "(when-let [7 8])"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;Extra Input;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect "if-let has too many parts here: (if-let [x false y true] \"then\" \"else\") The extra parts are: y true"
(log/babel-test-message "(if-let [x false y true] \"then\" \"else\")"))

(expect "if-let has too many parts here: (if-let [[w n] (re-find #\"a(d+)x\" \"aaa123xxx\")] [w n] :not-found :x) The extra parts are: :x"
(log/babel-test-message "(if-let [[w n] (re-find #\"a(d+)x\" \"aaa123xxx\")] [w n] :not-found :x)"))

(expect "if-let has too many parts here: (if-let [a 8 #(+ %1) #(+ %3)] 6 7) The extra parts are: #(+ %1) #(+ %3)"
(log/babel-test-message "(if-let [a 8 #(+ %) #(+ %3)] 6 7)"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;; Insufficient Input;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect "when-let requires more parts than given here: (when-let)"
(log/babel-test-message "(when-let)"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;Nested Error;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect "Expected a number, but a function was given instead." (log/babel-test-message "(let [g (+ #(* %1 3) 2)] 7)"))
