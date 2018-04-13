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
    :class "ExceptionInfo"
    ;; Need to extract the function name from "Call to #'spec-ex.spec-inte/+ did not conform to spec"
    ;:match #"(.*)/(.*) did not conform to spec(.*)" ; the data is in the data object, not in the message
    :match #"Call to \#'(.*)/(.*) did not conform to spec:\nIn: \[(\d*)\] val: (.*) fails at: \[:args :(\S*)\](.*)(\n(.*)(\n)?)*"
    ;:match #"(.*)(\n(.*))*(\n)?"
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "In function " (nth matches 2) :arg
                                                            " at position " (nth matches 3) :arg
                                                            " is expected to be a "  (nth matches 5) :type
                                                            " , but is " (nth matches 4) :type
                                                            "instead."))}
    ;:make-msg-info-obj (fn [matches] (str "In function " (nth matches 0)))}


   ;#############################
   ;### Class Cast Exceptions ###
   ;#############################

   #_{:key :class-cast-exception-cannot-cast-to-map-entry
      :class "ClassCastException"
      :match #"(.*) cannot be cast to java\.util\.Map\$Entry(.*)"
      :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Attempted to create a map using "
                                                             (get-type (nth matches 1)) :type
                                                             ", but a sequence of vectors of length 2 or a sequence of maps is needed."))}
   {:key :class-cast-exception
    :class "ClassCastException"
    :match #"Cannot cast (\S*) to (\S*) (.*)(\n(.*)(\n)?)*"
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Attempted to use "
                                                           (get-type (nth matches 1)) :type ", but "
                                                           (get-type (nth matches 2)) :type " was expected.\n"))}
    ;######################
    ;### Syntax Errors ###
    ;#####################

   {:key :compiler-exception-cannot-resolve-symbol
    :class "java.lang.RuntimeException"
    :match #"Unable to resolve symbol: (.+) in this context(.*)(\n(.*)(\n)?)*"
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Name "
                                                           (nth matches 1) :arg " is undefined.\n"))}

   ;#############################
   ;### Arithmetic Exception ###
   ;############################

   {:key :arithmetic-exception-divide-by-zero
    :class "ArithmeticException"
    :match #"Divide by zero(.*)(\n(.*)(\n)?)*"
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Tried to divide by zero\n"))};######################
   ;### Default Error ###
   ;#####################

   {:key :other
    :class "default"
    :match #"(.*)(\n(.*))*(\n)?"
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Default Error: " (nth matches 0) :arg "\n"))}])

(println "errors/error-dictionary loaded")
