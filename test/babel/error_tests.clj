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
;(expect "Attempted to use a string, but a number was expected" (get-error "(+ 8 \"seventeen\")"));;will not work until we write specs for core functions

;; NullPointerException with an object given
;; This might not be what we want (might need to process the object), but that's what it currently is
(expect #"(?s)An attempt to access a non-existing object:   java\.util\.regex\.Pattern\.<init> \(Pattern\.java:1336\)(.*) \(NullPointerException\)\."
        (get-error "(re-pattern nil)"))
