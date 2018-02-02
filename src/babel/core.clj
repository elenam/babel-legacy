(ns babel.core)
(require '[clojure.tools.nrepl :as repl]
         '[clojure.spec.alpha :as s]
         '[clojure.spec.test.alpha :as stest])


(def counter (atom 0))
(def out-watcher (atom 0))
(def out-keeper (atom 0))
(def inp-watcher (atom 0))
(def inp-keeper (atom 0))

(defn instrument-after-each
  [handler]
  (fn [inp-message]
    (do
      (swap! inp-keeper (fn [prev] (identity @inp-watcher)))
      (swap! inp-watcher (fn [prev] (identity inp-message)))
    (let [resp (handler inp-message)]
      (do
        (swap! out-keeper (fn [prev] (identity @out-watcher)))
        (swap! out-watcher (fn [prev] (identity resp )))
      resp)))))

#_(clojure.tools.nrepl.middleware/set-descriptor! #'instrument-after-each
        {:expects #{"eval"} :requires #{} :handles {}})


;;the below are just debug things
(defn blipper
  [inp]
  "blip")

(s/fdef blipper
  :args string?)

(prn "You have loaded babel.core")
