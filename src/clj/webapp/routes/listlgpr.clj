(ns webapp.routes.listlgpr
 (:refer-clojure :exclude [filter concat group-by max min count replace])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [split replace]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.form :refer :all]))

(def aama "http://localhost:3030/aama/query")

(defn listlgpr []
  (let [langlist (slurp "pvlists/langlist.txt")
        languages (split langlist #"\n")
        ldomlist (slurp "pvlists/ldomainlist.txt")
        ldoms (split ldomlist #"\n")
        lvallist (slurp "pvlists/langvals.txt")
        lvals (split lvallist #"\n")]
  (layout/common 
   [:h3 "Properties by POS for datastore languages"]
   [:hr]
   (form-to [:post "/listlgpr-gen"]
            [:table
             [:tr [:td "Part of Speech: "]
              [:td [:select#pos.required
                    {:title "Choose a pdgm type.", :name "pos"}
                    [:option {:value "fv" :label "Finite Verb"}]
                    [:option {:value "nfv" :label "Non-finite Verb"}]
                    [:option {:value "pro" :label "Pronoun"}]
                    [:option {:value "noun" :label "Noun"}]
                    ]]]
             ;;(submit-button "Get pdgm")
             [:tr [:td ]
              [:td [:input#submit
                    {:value "Make Language-Property List", :name "submit", :type "submit"}]]]]))))

(defn handle-listlgpr-gen
  [pos]
  ;; send SPARQL over HTTP request
  (let [query-sparql (sparql/lgpr-sparql pos)
        query-sparql-pr (replace query-sparql #"<" "&lt;")
        req (http/get aama
                      {:query-params
                       {"query" query-sparql ;;generated sparql
                        ;;"format" "application/sparql-results+json"}})]
                        "format" "text"}})]
         (log/info "sparql result status: " (:status req))
         (layout/common
          [:body
           [:h3#clickable "Part of speech: " pos]
           [:pre (:body req)]
           [:hr]
           [:h3#clickable "Query:"]
           [:pre query-sparql-pr]
           [:script {:src "js/goog/base.js" :type "text/javascript"}]
           [:script {:src "js/webapp.js" :type "text/javascript"}]
           [:script {:type "text/javascript"}
            "goog.require('webapp.core');"]])))

(defroutes listlgpr-routes
  (GET "/listlgpr" [] (listlgpr))
  (POST "/listlgpr-gen" [pos] (handle-listlgpr-gen pos))
  ;;(POST "/lgvldisplay" [ldomain lval] (handle-lgvldisplay ldomain lval))
  )


