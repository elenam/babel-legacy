(ns babel.fn-defn-test
  (:require
   [expectations :refer :all]
   [logs.utils :as log]))

;#############################################
;### Tests for fn and defn (specced macros)###
;#############################################

;; TO RUN tests, make sure you have repl started in a separate terminal

;start logging
(log/start-log)

(def to-log? true)

(expect #(not= % nil) (log/set-log to-log?))

(expect nil (log/add-log
              (do
                (def file-name "this file")
                (:file (meta #'file-name)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;; fn: missing a vector  ;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; Note: the name is optional, so the message may be misleading
; (expect "Syntax problems with (fn a b): fn requires a vector of parameters, but is given b instead."
; (log/babel-test-message "(fn a b)"))
;
; (expect "Syntax problems with (fn a): fn is missing a vector of parameters."
; (log/babel-test-message "(fn a)"))
;
; (expect "Syntax problems with (fn): fn is missing a vector of parameters."
; (log/babel-test-message "(fn)"))
;
; (expect "Syntax problems with (fn 5 6): fn requires a vector of parameters, but is given 5 instead."
; (log/babel-test-message "(fn 5 6)"))
;
; (expect "Syntax problems with (fn {x y}): fn requires a vector of parameters, but is given {x y} instead."
; (log/babel-test-message "(fn {x y})"))
;
; (expect "Syntax problems with (fn '(x y)): fn requires a vector of parameters, but is given '(x y) instead."
; (log/babel-test-message "(fn '(x y))"))
