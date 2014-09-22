(ns webapp3.views.layout
  (:require [hiccup.page :refer [html5 include-css]]))

(defn common [& body]
  (html5
    [:head
     [:title "Welcome to the Afroasiatic Morphological Archive"]
     (include-css "/css/screen.css")]
    [:body body]))
