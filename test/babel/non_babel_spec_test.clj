(ns babel.non-babel-spec-test
  (:require
    [logs.utils :as log]
    [babel.non-spec-test :refer [to-log?]]
    [babel.utils-for-testing :as t]
    [clojure.spec.alpha :as sc]
    [clojure.spec.test.alpha :as stest]
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


(defn my-test-fn
  [n s]
  (str (inc n) " " s))

(sc/fdef my-test-fn :args (sc/cat :one int? :two string?))

(stest/instrument `my-test-fn)


(expect (t/make-pattern "????")
(log/babel-test-message "(babel.non-babel-spec-test/my-test-fn 3 4)"))
