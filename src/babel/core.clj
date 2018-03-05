(ns babel.core
(:require [clojure.tools.nrepl :as repl]
         [clojure.spec.alpha :as s]
         [clojure.spec.test.alpha :as stest]
         [clojure.tools.nrepl.transport :as t]
         [clojure.tools.nrepl.middleware.pr-values :as prv])
(:import clojure.tools.nrepl.transport.Transport))


(def last-message-rollover (atom nil))
(def last-message (atom nil))
(def last-response (atom nil))
(def last-response-rollover (atom nil))
(def touch-errors (atom false))
(def last-error (atom false))

(defn modify-errors "takes a nREPL response, and returns a message with the errors fixed"
  [inp-message]
    (if (contains? inp-message :err)
          (assoc inp-message :err (str "\nerr: " (class (eval *e)) "\n"))
        inp-message))

(defn instrument-after-each
  [handler]
  (fn [inp-message]
    (let [transport (inp-message :transport)]
      (do
        (handler (assoc inp-message :transport
          (reify Transport
            (recv [this] (.recv transport))
            (recv [this timeout] (.recv transport timeout))
            (send [this msg] (do
              (.send transport (modify-errors msg)))))))))))

(defn ex-trap []
  (try (/ 8 0)
      (catch Exception e (identity e))))



#_(clojure.tools.nrepl.middleware/set-descriptor! #'instrument-after-each
        {:expects #{} :requires #{prv/pr-values} :handles {}})


;;the below are just debug things
(defn blipper
  [inp]
  "blip")

(s/fdef blipper
  :args string?)

(prn "You have loaded babel.core")
