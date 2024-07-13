(ns babel.test-print-eval
  (:require
   [expectations :refer [expect]]
   [logs.utils :as log]
   [babel.non-spec-test]
   [babel.utils-for-testing :as t]))

;#################################################
;### Tests for file names and line numbers. ######
;### Loads and calls functions from sample-files #
;#################################################

;; TO RUN tests, make sure you have repl started in a separate terminal

(expect #(not= % nil) (log/set-log babel.non-spec-test/to-log?))

(expect nil (log/add-log
              (do
                (def file-name "this file")
                (:file (meta #'file-name)))))

(expect (t/make-pattern "Wrong number of arguments in (even? 5 7): "
                        "the function even? expects one argument but was given two arguments."
                        #"(.*)"
                        "Called from the function: f; location unknown.")
(log/babel-test-message "(defn f [x] (lazy-seq (conj (even? x 7) [9 8])))
                        (f 5)"))

(expect (t/make-pattern "Wrong number of arguments in (even? 0 7): the function even? expects one argument but was given two arguments."
                        #"(.*)"
                        "Called from an anonymous function; location unknown.")
(log/babel-test-message "(#(lazy-seq (conj (even? % 7) [9])) 0)"))

;; Since we are reporting clojure.lang.Numbers function, we get isPos
;; instead of take.
(expect (t/make-pattern "Expected a number, but a sequence was given instead."
                        #"(.*)"
                        "In function: isPos; location unknown.")
(log/babel-test-message "(take (range) (range))"))

(expect (t/make-pattern "Expected a function, but a sequence was given instead."
                        #"(.*)"
                        "In function: keep; location unknown.")
(log/babel-test-message "(keep (range) [{:a 5} {:b 7}])"))

(expect (t/make-pattern "Expected a function, but a number was given instead."
                        #"(.*)"
                        "In function: keep_indexed; location unknown.")
(log/babel-test-message "(keep-indexed 7 [9 8])"))

(expect (t/make-pattern "This anonymous function cannot be called with one argument."
                        #"(.*)"
                        "In an anonymous function; location unknown.")
(log/babel-test-message "(lazy-seq (conj ((fn [x y] (+ x y)) 6) []))"))

(expect (t/make-pattern "This anonymous function cannot be called with one argument."
                        #"(.*)"
                        "In an anonymous function; location unknown.")
(log/babel-test-message "(lazy-seq (conj (#(+ %1 %2) 6) []))"))

(expect (t/make-pattern "This anonymous function cannot be called with one argument."
                        #"(.*)"
                        "In function: map; location unknown.")
(log/babel-test-message "(map #(7) [0])"))
