(ns babel.spec-macro-tests
  (:require
   [expectations :refer :all])
  (:use
   [loggings.loggingtool :only [get-error start-log add-log]]))

;;you need to have launched a nREPL server in babel for these to work.
;;this must be the same port specified in project.clj

;############################################
;########## Testing for 'let' ###############
;############################################

;;start logging
(expect nil (add-log
              (do
                (def file-name "this file")
                (:file (meta #'file-name)))))

(expect "Parameters for let must come in pairs, but one of them does not have a match.\n"
        (get-error "(defn hello [x] (let [y 2 z] (+ x y)))"))

(expect "Parameters for let must come in pairs, but one of them does not have a match.\n"
        (get-error "(defn hello [x] (let [y] (+ x y)))"))

;; I am not sure this is what we want, but this is how it currently works -Elena
(expect "Parameters for let must come in pairs, but one of them does not have a match.\n"
        (get-error " (let [[a b]] (+ a b))"))

(expect "Parameters for let require a vector, but a was given instead.\n"
        (get-error " (let a (+ a 2))"))

;############################################
;#### Testing for 'let-like forms ###########
;############################################

(expect "Parameters for if-let must be a pair, but only one element is given.\n"
        (get-error "(if-let [x] x)"))

(expect "Parameters for if-let must be only one name and one value, but more parameters were given.\n"
        (get-error "(if-let [x 2 y] x)"))

(expect "In let 2 is used instead of a variable name.\n"
        (get-error "(let [2 3] 8)"))

;############################################
;#### Testing for 'defn' ###########
;############################################

(expect "In defn [b c] is used instead of a function name.\n"
        (get-error "(defn [b c] (+ 4 3))"))

(expect "An argument for defn required a vector, but x was given instead.\n"
        (get-error "(defn afunc2 x (+ 3 x))"))

(expect "In defn- [b c] is used instead of a function name.\n"
        (get-error "(defn- [b c] (+ 4 3))"))

(expect "An argument for defn- required a vector, but x was given instead.\n"
        (get-error "(defn- afunc2 x (+ 3 x))"))

;############################################
;#### Testing for 'fn' ###########
;############################################
(expect "An argument for fn required a vector, but VARIABLE-NAME was given instead.\n"
        (get-error "(map (fn fn-name1 VARIABLE-NAME (* 4 VARIABLE-NAME)) (range 1 10))"))

(expect "An argument for fn required a vector, but VARIABLE-NAME was given instead.\n"
        (get-error "(map (fn VARIABLE-NAME (* 4 VARIABLE-NAME)) (range 1 10))"))

(expect "An argument for fn required a vector, but no vector was passed.\n"
        (get-error "(map (fn (* 4 VARIABLE-NAME)) (range 1 10))"))

(expect "An argument for fn required a vector, but p was given instead.\n"
        (get-error "(let [x 7] (fn [r] (fn p (+ p p))))"))

;############################################
;#### Testing for 'if-some' ###########
;############################################

(expect "Parameters for if-some must come in pairs, but one of them does not have a match.\n"
        (get-error "(if-some [[a b]] (+ a b) (+ b a))"))

(expect "Parameters for if-some require a vector, but a was given instead.\n"
        (get-error "(if-some a (+ a 2) (+ 2 a))"))

(expect "Parameters for if-some require a vector, but a was given instead.\n"
        (get-error "(if-some a (+ a 2))"))

(expect "if-some can only take two or three arguments; recieved one argument.\n"
        (get-error "(if-some [a 2])"))
