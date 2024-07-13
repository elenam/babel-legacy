(ns babel.non-babel-spec-test
  (:require
    [logs.utils :as log]
    [babel.non-spec-test]
    [babel.utils-for-testing :as t]
    [expectations :refer [expect]]))

;############################################
;####### Tests for non-babel specs  #########
;############################################

;; TO RUN tests, make sure you have repl started in a separate terminal

(expect #(not= % nil)  (log/set-log babel.non-spec-test/to-log?))

(expect nil (log/add-log
              (do
                (def file-name "this file")
                (:file (meta #'file-name)))))

(expect (t/make-pattern "In (my-test-fn 3 4) the second argument, which is a number 4, fails a requirement: clojure.core/string?")
(log/babel-test-message "(load-file \"src/sample_test_files/third_party_spec.clj\")
                         (sample-test-files.third-party-spec/my-test-fn 3 4)"))

(expect (t/make-pattern "In (my-test-fn \"a\" \"b\") the first argument, which is a string \"a\", fails a requirement: clojure.core/int?")
(log/babel-test-message "(load-file \"src/sample_test_files/third_party_spec.clj\")
                        (sample-test-files.third-party-spec/my-test-fn \"a\" \"b\")"))

(expect (t/make-pattern "In (my-test-fn [[8 9 0...]] #(...)) the first argument, which is a vector [[8 9 0...]], fails a requirement: clojure.core/int?")
(log/babel-test-message "(load-file \"src/sample_test_files/third_party_spec.clj\")
                        (sample-test-files.third-party-spec/my-test-fn [[8 9 0 7 6]] #(+ %))"))

(expect (t/make-pattern "In (my-test-fn #(...) #(...)) the first argument, which is an anonymous function, fails a requirement: clojure.core/int?")
(log/babel-test-message "(load-file \"src/sample_test_files/third_party_spec.clj\")
                        (sample-test-files.third-party-spec/my-test-fn #(+ %) #(+ %))"))

(expect (t/make-pattern "Wrong number of arguments in (my-test-fn 3 \"a\" 5): the function my-test-fn requires fewer than three arguments.")
(log/babel-test-message "(load-file \"src/sample_test_files/third_party_spec.clj\")
                         (sample-test-files.third-party-spec/my-test-fn 3 \"a\" 5)"))

(expect (t/make-pattern "Wrong number of arguments in (my-test-fn 3): the function my-test-fn requires more than one arguments.")
(log/babel-test-message "(load-file \"src/sample_test_files/third_party_spec.clj\")
                        (sample-test-files.third-party-spec/my-test-fn 3)"))

(expect (t/make-pattern "Wrong number of arguments in (my-test-fn ): the function my-test-fn cannot be called with no arguments.")
(log/babel-test-message "(load-file \"src/sample_test_files/third_party_spec.clj\")
                        (sample-test-files.third-party-spec/my-test-fn)"))

(expect (t/make-pattern "In (my-test-fn2 (1 2 3)) the first argument, which is a list (1 2 3), fails a requirement: clojure.core/vector? or clojure.core/map?")
(log/babel-test-message "(load-file \"src/sample_test_files/third_party_spec.clj\")
                        (sample-test-files.third-party-spec/my-test-fn2 '(1 2 3))"))

(expect (t/make-pattern "In (my-test-fn3 [8 9]) the first argument, which is a vector [8 9], fails a requirement: (clojure.core/fn [%] (clojure.core/> (clojure.core/count %) 2))")
(log/babel-test-message "(load-file \"src/sample_test_files/third_party_spec.clj\")
                        (sample-test-files.third-party-spec/my-test-fn3 [8 9])"))

(expect (t/make-pattern "In (my-test-fn4 #(...)) the first argument, which is an anonymous function, fails a requirement: (clojure.core/fn [%] (clojure.core/instance? java.lang.Double %))")
(log/babel-test-message "(load-file \"src/sample_test_files/third_party_spec.clj\")
                        (sample-test-files.third-party-spec/my-test-fn4 #(+ %2))"))

(expect (t/make-pattern "In (my-test-fn5 #(...)) the first argument, which is an anonymous function, fails a requirement: clojure.core/int?")
(log/babel-test-message "(load-file \"src/sample_test_files/third_party_spec.clj\")
                        (sample-test-files.third-party-spec/my-test-fn5 #(+ %2))"))

(expect (t/make-pattern "In (my-test-fn5 \"a\") the first argument, which is a string \"a\", fails a requirement: clojure.core/int?")
(log/babel-test-message "(load-file \"src/sample_test_files/third_party_spec.clj\")
                        (sample-test-files.third-party-spec/my-test-fn6 [6 \"a\" 5])"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;; Testing line numbers & stacktrace ;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
