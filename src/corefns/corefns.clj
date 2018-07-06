(ns corefns.corefns
 (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.spec.gen.alpha :as gen]))

;##### Length Functions ##### Credit to Tony Song (frogrammer)
(defn b-length1? [coll] (= (count coll) 1))
(defn b-length2? [coll] (= (count coll) 2))
(defn b-length3? [coll] (= (count coll) 3))
(defn b-length0-or-1? [coll] (or (= (count coll) 0)
                                     (= (count coll) 1)))
(defn b-length-greater0? [coll] (> (count coll) 0))
(defn b-length-greater1? [coll] (> (count coll) 1))
(defn b-length-greater2? [coll] (> (count coll) 2))
(defn b-not-0? [num] (not= num 0))

(s/def ::b-length-one b-length1?)
(s/def ::b-length-two b-length2?)
(s/def ::b-length-three b-length3?)
(s/def ::b-length-greater-zero b-length-greater0?)
(s/def ::b-length-greater-one b-length-greater1?)
(s/def ::b-length-greater-two b-length-greater2?)
(s/def ::b-length-zero-or-one b-length0-or-1?)


(s/def ::length-one-anything (s/and ::b-length-one (s/cat :any any?)))
(s/def ::length-one-number (s/and ::b-length-one (s/cat :number number?)))
(s/def ::length-greater-zero-number (s/and ::b-length-greater-zero (s/cat :number (s/+ number?))))
(s/def ::b-not-zero b-not-0?)

;##### Specs #####
(s/fdef clojure.core/+ ;inline issue
  :args (s/cat :checknum (s/* number?)))
(stest/instrument `clojure.core/+)

(s/fdef clojure.core/- ;inline issue
  :args (s/and ::b-length-greater-zero
               (s/cat :checknum (s/+ number?))))
(stest/instrument `clojure.core/-)

(s/fdef clojure.core/* ;inline issue
  :args (s/cat :checknum (s/* number?)))
(stest/instrument `clojure.core/*)

#_(s/fdef inc ;need to figure out how to deal with the inline, normal fix does not work here
  :args ::length-one-number)
;(stest/instrument `inc)

(s/fdef clojure.core/max ;inline issue
  :args ::length-greater-zero-number)
(stest/instrument `clojure.core/max)

(s/fdef clojure.core/min ;inline issue
  :args ::length-greater-zero-number)
(stest/instrument `clojure.core/min)

(s/fdef clojure.core/rand
  :args (s/and ::b-length-zero-or-one (s/cat :a (s/? number?))))
(stest/instrument `clojure.core/rand)

(s/fdef clojure.core/rand-int
  :args (s/and ::b-length-one (s/cat :a number?)))
(stest/instrument `clojure.core/rand-int)

(s/fdef clojure.core// ;check inline
  :args (s/and ::b-length-greater-zero
               (s/or :a (s/cat :checkfirst (s/and int? ::b-not-zero))
                     :b (s/cat :checkfirst int? :checkafter (s/+ (s/and int? #(not= % 0))))))) ;this part does not work for zero
(stest/instrument `clojure.core//)

(s/fdef clojure.core/string? ;use this as a base for functions like this
  :args ::length-one-anything)
(stest/instrument `clojure.core/string?)

(s/fdef clojure.core/even?
  :args ::length-one-number)
(stest/instrument `clojure.core/even?)

(s/fdef clojure.core/conj
  :args (s/and ::b-length-greater-zero
               (s/or :any (s/cat :any any?)
                     :seqableandany (s/cat :seqable seqable? :any (s/+ any?)))))
(stest/instrument `clojure.core/conj)

(s/fdef clojure.core/map
  :args (s/and ::b-length-greater-zero
               (s/cat :function ifn? :seqable (s/* seqable?)))) ;change to a + to block transducers
(stest/instrument `clojure.core/map)

(s/fdef clojure.core/mod
  :args (s/and ::b-length-two
               (s/cat :number number? :number (s/and number? ::b-not-zero)))) ;(fn [{:keys [a b]}] (not= b 0))))
(stest/instrument `clojure.core/mod)


;##### Inline Functions #####

#_(defn inc [argument1]
  (clojure.core/inc argument1))
