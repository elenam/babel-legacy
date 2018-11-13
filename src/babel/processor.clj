(ns babel.processor
 (:require [errors.messageobj :as m-obj]
           [errors.prettify-exception :as p-exc]))

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


(defn get-error
  [session-number]
  ((comp #(% #'clojure.core/*e)
         deref
         #(get % session-number)
         deref
         deref
        #(get % 'sessions))
   (ns-interns `clojure.tools.nrepl.middleware.session)))

(defn proccess-message
  "takes a session number, and returns the adjusted message as astring."
  [err]
  (if (= "class clojure.lang.ExceptionInfo" (str (class err)))
      (p-exc/process-spec-errors (str (.getMessage err)) (first (:clojure.spec.alpha/problems (.getData err))))
      (m-obj/get-all-text (:msg-info-obj (p-exc/process-errors (str (clojure.string/replace (str (class err)) #"class " "") " " (.getMessage err)))))))

(defn modify-errors [inp-message]
  (if (contains? inp-message :err)
      (assoc inp-message :err
             ;;Anything inside this s-expression that can be bencoded will be returned as the new error message
             ;;You need  to call (get-error (:session inp-message)) in order to recieve the error object,
             ;; but after that, the parser can do anything that produces a string to it.
             (let [err (get-error (:session inp-message))
                   proccessed-error (proccess-message err)]
                  (do
                    (update-recorder-detail proccessed-error)
                    (update-recorder-msg (str err))
                    proccessed-error)))

      inp-message))

(println "babel.processor loaded")
