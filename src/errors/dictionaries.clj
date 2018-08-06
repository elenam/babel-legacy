(ns errors.dictionaries
  (:use [errors.messageobj]))
(require '[clojure.string :as cs])

;;; A dictionary of known types and their user-friendly representations.
;;; Potentially, we can have multiple dictionaries depending on the level.


(def type-dictionary {:java.lang.String "a string"
                      :java.lang.CharSequence "a string"
                      :java.lang.Number "a number"
                      :clojure.lang.Keyword "a keyword"
                      :java.lang.Boolean "a boolean"
		                  ;; I think this is better for new students to lump all numbers together
                      :java.lang.Long "a number"
                      :java.lang.Integer "a number"
                      :java.lang.Double "a number"
                      :java.lang.Float "a number"
                      :java.lang.Short  "a number"
                      :clojure.lang.BigInt "a number"
		                  ;; perhaps add big ints and such
                      :java.lang.Character "a character" ;; switched back from a symbol
		                  ;; to short-cut processing of error messages for
		                  ;; "Don't know how to create a sequence from ..."
                      :clojure.lang.ISeq "a sequence"
                      :ISeq "a sequence"
		                  ;; Refs come up in turtle graphics
                      :clojure.lang.Ref "a mutable object"
		                  ;; regular expressions wouldn't make sense to beginners,
		                  ;; but it's better to recognize their types for easier
		                  ;; help with diagnostics
                      :java.util.regex.Pattern "a regular expression pattern"
                      :java.util.regex.Matcher "a regular expression matcher"
		                  ;; also not something beginners would know,
		                  ;; but useful for understanding errors
                      :clojure.lang.Symbol "a symbol"
                      :clojure.lang.IPersistentStack "an object that behaves as a stack (such as a vector or a list)"
                      :clojure.lang.PersistentArrayMap "a map"
		                  ;; assoc works on maps and vectors:
                      :clojure.lang.Associative "a map or a vector"
                      :clojure.lang.Reversible "a vector or a sorted-map"
                      :clojure.lang.Sorted "a collection stored in a sorted manner (such as sorted-map or sorted-set)"
                      :clojure.lang.Sequential "a sequential collection (such as a vector or a list)"
		                  ;; This is here because of shuffle. It's not ideal, too similar to Sequential
                      :java.util.Collection " a traversable collection (such as a vector, list, or set)" ; not sure if this makes sense in relation to the previous one
		                  ;; got this in a seesaw error message. Not sure what other types are "Named"
		                  ;; source: https://groups.google.com/forum/?fromgroups#!topic/clojure/rd-MDXvn3q8
                      :clojure.lang.Named "a keyword or a symbol"
                      :clojure.lang.nil "nil"
                      :java.io.BufferedReader "a file or an input stream"})

;; matching type interfaces to beginner-friendly names.
;; Note: since a type may implement more than one interface,
;; the order is essential. The lookup is done in order, so
;; the first match is returned.
;; That's why it's a vector, not a hashmap.
;; USE CAUTION WHEN ADDING NEW TYPES!

(def general-types [[Number "a number"]
                    [clojure.lang.IPersistentVector "a vector"]
                    [clojure.lang.IPersistentList "a list"]
                    [clojure.lang.IPersistentSet "a set"]
                    [clojure.lang.IPersistentMap "a map"]
                    [clojure.lang.ISeq "a sequence"]
		                ;; collections - must go before functions since some collections
		                ;; implement the IFn interface
                    [clojure.lang.IPersistentCollection "a collection"]
                    [clojure.lang.IFn "a function"]])

;; The best approximation of a type t not listed in the type-dictionary (as a string)
;;; best-approximation: type -> string
(defn best-approximation [t]
  "returns a string representation of a type t not listed in the type-dictionary for user-friendly error messages"
  (let [first-attempt (resolve (symbol t))
        attempt (if (= (type first-attempt) clojure.lang.Var) (type (var-get first-attempt)) first-attempt)
        type1 (or attempt (clojure.lang.RT/loadClassForName (str "clojure.lang." t))) ;; may need to add clojure.lang. for some types.
        matched-type (if type1 (first (filter #(isa? type1 (first %)) general-types)))]
    (if matched-type (second matched-type) (str "unrecognized type " t))))

;;; get-type: type -> string
(defn get-type
  "returns a user-friendly representation of a type if it exists in the type-dictionary,
	or its default representation as an unknown type"
  [t]
  ((keyword t) type-dictionary (best-approximation t)))

(defn get-type-with-nil
  "Takes a value that can be nil and returns its type in a user-readable form"
  [v]
  (if (nil? v) "nil" (get-type (.getName (type v)))))

;; hashmap of internal function names and their user-friendly versions
(def predefined-names {:_PLUS_ "+"  :_ "-" :_SLASH_ "/"})

;;; lookup-funct-name: predefined function name -> string
(defn lookup-funct-name
  "looks up pre-defined function names, such as _PLUS_. If not found,
	returns the original"
  [fname]
  (let [lookup ((keyword fname) predefined-names)]
    (if lookup lookup (-> fname
                          (clojure.string/replace #"_QMARK_" "?")
                          (clojure.string/replace #"_BANG_" "!")
                          (clojure.string/replace #"_EQ_" "=")
                          (clojure.string/replace #"_LT_" "<")
                          (clojure.string/replace #"_GT_" ">")
                          (clojure.string/replace #"_STAR_" "*")))))

;;; check-if-anonymous-function: string -> string
(defn check-if-anonymous-function
  "Takes a string as function name and returns a string \"anonymous function\"
   if it is an anonymous function, its name otherwise"
  [fname]
  (if (or (= fname "fn") (re-matches #"fn_(.*)" fname) (re-matches #"fn-(.*)" fname))
    "anonymous function" fname))

;;; get-match-name: string -> string
(defn get-match-name
  "extract a function name from a qualified name"
  [fname]
  (let [;check-spec ((merge corefns-map specs-map) fname)
        ;m (if check-spec check-spec (nth (re-matches #"(.*)\$(.*)" fname) 2))
        matched (or (nth (re-matches #"(.*)\$(.*)" fname) 2)
                    (nth (re-matches #"(.*)/(.*)" fname) 2)
                    ;; the last match is the function name we need:
                    (first (reverse (re-matches #"(([^\.]+)\.)*([^\.]+)" fname))))]
    (if matched
      (check-if-anonymous-function (lookup-funct-name matched))
      fname)))

;;; remove-inliner: string -> string
(defn remove-inliner
  "If fname ends with inliner or any generated name with --, this will return everything before it"
  [fname]
  (let [match1 (nth (re-matches #"(.*)--(.*)" fname) 1)
        match2 (if match1 (nth (re-matches #"(.*)--(.*)" match1) 1) nil)]
    (or match2 match1 fname)))

;;; get-function-name: string -> string
(defn get-function-name
  [fname]
  "Returns a human-readable unqualified name of the function,
   or \"anonymous function\" if no name is found."
  (remove-inliner (get-match-name fname)))

;;; get-macro-name: string -> string
(defn get-macro-name [mname]
  "extract a macro name from a qualified name"
  (nth (re-matches #"(.*)/(.*)" mname) 2))

;;; arg-str: non-negative integer as a string -> string
(defn arg-str
  "arg-str takes a non-negative integer as a string and matches it
   to the corresponding argument number as a string, number as adjective"
  [n]
  (let [abs (fn [m] (if (> 0 m) (- m) m))
        n0 (+ 1 (Integer. n))
        n1 (mod (abs n0) 100)
        n2 (mod (abs n0) 10)]
    (case n0
      1 "first argument"
      2 "second argument"
      3 "third argument"
      4 "fourth argument"
      5 "fifth argument"
      (cond
        (or (= 11 n1) (= 12 n1) (= 13 n1)) (str n0 "th argument")
        (= 1 n2) (str n0 "st argument")
        (= 2 n2) (str n0 "nd argument")
        (= 3 n2) (str n0 "rd argument")
        :else   (str n0 "th argument")))))

(defn number-word
  "number word takes a positive integer as a string and changes it to a
   string with the numbers corresponding spelling"
  [n]
  (case n
    "0" "zero"
    "1" "one"
    "2" "two"
    "3" "three"
    "4" "four"
    "5" "five"
    "6" "six"
    "7" "seven"
    "8" "eight"
    "9" "nine"
    n))

(defn number-arg
  "number-arg takes a positive integer as a string and changes it to a
   string with the numbers corresponding spelling followed by
   \"argument(s)\", this is the number of arguments"
  [n]
  (cond
    (= n "0") "no arguments"
    (= n "1") (str (number-word n) " argument")
    :else (str (number-word n) " arguments")))

(defn number-vals
  "number-vals takes two strings, one which are the arguments that caused
   an error and the length required of the thing it errored on. It returns the
   number of arguments in failedvals and uses failedlength to determine
   the correct response."
  [failedvals failedlength]
  (if (not= "nil" failedvals)
    (let [x (count (read-string failedvals))]
      (cond
        (= failedlength "b-length-one") (if (> x 1)
                                            (str (number-word (str x)) " arguments")
                                            (str "no arguments"))
        (= failedlength "b-length-two") (if (> x 2)
                                            (str (number-word (str x)) " arguments")
                                            (str "one argument"))
        (= failedlength "b-length-three") (if (> x 3)
                                              (str (number-word (str x)) " arguments")
                                              (if (= x 1)
                                                (str "one argument")
                                                (str (number-word (str x)) " arguments")))
        (= failedlength "b-length-greater-zero") (str "no arguments")
        (= failedlength "b-length-greater-one") (if (= x 1)
                                                  (str "one argument")
                                                  (str "no arguments"))
        (= failedlength "b-length-greater-two") (if (= x 2)
                                                 (str "two arguments")
                                                 (if (= x 1)
                                                  (str "one argument")
                                                  (str "no arguments")))
        (= failedlength "b-length-zero-or-one") (str (number-word (str x)) " arguments")
        (= failedlength "b-length-two-or-three") (if (> x 3)
                                              (str (number-word (str x)) " arguments")
                                              (if (= x 1)
                                                (str "one argument")
                                                (str "no arguments")))
        (= failedlength "b-length-zero-to-three") (str (number-word (str x)) " arguments")
        :else failedlength))
      "no arguments"))

(defn ?-name
  "?-name takes a string and converts it into a new string
  that is easier to understand when reading error messages
  it replaces specific strings and removes ? and \r at the end of
  every string that gets passed to it"
  [n]
  (case n
    "coll?" "collection"
    "ifn?" "function"
    "fn?" "function"
    "object" "function"
    "seqable?" "collection"
    (cond
      (= "?" (subs n (- (count n) 1))) (subs n 0 (- (count n) 1))
      (= "\r" (subs n (- (count n) 1))) (?-name (subs n 0 (- (count n) 1)))
      (clojure.string/includes? n "object") (str "function") ;watch to make sure this doesn't break anything
      :else n)))

(defn check-function-name
  "check-function-name takes a string and converts it into a new string
  that has \" function \" added to the end if it is not
  anonymous function"
  [n]
  (cond
    (= n "anonymous function") "This anonymous function"
    :else (str "The function " n)))

(defn beginandend
  "beginandend puts (?s) at the beginning of a string and (.*) at the end
   of a string and turns it into a regex"
  [x]
  (re-pattern (str "(?s)" x "(.*)")))

(defn get-spec-text
  "return the string that failed a given spec from a spec error"
  [full-error]
  (nth (re-matches (beginandend #"(.*):args \((.*)\)}, compiling(.*)") full-error) 2))

(defn get-dictionary-type
  "get-dictionary-type takes a string and returns the corresponding type
   if the string is \"nil\" we return an empty string so the result in the
   error dictionary does not return nil nil"
  [x]
  (if (nil? (read-string x))
    ""
    (if (and (symbol? (read-string x)) (resolve (symbol x)))
      (-> x
        get-type
        (str " "))
      (-> x
        read-string
        type
        str
        (clojure.string/replace #"class " "")
        get-type
        (str " ")))))

(defn change-if
  "change-if takes a string and will output a string based on if
   the string is \"if\" or not"
   [x]
   (if (= x "if")
     "You are not using if correctly"
     (str "Name " x " is undefined")))

(defn check-divide
  "check-divide takes a string and returns either \"/\" or n
   this is only used for / because / is removed from the resulting
   error message so this adds it back in."
  [n]
  (if (= n "") "/" n))

(defn type-and-val
  "Takes a value (as a string) from a spec error, returns a vector
  of its type and readable value. Returns \"anonymous function\" as a value
  when given an anonymous function"
  [s]
  (let
     [t (get-dictionary-type s)]
     (cond (and (= t "a function ") (= (get-function-name s) "anonymous function")) ["" "an anonymous function"]
                                  (= t "a function ") [t (get-function-name s)]
                                  (re-find #"unrecognized type" t) [t ""]
                                  :else [t s])))
