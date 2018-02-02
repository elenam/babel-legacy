(ns babel.core)
(require '[clojure.tools.nrepl :as repl]
         '[clojure.spec.alpha :as s]
         '[clojure.spec.test.alpha :as stest])


(def counter (atom 0))

(defn instrument-after-each
  [handler]
  (fn [inp-message]
    (let [resp (handler inp-message)]
      (do
        (swap! counter (fn [prev] (class resp)))
        resp))))


#_(clojure.tools.nrepl.middleware/set-descriptor! #'instrument-after-each
        {:expects #{} :requires {} :handles {}})


;;the below are just debug things
(defn blipper
  [inp]
  "blip")

(s/fdef blipper
  :args string?)

(prn "You have loaded babel.core")
