(ns errors.error-dictionary
  (:use [errors.messageobj]
        [errors.dictionaries]))

;; A vector of error dictionaries from the most specific one to the most general one.
;; Order matters because the vector is searched from top to bottom.

(def error-dictionary
  [
   ;#############################
   ;### Class Cast Exceptions ###
   ;#############################

   #_{:key :class-cast-exception-cannot-cast-to-map-entry
      :class "ClassCastException"
      :match #"(?s)(.*) cannot be cast to java\.util\.Map\$Entry(.*)"
      :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Attempted to create a map using "
                                                             (get-type (nth matches 1)) :type
                                                             ", but a sequence of vectors of length 2 or a sequence of maps is needed.\n"))}
   {:key :class-cast-exception
    :class "ClassCastException"
    :match (beginandend "Cannot cast (\\S*) to (\\S*)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Expected "
                                                           (get-type (nth matches 2)) :type ", but "
                                                           (get-type (nth matches 1)) :type " was given instead.\n"))}

    {:key :class-cast-exception-lower-case
     :class "ClassCastException"
     :match (beginandend "(\\S*) cannot be cast to (\\S*)")
     :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Expected "
                                                            (get-type (nth matches 2)) :type ", but "
                                                            (get-type (nth matches 1)) :type " was given instead.\n"))}

    ;###################################
    ;### Illegal Argument Exceptions ###
    ;###################################

    {:key :assoc-parity-error
    :class "IllegalArgumentException"
    :match (beginandend "assoc expects even number of arguments after map/vector, found odd number")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "The arguments following the hashmap or vector in assoc must come in pairs, but one argument does not have a match.\n"))}

    {:key :wrong-number-of-args-passed-to-a-keyword
    :class "IllegalArgumentException"
    :match (beginandend "Wrong number of args passed to keyword: (\\S*)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "A keyword: " (nth matches 1) :arg " can only take one or two arguments.\n"))}

    {:key :illegal-argument-no-val-supplied-for-key
    :class "IllegalArgumentException"
    :match (beginandend "No value supplied for key: (\\S*)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Every key for a hashmap must be followed by a value, but the key "
                                                           ; is this too wordy?
                                                           ;(nth matches 1) :arg ". Every key must be paired with a value; the value should be immediately following the key."))
                                                           (nth matches 1) :arg " does not have a matching value.\n"))}

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

    {:key :illegal-argument-exactly-two-forms
    :class "IllegalArgumentException"
    :match #"(?s)(\S*) requires exactly 2 forms in binding vector(.*)"
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (get-function-name (nth matches 1)) :arg " requires exactly two elements in its vector, but a different number was given.\n"))}

    {:key :cant-call-nil
    :class "IllegalArgumentException"
    :match (beginandend "Can't call nil, form: \\((.*)\\)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "You cannot call nil as a function. The expression was: " "(" (print-macro-arg (read-string (str "("(nth matches 1)")"))) ")"))}

    {:key :duplicate-key-hashmap
    :class "IllegalArgumentException"
    :match (beginandend "Duplicate key: (\\S*)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "You have duplicated the key " (nth matches 1) :arg ", you cannot use the same key in a hashmap twice.\n"))}

    {:key :loop-req-vector
    :class "IllegalArgumentException"
    :match (beginandend "loop requires a vector for its binding")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Loop requires a vector for its binding.\n"))}

    {:key :other-req-vector
    :class "IllegalArgumentException"
    :match (beginandend "(\\S+) requires a vector for its binding")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (get-function-name (nth matches 1)) :arg " requires a vector for its binding.\n"))}

    {:key :recur-arg-mismatch
    :class "IllegalArgumentException"
    :match (beginandend #"Mismatched argument count to recur, expected: (.*) args, got: (.*)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Recur expected " (number-arg (nth matches 1)) " but was given " (number-arg (nth matches 2)) :arg ".\n"))}

    {:key :no-matching-clause
    :class "IllegalArgumentException"
    :match (beginandend #"No matching clause: (.*)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "The 'case' input " (nth matches 1) " didn't match any of the options.\n"))}

    {:key :illegal-input-stream
    :class "IllegalArgumentException"
    :match (beginandend "Cannot open \\<(.*)\\> as an InputStream")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (nth matches 1) :arg
                                                           " cannot be opened as an InputStream.\n"))}

   {:key :illegal-input-must-be-x
   :class "IllegalArgumentException"
   :match (beginandend "Argument must be an (.*): (.*)  (.*)\\.(.*)\\.")
   :make-msg-info-obj (fn [matches] (make-msg-info-hashes "The argument must be an " (nth matches 1) :type
                                                           " but is " (nth matches 2) :arg
                                                           ".\n"))}

   {:key :illegal-not-supported-on-type
   :class "IllegalArgumentException"
   :match (beginandend #"(\S*) not supported on type: (\S+)")
   :make-msg-info-obj (fn [matches] (make-msg-info-hashes "The function " (get-function-name (nth matches 1))
                                                          " doesn't work on "
                                                          (get-type (nth matches 2)) ".\n"))}

    {:key :no-ctor-found
    :class "IllegalArgumentException"
    :match (beginandend #"No matching ctor found for class (.+)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "There is no constructor for the class " (nth matches 1) " with this number and type of arguments.\n"))}


  ;;(beginandend #"Call to (.*)/(.*) did not conform to spec:(.*)In: (.*) val: (.*) fails spec: :clojure\.core\.specs\.alpha/local-name (.*) predicate: simple-symbol\?")
   ;########################
   ;### Assertion Errors ###
   ;########################

   #_{:key :assertion-error-with-argument
    ;need to test
    :class "AssertionError"
    :match (beginandend "Assert failed: \\((\\S*) argument(\\S*)\\)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "You cannot assert on " (nth matches 2) ".\n"))} ; process-asserts-obj from dictionaries.clj

   {:key :assertion-error-without-argument
    :class "AssertionError"
    :match (beginandend "Assert failed: (\\S*)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "You cannot assert on " (nth matches 1) :arg ".\n"))}


    ;########################
    ;### Arity Exceptions ###
    ;########################

    ;; Need to revisit this one: we might want to add a spec to it
    {:key :wrong-number-of-args-passed-to-a-keyword
    :class "ArityException"
    :match (beginandend "Wrong number of args \\((\\S*)\\) passed to: core/keyword")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "A function " "keyword" :arg " can only take one or two arguments, but "
    (number-arg (nth matches 1)) " were passed to it.\n"))}

    {:key :wrong-number-of-args-passed-to-core
    ;we may want to find a way to make this less general
    :class "ArityException"
    :match (beginandend "Wrong number of args \\((\\S*)\\) passed to: core/(\\S*)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (check-function-name (get-function-name (nth matches 2))) :arg " cannot take " (number-arg (nth matches 1)) :arg ".\n"))}

    {:key :wrong-number-of-args-passed-to-user-defined-one-arg
    ;we may want to find a way to make this less general
    :class "ArityException"
    :match (beginandend "Wrong number of args \\(1\\) passed to: (\\S+) ")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (check-function-name (get-function-name (nth matches 1))) :arg " cannot be called with one argument.\n"))}

    {:key :wrong-number-of-args-passed-to-user-defined-other
    ;we may want to find a way to make this less general
    :class "ArityException"
    :match (beginandend "Wrong number of args \\((\\S*)\\) passed to: (\\S*) ")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (check-function-name (get-function-name (nth matches 2))) :arg " cannot be called with " (number-arg (nth matches 1)) :arg ".\n"))}

    ;#####################
    ;### Syntax Errors ###
    ;#####################

   {:key :compiler-exception-cannot-resolve-symbol
    :class "RuntimeException"
    :match (beginandend "Unable to resolve symbol: (.+) in this context")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (change-if (nth matches 1)) :arg ".\n"))}

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

    {:key :string-index-out-of-bounds
    :class "StringIndexOutOfBoundsException"
    :match (beginandend "String index out of range: (\\S+)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Position " (number-word (nth matches 1)) :arg " is outside of the string.\n"))}

    {:key :index-out-of-bounds-index-not-provided
    :class "IndexOutOfBoundsException"
    :match (beginandend "") ; an empty message
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "An index in a sequence is out of bounds or invalid.\n"))}

   ;###############################
   ;### Null Pointer Exceptions ###
   ;###############################

   {:key :null-pointer-non-existing-object-provided
    ;need to test
    :class "NullPointerException"
    :match (beginandend "(.+)") ; for some reason (.*) matches twice. Since we know there is at least one symbol, + is fine
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "An attempt to access a non-existing object: "
                                                           (nth matches 1) :arg " (NullPointerException).\n"))}

    {:key :null-pointer-non-existing-object-not-provided
    :class "NullPointerException"
    :match  (beginandend "")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "An attempt to access a non-existing object (NullPointerException).\n"))}

    ;########################################
    ;### Unsupported Operation Exceptions ###
    ;########################################

    {:key :unsupported-operation-wrong-type-of-argument
    ;need to test
    :class "UnsupportedOperationException"
    :match (beginandend "(\\S*) not supported on this type: (\\S*)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Function " (nth matches 1) :arg
                                                           " does not allow " (get-type (nth matches 2)) :type " as an argument in this position.\n"))}

    {:key :compiler-exception-must-recur-from-tail-position
    :class "UnsupportedOperationException"
    :match (beginandend "Can only recur from tail position")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Recur can only occur as a tail call: no operations can be done on its result.\n"))}

   ;##############################
   ;### ClassNotFoundException ###
   ;##############################

    {:key :class-not-found-exception
    :class "ClassNotFoundException"
    :match (beginandend "(\\S*)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Name " (nth matches 1) :arg " is undefined.\n"))}


   ;###############################
   ;### Number Format Exception ###
   ;###############################

    {:key :number-format-exception
    :class "NumberFormatException"
    :match (beginandend "Invalid number: (\\S*)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "The format of the number " (nth matches 1) :arg " is invalid.\n"))}

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
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (nth matches 1) :arg " is an invalid token.\n"))}

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
   :make-msg-info-obj (fn [matches] (make-msg-info-hashes (get-macro-name (nth matches 1)) :arg " is a macro and cannot be used by itself or passed to a function.\n"))}

   #_{:key :compiler-exception-cannot-resolve-symbol
    :class "RuntimeException"
    :match (beginandend "Unable to resolve symbol: (\\S*) in this context")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Name " (nth matches 1) :arg " is undefined.\n"))}

    {:key :compiler-exception-map-literal-even
    :class "RuntimeException"
    :match (beginandend "Map literal must contain an even number of forms")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "You have a key that's missing a value; a hashmap must consist of key/value pairs.\n"))}

    #_{:key :compiler-exception-first-argument-must-be-symbol
    ;spec
    :class "RuntimeException"
    :match (beginandend "First argument to (\\S*) must be a Symbol(\\S*)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (nth matches 1) :arg " must be followed by a name.\n"))}

    {:key :compiler-exception-unmatched-delimiter
    :class "RuntimeException"
    :match (beginandend "Unmatched delimiter: (\\S*)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "There is an unmatched delimiter " (nth matches 1) :arg ".\n"))}

    {:key :compiler-exception-too-many-arguments
    :class "RuntimeException"
    :match (beginandend "Too many arguments to (\\S*)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Too many arguments to " (nth matches 1) :arg ".\n"))}

   {:key :compiler-exception-too-few-arguments
    :class "RuntimeException"
    :match (beginandend "Too few arguments to (\\S*)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Too few arguments to " (nth matches 1) :arg ".\n"))}

    {:key :compiler-exception-end-of-file
    :class "RuntimeException"
    :match (beginandend "EOF while reading, starting at line (\\d+) ")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Unexpected end of file, starting at line " (nth matches 1) ". Probably a non-closing parenthesis or bracket.\n"))}

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

    {:key :compiler-exception-no-such-var
    :class "RuntimeException"
    :match (beginandend "No such var: (\\S*)/(\\S*),")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (nth matches 2) :args
                                                           " is not a function in the " (nth matches 1) :args
                                                           " library.\n"))}

   {:key :compiler-exception-same-arity
   :class "RuntimeException"
   :match (beginandend "Can't have 2 overloads with same arity")
   :make-msg-info-obj (fn [matches] (make-msg-info-hashes "The function definition has two cases with the same number of arguments; only one case is allowed.\n"))}

    {:key :compiler-exception-recur-tail
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
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "% can only be followed by & or a number.\n"))}

    {:key :illegal-state-validater
    :class "IllegalStateException"
    :match (beginandend "Invalid reference state  (\\S*)\\.validate")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "IllegalState: failed validation.\n"))}

    {:key :illegal-state-transaction
    :class "IllegalStateException"
    :match (beginandend "No transaction running  (\\S*)\\.LockingTransaction(\\S*)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "IllegalState: trying to lock a transaction that is not running.\n"))}

    {:key :illegal-state-transaction-IO
    :class "IllegalStateException"
    :match (beginandend "I/O in transaction")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "IllegalState: I/0 in transaction.\n"))}

    ;###################################
    ;### Memory and Stack Exceptions ###
    ;###################################

    {:key :out-of-memory
    :class "OutOfMemoryError"
    :match (beginandend "Java heap space")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Clojure ran out of memory, likely due to an infinite computation.\n"))}

    {:key :stack-overflow-with-name
    :class "StackOverflowError"
    :match (beginandend "(.*)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Clojure ran out of memory, likely due to an infinite computation or infinite recursion.\n"))}

    ;#################################
    ;### File Not Found Exceptions ###
    ;#################################

    {:key :file-does-not-exist
    :class "FileNotFoundException"
    :match (beginandend "(.*) \\(No such file or directory\\)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "The file " (nth matches 1) :arg
                                                           " does not exist.\n"))}

    {:key :file-does-not-exist-windows
    :class "FileNotFoundException"
    :match (beginandend "(.*) \\(The system cannot find the file specified\\)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "The file " (nth matches 1) :arg
                                                           " does not exist.\n"))}

    {:key :file-not-found-on-load
    :class "FileNotFoundException"
    :match (beginandend "Could not locate (\\S+).class or (\\S+).clj on classpath(.*)load")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "The system was looking for a class "
                                                           (str (nth matches 2) ".class") :arg
                                                           " or a file " (str (nth matches 2) ".clj") :arg
                                                           ", but neither one was found.\n"))}

    ;###############
    ;### Warning ###
    ;###############

    {:key :other
     :class "WARNING:"
     :match (beginandend "(\\S*) already refers to: (\\S*) in namespace: (\\S*), being replaced by: (\\S*)")
     :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Warning: " (nth matches 1) :arg
                                                            " already refers to: " (nth matches 2) :arg
                                                            " in namespace: " (nth matches 3) :arg
                                                            " being replaced by " (nth matches 4) :arg
                                                            ".\n\n"))}

   ;#####################
   ;### Default Error ###
   ;#####################

   {:key :other
    :class "default"
    :match (beginandend "")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Default Error: " (nth matches 0) :arg "\n"))}])
