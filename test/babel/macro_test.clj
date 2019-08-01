(ns babel.macro-test
  (:require
   [logs.utils :as log]
   [expectations :refer :all]))

;#########################################
;### Tests for macros                  ###
;#########################################

;; TO RUN tests, make sure you have repl started in a separate terminal
(expect "Syntax problems with (let((+ (# (* %1 3 ) ) 2 ) g )7 )):
In place of (+(# (* %1 3 ) ) 2 ) the following are allowed: a name or a vector or a hashmap"
(log/babel-test-message "(let [(+ #(* %1 3) 2) g] 7)"))

(expect "Syntax problems with (when-first(11 1 )):\nIn place of (11 1 ) the following are allowed: unknown type" (log/babel-test-message "(when-first 1 1 1)"))

(expect "Syntax problems with (let((+ x 1 ) g )g )):
In place of (+x 1 ) the following are allowed: a name or a vector or a hashmap"
(log/babel-test-message "(defn macro [s] (let [(+ x 1) g] g))"))

(expect "Expected a number, but a function was given instead." (log/babel-test-message "(let [g (+ #(* %1 3) 2)] 7)"))

(expect "Syntax problems with (let((+ 1 2 ) (+ 1 2 ) )(+ 1 2 ) )):
In place of (+1 2 ) the following are allowed: a name or a vector or a hashmap" (log/babel-test-message "(let [(+ 1 2) (+ 1 2)] (+ 1 2))"))
