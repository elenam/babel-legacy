(ns babel.testingtool
  (:require
   [expectations :refer :all]
   [clojure.tools.nrepl :as repl]
   [babel.processor :as processor])
  (:use
   [hiccup.core]))

;;you need to have launched a nREPL server in babel for these to work.
;;this must be the same port specified in project.clj
;;place (start-log) in the testing file to record testing time in the log

;;set server-port
(def server-port 7888)

;;counter atom
(def counter (atom {:total 1}))

;;reset atom
(defn reset-counter
  []
  (def counter (atom {:total 1})))

;;trap-response gets the returning message from open repl
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

;;execution funtion that takes a string and return its error message if applied
(defn record-error
  "takes code as a string, and returns the error from evaulating it on the nREPL server, or nil"
  [inp-code]
  (swap! counter update-in [:total] inc)
  (msgs-to-error (trap-response inp-code)))

(defn html-log-preset
  []
  (html [:title "Test_log"] [:h3 "Test_log: "] [:h4 (new java.util.Date)]))


;;add date to the test log
(defn start-log
  []
  (do
    (spit "./doc/testhtml.html" (html-log-preset) :append false)
    (spit "./doc/test_log.txt" (str (new java.util.Date) "\n")) :append true))

;;get original error msg
(defn get-original-error-by-key
  [key]
  (:value (first (filter :value (trap-response (str "(" key " @babel.processor/recorder)"))))))

;;content that is going to be put into the log
(defn log-content
  [inp-code]
  (if
    (not= (msgs-to-error (trap-response inp-code)) nil)
    (str "#" (:total @counter) ":\n\n"
         "code input: " inp-code "\n\n"
         "modified error: " (clojure.string/trim-newline (msgs-to-error (trap-response inp-code))) "\n\n"
         "original error: " (clojure.string/trim-newline (get-original-error-by-key :msg)) "\n\n"
         "error detail: "(clojure.string/trim-newline (get-original-error-by-key :detail)) "\n\n\n")
    (str "#" (:total @counter) ":\n\n"
         "code input: " inp-code "\n\n"
         "modified error: nil\n\n"
         "original error: nil\n\n"
         "error detail: nil\n\n\n")))

;;save the content into the log file
(defn save-log
  [inp-code]
  (spit "./doc/test_log.txt" (log-content inp-code) :append true))

;;read the exsiting log content
(defn read-log
  []
  (println (slurp "./doc/test_log.txt")))

(defn html-content
  [inp-code]
  (if
    (not= (msgs-to-error (trap-response inp-code)) nil)
    (html [:p "#" (:total @counter) ":<br />"
               "code input: " inp-code "<br />"
               "modified error: " (clojure.string/trim-newline (msgs-to-error (trap-response inp-code))) "<br />"
               "original error: " (clojure.string/trim-newline (get-original-error-by-key :msg)) "<br />"
               "error detail: "(clojure.string/trim-newline (get-original-error-by-key :detail)) "<br /><br />"])
    (html [:p "#" (:total @counter) ":<br />"
               "code input: " inp-code "<br />"
               "modified error: nil<br />"
               "original error: nil<br />"
               "error detail: nil<br /><br />"])))

(defn write-html
  [inp-code]
  (spit "./doc/testhtml.html" (html-content inp-code) :append true))

;;the execution funtion for the tests
(defn get-error
  [inp-code]
  (do
    (save-log inp-code)
    (write-html inp-code)
    (processor/reset-recorder)
    (record-error inp-code)))

(println "babel.testingtool loaded")
