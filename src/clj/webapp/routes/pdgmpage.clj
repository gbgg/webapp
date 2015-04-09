(ns webapp.routes.pdgmpage
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

(defn pdgmpage []
  (layout/common [:h1 "Paradigm Displays"]
                 [:hr]
                            ;; [:ul 
    [:p "The following pages experiment with different possibilities for display and comparison of paradigms. The comparisons for the moment are oriented to png-centered displays, and thus work reasonably well for finite verb and pronominal paradigms. Their application is less clear for non-finite verbs. Note that the present datastore contains " [:em "very "] "little material for nominal inflection."]
    [:ol
     [:li (link-to "/pdgm" "Individual Paradigms")]
     [:li (link-to "/pdgmcmp" "Paradigm Comparison")]
     [:li (link-to "/pdgmcheckbx"  "Checkbox for sequential display of multiple paradigms from a given language.")]
     [:li (link-to "/pdgmcbpll"  "Parallel display of paradigms within a given language from checkbox.")]
     [:li (link-to "/langcheckbx"  "Checkbox for displaying succession of individual language menus.")]
     ]))


(defroutes pdgmpage-routes
  (GET "/pdgmpage" [] (pdgmpage)))


