(ns corefns.corefns
 (:require [clojure.spec.alpha :as s] 
           [clojure.spec.test.alpha :as stest] 
           [clojure.core.specs.alpha] 
           [clojure.set]))

;; ####################################################
;; ########## Function definitions for specs ##########
;; ####################################################

;; Arity functions, credit to Tony Song (frogrammer)
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

;; Number validation functions
(defn b-not-0? [num] (and (number? num) (not= num 0)))
;; ^^^ I added the check to this one to first check that the argument is a number.
;; I don't know if this will break things or not.
;; QUESTION: This was breaking when the (number? num) part was using the babel number spec instead.
;; I tried testing this for a bit in the repl and I have no idea why this happened.
(defn greater-than-zero? [num] (and (number? num) (< 0 num)))

;; Specific collection functions
(defn coll-not-map? [coll] (and (coll? coll) (not (map? coll))))

;; Lazy sequences
(defn lazy? [lazy-sequence] (or (instance? clojure.lang.IChunkedSeq lazy-sequence)
                                (instance? clojure.lang.IPending lazy-sequence)))

;; Regex
;; clojure.spec.alpha/regex checks for spec regex like s/*, s/cat, not proper regular expressions
;; There might be some naming confusion here
(defn regex? [regex] (instance? java.util.regex.Pattern regex))

;; #################################
;; ########## Babel specs ##########
;; #################################

;; Babel specs for arity/length

;; I don't really think any functions have specific restrictions on collection length
;; if they take a collection as an argument. We are mostly using these for function argument count.
(s/def :babel.arity/one b-length1?)
(s/def :babel.arity/two b-length2?)
(s/def :babel.arity/three b-length3?)
(s/def :babel.arity/zero-or-greater b-length-0greater?)
(s/def :babel.arity/greater-than-zero b-length-greater0?)
(s/def :babel.arity/greater-than-one b-length-greater1?)
(s/def :babel.arity/greater-than-two b-length-greater2?)
(s/def :babel.arity/zero-to-one b-length0-to-1?)
(s/def :babel.arity/one-to-two b-length1-to-2?)
(s/def :babel.arity/two-to-three b-length2-to-3?)
(s/def :babel.arity/two-to-four b-length2-to-4?)
(s/def :babel.arity/one-to-three b-length1-to-3?)
(s/def :babel.arity/zero-to-three b-length0-to-3?)

;; Babel specs for basic data types

;; TODO: How many of these are actually used? Do we need to have all these redundant specs
;; for types? They are a part of the lazy specs below... so maybe that's a good reason to keep them.
(s/def :babel.type/number number?)
(s/def :babel.type/seqable seqable?)
(s/def :babel.type/string string?)
(s/def :babel.type/coll coll?)
(s/def :babel.type/coll-not-map coll-not-map?)
(s/def :babel.type/symbol symbol?)
;; Many functions allow lazy (unevaluated) sequences in place of other data types
(s/def :babel.type/lazy lazy?)

;; Babel specs for number validation
(s/def :babel.type/non-zero-number b-not-0?)
(s/def :babel.type/positive-number greater-than-zero?) ; Should this include a lazy case?
(s/def :babel.type/non-zero-number-or-lazy
  (s/alt :num-non-zero (s/and :babel.type/number ; Number spec should take precedence here
                              :babel.type/non-zero-number),
         :lazy :babel.type/lazy))

;; Babel specs for data types that can optionally be lazy sequences
(s/def :babel.type/function-or-lazy 
  (s/alt :function ifn?, 
         :lazy :babel.type/lazy))
(s/def :babel.type/number-or-lazy 
  (s/alt :number :babel.type/number, 
         :lazy :babel.type/lazy))
(s/def :babel.type/string-or-lazy 
  (s/alt :str :babel.type/string, 
         :lazy :babel.type/lazy))
;; We need s/or to combine the predicates; s/alt seem to be using 'apply'
;; and checks the elements of the first arg, not the first arg itself:
(s/def :babel.type/map-vec-or-lazy 
  (s/or :vector vector?,
        :map map?, 
        :lazy :babel.type/lazy))
(s/def :babel.type/any-or-lazy 
  (s/alt :any any?, 
         :lazy :babel.type/lazy))
(s/def :babel.type/num-or-coll
  (s/alt :arg-one :babel.type/number-or-lazy
         :arg-two (s/cat :number :babel.type/number-or-lazy
                         :collection (s/nilable :babel.type/seqable))))

;; Babel specs for regular expressions
(s/def :babel.type/regex-or-lazy 
  (s/alt :regex regex?, 
         :lazy :babel.type/lazy))

;; Higher-level Babel specs for arity that can optionally be lazy, 
;; + specific restrictions on data types
;; These could be considered both arity and type specs.

(s/def :babel.args/one-of-anything 
  (s/and :babel.arity/one, 
         (s/cat :any any?)))
(s/def :babel.args/one-number 
  (s/and :babel.arity/one 
         (s/cat :number :babel.type/number-or-lazy)))
(s/def :babel.args/some-numbers
  (s/and :babel.arity/greater-than-zero 
         (s/cat :number (s/+ :babel.type/number))))

;; These were (probably) used for macros, but are seemingly unused as of now 
;; (s/def ::bindings-seq2 (s/and vector? ::binding-seq))
;; (s/def ::binding-seq 
;;   (s/cat :a :clojure.core.specs.alpha/binding-form, 
;;          :b (s/or :a (s/nilable :babel.type/coll) 
;;                   :b :babel.type/symbol)))

;; #################################################
;; ########## clojure.core function specs ##########
;; #################################################

(s/fdef clojure.core/+ ;inline issue
  :args (s/cat :babel.type/number-or-lazy (s/* :babel.type/number-or-lazy)))
#_(stest/instrument `clojure.core/+)

(s/fdef clojure.core/- ;inline issue
  :args (s/and :babel.arity/greater-than-zero
               (s/cat :number (s/+ :babel.type/number))))
(stest/instrument `clojure.core/-)

(s/fdef clojure.core/* ;inline issue
  :args (s/cat :number (s/* :babel.type/number)))
(stest/instrument `clojure.core/*)

#_(s/fdef inc ;need to figure out how to deal with the inline, normal fix does not work here
  :args :babel.args/one-number)
#_(stest/instrument `inc)

(s/fdef clojure.core/max ;inline issue
  :args :babel.args/some-numbers)
(stest/instrument `clojure.core/max)

(s/fdef clojure.core/min ;inline issue
  :args :babel.args/some-numbers)
(stest/instrument `clojure.core/min)

(s/fdef clojure.core/rand
  :args (s/and :babel.arity/zero-to-one (s/cat :number (s/? :babel.type/number-or-lazy))))
(stest/instrument `clojure.core/rand)

(s/fdef clojure.core/rand-int
  :args (s/and :babel.arity/one (s/cat :number :babel.type/number-or-lazy)))
(stest/instrument `clojure.core/rand-int)

(s/fdef clojure.core// ;check inline
  :args (s/and :babel.arity/greater-than-zero
               (s/or :a (s/cat :checkfirst (s/and int? ::b-not-zero))
                     :b (s/cat :checkfirst int? :checkafter (s/+ (s/and int? #(not= % 0))))))) ;this part does not work for zero
(stest/instrument `clojure.core//)

(s/fdef clojure.core/string? ;use this as a base for functions like this
  :args :babel.args/one-of-anything)
(stest/instrument `clojure.core/string?)

(s/fdef clojure.core/even?
  :args :babel.args/one-number)
(stest/instrument `clojure.core/even?)

(s/fdef clojure.core/odd?
  :args :babel.args/one-number)
(stest/instrument `clojure.core/odd?)

(s/fdef clojure.core/conj
  :args (s/and :babel.arity/zero-or-greater
               (s/or :any (s/cat :any (s/? (s/nilable any?))) ;conj with a single arg acts like identity
                     :map-arg (s/cat :collection-map map? :sequence (s/alt :map map? :vec (s/* (s/coll-of any? :kind vector? :count 2))))
                     :collection (s/cat :collection (s/nilable :babel.type/coll-not-map) :any (s/+ any?))
                    )))
(stest/instrument `clojure.core/conj)

(s/fdef clojure.core/into
  :args (s/and :babel.arity/zero-to-three
               (s/or :arg-one (s/cat :any (s/? any?))
                     :arg-two (s/cat :coll (s/nilable :babel.type/coll) :any any?)
                     :arg-three (s/cat :coll (s/nilable :babel.type/coll) :function :babel.type/function-or-lazy :coll any?)
                     )))
(stest/instrument `clojure.core/into)

(s/fdef clojure.core/map
  :args (s/and :babel.arity/greater-than-zero
               (s/cat :function any? :collection (s/* :babel.type/seqable)))) ;change to a + to block transducers
(stest/instrument `clojure.core/map)

(s/fdef clojure.core/mod
  :args (s/and :babel.arity/two
               (s/cat :number :babel.type/number-or-lazy :num-non-zero :babel.type/non-zero-number-or-lazy)))
(stest/instrument `clojure.core/mod)

(s/fdef clojure.core/numerator
  :args (s/cat :ratio ratio?))
(stest/instrument `clojure.core/numerator)

(s/fdef clojure.core/denominator
  :args (s/cat :ratio ratio?))
(stest/instrument `clojure.core/denominator)

(s/fdef clojure.core/subs ;incomplete
  :args (s/and :babel.arity/two-to-three
               (s/or :arg-one (s/cat :string :babel.type/string-or-lazy :integer int?)
                     :arg-two (s/cat :string :babel.type/string-or-lazy :integer int? :integer int?))))
(stest/instrument `clojure.core/subs)

(s/fdef clojure.core/reduce
  :args (s/and :babel.arity/two-to-three
    (s/or :arg-one (s/cat :function :babel.type/function-or-lazy :collection (s/nilable :babel.type/coll))
          :arg-two (s/cat :function :babel.type/function-or-lazy :value any? :collection (s/nilable :babel.type/coll)))))
(stest/instrument `clojure.core/reduce)

(s/fdef clojure.core/get-in
  :args (s/and :babel.arity/two-to-three
    (s/or :arg-one (s/cat :collection (s/nilable :babel.type/coll) :collection (s/nilable :babel.type/coll))
          :arg-two (s/cat :collection (s/nilable :babel.type/coll) :collection (s/nilable :babel.type/coll) :value any?))))
(stest/instrument `clojure.core/get-in)

#_(s/fdef clojure.core/var-get
  :args (s/and :babel.arity/one
    (s/or :arg-one (s/cat :value var?))))
#_(stest/instrument `clojure.core/var-get)

#_(s/fdef clojure.core/future-cancel
  :args (s/and :babel.arity/one
    (s/or :arg-one (s/cat :future future?))))
#_(stest/instrument `clojure.core/future-cancel)

; (s/fdef clojure.core/->>
;   :args (s/and :babel.arity/greater-than-zero
;     (s/or :arg-one (s/cat :value (s/+ any?)))))
; (stest/instrument `clojure.core/->>)

; (s/fdef clojure.core/if-some
;   :args (s/and :babel.arity/two-to-three
;                (s/or :arg-one (s/cat :bindings :clojure.core.specs.alpha/bindings :value any?)
;                      :arg-two (s/cat :bindings :clojure.core.specs.alpha/bindings :value any? :value any?))))
; (stest/instrument `clojure.core/if-some)

(s/fdef clojure.core/gen-class
  :args (s/and :babel.arity/zero-or-greater
    (s/cat :arg-one (s/* any?))))
(stest/instrument `clojure.core/gen-class)

(s/fdef clojure.core/while
  :args (s/and :babel.arity/greater-than-zero
    (s/or :arg-one (s/cat :value (s/* any?)))))
(stest/instrument `clojure.core/while)

(s/fdef clojure.core/pvalues
  :args (s/and :babel.arity/zero-or-greater
    (s/cat :value (s/* any?))))
(stest/instrument `clojure.core/pvalues)

(s/fdef clojure.core/identical?
  :args (s/and :babel.arity/two
    (s/cat :value (s/nilable any?) :value (s/nilable any?))))
(stest/instrument `clojure.core/identical?)

(s/fdef clojure.core/contains?
  :args (s/and :babel.arity/two
    (s/alt :arg-one (s/cat :only-collection (s/alt :map (s/nilable map?) :set (s/nilable set?) :vector (s/nilable vector?) :lazy :babel.type/lazy) :any (s/nilable any?))
          :arg-two (s/cat :string (s/nilable :babel.type/string) :number (s/alt :number (s/nilable :babel.type/number) :lazy :babel.type/lazy)))))
(stest/instrument `clojure.core/contains?)

(s/fdef clojure.core/filter
  :args (s/and :babel.arity/one-to-two
    ;; TODO: figure out if there is any reason for any-or-lazy
    (s/or :arg-one (s/cat :function :babel.type/any-or-lazy :collection (s/nilable :babel.type/seqable))
          :arg-two (s/cat :function :babel.type/function-or-lazy))))
(stest/instrument `clojure.core/filter)

(s/fdef clojure.core/take
  :args (s/and :babel.arity/one-to-two
          (s/or :arg-one (s/cat :number :babel.type/number-or-lazy)
                :arg-two (s/cat :number :babel.type/number-or-lazy :collection (s/nilable :babel.type/seqable)))))
(stest/instrument `clojure.core/take)

(s/fdef clojure.core/take-nth
  :args (s/and :babel.arity/one-to-two
    (s/or :arg-one (s/cat :number :babel.type/number-or-lazy)
          :arg-two (s/cat :number-greater-than-zero :babel.type/positive-number :collection (s/nilable :babel.type/seqable)))))
(stest/instrument `clojure.core/take-nth)

(s/fdef clojure.core/take-last
  :args (s/and :babel.arity/two ; unlike other take/drop variants, doesn't have a 1-arity version
    (s/cat :number :babel.type/number-or-lazy :collection (s/nilable :babel.type/seqable))))
(stest/instrument `clojure.core/take-last)

(s/fdef clojure.core/take-while
  :args (s/and :babel.arity/one-to-two
        (s/or :arg-one (s/cat :function :babel.type/function-or-lazy)
              :arg-two (s/cat :function :babel.type/function-or-lazy :collection (s/nilable :babel.type/seqable)))))
(stest/instrument `clojure.core/take-while)

(s/fdef clojure.core/drop
  :args (s/and :babel.arity/one-to-two
        (s/or :arg-one (s/cat :number :babel.type/number-or-lazy)
              :arg-two (s/cat :number :babel.type/number-or-lazy :collection (s/nilable :babel.type/seqable)))))
(stest/instrument `clojure.core/drop)

(s/fdef clojure.core/drop-last
  :args (s/and :babel.arity/one-to-two
               (s/alt :arg-one (s/cat :collection (s/nilable :babel.type/seqable))
                      :arg-two (s/cat :number :babel.type/number-or-lazy :collection (s/nilable :babel.type/seqable)))))
(stest/instrument `clojure.core/drop-last)

(s/fdef clojure.core/drop-while
  :args (s/and :babel.arity/one-to-two
    (s/or :arg-one (s/cat :function :babel.type/function-or-lazy)
          :arg-two (s/cat :function :babel.type/function-or-lazy :collection (s/nilable :babel.type/seqable)))))
(stest/instrument `clojure.core/drop-while)

(s/fdef clojure.core/remove
  :args (s/and :babel.arity/one-to-two
    (s/or :arg-one (s/cat :function :babel.type/function-or-lazy)
          :arg-two (s/cat :function :babel.type/function-or-lazy :collection (s/nilable :babel.type/seqable)))))
(stest/instrument `clojure.core/remove)

(s/fdef clojure.core/group-by
  :args (s/and :babel.arity/two
    (s/cat :function :babel.type/function-or-lazy :collection (s/nilable :babel.type/seqable))))
(stest/instrument `clojure.core/group-by)

(s/fdef clojure.core/replace
  :args (s/and :babel.arity/one-to-two
    (s/or :arg-one (s/cat :map-or-vector (s/nilable :babel.type/map-vec-or-lazy))
          :arg-two (s/cat :map-or-vector (s/nilable :babel.type/map-vec-or-lazy) :collection (s/nilable :babel.type/seqable)))))
(stest/instrument `clojure.core/replace)

(s/fdef clojure.core/keep
  :args (s/and :babel.arity/one-to-two
    (s/or :arg-one (s/cat :function :babel.type/function-or-lazy)
          :arg-two (s/cat :function :babel.type/function-or-lazy :collection (s/nilable :babel.type/seqable)))))
(stest/instrument `clojure.core/keep)

(s/fdef clojure.core/partition
  :args (s/and :babel.arity/two-to-four
    (s/or
      :arg-one (s/cat :number :babel.type/number-or-lazy :collection (s/nilable :babel.type/seqable))
      :arg-two (s/cat :number :babel.type/number-or-lazy :number :babel.type/number-or-lazy :collection (s/nilable :babel.type/seqable))
      :arg-three (s/cat :number :babel.type/number-or-lazy :number :babel.type/number-or-lazy :value any? :collection (s/nilable :babel.type/seqable)))))
(stest/instrument `clojure.core/partition)

(s/fdef clojure.core/partition-by
  :args (s/and :babel.arity/one-to-two
          (s/or :arg-one (s/cat :function :babel.type/function-or-lazy)
                :arg-two (s/cat :function :babel.type/function-or-lazy :collection (s/nilable :babel.type/seqable)))))
(stest/instrument `clojure.core/partition-by)

(s/fdef clojure.core/partition-all :args
  (s/and :babel.arity/one-to-three
    (s/or :arg-one (s/cat :number :babel.type/number-or-lazy)
          :arg-two (s/cat :number :babel.type/number-or-lazy :collection (s/nilable :babel.type/seqable))
          :arg-three  (s/cat :number :babel.type/number-or-lazy :number :babel.type/number-or-lazy :collection (s/nilable :babel.type/seqable)))))
;(stest/instrument `clojure.core/partition-all)

(s/fdef clojure.string/split
  :args (s/and :babel.arity/two-to-three
    (s/or :arg-one (s/cat :string :babel.type/string-or-lazy :regex :babel.type/regex-or-lazy)
          :arg-two (s/cat :string :babel.type/string-or-lazy :regex :babel.type/regex-or-lazy :number :babel.type/number-or-lazy))))
(stest/instrument `clojure.string/split)

(s/fdef clojure.core/comp
  :args (s/and :babel.arity/greater-than-zero
               (s/cat :function (s/* any?))))
(stest/instrument `clojure.core/comp)

#_(s/fdef clojure.core/int
  :args (s/and :babel.arity/one
               (s/or :number :babel.type/number :char char?)))
#_(stest/instrument `clojure.core/int)

;;;;;;;;;;;;;;;;; Our spec for macros ;;;;;;;;;;;;;;;;;;;;;;;

;;; TODO: need to revisit

(s/def ::innervector (s/cat :symbol :babel.type/symbol :b (s/* (s/cat :key keyword :symbol-or-collection (s/or :symbol :babel.type/symbol
                                                                          :collection (s/nilable :babel.type/coll))))))
(s/def ::requiredlist (s/or :arg-one (s/cat :symbol (s/* :babel.type/symbol))
                            :arg-two (s/cat :symbol :babel.type/symbol :vector (s/and vector? ::innervector))))
(s/def ::requirelist (s/and list? ::requiredlist))
(s/def ::requiredvector (s/or :arg-one (s/cat :symbol :babel.type/symbol)
                              :arg-two (s/cat :symbol :babel.type/symbol :b (s/* (s/cat :key keyword? :b (s/or :symbol :babel.type/symbol
                                                                                                    :collection (s/nilable :babel.type/coll)
                                                                                                    :key keyword?))))
                              :arg-three (s/cat :symbol :babel.type/symbol :vector (s/and vector? ::innervector))))
(s/def ::requirevector (s/and vector? ::requiredvector))
(s/def ::importlists (s/and (s/nilable :babel.type/coll) ::importlist))
(s/def ::importlist (s/cat :a (s/+ (s/or :string :babel.type/string
                                         :symbol :babel.type/symbol
                                         :class class?
                                         :quotelist ::quotelist))))
(s/def ::quotelist (s/cat :a (s/+ (s/or :string :babel.type/string
                                        :symbol :babel.type/symbol
                                        :class class?))))

#_(s/fdef clojure.core/require
  :args (s/and :babel.arity/greater-than-zero
        (s/+ (s/cat :arg-one (s/or :list ::requirelist :vector ::requirevector :symbol :babel.type/symbol :class class? :key keyword?)
                    :arg-two (s/* (s/or :key keyword? :collection (s/nilable :babel.type/coll)))))))
#_(stest/instrument `clojure.core/require)

#_(s/fdef clojure.core/use
  :args (s/and :babel.arity/greater-than-zero
               (s/cat :a (s/+ (s/or :list ::requirelist :vector ::requirevector :symbol :babel.type/symbol :class class?))
                      :keyword (s/* keyword?))))
#_(stest/instrument `clojure.core/use)

#_(s/fdef clojure.core/refer
  :args (s/and :babel.arity/greater-than-zero
               (s/cat :symbol :babel.type/symbol :b (s/* (s/cat :key keyword? :collection (s/* (s/nilable :babel.type/coll)))))))
#_(stest/instrument `clojure.core/refer)

(def specced-lookup (clojure.set/map-invert {'map map, 'filter filter, '+ +, 'even? even?, 'odd? odd?}))
