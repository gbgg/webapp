(ns webapp.handler
  (:require [compojure.core :refer [defroutes routes]]
            [noir.util.middleware :as noir-middleware]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [hiccup.middleware :refer [wrap-base-url]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [webapp.routes.home :refer [home-routes]]
            [webapp.routes.pvlgpr :refer [pvlgpr-routes]]
            [webapp.routes.pvprvllg :refer [pvprvllg-routes]]
            [webapp.routes.pvlgvl :refer [pvlgvl-routes]]
            [webapp.routes.pvlists :refer [pvlists-routes]]
            [webapp.routes.pvdisp :refer [pvdisp-routes]]
            [webapp.routes.pdgmcmp :refer [pdgmcmp-routes]]
            [webapp.routes.pdgm :refer [pdgm-routes]]))

(defn init []
  (println "webapp is starting"))

(defn destroy []
  (println "webapp is shutting down"))

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (routes home-routes pdgm-routes pdgmcmp-routes pvdisp-routes pvlgpr-routes pvlgvl-routes pvprvllg-routes pvlists-routes app-routes)
      (handler/site)
      (wrap-base-url)))

;;(def app (noir-middleware/app-handler
;;          [home-routes
;;           pdgm-routes
;;           app-routes]))
