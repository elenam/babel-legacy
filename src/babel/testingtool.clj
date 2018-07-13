(ns babel.testingtool
  (:require
   [expectations :refer :all]
   [clojure.tools.nrepl :as repl]
   [babel.processor :as processor])
  (:use
   [hiccup.core]))

;;you need to have launched a nREPL server in babel for these to work.
;;this must be the same port specified in project.clj
;;you also need include hiccup dependency in project.clj
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


;;preset html environment
(defn html-log-preset
  []
  (html [:title "Babel testing log"]
        [:h3 {:style "padding-top:20px"} "Testing log: "]
        [:h4 (new java.util.Date)]
        [:script
         "function hideModified() {
           var x = document.getElementsByClassName(\"modifiedError\");
           if (document.getElementById(\"modified\").checked != true) {
             var i;
             for (i = 0; i < x.length; i++) {
               x[i].style.display='none';
             }
             } else {
             var i;
             for (i = 0; i < x.length; i++) {
               x[i].style.display='block';
             }
             }
           }
           function hideOriginal() {
             var x = document.getElementsByClassName(\"originalError\");
             if (document.getElementById(\"original\").checked != true) {
               var i;
               for (i = 0; i < x.length; i++) {
                 x[i].style.display='none';
               }
               } else {
               var i;
               for (i = 0; i < x.length; i++) {
                 x[i].style.display='block';
               }
               }
             }
             function hideDetail() {
               var x = document.getElementsByClassName(\"errorDetail\");
               if (document.getElementById(\"detail\").checked != true) {
                 var i;
                 for (i = 0; i < x.length; i++) {
                   x[i].style.display='none';
                 }
                 } else {
                 var i;
                 for (i = 0; i < x.length; i++) {
                   x[i].style.display='block';
                 }
                 }
               }"]
        [:p "Display options:"]
        [:div
         [:input#modified {:type "checkbox" :checked true :onclick "hideModified()"} [:a {:style "color:#00AE0C;padding-right:20px"}"modified error"]]
         [:input#original {:type "checkbox" :checked true :onclick "hideOriginal()"} [:a {:style "color:#D10101;padding-right:20px"} "original error"]]
         [:input#detail {:type "checkbox" :checked false :onclick "hideDetail()"} "error detail"]]))

;;add date to the txt and html test log
(defn start-log
  []
  (do
    (spit "./doc/test_log.html" (html-log-preset) :append false)
    (spit "./doc/test_log.txt" (str (new java.util.Date) "\n")) :append true))

;;get original error msg by key
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

;;save the content into the txt log file
(defn save-log
  [inp-code]
  (spit "./doc/test_log.txt" (log-content inp-code) :append true))

;;read the exsiting txt log content
(defn read-log
  []
  (println (slurp "./doc/test_log.txt")))

;;html content
(defn html-content
  [inp-code]
  (if
    (not= (msgs-to-error (trap-response inp-code)) nil)
    (html [:div
           [:hr]
           [:p "#" (:total @counter) ":<br />"]
           [:p {:style "color:#020793"} "code input: " inp-code "<br />"]
           [:p {:class "modifiedError" :style "color:#00AE0C"} "modified error: " (clojure.string/trim-newline (msgs-to-error (trap-response inp-code))) "<br />"]
           [:p {:class "originalError" :style "color:#D10101"} "original error: " (clojure.string/trim-newline (get-original-error-by-key :msg)) "<br />"]
           [:p {:class "errorDetail" :style "display:none"} "error detail: "(clojure.string/trim-newline (get-original-error-by-key :detail)) "<br /><br />"]])
    (html [:div
           [:hr]
           [:p "#" (:total @counter) ":<br />"]
           [:p {:style "color:#020793"} "code input: " inp-code "<br />"]
           [:p {:class "modifiedError" :style "color:#00AE0C"} "modified error: nil<br />"]
           [:p {:class "originalError" :style "color:#D10101"} "original error: nil<br />"]
           [:p {:class "errorDetail" :style "display:none"} "error detail: nil<br /><br />"]])))

;;write html content
(defn write-html
  [inp-code]
  (spit "./doc/test_log.html" (html-content inp-code) :append true))

;;the execution funtion for the tests
(defn get-error
  [inp-code]
  (do
    (save-log inp-code)
    (write-html inp-code)
    (processor/reset-recorder)
    (record-error inp-code)))

(println "babel.testingtool loaded")
