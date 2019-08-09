(ns babel.middleware
  (:require [babel.processor :as processor]
            [errors.dictionaries :as d]
            [nrepl.middleware]
            [nrepl.middleware.caught]
            [clojure.repl]
            [clojure.main :as cm :refer [ex-str ex-triage]])
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

(defn make-exception [exc msg]
  (let [exc-class (class exc)
        {:keys [via data]} (Throwable->map exc)
        msg-arr (to-array [msg])]
       (cond
         (and (= clojure.lang.ExceptionInfo exc-class) (= 1 (count via)))
             (Exception. (processor/spec-message data)) ;; repl doesn't use the message of ExceptionInfo; we need to replace the exception type
         (and (= clojure.lang.ExceptionInfo exc-class) (= (resolve (:type (second via))) clojure.lang.LispReader$ReaderException))
             (clojure.lang.LispReader$ReaderException.
               (:clojure.error/line (:data (second via)))
               (:clojure.error/column (:data (second via)))
               (clojure.lang.Reflector/invokeConstructor (resolve (:type (last via))) msg-arr))
          (and (= clojure.lang.ExceptionInfo exc-class) (= (resolve (:type (second via))) clojure.lang.ArityException))
              (process-arity-exception (:message (second via)))
          (= clojure.lang.ExceptionInfo exc-class)
              (clojure.lang.Reflector/invokeConstructor (resolve (:type (second via))) msg-arr)
          (= clojure.lang.Compiler$CompilerException exc-class)
              (cond (processor/macro-spec? exc) (Exception. (processor/spec-macro-message exc))
                    :else (clojure.lang.Compiler$CompilerException. "" 100 100 (Exception. msg))) ; a stub for now
          (= clojure.lang.ArityException exc-class)
               (process-arity-exception (.getMessage exc))
          :else (clojure.lang.Reflector/invokeConstructor exc-class msg-arr))))

(defn- record-message
  [e]
  (cm/ex-str (cm/ex-triage (Throwable->map e))))

;; I don't seem to be able to bind this var in middleware.
;; Running (setup-exc) in repl does the trick.
(defn setup-exc []
  (set! nrepl.middleware.caught/*caught-fn* #(do
    (reset! track {:e % :message (record-message %)}) ; for debugging - and possibly for logging
    (clojure.main/repl-caught (make-exception % (if (and (= clojure.lang.ExceptionInfo (class %)) (= 1 (count (:via (Throwable->map %)))))
                                                    "" (processor/process-message %)))))))
    ;(clojure.repl/pst % 3))))
(defn reset-track [](reset! track {}))
