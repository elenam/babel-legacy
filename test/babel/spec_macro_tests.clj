(ns babel.spec-macro-tests
  (:require
   [babel.error-tests :refer :all]
   [expectations :refer :all]))

;;you need to have launched a nREPL server in babel for these to work.
;;this must be the same port specified in project.clj

;############################################
;########## Testing for 'let' ###############
;############################################

(expect "Parameters for let must come in pairs, but one of them does not have a match.\n"
        (get-error "(defn hello [x] (let [y 2 z] (+ x y)))"))

(expect "Parameters for let must come in pairs, but one of them does not have a match.\n"
        (get-error "(defn hello [x] (let [y] (+ x y)))"))

;; I am not sure this is what we want, but this is how it currently works -Elena
(expect "Parameters for let must come in pairs, but one of them does not have a match.\n"
        (get-error " (let [[a b]] (+ a b))"))

;############################################
;#### Testing for 'let-like forms ###########
;############################################

(expect "Parameters for if-let must be a pair, but only one element is given.\n"
        (get-error "(if-let [x] x)"))

(expect "Parameters for if-let must be only one name and one value, but more parameters were given.\n"
        (get-error "(if-let [x 2 y] x)"))
