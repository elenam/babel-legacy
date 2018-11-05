(ns babel.processor
 (:require [errors.messageobj :as m-obj]
           [errors.prettify-exception :as p-exc]))



(defn get-error
  [session-number]
  ((comp #(% #'clojure.core/*e)
         deref
         #(get % session-number)
         deref
         deref
        #(get % 'sessions))
   (ns-interns `clojure.tools.nrepl.middleware.session)))

(defn modify-errors [inp-message]
  (if (contains? inp-message :err)
      (assoc inp-message :err
             ;;Anything inside this s-expression that can be bencoded will be returned as the new error message
             ;;You need  to call (get-error (:session inp-message)) in order to recieve the error object,
             ;; but after that, the parser can do anything that produces a string to it.
             (if (= "class clojure.lang.ExceptionInfo" (str (class (get-error (:session inp-message)))))
                   (first (:clojure.spec.alpha/problems (.getData (get-error (:session inp-message)))))
                   (str (.getMessage (get-error (:session inp-message))) (apply str (.getStackTrace (get-error (:session inp-message)))))))
      inp-message))

(println "babel.processor loaded")
