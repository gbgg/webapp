(ns webapp.routes.listmenulpv
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

(defn listmenulpv []
  (layout/common [:h1 "Language, Property, Value Lists for Selection Menus"]
                 [:hr]
    [:div
     [:p "This option will generate cached datastore-wide language, property, or value lists for use in selection drop-down menus.."]]))

(defroutes listmenulpv-routes
  (GET "/listmenulpv" [] (listmenulpv))
  )
