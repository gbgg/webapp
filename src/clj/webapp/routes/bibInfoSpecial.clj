(ns webapp.routes.bibInfoSpecial
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

(defn bibInfoSpecial []
  (let [biblioglist (slurp "pvlists/menu-bibliographies.txt")
        bibliogrefs (split biblioglist #"\n")]
  (layout/common 
   ;;[:h1#clickable "Afroasiatic Morphological Archive"]
   [:h3 "Display Project Bibliographic Information"]
   ;;[:p "(This option will enable a user to display full bibliographic information, given a bibref string.)"]
   [:hr]
   (form-to [:post "/bibInfoSpecial"]
            [:table
             [:tr [:td "Special Bibliography: " ]
              [:td [:select#bibliogref.required
                    {:title "Choose a bibliography.", :name "bibliogref"}
                    (for [bibliogref bibliogrefs]
                        [:option {:value bibliogref :label bibliogref}])]]]
             [:tr 
              [:td {:colspan "2"} [:input#submit
                    {:value "Choose Bibliography: ", :name "submit", :type "submit"}]]]]))))


(defn handle-bibInfoSpecial
  [bibliogref]
  (let [biblioglist (slurp "pvlists/menu-bibliographies.txt")
        bibliogrefs (split biblioglist #"\n")
        bibliography (str "pvlists/bibref-" bibliogref "-list.txt")
        reflist (slurp bibliography)
        bibrefs (split reflist #"\n")
        bibrefmap (read-string (slurp "pvlists/bibrefs.clj"))]
  (layout/common
   [:body
   ;;[:h1#clickable "Afroasiatic Morphological Archive"]
   [:h3 "Display Project Bibliographic Information"]
   ;;[:p "Form repeated here to enable successive searches. How make cumulative on page?"]
   [:hr]
   (form-to [:post "/bibInfoSpecial"]
            [:table
             [:tr [:td (str "Bibliography for: " bibliography) ]
              [:td [:select#bibliogref.required
                    {:title "Choose a bibliography.", :name "bibliogref"}
                    (for [bibliogref bibliogrefs]
                      [:option {:value bibliogref :label bibliogref}])]]]
             [:tr 
              [:td {:colspan "2"} [:input#submit
                                   {:value "Choose Bibliography: ", :name "submit", :type "submit"}]]]])
    ;;[:h4#clickable "Bibliographic Information: "]
    [:p]
    [:table {:class "linfo-table"} 
     [:tbody
      (for [bibref bibrefs]
        (let [bref  (read-string (str ":" bibref))
             ref (bref bibrefmap)]
          [:tr
        ;;[:p (str bibref)])
           [:th bibref] [:td ref ]]))]]
    [:script {:src "js/goog/base.js" :type "text/javascript"}]
    [:script {:src "js/webapp.js" :type "text/javascript"}]
    [:script {:type "text/javascript"}
     "goog.require('webapp.core');"]])))


(defroutes bibInfoSpecial-routes
  (GET "/bibInfoSpecial" [] (bibInfoSpecial))
  (POST "/bibInfoSpecial" [bibliogref] (handle-bibInfoSpecial bibliogref)))
