(ns corefns.corefns
 (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.spec.gen.alpha :as gen]
            [clojure.core.specs.alpha :as sp]))

;##### Length Functions ##### Credit to Tony Song (frogrammer)
(defn b-length1? [coll] (= (count coll) 1))
(defn b-length2? [coll] (= (count coll) 2))
(defn b-length3? [coll] (= (count coll) 3))
(defn b-length0-to-1? [coll] (or (= (count coll) 0)
                                     (= (count coll) 1)))
(defn b-length1-to-2? [coll] (or (= (count coll) 1)
                                     (= (count coll) 2)))
(defn b-length2-to-3? [coll] (or (= (count coll) 2)
                                     (= (count coll) 3)))
(defn b-length0-to-3? [coll] (and (>= (count coll) 0)
                                     (<= (count coll) 3)))
(defn b-length-0greater? [coll] (>= (count coll) 0))
(defn b-length-greater0? [coll] (> (count coll) 0))
(defn b-length-greater1? [coll] (> (count coll) 1))
(defn b-length-greater2? [coll] (> (count coll) 2))
(defn b-not-0? [num] (not= num 0))
#_(defn b-not-greater-count [str num1 & [num2]] (let [strc (count str)]
                                               (if num2
                                                  (and (>= strc num1) (>= strc num2) (>= num2 num1))
                                                  (>= strc num1))))

(s/def ::b-length-one b-length1?)
(s/def ::b-length-two b-length2?)
(s/def ::b-length-three b-length3?)
(s/def ::b-length-zero-or-greater b-length-0greater?)
(s/def ::b-length-greater-zero b-length-greater0?)
(s/def ::b-length-greater-one b-length-greater1?)
(s/def ::b-length-greater-two b-length-greater2?)
(s/def ::b-length-zero-to-one b-length0-to-1?)
(s/def ::b-length-one-to-two b-length1-to-2?)
(s/def ::b-length-two-to-three b-length2-to-3?)
(s/def ::b-length-zero-to-three b-length0-to-3?)

(s/def ::length-one-anything (s/and ::b-length-one (s/cat :any any?)))
(s/def ::length-one-number (s/and ::b-length-one (s/cat :number number?)))
(s/def ::length-greater-zero-number (s/and ::b-length-greater-zero (s/cat :number (s/+ number?))))
(s/def ::b-not-zero b-not-0?)
#_(s/def ::b-not-greater-str-count b-not-greater-count)

(s/def ::bindings-seq2 (s/and vector? ::binding-seq))
(s/def ::binding-seq (s/cat :a :clojure.core.specs.alpha/binding-form :b (s/nilable coll?)))

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
#_(stest/instrument `inc)

(s/fdef clojure.core/max ;inline issue
  :args ::length-greater-zero-number)
(stest/instrument `clojure.core/max)

(s/fdef clojure.core/min ;inline issue
  :args ::length-greater-zero-number)
(stest/instrument `clojure.core/min)

(s/fdef clojure.core/rand
  :args (s/and ::b-length-zero-to-one (s/cat :a (s/? number?))))
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

(s/fdef clojure.core/odd?
  :args ::length-one-number)
(stest/instrument `clojure.core/odd?)

(s/fdef clojure.core/conj
  :args (s/and ::b-length-greater-zero
               (s/or :any (s/cat :collection (s/nilable coll?)) ;conj can take anything but the intent of conj is that a single argument will be a collection
                     :collectionandany (s/cat :collection (s/nilable coll?) :any (s/+ any?)))))
(stest/instrument `clojure.core/conj)

(s/fdef clojure.core/into
  :args (s/and ::b-length-zero-to-three
               (s/or :c (s/cat :a (s/nilable coll?) :b ifn? :c seqable?)
                     :b (s/cat :a (s/nilable coll?) :b seqable?)
                     :a (s/cat :a (s/? seqable?)))))
(stest/instrument `clojure.core/into)

(s/fdef clojure.core/map
  :args (s/and ::b-length-greater-zero
               (s/cat :function ifn? :collections (s/* seqable?)))) ;change to a + to block transducers
(stest/instrument `clojure.core/map)

(s/fdef clojure.core/mod
  :args (s/and ::b-length-two
               (s/cat :number number? :number (s/and number? ::b-not-zero)))) ;(fn [{:keys [a b]}] (not= b 0))))
(stest/instrument `clojure.core/mod)

(s/fdef clojure.core/numerator
  :args (s/cat :a ratio?))
(stest/instrument `clojure.core/numerator)

(s/fdef clojure.core/denominator
  :args (s/cat :a ratio?))
(stest/instrument `clojure.core/denominator)

(s/fdef clojure.core/subs ;incomplete
  :args (s/and ::b-length-two-to-three
               (s/or :a (s/cat :str1 string? :int1 int?) ;(s/and (s/cat :str1 string? :int1 int?) (fn [{:keys [str1 int1]}] (b-not-greater-count str1 int1)))
                     :b (s/cat :str2 string? :int2 int? :int3 int?)))) ;(s/and (s/cat :str2 string? :int2 int? :int3 int?) (fn [{:keys [str2 int2 int3]}] (b-not-greater-count str2 int2 int3))))))
(stest/instrument `clojure.core/subs)

(s/fdef clojure.core/denominator
  :args (s/or :a (s/cat :a ratio?)))
(stest/instrument `clojure.core/denominator)

(s/fdef clojure.core/reduce
  :args (s/and ::b-length-two-to-three (s/or :a (s/cat :a ifn? :a (s/nilable coll?))
  :a (s/cat :a ifn? :a any? :a (s/nilable coll?)))))
(stest/instrument `clojure.core/reduce)

(s/fdef clojure.core/get-in :args (s/and ::b-length-two-to-three (s/or :a (s/cat :a (s/nilable coll?) :a (s/nilable coll?))
  :a (s/cat :a (s/nilable coll?) :a (s/nilable coll?) :a any?))))
(stest/instrument `clojure.core/get-in)

(s/fdef clojure.core/var-get :args (s/and ::b-length-one (s/or :a (s/cat :a var?))))
(stest/instrument `clojure.core/var-get)

(s/fdef clojure.core/future-cancel :args (s/and ::b-length-one (s/or :a (s/cat :a future?))))
(stest/instrument `clojure.core/future-cancel)

(s/fdef clojure.core/->> :args (s/and ::b-length-greater-zero (s/or :a (s/cat :a (s/+ any?)))))
(stest/instrument `clojure.core/->>)

(s/fdef clojure.core/if-some
  :args (s/and ::b-length-two-to-three
               (s/or :a (s/cat :bindings :clojure.core.specs.alpha/bindings :a any?)
                     :b (s/cat :bindings :clojure.core.specs.alpha/bindings :a any? :b any?))))
(stest/instrument `clojure.core/if-some)

(s/fdef clojure.core/when-first :args (s/and ::b-length-two (s/or :a (s/cat :a ::bindings-seq2 :a any?))))
(stest/instrument `clojure.core/when-first)

(s/fdef clojure.core/gen-class :args (s/and ::b-length-zero-or-greater (s/cat :a (s/* any?))))
(stest/instrument `clojure.core/gen-class)

(s/fdef clojure.core/while :args (s/and ::b-length-greater-zero (s/or :a (s/cat :a (s/* any?)))))
(stest/instrument `clojure.core/while)

(s/fdef clojure.core/pvalues :args (s/and ::b-length-zero-or-greater (s/cat :a (s/* any?))))
(stest/instrument `clojure.core/pvalues)

(s/def ::innervector (s/cat :a symbol? :b (s/* (s/cat :a keyword :b (s/or :a symbol?
                                                                          :b (s/nilable coll?))))))
(s/def ::requiredlist (s/or :a (s/cat :a symbol?)
                            :b (s/cat :a symbol? :b (s/and vector? ::innervector))))
(s/def ::requirelist (s/and list? ::requiredlist))
(s/def ::requiredvector (s/or :a (s/cat :a symbol?)
                              :b (s/cat :a symbol? :b (s/* (s/cat :a keyword? :b (s/or :a symbol?
                                                                                       :b (s/nilable coll?)
                                                                                       :c keyword?))))
                              :c (s/cat :a symbol? :b (s/and vector? ::innervector))))
(s/def ::requirevector (s/and vector? ::requiredvector))
(s/def ::importlists (s/and (s/nilable coll?) ::importlist))
(s/def ::importlist (s/cat :a (s/+ (s/or :a string?
                                         :b symbol?
                                         :c class?
                                         :d ::quotelist))))
(s/def ::quotelist (s/cat :a (s/+ (s/or :a string?
                                        :b symbol?
                                        :c class?))))

(s/fdef clojure.core/require
  :args (s/and ::b-length-greater-zero
               (s/+ (s/cat :a (s/or :a ::requirelist :b ::requirevector :c symbol? :d class?) :b (s/* keyword?)))))
(stest/instrument `clojure.core/require)

(s/fdef clojure.core/use
  :args (s/and ::b-length-greater-zero
               (s/cat :a (s/+ (s/or :a ::requirelist :b ::requirevector :c symbol? :d class?)) :b (s/* keyword?))))
(stest/instrument `clojure.core/use)

(s/fdef clojure.core/refer
  :args (s/and ::b-length-greater-zero
               (s/cat :a symbol? :b (s/* (s/cat :a keyword? :b (s/* (s/nilable coll?)))))))
(stest/instrument `clojure.core/refer)
