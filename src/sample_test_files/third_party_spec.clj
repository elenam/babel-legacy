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

(defn my-test-fn3
  [s]
  (map str s))

(sc/fdef my-test-fn3 :args (sc/cat :one (sc/and vector?  #(> (count %) 2))))

(stest/instrument `my-test-fn3)


(defn my-test-fn4
  [n]
  (+ n 6.7))

(sc/fdef my-test-fn4 :args (sc/cat :one #(instance? java.lang.Double %)))

(stest/instrument `my-test-fn4)

(defn my-test-fn5
  [n]
  (+ n 6.7))

(sc/def ::my-pred int?)

(sc/fdef my-test-fn5 :args (sc/cat :one ::my-pred))

(stest/instrument `my-test-fn5)

(defn my-test-fn6
  [s]
  (reduce + (map my-test-fn5 s)))
