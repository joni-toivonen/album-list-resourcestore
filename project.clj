(defproject album-list-resourcestore "0.1.0-SNAPSHOT"
  :description "REST backend that takes requests via HTTP and stores music album list data in Redis database"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [metosin/compojure-api "1.1.11"]
                 [com.taoensso/carmine "2.19.1"]]
  :ring {:handler album-list-resourcestore.handler/app}
  :uberjar-name "server.jar"
  :profiles {:dev {:dependencies [[javax.servlet/javax.servlet-api "3.1.0"]
                                  [cheshire "5.5.0"]
                                  [ring/ring-mock "0.3.0"]
                                  [midje "1.8.3"]]
                   :plugins [[lein-ring "0.12.0"]
                             [lein-midje "3.2"]
                             [lein-cloverage "1.1.1"]]}})
