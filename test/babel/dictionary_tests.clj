(ns babel.dictionary_tests
  (:require
    [errors.dictionaries :refer :all]
    [expectations :refer :all]))

;#########################################
;### Tests for supplementary functions ###
;### in errors.dictionaries            ###
;#########################################

(expect "inc" (remove-inliner "inc--inliner--5258"))
(expect "inc" (remove-inliner "inc--inliner"))
(expect "inc" (remove-inliner "inc--5258"))
(expect "inc5258" (remove-inliner "inc5258"))
(expect "inc-5258" (remove-inliner "inc-5258"))

(expect "days" (get-function-name "happy$days"))
(expect "days" (get-function-name "happy/days"))
(expect "first" (get-function-name "clojure.lang.RT.first"))
(expect "first" (get-function-name "clojure.lang.RT.first--5678"))
(expect "somethingElse" (get-function-name "somethingElse"))
(expect "best-approximation" (get-function-name "errors.dictionaries/best-approximation"))
(expect "anonymous function" (get-function-name "fn_test"))

(expect "anonymous function" (check-if-anonymous-function "fn"))
(expect "anonymous function" (check-if-anonymous-function "fn_test"))
(expect "anonymous function" (check-if-anonymous-function "fn_"))
(expect "random_function" (check-if-anonymous-function "random_function"))

(expect "a function" (best-approximation "clojure.spec.test.alpha$spec_checking_fn$fn__2943"))
(expect "unrecognized type 3" (best-approximation "3"))
(expect "unrecognized type \"a\"" (best-approximation "\"a\""))
(expect "a function" (best-approximation "clojure.core/map"))
(expect "a function" (best-approximation "errors.dictionaries/best-approximation"))

(expect "a string" (get-type "java.lang.String"))
(expect "a number" (get-type "java.lang.Long"))
(expect "a symbol" (get-type "clojure.lang.Symbol"))

(expect "a string " (get-dictionary-type "\"a\""))
(expect "a symbol " (get-dictionary-type "a"))
(expect "a number " (get-dictionary-type "3"))
(expect "a function " (get-dictionary-type "clojure.spec.test.alpha$spec_checking_fn$fn__2943"))
(expect "a regular expression pattern " (get-dictionary-type "#\" a \""))
(expect "a sequence " (get-dictionary-type "'()"))
(expect "a list " (get-dictionary-type "()"))
(expect "a vector " (get-dictionary-type "[3]"))
(expect "a vector " (get-dictionary-type "[3 \"a\" 5]"))
(expect "a vector " (get-dictionary-type "[3 \"a\" [5 [3]]]"))
(expect "a map " (get-dictionary-type "{:a 3}"))
(expect "a map " (get-dictionary-type "{:a 3 :b 6 :c 12 :d 24 :e 48}"))
(expect "a set " (get-dictionary-type "#{:a 3}"))
(expect "" (get-dictionary-type "nil"))
(expect "a character " (get-dictionary-type "\\a"))
(expect "a keyword " (get-dictionary-type ":a"))
(expect "a boolean " (get-dictionary-type "true"))

(expect "a?" (lookup-funct-name "a_QMARK_"))
(expect "a!" (lookup-funct-name "a_BANG_"))
(expect "a=" (lookup-funct-name "a_EQ_"))
(expect "a<" (lookup-funct-name "a_LT_"))
(expect "a>" (lookup-funct-name "a_GT_"))
(expect "a*" (lookup-funct-name "a_STAR_"))
(expect "+" (lookup-funct-name "_PLUS_"))
(expect "-" (lookup-funct-name "_"))
(expect "/" (lookup-funct-name "_SLASH_"))
(expect "a" (lookup-funct-name "a"))

(expect "first argument" (arg-str "0"))
(expect "second argument" (arg-str "1"))
(expect "third argument" (arg-str "2"))
(expect "fourth argument" (arg-str "3"))
(expect "fifth argument" (arg-str "4"))
(expect "6th argument" (arg-str "5"))
(expect "7th argument" (arg-str "6"))

(expect "zero" (number-word "0"))
(expect "10" (number-word "10"))

(expect "no arguments" (number-vals "nil" "b-length-one"))
(expect "two arguments" (number-vals "[1 2]" "b-length-one"))
(expect "no arguments" (number-vals "nil" "b-length-two"))
(expect "one argument" (number-vals "[1]" "b-length-two"))
(expect "three arguments" (number-vals "[1 2 3]" "b-length-two"))
(expect "no arguments" (number-vals "nil" "b-length-three"))
(expect "one argument" (number-vals "[1]" "b-length-three"))
(expect "two arguments" (number-vals "[1 2]" "b-length-three"))
(expect "four arguments" (number-vals "[1 2 3 4]" "b-length-three"))
(expect "no arguments" (number-vals "nil" "b-length-greater-zero"))
(expect "no arguments" (number-vals "nil" "b-length-greater-one"))
(expect "one argument" (number-vals "[1]" "b-length-greater-one"))
(expect "no arguments" (number-vals "nil" "b-length-greater-two"))
(expect "one argument" (number-vals "[1]" "b-length-greater-two"))
(expect "two arguments" (number-vals "[1 2]" "b-length-greater-two"))
(expect "two arguments" (number-vals "[1 2]" "b-length-zero-or-one"))
(expect "no arguments" (number-vals "nil" "b-length-two-or-three"))
(expect "one argument" (number-vals "[1]" "b-length-two-or-three"))
(expect "four arguments" (number-vals "[1 2 3 4]" "b-length-two-or-three"))
(expect "b-length-eight" (number-vals "[1 2 3 4]" "b-length-eight"))
(expect "no arguments" (number-vals "nil" "b-length-eight"))

(expect "function" (?-name "object\r"))
(expect "function" (?-name "ifn?\r"))
(expect "collection" (?-name "coll?"))
(expect "a" (?-name "a\r"))
(expect "a" (?-name "a?"))
(expect "a" (?-name "a?\r"))

(expect "a" (check-divide "a"))
(expect "/" (check-divide ""))

;;; Return spec error string Tests (These will need to be passed in functions without
;;; the error message filter, this is simply a proof of concept at the moment wiht excerpts from the spec error being used)

;;;; (import 3)
(expect "3"
        (get-spec-text " :spec #object[clojure.spec.alpha$regex_spec_impl$reify__2436 0x278fa0f8 'clojure.spec.alpha$regex_spec_impl$reify__2436@278fa0f8'],  :value (3), :args (3)}, compiling:(/tmp/"))
;;;; (when-first [a [3] b [4]] 3)
(expect "[a [3] b [4]] 3"
        (get-spec-text "], :value ([a [3] b [4]] 3), :args ([a [3] b [4]] 3)}, compiling:(/tmp/form-init1299280992515962485.clj:1:1)"))
;;;; user=> (map (fn (* 4 VARIABLE-NAME)) (range 1 10))
(expect "(* 4 VARIABLE-NAME)"
        (get-spec-text "], :value ((* 4 VARIABLE-NAME)), :args ((* 4 VARIABLE-NAME))}, compiling:(/tmp/form-init231752514381864372.clj:1:6)"))

;;;; user=> (defn hello [x] (let [y 2 z] (+ x y)))
(expect "[y 2 z] (+ x y)"
        (get-spec-text "Default Error: Call to clojure.core/let did not conform to spec:\nIn: [0] val: () fails spec: :
        clojure.core.specs.alpha/bindings at: [:args :bindings :init-expr] predicate: any?,  Insufficient input\n #:clojure.spec.
        alpha{:problems [{:path [:args :bindings :init-expr], :reason \"Insufficient input\", :pred clojure.core/any?, :val (), :via
        [:clojure.core.specs.alpha/bindings :clojure.core.specs.alpha/bindings], :in [0]}], :spec #object[clojure.spec.alpha$regex_spec_impl$reify__2436
        0x2377a56c \"clojure.spec.alpha$regex_spec_impl$reify__2436@2377a56c\"], :value ([y 2 z] (+ x y)), :args ([y 2 z] (+ x y))}, compiling:(/tmp/form-init8683
          024422163155580.clj:1:17) \n\n"))

(expect ["" "an anonymous function"] (type-and-val "clojure.spec.test.alpha$spec_checking_fn$fn__2943"))
(expect ["a function " "best-approximation"] (type-and-val "errors.dictionaries/best-approximation"))
(expect ["a vector " "[1 2 3]"] (type-and-val "[1 2 3]"))
(expect ["a string " "\"hello\""] (type-and-val "\"hello\""))
