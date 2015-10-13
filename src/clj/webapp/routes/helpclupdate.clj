(ns webapp.routes.helpclupdate
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
            [hiccup.element :refer [link-to]]
            [hiccup.form :refer :all]))

(defn helpclupdate []
  (layout/common 
   [:div {:class "help-page"}
     [:h3 "Help: Update [Command-line]"]
     [:p "The following command-line versions presuppose that the edn data files are in the  ~/aama-data/data/[LANG] directories:"]
      [:ul [:li [:h4 "Datastore Update "] 
            [:p "The following script will:" 
             [:ol 
              [:li "Delete current LANG sub-graph(s) from the datastore"]
              [:li "Run triple-count and sub-graph-list queries to verify deletion(s)"]
              [:li "Transform the revised edn file to a ttl file"]
              [:li "Insert revised LANG sub-graph(s) into datastore"]
              [:li "Run triple-count and sub-graph-list queries to verify insertion(s)"]]]
            [:p "Usage:" 
            [:ul 
             [:li "bin/aama-datastore-update.sh ../aama-data/data/[LANG]  (for a single language; from webapp dir)"]
             [:li " (~/aama-data/)bin/aama-datastore-setup.sh \"data/*\" (to [re-]initiate the whole datastore from ~/aama-data dir)"]]]]
       [:li [:h4 "Datastore Upload."] 
        [:p "The following scripts will:"
         [:ol
          [:li "Upload revised edn/ttl file(s) to aama/[LANG] repository"]
          [:li "Push the new edn/ttl file(s) to origin ("[:em "github.com/aama/[LANG]"]")"]
          [:li "Upload and push a revised aama-edn2ttl jar and source file"]]]
        [:p "Usage:"
        [:ul 
         [:li "bin/aama-cp2lngrepo.sh ../aama-data/data/[LANG] (for a single language; from webapp dir)"]
         [:li "(~/aama-data/)bin/aama-cp2lngrepo.sh \"data/*\" (to [re-]upload the whole datastore; from ~/aama-data dir)"]
         [:li "bin/aama-cptools2lngrepo.sh"]]]]]
   [:hr]
   [:h4 "[For more detail on the above, cf. the github aama-data/bin " (link-to "https://github.com/gbgg/aama-data/blob/master/bin/README.md" "README")"]"]]))

(defroutes helpclupdate-routes
  (GET "/helpclupdate" [] (helpclupdate)))


