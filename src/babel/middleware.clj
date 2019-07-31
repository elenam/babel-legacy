(ns babel.middleware
  (:require [babel.processor :as processor]
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
                        (send [this msg]     (.send transport msg))))))));(processor/modify-errors msg)))))))))
                        ;(send [this msg]     (.send transport msg))))))))

;;sets the appropriate flags on the middleware so it is placed correctly
(nrepl.middleware/set-descriptor! #'interceptor
                                                {:expects #{"eval"} :requires #{} :handles {}})

(defn make-exception [exc msg]
  (let [exc-class (class exc)]
       (if (= clojure.lang.ExceptionInfo exc-class)
           ;(ex-info msg (ex-data exc))
               (if (= 1 (count (:via (Throwable->map exc))))
                   (Exception. (processor/spec-message (:data (Throwable->map exc)))) ;; repl doesn't use the message of ExceptionInfo; we need to replace the exception type
                   (clojure.lang.Reflector/invokeConstructor (resolve (:type (second (:via (Throwable->map exc)))))  (to-array [msg])));(str "the type is: " (:type (second (:via (Throwable->map exc)))))))
           (if (= clojure.lang.Compiler$CompilerException exc-class)
               (if (processor/macro-spec? exc)
                   (Exception. (processor/spec-macro-message exc))
                   (clojure.lang.Compiler$CompilerException. "" 100 100 (Exception. msg))) ; a stub for now
               ;; For now we are just recreating ArityException. We would need to manually replace it by a processed exception
               (if (= clojure.lang.ArityException exc-class)
                  (let [[_ howmany fname] (re-matches #"Wrong number of args \((\S*)\) passed to: (\S*)" (.getMessage exc))]
                       (clojure.lang.ArityException. (Integer/parseInt howmany) fname))
                   (clojure.lang.Reflector/invokeConstructor exc-class (to-array [msg])))))))

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
