(ns webapp.routes.pdgm
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

(defn pdgm []
  (let [langlist (slurp "pvlists/langlist.txt")
        languages (split langlist #"\n")]
  (layout/common [:h1 "PDGM Type"]
                 [:p "Welcome to PDGM Type"]
                 ;; [:p error]
                 [:hr]
                 (form-to [:post "/pdgmqry"]
                          [:p "PDGM Language:" 
                           [:select#language.required
                            {:title "Choose a value.", :name "valstring"}
                            (for [language languages]
                              [:language  language ])
                            ]]
                          [:p "PDGM Type:" 
                           [:select#pos.required
                            {:title "Choose a value.", :name "valstring"}
                              [:option "fn"]
                              [:option "nfv"]
                              [:option "pro"]
                              [:option "noun"]
                            ]]
                          ;;(submit-button "Get pdgm")
                          [:input#submit
                           {:value "Get pdgm type", :name "submit", :type "submit"}]
                          )
                          [:hr])))

(defn handle-pdgmqry 
  [language pos]
  (let [valclusterfile (str "pvlists/-" pos "-list-" language ".txt")
        valclusterlist (slurp valclusterfile)
        valclusters (clojure.string/split valclusterlist #"\n")]
    (layout/common 
     [:h1 "PDGM Display"]
     [:p "Welcome to PDGM Display"]
     ;;[:p error]
     [:hr]
     (form-to [:post "/pdgmdisplay"]
              [:p "Language: " language]
              [:p "PDGM Type: " pos]
              [:p "PDGM Value Clusters:" 
               [:select#valstring.required
                {:title "Choose a value.", :name "valstring"}
                (for [valcluster valclusters]
                  [:option  valcluster])
                ]]
              ;;(submit-button "Get pdgm")
              [:input#submit
               {:value "Display pdgm", :name "submit", :type "submit"}])
     [:hr])))

(defn handle-pdgmdisplay
  [language valstring]
  ;; send SPARQL over HTTP request
  ;; see if can make separate ns & page 
  (let [lpref-map (slurp "pvlists/lpref-map")
        lpref (lpref-map language)
        query-sparql (sparql/pdgmqry-sparql language lpref valstring)
        req (http/get aama
                      {:query-params
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
            "goog.require('webapp.core');"]])))


(defroutes pdgm-routes
  (GET "/pdgm" [] (pdgm))
  (POST "/pdgmqry" [language pos] (handle-pdgmqry language pos))
  (POST "/pdgmdisplay" [language valstring] (handle-pdgmdisplay language valstring)))
