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
  (let [langlist (slurp "pvlists/langlist.txt")
        languages (split langlist #"\n")]
  (layout/common [:h1 "Property Value Displays"]
                          [:hr]
                            ;; [:ul 
    [:table
     [:tr [:td (link-to "/pvlgpr" "Language-property displays")]
      [:td "This family of queries . . . "]]
     [:tr [:td (link-to "/pvlgvl" "Language-value displays")]
      [:td "This family of queries . . . "]]
     [:tr [:td (link-to "/pvprvllg" "Language-property-value displays")]
      [:td "This family of queries . . ."]]
     ]
)))

(defroutes pvdisp-routes
  (GET "/pvdisp" [] (pvdisp))
  ;;(POST "/pdgmqry" [language pos] (handle-pdgmqry language pos))
  ;;(POST "/pdgmdisplay" [language valstring] (handle-pdgmdisplay language valstring))
  )


