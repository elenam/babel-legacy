(ns corefns.corefns
 (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.spec.gen.alpha :as gen]))

;##### Length Functions ##### Credit to Tony Song (frogrammer)
(defn babel-length1? [coll] (= (count coll) 1))
(defn babel-length2? [coll] (= (count coll) 2))
(defn babel-length3? [coll] (= (count coll) 3))
(defn babel-length0-or-1? [coll] (or (= (count coll) 0)
                                     (= (count coll) 1)))
(defn babel-length-greater0? [coll] (> (count coll) 0))
(defn babel-length-greater1? [coll] (> (count coll) 1))
(defn babel-length-greater2? [coll] (> (count coll) 2))

(s/def ::babel-length-one babel-length1?)
(s/def ::babel-length-two babel-length2?)
(s/def ::babel-length-three babel-length3?)
(s/def ::babel-length-greater-zero babel-length-greater0?)
(s/def ::babel-length-greater-one babel-length-greater1?)
(s/def ::babel-length-greater-two babel-length-greater2?)
(s/def ::babel-length-zero-or-one babel-length0-or-1?)


(s/def ::length-one-anything (s/and ::babel-length-one (s/cat :a any?)))
(s/def ::length-one-number (s/and ::babel-length-one (s/cat :a number?)))
(s/def ::length-greater-zero-number (s/and ::babel-length-greater-zero (s/cat :a (s/+ number?))))

;##### Specs #####
(s/fdef clojure.core/+
  :args (s/cat :checknum (s/* number?)))
(stest/instrument `clojure.core/+)

(s/fdef clojure.core/-
  :args (s/and ::babel-length-greater-zero
               (s/cat :checknum (s/+ number?))))
(stest/instrument `clojure.core/-)

(s/fdef clojure.core/*
  :args (s/cat :checknum (s/* number?)))
(stest/instrument `clojure.core/*)

#_(s/fdef inc ;need to figure out how to deal with the inline, normal fix does not work here
  :args ::length-one-number)
;(stest/instrument `inc)

(s/fdef clojure.core/max
  :args ::length-greater-zero-number)
(stest/instrument `clojure.core/max)

(s/fdef clojure.core/min
  :args ::length-greater-zero-number)
(stest/instrument `clojure.core/min)

(s/fdef clojure.core/rand
  :args (s/and ::babel-length-zero-or-one (s/cat :a (s/? number?))))
(stest/instrument `clojure.core/rand)

(s/fdef clojure.core/rand-int
  :args (s/and ::babel-length-one (s/cat :a number?)))
(stest/instrument `clojure.core/rand-int)

(s/fdef clojure.core//
  :args (s/and ::babel-length-greater-zero
               (s/or :a (s/cat :checkfirst (s/and int? #(not= % 0)))
                     :b (s/cat :checkfirst int? :checkafter (s/+ (s/and int? #(not= % 0)))))))
(stest/instrument `clojure.core//)

(s/fdef clojure.core/string? ;use this as a base for functions like this
  :args ::length-one-anything)
(stest/instrument `clojure.core/string?)

(s/fdef clojure.core/even?
  :args ::length-one-number)
(stest/instrument `clojure.core/even?)

(s/fdef clojure.core/conj
  :args (s/and ::babel-length-greater-zero
               (s/or :a (s/cat :a any?)
                     :b (s/cat :a seqable? :b (s/+ any?)))))
(stest/instrument `clojure.core/conj)

(s/fdef clojure.core/map
  :args (s/and ::babel-length-greater-zero
               (s/or :a (s/cat :a any?)
                     :b (s/cat :a ifn? :b (s/* seqable?)))))
(stest/instrument `clojure.core/map)

(s/fdef clojure.core/mod
  :args (s/and ::babel-length-two
               (s/cat :a number? :b number?) (fn [{:keys [a b]}] (not= b 0))))
(stest/instrument `clojure.core/mod)


;##### Inline Functions #####

#_(defn inc [argument1]
  (clojure.core/inc argument1))
