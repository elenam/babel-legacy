(ns babel.non-babel-spec-test
  (:require
    [logs.utils :as log]
    [babel.non-spec-test :refer [to-log?]]
    [babel.utils-for-testing :as t]
    [expectations :refer :all]))

;############################################
;####### Tests for non-babel specs  #########
;############################################

;; TO RUN tests, make sure you have repl started in a separate terminal

(expect #(not= % nil)  (log/set-log babel.non-spec-test/to-log?))

(expect nil (log/add-log
              (do
                (def file-name "this file")
                (:file (meta #'file-name)))))

(expect (t/make-pattern "Tried to divide by zero"
                        #"(.*)"
                        "In file sample1.clj on line 12.")
(log/babel-test-message "(load-file \"src/sample_test_files/third_party_spec.clj\")
                         (my-test-fn 3 4)"))
