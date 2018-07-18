(ns babel.testingtool
  (:require
   [expectations :refer :all]
   [clojure.tools.nrepl :as repl]
   [babel.processor :as processor])
  (:use
   [babel.html-log]))

;;you need to have launched a nREPL server in babel for these to work.
;;this must be the same port specified in project.clj
;;you also need include hiccup dependency in project.clj
;;place (start-log) in the testing file to record testing time in the log

;;set server-port
(def server-port 7888)

;;gets the returning message from open repl
(defn trap-response
  "evals the code given as a string, and returns the list of associated nREPL messages"
  [inp-code]
  (with-open [conn (repl/connect :port server-port)]
    (-> (repl/client conn 1000)
        (repl/message {:op :eval :code inp-code})
        doall)))

;;takes the response and returns only the error message
(defn msgs-to-error
  "takes a list of messages and returns nil if no :err is present, or the first present :err value"
  [list-of-messages]
  (:err (first (filter :err list-of-messages))))

;;takes a string and return its error message if applied, also adds counter atom
(defn record-error
  "takes code as a string, and returns the error from evaulating it on the nREPL server, or nil"
  [inp-code]
  (swap! counter update-in [:total] inc)
  (swap! counter update-in [:partial] inc)
  (msgs-to-error (trap-response inp-code)))


;;theses 4 funtions get specific error msg from repl
(defn get-modified-error
  [inp-code]
  (msgs-to-error (trap-response inp-code)))

;;get original error msg by key
(defn get-original-error-by-key
  [key]
  (:value (first (filter :value (trap-response (str "(" key " @babel.processor/recorder)"))))))

(defn get-original-error
  [inp-code]
  (get-original-error-by-key :msg))

(defn get-error-detail
  [inp-code]
  (get-original-error-by-key :detail))

;;the execution funtion for the tests
(defn get-error
  [inp-code]
  (do
    (save-log
      inp-code
      (:total @counter)
      (get-modified-error inp-code)
      (get-original-error inp-code))
    (write-html
      inp-code
      (:total @counter)
      (:partial @counter)
      (get-modified-error inp-code)
      (get-original-error inp-code)
      (get-error-detail inp-code))
    (processor/reset-recorder)
    (record-error inp-code)))

;;calls start-l from html-log
(defn start-log
  []
  (start-l))

;;calls add-l from html-log
(defn add-log
  [file-name]
  (add-l file-name))

(println "babel.testingtool loaded")
