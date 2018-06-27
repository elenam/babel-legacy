(ns babel.spec-macro-tests
  (:require
   [babel.error-tests :refer :all]
   [expectations :refer :all]))

;;you need to have launched a nREPL server in babel for these to work.
;;this must be the same port specified in project.clj

(expect "Parameters for let must come in pairs, but one of them does not have a match.\n"
        (get-error "(defn hello [x] (let [y 2 z] (+ x y)))"))

(expect "Parameters for let must come in pairs, but one of them does not have a match.\n"
        (get-error "(defn hello [x] (let [y] (+ x y)))"))

(expect "Parameters for if-let must be a pair, but only one element is given.\n"
        (get-error "(if-let [x] x)"))

(expect "Parameters for if-let must be only one name and one value, but more parameters were given.\n"
        (get-error "(if-let [x 2 y] x)"))
