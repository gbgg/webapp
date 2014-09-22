(defproject webapp3 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.6"]
                 [hiccup "1.0.5"]
                 [org.clojure/tools.logging "0.3.0"]
                 [stencil "0.3.4"]
                 [matsu "0.1.2"] ;; SPARQL query constructor
                 [clj-http "1.0.0"] ;; http client lib
                 [org.clojure/clojurescript "0.0-2311"]
                 [ring-server "0.3.1"]]
  :plugins [[lein-ring "0.8.10"]]
  :ring {:handler webapp3.handler/app
         :init webapp3.handler/init
         :destroy webapp3.handler/destroy}
  :aot :all
  :profiles
  {:production
   {:ring
    {:open-browser? false, :stacktraces? false, :auto-reload? false}}
   :dev
   {:dependencies [[ring-mock "0.1.5"] [ring/ring-devel "1.2.1"]]}})
