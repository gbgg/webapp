(ns webapp.core     
  (:use [jayq.core :only [$]])
  (:require [jayq.core :as jq]))

;; (enable-console-print!)

;; (println "Hello howdy world!")

;;(defn handle-click []
;;  (js/alert " Cf. cljs code in  src/cljs/webapp/core.cljs"))

;;(def clickable (.getElementById js/document "clickable"))

;;(.addEventListener clickable "click" handle-click)
(def $clickable ($ :#clickable))
 
(jq/bind $clickable :click (fn [evt] (js/alert "Cf. cljs code in  src/cljs/webapp/core.cljs")))





























