(ns babel.middleware
  (:require [babel.processor :as processor]
            [nrepl.middleware]
            [nrepl.middleware.caught]
            [clojure.repl]
            [clojure.main :as cm])
  (:import nrepl.transport.Transport)
  (:gen-class))

(def track (atom {})) ; for debugging purposes

(defn interceptor
  "applies processor/modify-errors to every response that emerges from the server"
  [handler]
  (fn [inp-message]
    (let [transport (inp-message :transport)]
          ;dummy (reset! track {:session sess})]
      (handler (assoc inp-message :transport
                      (reify Transport
                        (recv [_this] (.recv transport))
                        (recv [_this timeout] (.recv transport timeout))
                        (send [_this msg]     (.send transport msg))))))))

;; sets the appropriate flags on the middleware so it is placed correctly
(nrepl.middleware/set-descriptor! #'interceptor
                                                {:expects #{"eval"} :requires #{} :handles {}})

(defn- record-message
  [e]
  (cm/ex-str (cm/ex-triage (Throwable->map e))))

(defn- modify-message
  "TODO: Write some great docstring explaining what all of this does."
  [exc]
  (let [exc-type (class exc)
        {:keys [cause data via trace]} (Throwable->map exc)
        nested? (> (count via) 1)
        {:keys [type message]} (last via)
        phase (:clojure.error/phase (:data (first via)))
        exc-info? (= clojure.lang.ExceptionInfo exc-type)
        compiler-exc? (= clojure.lang.Compiler$CompilerException exc-type)]
        (cond (and nested? compiler-exc? (processor/macro-spec? cause via))
                   (str (processor/spec-macro-message cause data)
                        "\n"
                        (processor/location-macro-spec via))
              (and nested? compiler-exc? (processor/invalid-signature? cause via))
                   (str (processor/invalid-sig-message cause
                                                       (:clojure.error/symbol (:data (first via))))
                        "\n"
                        (processor/location-macro-spec via))
              (or (and exc-info? (not nested?))
                  (and compiler-exc? (= clojure.lang.ExceptionInfo (resolve type))))
                  (str (processor/spec-message data)
                       "\n"
                       (processor/location-function-spec data))
              (and exc-info? (= clojure.lang.ExceptionInfo (resolve type)))
                  (str (processor/spec-message data)
                       "\n"
                       (processor/location-print-phase-spec data))
              ;; Non-spec message in the print-eval phase:
              (= phase :print-eval-result)
                  (str (processor/process-message type message)
                       "\n"
                       (processor/location-print-phase via trace))
              :else
                  (str (processor/process-message type message)
                       "\n"
                       (processor/location-non-spec via trace)))))

;; I don't seem to be able to bind this var in middleware.
;; Running (setup-exc) in repl does the trick.
(defn setup-exc []
  (set! nrepl.middleware.caught/*caught-fn* #(do
    (let [modified (modify-message %)
          trace (processor/print-stacktrace %)
          _ (reset! track {:message (record-message %) :modified modified :trace trace})] ; for logging
    (println modified)
    (if (not= trace "") (println trace) ())))))

(defn reset-track [] (reset! track {}))