(ns errors.error-dictionary
  (:use [errors.messageobj]
        [errors.dictionaries]))

;; A vector of error dictionaries from the most specific one to the most general one.
;; Order matters because the vector is searched from top to bottom.

; (defn beginandend [x]
;   (re-pattern (str "(?s)" x "(.*)")))

(def error-dictionary
  [;########################
   ;##### Spec Errors ######
   ;########################

   {:key :bindings-even-number-of-forms
    :class "ExceptionInfo"
    :match (beginandend #"Call to (.*)/(.*) did not conform to spec(.*):clojure\.core\.specs\.alpha/bindings(.*)predicate: any\?,  Insufficient input")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Parameters for " (nth matches 2) :arg
    " must come in pairs, but one of them does not have a match.\n"))}

    {:key :vector-expected-for-bindings
     :class "ExceptionInfo"
     :match (beginandend #"Call to (.*)/(.*) did not conform to spec:(.*)In: (.*) val: (.*) fails spec: :clojure\.core\.specs\.alpha/bindings (.*) predicate: vector\?")
     :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Parameters for " (nth matches 2) :arg " require a vector, but " (nth matches 5) :arg " was given instead.\n"))}

    {:key :binding-requires-a-pair
     :class "ExceptionInfo"
     :match (beginandend #"Call to (.*)/(.*) did not conform to spec(.*):clojure\.core\.specs\.alpha/binding(.*)predicate: any\?,  Insufficient input")
     :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Parameters for " (nth matches 2) :arg
     " must be a pair, but only one element is given.\n"))}

     {:key :extra-input-for-a-binding
      :class "ExceptionInfo"
      :match (beginandend #"Call to (.*)/(.*) did not conform to spec(.*):clojure\.core\.specs\.alpha/binding (.*)Extra input")
      :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Parameters for " (nth matches 2) :arg
      " must be only one name and one value, but more parameters were given.\n"))}

   {:key :wrong-binding-name
    :class "ExceptionInfo"
    :match (beginandend #"Call to (.*)/(.*) did not conform to spec:(.*)In: (.*) val: (.*) fails spec: :clojure\.core\.specs\.alpha/local-name (.*) predicate: simple-symbol\?")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "In " (nth matches 2) :arg " "
    (nth matches 5) :arg " is used instead of a variable name.\n"))}

   {:key :wrong-binding-name-defn-args
    :class "ExceptionInfo"
    :match (beginandend #"Call to (.*)/(.*) did not conform to spec:(.*)In: (.*) val: (.*) fails spec: :clojure.core.specs.alpha/defn-args (.*) predicate: simple-symbol\?")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "In " (nth matches 2) :arg " "
    (nth matches 5) :arg " is used instead of a function name.\n"))}

   {:key :vector-expected-for-arg-list-second-var-arg
    :class "ExceptionInfo"
    :match (beginandend #"Call to (.*)/(.*) did not conform to spec:(.*)In: (.*) val: (.*) fails spec: :clojure\.core\.specs\.alpha/arg-list (.*) predicate: vector\?(.*):args \((.*) (.*) \(")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "An argument for " (nth matches 2) :arg " required a vector, but " (nth matches 9) :arg " was given instead.\n"))}

   {:key :vector-expected-for-arg-list
    :class "ExceptionInfo"
    :match (beginandend #"Call to (.*)/(.*) did not conform to spec:(.*)In: (.*) val: (.*) fails spec: :clojure\.core\.specs\.alpha/arg-list (.*) predicate: vector\?(.*):args \((.*) \(")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "An argument for " (nth matches 2) :arg " required a vector, but " (nth matches 8) :arg " was given instead.\n"))}\

   {:key :vector-expected-for-arg-list-general
    :class "ExceptionInfo"
    :match (beginandend #"Call to (.*)/(.*) did not conform to spec:(.*)In: (.*) val: (.*) fails spec: :clojure\.core\.specs\.alpha/arg-list (.*) predicate: vector\?")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "An argument for " (nth matches 2) :arg " required a vector, but no vector was passed.\n"))}\

   {:key :vector-expected-for-arg-list-general
    :class "ExceptionInfo"
    :match (beginandend #"Call to (.*)/(.*) did not conform to spec:(.*)In: (.*) val: (.*) fails spec: :clojure\.core\.specs\.alpha/arg-list (.*) predicate: vector\?")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "An argument for " (nth matches 2) :arg " required a vector, but no vector was passed.\n"))}\

  ;  {:key :insufficient-input
  ;   :class "ExceptionInfo"
  ;   :match (beginandend #"Call to (.*)/(.*) did not conform to spec:\n(.*) :reason Insufficient Input")
  ;   :make-msg-info-obj (fn [matches] (make-msg-info-hashes "There was insuffieint input for a function call\n"))}\





   {:key :length-not-greater-zero
    :class "ExceptionInfo"
    :match (beginandend "Call to \\#'(.*)/(.*) did not conform to spec:\\nval: (.*) fails spec: :(.*)/b-length-greater-zero")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (check-divide (nth matches 2)) :arg
                                                           " can only take one or more arguments; recieved " (number-vals (nth matches 3) "b-length-greater-zero") :arg
                                                           ".\n"))}

   {:key :length-not-greater-zero-macro
    :class "ExceptionInfo"
    :match (beginandend "Call to (.*)/(.*) did not conform to spec:\\nval: (.*) fails spec: :(.*)/b-length-greater-zero")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (check-divide (nth matches 2)) :arg
                                                           " can only take one or more arguments; recieved " (number-vals (nth matches 3) "b-length-greater-zero") :arg
                                                           ".\n"))}

   {:key :length-not-greater-one
    :class "ExceptionInfo"
    :match (beginandend "Call to \\#'(.*)/(.*) did not conform to spec:\\nval: (.*) fails spec: :(.*)/b-length-greater-one")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (nth matches 2) :arg
                                                           " can only take two or more arguments; recieved " (number-vals (nth matches 3) "b-length-greater-one") :arg
                                                           ".\n"))}

   {:key :length-not-greater-one-macro
    :class "ExceptionInfo"
    :match (beginandend "Call to (.*)/(.*) did not conform to spec:\\nval: (.*) fails spec: :(.*)/b-length-greater-one")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (nth matches 2) :arg
                                                           " can only take two or more arguments; recieved " (number-vals (nth matches 3) "b-length-greater-one") :arg
                                                           ".\n"))}

   {:key :length-not-greater-two
    :class "ExceptionInfo"
    :match (beginandend "Call to \\#'(.*)/(.*) did not conform to spec:\\nval: (.*) fails spec: :(.*)/b-length-greater-two")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (nth matches 2) :arg
                                                           " can only take three or more arguments; recieved " (number-vals (nth matches 3) "b-length-greater-two") :arg
                                                           ".\n"))}

   {:key :length-not-greater-two-macro
    :class "ExceptionInfo"
    :match (beginandend "Call to (.*)/(.*) did not conform to spec:\\nval: (.*) fails spec: :(.*)/b-length-greater-two")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (nth matches 2) :arg
                                                           " can only take three or more arguments; recieved " (number-vals (nth matches 3) "b-length-greater-two") :arg
                                                           ".\n"))}

   {:key :length-not-zero-or-one
    :class "ExceptionInfo"
    :match (beginandend "Call to \\#'(.*)/(.*) did not conform to spec:\\nval: (.*) fails spec: :(.*)/b-length-zero-to-one")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (nth matches 2) :arg
                                                           " can only take zero or one arguments; recieved " (number-vals (nth matches 3) "b-length-zero-or-one") :arg
                                                           ".\n"))}

   {:key :length-not-zero-or-one-macro
    :class "ExceptionInfo"
    :match (beginandend "Call to (.*)/(.*) did not conform to spec:\\nval: (.*) fails spec: :(.*)/b-length-zero-to-one")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (nth matches 2) :arg
                                                           " can only take zero or one arguments; recieved " (number-vals (nth matches 3) "b-length-zero-or-one") :arg
                                                           ".\n"))}

   {:key :length-not-two-or-three
    :class "ExceptionInfo"
    :match (beginandend "Call to \\#'(.*)/(.*) did not conform to spec:\\nval: (.*) fails spec: :(.*)/b-length-two-to-three")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (nth matches 2) :arg
                                                           " can only take two or three arguments; recieved " (number-vals (nth matches 3) "b-length-two-or-three") :arg
                                                           ".\n"))}

   {:key :length-not-two-or-three-macro
    :class "ExceptionInfo"
    :match (beginandend "Call to (.*)/(.*) did not conform to spec:\\nval: (.*) fails spec: :(.*)/b-length-two-to-three")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (nth matches 2) :arg
                                                           " can only take two or three arguments; recieved " (number-vals (nth matches 3) "b-length-two-or-three") :arg
                                                           ".\n"))}

   {:key :length-not-zero-to-three
    :class "ExceptionInfo"
    :match (beginandend "Call to \\#'(.*)/(.*) did not conform to spec:\\nval: (.*) fails spec: :(.*)/b-length-zero-to-three")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (nth matches 2) :arg
                                                           " can only take zero to three arguments; recieved " (number-vals (nth matches 3) "b-length-zero-to-three") :arg
                                                           ".\n"))}

   {:key :length-not-zero-to-three-macro
    :class "ExceptionInfo"
    :match (beginandend "Call to (.*)/(.*) did not conform to spec:\\nval: (.*) fails spec: :(.*)/b-length-zero-to-three")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (nth matches 2) :arg
                                                           " can only take zero to three arguments; recieved " (number-vals (nth matches 3) "b-length-zero-to-three") :arg
                                                           ".\n"))}

   {:key :length-not-one
    :class "ExceptionInfo"
    :match (beginandend "Call to \\#'(.*)/(.*) did not conform to spec:\\nval: (.*) fails spec: :(.*)/b-length-one")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (nth matches 2) :arg
                                                           " can only take one argument; recieved " (number-vals (nth matches 3) "b-length-one") :arg
                                                           ".\n"))}

   {:key :length-not-one-macro
    :class "ExceptionInfo"
    :match (beginandend "Call to (.*)/(.*) did not conform to spec:\\nval: (.*) fails spec: :(.*)/b-length-one")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (nth matches 2) :arg
                                                           " can only take one argument; recieved " (number-vals (nth matches 3) "b-length-one") :arg
                                                           ".\n"))}

   {:key :length-not-two
    :class "ExceptionInfo"
    :match (beginandend "Call to \\#'(.*)/(.*) did not conform to spec:\\nval: (.*) fails spec: :(.*)/b-length-two")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (nth matches 2) :arg
                                                           " can only take two arguments; recieved " (number-vals (nth matches 3) "b-length-two") :arg
                                                           ".\n"))}

   {:key :length-not-two-macro
    :class "ExceptionInfo"
    :match (beginandend "Call to (.*)/(.*) did not conform to spec:\\nval: (.*) fails spec: :(.*)/b-length-two")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (nth matches 2) :arg
                                                           " can only take two arguments; recieved " (number-vals (nth matches 3) "b-length-two") :arg
                                                           ".\n"))}

   {:key :length-not-three
    :class "ExceptionInfo"
    :match (beginandend "Call to \\#'(.*)/(.*) did not conform to spec:\\nval: (.*) fails spec: :(.*)/b-length-three")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (nth matches 2) :arg
                                                           " can only take three arguments; recieved " (number-vals (nth matches 3) "b-length-three") :arg
                                                           ".\n"))}

   {:key :length-not-three-macro
    :class "ExceptionInfo"
    :match (beginandend "Call to (.*)/(.*) did not conform to spec:\\nval: (.*) fails spec: :(.*)/b-length-three")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (nth matches 2) :arg
                                                           " can only take three arguments; recieved " (number-vals (nth matches 3) "b-length-three") :arg
                                                           ".\n"))}

   {:key :0-not-valid
    :class "ExceptionInfo"
    :match (beginandend "Call to \\#'(.*)/(.*) did not conform to spec:\\nIn: \\[(\\d*)\\] val: (.*) fails spec: :(.*)/b-not-zero") ;at: \\[:args :(\\S*)\\](.*)(\\n(.*)(\\n)?)*")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "In function " (check-divide (nth matches 2)) :arg
                                                           ", the " (arg-str (nth matches 3)) :arg
                                                           " cannot be 0.\n"))}

   #_{:key :count-less-than-int
    :class "ExceptionInfo"
    :match (beginandend "Call to \\#'(.*)/(.*) did not conform to spec:\\nval:"); (.*) fails at: [(.*)] predicate: (.*), (.*) input"); (.*) fails at: [(.*)] predicate: (.*) \\(b-not-greater-count (.*)\\)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "In function " (check-divide (nth matches 2)) :arg
                                                           ", the " (arg-str (nth matches 3)) :arg
                                                           " cannot be 0.\n"))}
   {:key :exception-info-extra-input
    :class "ExceptionInfo"
    ;; Need to extract the function name from "Call to #'spec-ex.spec-inte/+ did not conform to spec"
    ;:match #"(.*)/(.*) did not conform to spec(.*)" ; the data is in the data object, not in the message
    :match (beginandend "Call to \\#'(.*)/(.*) did not conform to spec:\\nIn: \\[(\\d*)\\] val: (\\S*) fails at: \\[:args (:\\S*\\s)*\\:(\\S*)\\] predicate: \\(cat (.*) (.*)\\),  Extra input")
    ;:match #"(.*)(\n(.*))*(\n)?"
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (nth matches 2) :arg
                                                           " cannot take as many arguments as are currently in it, needs fewer arguments.\n"))}

   {:key :exception-info-insufficient-input
    :class "ExceptionInfo"
    ;; Need to extract the function name from "Call to #'spec-ex.spec-inte/+ did not conform to spec"
    ;:match #"(.*)/(.*) did not conform to spec(.*)" ; the data is in the data object, not in the message
    :match (beginandend "Call to \\#'(.*)/(.*) did not conform to spec:\\nval: (.*) fails at: \\[:args (:\\S*\\s)*\\:(\\S*)\\] predicate: (\\S*),  Insufficient input")
    ;:match #"(.*)(\n(.*))*(\n)?"
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes (nth matches 2) :arg
                                                           " cannot take as few arguments as are currently in it, needs more arguments.\n"))}

   {:key :exception-info
    :class "ExceptionInfo"
    ;; Need to extract the function name from "Call to #'spec-ex.spec-inte/+ did not conform to spec"
    ;:match #"(.*)/(.*) did not conform to spec(.*)" ; the data is in the data object, not in the message
    :match (beginandend "Call to \\#'(.*)/(.*) did not conform to spec:\\nIn: \\[(\\d*)\\] val: (\\S*) fails at: \\[:args :(\\S*)\\] predicate: (\\S*)(\\s+)")
    ;:match #"(.*)(\n(.*))*(\n)?"
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "In function " (nth matches 2) :arg
                                                           ", the " (arg-str (nth matches 3)) :arg
                                                           " is expected to be a "  (?-name (nth matches 6)) :type
                                                           ", but is " (get-dictionary-type (nth matches 4)) :type
                                                           (nth matches 4) :arg
                                                           " instead.\n"))}
    ;:make-msg-info-obj (fn [matches] (str "In function " (nth matches 0)))}

    {:key :exception-info-or-after-in-0
      :class "ExceptionInfo"
      :match (beginandend "Call to \\#'(.*)/(.*) did not conform to spec:\\nIn: \\[(\\d*)\\] val: (\\S*) fails at: \\[:args (:\\S*\\s)*\\:(\\S*)\\] predicate: (\\S*)(\\s+)(\\n(.*)(\\n)?)*In: \\[(\\d*)\\]")
      :make-msg-info-obj (fn [matches] (make-msg-info-hashes "In function " (nth matches 2) :arg
                                                              ", the " (arg-str (nth matches 3)) :arg
                                                              " is expected to be a "  (?-name (nth matches 7)) :type
                                                              ", but is " (get-dictionary-type (nth matches 4)) :type
                                                              (nth matches 4) :arg
                                                              " instead.\n"))}

    {:key :exception-info-functions
      :class "ExceptionInfo"
      :match (beginandend "Call to \\#'(.*)/(.*) did not conform to spec:\\nIn: \\[(\\d*)\\] val: \\#(\\S*)\\[(\\S*) (\\S*) (\\S*)\\] fails at: \\[:args (:\\S*\\s)*\\:(\\S*)\\] predicate: (\\S*)(\\s+)(\\n(.*)(\\n)?)*In: \\[(\\d*)\\]")
      :make-msg-info-obj
      (fn [matches]
        (let [[print-type print-val] (type-and-val (nth matches 5))]
          (make-msg-info-hashes "In function " (nth matches 2) :arg
                                                              ", the " (arg-str (nth matches 3)) :arg
                                                              " is expected to be a "  (?-name (nth matches 10)) :type
                                                              ", but is " print-type :type
                                                              print-val :arg
                                                              " instead.\n")))}

    {:key :exception-info-or-one-line-functions
      :class "ExceptionInfo"
      :match (beginandend "Call to \\#'(.*)/(.*) did not conform to spec:\\nIn: \\[(\\d*)\\] val: \\#(\\S*)\\[(\\S*) (\\S*) (\\S*)\\] fails at: \\[:args (:\\S*\\s)*\\:(\\S*)\\] predicate: (\\S*)(\\s+)  (.*)/(.*)")
      :make-msg-info-obj
      (fn [matches]
        (let [[print-type print-val] (type-and-val (nth matches 5))]
           (make-msg-info-hashes "In function " (nth matches 2) :arg
                                                              ", the " (arg-str (nth matches 3)) :arg
                                                              " is expected to be a "  (?-name (nth matches 10)) :type
                                                              ", but is " print-type :type
                                                              print-val :arg
                                                              " instead.\n")))}

    {:key :exception-info-or
     :class "ExceptionInfo"
     :match (beginandend "Call to \\#'(.*)/(.*) did not conform to spec:\\nIn: \\[(\\d*)\\] val: \\#(\\S*)\\[(.*)\\] fails at: \\[:args (:\\S*\\s)*\\:(\\S*)\\] predicate: (\\S*)\\n(\\n(.*)(\\n)?)*In: \\[0\\]")
     :make-msg-info-obj (fn [matches]
        (let [[print-type print-val] (type-and-val (nth matches 4))]
          (make-msg-info-hashes "In function " (nth matches 2) :arg
                                                            ", the " (arg-str (nth matches 3)) :arg
                                                            " is expected to be a "  (?-name (nth matches 7)) :type
                                                            ", but is " print-type :type
                                                            print-val :arg
                                                            " instead.\n")))}

    {:key :exception-require-innervector
      :class "ExceptionInfo"
      :match (beginandend "Call to \\#'(.*)/(.*) did not conform to spec:\\nIn: \\[(\\d*\\s*)*\\] val: (.*) fails spec: (\\S*)/innervector (.*) Insufficient input")
      :make-msg-info-obj (fn [matches]
        (let [[print-type print-val] (type-and-val (nth matches 4))]
           (make-msg-info-hashes "In function " (nth matches 2) :arg
                                 ", args must come in key value pairs.\n")))}

    {:key :exception-require-extrainput
      :class "ExceptionInfo"
      :match (beginandend "Call to \\#'(.*)/(.*) did not conform to spec:\\nIn: \\[(\\d*\\s*)*\\] val: (.*) fails spec: (\\S*)/(\\S*) (.*) Extra input")
      :make-msg-info-obj (fn [matches]
        (let [[print-type print-val] (type-and-val (nth matches 4))]
           (make-msg-info-hashes "In function " (nth matches 2) :arg
                                 ", the list/vector may only be followed by a keyword.\n")))}

    {:key :exception-info-require-single-arg
      :class "ExceptionInfo"
      :match (beginandend "Call to \\#'(.*)/(.*) did not conform to spec:\\nIn: \\[(\\d*)\\] val: (\\S*) fails spec: :(\\S*)/(\\S*) at: \\[:args (:\\S*\\s)*\\:(\\S*)\\] predicate: (\\S*)(\\s+)(In: \\[(\\d*)\\](.*))+(.*)")
      :make-msg-info-obj (fn [matches]
        (let [[print-type print-val] (type-and-val (nth matches 4))]
           (make-msg-info-hashes "In function " (nth matches 2) :arg
                                                           ", the " (arg-str (nth matches 3)) :arg
                                                           " is expected to be a "  (?-name (nth matches 9)) :type
                                                           ", but is " print-type :type
                                                           print-val :arg
                                                           " instead.\n")))}

    {:key :exception-info-general-seq
      :class "ExceptionInfo"
      :match (beginandend "Call to \\#'(.*)/(.*) did not conform to spec:\\nIn: \\[(\\d*\\s*)*(\\d*)\\](.*)In: \\[(\\d*)\\] val: (.*) fails spec: :(\\S*)/(\\S*) at: \\[:args (:\\S*\\s)*\\:(\\S*)\\] predicate: (\\S*)(\\s+)In: \\[(\\d*)\\]")
      :make-msg-info-obj (fn [matches]
        (let [[print-type print-val] (type-and-val (nth matches 7))]
           (make-msg-info-hashes "In function " (nth matches 2) :arg
                                                           ", the " (arg-str (subs (nth matches 6) 0 1)) :arg
                                                           " is expected to be a "  (?-name (nth matches 12)) :type
                                                           ", but is " print-type :type
                                                           print-val :arg
                                                           " instead.\n")))}

    {:key :exception-info-require-seq
      :class "ExceptionInfo"
      :match (beginandend "Call to \\#'(.*)/(.*) did not conform to spec:\\nIn: \\[(\\d*\\s*)*(\\d*)\\](.*)In: \\[(\\d*)\\] val: (.*) fails spec: :(\\S*)/(\\S*) at: \\[:args (:\\S*\\s)*\\:(\\S*)\\] predicate: (\\S*)(.*)")
      :make-msg-info-obj (fn [matches]
        (let [[print-type print-val] (type-and-val (nth matches 7))]
           (make-msg-info-hashes "In function " (nth matches 2) :arg
                                                           ", the " (arg-str (subs (nth matches 6) 0 1)) :arg
                                                           " is expected to be a "  (?-name (nth matches 12)) :type
                                                           ", but is " print-type :type
                                                           print-val :arg
                                                           " instead.\n")))}

    {:key :exception-info-or-one-line
      :class "ExceptionInfo"
      :match (beginandend "Call to \\#'(.*)/(.*) did not conform to spec:\\nIn: \\[(\\d*)\\] val: (.*) fails at: \\[:args (:\\S*\\s)*\\:(\\S*)\\] predicate: (\\S*)(\\s+)  (.*)/(.*)")
      :make-msg-info-obj (fn [matches]
        (let [[print-type print-val] (type-and-val (nth matches 4))]
           (make-msg-info-hashes "In function " (nth matches 2) :arg
                                                           ", the " (arg-str (nth matches 3)) :arg
                                                           " is expected to be a "  (?-name (nth matches 7)) :type
                                                           ", but is " print-type :type
                                                           print-val :arg
                                                           " instead.\n")))}

    {:key :exception-info-extra-input-spec
     :class "ExceptionInfo"
     ;; Need to extract the function name from "Call to #'spec-ex.spec-inte/+ did not conform to spec"
     ;:match #"(.*)/(.*) did not conform to spec(.*)" ; the data is in the data object, not in the message
     :match (beginandend "Call to \\#'(.*)/(.*) did not conform to spec:\\nIn: \\[(\\d*)\\] val: (\\S*) fails spec: (\\S*) at: \\[:args (:\\S*\\s)*\\:(\\S*)\\] predicate: \\(cat (.*) (.*)\\),  Extra input")
     ;:match #"(.*)(\n(.*))*(\n)?"
     :make-msg-info-obj (fn [matches] (make-msg-info-hashes (nth matches 2) :arg
                                                            " cannot take as many arguments as are currently in it, needs fewer arguments.\n"))}

    {:key :exception-info-insufficient-input-spec
     :class "ExceptionInfo"
     ;; Need to extract the function name from "Call to #'spec-ex.spec-inte/+ did not conform to spec"
     ;:match #"(.*)/(.*) did not conform to spec(.*)" ; the data is in the data object, not in the message
     :match (beginandend "Call to \\#'(.*)/(.*) did not conform to spec:\\nval: (.*) fails spec: (\\S*) at: \\[:args (:\\S*\\s)*\\:(\\S*)\\] predicate: (\\S*),  Insufficient input")
     ;:match #"(.*)(\n(.*))*(\n)?"
     :make-msg-info-obj (fn [matches] (make-msg-info-hashes (nth matches 2) :arg
                                                            " cannot take as few arguments as are currently in it, needs more arguments.\n"))}

    {:key :exception-info-extra-input-spec-no-spec
     :class "ExceptionInfo"
     ;; Need to extract the function name from "Call to #'spec-ex.spec-inte/+ did not conform to spec"
     ;:match #"(.*)/(.*) did not conform to spec(.*)" ; the data is in the data object, not in the message
     :match (beginandend "Call to \\#'(.*)/(.*) did not conform to spec:\\nIn: \\[(\\d*)\\] val: (\\S*) fails at: \\[:args (:\\S*\\s)*\\:(\\S*)\\] predicate: \\((.*)\\),  Extra input")
     ;:match #"(.*)(\n(.*))*(\n)?"
     :make-msg-info-obj (fn [matches] (make-msg-info-hashes (nth matches 2) :arg
                                                            " cannot take as many arguments as are currently in it, needs fewer arguments.\n"))}

    {:key :exception-info-insufficient-input-spec-no-spec
     :class "ExceptionInfo"
     ;; Need to extract the function name from "Call to #'spec-ex.spec-inte/+ did not conform to spec"
     ;:match #"(.*)/(.*) did not conform to spec(.*)" ; the data is in the data object, not in the message
     :match (beginandend "Call to \\#'(.*)/(.*) did not conform to spec:\\nval: (.*) fails at: \\[:args (:\\S*\\s)*\\:(\\S*)\\] predicate: (.*),  Insufficient input")
     ;:match #"(.*)(\n(.*))*(\n)?"
     :make-msg-info-obj (fn [matches] (make-msg-info-hashes (nth matches 2) :arg
                                                            " cannot take as few arguments as are currently in it, needs more arguments.\n"))}

    {:key :exception-info-insufficient-input-spec-map
     :class "ExceptionInfo"
     ;; Need to extract the function name from "Call to #'spec-ex.spec-inte/+ did not conform to spec"
     ;:match #"(.*)/(.*) did not conform to spec(.*)" ; the data is in the data object, not in the message
     :match (beginandend "Call to \\#'(.*)/(.*) did not conform to spec:\\nval: (.*) fails spec: (.*) at: \\[:args (:\\S*\\s)*\\:(\\S*)\\] predicate: (.*),  Insufficient input")
     ;:match #"(.*)(\n(.*))*(\n)?"
     :make-msg-info-obj (fn [matches] (make-msg-info-hashes (nth matches 2) :arg
                                                            " cannot take as few arguments as are currently in it, needs more arguments. This is most likely do to a map missing arguments.\n"))}

   {:key :exception-info-other-spec-functions
     :class "ExceptionInfo"
     :match (beginandend "Call to \\#'(.*)/(.*) did not conform to spec:\\nIn: \\[(.*)\\] val: (.*) fails spec: (.*) at: \\[:args (:\\S*\\s)*\\:(\\S*)\\] predicate: (.*)(\\s+)  (.*)/(.*)")
     :make-msg-info-obj (fn [matches]
       (let [[print-type print-val] (type-and-val (nth matches 4))]
          (make-msg-info-hashes "In function " (nth matches 2) :arg
                                                          ", the " (arg-str (subs (nth matches 3) 0 1)) :arg
                                                          " is expected to be a "  (?-name (nth matches 7)) :type
                                                          ", but is " print-type :type
                                                          print-val :arg
                                                          " instead.\n")))}



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

    ;; This might go away now
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

    {:key :loop-req-vector
    :class "IllegalArgumentException"
    :match (beginandend "loop requires a vector for its binding (\\S*)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Loop requires a vector for its binding.\n"))}

    {:key :recur-arg-mismatch
    :class "IllegalArgumentException"
    :match (beginandend #"Mismatched argument count to recur, expected: (.*) args, got: (.*)\,")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Mismatch between the number of arguments of outside function and recur: recur must take " (number-arg (nth matches 1)) " but was given " (number-arg (nth matches 2)) :arg ".\n"))}

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


  ;;(beginandend #"Call to (.*)/(.*) did not conform to spec:(.*)In: (.*) val: (.*) fails spec: :clojure\.core\.specs\.alpha/local-name (.*) predicate: simple-symbol\?")
   ;########################
   ;### Assertion Errors ###
   ;########################

   #_{:key :assertion-error-with-argument
    ;need to test
    :class "AssertionError"
    :match (beginandend "Assert failed: \\((\\S*) argument(\\S*)\\)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Cannot assert on " (nth matches 2) ".\n"))} ; process-asserts-obj from dictionaries.clj

   {:key :assertion-error-without-argument
    :class "AssertionError"
    :match (beginandend "Assert failed: (\\S*)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Cannot assert on " (nth matches 1) :arg ".\n"))}


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
                                                           " does not allow " (get-type (nth matches 2)) :type " as an argument.\n"))}

    {:key :compiler-exception-must-recur-from-tail-position
    :class "UnsupportedOperationException"
    :match (beginandend "Can only recur from tail position")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Recur can only occur as a tail call: no operations can be done after its return.\n"))}

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
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Invalid number: " (nth matches 1) :arg ".\n"))}

    ;#####################################################################
    ;### Runtime Exceptions or clojure.lang.LispReader$ReaderException ###
    ;#####################################################################

    {:key :reader-tag-must-be-symbol
    :class "RuntimeException"
    :match (beginandend "Reader tag must be a symbol")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "# must be followed by a symbol.\n"))}

    #_{:key :invalid-tolken-error
    ;need to test
    :class "RuntimeException"
    :match (beginandend "java.lang.RuntimeException: Invalid token: (\\S*)")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "You cannot use " (nth matches 1) :arg " in this position.\n"))}

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
   :match (beginandend "Can't take value of a macro: (\\S*),")
   :make-msg-info-obj (fn [matches] (make-msg-info-hashes (get-macro-name (nth matches 1)) :arg " is a macro and cannot be used by itself or passed to a function.\n"))}

   #_{:key :compiler-exception-cannot-resolve-symbol
    :class "RuntimeException"
    :match (beginandend "Unable to resolve symbol: (\\S*) in this context")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Name " (nth matches 1) :arg " is undefined.\n"))}

    {:key :compiler-exception-map-literal-even
    :class "RuntimeException"
    :match (beginandend "Map literal must contain an even number of forms")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "A hash map must consist of key/value pairs; you have a key that's missing a value.\n"))}

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
    :match (beginandend "Too many arguments to (\\S*),")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Too many arguments to " (nth matches 1) :arg ".\n"))}

   {:key :compiler-exception-too-few-arguments
    :class "RuntimeException"
    :match (beginandend "Too few arguments to (\\S*),")
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
    :match (beginandend "Java heap space ")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Clojure ran out of memory, likely due to an infinite computation.\n"))}

    {:key :stack-overflow-with-name
    :class "StackOverflowError"
    :match (beginandend "\\s*(\\S+)\\s+")
    :make-msg-info-obj (fn [matches] (make-msg-info-hashes "Clojure ran out of memory, likely due to an infinite computation or infinite recursion.\n"
    " Detected in function " (get-function-name (nth matches 1)) :arg ".\n"))}

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
