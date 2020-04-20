(ns corefns.corefns
 (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.spec.gen.alpha :as gen]
            [clojure.core.specs.alpha :as sp]
            [clojure.set]))

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
(defn b-length2-to-4? [coll] (or (= (count coll) 2)(= (count coll) 3)
  (= (count coll) 4)))

(defn b-length0-to-3? [coll] (and (>= (count coll) 0)
                                     (<= (count coll) 3)))
(defn b-length1-to-3? [coll] (and (>= (count coll) 1)
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
(defn greater-than-zero? [number] (and (number? number)(< 0 number)))

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
(s/def ::b-length-two-to-four b-length2-to-4?)
(s/def ::b-length-one-to-three b-length1-to-3?)
(s/def ::b-length-zero-to-three b-length0-to-3?)

;#########Lazy functions############
(defn not-map? [coll] (and (coll? coll) (not (map? coll))))
(s/def ::not-map not-map?)

(defn lazy? [lazy-sequence] (or (instance? clojure.lang.IChunkedSeq lazy-sequence)
                                (instance? clojure.lang.IPending lazy-sequence)))
(s/def ::lazy lazy?)

(s/def ::function-or-lazy (s/alt :function ifn? :lazy ::lazy))
(s/def ::number-or-lazy (s/alt :num number? :lazy ::lazy))
(s/def ::string-or-lazy (s/alt :num string? :lazy ::lazy))
(s/def ::map-vec-or-lazy (s/alt :or (s/alt :map map? :vector vector?) :lazy ::lazy))
(s/def ::any-or-lazy (s/alt :any any? :lazy ::lazy))
(s/def ::greater-than-zero greater-than-zero?)
(s/def ::b-not-zero-or-lazy (s/alt :num-non-zero (s/and number? b-not-0?) :lazy ::lazy))

(defn regex2? [regex] (instance? java.util.regex.Pattern regex))
(s/def ::regex-or-lazy (s/alt :regex regex2? :lazy ::lazy))

(s/def ::number-or-collection (s/alt :arg-one ::number-or-lazy
                                     :arg-two (s/cat :number ::number-or-lazy
                                                     :collection (s/nilable seqable?))))

;;; ###################### Length-with-lazy ##############################

(s/def ::length-one-anything (s/and ::b-length-one (s/cat :any any?)))
(s/def ::length-one-number (s/and ::b-length-one (s/cat :number ::number-or-lazy)))
(s/def ::length-greater-zero-number (s/and ::b-length-greater-zero (s/cat :number (s/+ number?))))
#_(s/def ::b-not-greater-str-count b-not-greater-count)

(s/def ::bindings-seq2 (s/and vector? ::binding-seq))
(s/def ::binding-seq (s/cat :a :clojure.core.specs.alpha/binding-form :b (s/or :a (s/nilable coll?)
                                                                              :b symbol?)))




;##### Specs #####
(s/fdef clojure.core/+ ;inline issue
  :args (s/cat ::number-or-lazy (s/* ::number-or-lazy)))
(stest/instrument `clojure.core/+)

(s/fdef clojure.core/- ;inline issue
  :args (s/and ::b-length-greater-zero
               (s/cat :number (s/+ number?))))
(stest/instrument `clojure.core/-)

(s/fdef clojure.core/* ;inline issue
  :args (s/cat :number (s/* number?)))
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
  :args (s/and ::b-length-zero-to-one (s/cat :number (s/? ::number-or-lazy))))
(stest/instrument `clojure.core/rand)

(s/fdef clojure.core/rand-int
  :args (s/and ::b-length-one (s/cat :number ::number-or-lazy)))
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
  :args  ::length-one-number)
(stest/instrument `clojure.core/even?)

(s/fdef clojure.core/odd?
  :args ::length-one-number)
(stest/instrument `clojure.core/odd?)

(s/fdef clojure.core/conj
  :args (s/and ::b-length-zero-or-greater
               (s/or :any (s/cat :any (s/? (s/nilable any?))) ;conj can take anything but the intent of conj is that a single argument will be a collection
                     :map-arg (s/cat :collection-map map? :sequence (s/alt :map map? :vec (s/* (s/coll-of any? :kind vector? :count 2))))
                     :collection (s/cat :collection (s/nilable ::not-map) :any (s/+ any?))
                    )))
(stest/instrument `clojure.core/conj)

(s/fdef clojure.core/into
  :args (s/and ::b-length-zero-to-three
               (s/or :arg-one (s/cat :any (s/? any?))
                     :arg-two (s/cat :coll (s/nilable coll?) :any any?)
                     :arg-three (s/cat :coll (s/nilable coll?) :function ::function-or-lazy :coll any?)
                     )))
(stest/instrument `clojure.core/into)

(s/fdef clojure.core/map
  :args (s/and ::b-length-greater-zero
               (s/cat :function any? :collection (s/* seqable?)))) ;change to a + to block transducers
(stest/instrument `clojure.core/map)

(s/fdef clojure.core/mod
  :args (s/and ::b-length-two
               (s/cat :number ::number-or-lazy :number ::b-not-zero-or-lazy)))
(stest/instrument `clojure.core/mod)

(s/fdef clojure.core/numerator
  :args (s/cat :ratio ratio?))
(stest/instrument `clojure.core/numerator)

(s/fdef clojure.core/denominator
  :args (s/cat :ratio ratio?))
(stest/instrument `clojure.core/denominator)

(s/fdef clojure.core/subs ;incomplete
  :args (s/and ::b-length-two-to-three
               (s/or :arg-one (s/cat :string ::string-or-lazy :integer int?)
                     :arg-two (s/cat :string ::string-or-lazy :integer int? :integer int?))))
(stest/instrument `clojure.core/subs)

(s/fdef clojure.core/reduce
  :args (s/and ::b-length-two-to-three
    (s/or :arg-one (s/cat :function ::function-or-lazy :collection (s/nilable coll?))
          :arg-two (s/cat :function ::function-or-lazy :value any? :collection (s/nilable coll?)))))
(stest/instrument `clojure.core/reduce)

(s/fdef clojure.core/get-in
  :args (s/and ::b-length-two-to-three
    (s/or :arg-one (s/cat :collection (s/nilable coll?) :collection (s/nilable coll?))
          :arg-two (s/cat :collection (s/nilable coll?) :collection (s/nilable coll?) :value any?))))
(stest/instrument `clojure.core/get-in)

#_(s/fdef clojure.core/var-get
  :args (s/and ::b-length-one
    (s/or :arg-one (s/cat :value var?))))
#_(stest/instrument `clojure.core/var-get)

#_(s/fdef clojure.core/future-cancel
  :args (s/and ::b-length-one
    (s/or :arg-one (s/cat :future future?))))
#_(stest/instrument `clojure.core/future-cancel)

; (s/fdef clojure.core/->>
;   :args (s/and ::b-length-greater-zero
;     (s/or :arg-one (s/cat :value (s/+ any?)))))
; (stest/instrument `clojure.core/->>)

; (s/fdef clojure.core/if-some
;   :args (s/and ::b-length-two-to-three
;                (s/or :arg-one (s/cat :bindings :clojure.core.specs.alpha/bindings :value any?)
;                      :arg-two (s/cat :bindings :clojure.core.specs.alpha/bindings :value any? :value any?))))
; (stest/instrument `clojure.core/if-some)

(s/fdef clojure.core/gen-class
  :args (s/and ::b-length-zero-or-greater
    (s/cat :arg-one (s/* any?))))
(stest/instrument `clojure.core/gen-class)

(s/fdef clojure.core/while
  :args (s/and ::b-length-greater-zero
    (s/or :arg-one (s/cat :value (s/* any?)))))
(stest/instrument `clojure.core/while)

(s/fdef clojure.core/pvalues
  :args (s/and ::b-length-zero-or-greater
    (s/cat :value (s/* any?))))
(stest/instrument `clojure.core/pvalues)

(s/fdef clojure.core/identical?
  :args (s/and ::b-length-two
    (s/cat :value (s/nilable any?) :value (s/nilable any?))))
(stest/instrument `clojure.core/identical?)

(s/fdef clojure.core/contains?
  :args (s/and ::b-length-two
    (s/alt :arg-one (s/cat :only-collection (s/alt :map (s/nilable map?) :set (s/nilable set?) :vector (s/nilable vector?) :lazy ::lazy) :any (s/nilable any?))
          :arg-two (s/cat :string (s/nilable string?) :number (s/alt :number (s/nilable number?) :lazy ::lazy)))))
(stest/instrument `clojure.core/contains?)

(s/fdef clojure.core/filter
  :args (s/and ::b-length-one-to-two
    (s/or :arg-one (s/cat :function ::any-or-lazy :collection (s/nilable seqable?))
          :arg-two (s/cat :function ::function-or-lazy))))
(stest/instrument `clojure.core/filter)

(s/fdef clojure.core/take
  :args (s/and ::b-length-one-to-two
          (s/or :arg-one (s/cat :number ::number-or-lazy)
                :arg-two (s/cat :number ::number-or-lazy :collection (s/nilable seqable?)))))
(stest/instrument `clojure.core/take)

(s/fdef clojure.core/take-nth
  :args (s/and ::b-length-one-to-two
    (s/or :arg-one (s/cat :number ::number-or-lazy)
          :arg-two (s/cat :number-greater-than-zero ::greater-than-zero :collection (s/nilable seqable?)))))
(stest/instrument `clojure.core/take-nth)

(s/fdef clojure.core/take-last
  :args (s/and ::b-length-one-to-two
    (s/cat :number ::number-or-lazy :collection (s/nilable seqable?))))
(stest/instrument `clojure.core/take-last)

(s/fdef clojure.core/take-while
  :args (s/and ::b-length-one-to-two
        (s/or :arg-one (s/cat :function ::function-or-lazy)
              :arg-two (s/cat :function ::function-or-lazy :collection (s/nilable seqable?)))))
(stest/instrument `clojure.core/take-while)

(s/fdef clojure.core/drop
  :args (s/and ::b-length-one-to-two
        (s/or :arg-one (s/cat :number ::number-or-lazy)
              :arg-two (s/cat :number ::number-or-lazy :collection (s/nilable seqable?)))))
(stest/instrument `clojure.core/drop)

(s/fdef clojure.core/drop-last
  :args (s/and ::b-length-one-to-two
               (s/alt :arg-one (s/cat :collection (s/nilable seqable?))
                      :arg-two (s/cat :number ::number-or-lazy :collection (s/nilable seqable?)))))
(stest/instrument `clojure.core/drop-last)

(s/fdef clojure.core/drop-while
  :args (s/and ::b-length-one-to-two
    (s/or :arg-one (s/cat :function ::function-or-lazy)
          :arg-two (s/cat :function ::function-or-lazy :collection (s/nilable seqable?)))))
(stest/instrument `clojure.core/drop-while)

(s/fdef clojure.core/remove
  :args (s/and ::b-length-one-to-two
    (s/or :arg-one (s/cat :function ::function-or-lazy)
          :arg-two (s/cat :function ::function-or-lazy :collection (s/nilable seqable?)))))
(stest/instrument `clojure.core/remove)

(s/fdef clojure.core/group-by
  :args (s/and ::b-length-two
    (s/cat :function ::function-or-lazy :collection (s/nilable seqable?))))
(stest/instrument `clojure.core/group-by)

(s/fdef clojure.core/replace
  :args (s/and ::b-length-one-to-two
    (s/or :arg-one (s/cat :map-or-vector (s/nilable ::map-vec-or-lazy))
          :arg-two (s/cat :map-or-vector (s/nilable ::map-vec-or-lazy) :collection (s/nilable seqable?)))))
(stest/instrument `clojure.core/replace)

(s/fdef clojure.core/keep
  :args (s/and ::b-length-one-to-two
    (s/or :arg-one (s/cat :function ::function-or-lazy)
          :arg-two (s/cat :function ::function-or-lazy :collection (s/nilable seqable?)))))
(stest/instrument `clojure.core/keep)

(s/fdef clojure.core/partition
  :args (s/and ::b-length-two-to-four
    (s/or
      :arg-one (s/cat :number ::number-or-lazy :collection (s/nilable seqable?))
      :arg-two (s/cat :number ::number-or-lazy :number ::number-or-lazy :collection (s/nilable seqable?))
      :arg-three (s/cat :number ::number-or-lazy :number ::number-or-lazy :value any? :collection (s/nilable seqable?)))))
(stest/instrument `clojure.core/partition)

(s/fdef clojure.core/partition-by
  :args (s/and ::b-length-one-to-two
          (s/or :arg-one (s/cat :function ::function-or-lazy)
                :arg-two (s/cat :function ::function-or-lazy :collection (s/nilable seqable?)))))
(stest/instrument `clojure.core/partition-by)

(s/fdef clojure.core/partition-all :args
  (s/and ::b-length-one-to-three
    (s/or :arg-one (s/cat :number ::number-or-lazy)
          :arg-two (s/cat :number ::number-or-lazy :collection (s/nilable seqable?))
          :arg-three  (s/cat :number ::number-or-lazy :number ::number-or-lazy :collection (s/nilable seqable?)))))
;(stest/instrument `clojure.core/partition-all)

(s/fdef clojure.string/split
  :args (s/and ::b-length-two-to-three
    (s/or :arg-one (s/cat :string ::string-or-lazy :regex ::regex-or-lazy)
          :arg-two (s/cat :string ::string-or-lazy :regex ::regex-or-lazy :number ::number-or-lazy))))
(stest/instrument `clojure.string/split)

(s/fdef clojure.core/comp
  :args (s/and ::b-length-greater-zero
               (s/cat :function (s/* any?))))
(stest/instrument `clojure.core/comp)

#_(s/fdef clojure.core/int
  :args (s/and ::b-length-one
               (s/or :number number? :char char?)))
#_(stest/instrument `clojure.core/int)

(s/def ::innervector (s/cat :symbol symbol? :b (s/* (s/cat :key keyword :symbol-or-collection (s/or :symbol symbol?
                                                                          :collection (s/nilable coll?))))))
(s/def ::requiredlist (s/or :arg-one (s/cat :symbol (s/* symbol?))
                            :arg-two (s/cat :symbol symbol? :vector (s/and vector? ::innervector))))
(s/def ::requirelist (s/and list? ::requiredlist))
(s/def ::requiredvector (s/or :arg-one (s/cat :symbol symbol?)
                              :arg-two (s/cat :symbol symbol? :b (s/* (s/cat :key keyword? :b (s/or :symbol symbol?
                                                                                                    :collection (s/nilable coll?)
                                                                                                    :key keyword?))))
                              :arg-three (s/cat :symbol symbol? :vector (s/and vector? ::innervector))))
(s/def ::requirevector (s/and vector? ::requiredvector))
(s/def ::importlists (s/and (s/nilable coll?) ::importlist))
(s/def ::importlist (s/cat :a (s/+ (s/or :string string?
                                         :symbol symbol?
                                         :class class?
                                         :quotelist ::quotelist))))
(s/def ::quotelist (s/cat :a (s/+ (s/or :string string?
                                        :symbol symbol?
                                        :class class?))))

#_(s/fdef clojure.core/require
  :args (s/and ::b-length-greater-zero
        (s/+ (s/cat :arg-one (s/or :list ::requirelist :vector ::requirevector :symbol symbol? :class class? :key keyword?)
                    :arg-two (s/* (s/or :key keyword? :collection (s/nilable coll?)))))))
#_(stest/instrument `clojure.core/require)

#_(s/fdef clojure.core/use
  :args (s/and ::b-length-greater-zero
               (s/cat :a (s/+ (s/or :list ::requirelist :vector ::requirevector :symbol symbol? :class class?))
                      :keyword (s/* keyword?))))
#_(stest/instrument `clojure.core/use)

#_(s/fdef clojure.core/refer
  :args (s/and ::b-length-greater-zero
               (s/cat :symbol symbol? :b (s/* (s/cat :key keyword? :collection (s/* (s/nilable coll?)))))))
#_(stest/instrument `clojure.core/refer)

(def specced-lookup (clojure.set/map-invert {'map map, 'filter filter, '+ +, 'even? even?, 'odd? odd?}))
