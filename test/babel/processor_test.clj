(ns babel.processor-test
  (:require [expectations :refer :all]
            [babel.processor :refer :all]))

;;functions to wrap error messages to look like nREPL messages
(defn msg-from-err [inp-error] {:err inp-error})
(defn err-from-msg [inp-message] (inp-message :err))

;;attempting to capture error messages from an actual nREPL server


(def divide-by-zero-error
  (msg-from-err "ArithmeticException Divide by zero  clojure.lang.Numbers.divide (Numbers.java:163)"))

(expect #"Tried to divide by zero" (err-from-msg (modify-errors divide-by-zero-error)))

(def unable-to-resolve-symbol-error
  (msg-from-err "CompilerException java.lang.RuntimeException: Unable to resolve symbol: monkey in this context, compiling:(/tmp/form-init8371041852706021326.clj:1:1)"))

(expect #"Name (.*) is undefined" (err-from-msg (modify-errors unable-to-resolve-symbol-error)))
