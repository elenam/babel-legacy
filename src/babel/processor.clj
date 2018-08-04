(ns babel.processor
  (:require  [clojure.tools.nrepl :as repl]
             [clojure.spec.test.alpha :as stest]
             [errors.messageobj :as m-obj]
             [errors.prettify-exception :as p-exc]
             [corefns.corefns :as cf]))

;;an atom that record original error response
(def recorder (atom {:msg [] :detail []}))

(defn reset-recorder
  "This function reset the recorder atom"
  []
  (reset! recorder {:msg [] :detail []}))

(defn update-recorder-msg
  "takes an unfixed error message, and put it into the recorder"
  [inp-message]
  (swap! recorder update-in [:msg] conj inp-message))
  ;(swap! recorder assoc :msg inp-message))

(defn update-recorder-detail
  "takes error message details, and put them into the recorder"
  [inp-message]
  (swap! recorder update-in [:detail] conj inp-message))

(defn modify-errors
  "takes a nREPL response, and returns a message with the errors fixed"
  [inp-message]
  (if (contains? inp-message :err)
    ;;replace the assoced value with a function call as needed.
    (assoc inp-message :err
      (let [err (inp-message :err)
            processed (p-exc/process-spec-errors err)]
            (m-obj/get-all-text (:msg-info-obj (do (update-recorder-detail processed)
                                                   (update-recorder-msg err)
                                                   processed)))))
    ;(assoc inp-message :err (str (inp-message :err))) ;; Debugging
    ;(assoc inp-message :err (str "\n" inp-message "\n" (p-exc/process-spec-errors (inp-message :err)))) ;; Debugging
    inp-message))

(println "babel.processor loaded")
