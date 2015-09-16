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
         (include-css "/css/sm-core.css")
         (include-css "/css/sm-simple.css")
         (include-css "/css/dragtable.css")
         (include-js "/js/jquery-1.11.3.min.js")
         (include-js "/js/jquery-ui.min.js")
         (include-js "/js/jquery.smartmenus.min.js")
         (include-js "/js/jquery.dragtable.js")
         (include-js "/js/jquery.tablesorter.min.js")
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
             $('#handlerTable').dragtable({dragHandle:'.some-handle'});
             $('#handlerTable').tablesorter();
             // http://www.sanwebe.com/2014/01/how-to-select-all-deselect-checkboxes-jquery
             $('#selectall').click(function(event) {
               if(this.checked) {
                 $('.checkbox1').each(function() {
                  this.checked = true;               
                 });
               }else{
                 $('.checkbox1').each(function() {
                   this.checked = false;                       
                 });         
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
   [:li (link-to "#" "Home")
     [:ul
      [:li (link-to "/aamaApp" "The AAMA Application")]
      [:li (link-to "/langInfo" "The Languages")]
      [:li (link-to "/bibInfo" "Bibliography")]]]
   [:li (link-to "#" "Paradigms")
     [:ul
      [:li (link-to "/pdgm" "Single Paradigm")]
      [:li (link-to "#" "Multiple Paradigms")
       [:ul
        [:li (link-to "/multipdgmseq" "Sequential Display")]
        [:li (link-to "/multipdgmmod" "Modifiable Display")]]]]]
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
        [:li (link-to "/listvlclplex" "POS Paradigm Value-Clusters")]
        [:li (link-to "/listmenulpv" "Lists for Menus")]
        [:li (link-to "/listlpv" "Prop-Val Indices by L-Domain")]]]
      [:li (link-to "#" "Update:")
       [:ul 
        [:li (link-to "/update" "Update Local Datastore")]
        [:li (link-to "/upload" "Upload to Remote Repository")]]]]]
   [:li (link-to "/helppage" "Help")]]

    [:div.content content]))

;;(defn common [& content]
;;  (base
;;    [:div#usermenu
;;   [:div.menuitem (link-to "/" "Home")]
;;   [:div.menuitem (link-to "/pdgmpage" "Paradigms")]
;;   [:div.menuitem (link-to "/pvdisp" "Property-value Display")]
;;   [:div.menuitem (link-to "/utilities" "Utilities")]
;;   [:div.menuitem (link-to "/trial" "Trial")]]
;;    [:div.content content]))
