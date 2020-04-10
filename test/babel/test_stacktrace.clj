(ns babel.test-stacktrace
  (:require
    [logs.utils :as log]
    [babel.non-spec-test :refer [to-log?]]
    [babel.utils-for-testing :as t]
    [expectations :refer :all]))

;############################################
;### Tests for functions that have specs  ###
;############################################

;; TO RUN tests, make sure you have repl started in a separate terminal

(expect #(not= % nil)  (log/set-log babel.non-spec-test/to-log?))

(expect nil (log/add-log
              (do
                (def file-name "this file")
                (:file (meta #'file-name)))))

; (expect #"(?s)Expected a number, but a sequence was given instead\.(.*)"
; (log/babel-test-message "(+ (map #(/ 9 %) [9 0]))"))
;
; (expect #"(?s)Expected a number, but a sequence was given instead\.(.*)"
; (log/babel-test-message "(+ 2 (map #(/ 9 %) [9 0]))"))
;
; (expect #"(?s)Expected an integer number, but a sequence was given instead\.(.*)"
; (log/babel-test-message "(even? (map inc [0 9]))"))
;
; (expect #"(?s)Tried to divide by zero(.*)"
; (log/babel-test-message "(even? (map #(/ 9 %) [9 0]))"))
