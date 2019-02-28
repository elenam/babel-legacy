(defproject babel-middleware "0.1.4-alpha"
  :description "A proof of concept library to rewrite error messages."
  :url "https://github.com/Clojure-Intro-Course/babel"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/tools.nrepl "0.2.13"]
                 [expectations "2.2.0-rc3"]
                 [hiccup "1.0.5"]]
  :plugins [[lein-expectations "0.0.8"]]
  :repl-options {:nrepl-middleware
                 [babel.middleware/interceptor]
                 :port 7888}
   :injections [(require 'corefns.corefns)]
   :main babel.middleware
   :aot [babel.middleware])
