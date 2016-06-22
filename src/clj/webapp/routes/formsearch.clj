(ns webapp.routes.formsearch
 (:refer-clojure :exclude [filter concat group-by max min])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [capitalize lower-case upper-case split join]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.element :refer [link-to]]
            [hiccup.form :refer :all]))

(def aama "http://localhost:3030/aama/query")

(defn formsearch []
  (let [langlist (slurp "pvlists/menu-langs.txt")
        languages (split langlist #"\n")]
  (layout/common 
   [:h3 "Language-Property-Value Cooccurrences"]
   [:h4 "Choose Language Domain and Enter qstring: prop=Val,...prop=?prop,..."]
   (form-to [:post "/formdisplay"]
            [:table
             [:tr [:td "Language(s) to be Queried: " ]
              [:td 
               {:title "Choose one or more languages.", :name "language"}
               (for [language languages]
                 ;;[:option {:value (lower-case language)} language])]]]
                 [:div {:class "form-group"}
                  [:label 
                   (check-box {:name "languages[]" :value (lower-case language)} language) language]])]]
             [:tr [:td "Query List: " ]
              [:td 
               (text-field 
                {:placeholder "person=Person2,gender=Fem,pos=?pos,number=?number"} 
                "qstring") ]]
             [:tr [:td "RE Form Filter: " ]
              [:td
               (text-field {:placeholder ""} "filter")]]
              ;;(submit-button "Get values")
             [:tr [:td ]
              [:td [:input#submit
                    {:value "Get Queried Forms ", :name "submit", :type "submit"}]]]]))))

(defn csv2formtable
"Takes sorted n-col csv list with vectors of headers, and outputs n-col html table."
 [heads formstr]
(let  [formrows (split formstr #"\r\n")]
  [:div
   (form-to [:post "/formpvlist"]
   [:table {:id "handlerTable" :class "tablesorter sar-table"}
    [:thead
     [:tr
      (for [head heads]
        [:th [:div {:class "some-handle"}[:br] (upper-case head)]])]]
    [:tbody 
     (for [formrow formrows]
       (let [formrow1 (clojure.string/replace formrow #"(.*,.*),(.*,.*)$" "$1%%$2")
             formrow2 (split formrow1 #"%%")
             qprops (split (first formrow2) #",")
             tokenID (last formrow2)
             dataID (first (split tokenID #","))
             token (last (split tokenID #","))]
         [:tr
           (for [qprop qprops]
            [:td qprop])
          [:td dataID]
          [:td 
           [:label (str token " : ") (check-box {:name "tokenIDs[]" :value tokenID} token) ]]]))
     [:tr
      (for [x (range 1 (count heads) )]
            [:td])
          [:td
           [:input#submit {:value "Show", :name "submit", :type "submit"}]]]]])]))

(defn handle-formdisplay
  "This version has form for parallel display of tokens."
  [languages qstring filter]
  ;; send SPARQL over HTTP request
  (let [query-sparql (sparql/prvllg-sparql languages qstring filter)
        query-sparql-pr (clojure.string/replace query-sparql #"<" "&lt;")
        req (http/get aama
                      {:query-params
                       {"query" query-sparql ;;generated sparql
                        ;;"format" "application/sparql-results+json"}})]
                        ;;"format" "text"}})]
                        "format" "csv"}})
        csvstring (:body req)
        csvstr (split csvstring #"\r\n" 2)
        ;; csvstr is a string of comma-separated cells, with rows separated by \r\n 
        ;; Take off the top header
        headers (first csvstr)
        heads (split headers #",")
        formstring (last csvstr)
        formstring2 (clojure.string/replace formstring #"\r\n$" "")
        ;; the following is to strip the lang name from the pdgmLabel
        ;;formstring3  (clojure.string/replace formstring2 #"(\r\n.*,.*),.*?-(.*,)" "$1,$2")
        formtable (csv2formtable heads formstring2)]
    (log/info "sparql result status: " (:status req))
    (layout/common
     [:body
      [:h3#clickable "Language-Property-Values: " ]
      [:p [:h4 "Language Domain: "]
       [:em (str languages)]]
      [:p [:h4 "Query String: "]
       [:em qstring]]
      [:p "Click on column to sort (multiple sort by holding down shift key). Columns can be dragged by clicking and holding on 'drag-bar' at top of column."]
      [:p "Click in check-box next to token for complete MS analysis of form."]
      [:hr]
      formtable
      [:hr]
      [:div [:h4 "======= Debug Info: ======="]
       [:p "formstring2: "]
       [:p [:pre formstring2]]
       [:p "Query: "]
       [:p [:pre query-sparql-pr]]
       [:p "Response: "]
       [:p [:pre csvstring]]
       [:h4 "==========================="]]
      [:script {:src "js/goog/base.js" :type "text/javascript"}]
      [:script {:src "js/webapp.js" :type "text/javascript"}]
      [:script {:type "text/javascript"}
       "goog.require('webapp.core');"]])))

(defn handle-formpvlist
  [tokenIDs]
  (layout/common
   [:body
    [:p [:h3 "Form Property-Value Table: "] ]
     [:hr]
     [:table 
      [:thead
       [:tr
        [:th  [:br] "TOKEN" ]
        [:th  [:br]  "DATA ID" ]
        [:th  [:br]  "MS VALUES" ]
        ;;[:th [:div {:class "some-handle"} [:br]  "query"]]
        ]]
      [:tbody 
       (for [tokenID tokenIDs]
         [:tr
          (let [dataID (first (split tokenID #","))
                token (last (split tokenID #","))
                query-sparql (sparql/formpv-sparql tokenID)
                req (http/get aama
                              {:query-params
                               {"query" query-sparql ;;generated sparql
                                ;;"format" "application/sparql-results+json"}})]
                                ;;"format" "csv"}})]
                                "format" "text"}})]
            [:div
             [:td token]
             [:td dataID] 
             [:td [:pre (:body req)]]
             ;;[:td [:pre query-sparql]]
             ]
            )])]]
       [:hr]
      [:p " "]
       [:div [:h4 "======= Debug Info: ======="]
        [:p "tokenIDs: " [:pre tokenIDs]]
        [:p "tokenIDs: " (str tokenIDs)]
        ;;[:p "token: " [:pre token]]
        ;;[:p "dataID: " [:pre dataID]]
        ;;[:p "req: "  [:pre (:body req)]]
        [:p "==========================="]]
      [:script {:src "js/goog/base.js" :type "text/javascript"}]
      [:script {:src "js/webapp.js" :type "text/javascript"}]
      [:script {:type "text/javascript"}
       "goog.require('webapp.core');"]]))

(defroutes formsearch-routes
  (GET "/formsearch" [] (formsearch))
  ;;(POST "/pdgmqry" [language pos] (handle-pdgmqry language pos))
  (POST "/formdisplay" [languages qstring filter] (handle-formdisplay languages qstring filter))
  (POST "/formpvlist" [tokenIDs] (handle-formpvlist tokenIDs)) 
  )


