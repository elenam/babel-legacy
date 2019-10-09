(ns errors.dictionaries
  (:require [errors.messageobj :as m-obj]
            [clojure.string :as s]
            [corefns.corefns :as cf]))

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
        matched (or (nth (re-matches #"(.*)\$(.*)@(.*)" fname) 2)
                    (nth (re-matches #"(.*)\$(.*)" fname) 2)
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
  "arg-str takes a non-negative integer and matches it
   to the corresponding argument number as a string, number as adjective"
  [n]
  (let [m (inc n)
        n1 (mod (Math/abs m) 100)
        n2 (mod (Math/abs m) 10)]
    (case m
      1 "first argument"
      2 "second argument"
      3 "third argument"
      4 "fourth argument"
      5 "fifth argument"
      (cond
        (or (= 11 n1) (= 12 n1) (= 13 n1)) (str m "th argument")
        (= 1 n2) (str m "st argument")
        (= 2 n2) (str m "nd argument")
        (= 3 n2) (str m "rd argument")
        :else   (str m "th argument")))))

(defn number-word
  "number word takes a positive integer as a string and changes it to a
   string with the numbers corresponding spelling"
  [n]
  (case n
     0 "zero"
     1 "one"
     2 "two"
     3 "three"
     4 "four"
     5 "five"
     6 "six"
     7 "seven"
     8 "eight"
     9 "nine"
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
  [x]
  "get-dictionary-type takes an object and returns its general type.
  If nil is passed returns an empty string."
  (if (nil? x) ""
      (-> x
          type
          str
          (clojure.string/replace #"class " "")
          get-type
          (str " "))))

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

(defn- is-specced-fn?
  "Takes a value and returns true if it is a specced functions
   and false otherwise"
   [val]
   (and (ifn? val)  (re-matches #"clojure\.spec\.test(.*)" (str val))))

(defn- specced-fn-name
  "Takes a specced function and attempts to find its user-readable name.
   If a name is found, it is returned. Otherwise the original name is returned
   unchanged."
   [s]
   (or (cf/specced-lookup s) s))

(defn type-and-val
  "Takes a value from a spec error, returns a vector
  of its type and readable value. Returns \"anonymous function\" as a value
  when given an anonymous function."
  [s]
  (cond (string? s) ["a string " (str "\"" s "\"")]
        (nil? s) ["nil " "nil"]
        :else (let [t (get-dictionary-type s)]
                   (cond
                         (is-specced-fn? s) ["a function " (str (specced-fn-name s))]
                         (and (= t "a function ") (= (get-function-name (str s)) "anonymous function"))
                              ["" "an anonymous function"]
                         (= t "a function ") [t (get-function-name (str s))]
                         (re-find #"unrecognized type" t) [t ""]
                         :else [t s]))))

(defn anonymous?
  "Returns a string representation of an anonymous function."
  [a]
  (if (= (str a) "an anonymous function") "#(...)" a))

(defn range-collapse
  "takes a range and if the collection is over 10 elements, returns the first 10 elements"
  [n]
  (if (and (cf/lazy? n) (< 10 (count n))) (cons (take 10 n) '(...)) n))

(defn macro-args->str
  "Takes a sequence of arguments for a macro, returns a string
   representation of these arguments for printing."
  [args]
  ; TO-DO: add printing of anonymous functions
  (if (empty? args) " "
      (let [args-with-spaces (conj (interpose " " args) " ")]
           (apply str args-with-spaces))))

  (defn extra-macro-args-info
    "Takes a spec problem map. Returns information about extra input for a macro
     if it exists (as a string), otherwise an empty string"
    [spec-problem]
    (let [{:keys [val]} spec-problem]
         (if-not (empty? val)
                 (str " The extra parts are:" (macro-args->str val))
                 "")))


 (defn print-single-arg
   "Takes a single (atomic) argument of a macro and returns its string representation"
   [val]
   (cond
     (and (re-matches #"p(\d)__(.*)" (str val)) (vector? val)) ""
     (string? val) (str "\"" val "\"")
     (nil? val) "nil"
     (= "fn*" (str val)) "#"
     (re-matches #"p(\d)__(.*)" (str val)) (s/replace (subs (str val) 0 2) #"p" "%")
     :else (str val)))

(defn- args->str
  "Takes a vector of (as strings) arguments and returns a string of these symbols
  with separated by empty spaces, enclosed into open-sym at the start and close-sym
  at the end, if provided"
  ([args]
  (apply print-str args))
  ([args open-sym close-sym]
  ;(s/join (cons open-sym (conj (vec (interpose " " args)) close-sym)))))
  (apply str [open-sym (apply print-str args) close-sym])))

(defn- single-arg?
  "Returns true if the argument is not seqable or a string or nil,
   false otherwise"
  [val]
  (or (not (seqable? val)) (string? val) (nil? val)))

(declare macro-args-rec) ;; needed for mutually recursive definitions

(defn- map-arg->str
  "Takes a map argument for a macro and returns its string representation"
  [map-arg]
  (args->str (map #(args->str (into (macro-args-rec (first %)) (macro-args-rec (second %)))) map-arg) "{"  "}"))

(defn seq-arg->str
  [arg]
  (cond
    (vector? arg) [(args->str (macro-args-rec arg) "[" "]")]
    (set? arg) [(args->str (macro-args-rec arg) "#{" "}")]
    :else [(args->str (macro-args-rec arg) "(" ")")]))

(defn macro-args-rec
  "Takes a potentially nested sequence of arguments of a macro and recursively
   constructs a flat vector of string representations of its elements"
  [args]
  (cond
    (single-arg? args) [(print-single-arg args)]
    ;; a sequence of a hashmap is two-element vectors; elements can have nested sequences:
    (map? args) [(map-arg->str args)]
    (map? (first args)) (into [(map-arg->str (first args))] (macro-args-rec (rest args)))
    (empty? args) []
    (not (single-arg? (first args))) (into (seq-arg->str (first args)) (macro-args-rec (rest args)))
    (and (not (empty? (rest args))) (= "fn*" (str (first args))))
          [(args->str (macro-args-rec (rest (rest args)))"#" "")] ;;condition to remove a vector after fn*
    :else (into [(print-single-arg (first args))] (macro-args-rec (rest args)))))

(defn print-macro-arg
  "Takes a potentially nested sequence of arguments of a macro and returns
   its string represntation"
  [val]
  (cond
    (single-arg? val) (print-single-arg val)
    (empty? val) ""
    (or (map? val) (map? (first val))) (args->str (macro-args-rec val))
    (not (single-arg? (first val))) (args->str (into (seq-arg->str (first val))  (macro-args-rec (rest val))))
    :else (args->str (into [(print-single-arg (first val))] (macro-args-rec (rest val))))))
