(ns babel.long-test
  (:require
   [expectations :refer :all]
   [babel.non-spec-test :refer [to-log?]]
   [logs.utils :as log]))

;#########################################
;### Time-consuming tests for which ######
;### testing may be skipped  #############
;#########################################

;; TO RUN tests, make sure you have repl started in a separate terminal

;; For long test make sure to keep timeout in utils.clj at 100000 or higher

(expect #(not= % nil) (log/set-log babel.non-spec-test/to-log?))

(expect nil (log/add-log
              (do
                (def file-name "this file")
                (:file (meta #'file-name)))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;; OutOfMemoryError ;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect "Clojure ran out of memory, likely due to an infinite computation or infinite recursion." (log/babel-test-message "(defn f[x] (f (inc x)))  (f 0)"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;; StackOverflowError ;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(expect "Clojure ran out of memory, likely due to an infinite computation." (log/babel-test-message "(defn f[s] (f (str (repeat 1000 s) (repeat 1000 s)))) (f \"fill up the memory!!!\")"))
