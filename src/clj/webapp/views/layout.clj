(ns webapp.views.layout
  (:require [hiccup.page :refer [html5 include-css]]
            [hiccup.element :refer [link-to]]
            [hiccup.form :refer :all]
            [hiccup.page :refer [include-css include-js]]
            [ring.util.response :refer [content-type response]]
            [compojure.response :refer [Renderable]]))

(defn utf-8-response [html]
  (content-type (response html) "text/html; charset=utf-8"))

(deftype RenderablePage [content]
  Renderable
  (render [this request]
    (utf-8-response
      (html5
        [:head
         [:title "Welcome to the Afroasiatic Morphological Archive"]
         (include-css "/css/screen.css")
         [:script {:type "text/javascript"} 
          (str "var context=\"" (:context request) "\";")]
         ;;(include-js "//code.jquery.com/jquery-2.0.2.min.js"
         ;;            "/js/colors.js"
         ;;            "/js/site.js")
         ]
        [:body content]))))

(defn base [& content]
  (RenderablePage. content
  ))

(defn make-menu [& items]
  [:div#usermenu (for [item items] [:div.menuitem item])])

(defn user-menu []
  (make-menu 
    (link-to "/" "home")
    (link-to "/pdgm" "paradigm choice")
    (link-to "/pdgmcmp" "paradigm comparison")
    (link-to "/propval" "property-value displays")
    (link-to "/lists" "property-vaue lists")))

(defn common [& content]
  (base    
    [:div#usermenu 
   [:div.menuitem (link-to "/" "Home")]
   [:div.menuitem (link-to "/pdgm" "Paradigms")]
   [:div.menuitem (link-to "/pdgmcmp" "Paradigm Comparison")]
   [:div.menuitem (link-to "/pvdisp" "Property-value Display")]
   [:div.menuitem (link-to "/utilities" "Utilities")]
   [:div.menuitem (link-to "/trial" "Trial")]]
    [:div.content content]))

