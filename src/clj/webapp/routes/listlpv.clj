(ns webapp.routes.listlpv
 (:refer-clojure :exclude [filter concat group-by max min count replace])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [split replace]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.form :refer :all]
            ;;[clojure-csv.core :as csv]
            ))

(def aama "http://localhost:3030/aama/query")

(defn listlpv []
  (let [langlist (slurp "pvlists/langlist.txt")
        languages (split langlist #"\n")
        ldomlist (slurp "pvlists/ldomainlist.txt")
        ldoms (split ldomlist #"\n")
        lvallist (slurp "pvlists/langvals.txt")
        lvals (split lvallist #"\n")]
  (layout/common 
   [:h3 "Language-Property-Value Co-occurrences"]
   [:p "Will write requested Language-Property-Value co-occurrence list to file(s) pvlists/pname-POS-list-LANG.txt for selected language(s)."] 
   [:hr]
   (form-to [:post "/listlpv-gen"]
            [:table
             [:tr [:td "Language Domain: " ]
              [:td 
               [:select#ldomain.required
               {:title "Choose a language domain.", :name "ldomain"}
                [:optgroup {:label "Languages"} 
                (for [language languages]
                (let [opts (split language #" ")]
               [:option {:value (first opts)} (last opts) ]))]
                [:optgroup {:label "Language Families"} 
               (for [ldom ldoms]
                (let [opts (split ldom #" ")]
               [:option {:value (last opts)} (first opts) ]))
                 [:option {:disabled "disabled"} "Other"]]]]]
             [:tr [:td "Column Order: "]
              [:td [:select#colorder.required
                    {:title "Choose a column order.", :name "colorder"}
                    [:option {:value "lpv" :label "Language-Property-Value"}]
                    [:option {:value "pvl" :label "Property-Value-Language"}]
                    [:option {:value "vpl" :label "Value-Property-Language"}]
                    [:option {:value "plv" :label "Property-Language-Value"}]
                    [:option {:value "cpvl" :label "PClass-Property-Value-Language"}]
                    [:option {:value "lpvmod" :label "Drag/Sort Language-Property-Value"}]]]]
             ;;(submit-button "Get values")
             [:tr [:td ]
              [:td [:input#submit
                    {:value "Make Language-Property-Value Lists", :name "submit", :type "submit"}]]]]))))

(defn csv2cpvl
"Takes sorted 3-col csv list and outputs 4-col html table with empty col1 for prop-class and string of col4 vals for repeated col3. [ONLY USEFUL WHEN PROPERTY CLASSES HAVE BEEN ESTABLISHED.]"
 [lpvs]
(let  [curcat1 (atom "")
      curcat2 (atom "")]
  ;; For visible borders set {:border "1"}.
 [:table {:border "0"}
(for [lpv lpvs]
 (let [catmap (zipmap [:cat1 :cat2 :cat3] (split lpv #","))]
   (if (= (:cat1 catmap) @curcat1)

     (if (= (:cat2 catmap) @curcat2)
       (str (:cat3 catmap) " ")
       (do (reset! curcat2 (:cat2 catmap))
           (str "</td></tr><tr><td width=\"100\"></td><td></td><td valign=top>"@curcat2"</td><td>" (:cat3 catmap) " ")))
       
     (do (reset! curcat1 (:cat1 catmap))
         (reset! curcat2 (:cat2 catmap))
         (str "</td></tr><tr><td width=\"100\">(other)</td><td>" @curcat1 "</td><td valign=top>" @curcat2 "</td><td>" (:cat3 catmap) " ")))))
  (str "</td></tr>")]))

(defn csv2table
"Takes sorted 3-col csv list and outputs html table with empty [:td]  for repeated col1 and string of col3 vals for repeated col2."
 [lpvs]
(let  [curcat1 (atom "")
      curcat2 (atom "")]
  ;; For visible borders set {:border "1"}.
 [:table {:border "1"}
(for [lpv lpvs]
 (let [catmap (zipmap [:cat1 :cat2 :cat3] (split lpv #","))]
   (if (= (:cat1 catmap) @curcat1)

     (if (= (:cat2 catmap) @curcat2)
       (str (:cat3 catmap) " ")
       (do (reset! curcat2 (:cat2 catmap))
           (str "</td></tr><tr><td></td><td valign=top>"@curcat2"</td><td>" (:cat3 catmap) " ")))
       
     (do (reset! curcat1 (:cat1 catmap))
         (reset! curcat2 (:cat2 catmap))
         (str "</td></tr><tr><td>" @curcat1 "</td><td valign=top>" @curcat2 "</td><td>" (:cat3 catmap) " ")))))
  (str "</td></tr>")]))

(defn csv2tablemod
"Takes sorted 3-col csv list and outputs sortable, draggable html table."
 [header lpvs]
(let  [curcat1 (atom "")
      curcat2 (atom "")
       heads (split header #",")]
  ;; For visible borders set {:border "1"}.
  [:table {:id "handlerTable" :class "tablesorter sar-table"}
   [:thead
    (for [head heads]
      [:th [:div {:class "some-handle"}] head])]
   [:tbody
    (for [lpv lpvs]
      [:tr
      (let [cats (split lpv #",")]
        (for [cat cats]
          [:td cat]))])]]))

(defn handle-listlpv-gen
  [ldomain colorder]
  (layout/common
   [:h3#clickable "List Type: " colorder]
    [:h3#clickable "Language Domain: " ldomain]
        ;; send SPARQL over HTTP request"
        (let [query-sparql (cond 
                      (= colorder "lpv")
                      (sparql/listlpv-sparql ldomain)
                      (= colorder "lpvmod")
                      (sparql/listlpv-sparql ldomain)
                      (= colorder "vpl")
                      (sparql/listvpl-sparql ldomain)
                      (= colorder "plv")
                      (sparql/listplv-sparql ldomain)
                      :else (sparql/listpvl-sparql ldomain))
              query-sparql-pr (replace query-sparql #"<" "&lt;")
              req (http/get aama
                            {:query-params
                             {"query" query-sparql ;;generated sparql
                              ;;"format" "application/sparql-results+json"}})]
                              ;;"format" "text"}})
                              "format" "csv"}})
              ;;reqvec (csv/parse-csv req)
              reqvec (split (:body req) #"\n")
              header (first reqvec)
              lpvs (rest reqvec)
              lpvtable (cond
                        (= colorder "cpvl")
                        (csv2cpvl lpvs)
                        (= colorder "lpvmod")
                        (csv2tablemod header lpvs)
                        :else (csv2table lpvs))]
          (log/info "sparql result status: " (:status req))
          [:div
           [:p "Column Order: " colorder]
           [:hr]
           lpvtable
          [:hr]
          [:h3#clickable "Query:"]
          [:pre query-sparql-pr]])
          [:script {:src "js/goog/base.js" :type "text/javascript"}]
          [:script {:src "js/webapp.js" :type "text/javascript"}]
          [:script {:type "text/javascript"}
           "goog.require('webapp.core');"]))

(defroutes listlpv-routes
  (GET "/listlpv" [] (listlpv))
  (POST "/listlpv-gen" [ldomain colorder] (handle-listlpv-gen ldomain colorder)))


