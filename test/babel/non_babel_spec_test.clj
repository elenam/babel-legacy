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

(expect (t/make-pattern "In (my-test-fn 3 4) the second argument 4 fails a requirement: clojure.core/string?")
(log/babel-test-message "(load-file \"src/sample_test_files/third_party_spec.clj\")
                         (sample-test-files.third-party-spec/my-test-fn 3 4)"))

(expect (t/make-pattern "In (my-test-fn \"a\" \"b\") the first argument \"a\" fails a requirement: clojure.core/int?")
(log/babel-test-message "(load-file \"src/sample_test_files/third_party_spec.clj\")
                        (sample-test-files.third-party-spec/my-test-fn \"a\" \"b\")"))

(expect (t/make-pattern "In (my-test-fn #(...) #(...)) the first argument #(...) fails a requirement: clojure.core/int?")
(log/babel-test-message "(load-file \"src/sample_test_files/third_party_spec.clj\")
                        (sample-test-files.third-party-spec/my-test-fn #(+ %) #(+ %))"))
