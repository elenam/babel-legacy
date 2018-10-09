(defproject babel-middleware :lein-v
  :description "A proof of concept library to rewrite error messages."
  :url "https://github.com/Clojure-Intro-Course/babel"
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
  :plugins [[lein-expectations "0.0.8"]
            [org.apache.maven.wagon/wagon-ssh-external "2.6"]
            [com.roomkey/lein-v "6.4.0"]]
  :repl-options {:nrepl-middleware
                 [babel.middleware/interceptor]
                 :port 7888};)
   :injections [(require 'corefns.corefns)]
   :main babel.middleware
   :aot [babel.middleware]
   :release-tasks
        [["vcs" "assert-committed"]
         ["v" "update"] ;; compute new version & tag it
         ["vcs" "push"]
         ["deploy"]]
   :repositories [["releases" {:url "https://clojars.org/babel-middleware"}
                              :creds :gpg]])
