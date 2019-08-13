(ns babel.non-spec-test
  (:require
   [expectations :refer :all]
   [logs.utils :as log]))

;#########################################
;### Tests for errors that aren't      ###
;### spec failures                     ###
;#########################################

;; TO RUN tests, make sure you have repl started in a separate terminal

;start logging
(log/start-log)

(def to-log? true)

(expect #(not= % nil) (log/set-log to-log?))

(expect nil (log/add-log
              (do
                (def file-name "this file")
                (:file (meta #'file-name)))))

(expect "Tried to divide by zero" (log/babel-test-message "(/ 70 0)"))

(expect "Tried to divide by zero" (log/babel-test-message "(/ 70 8 0)"))

(expect nil (log/babel-test-message "(* 3 0)"))

(expect nil (log/babel-test-message "#(+ 1)"))

(expect nil (log/babel-test-message "(- 2)"))

;; 3-argument usage of 'into' has a transducer as its second argument. {} is a function
;; so it passes the type check, but throws a null pointer exception when applied.
(expect "An attempt to access a non-existing object (NullPointerException)." (log/babel-test-message "(into [] {} \"a\")"))

;(expect "Expected a number, but a sequence was given instead." (log/babel-test-message "(defn greater-than-zero [x] (> x 0)) (take (range) (range))"))

;(expect "The second argument of (map f f) was expected to be a sequence but is a function f instead." (log/babel-test-message "(defn f [x] (+ x 2)) (map f f)"))

;(expect "Tried to divide by zero" (log/babel-test-message "(defn not-divide-zero [x] (/ x 0))"))

;(expect "Expected a number, but a string was given instead." (log/babel-test-message "(defn not-divide-zero [x] (/ x \"a\"))"))

(expect #"(?s)There is an unmatched delimiter ]\.(.*)" (log/babel-test-message "(+ (])"))

(expect #"(?s)You have a key that's missing a value; a hashmap must consist of key/value pairs\.(.*)" (log/babel-test-message "{9 8 7}"))

(expect "The format of the number 8.5.1 is invalid." (log/babel-test-message "8.5.1"))

(expect "Wrong number of args (1) passed to: anonymous function" (log/babel-test-message "(map #(7) [0])"))

(expect "Wrong number of args (2) passed to: anonymous function" (log/babel-test-message "(map #(+ %1) [9] [0])"))

(expect "Wrong number of args (2) passed to: f" (log/babel-test-message "(defn f[x] (inc x)) (f 5 6)"))

(expect #"(?s)# must be followed by a symbol\.(.*)" (log/babel-test-message "(map # [0])"))

(expect #"(?s)Syntax error compiling at \(:(\d+):(\d+)\)\.(.*)Too many arguments to def." (log/babel-test-message "(def 7 8 9)"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;; IllegalArgumentException ;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; I don't think this is the wording we want, but this is what our processing currently does
(expect "A keyword: :a can only take one or two arguments." (log/babel-test-message "(:a 4 5 6)"))

(expect "Every key for a hashmap must be followed by a value, but the key :2 does not have a matching value."
        (log/babel-test-message "(hash-map :1 1, :2)"))

;; Might want to change the printing of CompilerException; definitely want to report the form (it's in the exception)
(expect #"(?s)Syntax error \(IllegalArgumentException\) compiling at \(:(\d+):(\d+)\)\.(.*)You cannot call nil as a function." (log/babel-test-message "(nil)"))

(expect #"(?s)Syntax error \(IllegalArgumentException\) compiling at \(:(\d+):(\d+)\)\.(.*)You cannot call nil as a function." (log/babel-test-message "(nil 5)"))

;; Eventually will need to fix the arg printing in this:
(expect #"(?s)Syntax error \(IllegalArgumentException\) compiling at \(:(\d+):(\d+)\)\.(.*)You cannot call nil as a function." (log/babel-test-message "(nil even? #(inc %))"))
