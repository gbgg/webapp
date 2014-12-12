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
                    [:option {:value "plv" :label "Property-Language-Value"}]
                    ]]]

             ;;(submit-button "Get values")
             [:tr [:td ]
              [:td [:input#submit
                    {:value "Make Language-Property-Value Lists", :name "submit", :type "submit"}]]]]))))

(defn csv2table 
"Takes sorted 3-col csv list and outputs html table with empty [:td]  for repeated col1 and vec of col3 vals for repeated col2. [IN PROGRESS!]"
 [lpvs]
 [:table
(doseq [lpv lpvs]
 (let [curmap (zipmap [:cat1 :cat2 :cat3] (split (first lpvs) #","))
       nextmap (zipmap [:cat1 :cat2 :cat3] (split (first (rest lpvs)) #","))
       ;;cur3str (str "")
       cur3vec (vector "")
       ]
   (if (= (:cat1 curmap) (:cat1 nextmap))
     
     (if (= (:cat2 curmap) (:cat2 nextmap))
       (conj cur3vec (:cat3 curmap))
       [:tr [:td] [:td (:cat2 curmap)] [:td (str cur3vec " " (:cat3 curmap))]])
             
     [:tr [:td (:cat1 curmap)] [:td (:cat2 curmap)] [:td (str cur3vec " " (:cat3 curmap))]])))])

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
                              ;;"format" "text"}})]
                              "format" "csv"}})
              ;;reqvec (csv/parse-csv req)
              reqvec (split (:body req) #"\n")
              header (first reqvec)
              lpvs (rest reqvec)
              lpvtable (csv2table lpvs)
              ]
          (log/info "sparql result status: " (:status req))
          [:div
          [:pre (:body req)]
           [:p header]
           [:hr]
           ;;[:pre reqvec]
           [:pre lpvtable]
          [:hr]
          [:h3#clickable "Query:"]
          [:pre query-sparql-pr]])
          [:script {:src "js/goog/base.js" :type "text/javascript"}]
          [:script {:src "js/webapp.js" :type "text/javascript"}]
          [:script {:type "text/javascript"}
           "goog.require('webapp.core');"]))

(defroutes listlpv-routes
  (GET "/listlpv" [] (listlpv))
  (POST "/listlpv-gen" [ldomain colorder] (handle-listlpv-gen ldomain colorder))
  ;;(POST "/lgvldisplay" [ldomain lval] (handle-lgvldisplay ldomain lval))
  )


