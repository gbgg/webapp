(ns webapp.routes.trial
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

(defn trial []
  (layout/common [:h1 "Trial"]
                 [:hr]
    [:div
     [:h3 "Trial Pages:"]
     [:p "These pages will be integrated (or not) into the core application as they are found to be feasible and to realize useful and significantly different display possibilities."]
     [:p (link-to "/pdgmcmpn"  "Display of multiple paradigm menus.")]
     [:p (link-to "/pdgmcheckbx"  "Display of multiple paradigms in succession.")]
     [:p (link-to "/pdgmpll"  "Parallel display of multiple paradigms.")]
     [:p (link-to "/pdgmmenu"  "Cascading menu: lang > pos > pdgm.")]
     [:p (link-to "/langcheckbx"  "Checkbox for displaying succession of individual language menus.")]]))

(defroutes trial-routes
  (GET "/trial" [] (trial)))
