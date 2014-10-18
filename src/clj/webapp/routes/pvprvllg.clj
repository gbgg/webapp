(ns webapp.routes.pvprvllg
 (:refer-clojure :exclude [filter concat group-by max min count])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [capitalize split]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.form :refer :all]))

(def aama "http://localhost:3030/aama/query")

(defn pvprvllg []
  (let [langlist (slurp "pvlists/langlist.txt")
        languages (split langlist #"\n")]
  (layout/common [:h1 "Property Value Displays"]
                          [:hr]
)))

(defroutes pvprvllg-routes
  (GET "/pvprvllg" [] (pvprvllg))
  ;;(POST "/pdgmqry" [language pos] (handle-pdgmqry language pos))
  ;;(POST "/pdgmdisplay" [language valstring] (handle-pdgmdisplay language valstring))
  )


