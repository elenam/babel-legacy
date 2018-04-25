(ns babel.processor
  (:require  [clojure.tools.nrepl :as repl]
             [clojure.spec.test.alpha :as stest]
             [errors.messageobj :as m-obj]
             [errors.prettify-exception :as p-exc]))

(defn modify-errors "takes a nREPL response, and returns a message with the errors fixed"
  [inp-message]
  (if (contains? inp-message :err)
    (assoc inp-message :err (m-obj/get-all-text (:msg-info-obj (p-exc/process-spec-errors (inp-message :err)))))
    inp-message))

(println "babel.processor loaded")
