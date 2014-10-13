(ns webapp.routes.home
  (:require [compojure.core :refer :all]
            [hiccup.element :refer [link-to]]
            [webapp.views.layout :as layout]))

(defn home []
  (layout/common
   [:h1 "Afroasiatic Morphological Archive"]
   [:h1 "Query and Display Demo"]
   [:p "The purpose of this demo application is to test various general formats of SPARQL queries that can be used to explore the morphological data registered in the Afroasiatic Morphological Archive. For the purposes of this demo these queries are divided into three large groups:"]
    [:ol
    [:li "Paradigm Displays:"]
    [:li "Property-value Displays:"]
    [:li "Property-value Lists:"]]
    [:p "In each case the query parameters will be specified by pick-lists or text-input boxes, and the query response returned from the datastore will be followed by a listing of the query which produced that response."]
))
   

(defroutes home-routes
  (GET "/" [] (home)))
