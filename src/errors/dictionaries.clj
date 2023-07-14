(ns errors.dictionaries
  (:require
            [clojure.string :as s]
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
                      :java.io.BufferedReader "a file or an input stream"
                      ;; gives enough to beginners to look it up:
                      :java.lang.Throwable "an exception (Throwable)"
                      :java.lang.Exception "an exception"})

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
    (or matched-type (str t))))

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
(def predefined-names {:_ "-" :_SLASH_ "/"})

(defn replace-special-symbols
  "Takes a name with special symbols, such as _STAR_, and replaces them
   with their printed equivalents. Special handling for '-' and '/'
   which are replaced only when appearing as entire names."
  [fname]
  (let [lookup ((keyword fname) predefined-names)]
    (or lookup
        (-> fname
            (s/replace #"_QMARK_" "?")
            (s/replace #"_BANG_" "!")
            (s/replace #"_EQ_" "=")
            (s/replace #"_LT_" "<")
            (s/replace #"_GT_" ">")
            (s/replace #"_STAR_" "*")
            (s/replace #"_SINGLEQUOTE_" "'")
            (s/replace #"_PLUS_" "+") ;; Note: _SLASH_ also may need to be replaced, but we might not want to since it has a special meaning
            (s/replace #"_AMPERSAND_" "&")))))

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
      (fn-name-or-anonymous (replace-special-symbols matched))
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
(defn get-macro-name
  "extract a macro name from a qualified name"
  [mname]
  (nth (re-matches #"(.*)/(.*)" mname) 2))

(defn position-0-based->word
  "Takes a non-negative integer as a number and changes it to its
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
   to the corresponding argument number as a string, number as adjective."
  [n]
  (str (position-0-based->word n)
       " argument"))

(defn number-word
  "number-word takes a non-negative integer as a string and changes it to a
   string with the number's corresponding spelling"
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

(defn- stringlike?
  "Returns true if its argument should be printed as a string (in double quotes)
   and false otherwise."
  [s]
  (or (string? s)
      (instance? StringBuffer s)
      (instance? StringBuilder s)))

(defn- message-or-empty
  "Takes an exception and returns its message formatting for a fucntion argument
   or an empty string if the message is nil."
  [s]
  (let [m (.getMessage s)]
       (if m (str ": \"" m "\"") "")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;; Functions for printing args for non-macro spec failures ;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(declare type-and-val)

(defn- insert-str-into-coll
  "Takes a string and a string to insert (such as \"...\") and inserts it before the last character."
  [s dots]
  (let [n (count s)
        m (dec n)]
        (str (subs s 0 m)
             dots
             (subs s m n))))

(def LEVEL-1-LENGTH 10)
(def NESTED-LENGTH 3)

(defn- print-coll-elt
  [x n]
  (-> x
      (type-and-val n)
      second))

(defn- trim-to-n
  "Takes a non-map collection and returns the string representation of its
   first up to n elements, with ellipses as needed."
  [c n]
  (let [m (count c)]
       (if (<= m n)
           (print-str (sp/transform [sp/ALL] #(print-coll-elt % NESTED-LENGTH) c))
           (let [c1 (sp/transform [(sp/srange n m)] sp/NONE c)
                 c2 (if (set? c)
                        (into #{} c1)
                        c1)
                 c3 (print-str (sp/transform [sp/ALL] #(print-coll-elt % NESTED-LENGTH) c2))]
                 (insert-str-into-coll c3 "...")))))

(defn- trim-map-to-n
  "Takes a map and returns the string representation of up to n of its
   key/val pairs. Elements are chosen based on the first n values of the
   map's keys"
  [m n]
  (let [k (count m)] ; counts the number of key/val pairs
       (if (<= k n)
           ;; need to make recursive calls on all of its keys and vals
           (print-str (sp/transform [sp/ALL]
                                    (fn [[k v]] [(print-coll-elt k NESTED-LENGTH)
                                                 (print-coll-elt v NESTED-LENGTH)])
                                    m))
           (let [ks (take n (keys m))
                 m1 (sp/select-one (sp/submap ks) m)
                 m2 (print-str (sp/transform [sp/ALL]
                                             (fn [[k v]] [(print-coll-elt k NESTED-LENGTH)
                                                          (print-coll-elt v NESTED-LENGTH)])
                                             m1))]
                (insert-str-into-coll m2 ",...")))))

(defn anonymous->str
  "Replaces the 'anonymous function' wording with #(...)."
  [s]
  (s/replace s "an anonymous function" "#(...)"))

(defn non-macro-spec-arg->str
  "Takes a non-macro argument and returns its easy-to-read string representation."
  [s]
  (-> s
      (print-coll-elt LEVEL-1-LENGTH)
      anonymous->str))

(defn anon-fn-handling
  "Takes a processed value and replaces anonymous functions in it, unless
   the entire expression is 'an anonymous function'."
  [s]
  (if (= s "an anonymous function")
      s
      (anonymous->str s)))

(def java-packages ["java." "javax." "com.sun." "jdk." "netscape.javascript" "org.ietf." "org.w3c." "org.xml."])

(defn- matching-prefix?
  "Takes a package name and returns a truthy value if it starts with one of teh listed packages,
  nil otherwise."
  [st]
  (filter #(s/starts-with? st %) java-packages))

(defn- isJava?
  "Takes a type and returns true if it's a Java (non-Clojure) type
   and false otherwise."
   [t]
   (-> t
       type
       .getPackage
       .getName
       matching-prefix?))

(defn type-and-val
  "Takes a value from a spec error, returns a vector
  of its type and readable value. Returns \"anonymous function\" as a value
  when given an anonymous function."
  ([s]
    (type-and-val s LEVEL-1-LENGTH))
  ([s n]
  (cond (nil? s) ["" "nil"]
        (stringlike? s) ["a string " (str "\"" s "\"")]
        (= Object (class s)) ["an object " "<...>"]
        (instance? java.lang.Throwable s) ["an exception " (str "<"
                                                                (.getSimpleName (type s))
                                                                (message-or-empty s)
                                                                ">")]
        (instance? java.util.regex.Pattern s) ["a regular expression pattern " (str "#\"" s "\"")]
        (instance? java.lang.reflect.Type s) ["a type " (.getTypeName s)]
        (instance? clojure.lang.LazySeq s) ["a sequence " (print-str s)]
        (map? s) [(get-dictionary-type s) (trim-map-to-n s (Math/ceil (/ n 2)))] ; passing the number of pairs for a map
        (coll? s) [(get-dictionary-type s) (trim-to-n s n)]
        (.isArray (type s)) ["an array "  (s/trim (with-out-str (clojure.pprint/pprint s)))]
        (.isEnum (type s)) ["a constant "  (s/trim (str s))]
        :else (let [t (get-dictionary-type s)]
                   (cond
                         (is-specced-fn? s) ["a function " (str (specced-fn-name s))]
                         (and (= t "a function ") (= (get-function-name (str s)) "anonymous function"))
                              ["" "an anonymous function"]
                         (= t "a function ") [t (get-function-name (str s))]
                         (and (re-find #"unrecognized type" t) (isJava? s)) [(str "a Java type ") (str s)]
                         (re-find #"unrecognized type" t) [t ""]
                         :else [t s])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;; Printing macro args ;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(declare print-macro-arg)

(defn extra-macro-args-info
  "Takes a spec problem map. Returns information about extra input for a macro
   if it exists (as a string), otherwise an empty string."
  [spec-problem]
  (let [{:keys [val]} spec-problem]
       (if-not (empty? val)
               (str " The extra parts are: " (print-macro-arg val :no-parens))
               "")))

(defn print-single-arg
 "Takes a single (atomic) argument of a macro and returns its string representation."
 [val]
 (cond
   (nil? val) "nil"
   (char? val) (str "\\" val)
   (string? val) (str "\"" val "\"")
   (instance? java.util.regex.Pattern val) (str "#\"" val "\"")
   (re-matches #"p(\d)__(.*)" (str val)) (s/replace (subs (str val) 0 2) #"p" "%")
   (re-matches #"p__(.*)" (str val)) ""
   (re-matches #"rest__(\d+)(.*)" (str val)) "%&"
   :else (str val)))

(defn- args->str
  "Takes a vector of arguments (as strings) and returns a string of these symbols
  separated by empty spaces, enclosed into open-sym at the start and close-sym
  at the end, if provided"
  ([args]
  (apply print-str args))
  ([args open-sym close-sym]
  (apply str [open-sym (apply print-str args) close-sym])))

(defn- single-arg?
  "Returns true if the argument is not seqable or a string or nil,
   false otherwise"
  [val]
  (or (nil? val) (not (seqable? val)) (string? val)))

(declare macro-args-rec) ;; needed for mutually recursive definitions & process-args

(defn- map-entry->str
  "Takes a map entry, applies macro-args-rec to both the key and the value,
   and joins the two vectors together."
  [[k v]]
  (into (macro-args-rec k) (macro-args-rec v)))

(defn- map-arg->str
  "Takes a map argument for a macro and returns its string representation"
  [map-arg]
  (str "{"
       (s/join ", " (map #(args->str (map-entry->str %)) map-arg))
       "}"))

(defn- process-arg
  [arg]
  (cond (single-arg? arg) (print-single-arg arg)
        (map? arg) (map-arg->str arg)
        (vector? arg) (args->str (macro-args-rec arg) "[" "]")
        (set? arg) (args->str (macro-args-rec arg) "#{" "}")
        (= "fn*" (str (first arg))) (args->str (macro-args-rec (rest (rest arg))) "#" "")
        (and (symbol? (first arg)) (= "quote" (str (first arg)))) (args->str (macro-args-rec (rest arg)) "'" "")
        :else (args->str (macro-args-rec arg) "(" ")")))

(defn- process-args
  [args]
  (cond (single-arg? args) [(print-single-arg args)]
        (map? args) [(map-arg->str args)]
        (= "fn*" (str (first args))) [(args->str (macro-args-rec (rest (rest args))) "#" "")]
        (and (symbol? (first args)) (= "quote" (str (first args)))) [(args->str (macro-args-rec (rest args)) "'" "")]
        :else (vec (map process-arg args))))

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
 [val & mods]
  (cond (and (some #{:nil} mods) (nil? val)) "nil"
        (nil? val) ""
        (some #{:no-parens} mods)
              (or (second (re-matches #"\((.*)\)" (process-arg val)))
                  (process-arg val))
        :else (process-arg val)))

;; Note that, while this has an overlap with general-types, I prefer
;; to keep it separate since it's used for a different purpose:

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
