(ns babel.processor
  (:require  [clojure.tools.nrepl :as repl]
             [clojure.spec.test.alpha :as stest]
             [errors.messageobj :as m-obj]
             [errors.prettify-exception :as p-exc]))

;;an atom that record original error response
(def recorder (atom {:msg "" :detail ""}))

;;reset the recorder
(defn reset-recorder
  []
  (def recorder (atom {:msg "" :detail ""})))

;;update recorded message
(defn update-recorder-msg
  [inp-message]
  (swap! recorder assoc :msg inp-message))

;;update recorded detail
(defn update-recorder-detail
  [inp-message]
  (swap! recorder assoc :detail inp-message))

(defn modify-errors "takes a nREPL response, and returns a message with the errors fixed"
  [inp-message]
  (if (contains? inp-message :err)
    ;;replace the assoced value with a function call as needed.
    (assoc inp-message :err (m-obj/get-all-text (:msg-info-obj (do
                                                                 (update-recorder-detail (p-exc/process-spec-errors (inp-message :err)))
                                                                 (p-exc/process-spec-errors (do
                                                                                              (update-recorder-msg (inp-message :err))
                                                                                              (inp-message :err)))))))
    ;(assoc inp-message :err (str (inp-message :err))) ;; Debugging
    inp-message))

(println "babel.processor loaded")
