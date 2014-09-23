(ns webapp.routes.home
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [clojure.tools.logging :as log]
            [hiccup.form :refer :all]
            ;;[clojure.string :as str]
))


(defn show-pdgm [language labbrev valstring]
       (let [pquery (sparql/pdgmquery language labbrev valstring)]
         [:p pquery]))

(defn pdgm-page []
  (layout/common [:h1 "PDGM Page"]))

(defn home []
  (layout/common [:h1 "PDGM Display"]
                 [:p "Welcome to PDGM Display"]
                 ;; [:p error]
                 [:hr]
                 (form-to [:post "/pdgm"]
                          [:p "Language:" (text-field "language")]
                          [:p "LangAbbrev:" (text-field "labbrev")]
                          [:p "Value String:" (text-field "valstring")]
                           (submit-button "Get pdgm"))
                          [:hr]))

(defroutes home-routes
  (GET "/" [] (home))
  (POST "/pdgm" [language labbrev valstring] 
        (layout/common 
         (show-pdgm language labbrev valstring))))
