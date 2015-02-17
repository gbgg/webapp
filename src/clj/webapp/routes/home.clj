(ns webapp.routes.home
  (:require [compojure.core :refer :all]
            [hiccup.element :refer [link-to]]
            [webapp.views.layout :as layout]))

(defn home []
  (layout/common
   [:h1 "Afroasiatic Morphological Archive"]
   [:h1 "Query and Display Tool"]
   [:p "The purpose of this application is to develop and test various general formats of SPARQL queries that can be used to explore the morphological data registered in the Afroasiatic Morphological Archive. For the purposes of this provisional tool these queries are divided into four large groups, with an additional space for experimental displays:"]
    [:ol
    [:li "Paradigm Displays:"]
    [:li "Paradigm Comparisons:"]
    [:li "Property-value Displays:"]
    [:li "Utilities:"]
    [:li "Trial Query/Display Pages:"]]
    [:p "In each case the query parameters will be specified by pick-lists or text-input boxes, and wherever feasible the query response returned from the datastore will be followed by a listing of the query which produced that response."]
))
   

(defroutes home-routes
  (GET "/" [] (home)))
