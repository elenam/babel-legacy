(ns babel.non-spec-test
  (:require
   [expectations :refer [expect]]
   [logs.utils :as log]
   [babel.utils-for-testing :as t]))

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

(expect (t/make-pattern #"(?s)Tried to divide by zero(.*)"
                        #"In Clojure interactive session on line (\d+)\.") ;; No position number in the exception
(log/babel-test-message "(/ 70 0)"))

(expect #"(?s)Tried to divide by zero(.*)"
(log/babel-test-message "(/ 70 8 0)"))


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
(expect #"(?s)An attempt to access a non-existing object \(NullPointerException\)\.(.*)"
(log/babel-test-message "(into [] {} \"a\")"))


;###############################
;### Number Format Exception ###
;###############################

(expect #"(?s)The format of the number 8.5.1 is invalid\.(.*)"
(log/babel-test-message "8.5.1"))

;###############################
;##### Class Format Error  #####
;###############################

(expect (t/make-pattern "You cannot name a variable \"?&5.a\".")
(log/babel-test-message "(fn [?&5.a])"))

(expect (t/make-pattern "You cannot name a variable \"?'.a\".")
(log/babel-test-message "(fn [?'.a])"))

;; Note: there is no way to restore _ back to - since there is
;; no way to distiniguish the two:
(expect (t/make-pattern "You cannot name a variable \"*_7.8\".")
(log/babel-test-message "(fn [*-7.8])"))

(expect (t/make-pattern "You cannot name a variable \"*7.6\".")
(log/babel-test-message "(let [*7.6 8] 9)"))

(expect (t/make-pattern "You cannot name a variable \"*7.6\".")
(log/babel-test-message "(let [x 6 [*7.6] 8] 9)"))

(expect (t/make-pattern "You cannot name a variable \"*+7.6\".")
(log/babel-test-message "(let [x 6 [*+7.6] 8] 9)"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;; RuntimeException ;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect (t/make-pattern "Too many arguments to def."
                                         #"(.*)"
                                         #"In Clojure interactive session on line (\d+) at position (\d+)\.")
(log/babel-test-message "(def 7 8 9)"))

(expect (t/make-pattern "Name orange is undefined."
                        #"(.*)"
                        #"In Clojure interactive session on line (\d+) at position (\d+)\.")
(log/babel-test-message "(+ orange 3)"))

(expect (t/make-pattern "Name kiwi is undefined.")
(log/babel-test-message "(kiwi)"))

(expect (t/make-pattern "Name def is undefined."
                        #"(.*)"
                        #"In Clojure interactive session on line (\d+) at position (\d+)\.")
(log/babel-test-message "def"))

(expect (t/make-pattern "def must be followed by a name."
                        #"(.*)"
                        #"In Clojure interactive session on line (\d+) at position (\d+)\.")
(log/babel-test-message "(def 2 3)"))

(expect (t/make-pattern "let is a macro and cannot be used by itself or passed to a function.")
(log/babel-test-message "(even? let)"))

(expect (t/make-pattern "let is a macro and cannot be used by itself or passed to a function.")
(log/babel-test-message "let"))

(expect (t/make-pattern #"(?s)Too few arguments to if\."
                        #"(.*)"
                        #"In Clojure interactive session on line (\d+) at position (\d+)\.")
(log/babel-test-message "(if)"))

(expect #"(?s)Too many arguments to if\."
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
(expect #"(?s)The function :a cannot be called with three arguments\.(.*)"
(log/babel-test-message "(:a 4 5 6)"))

(expect #"(?s)An argument for a vector must be an integer number\.(.*)"
(log/babel-test-message "([7] \"a\")"))

(expect #"(?s)Every key for a hashmap must be followed by a value, but the key :2 does not have a matching value\.(.*)"
(log/babel-test-message "(hash-map :1 1, :2)"))

;; Might want to change the printing of CompilerException; definitely want to report the form (it's in the exception)
(expect (t/make-pattern "You cannot call nil as a function. The expression was: (nil)")
(log/babel-test-message "(nil)"))

(expect (t/make-pattern "You cannot call nil as a function. The expression was: (nil 5)")
(log/babel-test-message "(nil 5)"))

;; Eventually will need to fix the arg printing in this:
(expect (t/make-pattern "You cannot call nil as a function. The expression was: (nil even? #(inc %1))")
(log/babel-test-message "(nil even? #(inc %))"))

(expect #"(?s)You have duplicated the key 1, you cannot use the same key in a hashmap twice\.(.*)"
(log/babel-test-message "{1 1 1 1}"))

(expect #"(?s)You have duplicated the key 1, you cannot use the same key in a hashmap twice\.(.*)"
(log/babel-test-message "{1 0 (- 3 2) 8}"))

(expect (t/make-pattern "Recur expected no arguments but was given one argument.")
(log/babel-test-message "(loop [] (recur 5))"))

(expect (t/make-pattern "Recur expected one argument but was given no arguments.")
(log/babel-test-message "(loop [x 2] (recur))"))

(expect (t/make-pattern "Recur expected two arguments but was given one argument.")
(log/babel-test-message "(defn f[x y] (if x 1 (recur 2)))"))

(expect (t/make-pattern "Recur expected one argument but was given two arguments.")
(log/babel-test-message "(fn [x] (recur 2 3))"))

(expect #"(?s)The 'case' input 13 didn't match any of the options\.(.*)"
(log/babel-test-message "(case (+ 6 7))"))

(expect #"(?s)The 'case' input Hi there didn't match any of the options\.(.*)"
(log/babel-test-message "(case \"Hi there\")"))

(expect #"(?s)The 'case' input (\S+) didn't match any of the options\.(.*)"
(log/babel-test-message "(case map)"))

(expect #"(?s)The 'case' input (\S+) didn't match any of the options\.(.*)"
(log/babel-test-message "(case #(+ % 1))"))

(expect (t/make-pattern "There is no constructor for the class clojure.lang.ArityException with this number and type of arguments.")
(log/babel-test-message "(clojure.lang.ArityException. \"hello\")"))

(expect (t/make-pattern "There is no method indexOf with three arguments or with this type of argument(s) for a string (java.lang.String).")
(log/babel-test-message "(.indexOf \"abc\" 1 2 3)"))

(expect (t/make-pattern "There is no method indexOf with no arguments or a field indexOf for a string (java.lang.String).")
(log/babel-test-message "(.indexOf \"abc\")"))

(expect (t/make-pattern "There is no method index with one argument or with this type of argument(s) for a string (java.lang.String).")
(log/babel-test-message "(.index \"abc\" 7)"))

(expect (t/make-pattern "There is no method stuff with one argument or with this type of argument(s) in the class java.lang.Math.")
(log/babel-test-message "(Math/stuff 7)"))

(expect (t/make-pattern "There is no method compareTo with two arguments or with this type of argument(s) in the class java.lang.Boolean.")
(log/babel-test-message "(.compareTo true false true)"))

(expect (t/make-pattern "There is no method digit with no arguments or a field digit for a character (java.lang.Character).")
(log/babel-test-message "(.digit \\n)"))

(expect (t/make-pattern "There is no method stuff with two arguments or with this type of argument(s) for a number (java.lang.Long).")
(log/babel-test-message "(.stuff 7 8 9)"))

(expect (t/make-pattern "There is no method stuff with two arguments or with this type of argument(s) for a number (java.lang.Double).")
(log/babel-test-message "(.stuff 7.7 8 9)"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;; IllegalStateException ;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect #"(?s)% can only be followed by & or a number\.(.*)"
(log/babel-test-message "(#(+ %a 1) 2 3)"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;; ArityException ;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect #"(?s)The function hello cannot be called with one argument\.(.*)"
(log/babel-test-message "(defn hello [x y] (* x y)) (hello 1)"))

(expect #"(?s)The function hello cannot be called with no arguments\.(.*)"
(log/babel-test-message "(defn hello [x y] (* x y)) (hello)"))

(expect #"(?s)The function hello cannot be called with three arguments\.(.*)"
(log/babel-test-message "(defn hello [x y] (* x y)) (hello 1 2 3)"))

(expect (t/make-pattern "This anonymous function cannot be called with one argument.")
(log/babel-test-message "(map #(7) [0])"))

(expect (t/make-pattern "This anonymous function cannot be called with two arguments.")
(log/babel-test-message "(map #(+ %1) [9] [0])"))

(expect #"(?s)The function f cannot be called with two arguments\.(.*)"
(log/babel-test-message "(defn f[x] (inc x)) (f 5 6)"))

;; Via CompilerException, probably because of inlining:
(expect (t/make-pattern "The function int cannot be called with two arguments.")
(log/babel-test-message "(int 4 5)"))

(expect (t/make-pattern "A vector cannot be called with two arguments.")
(log/babel-test-message "([0] 8 9)"))

(expect (t/make-pattern "A map cannot be called with three arguments.")
(log/babel-test-message "({9 6} 8 7 9)"))

(expect (t/make-pattern "A map cannot be called with no arguments.")
(log/babel-test-message "({9 6})"))

(expect (t/make-pattern "A set cannot be called with no arguments.")
(log/babel-test-message "(#{9 6})"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;; ClassCastException ;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect #"(?s)Expected a function, but a number was given instead\.(.*)"
(log/babel-test-message "(5 6)"))

(expect #"(?s)Expected a function, but a number was given instead\.(.*)"
(log/babel-test-message "(drop 3 (1 2 3))"))

(expect #"(?s)Expected a function, but a number was given instead\.(.*)"
(log/babel-test-message "(def apple 5) (apple 0)"))

(expect #"(?s)Expected a function, but a string was given instead\.(.*)"
(log/babel-test-message "(\"apple\")"))

(expect #"(?s)Expected a string, but a number was given instead\.(.*)"
(log/babel-test-message "(compare \"5\" 7)"))

(expect #"(?s)Expected a number, but a string was given instead\.(.*)"
(log/babel-test-message "(compare 7 \"5\")"))

(expect #"(?s)Expected a character, but a string was given instead\.(.*)"
(log/babel-test-message "(compare \\a \"a\")"))

(expect #"(?s)Expected a file or an input stream, but a number was given instead\.(.*)"
(log/babel-test-message "(line-seq 3)"))

(expect #"(?s)Expected a regular expression pattern, but a number was given instead\.(.*)"
(log/babel-test-message "(re-find 5 6)"))

(expect #"(?s)Expected a string, but a number was given instead\.(.*)"
(log/babel-test-message "(re-find #\"a\" 6)"))

(expect #"(?s)Expected a string, but a regular expression pattern was given instead\.(.*)"
(log/babel-test-message "(re-find #\"a\" #\"a\")"))

(expect #"(?s)Expected a function, but a number was given instead\.(.*)"
(log/babel-test-message "(-> 4 5)"))

(expect #"(?s)Expected a function, but a number was given instead\.(.*)"
(log/babel-test-message "(->> 4 5)"))

;; The tests below might stop working if our spec on map and + changes:

(expect #"(?s)Expected a number, but a character was given instead\.(.*)"
(log/babel-test-message "(map #(> % 5) \"strawberry\")"))

(expect #"(?s)Expected an exception \(Throwable\), but a number was given instead\.(.*)"
(log/babel-test-message "(throw 5)"))

(expect #"(?s)Expected a function, but an exception was given instead\.(.*)"
(log/babel-test-message "(map (Exception. \"hi\") [9 0])"))

(expect #"(?s)Expected a number, but an exception was given instead\.(.*)"
(log/babel-test-message "(+ 5 (Exception. \"hi\"))"))

(expect #"(?s)Expected a number, but an exception \(Throwable\) was given instead\.(.*)"
(log/babel-test-message "(+ (Throwable. \"hi\") [9 0])"))

;; What used to be 'unrecognized class', now just prints the class:
(expect #"(?s)Expected a number, but java\.lang\.Class was given instead\.(.*)"
(log/babel-test-message "(+ 8 (type (class 7)))"))

(expect nil (log/babel-test-message "(compare 5 nil)"))

(expect nil (log/babel-test-message "(compare nil 5)"))

(expect nil (log/babel-test-message "(compare nil nil)"))

;; Lazy sequences aren't evaluated, give a class cast exception instead
(expect #"(?s)Expected a number, but a sequence was given instead\.(.*)"
(log/babel-test-message "(take (range) (range))"))
