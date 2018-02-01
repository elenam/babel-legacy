(ns babel.core)
(require '[clojure.tools.nrepl :as repl]
         '[clojure.spec.alpha :as s]
         '[clojure.spec.test.alpha :as stest])


(def counter (atom []))

(defn instrument-after-each
  [handler]
  (fn [inp-message]
    (do
      (handler inp-message ))))

(clojure.tools.nrepl.middleware/set-descriptor! #'instrument-after-each
        {:requires #{}
         :expects #{"eval"}
         :handles {}})


;;the below are just debug things
(defn blipper
  [inp]
  "blip")

(s/fdef blipper
  :args string?)

(prn "You have loaded babel.core")
