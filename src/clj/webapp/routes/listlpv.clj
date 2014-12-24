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
                    [:option {:value "plv" :label "Property-Language-Value"}]]]]
             ;;(submit-button "Get values")
             [:tr [:td ]
              [:td [:input#submit
                    {:value "Make Language-Property-Value Lists", :name "submit", :type "submit"}]]]]))))

(defn csv2table2
"Takes sorted 3-col csv list and outputs html table with empty [:td]  for repeated col1 and rows of col3 vals for repeated col2."
 [lpvs]
(let  [curcat1 (atom "")
      curcat2 (atom "")]
 [:table
(for [lpv lpvs]
 (let [catmap (zipmap [:cat1 :cat2 :cat3] (split lpv #","))]
   (if (= (:cat1 catmap) @curcat1)

     (if (= (:cat2 catmap) @curcat2)
       [:tr [:td] [:td][:td (:cat3 catmap)]]
       (do (reset! curcat2 (:cat2 catmap))
           [:tr [:td] [:td @curcat2][:td (:cat3 catmap)]]))
       
     (do (reset! curcat1 (:cat1 catmap))
         (reset! curcat2 (:cat2 catmap))
         [:tr [:td @curcat1][:td @curcat2][:td (:cat3 catmap)]]))))]))

(defn csv2table
"Takes sorted 3-col csv list and outputs html table with empty [:td]  for repeated col1 and string of col3 vals for repeated col2."
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
           (str "</td></tr><tr><td></td><td valign=top>"@curcat2"</td><td>" (:cat3 catmap) " ")))
       
     (do (reset! curcat1 (:cat1 catmap))
         (reset! curcat2 (:cat2 catmap))
         (str "</td></tr><tr><td>" @curcat1 "</td><td valign=top>" @curcat2 "</td><td>" (:cat3 catmap) " ")))))
  (str "</td></tr>")]))

(defn handle-listlpv-gen
  [ldomain colorder]
  (layout/common
   [:h3#clickable "List Type: " colorder]
    [:h3#clickable "Language Domain: " ldomain]
        ;; send SPARQL over HTTP request"
        (let [query-sparql (cond 
                      (= colorder "pvl")
                      (sparql/listpvl-sparql ldomain)
                      (= colorder "vpl")
                      (sparql/listvpl-sparql ldomain)
                      (= colorder "plv")
                      (sparql/listplv-sparql ldomain)
                      :else (sparql/listlpv-sparql ldomain))
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
              lpvtable (csv2table lpvs)]
          (log/info "sparql result status: " (:status req))
          [:div
           [:p "Column Order: " header]
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


