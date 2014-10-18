(ns webapp.routes.pvlgpr
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
            [hiccup.form :refer :all]))

(def aama "http://localhost:3030/aama/query")

(defn pvlgpr []
  (let [langlist (slurp "pvlists/langlist.txt")
        languages (split langlist #"\n")]
  (layout/common 
   [:h3 "Language-Property-Value Cooccurrences"]
   [:p "Choose Language Domain and Property"]
   ;; [:p error]
   [:hr]
   (form-to [:post "/lgprdisplay"]
            [:table
             [:tr [:td "Language Domain: " ]
              [:td 
               ;;[:select#ldomain.required
               ;;{:title "Choose a language domain.", :name "ldomain"}
               ;;(for [language languages]
               ;;[:option  language ])
               ;;]
               (text-field {:placeholder "Enter a language domain"} "ldomain")
               ]
              ]
             [:tr [:td "Property: " ]
              [:td 
               ;;[:select#prop.required
               ;;{:title "Choose a property.", :name "[prop"}
               ;;(for [prop props]
               ;;[:option  prop])
               ;;]
               (text-field {:placeholder "Enter a property"} "prop")
               ]]
             ;;(submit-button "Get pdgm")
             [:tr [:td ]
              [:td [:input#submit
                    {:value "Get language domain and property", :name "submit", :type "submit"}]]]]
            )
   [:hr])))

(defn handle-lgprdisplay
  [ldomain prop]
  ;; How do we handle this if ldomain is > 1?
  ;; send SPARQL over HTTP request
  (let [query-sparql (sparql/lgpr-sparql ldomain prop)
        req (http/get aama
                      {:query-params
                       {"query" query-sparql ;;generated sparql
                        ;;"format" "application/sparql-results+json"}})]
                        "format" "text"}})]
         (log/info "sparql result status: " (:status req))
         (layout/common
          [:body
           [:h3#clickable "Language-Property-Values: " ldomain " / " prop]
           [:pre (:body req)]
           [:hr]
           [:h3#clickable "Query:"]
           [:pre query-sparql]
           [:script {:src "js/goog/base.js" :type "text/javascript"}]
           [:script {:src "js/webapp.js" :type "text/javascript"}]
           [:script {:type "text/javascript"}
            "goog.require('webapp.core');"]])))

(defroutes pvlgpr-routes
  (GET "/pvlgpr" [] (pvlgpr))
  ;;(POST "/lgprqry" [ldomain prop] (handle-lgprqry ldomain prop))
  (POST "/lgprdisplay" [ldomain prop] (handle-lgprdisplay ldomain prop))
  )


