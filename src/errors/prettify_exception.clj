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

(defn msg-from-matched-entry [entry message]
  "Creates a message info object from an exception and its data, if exists"
  (cond
    ;(and data entry) (msg-info-obj-with-data entry message data)
    entry ((:make-msg-info-obj entry) (re-matches (:match entry) message))
    :else (make-msg-info-hashes message)))

; This was added from another file and isn't needed:
(defn get-sum-text [msg-obj]
  "concatenate all text from a message object into a string"
      ;(println (str "MESSAGE in get-all-text" msg-obj))
  (reduce #(str %1 (:msg %2)) "" msg-obj))


;; TO-DO: simplify handling the rest: now there is only one match -EM 5/20
(defn get-exception-class-and-rest
  "returns a vector containing the class and then the message without the class marking"
  [ex-str]
  (let [compiler-exc (re-matches #"(?s)CompilerException (\S*): (.*)" ex-str) ; first we check if it is a compiler exception
        matches (or compiler-exc (re-matches #"(?s)(\S*) (.*)" ex-str))
        e-class (second matches)
        qual-name (nth (re-matches #"(\w+)\.(\w+)\.(.*)" e-class) 3)
        e-class1 (or qual-name e-class)
        rest (apply str (drop 2 matches))]
    [e-class1 rest]))

(defn process-spec-errors
  "Takes a message from an exception as a string and returns a message object,
  to be displayed by the repl or IDE"
  [ex-str]
  (let [parsing (get-exception-class-and-rest ex-str)
        e-class (first parsing)
        message (second parsing)
        entry (get-match e-class message)
        ;msg-info-obj (make-msg-info-hashes ex-str "\n" e-class " & " message "\n")]
        msg-info-obj (msg-from-matched-entry entry message)]
    {:exception-class e-class
     :msg-info-obj  msg-info-obj}))

;#########################################
;############ Location format  ###########
;#########################################

(defn line-number-format
  "Takes a line number and a character position and returns a string
   of how they are reported in an error message"
  [line ch]
  (str " on, or before, line " line))
