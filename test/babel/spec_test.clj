(ns babel.spec-test
  (:require
    [logs.utils :as log]
    [babel.non-spec-test :refer [to-log?]]
    [babel.utils-for-testing :as t]
    [expectations :refer :all]))

;############################################
;### Tests for functions that have specs  ###
;############################################

;; TO RUN tests, make sure you have repl started in a separate terminal
; (expect "The second argument of (take 9 8) was expected to be a sequence but is a number 9 instead."
; (log/babel-test-message "(take 9 9)"))
(expect #(not= % nil)  (log/set-log babel.non-spec-test/to-log?))

(expect nil (log/add-log
              (do
                (def file-name "this file")
                (:file (meta #'file-name)))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;Insufficient Input;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(expect (t/make-pattern "Wrong number of arguments, expected in (map ): the function map expects one or more arguments but was given no arguments")
(log/babel-test-message "(map)"))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;Extra Output;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(expect (t/make-pattern "Wrong number of arguments, expected in (contains? {} \"a\" #{}): the function contains? expects two arguments but was given three arguments")
(log/babel-test-message "(contains? {} \"a\" #{})"))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;First Argument;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(expect (t/make-pattern "The first argument of (conj \"lijk\" \"jlksdfj\") was expected to be a sequence but is a string \"lijk\" instead.")
(log/babel-test-message "(conj \"lijk\" \"jlksdfj\")"))

(expect (t/make-pattern "The first argument of (conj 1 \"a\") was expected to be a sequence but is a number 1 instead.")
(log/babel-test-message "(conj 1 \"a\")"))

(expect (t/make-pattern "The first argument of (conj 1 \"a\") was expected to be a sequence but is a number 1 instead.")
(log/babel-test-message "(conj 1 \"a\")" ))

(expect (t/make-pattern "The first argument of (into 1 even?) was expected to be a sequence but is a number 1 instead.")
(log/babel-test-message "(into 1 even?)"))

(expect (t/make-pattern "The first argument of (contains? :a :a) was expected to be a collection but is a keyword :a instead.")
(log/babel-test-message "(contains? :a :a)"))

;; TODO: the message is correct since only a number is allowed as teh second arg when the first one is a string
;; However, this may not be the best message for beginners.
(expect (t/make-pattern "The second argument of (contains? \"a\" :a) was expected to be a number but is a keyword :a instead.")
(log/babel-test-message "(contains? \"a\" :a)"))

(expect (t/make-pattern "The first argument of (contains? 1 :a) was expected to be a collection but is a number 1 instead.")
(log/babel-test-message "(contains? 1 :a)"))

(expect (t/make-pattern "The first argument of (contains? j :a) was expected to be a collection but is a character j instead.")
(log/babel-test-message "(contains? \\j :a)"))

(expect (t/make-pattern "The first argument of (reduce 4 \"strawberry\") was expected to be a function but is a number 4 instead.")
(log/babel-test-message "(reduce 4 \"strawberry\")"))

(expect (t/make-pattern "The first argument of (even? (0 1 2 3 4)) was expected to be a number but is a sequence (0 1 2 3 4) instead.")
(log/babel-test-message "(even? (range 5))"))

(expect (t/make-pattern "The first argument of (even? (0 1 2 3 4 5 6 7 8 9)) was expected to be a number but is a sequence (0 1 2 3 4 5 6 7 8 9) instead.")
(log/babel-test-message "(even? (range 10))"))

(expect (t/make-pattern "The first argument of (even? ((0 1 2 3 4 5 6 7 8 9) ...)) was expected to be a number but is a sequence ((0 1 2 3 4 5 6 7 8 9) ...) instead.")
(log/babel-test-message "(even? (range 11))"))

(expect (t/make-pattern "The first argument of (filter (1)) was expected to be a function but is a list (1) instead.")
(log/babel-test-message "(filter '(1))"))

(expect (t/make-pattern "The first argument of (take [] []) was expected to be a number but is a vector [] instead.")
(log/babel-test-message "(take [] [])"))

(expect (t/make-pattern "The first argument of (take \"apple\" \"banana\") was expected to be a number but is a string \"apple\" instead.")
(log/babel-test-message "(take \"apple\" \"banana\")"))

(expect (t/make-pattern "The first argument of (contains? 1 1) was expected to be a collection but is a number 1 instead.")
(log/babel-test-message "(contains? 1 1)"))

(expect (t/make-pattern "The first argument of (contains? (1 2 3) 9) was expected to be a collection but is a list (1 2 3) instead.")
(log/babel-test-message "(contains? '(1 2 3) 9)"))

(expect (t/make-pattern "The first argument of (take nil []) was expected to be a number but is nil instead.")
(log/babel-test-message "(take nil [])"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;Second Argument;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(expect (t/make-pattern "The second argument of (map map map) was expected to be a sequence but is a function map instead.")
(log/babel-test-message "(map map map)"))

(expect (t/make-pattern "The second argument of (map f f) was expected to be a sequence but is a function f instead.")
(log/babel-test-message "(defn f [x] (+ x 2)) (map f f)"))

(expect (t/make-pattern "The second argument of (conj {} \"a\") was expected to be a sequence of vectors with only 2 elements or a map with key-value pairs but is a string \"a\" instead.")
(log/babel-test-message "(conj {} \"a\")"))

(expect (t/make-pattern "The second argument of (conj {} []) was expected to be a sequence of vectors with only 2 elements or a map with key-value pairs but is a vector [] instead.")
(log/babel-test-message "(conj {} [])"))

(expect (t/make-pattern "The second argument of (partition 1 1 1 1) was expected to be a sequence but is a number 1 instead.")
(log/babel-test-message "(partition 1 1 1 1)"))

(expect (t/make-pattern "The second argument of (partition 1 1 1) was expected to be a sequence but is a number 1 instead.")
(log/babel-test-message "(partition 1 1 1)"))

(expect (t/make-pattern "The second argument of (drop 1 :a) was expected to be a sequence but is a keyword :a instead.")
(log/babel-test-message "(drop 1 :a)"))

(expect (t/make-pattern "The second argument of (get-in [1] \"a\") was expected to be a sequence but is a string \"a\" instead.")
(log/babel-test-message "(get-in [1] \"a\")"))

(expect (t/make-pattern "The second argument of (filter even? odd?) was expected to be a sequence but is a function odd? instead.")
(log/babel-test-message "(filter even? odd?)"))

(expect (t/make-pattern "The second argument of (take 4 5) was expected to be a sequence but is a number 5 instead.")
(log/babel-test-message "(take 4 5)"))

(expect (t/make-pattern "The second argument of (contains? \"apple\" :a) was expected to be a number but is a keyword :a instead.")
(log/babel-test-message "(contains? \"apple\" :a)"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Spec errors on functions invoked by higher order functions ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect (t/make-pattern "The first argument of (even? s) was expected to be a number "
                        "but is a character s instead.")
(log/babel-test-message "(filter even? \"strawberry\")"))

(expect (t/make-pattern "Wrong number of arguments, expected in (mod 0): the function mod "
                        "expects two arguments but was given one arguments")
(log/babel-test-message "(filter mod (range 5))"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;Lazy Sequence;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect #"(?s)Expected a number, but a sequence was given instead\.(.*)"
(log/babel-test-message "(drop (range 20) (range 20))"))

(expect #"(?s)Expected a number, but a sequence was given instead\.(.*)"
(log/babel-test-message "(rand (range))"))

(expect #"(?s)Expected a number, but a sequence was given instead\.(.*)"
(log/babel-test-message "(rand-int (range 20))"))

(expect #"(?s)Expected a number, but a sequence was given instead\.(.*)"
(log/babel-test-message "(mod (range 5) (range 10))"))

(expect #"(?s)The function contains\? doesn't work on a sequence\.(.*)"
(log/babel-test-message "(contains? (range) 2)"))

(expect #"(?s)The function contains\? doesn't work on a sequence\.(.*)"
(log/babel-test-message "(contains? (drop 3 '(1 2 3)) 1)"))

(expect #"(?s)The function contains\? doesn't work on a sequence\.(.*)"
(log/babel-test-message "(contains? (seq [1 2 3]) 9)"))

(expect #"(?s)The function contains\? doesn't work on a string\.(.*)"
(log/babel-test-message "(contains?\"a\" (range))"))

;; This test will go away once the spec for max is fixed, will need to replace it by a function with custom-made
;; spec
(expect (t/make-pattern "The first argument of (max (9 0)) was expected to be a number but is a sequence (9 0) instead.")
(log/babel-test-message "(max (map first [[9] [0]]))"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;Pass Tests;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect nil (log/babel-test-message "(map char? \"abc\")")) ;; there should be no error for this

(expect nil (log/babel-test-message "(rand)"))

(expect nil (log/babel-test-message "(filter even? '(1 2 3))"))

(expect nil (log/babel-test-message "(filter 2 [])")) ;fails

(expect nil (log/babel-test-message "(take 2 \"hmmmm\")"))

(expect nil (log/babel-test-message "(conj)"))

(expect nil (log/babel-test-message "(contains? {:a nil} :a)"))

(expect nil (log/babel-test-message "(contains? [:a :b :c] 2)"))

(expect nil (log/babel-test-message "(contains? \"f\" 2)"))

(expect nil (log/babel-test-message "(contains? [1 2 3] (contains? \"a\" 1))"))

(expect nil (log/babel-test-message "(contains? [] '(1 2 3))"))
