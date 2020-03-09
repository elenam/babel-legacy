(ns babel.test-line-numbers
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
                        "In file sample1.clj on line 5")
(log/babel-test-message "(load-file \"src/sample_test_files/sample1.clj\")
                         (sample-test-files.sample1/div0-test)"))
