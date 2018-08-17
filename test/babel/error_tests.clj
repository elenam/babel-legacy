(ns babel.error-tests
  (:require
   [expectations :refer :all]
   [corefns.instrumentfunctionsfortesting])
  (:use
   [loggings.loggingtool :only [get-error start-log add-log]]))

;;you need to have launched a nREPL server in babel for these to work.
;;this must be the same port specified in project.clj

;;start logging
(start-log true)
(expect nil (add-log
              (do
                (def file-name "this file")
                (:file (meta #'file-name)))))


;;test non erroring commands
(expect  nil (get-error "(+ 5 8)"))
(expect  nil (get-error "(prn \"error\")"))
(expect  nil (get-error "(take 5 (filter #(> 8 %) (repeatedly #(rand-int 10))))"))

;;arithmetic-exception-divide-by-zero
(expect "Tried to divide by zero\n" (get-error "(/ 70 0)"))

(expect "Tried to divide by zero\n" (get-error "(/ 70 8 0)"))

;;compiler-exception-cannot-resolve-symbol
(expect "Name smoked-cod is undefined.\n" (get-error "(smoked-cod)"))
(expect "Name Ebeneezer is undefined.\n" (get-error "(Ebeneezer)"))

;;class-cast-exception
(expect "Expected a number, but a string was given instead.\n" (get-error "(+ 8 \"seventeen\")"))
(expect "Expected a number, but a string was given instead.\n" (get-error "(+ \"hello\" 3)"))
;(expect "Attempted to use a number, but a function was expected.\n" (get-error "(map 5 [3])"))

(expect "The arguments following the map or vector in assoc must come in pairs, but one of them does not have a match.\n" (get-error "(assoc {} 1 \"hello\" 2)"))

;(expect "\n" (get-error ""))
(expect "A function keyword can only take one or two arguments, but three arguments were passed to it.\n" (get-error "(keyword \"hello\" \"goodbye\" \"hello\")"))

;(expect "Vectors added to a map must consist of two elements: a key and a value.\n" (get-error "(conj {} [1 1 1])"))

(expect "No value found for key :2. Every key for a hash-map must be followed by a value.\n" (get-error "(hash-map :1 1, :2)"))

(expect "The function symbol cannot take three arguments.\n" (get-error "(symbol \"hello\" \"goodbye\" \"hello\")"))

(expect "Cannot call nil as a function.\n" (get-error "(nil)"))

(expect "You cannot use the same key in a hash-map twice, but you have duplicated the key 1.\n" (get-error "{1 1 1 1}"))

(expect "Unexpected end of file, starting at line 1. Probably a non-closing parenthesis or bracket.\n" (get-error "(def elements-that-can-contain-simple-types #_=> #{:xs:attribute"))

(expect "Invalid number: 1.2.2.\n" (get-error "(+ 1.2.2 0)"))

(expect "You cannot use / in this position.\n" (get-error "(/string \"abcd\")"))

(expect "Parameters for cond must come in pairs, but one of them does not have a match.\n" (get-error "(cond (seq? [1 2]) 5 (seq? [1 3]))"))
(expect "Parameters for loop must come in pairs, but one of them does not have a match.\n" (get-error "(defn s [s] (loop [s]))"))

(expect "with-open is a macro and cannot be used by itself or passed to a function.\n" (get-error "(defn makeStructs [fName] with-open[r (reader (file fName))] (let [r res (doall (map makeStruct (line-seq r)))] (. r close) res))")) ;credit: https://stackoverflow.com/questions/5751262/cant-take-value-of-a-macro-clojure

(expect "% can only be followed by & or a number.\n" (get-error "(#(+ %a 1) 2 3)"))

(expect "Position five is outside of the string.\n" (get-error "(nth \"hello\" 5)"))

(expect "An index in a sequence is out of bounds or invalid.\n" (get-error "(nth (seq [1 2 3]) 5)"))

;(expect "An attempt to access a non-existing object (NullPointerException).\n" (get-error "(defn my-even [ilist] (if (= (mod (first ilist) 2) 0)(concat (list (first ilist)) (my-even (rest ilist)))(my-even (rest ilist)))) (my-even '(1,2,3,4,5))")) ;credit: https://stackoverflow.com/questions/7584337/clojure-nullpointerexception-error

(expect "Name clojure.hello is undefined.\n" (get-error "(clojure.hello/union #{1 2 3} #{3 4})"))

(expect "A hash map must consist of key/value pairs; you have a key that's missing a value.\n" (get-error "{:body {(str \"hello\")}}"))

(expect "The function hello cannot be called with one argument.\n" (get-error "(defn hello [x y] (* x y)) (hello 1)"))

(expect "The function hello cannot be called with three arguments.\n" (get-error "(defn hello [x y] (* x y)) (hello 1 2 3)"))

(expect "The function hello cannot be called with no arguments.\n" (get-error "(defn hello [x & xs] (* x 1)) (hello)"))

;; Should not use "Function" here, but ok for now
(expect "This anonymous function cannot be called with one argument.\n" (get-error "(map #(+ %1 %2) [1 2 3])"))

(expect "Too many arguments to if.\n" (get-error "(if (= 0 0) (+ 2 3) (+ 2 3) (+2 3))"))

(expect "Too few arguments to if.\n" (get-error "(if (= 0 0))"))

(expect "Loop requires a vector for its binding.\n" (get-error "(loop x 5 (+ x 5))"))

(expect "Mismatch between the number of arguments of outside function and recur: recur must take one argument but was given two arguments.\n" (get-error "(defn reduce-to-zero [x] (if (= x 0) x (recur reduce-to-zero (- x 1))))"))

(expect "Mismatch between the number of arguments of outside function and recur: recur must take one argument but was given two arguments.\n" (get-error "(loop [x 5] (if (< x 1) \"hi\" (recur (dec x) (print x))))"))

(expect "Expected a number, but a string was given instead.\n" (get-error "(map #(+ % \"a\") [3])"))

(expect "Expected a file or an input stream, but a number was given instead.\n" (get-error "(line-seq 3)"))

(expect "let is a macro and cannot be used by itself or passed to a function.\n" (get-error "(map let let)"))

(expect "You are not using if correctly.\n" (get-error "if"))

(expect "Recur can only occur as a tail call: no operations can be done after its return.\n" (get-error "(loop [x 5] (recur x)(recur x))"))

;(expect "Variable name was expected but 5 was used instead.\n" (get-error "(loop [5 y])"))


;; This is not a good error message, but we can't do better. The real cause is destructuring.
(expect "Function nth does not allow a map as an argument.\n" (get-error " (defn f [[x y]] (+ x y)) (f {2 3})"))



;; it doesn't look like we can run this test; works correctly in repl
#_(expect "Clojure ran out of memory, likely due to an infinite computation.\n" (get-error "(range)"))

;(expect "Attempted to use a string, but a number was expected" (get-error "(+ 8 \"seventeen\")"));;will not work until we write specs for core functions

;; NullPointerException with an object given
;; This might not be what we want (might need to process the object), but that's what it currently is
(expect #"(?s)An attempt to access a non-existing object:   java\.util\.regex\.Pattern\.<init> (.*) \(NullPointerException\)\."
        (get-error "(re-pattern nil)"))

(expect "Position -2 is outside of the string.\n" (get-error "(subs \"a\" 3)"))

(expect "3 cannot be opened as an InputStream.\n" (get-error "(slurp 3)"))

(expect #"The file a\.txt does not exist(.*)" (get-error "(slurp \"a.txt\")"))

(expect "The system was looking for a class nonsense.class or a file nonsense.clj, but neither one was found.\n"
        (get-error "(require 'nonsense)"))

(expect "The system was looking for a class nonsense/1/2.class or a file nonsense/1/2.clj, but neither one was found.\n"
        (get-error "(require 'nonsense.1.2)"))

(expect "You cannot use nonsense/1 in this position.\n"
        (get-error "(require 'nonsense/1)"))

(expect "The system was looking for a class clojure/string/stuff.class or a file clojure/string/stuff.clj, but neither one was found.\n"
        (get-error "(require '[clojure.string.stuff :as stuff])"))

(expect "No value found for key 3. Every key for a hash-map must be followed by a value.\n" (get-error "(map #(slurp \"usethistext.txt\" %) [3])"))

;; TO-DO: clean up function names
;; Note: this test runs correctly, but it redefines fn every time it runs.
;; Restarting nrepl is required after the tests are run.
#_(expect #"Warning: fn already refers to: \#'clojure.core/fn in namespace: utilities\.spec_generator, being replaced by: \#'utilities\.spec_generator/fn(.*)" (get-error "(defn fn [x] x)"))

#_(expect "IllegalState: failed validation.\n" (get-error "(def a (atom [3])) (set-validator! a #(every? odd? %)) (swap! a into [2])"))
(expect "IllegalState: failed validation.\n" (get-error "(ref 0 :validator pos?)"))
#_(expect "IllegalState: trying to lock a transaction that is not running.\n" (get-error "(def b (ref 3)) (ref-set b 6)"))
#_(expect "IllegalState: trying to lock a transaction that is not running.\n" (get-error "(ensure b)"))
(expect "IllegalState: I/0 in transaction.\n" (get-error "(dosync (io! (println \"h\")))"))

(expect "a is not a function in the clojure.string library.\n" (get-error "(clojure.string/a 3)"))
(expect "f is not a function in the clojure.string library.\n" (get-error "(clojure.string/f 3)"))

;##### Spec Testing #####

;; Note: spec-ed functions come out as anonymous at this point because the name refers to spec, not to the function. This will be fixed.
(expect #"In function conj, the first argument is expected to be a collection, but is an anonymous function instead.\n" (get-error "(conj even? \"a\")")) ;not the result we want but good for now
(expect #"In function map, the second argument is expected to be a collection, but is an anonymous function instead.\n" (get-error "(map even? even?)")) ;not the result we want but good for now
(expect #"In function map, the second argument is expected to be a collection, but is an anonymous function instead.\n" (get-error "(map even? #(+ % 2))"))
(expect #"In function map, the second argument is expected to be a collection, but is a function f instead.\n" (get-error "(defn f [x] (+ x 2)) (map even? f)"))
(expect #"In function map, the second argument is expected to be a collection, but is a function f\? instead.\n" (get-error "(defn f? [x] (+ x 2)) (map even? f?)"))
(expect #"In function denominator, the first argument is expected to be a ratio, but is an anonymous function instead.\n" (get-error "(denominator even?)")) ;not the result we want but good for now
(expect "In function map, the first argument is expected to be a function, but is nil instead.\n" (get-error "(map nil)"))
(expect "In function conj, the first argument is expected to be a collection, but is a character \\a instead.\n" (get-error "(conj \\a \"a\")"))
(expect "In function conj, the first argument is expected to be a collection, but is a string \"a\" instead.\n" (get-error "(conj \"a\" 3)"))
(expect "In function even?, the first argument is expected to be a number, but is a string \"a\" instead.\n" (get-error "(even? \"a\")"))
(expect "In function rand-int, the first argument is expected to be a number, but is a string \"3\" instead.\n" (get-error "(rand-int \"3\")"))
(expect "In function conj, the first argument is expected to be a collection, but is a number 3 instead.\n" (get-error "(conj 3)"))
(expect "In function conj, the first argument is expected to be a collection, but is a number 3 instead.\n" (get-error "(conj 3 3)"))
(expect "In function map, the second argument is expected to be a collection, but is a number 3 instead.\n" (get-error "(map even? 3)"))
(expect "In function map, the 6th argument is expected to be a collection, but is a number 3 instead.\n" (get-error "(map even? [3] [3] [3] [3] 3)"))
(expect "In function map, the first argument is expected to be a function, but is a number 3 instead.\n" (get-error "(map 3 [3])"))
(expect "In function denominator, the first argument is expected to be a ratio, but is a number 3 instead.\n" (get-error "(denominator 3)"))
(expect "In function numerator, the first argument is expected to be a ratio, but is a number 3 instead.\n" (get-error "(numerator 3)"))
(expect "In function map, the second argument is expected to be a collection, but is a regular expression pattern #\"h\" instead.\n" (get-error "(map [3 2 3 4 5] #\"h\")"))
(expect "In function even?, the first argument is expected to be a number, but is unrecognized type java.lang.Object  instead.\n" (get-error "(even? (new Object))"))
(expect "In function even?, the first argument is expected to be a number, but is a file or an input stream java.io.BufferedReader instead.\n" (get-error "(even? (clojure.java.io/reader \"usethistext.txt\"))"))
(expect "In function even?, the first argument is expected to be a number, but is a function even? instead.\n" (get-error "(even? (read-string (first (reverse (line-seq (clojure.java.io/reader \"usethistext.txt\"))))))"))

(expect "conj can only take one or more arguments; recieved no arguments.\n" (get-error "(conj)"))
(expect "map can only take one or more arguments; recieved no arguments.\n" (get-error "(map)"))
(expect "rand-int can only take one argument; recieved no arguments.\n" (get-error "(rand-int)"))
(expect "rand-int can only take one argument; recieved three arguments.\n" (get-error "(rand-int 2 3 4)"))

(expect "denominator cannot take as few arguments as are currently in it, needs more arguments.\n" (get-error "(denominator)"))
(expect "denominator cannot take as many arguments as are currently in it, needs fewer arguments.\n" (get-error "(denominator 1/3 3)"))

(expect "In function require, the list/vector may only be followed by a keyword.\n" (get-error "(require '(clojure.java [io :as a :refer [ends-with?]] \"a\"))"))
(expect "In function require, args must come in key value pairs.\n" (get-error "(require '(clojure.java [io :as a :refer [ends-with?] :rename] :reload))"))

(expect "In function require, the first argument is expected to be a list, but is a number 3 instead.\n" (get-error "(require 3)"))
(expect "In function require, the first argument is expected to be a list, but is nil instead.\n" (get-error "(require nil)"))
(expect "In function require, the first argument is expected to be a list, but is a string \"a\" instead.\n" (get-error "(require \"a\")"))
(expect "In function require, the first argument is expected to be a list, but is a vector [3] instead.\n" (get-error "(require [3])"))
(expect "In function require, the first argument is expected to be a list, but is a vector [3 2] instead.\n" (get-error "(require [3 2])"))

;#################################################################
;############################## Specs Not From Core ##############
;#################################################################

(expect "In function query, the first argument is expected to be a string, but is a map {} instead.\n" (get-error "(clojure.java.jdbc/query {})"))
(expect "In function query, the first argument is expected to be a string, but is a map {:a 3} instead.\n" (get-error "(clojure.java.jdbc/query {:a 3})"))
(expect "update! cannot take as few arguments as are currently in it, needs more arguments. This is most likely do to a map missing arguments.\n" (get-error "(clojure.java.jdbc/update! \"a\" :a {:s 3})"))
(expect "In function db-query-with-resultset, the second argument is expected to be a string, but is a number 3 instead.\n" (get-error "(clojure.java.jdbc/db-query-with-resultset \"a\" 3)"))
(expect "In function get-by-id, the second argument is expected to be a keyword, but is a number 3 instead.\n" (get-error "(clojure.java.jdbc/get-by-id \"a\" 3)"))

;#################################################################
;############################## Nested errors ####################
;#################################################################

(expect "Tried to divide by zero\n" (get-error "(even? [(map #(/ % 0) [1 2])])"))

(expect "In function map, the first argument is expected to be a function, but is a number 2 instead.\n" (get-error "(even? [(map 2 [1 2])])"))

;; spec isn't checked on secondary errors in arguments that failed (if they are lazy sequences). This is not a spec error:
(expect "Expected a function, but a number was given instead.\n" (get-error "(even? (lazy-cat [2 3] (map 5 [1 2])))"))

;; If the sequence evaluation is forced, we get a spec error:
(expect "In function map, the first argument is expected to be a function, but is a number 5 instead.\n" (get-error "(even? (doall (lazy-cat [2 3] (map 5 [1 2]))))"))

;; lazy sequences as unrelated function arguments aren't evaluated when a spec fails:
(expect "In function map, the first argument is expected to be a function, but is a number 5 instead.\n" (get-error "(map 5 (lazy-cat [2 3] [(/ 1 0) 8]))"))

(expect "In function even?, the first argument is expected to be a number, but is an anonymous function instead.\n" (get-error "(odd? (even? even?))"))

(expect "In function even?, the first argument is expected to be a number, but is a list (2 3 1/2 8) instead.\n" (get-error "(map #(+ % (even? (lazy-cat [2 3] [(/ 1 2) 8]))) [3])"))
