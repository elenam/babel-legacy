(ns errors.prettify-exception
  (:require [clojure.string :as str]
            [errors.error-dictionary :refer :all])
  (:use [errors.dictionaries]
        [errors.messageobj]))

;; Main error processing file. Standard errors are processed by `standard` function, and
;; modified errors are processed by `prettify-exception` function.


(defn first-match
  [e-class message]
  (first (filter #(and (= (:class %) e-class) (re-matches (:match %) message))
                 error-dictionary)))

(defn get-match
  [e-class message]
  (let [match (first-match e-class message)]
    (if match match (first-match "default" message))))

(defn fn-name
  "Takes a function object and returns a symbol that corresponds to the result of
   the lookup of its name.
   If no name is found, a symbol 'anonymous function' (non-conformant)
   is returned.
   Handles spec-checking functions differently since they are looked up in corefns-map
   by full name.
   Warning: 'anonymous function' symbol is non-conformant"
  [f]
  (let [f-str (str f)]
    (if (re-matches #"clojure\.spec\.test\$spec_checking_fn(.*)" f-str)
      (symbol (get-function-name f-str))
      (symbol (get-function-name (.getName (type f)))))))

(defn is-function?
  "Uses our dictionary to check if a value should be printed as a function"
  [v]
  ;; checking for nil first:
  (and v (= (get-type (.getName (type v))) "a function")))

(defn single-val-str
  "Takes a single (non-collection) value and returns its string represntation.
   Returns a string 'nil' for nil, encloses strings into double quotes,
   performs a lookup for function names, returns 'anonymous function' for
   anonymous functions"
  [v]
  (cond
    (nil? v) "nil"
    (string? v) (str "\"" v "\"")
    (is-function? v) (fn-name v)
    :else (str v)))

(defn lookup-fns
  "Recursively replace internal Clojure function names with user-readable ones
   in the given value"
  [v]
  (cond
    (not (coll? v)) (if (is-function? v) (fn-name v) v)
    (vector? v) (into [] (map lookup-fns v))
    (seq? v) (into '() (reverse (map lookup-fns v)))
    (set? v) (into #{} (map lookup-fns v))
    (map? v) (reduce #(apply assoc %1 %2) {} (map lookup-fns v));; map has key/val pairs
    :else v))

(defn val-str
  "If v is a not a collection, returns the result of apply single-val-str to it.
   Otherwise returns the same collection, but with functions replaced by their
   names, recursively throughout the collection."
  [v]
  (if-not (coll? v) (single-val-str v) (lookup-fns v)))

(defn- message-arity
  "Gives the arity part of the message when there is an arity error detected by spec"
  ;; currently doesn't use the reason, for consistency with the wording of non-spec arity errors
  ;; the reasons may be "Insufficient input" and "Extra input" (as strings)
  [reason args fname]
  (let [arg-args (if (= 1 (count args)) " argument" " arguments")
        arity (lookup-arity fname)]
    (make-msg-info-hashes "You cannot pass " (number-word (str (count args))) arg-args " to a function "
                          fname :arg (if arity (str ", need " arity) ""))))

(defn- function-call-string
  "Gives the function call part of the message for spec error messages"
  [args fname]
  (let [all-args-str (if args (str (val-str args)) "")
        call-str (str "(" fname " " (if args (subs all-args-str 1) ")"))]
    (make-msg-info-hashes ",\n" "in the function call " call-str :call)))

(defn- type-from-failed-pred
  "Returns a type name from the name of a failed predicate"
  [pred-str]
  (cond (= pred-str "seqable?") "a sequence"
        (= pred-str "ifn?") "a function"
        (= pred-str "map?") "a hashmap"
        (= pred-str "integer?") "an integer number"
        :else (str "a " (subs pred-str 0 (dec (count pred-str))))))

(defn- or-str
  "Takes a vector of predicates, returns a string of their names separated by 'or'"
  [pred-strs]
  (apply str (interpose " or " (filter #(not (str/starts-with? % "a length")) ;; to weed out the args-length mismatches
                                       (distinct (map type-from-failed-pred pred-strs))))))

(defn- messages-types
  "Gives the part of the message for spec conditions failure"
  [problems value arg-num n]
  (let [pred-str (if (map? problems) (str (:pred problems)) (map #(str (:pred %)) problems)) ;; it's a map when it's a single predcate
        pred-type (if (map? problems) (type-from-failed-pred pred-str) (or-str pred-str))
        value-str (val-str value)
        value-type (get-type-with-nil value)
        arg-num-str (if arg-num (if (= n 1) "argument" (arg-str (inc (first arg-num)))) "")]
    (if (nil? value)
      (make-msg-info-hashes (str ", the " arg-num-str) " must be " pred-type :type " but is " value-type :arg)
      (make-msg-info-hashes (str ", the " arg-num-str " ") value-str :arg  " must be " pred-type :type " but is " value-type :type))))

(defn- get-predicates
  "If there is only one non-nil predicate in data, the hash map for that predicate
   is returned. If there are several non-nil predicates, a vector of their hash maps is
   returned."
  [data]
  (let [predicates (:clojure.spec/problems data)]
    (if (= 1 (count predicates))
      (first predicates)
      (let [non-nils (filter #(not= "nil?" (str (:pred %))) predicates)]
        (if (= (count non-nils) 1) (first non-nils) non-nils)))))

(defn- arity-error?
  "Returns true if all predicates have arity errors and false otherwise.
   Assumes that spec predicates for non-matching number of arguments start with 'length'"
  [problems]
  (every? #(str/starts-with? (str (:pred %)) "length") problems))

(defn- first-non-length
  "Returns the first non-length predicate"
  [problems]
  (first (filter #(not (str/starts-with? (str (:pred %)) "length")) problems)))

(defn msg-from-matched-entry [entry message]
  "Creates a message info object from an exception and its data, if exists"
  (cond
    ;(and data entry) (msg-info-obj-with-data entry message data)
    entry ((:make-msg-info-obj entry) (re-matches (:match entry) message))
    :else (make-msg-info-hashes message)))

(defn get-sum-text [msg-obj]
  "concatenate all text from a message object into a string"
      ;(println (str "MESSAGE in get-all-text" msg-obj))
  (reduce #(str %1 (:msg %2)) "" msg-obj))

(defn process-spec-errors
  [ex-str]
  (let [chunks (re-matches #"(\S*) (.*)(\n(.*))*(\n)?" ex-str)
        e-class (second chunks)
        message (apply str (drop 2 chunks))
        entry (get-match e-class message)
        msg-info-obj (msg-from-matched-entry entry message)]
        ;[{:msg "Hello"}])]
    {:exception-class e-class
     :msg-info-obj  msg-info-obj}))

;#########################################
;############ Location format  ###########
;#########################################

(defn line-number-format
  "Takes a line number and a character poistion and returns a string
   of how they are reported in an error message"
  [line ch]
  (str " on, or before, line " line))

(println "errors/prettify-exception loaded")
