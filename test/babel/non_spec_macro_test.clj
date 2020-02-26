(ns babel.non-spec-macro-test
  (:require
   [expectations :refer :all]
   [logs.utils :as log]))

;#########################################
;### Tests for errors that aren't      ###
;### spec failures                     ###
;#########################################

;; TO RUN tests, make sure you have repl started in a separate terminal

;start logging
(log/start-log)

(def to-log? true)

(expect #(not= % nil) (log/set-log to-log?))

(expect nil (log/add-log
              (do
                (def file-name "this file")
                (:file (meta #'file-name)))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;IllegalArgumentException;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect "Loop requires a vector for its binding."
(log/babel-test-message "(loop x 5 (+ x 5))"))

(expect "Parameters for cond must come in pairs, but one of them does not have a match."
(log/babel-test-message "(cond 4 5 6)"))

(expect "Parameters for cond must come in pairs, but one of them does not have a match."
(log/babel-test-message "(cond 4)"))

(expect "when-some requires a vector for its binding."
(log/babel-test-message "(when-some 8)"))

(expect #"when-some requires exactly two elements in its vector, but a different number was given\.(.*)"
(log/babel-test-message "(when-some [6] 8)"))

(expect #"if-some requires exactly two elements in its vector, but a different number was given\.(.*)"
(log/babel-test-message "(if-some [6] 8)"))

(expect #"if-some requires exactly two elements in its vector, but a different number was given\.(.*)"
(log/babel-test-message "(if-some [4 7 8] 9)"))

(expect "when-first requires a vector for its binding."
(log/babel-test-message "(when-first 1 1 1)"))

(expect "Recur expected two arguments but was given one argument."
(log/babel-test-message "(fn [x y] (recur 5))"))

(expect "Recur expected no arguments but was given one argument."
(log/babel-test-message "(fn [] (recur 5))"))
