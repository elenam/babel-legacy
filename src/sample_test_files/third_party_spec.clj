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

;; Actually, without sc/cat it checks the sequence of args,
;; so it fails on a call (my-test-fn2 [2 3]) since '([2 3])
;; isn't a vector or a map
; ;; Without sc/cat:
; (defn my-test-fn2
;   [s]
;   (map str s))
;
; (sc/fdef my-test-fn2 :args (sc/or :vector vector? :map map?))
;
; (stest/instrument `my-test-fn2)

;; With sc/cat:
(defn my-test-fn2
  [s]
  (map str s))

(sc/fdef my-test-fn2 :args (sc/cat :one (sc/or :vector vector? :map map?)))

(stest/instrument `my-test-fn2)
