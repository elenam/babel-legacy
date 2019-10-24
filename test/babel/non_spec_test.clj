(ns babel.non-spec-test
  (:require
   [expectations :refer :all]
   [logs.utils :as log]))

;#########################################
;### Tests for errors that aren't      ###
;### spec failures                     ###
;#########################################

;; TO RUN tests, make sure you have repl started in a separate terminal

(def to-log? true)

(expect #(not= % nil) (log/set-log to-log?))

(expect nil (log/add-log
              (do
                (def file-name "this file")
                (:file (meta #'file-name)))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;ArithmeticExceptions;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect "Tried to divide by zero" (log/babel-test-message "(/ 70 0)"))

(expect "Tried to divide by zero" (log/babel-test-message "(/ 70 8 0)"))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;PassTest;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect nil (log/babel-test-message "(* 3 0)"))

(expect nil (log/babel-test-message "#(+ 1)"))

(expect nil (log/babel-test-message "(- 2)"))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;NullPointerException;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 3-argument usage of 'into' has a transducer as its second argument. {} is a function
;; so it passes the type check, but throws a null pointer exception when applied.
(expect "An attempt to access a non-existing object (NullPointerException)." (log/babel-test-message "(into [] {} \"a\")"))


;###############################
;### Number Format Exception ###
;###############################

(expect "The format of the number 8.5.1 is invalid." (log/babel-test-message "8.5.1"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;; RuntimeException ;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect #"(?s)Syntax error compiling at \(:(\d+):(\d+)\)\.(.*)Too many arguments to def\."
(log/babel-test-message "(def 7 8 9)"))

(expect #"(?s)Syntax error compiling at \(:(\d+):(\d+)\)\.(.*)Name orange is undefined\."
(log/babel-test-message "(+ orange 3)"))

(expect #"(?s)Syntax error compiling at \(:(\d+):(\d+)\)\.(.*)Name kiwi is undefined\."
(log/babel-test-message "(kiwi)"))

(expect #"(?s)Syntax error compiling at \(:(\d+):(\d+)\)\.(.*)Name def is undefined\."
(log/babel-test-message "def"))

(expect #"(?s)Syntax error compiling at \(:(\d+):(\d+)\)\.(.*)def must be followed by a name\.(.*)"
(log/babel-test-message "(def 2 3)"))

(expect #"(?s)Syntax error compiling at \(:(\d+):(\d+)\)\.(.*)let is a macro and cannot be used by itself or passed to a function\."
(log/babel-test-message "(even? let)"))

(expect #"(?s)Syntax error compiling at \(:(\d+):(\d+)\)\.(.*)let is a macro and cannot be used by itself or passed to a function\."
(log/babel-test-message "let"))

(expect #"(?s)Syntax error compiling at \(:(\d+):(\d+)\)\.(.*)Too few arguments to if\."
(log/babel-test-message "(if)"))

(expect #"(?s)Syntax error compiling at \(:(\d+):(\d+)\)\.(.*)Too many arguments to if\."
(log/babel-test-message "(if (= 0 0) (+ 2 3) (+ 2 3) (+2 3))"))

(expect #"(?s)# must be followed by a symbol\.(.*)"
(log/babel-test-message "(map # [0])"))

(expect #"(?s)There is an unmatched delimiter ]\.(.*)"
(log/babel-test-message "(+ (])"))

(expect #"(?s)You have a key that's missing a value; a hashmap must consist of key/value pairs\.(.*)"
(log/babel-test-message "{9 8 7}"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;; IllegalArgumentException ;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; I don't think this is the wording we want, but this is what our processing currently does
(expect "A keyword: :a can only take one or two arguments." (log/babel-test-message "(:a 4 5 6)"))

(expect "Every key for a hashmap must be followed by a value, but the key :2 does not have a matching value."
        (log/babel-test-message "(hash-map :1 1, :2)"))

;; Might want to change the printing of CompilerException; definitely want to report the form (it's in the exception)
(expect #"(?s)Syntax error \(IllegalArgumentException\) compiling at \(:(\d+):(\d+)\)\.(.*)You cannot call nil as a function. The expression was: \(nil\)(.*)"
(log/babel-test-message "(nil)"))

(expect #"(?s)Syntax error \(IllegalArgumentException\) compiling at \(:(\d+):(\d+)\)\.(.*)You cannot call nil as a function. The expression was: \(nil 5\)(.*)"
(log/babel-test-message "(nil 5)"))

;; Eventually will need to fix the arg printing in this:
(expect #"(?s)Syntax error \(IllegalArgumentException\) compiling at \(:(\d+):(\d+)\)\.(.*)You cannot call nil as a function. The expression was: \(nil even\? #\(inc %1\)\)(.*)"
(log/babel-test-message "(nil even? #(inc %))"))

(expect "You have duplicated the key 1, you cannot use the same key in a hashmap twice." (log/babel-test-message "{1 1 1 1}"))

(expect "You have duplicated the key 1, you cannot use the same key in a hashmap twice." (log/babel-test-message "{1 0 (- 3 2) 8}"))

(expect #"(?s)Syntax error \(IllegalArgumentException\) compiling at \(:(\d+):(\d+)\)\.(.*)Recur expected no arguments but was given 1 argument\.(.*)"
(log/babel-test-message "(loop [] (recur 5))"))

(expect #"(?s)Syntax error \(IllegalArgumentException\) compiling at \(:(\d+):(\d+)\)\.(.*)Recur expected 1 argument but was given no arguments\.(.*)"
(log/babel-test-message "(loop [x 2] (recur))"))

(expect #"(?s)Syntax error \(IllegalArgumentException\) compiling at \(:(\d+):(\d+)\)\.(.*)Recur expected 2 arguments but was given 1 argument\.(.*)"
(log/babel-test-message "(defn f[x y] (if x 1 (recur 2)))"))

(expect #"(?s)Syntax error \(IllegalArgumentException\) compiling at \(:(\d+):(\d+)\)\.(.*)Recur expected 1 argument but was given 2 arguments\.(.*)"
(log/babel-test-message "(fn [x] (recur 2 3))"))

(expect #"The 'case' input 13 didn't match any of the options."
(log/babel-test-message "(case (+ 6 7))"))

(expect #"The 'case' input Hi there didn't match any of the options."
(log/babel-test-message "(case \"Hi there\")"))

(expect #"The 'case' input (\S+) didn't match any of the options."
(log/babel-test-message "(case map)"))

(expect #"The 'case' input (\S+) didn't match any of the options."
(log/babel-test-message "(case #(+ % 1))"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;; IllegalStateException ;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect "% can only be followed by & or a number." (log/babel-test-message "(#(+ %a 1) 2 3)"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;; ArityException ;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; TODO: change the wording of this error!!!

(expect "Wrong number of args (1) passed to: hello" (log/babel-test-message "(defn hello [x y] (* x y)) (hello 1)"))

(expect "Wrong number of args (0) passed to: hello" (log/babel-test-message "(defn hello [x y] (* x y)) (hello)"))

(expect "Wrong number of args (3) passed to: hello" (log/babel-test-message "(defn hello [x y] (* x y)) (hello 1 2 3)"))

(expect "Wrong number of args (1) passed to: anonymous function" (log/babel-test-message "(map #(7) [0])"))

(expect "Wrong number of args (2) passed to: anonymous function" (log/babel-test-message "(map #(+ %1) [9] [0])"))

(expect "Wrong number of args (2) passed to: f" (log/babel-test-message "(defn f[x] (inc x)) (f 5 6)"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;; ClassCastException ;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect "Expected a function, but a number was given instead." (log/babel-test-message "(5 6)"))

(expect "Expected a function, but a number was given instead." (log/babel-test-message "(def apple 5) (apple 0)"))

(expect "Expected a function, but a string was given instead." (log/babel-test-message "(\"apple\")"))

(expect "Expected a string, but a number was given instead." (log/babel-test-message "(compare \"5\" 7)"))

(expect "Expected a number, but a string was given instead." (log/babel-test-message "(compare 7 \"5\")"))

(expect "Expected a character, but a string was given instead." (log/babel-test-message "(compare \\a \"a\")"))

(expect "Expected a file or an input stream, but a number was given instead." (log/babel-test-message "(line-seq 3)"))

(expect "Expected a regular expression pattern, but a number was given instead." (log/babel-test-message "(re-find 5 6)"))

(expect "Expected a string, but a number was given instead." (log/babel-test-message "(re-find #\"a\" 6)"))

(expect "Expected a string, but a regular expression pattern was given instead." (log/babel-test-message "(re-find #\"a\" #\"a\")"))

(expect "Expected a function, but a number was given instead." (log/babel-test-message "(-> 4 5)"))

(expect "Expected a function, but a number was given instead." (log/babel-test-message "(->> 4 5)"))

(expect nil (log/babel-test-message "(compare 5 nil)"))

(expect nil (log/babel-test-message "(compare nil 5)"))

(expect nil (log/babel-test-message "(compare nil nil)"))

;; Lazy sequences aren't evaluated, give a class cast exception instead
(expect "Expected a number, but a sequence was given instead." (log/babel-test-message "(take (range) (range))"))
