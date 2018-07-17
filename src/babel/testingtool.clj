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

;;--funtionality-- set server-port
(def server-port 7888)


;;--logging-- set time with the file name format
(declare current-time)

(defn update-time
  []
  (def current-time (.format (java.text.SimpleDateFormat. "MM'_'dd'_'yyyy'_T'HH'_'mm'_'ss") (new java.util.Date))))

;;--logging-- counter atom
(def counter (atom {:total 1 :partial 1}))

;;--logging-- reset atom
(defn reset-counter
  []
  (def counter (atom {:total 1 :partial 1})))

;;--funtionality-- gets the returning message from open repl
(defn trap-response
  "evals the code given as a string, and returns the list of associated nREPL messages"
  [inp-code]
  (with-open [conn (repl/connect :port server-port)]
    (-> (repl/client conn 1000)
        (repl/message {:op :eval :code inp-code})
        doall)))

;;--funtionality-- takes the response and returns only the error message
(defn msgs-to-error
  "takes a list of messages and returns nil if no :err is present, or the first present :err value"
  [list-of-messages]
  (:err (first (filter :err list-of-messages))))

;;--funtionality-- takes a string and return its error message if applied, also adds counter atom
(defn record-error
  "takes code as a string, and returns the error from evaulating it on the nREPL server, or nil"
  [inp-code]
  (swap! counter update-in [:total] inc)
  (swap! counter update-in [:partial] inc)
  (msgs-to-error (trap-response inp-code)))

;;--logging-- preset html log contents
(defn html-log-preset
  []
  (html [:title "Babel testing log"]
        [:h3 {:style "padding-top:10px"} "Testing log : "]
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

               }
               function hidenils() {
                 var x = document.getElementsByClassName(\"nilResult\");
                 if (document.getElementById(\"nil\").checked != true) {
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
        [:div#displayOptions
         [:input#nil {:type "checkbox" :checked true :onclick "hidenils()"} [:a {:style "color:#808080;padding-right:20px"} "nil error"]]
         [:input#modified {:type "checkbox" :checked true :onclick "hideModified()"} [:a {:style "color:#00AE0C;padding-right:20px"}"modified error"]]
         [:input#original {:type "checkbox" :checked true :onclick "hideOriginal()"} [:a {:style "color:#D10101;padding-right:20px"} "original error"]]
         [:input#detail {:type "checkbox" :checked false :onclick "hideDetail()"} "error detail"]]))

;;--logging-- set html division for different test files
(defn log-division
  [file-name]
  (html
    [:div {:class "fileDivision"}
     [:hr]
     [:h3 "<br />Test file: " file-name "<br /><br />"]]))

;;--logging-- start of txt and html test log, including preset up
(defn start-log
  []
  (do
    (update-time)
    (spit (str "./log/history/" current-time ".html") (html-log-preset) :append false)
    (spit "./log/last_test.txt" (str (new java.util.Date) "\n") :append false)))

;;--logging-- add html division previously defined
(defn add-log
    [file-name]
    (do
      (swap! counter assoc :partial 1)
      (spit (str "./log/history/" current-time ".html") (log-division file-name) :append true)))

;;--funtionality-- get original error msg by key
(defn get-original-error-by-key
  [key]
  (:value (first (filter :value (trap-response (str "(" key " @babel.processor/recorder)"))))))

;;--logging-- content that is going to be put into the log
(defn log-content
  [inp-code]
  (if
    (not= (msgs-to-error (trap-response inp-code)) nil)
    (str "#" (:total @counter) ":\n\n"
         "code input: " inp-code "\n\n"
         "modified error: " (clojure.string/trim-newline (msgs-to-error (trap-response inp-code))) "\n\n"
         "original error: " (clojure.string/trim-newline (get-original-error-by-key :msg)) "\n\n\n")
    (str "#" (:total @counter) ":\n\n"
         "code input: " inp-code "\n\n"
         "modified error: nil\n\n"
         "original error: nil\n\n\n")))

;;--logging-- save the content into the txt log file
(defn save-log
  [inp-code]
  (spit "./log/last_test.txt" (log-content inp-code) :append true))

;;--logging-- read the exsiting txt log content
(defn read-log
  []
  (println (slurp "./log/last_test.txt")))

;;--logging-- define html content
(defn html-content
  [inp-code]
  (if
    (not= (msgs-to-error (trap-response inp-code)) nil)
    (html [:div {:class "nonNilResult"}
           [:hr]
           [:div
             [:p {:style "width:50%;float:left"} "#" (:partial @counter) ":<br />"]
             [:p {:style "width:50%;text-align:right;float:right"} (:total @counter)]]
           [:p {:style "color:#020793"} "code input: " inp-code "<br />"]
           [:p {:class "modifiedError" :style "color:#00AE0C"} "modified error: " (clojure.string/trim-newline (msgs-to-error (trap-response inp-code))) "<br />"]
           [:p {:class "originalError" :style "color:#D10101"} "original error: " (clojure.string/trim-newline (get-original-error-by-key :msg)) "<br />"]
           [:p {:class "errorDetail" :style "display:none"} "error detail: "(clojure.string/trim-newline (get-original-error-by-key :detail)) "<br /><br />"]])
    (html [:div {:class "nilResult"}
           [:hr]
           [:div
             [:p {:style "width:50%;float:left"} "#" (:partial @counter) ":<br />"]
             [:p {:style "width:50%;text-align:right;float:right"} (:total @counter)]]
           [:p {:style "color:#020793"} "code input: " inp-code "<br />"]
           [:p {:class "modifiedError" :style "color:#808080"} "modified error: nil<br />"]
           [:p {:class "originalError" :style "color:#808080"} "original error: nil<br />"]
           [:p {:class "errorDetail" :style "color:#808080;display:none"} "error detail: nil<br /><br />"]])))


;;--logging-- write html content
(defn write-html
  [inp-code]
  (spit (str "./log/history/" current-time ".html") (html-content inp-code) :append true))

;;--funtionality-- the execution funtion for the tests
(defn get-error
  [inp-code]
  (do
    (save-log inp-code)
    (write-html inp-code)
    (processor/reset-recorder)
    (record-error inp-code)))

(println "babel.testingtool loaded")
