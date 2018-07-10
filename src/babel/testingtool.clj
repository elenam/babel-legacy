(ns babel.testingtool
  (:require
   [expectations :refer :all]
   [clojure.tools.nrepl :as repl]))

;;you need to have launched a nREPL server in babel for these to work.
;;this must be the same port specified in project.clj

;;you also need to have a (\babel>lein repl :start :port 8888) for the original messages
;;and have (start-log) in the testing file to record testing time in the log

;;set a pair of server-ports, one is through our middleware and one is not
(def server-port1 7888)

(def server-port2 8888)

;;message saver atom
(def counter (atom {:total 1}))

;;reset atom
(defn reset-counter
  []
  (def counter (atom {:total 1})))

;;a pair of trap-responses gets the returning message from 2 open repl
(defn trap-response-port1
  "evals the code given as a string, and returns the list of associated nREPL messages"
  [inp-code]
  (with-open [conn (repl/connect :port server-port1)]
    (-> (repl/client conn 1000)
        (repl/message {:op :eval :code inp-code})
        doall)))

(defn trap-response-port2
  "evals the code given as a string, and returns the list of associated nREPL messages"
  [inp-code]
  (with-open [conn (repl/connect :port server-port2)]
    (-> (repl/client conn 1000)
        (repl/message {:op :eval :code inp-code})
        doall)))

;;takes the response and returns only the error message
(defn msgs-to-error
  "takes a list of messages and returns nil if no :err is present, or the first present :err value"
  [list-of-messages]
  (:err (first (filter :err list-of-messages))))

;;execution funtion that takes a string and return its error message if applied
(defn record-error
  "takes code as a string, and returns the error from evaulating it on the nREPL server, or nil"
  [inp-code]
  (swap! counter update-in [:total] inc)
  (msgs-to-error (trap-response-port1 inp-code)))

;;add date to the test log
(defn start-log
  []
  (spit "./doc/test_log.txt" (str (new java.util.Date) "\n")) :append true)

;;content that is going to be put into the log
(defn log-content
  [inp-code]
  (if
    (not= (msgs-to-error (trap-response-port1 inp-code)) nil)
    (str "#" (:total @counter) ":\n"
         "code input: " inp-code "\n"
         "modified error: " (clojure.string/trim-newline (msgs-to-error (trap-response-port1 inp-code))) "\n"
         "original error: " (clojure.string/trim-newline (msgs-to-error (trap-response-port2 inp-code))) "\n\n")
    (str "#" (:total @counter) ":\n"
         "code input: " inp-code "\n"
         "modified error: nil\n"
         "original error: nil\n")))

;;save the content into the log file
(defn save-log
  [inp-code]
  (spit "./doc/test_log.txt" (log-content inp-code) :append true))

;;read the exsiting log content
(defn read-log
  []
  (println (slurp "./doc/test_log.txt")))

;;the execution funtion for the tests
(defn get-error
  [inp-code]
  (do
    (save-log inp-code)
    (record-error inp-code)))

(println "babel.testingtool loaded")
