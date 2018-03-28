(ns errors.error-dictionary
  (:use [errors.messageobj]
        [errors.dictionaries]))


;; A vector of error dictionaries from the most specific one to the most general one.
;; Order matters because the vector is searched from top to bottom.


(def error-dictionary
  [;########################
   ;##### Spec Error #######
   ;########################

   ;; Wild cards in regular expressions don't match \n, so we need to include multi-line
   ;; messages explicitly

   {:key :exception-info
    :class "clojure.lang.ExceptionInfo"
    ;; Need to extract the function name from "Call to #'spec-ex.spec-inte/+ did not conform to spec"
    ;:match #"(.*)/(.*) did not conform to spec(.*)" ; the data is in the data object, not in the message
    ;:match #"(.*) Call to \#'(.*)/(.*) did not conform to spec:(.*)(\n(.*)(\n)?)*"
    :match #"(.*)(\n(.*))*(\n)?"
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "In function " (nth matches 0) :arg))}
    ;:make-msg-info-obj (fn [matches] (str "In function " (nth matches 0)))}
   ])

  (println "errors/error-dictionary loaded")
