(ns babel.processor
  (:require  [clojure.tools.nrepl :as repl]
             [clojure.spec.test.alpha :as stest]
             [errors.messageobj :as m-obj]
             [errors.prettify-exception :as p-exc]
             ))



#_(defn process-spec-errors
 [ex-str]
 (let [e-class "smoked kipper"
       message ex-str
       ;entry (first-match e-class message)
       ;msg-info-obj (if entry (msg-from-matched-entry entry message) message)]
       ]
       {:exception-class e-class
        :msg-info-obj ex-str}))

(defn modify-errors "takes a nREPL response, and returns a message with the errors fixed"
  [inp-message]
    (if (contains? inp-message :err)
          ;;replace the assoced value with a function call as needed.
          (assoc inp-message :err (str  (m-obj/get-all-text (p-exc/process-spec-errors (inp-message :err)))  " -sorry\n"))
          ;(assoc inp-message :err (str (inp-message :err) " -sorry\n")))
        inp-message))

(println "babel.processor loaded")
