(ns webapp.routes.pvprvllg
 (:refer-clojure :exclude [filter concat group-by max min count])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [capitalize split replace]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.form :refer :all]))

(def aama "http://localhost:3030/aama/query")

(defn pvprvllg []
  (let [langlist (slurp "pvlists/langlist.txt")
        languages (split langlist #"\n")
        ldomlist (slurp "pvlists/ldomainlist.txt")
        ldoms (split ldomlist #"\n")]
  (layout/common 
   [:h3 "Language-Property-Value Cooccurrences"]
   [:p "Choose Language Domain and Enter qstring: prop=Val,...prop=?prop,..."]
   ;; [:p error]
   [:hr]
   (form-to [:post "/prvllgdisplay"]
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
             [:tr [:td "Prop=Val List: " ]
              [:td 
              (text-field 
               {:placeholder "person=Person2,gender=Fem,pos=?pos,number=?number"} 
               "qstring") ]
              ]
             ;;(submit-button "Get values")
             [:tr [:td ]
              [:td [:input#submit
                    {:value "Get language-prop-val", :name "submit", :type "submit"}]]]]
            )
   [:hr])))

(defn handle-prvllgdisplay
  [ldomain qstring]
  ;; send SPARQL over HTTP request
  (let [query-sparql (sparql/prvllg-sparql ldomain qstring)
        query-sparql-pr (replace query-sparql #"<" "&lt;")
        req (http/get aama
                      {:query-params
                       {"query" query-sparql ;;generated sparql
                        ;;"format" "application/sparql-results+json"}})]
                        "format" "text"}})]
         (log/info "sparql result status: " (:status req))
         (layout/common
          [:body
           [:h3#clickable "Language-Property-Values: " ldomain " / " qstring]
           [:pre (:body req)]
           [:hr]
           [:h3#clickable "Query:"]
           [:pre query-sparql-pr]
           [:script {:src "js/goog/base.js" :type "text/javascript"}]
           [:script {:src "js/webapp.js" :type "text/javascript"}]
           [:script {:type "text/javascript"}
            "goog.require('webapp.core');"]])))


(defroutes pvprvllg-routes
  (GET "/pvprvllg" [] (pvprvllg))
  ;;(POST "/pdgmqry" [language pos] (handle-pdgmqry language pos))
  (POST "/prvllgdisplay" [ldomain qstring] (handle-prvllgdisplay ldomain qstring))
  )


