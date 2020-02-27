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

(defn make-exception
  [exc msg]
  (let [exc-class (class exc)
        {:keys [via data]} (Throwable->map exc)
        msg-arr (to-array [msg])]
       (cond
          (and (= clojure.lang.ExceptionInfo exc-class) (= (resolve (:type (second via))) clojure.lang.ArityException))
              (process-arity-exception (:message (second via)))
          (= clojure.lang.ExceptionInfo exc-class)
              (clojure.lang.Reflector/invokeConstructor (resolve (:type (second via))) msg-arr)
          (= clojure.lang.Compiler$CompilerException exc-class)
              (clojure.lang.Compiler$CompilerException.
                ""
                (:clojure.error/line (:data (first via)))
                (:clojure.error/column (:data (first via)))
                (let [inner-exc-class (resolve (:type (last via)))]
                     (if (= clojure.lang.ArityException inner-exc-class)
                         (process-arity-exception (:message (last via)))
                         (clojure.lang.Reflector/invokeConstructor inner-exc-class
                                                                   (to-array [(msg-o/get-all-text
                                                                              (:msg-info-obj (p-exc/process-errors
                                                                                 (str (:type (last via))
                                                                                  " "
                                                                                  (:message (last via))))))])))))
          :else (clojure.lang.Reflector/invokeConstructor exc-class msg-arr))))

(defn- record-message
  [e]
  (cm/ex-str (cm/ex-triage (Throwable->map e))))

(defn- modify-message
  [exc]
  (let [exc-class (class exc)
        {:keys [via data cause]} (Throwable->map exc)
        [_ {:keys [type message]}] via ;; If type is bound, there is a nested exception
        exc-info? (= clojure.lang.ExceptionInfo exc-class)
        compiler-exc? (= clojure.lang.Compiler$CompilerException exc-class)]
        (cond (and exc-info? (not type)) (processor/spec-message data)
              (not type) (processor/process-message exc)
              (and type exc-info? (= (resolve type) clojure.lang.LispReader$ReaderException)) (processor/process-message exc)
              (and type compiler-exc? (processor/macro-spec? exc)) (processor/spec-macro-message exc)
              :else (s/trim (:message (last (:via (Throwable->map (make-exception exc (processor/process-message exc))))))))))

;; I don't seem to be able to bind this var in middleware.
;; Running (setup-exc) in repl does the trick.
(defn setup-exc []
  (set! nrepl.middleware.caught/*caught-fn* #(do
    (let [modified (modify-message %)
          _ (reset! track {:message (record-message %) :modified modified})] ; for logging
    (println modified)))))

(defn reset-track [](reset! track {}))
