(ns babel.test-print-eval
  (:require
   [expectations :refer :all]
   [logs.utils :as log]
   [babel.non-spec-test :refer [to-log?]]
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
                        #"Called from the function: f; location unknown.")
(log/babel-test-message "(defn f [x] (lazy-seq (conj (even? x 7) [9 8])))
                        (f 5)"))

(expect (t/make-pattern "Wrong number of arguments, expected in (even? 6 7): the function even? expects one argument but was given two arguments"
                        #"(.*)"
                        #"Called from the function: f; location unknown.")
(log/babel-test-message "(defn f [x] (lazy-seq (conj (even? x 7) [9 8])))
                        (let [z (f 6)] (conj 6 z))"))

(expect (t/make-pattern "Expected a number, but a sequence was given instead."
                        #"(.*)"
                        #"In function: take; location unknown.")
(log/babel-test-message "(take (range) (range))"))
