(ns webapp.routes.pdgmqry
 (:refer-clojure :exclude [filter concat group-by max min count])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [capitalize split]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            [boutros.matsu.sparql :refer :all]
            [boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.form :refer :all]))


(defn pdgmqry 
  [language pos]
  (let [optionfile (str lg "-" pos "-list.txt")
        optionlist (slurp optionfile)
        options (clojure.string/split optionlist #"\n")]
    (layout/common [:h1 "PDGM Display"]
                 [:p "Welcome to PDGM Display"]
                 ;; [:p error]
                 [:hr]
                 (form-to [:post "/pdgmdisplay"]
                          [:p "Language: " language]
                          [:p "PDGM Type: " pos]
                          [:p "PDGM Value Clusters:" 
                           [:select#valstring.required
                            {:title "Choose a value.", :name "valstring"}
                            (for [option options]
                              [:option  option ])
                            ]]
                          ;;(submit-button "Get pdgm")
                          [:input#submit
                           {:value "Display pdgm", :name "submit", :type "submit"}]
                          )
                          [:hr])))



(defroutes pdgmqry-routes
  (GET "/pdgmqry" [language pos] (pdgmqry language pos))
  (POST "/pdgmdisplay" [language valstring]
       ;; send SPARQL over HTTP request
       ;; see if can make separate ns & page 
       (let [query-sparql (sparql/pdgmqry-sparql language valstring)
             req (http/get aama
                           {:query-params
                            ;;{"query" (aama-qry) ;;canned matsu
                            ;;{"query" query-matsu ;;generated matsu
                            ;;{"query" pdgm-qry ;;canned sparql
                            {"query" query-sparql ;;generated sparql
                             ;;"format" "application/sparql-results+json"}})]
                             "format" "text"}})]
         (log/info "sparql result status: " (:status req))
         (layout/common
          [:body
           [:h1#clickable "Result with query-sparql"]
           [:pre (:body req)]
           [:pre query-sparql]
           [:script {:src "js/goog/base.js" :type "text/javascript"}]
           [:script {:src "js/webapp.js" :type "text/javascript"}]
           [:script {:type "text/javascript"}
            "goog.require('webapp.core');"]]))))
