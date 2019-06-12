(ns babel.middleware
  (:require [babel.processor :as processor]
            [nrepl.middleware]
            [nrepl.middleware.caught]
            [clojure.repl])
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

(defn- make-exception [exc msg]
  (let [exc-class (class exc)]
       (if (= exc-class clojure.lang.ExceptionInfo)
           (ex-info msg (ex-data exc)) exc)))
           ;(if (= "clojure.lang.Compiler$CompilerException" errclass)
            ;   (clojure.lang.Reflector/invokeConstructor () (to-array [msg])))))
           ;(clojure.lang.Reflector/invokeConstructor exc-class (to-array [msg])))))

;; I don't seem to be able to bind this var in middleware.
;; Running (setup-exc) in repl does the trick.
(defn setup-exc []
  (set! nrepl.middleware.caught/*caught-fn* #(do
    (reset! track {:e %}) ; for debugging - and possibly for logging
    (clojure.main/repl-caught (make-exception % (processor/process-message %))))))
    ;(clojure.repl/pst % 3))))
