(ns sample-test-files.third-party-spec
  (:require
    [clojure.spec.alpha :as sc]
    [clojure.spec.test.alpha :as stest]
    [corefns.corefns]))

;; Has function and spec declarations for testing non-babel specs

(defn my-test-fn
  [n s]
  (str (inc n) " " s))

(sc/fdef my-test-fn :args (sc/cat :one int? :two string?))

(stest/instrument `my-test-fn)
