(ns webapp.core)

;; (enable-console-print!)

;; (println "Hello howdy world!")

(defn handle-click []
  (js/alert "Cf. core.cljs"))

(def clickable (.getElementById js/document "clickable"))

(.addEventListener clickable "click" handle-click)





























