(ns webapp.routes.bibInfo
 (:refer-clojure :exclude [filter concat group-by max min count replace])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [split lower-case replace]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.form :refer :all]))

(def aama "http://localhost:3030/aama/query")

(defn bibInfo []
  (let [reflist (slurp "pvlists/bibref-list.txt")
        bibrefs (split reflist #"\n")]
  (layout/common 
   [:h1#clickable "Afroasiatic Morphological Archive"]
   [:h3 "Display Bibliographic Information"]
   [:p "(This option will enable a user to display full bibliographic information, given a bibref string.)"]
   [:hr]
   (form-to [:post "/bibInfo-make"]
            [:table
             [:tr [:td "Bibliographic Reference: " ]
              [:td [:select#bibref.required
                    {:title "Choose a Bibliographic Reference.", :name "bibref"}
                    (for [bibref bibrefs]
                      [:option {:value bibref} bibref])]]]
             ;;(submit-button "Get pdgm")
             [:tr 
              [:td {:colspan "2"} [:input#submit
                    {:value "Display Bibliographic Information", :name "submit", :type "submit"}]]]]))))

(defn handle-bibInfo-make
  [bibref]
  (let [bibrefmap (read-string (slurp "pvlists/bibrefs.clj"))
        bref (keyword (str bibref))
        ref (bref bibrefmap)]
  (layout/common
   [:body
    [:h4#clickable "Bibliographic Information: "]
    ;;[:p "bref = "  [:pre bref]]
    ;;[:p "bibkeys = " bibkeys]
    [:table {:class "linfo-table"}
     [:tbody
      [:tr
       [:th "Bibref:"] [:td bibref]]
      [:tr 
       [:th "Full Form:"] [:td ref]]]] 
    [:script {:src "js/goog/base.js" :type "text/javascript"}]
    [:script {:src "js/webapp.js" :type "text/javascript"}]
    [:script {:type "text/javascript"}
     "goog.require('webapp.core');"]])))


(defroutes bibInfo-routes
  (GET "/bibInfo" [] (bibInfo))
  (POST "/bibInfo-make" [bibref] (handle-bibInfo-make bibref)))
