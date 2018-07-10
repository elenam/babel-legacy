(ns babel.processor
  (:require  [clojure.tools.nrepl :as repl]
             [clojure.spec.test.alpha :as stest]
             [errors.messageobj :as m-obj]
             [errors.prettify-exception :as p-exc]))

;;an atom that record original error response
(def recorder (atom {:msg ""}))

;;reset the recorder
(defn reset-recorder
  []
  (def recorder (atom {:msg ""})))

;;update recorded message
(defn update-recorder
  [inp-message]
  (swap! recorder assoc :msg inp-message))

(defn modify-errors "takes a nREPL response, and returns a message with the errors fixed"
  [inp-message]
  (if (contains? inp-message :err)
    ;;replace the assoced value with a function call as needed.
    (assoc inp-message :err (m-obj/get-all-text (:msg-info-obj (p-exc/process-spec-errors (do (update-recorder (:err inp-message)) (inp-message :err))))))
    ;(assoc inp-message :err (str (inp-message :err))) ;; Debugging
    inp-message))

(println "babel.processor loaded")
