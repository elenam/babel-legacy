(ns babel.test-line-numbers
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


(expect (t/make-pattern "Tried to divide by zero"
                        #"(.*)"
                        "In file sample1.clj on line 12.")
(log/babel-test-message "(load-file \"src/sample_test_files/sample1.clj\")
                         (sample-test-files.sample1/div0-test)"))

(expect (t/make-pattern "The second argument of (take 4 5) was expected to be a sequence but is a number 5 instead."
                        #"(.*)"
                        "In file sample1.clj on line 16.")
(log/babel-test-message "(load-file \"src/sample_test_files/sample1.clj\")
                        (sample-test-files.sample1/take-test)"))

(expect (t/make-pattern "The format of the number 8.5.1 is invalid."
                        #"(.*)"
                        "In file fragment1.clj on line 7 at position 6.")
(log/babel-test-message "(load-file \"src/sample_test_files/fragment1.clj\")"))

(expect (t/make-pattern "Expected a number, but a sequence was given instead."
                        #"(.*)"
                        "In function: isPos; location unknown.")
(log/babel-test-message "(load-file \"src/sample_test_files/sample1.clj\")
                        (sample-test-files.sample1/take-lazy-test)"))

(expect (t/make-pattern "The second argument of (map map map) was expected to be a sequence but is a function map instead."
                        #"(.*)"
                        "In file sample1.clj on line 28.")
(log/babel-test-message "(load-file \"src/sample_test_files/sample1.clj\")
                        (sample-test-files.sample1/map-spec-test)"))

;; This test doesn't seem to actually produce any error sometimes,
;; consider commenting it out - we are unsure what to do with it
;; (expect (t/make-pattern "Clojure ran out of memory, likely due to an infinite computation."
;;                         #"(.*)"
;;                         "In file sample1.clj on line 32.")
;; (log/babel-test-message "(load-file \"src/sample_test_files/sample1.clj\")
;;                         (sample-test-files.sample1/out-of-memory-test)"))

(expect (t/make-pattern "Expected a character, but a string was given instead."
                        #"(.*)"
                        "In file sample1.clj on line 40.")
(log/babel-test-message "(load-file \"src/sample_test_files/sample1.clj\")
                        (sample-test-files.sample1/compare-char-test)"))

(expect (t/make-pattern "The function f1 cannot be called with one argument."
                        #"(.*)"
                        "In file sample1.clj on line 48.")
(log/babel-test-message "(load-file \"src/sample_test_files/sample1.clj\")
                        (sample-test-files.sample1/arity-defn-test)"))

(expect (t/make-pattern "Tried to divide by zero"
                        #"(.*)"
                        "In function: divide; location unknown.")
(log/babel-test-message "(load-file \"src/sample_test_files/sample1.clj\")
                        (sample-test-files.sample1/div-0-in-map-test)"))

(expect (t/make-pattern "The first argument of (even? s) was expected to be a number but is a character s instead."
                        #"(.*)"
                        #"Called from the function: filter; location unknown\.")
(log/babel-test-message "(load-file \"src/sample_test_files/sample1.clj\")
                        (sample-test-files.sample1/spec-in-filter-test)"))

(expect (t/make-pattern "Syntax problems in defn: instead of [[] a] you need a list."
                        #"(.*)"
                        "In file fragment3.clj on line 7 at position 1.")
(log/babel-test-message "(load-file \"src/sample_test_files/fragment3.clj\")"))
