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
;;      [:li (link-to "/langInfo" "The Languages")]
      [:li (link-to "#" "The Languages")
       [:ul
        [:li (link-to "/langInfo"  "Afar")]
        [:li (link-to "/langInfo"  "Akkadian-ob")]
        [:li (link-to "/langInfo"  "Alaaba")]
        [:li (link-to "/langInfo"  "Arabic")]
        [:li (link-to "/langInfo"  "Arbore")]
        [:li (link-to "/langInfo"  "Awngi")]
        [:li (link-to "/langInfo"  "Bayso")]
        [:li (link-to "/langInfo"  "Beja-arteiga")]
        [:li (link-to "/langInfo"  "Beja-atmaan")]
        [:li (link-to "/langInfo"  "Beja-beniamer")]
        [:li (link-to "/langInfo"  "Beja-bishari")]
        [:li (link-to "/langInfo"  "Beja-hadendowa")]
        [:li (link-to "/langInfo"  "Bilin")]
        [:li (link-to "/langInfo"  "Boni-jara")]
        [:li (link-to "/langInfo"  "Boni-kijee-bala")]
        [:li (link-to "/langInfo"  "Boni-kilii")]
        [:li (link-to "/langInfo"  "Burji")]
        [:li (link-to "/langInfo"  "Burunge")]
        [:li (link-to "/langInfo"  "Coptic-sahidic")]
        [:li (link-to "/langInfo"  "Dahalo")]
        [:li (link-to "/langInfo"  "Dhaasanac")]
        [:li (link-to "/langInfo"  "Dizi")]
        [:li (link-to "/langInfo"  "Egyptian-middle")]
        [:li (link-to "/langInfo"  "Elmolo")]
        [:li (link-to "/langInfo"  "Gawwada")]
        [:li (link-to "/langInfo"  "Gedeo")]
        [:li (link-to "/langInfo"  "Geez")]
        [:li (link-to "/langInfo"  "Hadiyya")]
        [:li (link-to "/langInfo"  "Hebrew")]
        [:li (link-to "/langInfo"  "Iraqw")]
        [:li (link-to "/langInfo"  "Kambaata")]
        [:li (link-to "/langInfo"  "Kemant")]
        [:li (link-to "/langInfo"  "Khamtanga")]
        [:li (link-to "/langInfo"  "Koorete")]
        [:li (link-to "/langInfo"  "Maale")]
        [:li (link-to "/langInfo"  "Oromo")]
        [:li (link-to "/langInfo"  "Rendille")]
        [:li (link-to "/langInfo"  "Saho")]
        [:li (link-to "/langInfo"  "Shinassha")]
        [:li (link-to "/langInfo"  "Sidaama")]
        [:li (link-to "/langInfo"  "Somali-standard")]
        [:li (link-to "/langInfo"  "Syriac")]
        [:li (link-to "/langInfo"  "Tsamakko")]
        [:li (link-to "/langInfo"  "Wolaytta")]
        [:li (link-to "/langInfo"  "Yaaku")]
        [:li (link-to "/langInfo"  "Yemsa")]]]
      [:li (link-to  "#" "Bibliography References")
       [:ul
        [:li (link-to "/bibInfo" "Bibliography")]
         [:li (link-to "/bibInfo" "Almkvist1881")]
         [:li (link-to "/bibInfo" "Amha2001")]
         [:li (link-to "/bibInfo" "Appleyard1975")]
         [:li (link-to "/bibInfo" "Appleyard1975")]
         [:li (link-to "/bibInfo" "Appleyard2007a")]
         [:li (link-to "/bibInfo" "Appleyard2007b")]
         [:li (link-to "/bibInfo" "Banti-Vergari2003")]
         [:li (link-to "/bibInfo" "Beachy2005")]
         [:li (link-to "/bibInfo" "BinyamSisay2008")]
         [:li (link-to "/bibInfo" "Gragg1976")]
         [:li (link-to "/bibInfo" "Gragg1998")]
         [:li (link-to "/bibInfo" "Hayward1978")]
         [:li (link-to "/bibInfo" "Hayward1979")]
         [:li (link-to "/bibInfo" "Hayward1984a")]
         [:li (link-to "/bibInfo" "Heine1980d")]
         [:li (link-to "/bibInfo" "Heine1982")]
         [:li (link-to "/bibInfo" "Hetzron1969b ")]
         [:li (link-to "/bibInfo" "HudsonR1976")]
         [:li (link-to "/bibInfo" "Huehnergard1997")]
         [:li (link-to "/bibInfo" "Kawachi2007")]
         [:li (link-to "/bibInfo" "Kiessling1994")]
         [:li (link-to "/bibInfo" "Lambdin1971")]
         [:li (link-to "/bibInfo" "Lambdin1983")]
         [:li (link-to "/bibInfo" "Lamberti-Sottile1997")]
         [:li (link-to "/bibInfo" "Lamberti1993a")]
         [:li (link-to "/bibInfo" "Lamberti1993b")]
         [:li (link-to "/bibInfo" "Mous1993")]
         [:li (link-to "/bibInfo" "Orwin1995")]
         [:li (link-to "/bibInfo" "Parker-Hayward1985 ")]
         [:li (link-to "/bibInfo" "Pillinger-Galboran1999")]
         [:li (link-to "/bibInfo" "Reinisch1893")]
         [:li (link-to "/bibInfo" "Roper1928")]
         [:li (link-to "/bibInfo" "Saeed1999")]
         [:li (link-to "/bibInfo" "Sava2005")]
         [:li (link-to "/bibInfo" "Schneider-Blom2007")]
         [:li (link-to "/bibInfo" "SimM1985")]
         [:li (link-to "/bibInfo" "SimR1985")]
         [:li (link-to "/bibInfo" "Thackston1994")]
         [:li (link-to "/bibInfo" "Thackston1999")]
         [:li (link-to "/bibInfo" "Tosco1991")]
         [:li (link-to "/bibInfo" "Tosco2001b")]
         [:li (link-to "/bibInfo" "Tosco2007b")]
         [:li (link-to "/bibInfo" "Wedekind-etal2007")]
         [:li (link-to "/bibInfo" "WedekindC1985")]
         [:li (link-to "/bibInfo" "WedekindK1985")]
         [:li (link-to "/bibInfo" "Welmers1952")]]]]]
       [:li (link-to "#" "Paradigms")
        [:ul
         [:li (link-to "#" "Single Paradigm")
          [:ul
           [:li (link-to "#" "Afar")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Akkadian-ob")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Alaaba")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Arabic")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Arbore")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Awngi")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Bayso")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Beja-arteiga")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Beja-atmaan")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Beja-beniamer")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Beja-bishari")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Beja-hadendowa")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Bilin")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Boni-jara")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Boni-kijee-bala")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Boni-kilii")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Burji")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Burunge")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Coptic-sahidic")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Dahalo")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Dhaasanac")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Dizi")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Egyptian-middle")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Elmolo")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Gawwada")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Gedeo")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Geez")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Hadiyya")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Hebrew")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Iraqw")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Kambaata")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Kemant")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Khamtanga")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Koorete")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Maale")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Oromo")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Rendille")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Saho")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Shinassha")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Sidaama")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Somali-standard")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Syriac")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Tsamakko")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Wolaytta")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Yaaku")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]
          [:li (link-to "#" "Yemsa")
            [:ul
             [:li (link-to "/pdgm" "Finite Verb")]
             [:li (link-to "/pdgm" "Non-Finite Verb")]
             [:li (link-to "/pdgm" "Pronoun")]
             [:li (link-to "/pdgm" "Noun")]]]]]
         [:li (link-to "#" "Multiple Paradigms")
          [:ul
           [:li (link-to "/multipdgmseq" "Default Display")]
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
       [:li (link-to "#" "Help")
        [:ul
         [:li (link-to "/helppdgms" "Paradigms")]
         [:li (link-to "/helppvdisp" "Property Value Displays")]
         [:li (link-to "/helplistgen" "List Generation")]
         [:li (link-to "#" "Update")
          [:ul
           [:li (link-to "/helpwebupdate" "Webapp")]
           [:li (link-to "/helpclupdate" "Command Line")]]]]]]
      
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
