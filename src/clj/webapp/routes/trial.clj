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
     [:h3 "Trial Pages:"]
     [:p "These pages will be integrated (or not) into the core application as they are found to be feasible and to realize useful and significantly different display possibilities."]
     [:ul
     [:li (link-to "/pdgmmenu"  "Parallel drop-down pdgm menus by pos.")]
     [:li (link-to "/pdgmcmpn"  "Display of N paradigm menus.")]
     [:li (link-to "/pdgmpll"  "Parallel display of N paradigms.")]
     ]))

(defroutes trial-routes
  (GET "/trial" [] (trial)))
