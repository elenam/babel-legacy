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
;;;;;;;;;;;;;;;;;ArithmeticExceptions;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect "Breaks, needs fixing!!!!" (log/babel-test-message "(loop x 5 (+ x 5))"))
