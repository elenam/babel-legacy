(ns errors.error-dictionary
  (:use [errors.messageobj]
        [errors.dictionaries]))

;; A vector of error dictionaries from the most specific one to the most general one.
;; Order matters because the vector is searched from top to bottom.

(defn beginandend [x]
  (re-pattern (str "(?s)" x "(.*)")))

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
      :match #"(?s)(.*) cannot be cast to java\.util\.Map\$Entry(.*)"
      :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Attempted to create a map using "
                                                             (get-type (nth matches 1)) :type
                                                             ", but a sequence of vectors of length 2 or a sequence of maps is needed."))}
   {:key :class-cast-exception
    :class "ClassCastException"
    :match (beginandend "Cannot cast (\\S*) to (\\S*)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Attempted to use "
                                                           (get-type (nth matches 1)) :type ", but "
                                                           (get-type (nth matches 2)) :type " was expected.\n"))}

    {:key :class-cast-exception-lower-case
     :class "ClassCastException"
     :match (beginandend "(\\S*) cannot be cast to (\\S*)")
     :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Attempted to use "
                                                            (get-type (nth matches 1)) :type ", but "
                                                            (get-type (nth matches 2)) :type " was expected.\n"))}

    ;###################################
    ;### Illegal Argument Exceptions ###
    ;###################################

    {:key :assoc-parity-error
    :class "IllegalArgumentException"
    :match (beginandend "assoc expects even number of arguments after map/vector, found odd number")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "The arguments following the map or vector in assoc must come in pairs, but one of them does not have a match.\n"))}

    {:key :wrong-number-of-args-passed-to-a-keyword
    :class "IllegalArgumentException"
    :match (beginandend "Wrong number of args passed to keyword: (\\S*)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "A keyword: " (nth matches 1) :arg " can only take one or two arguments.\n"))}

    {:key :illegal-argument-no-val-supplied-for-key
    :class "IllegalArgumentException"
    :match (beginandend "No value supplied for key: (\\S*)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "No value found for key "
                                                           ; is this too wordy?
                                                           ;(nth matches 1) :arg ". Every key must be paired with a value; the value should be immediately following the key."))
                                                           (nth matches 1) :arg ". Every key for a hash-map must be followed by a value.\n"))}

    {:key :illegal-argument-vector-arg-to-map-conj
    :class "IllegalArgumentException"
    :match (beginandend "Vector arg to map conj must be a pair")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Vectors added to a map must consist of two elements: a key and a value.\n"))}

    {:key :illegal-argument-cannot-convert-type
    ;spec
    :class "IllegalArgumentException"
    :match #"(?s)Don't know how to create (\S*) from: (\S*)(.*)"
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Don't know how to create " (get-type (nth matches 1)) :type " from "(get-type (nth matches 2)) :type ".\n"))}


    {:key :illegal-argument-even-number-of-forms
    :class "IllegalArgumentException"
    :match #"(?s)(\S*) requires an even number of forms(.*)"
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Parameters for " (nth matches 1) :arg " must come in pairs, but one of them does not have a match.\n"))}

    {:key :cant-call-nil
    :class "IllegalArgumentException"
    :match (beginandend "Can't call nil")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Cannot call nil as a function.\n"))}

    {:key :duplicate-key-hashmap
    :class "IllegalArgumentException"
    :match (beginandend "Duplicate key: (\\S*)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "You cannot use the same key in a hash-map twice, but you have duplicated the key " (nth matches 1) :arg ".\n"))}

    ;########################
    ;### Arity Exceptions ###
    ;########################

    {:key :wrong-number-of-args-passed-to-a-keyword
    :class "ArityException"
    :match (beginandend "Wrong number of args (\\S*) passed to: core/keyword")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "A keyword can only take one or two arguments.\n"))}

    {:key :wrong-number-of-args-passed-to-core
    ;we may want to find a way to make this less general
    :class "ArityException"
    :match (beginandend "Wrong number of args (\\S*) passed to: core/(\\S*)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "A " (nth matches 2) :arg " cannot take " (nth matches 1) :arg " arguments.\n"))}

    ;#####################
    ;### Syntax Errors ###
    ;#####################

   {:key :compiler-exception-cannot-resolve-symbol
    :class "RuntimeException"
    :match (beginandend "Unable to resolve symbol: (.+) in this context")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Name "
                                                           (nth matches 1) :arg " is undefined.\n"))}

   ;############################
   ;### Arithmetic Exception ###
   ;############################

   {:key :arithmetic-exception-divide-by-zero
    :class "ArithmeticException"
    :match (beginandend "Divide by zero")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Tried to divide by zero\n"))}

    ;######################################
    ;### Index Out of Bounds Exceptions ###
    ;######################################

  ;###############################
   ;### Number Format Exception ###
   ;###############################

    {:key :number-format-exception
    :class "NumberFormatException"
    :match (beginandend "Invalid number: (\\S*)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Invalid number: " (nth matches 1) :arg ".\n"))}

    ;#####################################################################
    ;### Runtime Exceptions or clojure.lang.LispReader$ReaderException ###
    ;#####################################################################

    {:key :reader-tag-must-be-symbol
    :class "RuntimeException"
    :match (beginandend "Reader tag must be a symbol")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "# must be followed by a symbol.\n"))}

    {:key :invalid-tolken-error
    :class "RuntimeException"
    :match (beginandend "Invalid token: (\\S*)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "You cannot use " (clojure.string/replace (nth matches 1) #"^/.*|.*/$" "/") :arg " in this position.\n"))}

    {:key :syntax-error-cant-specifiy-over-20-args
    :class "RuntimeException"
    :match (beginandend "Can't specify more than 20 params")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "A function may not take more than 20 parameters.\n" ))}

    {:key :compiler-exception-first-argument-must-be-symbol
    :class "RuntimeException"
    :match (beginandend "First argument to (\\S*) must be a Symbol")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (nth matches 1) :arg " must be followed by a name.\n"))}

    {:key :compiler-exception-cannot-take-value-of-macro
   :class "RuntimeException"
   :match (beginandend "Can't take value of a macro: (\\S*)")
   :make-msg-info-obj (fn [matches] (make-msg-info-hashes
                                                          (get-macro-name (nth matches 1)) :arg
                                                          " is a macro, cannot be passed to a function.\n"))}

    #_{:key :compiler-exception-first-argument-must-be-symbol
    ;spec
    :class "RuntimeException"
    :match (beginandend "First argument to (\\S*) must be a Symbol(\\S*)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (nth matches 1) :arg " must be followed by a name.\n"))}

    {:key :compiler-exception-unmatched-delimiter
    :class "RuntimeException"
    :match (beginandend "Unmatched delimiter: (\\S*)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "There is an unmatched delimiter " (nth matches 1) :arg ".\n"))}

    {:key :compiler-exception-end-of-file
    :class "RuntimeException"
    :match (beginandend "EOF while reading, starting at line (.+)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "End of file, starting at line.\nProbably a non-closing parenthesis or bracket.\n"))}

    {:key :compiler-exception-end-of-file-string
    ;this cannot be done in repl needs to be tested still
    :class "RuntimeException"
    :match (beginandend "EOF while reading string")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "An opened " "\"" :arg " does not have a matching closing one.\n"))}

    {:key :compiler-exception-end-of-file-##
    ;This error message needs to be improved
    :class "RuntimeException"
    :match (beginandend "EOF while reading")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "End of file " (nth matches 1) :arg ".\n"))}

    {:key :compiler-exception-end-of-file
    ;This error message needs to be improved
    :class "UnsupportedOperationException"
    :match (beginandend "Can only recur from tail")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "You can only recur from the tail\n"))}

    ;###############################
    ;### Illegal State Exception ###
    ;###############################

    {:key :compiler-exception-end-of-file
    :class "IllegalStateException"
    :match (beginandend "arg literal must be %, %& or %integer")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "% can only be followed & or a number.\n"))}


   ;#####################
   ;### Default Error ###
   ;#####################

   {:key :other
    :class "default"
    :match (beginandend "")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Default Error: " (nth matches 0) :arg "\n"))}])
