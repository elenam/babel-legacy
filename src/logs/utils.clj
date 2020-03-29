(ns logs.utils
  (:require
   [babel.middleware]
   [expectations :refer :all]
   [nrepl.core :as repl]
   [clojure.string :as s :refer [trim]]
   [nrepl.middleware.caught])
  (:use
   [logs.html-log]))

;;you need to have launched a nREPL server in babel for these to work.
;;this must be the same port specified in project.clj
;;you also need include hiccup dependency in project.clj
;;place (start-log) in the testing file to record testing time in the log

;;set server-port
(def server-port 7888)

;;gets the returning message from open repl
(defn trap-response
  "evals the code given as a string, and returns the list of associated nREPL messages"
  [code]
  (with-open [conn (repl/connect :port server-port)]
    (-> (repl/client conn 100000)
        (repl/message {:op :eval :code (str "(babel.middleware/setup-exc)" code)})
        doall)))

(defn get-error-parts
  "Takes the object returned by trap-response and separates it into different
  parts, returned as a map"
  [response]
  (let [err-response (:err (first (filter :err response)))
        err-str (or err-response "")
        [whole1 _ type1 at1 message1 line1 in1 _] (re-matches #"(?s)(\S+) error \((\S+)\) at (.*)\n(.*)\nLine: (\d*)\nIn: (.*)\n(.*)" err-str)
        [whole2 _ type2 at2 message2 _] (re-matches #"(?s)(\S+) error \((\S+)\) at (.*)\n(.+)\n(.*)" err-str)
        [whole3 _ at3 message3 _] (re-matches #"(?s)(\S+) error at (.*?)\n(.+)\n(.*)" err-str)
        type (or type1 type2)
        at (or at1 at2 at3)
        line line1
        in in1
        msg (or message1 message2 message3)
        message (if-not (nil? msg) (s/trim msg))]
      {:type type :at at :message (or message err-response) :line line :in in}))

(defn get-tracked-errors
  []
  (with-open [conn (repl/connect :port server-port)]
      (-> (repl/client conn 1000)
          (repl/message {:op :eval :code "(deref babel.middleware/track)"})
          doall
          first
          :value ; returns a string, not a map
          read-string)))

(defn reset-error-tracking
  "Resets the atom used for tracking errors."
  []
    (with-open [conn (repl/connect :port server-port)]
        (-> (repl/client conn 1000)
            (repl/message {:op :eval :code "(babel.middleware/reset-track)"}))))

(defn set-log
  "Sets the :log? value in the atom counter to b. This allows turning logging
  on and off"
  [b]
  (swap! counter assoc :log? b))

(defn write-log
  "Writes the log files."
  [info]
  (let [{:keys [modified original code trace]} info
        _ (swap! counter update-in [:total] inc)
        _ (swap! counter update-in [:partial] inc)]
    (write-html code (:total @counter) (:partial @counter) (str modified "\n" trace) original)))

(defn get-all-info
  "Executes code and returns a map with the error part of the response
  (separated into :type, :at, :message, :line, and :in fields - some may
  be nil) and the original repl error as :original :trace trace. Also adds the code
  itself as :code"
  [code]
  (let [_ (trap-response code)]
    (when (:log? @counter)
      (let [{:keys [message modified trace]} (get-tracked-errors)
            all-info (assoc {} :original message :modified modified :code code :trace trace)
            _ (reset-error-tracking)
            _ (when (:log? @counter) (write-log all-info))]
            all-info))))

(defn babel-test-message
  "Takes code as a string and returns the error message corresponding to the code
   or nil if there was no error"
  [code]
  (let [{:keys [modified trace]} (get-all-info code)]
        (if modified
            (str modified "\n" trace)
            nil)))

;;calls add-l from html-log
(defn add-log
  "takes a file name and inserts it to the log"
  [file-name]
  (when (:log? @counter)
      (add-l file-name)))

;;start of txt and html test log, including preset up
(defn start-log
  ""
  []
  (when (:log? @counter)
    (do
      (update-time)
      (reset-error-tracking)
      (make-category)
      (spit "./log/log_category.html" (add-category current-time) :append true)
      (clojure.java.io/make-parents "./log/history/test_logs.html")
      (spit (str "./log/history/" current-time ".html") (html-log-preset) :append false)
      (spit "./log/last_test.txt" (str (new java.util.Date) "\n") :append false))))
