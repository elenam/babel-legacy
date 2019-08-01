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
    (-> (repl/client conn 1000)
    ;; Note: adding deref may be an issue if the code has a syntax error
        (repl/message {:op :eval :code (str "(babel.middleware/setup-exc)" code)})
        doall)))

(defn get-error-parts
  "Takes the object returned by trap-response and separates it into different
  parts, returned as a map"
  [response]
  (let [err-str (:err (second response))
        match1 (re-matches #"(?s)(\S+) error \((\S+)\) at (.*)\n(.*)\nLine: (\d*)\nIn: (.*)\n(.*)" err-str)
        matches (or match1 (re-matches #"(?s)(\S+) error at (.*?)\n(.+)\n(.*)" err-str))
        n (if match1 2 1)
        type (get matches n)
        at (get matches (+ n 1))
        message (s/trim (get matches (+ n 2)))
        line (get matches (+ n 3))
        in (get matches (+ n 4))]
      {:type type :at at :message message :line line :in in}))

(defn get-original-error
  ""
  []
    (with-open [conn (repl/connect :port server-port)]
        (-> (repl/client conn 1000)
            (repl/message {:op :eval :code "(:message (deref babel.middleware/track))"})
            doall
            first
            :value)))

;; TODO: Add logging
(defn get-all-info
  "Executes code and returns a map with the error part of the response
  (separated into :type, :at, :message, :line, and :in fields - some may
  be nil) and the original repl error as :original. Also adds the code
  itself as :code"
  [code]
  (let [modified-msg (get-error-parts (trap-response code))
        original-msg (get-original-error)
        all-info (assoc modified-msg :original original-msg :code code)
        _ (when (:log? @counter) (write-log all-info))]
    all-info))

(defn babel-test-message
  "Takes code as a string and returns the error message corresponding to the code
   or nil if there was no error"
  [code]
  (:message (get-all-info code)))


;;takes a string and return its error message if applied, also adds counter atom
; (defn record-error
;   "takes code as a string, and returns the error from evaulating it on the nREPL server, or nil"
;   [inp-code]
;   (swap! counter update-in [:total] inc)
;   (swap! counter update-in [:partial] inc)
;   (first (msgs-to-error (trap-response inp-code))))
;
; ;;theses 4 funtions get specific error msg from repl
; (defn- get-modified-error
;   [inp-code]
;   (let [errors (msgs-to-error (trap-response inp-code))]
;     (if (nil? (first errors))
;         nil
;         (loop [errs errors
;                coll ["<br />&nbsp;"]]
;             (if (nil? (first errs))
;               (clojure.string/join "<br />&nbsp;" coll)
;               (recur (rest errs)
;                      (conj coll (str "\"" (first errs) "\""))))))))
;
; ;;get original error msg by key
; (defn- get-original-error-by-key
;   [key]
;   (:value (first (filter :value (trap-response (str "(" key " @babel.processor/recorder))"))))))
;
; (defn- get-original-error
;   []
;   (get-original-error-by-key :msg))
;
; (defn- get-error-detail
;   []
;   (get-original-error-by-key :detail))
;
; ;;this function triggers the babel.processor/reset-recorder
; (defn- reset-recorder
;   []
;   (trap-response (str "(babel.processor/reset-recorder)")))
;
; ;;the execution function for the tests
; (defn get-error
;   "takes a testing expr and return its modified error message"
;   [inp-code]
;   (do
;     (if (= (:log? @counter) true)
;       (do
;         (reset-recorder)
;         (let [modified (get-modified-error inp-code)
;               original (get-original-error)]
;           (do
;             (save-log
;               inp-code
;               (:total @counter)
;               modified
;               original)
;             (write-html
;               inp-code
;               (:total @counter)
;               (:partial @counter)
;               modified
;               original
;               (get-error-detail)))))
;         nil)
;     (record-error inp-code)))
;
;
;
; ;;calls start-l from html-log
; (defn start-log
;   "used to create log file"
;   [boo]
;   (cond (= boo false) (do-log false)
;         (= boo true) (do
;                     (do-log true)
;                     (start-l))
;         :else (start-l)))
;
; ;;calls add-l from html-log
; (defn add-log
;   "takes a file name and inserts it to the log"
;   [file-name]
;   (if (= (:log? @counter) true)
;       (add-l file-name)))
