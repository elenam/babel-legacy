(ns babel.non-spec-macro-test
  (:require
   [expectations :refer [expect]]
   [logs.utils :as log]
   [babel.utils-for-testing :as t]))

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

(expect (t/make-pattern "Loop requires a vector for its binding.")
(log/babel-test-message "(loop x 5 (+ x 5))"))

(expect (t/make-pattern "Parameters for cond must come in pairs, but one of them does not have a match.")
(log/babel-test-message "(cond 4 5 6)"))

(expect (t/make-pattern "Parameters for cond must come in pairs, but one of them does not have a match.")
(log/babel-test-message "(cond 4)"))

(expect (t/make-pattern "when-some requires a vector for its binding.")
(log/babel-test-message "(when-some 8)"))

(expect #"when-some requires exactly two elements in its vector, but a different number was given\.(.*)"
(log/babel-test-message "(when-some [6] 8)"))

(expect #"if-some requires exactly two elements in its vector, but a different number was given\.(.*)"
(log/babel-test-message "(if-some [6] 8)"))

(expect #"if-some requires exactly two elements in its vector, but a different number was given\.(.*)"
(log/babel-test-message "(if-some [4 7 8] 9)"))

(expect (t/make-pattern "when-first requires a vector for its binding.")
(log/babel-test-message "(when-first 1 1 1)"))

(expect (t/make-pattern "Recur expected two arguments but was given one argument.")
(log/babel-test-message "(fn [x y] (recur 5))"))

(expect (t/make-pattern "Recur expected no arguments but was given one argument.")
(log/babel-test-message "(fn [] (recur 5))"))

(expect (t/make-pattern "Recur can only occur as a tail call: no operations can be done on its result."
                        #"(.*)"
                        "In Clojure interactive session on line 1 at position 46.")
(log/babel-test-message "(loop [x 1] (+ 1 (recur (#(+ %1 %2) 5))))"))
