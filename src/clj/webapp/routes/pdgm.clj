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
  (layout/common [:h3 "Paradigms"]
                 [:p "Choose Language and Type"]
                 ;; [:p error]
                 [:hr]
                 (form-to [:post "/pdgmqry"]
                          [:p "PDGM Language:" 
                           [:select#language.required
                            {:title "Choose a language.", :name "language"}
                            (for [language languages]
                              [:option  language ])
                            ]]
                          [:p "PDGM Type:" 
                           [:select#pos.required
                            {:title "Choose a pdgm type.", :name "pos"}
                              [:option "fv"]
                              [:option "nfv"]
                              [:option "pro"]
                              [:option "noun"]
                            ]]
                          ;;(submit-button "Get pdgm")
                          [:input#submit
                           {:value "Get pdgm language/type", :name "submit", :type "submit"}]
                          )
                          [:hr])))

(defn handle-pdgmqry 
  [language pos]
  (let [valclusterfile (str "pvlists/pname-" pos "-list-" language ".txt")
        valclusterlist (slurp valclusterfile)
        valclusters (clojure.string/split valclusterlist #"\n")]
    (layout/common 
     [:h3 "Paradigms"]
     [:p "Choose Value Clusters"]
     ;;[:p error]
     [:hr]
     (form-to [:post "/pdgmdisplay"]
              [:p "Language: "
               [:select#language.required
                {:title "Choose a language.", :name "language"}
                  [:option  language]
                ]]
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
  (let [Language (capitalize language)
        lprefmap (read-string (slurp "pvlists/lprefs.clj"))
        lang (read-string (str ":" language))
        lpref (lang lprefmap)
        query-sparql (sparql/pdgmqry-sparql language lpref valstring)
        req (http/get aama
                      {:query-params
                       {"query" query-sparql ;;generated sparql
                        ;;"format" "application/sparql-results+json"}})]
                        "format" "text"}})]
         (log/info "sparql result status: " (:status req))
         (layout/common
          [:body
           [:h3#clickable "Paradigm: " Language " / " valstring]
           [:pre (:body req)]
           [:h3#clickable "Query"]
           [:pre query-sparql]
           [:script {:src "js/goog/base.js" :type "text/javascript"}]
           [:script {:src "js/webapp.js" :type "text/javascript"}]
           [:script {:type "text/javascript"}
            "goog.require('webapp.core');"]])))

(defroutes pdgm-routes
  (GET "/pdgm" [] (pdgm))
  (POST "/pdgmqry" [language pos] (handle-pdgmqry language pos))
  (POST "/pdgmdisplay" [language valstring] (handle-pdgmdisplay language valstring))
  )
