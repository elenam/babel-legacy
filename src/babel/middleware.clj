(ns babel.middleware
  (:require [babel.processor :as processor]
            [errors.dictionaries :as d]
            [errors.prettify-exception :as p-exc]
            [errors.messageobj :as msg-o]
            [nrepl.middleware]
            [nrepl.middleware.caught]
            [clojure.repl]
            [clojure.main :as cm :refer [ex-str ex-triage]]
            [clojure.string :as s :refer [trim]])
  (:import nrepl.transport.Transport)
  (:gen-class))

(def track (atom {})) ; for debugging purposes

(defn interceptor
  "applies processor/modify-errors to every response that emerges from the server"
  [handler]
  (fn [inp-message]
    (let [transport (inp-message :transport)
          sess (inp-message :session)]
          ;dummy (reset! track {:session sess})]
      (handler (assoc inp-message :transport
                      (reify Transport
                        (recv [this] (.recv transport))
                        (recv [this timeout] (.recv transport timeout))
                        (send [this msg]     (.send transport msg))))))))

;;sets the appropriate flags on the middleware so it is placed correctly
(nrepl.middleware/set-descriptor! #'interceptor
                                                {:expects #{"eval"} :requires #{} :handles {}})

;; For now we are just recreating ArityException. We would need to manually replace it by a processed exception
(defn- process-arity-exception
  "Takes a message from arity exception and forms a new exception"
  [msg]
  (let [[_ howmany fname] (re-matches #"Wrong number of args \((\S*)\) passed to: (\S*)" msg)]
       (clojure.lang.ArityException. (Integer/parseInt howmany) (d/get-function-name fname))))

(defn- record-message
  [e]
  (cm/ex-str (cm/ex-triage (Throwable->map e))))

(defn- modify-message
  [exc]
  (let [exc-type (class exc)
        {:keys [via data cause]} (Throwable->map exc)
        nested? (> (count via) 1)
        {:keys [type message]} (last via)
        exc-info? (= clojure.lang.ExceptionInfo exc-type)
        compiler-exc? (= clojure.lang.Compiler$CompilerException exc-type)]
        (cond (and exc-info? (not nested?))
                   (str (processor/spec-message data) "\n" (processor/location-function-spec data))
              (and nested? compiler-exc? (processor/macro-spec? cause via))
                   (str (processor/spec-macro-message exc) (processor/location-macro-spec via))
              :else (processor/process-message type message))))

;; I don't seem to be able to bind this var in middleware.
;; Running (setup-exc) in repl does the trick.
(defn setup-exc []
  (set! nrepl.middleware.caught/*caught-fn* #(do
    (let [modified (modify-message %)
          _ (reset! track {:message (record-message %) :modified modified})] ; for logging
    (println modified)))))

(defn reset-track [](reset! track {}))
