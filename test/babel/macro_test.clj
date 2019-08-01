(ns babel.macro-test
  (:require
   [logs.utils :as log]
   [expectations :refer :all]))

;#########################################
;### Tests for macros                  ###
;#########################################

;; TO RUN tests, make sure you have repl started in a separate terminal

(expect nil (log/add-log
              (do
                (def file-name "this file")
                (:file (meta #'file-name)))))

(expect "Syntax problems with (let [(+ (fn* [p1__1196#] (* p1__1196# 3)) 2) g] 7):
In place of (+(# (* %1 3 ) ) 2 ) the following are allowed: a name or a vector or a hashmap
" (log/babel-test-message "(let [(+ #(* %1 3) 2) g] 7)"))
