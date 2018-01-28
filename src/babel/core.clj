(ns babel.core)
(require '[clojure.tools.nrepl :as repl]
         '[clojure.spec.alpha :as s]
         '[clojure.spec.test.alpha :as stest])




(defn instrument-after-each
  [handler]
  (fn [inp-message]
    (do
      (stest/instrument)
      (handler inp-message))))

#_(clojure.tools.nrepl.middleware/set-descriptor! #'print-side-effect
        {:requires #{}
         :expects #{"eval"}
         :handles {}})


::the below are just debug things
(defn blipper
  [inp]
  "blip")

(s/fdef blipper
  :args string?)

(prn "You have loaded babel.core")
