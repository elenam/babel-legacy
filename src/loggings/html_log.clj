(ns loggings.html-log
  (:use hiccup.core))

;;counter atom that count the amount of testing units.
(def counter (atom {:total 1 :partial 1 :log? true}))

;;reset the counter
(defn- reset-counter
  []
  (def counter (atom {:total 1 :partial 1 :log? true})))

;;sets time with the file name format
(declare current-time)
(defn- update-time
  []
  (def current-time (.format (java.text.SimpleDateFormat. "MM'_'dd'_'yyyy'_T'HH'_'mm'_'ss") (new java.util.Date))))

;;preset html log contents
(defn- html-log-preset
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

               }
               function checkData() {
                   var nonNilResults = document.getElementsByClassName(\"nonNilResult\");
                   var nilResults = document.getElementsByClassName(\"nilResult\");
                   if (nonNilResults.length != 0 || nilResults.length != 0) {
                     document.getElementById(\"loadingError\").style.display=\"none\";
                   }

               }
               window.onload = checkData;"]
        [:p "Display options:"]
        [:div#displayOptions
         [:input#nil {:type "checkbox" :checked true :onclick "hidenils()"} [:a {:style "color:#808080;padding-right:20px"} "nil error"]]
         [:input#modified {:type "checkbox" :checked true :onclick "hideModified()"} [:a {:style "color:#00AE0C;padding-right:20px"}"modified error"]]
         [:input#original {:type "checkbox" :checked true :onclick "hideOriginal()"} [:a {:style "color:#D10101;padding-right:20px"} "original error"]]
         [:input#detail {:type "checkbox" :checked false :onclick "hideDetail()"} "error detail"]]
        [:div#loadingError {:style "display:block"}
         [:hr]
         [:h4 "Error loading test data!!!"]]))

;;adds a new log to the category
(defn- add-category
  [file-name]
  (html
    [:p
     [:a {:href (str "./history/" file-name ".html") :class "logFiles"} (str file-name ".html")]]))

;;returns html format content of existing logs
(defn- check-existing-log
  []
  (loop [dir (rest (file-seq (clojure.java.io/file "./log/history/")))
         coll [:body]]
    (let [target (first dir)]
      (if (empty? dir)
        coll
        (recur (rest dir)
               (conj coll (str "<p><a href=\"." (subs (str target) 5) "\" class=\"logFiles\"> "(subs (str target) 14)" </a></p>")))))))

;;category html page presetting
(defn- category-preset
  []
  (html
    [:title "Log Category"]
    [:h3 {:style "padding-top:10px"} "Log Category"]
    [:div
     [:h4 "File name"]
     [:hr]]
    (check-existing-log)))

;;makes the category html
(defn- make-category
  []
  (do
    (clojure.java.io/make-parents "./log/log_category.html")
    (spit "./log/log_category.html" (category-preset) :append false)))

;;start of txt and html test log, including preset up
(defn start-l
  []
  (do
    (update-time)
    (make-category)
    (spit "./log/log_category.html" (add-category current-time) :append true)
    (clojure.java.io/make-parents "./log/history/test_logs.html")
    (spit (str "./log/history/" current-time ".html") (html-log-preset) :append false)
    (spit "./log/last_test.txt" (str (new java.util.Date) "\n") :append false)))

;;sets html division for different test files
(defn- log-division
  [file-name]
  (html
    [:div {:class "fileDivision"}
     [:hr]
     [:h3 "<br />Test file: " file-name "<br /><br />"]]))

;;adds html division previously defined
(defn add-l
    [file-name]
    (do
      (swap! counter assoc :partial 1)
      (spit (str "./log/history/" current-time ".html") (log-division file-name) :append true)))

;;content that is going to be put into the log
(defn- log-content
  [inp-code total modified original]
  (if
    (not= modified nil)
    (str "#" total ":\n\n"
         "code input: " inp-code "\n\n"
         "modified error: " (clojure.string/trim-newline modified) "\n\n"
         "original error: " (clojure.string/trim-newline original) "\n\n\n")
    (str "#" total ":\n\n"
         "code input: " inp-code "\n\n"
         "modified error: nil\n\n"
         "original error: nil\n\n\n")))

;;saves the content into the txt log file
(defn save-log
  [inp-code total modified original]
  (spit "./log/last_test.txt" (log-content inp-code total modified original) :append true))

;;read the exsiting txt log content
;;this is disabled because it is removed from the middleware
#_(defn read-log
   []
   (println (slurp "./log/last_test.txt")))

;;define html content
(defn- html-content
  [inp-code total partial modified original detail]
  (if
    (not= modified nil)
    (html [:div {:class "nonNilResult"}
           [:hr]
           [:div
             [:p {:style "width:50%;float:left"} "#" partial ":<br />"]
             [:p {:style "width:50%;text-align:right;float:right"} total]]
           [:p {:style "color:#020793"} "code input: " inp-code "<br />"]
           [:p {:class "modifiedError" :style "color:#00AE0C"} "modified error: " (clojure.string/trim-newline modified) "<br />"]
           [:p {:class "originalError" :style "color:#D10101"} "original error: " (clojure.string/trim-newline original) "<br />"]
           [:p {:class "errorDetail" :style "display:none"} "error detail: " detail "<br /><br />"]])
    (html [:div {:class "nilResult"}
           [:hr]
           [:div
             [:p {:style "width:50%;float:left"} "#" partial ":<br />"]
             [:p {:style "width:50%;text-align:right;float:right"} total]]
           [:p {:style "color:#020793"} "code input: " inp-code "<br />"]
           [:p {:class "modifiedError" :style "color:#808080"} "modified error: nil<br />"]
           [:p {:class "originalError" :style "color:#808080"} "original error: nil<br />"]
           [:p {:class "errorDetail" :style "color:#808080;display:none"} "error detail: nil<br /><br />"]])))


;;write html content
(defn write-html
  [inp-code total partial modified origitnal detail]
  (spit (str "./log/history/" current-time ".html") (html-content inp-code total partial modified origitnal detail) :append true))
