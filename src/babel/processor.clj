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

             (let [err (get-error (:session inp-message))]
                (if (= "class clojure.lang.ExceptionInfo" (str (class err)))
                   (first (:clojure.spec.alpha/problems (.getData err)))
                   (m-obj/get-all-text (:msg-info-obj (p-exc/process-spec-errors (str (clojure.string/replace (str (class err)) #"class " "") " " (.getMessage err))))))))
      inp-message))

(println "babel.processor loaded")
