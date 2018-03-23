(ns babel.processor
  (:require  [clojure.tools.nrepl :as repl]
             [clojure.spec.test.alpha :as stest]))

(defn modify-errors "takes a nREPL response, and returns a message with the errors fixed"
  [inp-message]
    (if (contains? inp-message :err)
        (do
          (stest/instrument)
          ;;replace the assoced value with a function call as needed.
          (assoc inp-message :err (str (inp-message :err) " -sorry!\n")))
        inp-message))
