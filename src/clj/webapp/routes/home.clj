(ns webapp.routes.home
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]))

(defn home []
  (layout/common))


(defroutes home-routes
  (GET "/" [] (home)))
