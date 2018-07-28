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
                      :clojure.lang.nil "nil"})

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
  (let [attempt (resolve (symbol t))
        type (if attempt attempt (clojure.lang.RT/loadClassForName (str "clojure.lang." t))) ;; may need to add clojure.lang. for some types.
        matched-type (if type (first (filter #(isa? type (first %)) general-types)))]
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
    "anonymous-function" fname))

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

  ;;; get-spec-text: string -> string
  (defn get-spec-text [full-error]
    "return the string that failed a given spec from a spec error"
  (nth (re-matches #"(.*):args \((.*)\)}, compiling(.*)" full-error) 2))

(defn pretty-print-single-value
  "returns a pretty-printed value that is not a collection"
  [value]
  ;; need to check for nil first because .getName fails otherwise
  (if (nil? value) "nil"
      (let [fname (.getName (type value))]
        (cond (string? value) (str "\"" value "\"")  ; strings are printed in double quotes
            ; extract a function from the class fname (easier than from value):
              (= (get-type fname) "a function") (get-function-name fname)
              (coll? value) "(...)"
              :else value))))

(defn delimeters
  "takes a collection and returns a vector of its delimeters as a vector of two strings"
  [coll]
  (cond
    (vector? coll) ["[" "]"]
    (set? coll) ["#{" "}"]
    (map? coll) ["{" "}"]
    :else ["(" ")"]))

(defn add-commas
  "takes a sequence and returns a sequence of the same elements with a comma
  inserted after every 3rd element in every 4-element group"
  [sq]
  (loop [result [] s sq n 1]
    (if (empty? s) result
        (if (= n 4)
          (recur (into result ["," (first s)]) (rest s) 1)
          (recur (conj result (first s)) (rest s) (inc n))))))

(defn add-spaces-etc
  "takes a sequence s and a limit n and returns the elements of s with spaces in-between
  and with ... at the end if s is longer than n"
  [s n is-map]
  (let [seq-with-spaces (interpose " " (take n s))
        seq-done (if is-map (add-commas seq-with-spaces) seq-with-spaces)]
    (if (> (count s) n)  (concat seq-done '("...")) seq-done)))

(defn pretty-print-nested-values
  "returns a vector of pretty-printed values. If it's a collection, uses the first limit
  number as the number of elements it prints, passes the rest of the limit numbers
  to the call that prints the nested elements. If no limits passed, returns (...)
  for a collection and teh string of a value for a single value"
  [value & limits]
  (if (or (not limits) (not (coll? value))) (pretty-print-single-value value)
      (let [[open close] (delimeters value)
          ;; a sequence of a map is a sequence of vectors, turning it into a flat sequence:
            flat-seq (if (map? value) (flatten (seq value)) value)]
        (conj (into [open] (add-spaces-etc
                            (take (inc (first limits)) (map #(apply pretty-print-nested-values (into [%] (rest limits))) flat-seq))
                            (first limits)
                            (map? value)))
              close))))

(defn preview-arg
  "returns a pretty-printed value of a preview of an arbitrary collection or value.
  The input consists of a value and a variable number of integers that indicate
  how many elements of a collection are displayed at each level. The rest of the
  sequence is truncated, ... is included to indicate truncated sequences."
  [& params]
  (let [pretty-val (apply pretty-print-nested-values params)]
    (if (coll? pretty-val) (cs/join (flatten pretty-val)) (str pretty-val))))

;;; arg-str: non-negative integer as a string -> string
(defn arg-str
  "arg-str takes a non-negative integer as a string and matches it
   to the corresponding argument number as a string"
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
    (cond
      (= "?" (subs n (- (count n) 1))) (subs n 0 (- (count n) 1))
      (= "\r" (subs n (- (count n) 1))) (?-name (subs n 0 (- (count n) 1)))
      (clojure.string/includes? n "object") (str "function") ;watch to make sure this doesn't break anything
      :else n)))

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

(defn check-divide
  "check-divide takes a string and returns either \"/\" or n
   this is only used for / because / is removed from the resulting
   error message so this adds it back in."
  [n]
  (if (= n "") "/" n))

(defn get-compile-error-location
  "takes a message of a compiler error and returns
  the location part that matches after 'compiling',
  as a hashmap. Returns an empty hashmap (no keys)
  when there is no match"
  [m]
  (zipmap [:file :line :char] (rest (rest (re-matches #"(.*), compiling:\((.+):(.+):(.+)\)" m)))))
