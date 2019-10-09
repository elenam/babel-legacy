(ns babel.macro-test
  (:require
   [logs.utils :as log]
   [babel.non-spec-test :refer [to-log?]]
   [expectations :refer :all]))

;#########################################
;### Tests for macros                  ###
;#########################################

;; TO RUN tests, make sure you have repl started in a separate terminal

(expect #(not= % nil) (log/set-log babel.non-spec-test/to-log?))

(expect nil (log/add-log
              (do
                (def file-name "this file")
                (:file (meta #'file-name)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;Syntax Problems;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect "Syntax problems with (let [(+ (#(* %1 3)) 2) g] 7):
In place of + (#(* %1 3)) 2 the following are allowed: a name or a vector or a hashmap"
(log/babel-test-message "(let [(+ #(* %1 3) 2) g] 7)"))

(expect "Syntax problems with (let [{:a 1} g] 7):
In place of {:a 1} the following are allowed: a name or a vector
In place of :a the following are allowed: a name or a vector or a hashmap
In place of 1 the following are allowed: a vector"
(log/babel-test-message " (let [{:a 1} g] 7)"))

(expect "Syntax problems with (when-first 1 1 1):
In place of 1 1 1 the following are allowed: unknown type" (log/babel-test-message "(when-first 1 1 1)"))

(expect "Syntax problems with (let [(+ x 1) g] g):
In place of + x 1 the following are allowed: a name or a vector or a hashmap"
(log/babel-test-message "(defn macro [s] (let [(+ x 1) g] g))"))

(expect "Syntax problems with (let [(+ 1 2) (+ 1 2)] (+ 1 2)):
In place of + 1 2 the following are allowed: a name or a vector or a hashmap" (log/babel-test-message "(let [(+ 1 2) (+ 1 2)] (+ 1 2))"))

(expect "Syntax problems with (let [[[3 4] [5 6]] y]):
In place of [3 4] [5 6] the following are allowed: a name or a hashmap" (log/babel-test-message "(let [[[3 4] [5 6]] y])"))

(expect "Syntax problems with (let [5 x]):
In place of 5 the following are allowed: a name or a vector or a hashmap" (log/babel-test-message "(let [5 x])"))

(expect "Syntax problems with (let [#{\"chromosome\" [\"x\"] \"y\"} z]):\nIn place of \"chromosome\" [\"x\"] \"y\" the following are allowed: a name or a vector or a hashmap"
(log/babel-test-message "(let [#{\"chromosome\" [\"x\"] \"y\"} z])"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;Extra Input;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect "if-let has too many parts here: (if-let[x false y true] \"then\" \"else\") The extra parts are: y true"
(log/babel-test-message "(if-let [x false y true] \"then\" \"else\")"))

(expect "if-let has too many parts here: (if-let[[w n] (re-find a(d+)x \"aaa123xxx\")] [w n] :not-found :x) The extra parts are: :x"
(log/babel-test-message "(if-let [[w n] (re-find #\"a(d+)x\" \"aaa123xxx\")] [w n] :not-found :x)"))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;Nested Error;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect "Expected a number, but a function was given instead." (log/babel-test-message "(let [g (+ #(* %1 3) 2)] 7)"))
