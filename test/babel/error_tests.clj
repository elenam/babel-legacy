(ns babel.error-tests
  (:require
   [expectations :refer :all]
   [clojure.tools.nrepl :as repl]))

;;you need to have launched a nREPL server in babel for these to work.
;;this must be the same port specified in project.clj
(def server-port 7888)

(defn trap-response
  "evals the code given as a string, and returns the list of associated nREPL messages"
  [inp-code]
  (with-open [conn (repl/connect :port server-port)]
    (-> (repl/client conn 1000)
        (repl/message {:op :eval :code inp-code})
        doall)))

(defn msgs-to-error
  "takes a list of messages and returns nil if no :err is present, or the first present :err value"
  [list-of-messages]
  (:err (first (filter :err list-of-messages))))

(defn get-error
  "takes code as a string, and returns the error from evaulating it on the nREPL server, or nil"
  [inp-code]
  (msgs-to-error (trap-response inp-code)))

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
(expect "Attempted to use a string, but a number was expected.\n" (get-error "(+ 8 \"seventeen\")"))
(expect "Attempted to use a string, but a number was expected.\n" (get-error "(+ \"hello\" 3)"))
;(expect "Attempted to use a number, but a function was expected.\n" (get-error "(map 5 [3])"))

(expect "The arguments following the map or vector in assoc must come in pairs, but one of them does not have a match.\n" (get-error "(assoc {} 1 \"hello\" 2)"))

;(expect "\n" (get-error ""))
(expect "A function keyword can only take one or two arguments, but 3 were passed to it.\n" (get-error "(keyword \"hello\" \"goodbye\" \"hello\")"))

;(expect "Vectors added to a map must consist of two elements: a key and a value.\n" (get-error "(conj {} [1 1 1])"))

(expect "No value found for key :2. Every key for a hash-map must be followed by a value.\n" (get-error "(hash-map :1 1, :2)"))

(expect "A symbol cannot take 3 arguments.\n" (get-error "(symbol \"hello\" \"goodbye\" \"hello\")"))

(expect "Cannot call nil as a function.\n" (get-error "(nil)"))

(expect "You cannot use the same key in a hash-map twice, but you have duplicated the key 1.\n" (get-error "{1 1 1 1}"))

(expect "End of file, starting at line.\nProbably a non-closing parenthesis or bracket.\n" (get-error "(def elements-that-can-contain-simple-types #_=> #{:xs:attribute"))

(expect "Invalid number: 1.2.2.\n" (get-error "(+ 1.2.2 0)"))

(expect "You cannot use / in this position.\n" (get-error "(/string \"abcd\")"))

(expect "Parameters for cond must come in pairs, but one of them does not have a match.\n" (get-error "(cond (seq? [1 2]) 5 (seq? [1 3]))"))
(expect "Parameters for loop must come in pairs, but one of them does not have a match.\n" (get-error "(defn s [s] (loop [s]))"))

(expect "with-open, is a macro, cannot be passed to a function.\n" (get-error "(defn makeStructs [fName] with-open[r (reader (file fName))] (let [r res (doall (map makeStruct (line-seq r)))] (. r close) res))")) ;credit: https://stackoverflow.com/questions/5751262/cant-take-value-of-a-macro-clojure

(expect "% can only be followed by & or a number.\n" (get-error "(#(+ %a 1) 2 3)"))

(expect "Position 5 is outside of the string.\n" (get-error "(nth \"hello\" 5)"))

(expect "An index in a sequence is out of bounds or invalid.\n" (get-error "(nth (seq [1 2 3]) 5)"))

;(expect "An attempt to access a non-existing object (NullPointerException).\n" (get-error "(defn my-even [ilist] (if (= (mod (first ilist) 2) 0)(concat (list (first ilist)) (my-even (rest ilist)))(my-even (rest ilist)))) (my-even '(1,2,3,4,5))")) ;credit: https://stackoverflow.com/questions/7584337/clojure-nullpointerexception-error

(expect "Name clojure.hello is undefined.\n" (get-error "(clojure.hello/union #{1 2 3} #{3 4})"))

(expect "A hash map must consist of key/value pairs; you have a key that's missing a value.\n" (get-error "{:body {(str \"hello\")}}"))

(expect "hello cannot take 1 arguments.\n" (get-error "(defn hello [x y] (* x y)) (hello 1)"))

(expect "Too many arguments to if.\n" (get-error "(if (= 0 0) (+ 2 3) (+ 2 3) (+2 3))"))

(expect "Too few arguments to if.\n" (get-error "(if (= 0 0))"))

;; it doesn't look like we can run this test; works correctly in repl
#_(expect "Clojure ran out of memory, likely due to an infinite computation.\n" (get-error "(range)"))

;(expect "Attempted to use a string, but a number was expected" (get-error "(+ 8 \"seventeen\")"));;will not work until we write specs for core functions

;; NullPointerException with an object given
;; This might not be what we want (might need to process the object), but that's what it currently is
(expect #"(?s)An attempt to access a non-existing object:   java\.util\.regex\.Pattern\.<init> (.*) \(NullPointerException\)\."
        (get-error "(re-pattern nil)"))

(expect "Position -2 is outside of the string.\n" (get-error "(subs \"a\" 3)"))

;##### Spec Testing #####

(expect #"In function conj, the first argument is expected to be a collection, but is a function (\S*) instead.\n" (get-error "(conj even? \"a\")")) ;not the result we want but good for now
(expect #"In function map, the second argument is expected to be a collection, but is a function (\S*) instead.\n" (get-error "(map even? even?)")) ;not the result we want but good for now
(expect #"In function denominator, the first argument is expected to be a ratio, but is a function (\S*) instead.\n" (get-error "(denominator even?)")) ;not the result we want but good for now
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

(expect "conj can only take one or more arguments; recieved no arguments.\n" (get-error "(conj)"))
(expect "map can only take one or more arguments; recieved no arguments.\n" (get-error "(map)"))
(expect "rand-int can only take one argument; recieved no arguments.\n" (get-error "(rand-int)"))
(expect "rand-int can only take one argument; recieved three arguments.\n" (get-error "(rand-int 2 3 4)"))

(expect "denominator cannot take as few arguments as are currently in it, needs more arguments.\n" (get-error "(denominator)"))
(expect "denominator cannot take as many arguments as are currently in it, needs fewer arguments.\n" (get-error "(denominator 1/3 3)"))

;#################################################################
;############################## Nested errors ####################
;#################################################################

(expect "Tried to divide by zero\n" (get-error "(even? [(map #(/ % 0) [1 2])])"))

(expect "In function map, the first argument is expected to be a function, but is a number 2 instead.\n" (get-error "(even? [(map 2 [1 2])])"))
