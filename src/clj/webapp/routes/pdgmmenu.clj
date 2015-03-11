(ns webapp.routes.pdgmmenu
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

(defn pdgmmenu []
  (layout/common [:h1 "Drop-down Paradigm Menus"]
                 [:hr]
    [:div
     [:p "By this option, after selecting a language, user will have parallel drop-down menus of the paradigms of each paradigm type."]]))

(defroutes pdgmmenu-routes
  (GET "/pdgmmenu" [] (pdgmmenu))
  )
