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
         (include-css "/js/smartmenus/sm-core.css")
         (include-css "/js/smartmenus/sm-simple.css")
         (include-js "/js/jquery-1.11.3.min.js")
         (include-js "/js/smartmenus/jquery.smartmenus.min.js")
         [:script {:type "text/javascript"}
          (str 
           "$(document).ready(function() {
            $('.sm').smartmenus({
              showFunction: function($ul, complete) {
                $ul.slideDown(250, complete);
              },
              hideFunction: function($ul, complete) {
                $ul.slideUp(250, complete);
              }
             }); 
           });")]
         ;;[:script {:type "text/javascript"} 
         ;; (str "var context=\"" (:context request) "\";")]
         ;;(include-js "//code.jquery.com/jquery-2.0.2.min.js")
         ;;            "/js/colors.js"
         ;;            "/js/site.js")
         ]
        [:body content]))))

(defn base [& content]
  (RenderablePage. content
  ))

(defn common [& content]
  (base    
      [:ul {:class "sm sm-simple"}
   [:li (link-to "/" "Home")]
   [:li (link-to "#" "Paradigms")
     [:ul
      [:li (link-to "/pdgm" "Individual Paradigms")]
      [:li (link-to "/pdgmcmp" "Paradigm Comparison")]
      [:li (link-to "/pdgmcheckbx"  "Checkbox: Monolingual Seq")]
      [:li (link-to "/pdgmcbpll"  "Checkbox: Monolingual Parallel")]
      [:li (link-to "/langcheckbx"  "Checkbox: Multilingual Display")]]]
   [:li (link-to "#" "Property-value Display")
    [:ul
     [:li (link-to "/pvlgpr" "Language-property")]
     [:li (link-to "/pvlgvl" "Language-value")]
     [:li (link-to "/pvprvllg" "Language-property-value")]]]
   [:li (link-to "#" "Utilities")
     [:ul 
      [:li (link-to "#" "List Generation:")
       [:ul
        [:li (link-to "/listlgpr"  "POS Properties")]
        [:li (link-to "/listvlcl" "POS Paradigm Value-Clusters")]
        [:li (link-to "/listmenulpv" "Lists for Menus")]
        [:li (link-to "/listlpv" "Prop-Val Indices by L-Domain")]]]
      [:li (link-to "#" "Update:")
       [:ul 
        [:li (link-to "/update" "Update Local Datastore")]
        [:li (link-to "/upload" "Upload to Remote Repository")]]]]]
   [:li (link-to "/helppage" "Help")]]

    [:div.content content]))

