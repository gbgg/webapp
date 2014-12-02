(ns webapp.routes.listlpv
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

(defn listlpv []
  (let [langlist (slurp "pvlists/langlist.txt")
        languages (split langlist #"\n")
        ldomlist (slurp "pvlists/ldomainlist.txt")
        ldoms (split ldomlist #"\n")
        lvallist (slurp "pvlists/langvals.txt")
        lvals (split lvallist #"\n")]
  (layout/common 
   [:h3 "Language-Property-Value Co-occurrences"]
   [:hr]
   (form-to [:post "/listlpv-gen"]
            [:table
             [:tr [:td "Language Domain: " ]
              [:td 
               [:select#ldomain.required
               {:title "Choose a language domain.", :name "ldomain"}
                [:optgroup {:label "Languages"} 
                (for [language languages]
                (let [opts (split language #" ")]
               [:option {:value (first opts)} (last opts) ]))]
                [:optgroup {:label "Language Families"} 
               (for [ldom ldoms]
                (let [opts (split ldom #" ")]
               [:option {:value (last opts)} (first opts) ]))
                 [:option {:disabled "disabled"} "Other"]]]]]
             ;;(submit-button "Get values")
             [:tr [:td ]
              [:td [:input#submit
                    {:value "Make Language-Property-Value Lists", :name "submit", :type "submit"}]]]]))))

(defn handle-listlpv-gen
  [ldomain]
  ;; send SPARQL over HTTP request
  (let [query-sparql (sparql/lgpr-sparql ldomain)
        query-sparql-pr (replace query-sparql #"<" "&lt;")
        req (http/get aama
                      {:query-params
                       {"query" query-sparql ;;generated sparql
                        ;;"format" "application/sparql-results+json"}})]
                        "format" "text"}})]
         (log/info "sparql result status: " (:status req))
         (layout/common
          [:body
           [:h3#clickable "Language Domain: " ldomain]
           [:pre (:body req)]
           [:hr]
           [:h3#clickable "Query:"]
           [:pre query-sparql-pr]
           [:script {:src "js/goog/base.js" :type "text/javascript"}]
           [:script {:src "js/webapp.js" :type "text/javascript"}]
           [:script {:type "text/javascript"}
            "goog.require('webapp.core');"]])))

(defroutes listlpv-routes
  (GET "/listlpv" [] (listlpv))
  (POST "/listlpv-gen" [ldomain] (handle-listlpv-gen ldomain))
  ;;(POST "/lgvldisplay" [ldomain lval] (handle-lgvldisplay ldomain lval))
  )


