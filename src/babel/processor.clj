(ns babel.processor
 (:require [errors.messageobj :as m-obj]
           [errors.prettify-exception :as p-exc]))



(defn get-error
  []
  ((comp #(% #'clojure.core/*e)
         deref
         second
         first
         deref
         deref
        #(get % 'sessions))
   (ns-interns `clojure.tools.nrepl.middleware.session)))

(defn modify-errors [inp-message]
  (if (contains? inp-message :err)
      (assoc inp-message :err
             (let [err (str (get-error) "\n")
                   processed (p-exc/process-spec-errors err)]
                  (m-obj/get-all-text (:msg-info-obj processed))))      
      inp-message))

(println "babel.processor loaded")
