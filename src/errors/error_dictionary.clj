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

   {:key :class-cast-exception
    :class "ClassCastException"
    :match (beginandend "Cannot cast (\\S*) to (\\S*)")
    :make-msg-info-obj (fn [matches] (str "Expected "
                                           (get-type (nth matches 2)) ", but "
                                           (get-type (nth matches 1)) " was given instead.\n"))}

    {:key :class-cast-exception-lower-case
     :class "ClassCastException"
     :match (beginandend "(\\S*) cannot be cast to (\\S*)")
     :make-msg-info-obj (fn [matches] (str "Expected "
                                           (get-type (nth matches 2)) ", but "
                                           (get-type (nth matches 1)) " was given instead.\n"))}

    ;###################################
    ;### Illegal Argument Exceptions ###
    ;###################################

    {:key :assoc-parity-error
    :class "IllegalArgumentException"
    :match (beginandend "assoc expects even number of arguments after map/vector, found odd number")
    :make-msg-info-obj (fn [matches] (str "The arguments following the hashmap or vector in assoc must come in pairs,"
                                          " but one argument does not have a match.\n"))}

    {:key :wrong-number-of-args-passed-to-a-keyword
    :class "IllegalArgumentException"
    :match (beginandend "Wrong number of args passed to keyword: (\\S*)")
    :make-msg-info-obj (fn [matches] (str "A keyword: "
                                          (nth matches 1)
                                          " can only take one or two arguments.\n"))}

    {:key :illegal-argument-no-val-supplied-for-key
    :class "IllegalArgumentException"
    :match (beginandend "No value supplied for key: (\\S*)")
    :make-msg-info-obj (fn [matches] (str "Every key for a hashmap must be followed by a value, but the key "
                                          (nth matches 1)
                                          " does not have a matching value.\n"))}

    {:key :illegal-argument-vector-arg-to-map-conj
    :class "IllegalArgumentException"
    :match (beginandend "Vector arg to map conj must be a pair")
    :make-msg-info-obj (fn [matches] (str "Vectors added to a map must consist of two elements: a key and a value.\n"))}

    {:key :illegal-argument-cannot-convert-type
    ;spec
    :class "IllegalArgumentException"
    :match #"(?s)Don't know how to create (\S*) from: (\S*)(.*)"
    :make-msg-info-obj (fn [matches] (str "Don't know how to create "
                                          (get-type (nth matches 1))
                                          " from "
                                          (get-type (nth matches 2))
                                          ".\n"))}

    {:key :illegal-argument-even-number-of-forms
    :class "IllegalArgumentException"
    :match #"(?s)(\S*) requires an even number of forms(.*)"
    :make-msg-info-obj (fn [matches] (str "Parameters for "
                                          (nth matches 1)
                                          " must come in pairs, but one of them does not have a match.\n"))}

    {:key :illegal-argument-exactly-two-forms
    :class "IllegalArgumentException"
    :match #"(?s)(\S*) requires exactly 2 forms in binding vector(.*)"
    :make-msg-info-obj (fn [matches] (str (get-function-name (nth matches 1))
                                          " requires exactly two elements in its vector, "
                                          "but a different number was given.\n"))}

    {:key :cant-call-nil
    :class "IllegalArgumentException"
    :match (beginandend "Can't call nil, form: \\((.*)\\)")
    :make-msg-info-obj (fn [matches] (str "You cannot call nil as a function. The expression was: "
                                          "("
                                          (print-macro-arg (read-string (str "("(nth matches 1)")")))
                                          ")"))}

    {:key :duplicate-key-hashmap
    :class "IllegalArgumentException"
    :match (beginandend "Duplicate key: (\\S*)")
    :make-msg-info-obj (fn [matches] (str "You have duplicated the key "
                                          (nth matches 1)
                                          ", you cannot use the same key in a hashmap twice.\n"))}

    {:key :loop-req-vector
    :class "IllegalArgumentException"
    :match (beginandend "loop requires a vector for its binding")
    :make-msg-info-obj (fn [matches] (str "Loop requires a vector for its binding.\n"))}

    {:key :other-req-vector
    :class "IllegalArgumentException"
    :match (beginandend "(\\S+) requires a vector for its binding")
    :make-msg-info-obj (fn [matches] (str (get-function-name (nth matches 1))
                                          " requires a vector for its binding.\n"))}

    {:key :recur-arg-mismatch
    :class "IllegalArgumentException"
    :match (beginandend #"Mismatched argument count to recur, expected: (.*) args, got: (.*)")
    :make-msg-info-obj (fn [matches] (str "Recur expected "
                                          (number-arg (nth matches 1))
                                          " but was given "
                                          (number-arg (nth matches 2))
                                          ".\n"))}

    {:key :no-matching-clause
    :class "IllegalArgumentException"
    :match (beginandend #"No matching clause: (.*)")
    :make-msg-info-obj (fn [matches] (str "The 'case' input "
                                          (nth matches 1)
                                          " didn't match any of the options.\n"))}

    {:key :illegal-input-stream
    :class "IllegalArgumentException"
    :match (beginandend "Cannot open \\<(.*)\\> as an InputStream")
    :make-msg-info-obj (fn [matches] (str (nth matches 1)
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
   :make-msg-info-obj (fn [matches] (str "The function "
                                         (get-function-name (nth matches 1))
                                         " doesn't work on "
                                         (get-type (nth matches 2))
                                         ".\n"))}

    {:key :no-ctor-found
    :class "IllegalArgumentException"
    :match (beginandend #"No matching ctor found for class (.+)")
    :make-msg-info-obj (fn [matches] (str "There is no constructor for the class "
                                          (nth matches 1)
                                          " with this number and type of arguments.\n"))}

    {:key :no-method-found
    :class "IllegalArgumentException"
    :match (beginandend #"No matching method (.+) found taking (\d+) args for class (.+)")
    :make-msg-info-obj (fn [matches] (str "There is no method "
                                          (nth matches 1)
                                          " with "
                                          (number-arg (nth matches 2))
                                          " or with this type of argument(s)"
                                          (get-common-class (nth matches 3))
                                          ".\n"))}

    ;; Note: could be a field or a method call with no args
    {:key :no-field-found
    :class "IllegalArgumentException"
    :match (beginandend #"No matching field found: (.+) for class (.+)")
    :make-msg-info-obj (fn [matches] (str "There is no method "
                                          (nth matches 1)
                                          " with no arguments or a field "
                                          (nth matches 1)
                                          (get-common-class (nth matches 2))
                                          ".\n"))}

    {:key :illegal-arg-must-be-int-lazy
    :class "IllegalArgumentException"
    :match (beginandend #"Argument must be an integer: clojure\.lang\.LazySeq(.*)")
    :make-msg-info-obj (fn [matches] (str "Expected an integer number, but a sequence was given instead.\n"))}

    {:key :illegal-arg-must-be-int-seq
    :class "IllegalArgumentException"
    :match (beginandend #"Argument must be an integer: (.*)")
    :make-msg-info-obj (fn [matches] (str "Expected an integer number, but a sequence "
                                          (nth matches 1)
                                          " was given instead.\n"))}

   ;########################
   ;### Assertion Errors ###
   ;########################

;; TODO: fix those! Issue #103
   #_{:key :assertion-error-with-argument
    ;need to test
    :class "AssertionError"
    :match (beginandend "Assert failed: \\((\\S*) argument(\\S*)\\)")
    :make-msg-info-obj (fn [matches] (str "You cannot assert on " (nth matches 2) ".\n"))} ; process-asserts-obj from dictionaries.clj

   {:key :assertion-error-without-argument
    :class "AssertionError"
    :match (beginandend "Assert failed: (\\S*)")
    :make-msg-info-obj (fn [matches] (str "You cannot assert on " (nth matches 1) ".\n"))}


    ;########################
    ;### Arity Exceptions ###
    ;########################

    ;; Need to revisit this one: we might want to add a spec to it
    {:key :wrong-number-of-args-passed-to-a-keyword
    :class "ArityException"
    :match (beginandend "Wrong number of args \\((\\S*)\\) passed to: core/keyword")
    :make-msg-info-obj (fn [matches] (str "A function "
                                          "keyword" ; Might need to add single quotes
                                          " can only take one or two arguments, but "
                                          (number-arg (nth matches 1))
                                          " were passed to it.\n"))}

    {:key :wrong-number-of-args-passed-to-core
    ;we may want to find a way to make this less general
    :class "ArityException"
    :match (beginandend "Wrong number of args \\((\\S*)\\) passed to: core/(\\S*)")
    :make-msg-info-obj (fn [matches] (str (check-function-name (get-function-name (nth matches 2)))
                                          " cannot take "
                                          (number-arg (nth matches 1))
                                          ".\n"))}

    {:key :wrong-number-of-args-passed-to-user-defined-one-arg
    ;we may want to find a way to make this less general
    :class "ArityException"
    :match (beginandend "Wrong number of args \\(1\\) passed to: (\\S+)")
    :make-msg-info-obj (fn [matches] (str (check-function-name (get-function-name (nth matches 1)))
                                          " cannot be called with one argument.\n"))}

    {:key :wrong-number-of-args-passed-to-user-defined-other
    ;we may want to find a way to make this less general
    :class "ArityException"
    :match (beginandend "Wrong number of args \\((\\S*)\\) passed to: (\\S*)")
    :make-msg-info-obj (fn [matches] (str (check-function-name (get-function-name (nth matches 2)))
                                          " cannot be called with "
                                          (number-arg (nth matches 1))
                                          ".\n"))}

    ;#####################
    ;### Syntax Errors ###
    ;#####################

   {:key :compiler-exception-cannot-resolve-symbol-if
    :class "RuntimeException"
    :match (beginandend "Unable to resolve symbol: if in this context")
    :make-msg-info-obj (fn [matches] (str "You are not using if correctly.\n"))}

  {:key :compiler-exception-cannot-resolve-symbol
   :class "RuntimeException"
   :match (beginandend "Unable to resolve symbol: (.+) in this context")
   :make-msg-info-obj (fn [matches] (str (change-if (nth matches 1))
                                         ".\n"))}

;;
   ;############################
   ;### Arithmetic Exception ###
   ;############################

   {:key :arithmetic-exception-divide-by-zero
    :class "ArithmeticException"
    :match (beginandend "Divide by zero")
    :make-msg-info-obj (fn [matches] (str "Tried to divide by zero\n"))}

   ;######################################
   ;### Index Out of Bounds Exceptions ###
   ;######################################

    {:key :string-index-out-of-bounds
    :class "StringIndexOutOfBoundsException"
    :match (beginandend "String index out of range: (\\S+)")
    :make-msg-info-obj (fn [matches] (str "Position "
                                          (number-word (nth matches 1))
                                          " is outside of the string.\n"))}

    {:key :index-out-of-bounds-index-not-provided
    :class "IndexOutOfBoundsException"
    :match (beginandend "") ; an empty message
    :make-msg-info-obj (fn [matches] (str "An index in a sequence is out of bounds or invalid.\n"))}

   ;###############################
   ;### Null Pointer Exceptions ###
   ;###############################

   {:key :null-pointer-non-existing-object-provided
    ;need to test
    :class "NullPointerException"
    :match (beginandend "(.+)") ; for some reason (.*) matches twice. Since we know there is at least one symbol, + is fine
    :make-msg-info-obj (fn [matches] (str "An attempt to access a non-existing object: "
                                          (nth matches 1)
                                          " (NullPointerException).\n"))}

    {:key :null-pointer-non-existing-object-not-provided
    :class "NullPointerException"
    :match  (beginandend "")
    :make-msg-info-obj (fn [matches] (str "An attempt to access a non-existing object (NullPointerException).\n"))}

    ;########################################
    ;### Unsupported Operation Exceptions ###
    ;########################################

    {:key :unsupported-operation-wrong-type-of-argument
    ;need to test
    :class "UnsupportedOperationException"
    :match (beginandend "(\\S*) not supported on this type: (\\S*)")
    :make-msg-info-obj (fn [matches] (str "Function "
                                          (nth matches 1)
                                          " does not allow "
                                          (get-type (nth matches 2))
                                          " as an argument in this position.\n"))}

    {:key :compiler-exception-must-recur-from-tail-position
    :class "UnsupportedOperationException"
    :match (beginandend "Can only recur from tail position")
    :make-msg-info-obj (fn [matches] (str "Recur can only occur as a tail call: no operations can be done on its result.\n"))}

   ;##############################
   ;### ClassNotFoundException ###
   ;##############################

    {:key :class-not-found-exception
    :class "ClassNotFoundException"
    :match (beginandend "(\\S*)")
    :make-msg-info-obj (fn [matches] (str "Name "
                                          (nth matches 1)
                                          " is undefined.\n"))}


   ;###############################
   ;### Number Format Exception ###
   ;###############################

    {:key :number-format-exception
    :class "NumberFormatException"
    :match (beginandend "Invalid number: (\\S*)")
    :make-msg-info-obj (fn [matches] (str "The format of the number "
                                          (nth matches 1)
                                          " is invalid.\n"))}

    ;#####################################################################
    ;### Runtime Exceptions or clojure.lang.LispReader$ReaderException ###
    ;#####################################################################

    {:key :reader-tag-must-be-symbol
    :class "RuntimeException"
    :match (beginandend "Reader tag must be a symbol")
    :make-msg-info-obj (fn [matches] (str "# must be followed by a symbol.\n"))}

    {:key :invalid-tolken-error
    :class "RuntimeException"
    :match (beginandend "Invalid token: (\\S*)")
    :make-msg-info-obj (fn [matches] (str (nth matches 1)
                                          " is an invalid token.\n"))}

    {:key :syntax-error-cant-specifiy-over-20-args
    :class "RuntimeException"
    :match (beginandend "Can't specify more than 20 params")
    :make-msg-info-obj (fn [matches] (str "A function may not take more than 20 parameters.\n" ))}

    {:key :compiler-exception-first-argument-must-be-symbol
    :class "RuntimeException"
    :match (beginandend "First argument to (\\S*) must be a Symbol")
    :make-msg-info-obj (fn [matches] (str (nth matches 1)
                                          " must be followed by a name.\n"))}

    {:key :compiler-exception-cannot-take-value-of-macro
     :class "RuntimeException"
     :match (beginandend "Can't take value of a macro: (\\S*)")
     :make-msg-info-obj (fn [matches] (str (get-macro-name (nth matches 1))
                                           " is a macro and cannot be used by itself or passed to a function.\n"))}

    {:key :compiler-exception-map-literal-even
     :class "RuntimeException"
     :match (beginandend "Map literal must contain an even number of forms")
     :make-msg-info-obj (fn [matches] (str "You have a key that's missing a value; a hashmap must consist of key/value pairs.\n"))}

    {:key :compiler-exception-unmatched-delimiter
     :class "RuntimeException"
     :match (beginandend "Unmatched delimiter: (\\S*)")
     :make-msg-info-obj (fn [matches] (str "There is an unmatched delimiter "
                                          (nth matches 1)
                                          ".\n"))}

    {:key :compiler-exception-too-many-arguments
     :class "RuntimeException"
     :match (beginandend "Too many arguments to (\\S*)")
     :make-msg-info-obj (fn [matches] (str "Too many arguments to "
                                           (nth matches 1)
                                           ".\n"))}

   {:key :compiler-exception-too-few-arguments
    :class "RuntimeException"
    :match (beginandend "Too few arguments to (\\S*)")
    :make-msg-info-obj (fn [matches] (str "Too few arguments to "
                                          (nth matches 1)
                                          ".\n"))}

    {:key :compiler-exception-end-of-file
     :class "RuntimeException"
     :match (beginandend "EOF while reading, starting at line (\\d+) ")
     :make-msg-info-obj (fn [matches] (str "Unexpected end of file, starting at line "
                                           (nth matches 1)
                                           ". Probably a non-closing parenthesis or bracket.\n"))}

    {:key :compiler-exception-end-of-file-string
    ;this cannot be done in repl needs to be tested still
     :class "RuntimeException"
     :match (beginandend "EOF while reading string")
     :make-msg-info-obj (fn [matches] (str "An opened "
                                           "\""
                                           " does not have a matching closing one.\n"))}

    {:key :compiler-exception-end-of-file-##
    ;This error message needs to be improved
     :class "RuntimeException"
     :match (beginandend "EOF while reading")
     :make-msg-info-obj (fn [matches] (str "End of file "
                                           (nth matches 1)
                                           ".\n"))}

    {:key :compiler-exception-no-such-var
     :class "RuntimeException"
     :match (beginandend "No such var: (\\S*)/(\\S*),")
     :make-msg-info-obj (fn [matches] (str (nth matches 2)
                                           " is not a function in the "
                                           (nth matches 1)
                                           " library.\n"))}

   {:key :compiler-exception-same-arity
    :class "RuntimeException"
    :match (beginandend "Can't have 2 overloads with same arity")
    :make-msg-info-obj (fn [matches] (str "The function definition has two cases with the same number of arguments; only one case is allowed.\n"))}

    {:key :compiler-exception-recur-tail
    ;This error message needs to be improved
     :class "UnsupportedOperationException"
     :match (beginandend "Can only recur from tail")
     :make-msg-info-obj (fn [matches] (str "You can only recur from the tail\n"))}

    ;###############################
    ;### Illegal State Exception ###
    ;###############################

    {:key :compiler-exception-end-of-file
     :class "IllegalStateException"
     :match (beginandend "arg literal must be %, %& or %integer")
     :make-msg-info-obj (fn [matches] (str "% can only be followed by & or a number.\n"))}

    {:key :illegal-state-validater
     :class "IllegalStateException"
     :match (beginandend "Invalid reference state  (\\S*)\\.validate")
     :make-msg-info-obj (fn [matches] (str "IllegalState: failed validation.\n"))}

    {:key :illegal-state-transaction
     :class "IllegalStateException"
     :match (beginandend "No transaction running  (\\S*)\\.LockingTransaction(\\S*)")
     :make-msg-info-obj (fn [matches] (str "IllegalState: trying to lock a transaction that is not running.\n"))}

    {:key :illegal-state-transaction-IO
     :class "IllegalStateException"
     :match (beginandend "I/O in transaction")
     :make-msg-info-obj (fn [matches] (str "IllegalState: I/0 in transaction.\n"))}

    ;###################################
    ;### Memory and Stack Exceptions ###
    ;###################################

    {:key :out-of-memory
     :class "OutOfMemoryError"
     :match (beginandend "Java heap space")
     :make-msg-info-obj (fn [matches] (str "Clojure ran out of memory, likely due to an infinite computation.\n"))}

    {:key :stack-overflow-with-name
     :class "StackOverflowError"
     :match (beginandend "(.*)")
     :make-msg-info-obj (fn [matches] (str "Clojure ran out of memory, likely due to an infinite computation or infinite recursion.\n"))}

    ;#################################
    ;### File Not Found Exceptions ###
    ;#################################

    {:key :file-does-not-exist
     :class "FileNotFoundException"
     :match (beginandend "(.*) \\(No such file or directory\\)")
     :make-msg-info-obj (fn [matches] (str "The file "
                                           (nth matches 1)
                                           " does not exist.\n"))}

    {:key :file-does-not-exist-windows
     :class "FileNotFoundException"
     :match (beginandend "(.*) \\(The system cannot find the file specified\\)")
     :make-msg-info-obj (fn [matches] (str "The file "
                                           (nth matches 1)
                                           " does not exist.\n"))}

    {:key :file-not-found-on-load
     :class "FileNotFoundException"
     :match (beginandend "Could not locate (\\S+).class or (\\S+).clj on classpath(.*)load")
     :make-msg-info-obj (fn [matches] (str "The system was looking for a class "
                                          (nth matches 2)
                                          ".class"
                                          " or a file " (nth matches 2)
                                          ".clj"
                                          ", but neither one was found.\n"))}

    ;###############
    ;### Warning ###
    ;###############

    {:key :other
     :class "WARNING:"
     :match (beginandend "(\\S*) already refers to: (\\S*) in namespace: (\\S*), being replaced by: (\\S*)")
     :make-msg-info-obj (fn [matches] (str "Warning: "
                                           (nth matches 1)
                                           " already refers to: "
                                           (nth matches 2)
                                           " in namespace: "
                                           (nth matches 3)
                                           " being replaced by "
                                           (nth matches 4)
                                           ".\n\n"))}

   ;#####################
   ;### Default Error ###
   ;#####################

   {:key :other
    :class "default"
    :match (beginandend "")
    :make-msg-info-obj (fn [matches] (str "Default Error: "
                                          (nth matches 0)
                                          "\n"))}])
