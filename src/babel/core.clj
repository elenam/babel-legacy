(ns babel.core)
(require '[clojure.tools.nrepl :as repl]
         '[clojure.spec.alpha :as s]
         '[clojure.spec.test.alpha :as stest])


(def counter (atom 0))

(defn instrument-after-each
  [handler]
  (fn [inp-message]
    (do
      (stest/instrument)
      (swap! counter (fn [nope] (identity
        {:str (str inp-message)
        :class (class inp-message)
        :identity (assoc inp-message :sumin 5)})))
      #_(print @counter)
      (print   inp-message)
      (print "")
      (handler  inp-message))))

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
