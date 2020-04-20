(ns errors.dictionaries
  (:require [clojure.string :as s]
            [com.rpl.specter :as sp]
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

(def general-types [[java.lang.Number "a number"]
                    [clojure.lang.IPersistentVector "a vector"]
                    [clojure.lang.IPersistentList "a list"]
                    [clojure.lang.IPersistentSet "a set"]
                    [clojure.lang.IPersistentMap "a map"]
                    [clojure.lang.ISeq "a sequence"]
		                ;; collections - must go before functions since some collections
		                ;; implement the IFn interface
                    [clojure.lang.IPersistentCollection "a collection"]
                    [clojure.lang.IFn "a function"]])

(defn- lookup-general-type
  [t]
  (sp/select-first [sp/ALL #(isa? t (first %)) (sp/nthpath 1)] general-types))

;; The best approximation of a type t not listed in the type-dictionary (as a string)
;;; best-approximation: type -> string
(defn best-approximation
  "returns a string representation of a type t not listed in the type-dictionary for user-friendly error messages"
  [t]
  (let [t1 (resolve (symbol t))
        t2 (if (= (type t1) clojure.lang.Var) (type (var-get t1)) t1)
        t3 (or t2 (clojure.lang.RT/loadClassForName (str "clojure.lang." t))) ;; may need to add clojure.lang. for some types.
        matched-type (lookup-general-type t3)]
    (or matched-type (str "unrecognized type " t))))

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
    (or lookup
        (-> fname
            (s/replace #"_QMARK_" "?")
            (s/replace #"_BANG_" "!")
            (s/replace #"_EQ_" "=")
            (s/replace #"_LT_" "<")
            (s/replace #"_GT_" ">")
            (s/replace #"_STAR_" "*")))))

;;; fn-name-or-anonymous: string -> string
(defn fn-name-or-anonymous
  "Takes a string as function name and returns a string \"anonymous function\"
   if it is an anonymous function, its name otherwise"
  [fname]
  (if (or (= fname "fn")
          (re-matches #"fn_(.*)" fname) ;; underscore
          (re-matches #"fn-(.*)" fname) ;; dash
          (re-matches #"eval(\d+)" fname))
      "anonymous function" fname))

;;; get-match-name: string -> string
(defn get-match-name
  "extract a function name from a qualified name"
  [name]
  (let [fname (or name "")
        matched (or (nth (re-matches #"(.*)\$(.*)@(.*)" fname) 2)
                    (nth (re-matches #"(.*)\$(.*)" fname) 2)
                    (nth (re-matches #"(.*)/(.*)" fname) 2)
                    ;; the last match is the function name we need:
                    (first (reverse (re-matches #"(([^\.]+)\.)*([^\.]+)" fname))))]
    (if matched
      (fn-name-or-anonymous (lookup-funct-name matched))
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

(defn position-0-based->word
  "number word takes a positive integer as a string and changes it to its
  position (first, second, etc.) spelled out; 0 corresponds to 'first'."
  [n]
  (let [m (inc n)
        n1 (mod (Math/abs m) 100)
        n2 (mod (Math/abs m) 10)]
        (if (< n 5)
            (["first" "second" "third" "fourth" "fifth"] n)
            (cond
              (or (= 11 n1) (= 12 n1) (= 13 n1)) (str m "th")
              (= 1 n2) (str m "st")
              (= 2 n2) (str m "nd")
              (= 3 n2) (str m "rd")
              :else   (str m "th")))))

;;; arg-str: non-negative integer as a string -> string
(defn arg-str
  "arg-str takes a non-negative integer and matches it
   to the corresponding argument number as a string, number as adjective"
  [n]
  (str (position-0-based->word n) " argument"))

(defn number-word
  "number-word takes a non-negative integer as a string and changes it to a
   string with the numbers corresponding spelling"
  [n]
  (if (< n 10) (["zero" "one" "two" "three" "four" "five" "six" "seven" "eight" "nine"] n) n))

(defn number-arg
  "number-arg takes a positive integer as a string and changes it to a
   string with the numbers corresponding spelling followed by
   \"argument(s)\", this is the number of arguments"
  [n]
  (cond
    (= n "0") "no arguments"
    (= n "1") "one argument"
    :else (str (number-word (read-string n)) " arguments")))

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
  [f-name]
  (let [gen-type-lookup (->> f-name
                             (str "clojure.lang.")
                             read-string
                             resolve
                             lookup-general-type)]
        (cond
          (= f-name "anonymous function")
             "This anonymous function"
          (and gen-type-lookup (not= gen-type-lookup "a function"))
              (str (s/capitalize gen-type-lookup)) ;; For vectors and such used as function
          :else (str "The function " f-name))))

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
        (nil? s) ["" "nil"]
        (instance? java.util.regex.Pattern s) ["a regular expression pattern " (str "#\"" s "\"")]
        (instance? clojure.lang.LazySeq s) ["a sequence " (print-str s)]
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

(declare print-macro-arg)

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
                 (str " The extra parts are: " (print-macro-arg val))
                 "")))


 (defn print-single-arg
   "Takes a single (atomic) argument of a macro and returns its string representation"
   [val]
   (cond
     (and (re-matches #"p(\d)__(.*)" (str val)) (vector? val)) ""
     (string? val) (str "\"" val "\"")
     (char? val) (str "\\" val)
     (instance? java.util.regex.Pattern val) (str "#\"" val "\"")
     (nil? val) "nil"
     (and (symbol? val) (= "quote" (str val))) "'"
     (= "fn*" (str val)) "#"
     (re-matches #"p(\d)__(.*)" (str val)) (s/replace (subs (str val) 0 2) #"p" "%")
     (re-matches #"p__(.*)" (str val)) ""
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

(declare macro-args-rec) ;; needed for mutually recursive definitions & process-args

(defn- map-arg->str
  "Takes a map argument for a macro and returns its string representation"
  [map-arg]
  (args->str (map #(args->str (into (macro-args-rec (first %)) (macro-args-rec (second %)))) map-arg) "{"  "}"))

(defn- seq-arg->str
  [arg]
  (cond
    (vector? arg) [(args->str (macro-args-rec arg) "[" "]")]
    (set? arg) [(args->str (macro-args-rec arg) "#{" "}")]
    (= "fn*" (str (first arg)))  ;;condition to remove a vector after fn*
                              [(args->str (macro-args-rec (rest (rest arg)))"#" "")]
    (and (symbol? (first arg)) (= "quote" (str (first arg)))) [(args->str (macro-args-rec (rest arg)) "'" "")]
    :else [(args->str (macro-args-rec arg) "(" ")")]))

(defn- process-args
  [args]
  (cond
      (single-arg? args) [(print-single-arg args)]
      ;; a sequence of a hashmap is two-element vectors; elements can have nested sequences:
      (map? args) [(map-arg->str args)]
      (map? (first args)) (into [(map-arg->str (first args))] (macro-args-rec (rest args)))
      (empty? args) []
      (not (single-arg? (first args)))
            (into (seq-arg->str (first args)) (macro-args-rec (rest args)))
      (= "fn*" (str (first args))) [(args->str (macro-args-rec (rest (rest args)))"#" "")]
      (and (symbol? (first args)) (= "quote" (str (first args)))) [(args->str (macro-args-rec (rest args)) "'" "")]
      :else (into [(print-single-arg (first args))] (macro-args-rec (rest args)))))

(defn- macro-args-rec
  "Takes a potentially nested sequence of arguments of a macro and recursively
   constructs a flat vector of string representations of its elements"
  [args]
  (->> args
       process-args
       (filter #(not (re-matches #"( *)" %)))
       vec))

(defn print-macro-arg
  "Takes a potentially nested sequence of arguments of a macro and returns
   its string represntation. If one argument is given, doesn't add any
   delimeters. Can take optional delimeters."
  ([val]
   (cond (nil? val) ""
         (set? val) (str "#{" (s/join " " (macro-args-rec val)) "}")
         (vector? val) (str "[" (s/join " " (macro-args-rec val)) "]")
         :else (s/join " " (macro-args-rec val))))
  ([val k]
   (if (and (= k :sym) (not (single-arg? val)) (not (map? val)))
       (s/join " " (macro-args-rec (list val)))
       (s/join " " (macro-args-rec val))))
  ([val open-sym close-sym]
   (str open-sym (s/join " " (macro-args-rec val)) close-sym)))

;; Note that, while this has an overlap with general-types, I prefer
;; to keep it separate since it's used for a different purpose.
(def class-lookup [[java.lang.Number "a number"]
                   [java.lang.String "a string"]
                   [java.lang.Character "a character"]])

(defn get-common-class
  "Takes a class name from a missing method error message and looks
   up some common classes, reurning a string to include into the error message."
  [c]
  (let [t (resolve (symbol c))
        name (sp/select-first [sp/ALL #(isa? t (first %)) (sp/nthpath 1)] class-lookup)]
       (if name
           (str " for " name " (" c ")")
           (str " in the class " c))))
