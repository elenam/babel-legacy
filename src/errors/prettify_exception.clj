(ns errors.prettify-exception
  (:require [clojure.string :as s]
            [errors.error-dictionary :refer :all])
  (:use [errors.dictionaries]))

;; Main error processing file. Standard errors are processed by `standard` function, and
;; modified errors are processed by `prettify-exception` function.

(defn first-match
  [e-class message]
  (first (filter #(and (= (:class %) e-class) (re-matches (:match %) message))
                 error-dictionary)))

(defn get-match
  [e-class message]
  (let [match (first-match e-class message)]
    (or match (first-match "default" message))))

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

(defn msg-from-matched-entry
  "Returns the modified error message by applying a function in the error
  dictionary to the message. If no match found, returns the message as is"
  [entry message]
  (cond
    ;(and data entry) (msg-info-obj-with-data entry message data)
    entry ((:fn entry) (re-matches (:match entry) message))
    :else message))

(defn process-errors
  "Takes a message from an exception as a string and returns a message object,
  to be displayed by the repl or IDE"
  [t m]
  (let [e-class (nth (re-matches #"(\w+)\.(\w+)\.(.*)" (str t)) 3)
        message (or m "") ; m can be nil
        entry (get-match e-class message)
        modified (msg-from-matched-entry entry message)]
       modified))

;#########################################
;############ Location format  ###########
;#########################################

(defn line-number-format
  "Takes a line number and a character position and returns a string
   of how they are reported in an error message"
  [line ch]
  (str " on, or before, line " line))
