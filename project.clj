(defproject babel "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.nrepl "0.2.13"]
                 [expectations "2.2.0-rc3"]
                 [hiccup "1.0.5"]
                 [org.onyxplatform/onyx "0.13.3-alpha4"]
                 [org.onyxplatform/onyx-spec "0.12.7.0"]
                 [org.clojure/java.jdbc "0.7.8"]]
                 ;[ring "1.7.0-RC1"]
                 ;[javax.servlet/servlet-api "2.5"]
  :plugins [[lein-expectations "0.0.8"]]
  :repl-options {:nrepl-middleware
                 [babel.middleware/interceptor]
                 :port 7888};)
   :main utilities.spec_generator)
   ;:main specfiletesting.core)
