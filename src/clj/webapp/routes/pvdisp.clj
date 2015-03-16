(ns webapp.routes.pvdisp
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
            [hiccup.element :refer [link-to]]
            [hiccup.form :refer :all]))

(def aama "http://localhost:3030/aama/query")

(defn pvdisp []
  (layout/common [:h1 "Property Value Displays"]
                 [:hr]
                            ;; [:ul 
    [:p "The following pages are designed to permit querying for arbitrary combinations of language, property, and value."]
    [:ul
     [:li (link-to "/pvlgpr" "Language-property")]
     [:li (link-to "/pvlgvl" "Language-value")]
     [:li (link-to "/pvprvllg" "Language-property-value")]
     ]))


(defroutes pvdisp-routes
  (GET "/pvdisp" [] (pvdisp)))


