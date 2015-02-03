(ns webapp.routes.listvlcl
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
            [hiccup.form :refer :all]))

(def aama "http://localhost:3030/aama/query")

(defn listvlcl []
  (let [langlist (slurp "pvlists/langlist.txt")
        languages (split langlist #"\n")
        ldomlist (slurp "pvlists/ldomainlist.txt")
        ldoms (split ldomlist #"\n")]
  (layout/common 
   [:h3 "PDGM Value-Cluster List"]
   [:p "(Only 'Finite Verb' enabled at this time.)"]
   [:hr]
   (form-to [:post "/listvlcl-gen"]
            [:table
             [:tr [:td "PDGM Language Domain: " ]
              [:td [:select#ldomain.required
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
              [:tr [:td "Part of Speech: "]
              [:td [:select#pos.required
                    {:title "Choose a pdgm type.", :name "pos"}
                    [:option {:value "fv" :label "Finite Verb"}]
                    [:option {:disabled "disabled" :value "nfv" :label "Non-finite Verb"}]
                    [:option {:value "pro" :label "Pronoun"}]
                    [:option {:disabled "disabled" :value "noun" :label "Noun"}]
                    ]]]
             ;;(submit-button "Get pdgm")
             [:tr [:td ]
              [:td [:input#submit
                    {:value "Make PDGM Value-Clusters List", :name "submit", :type "submit"}]]]]))))

(defn handle-listvlcl-gen
  [ldomain pos]
  (layout/common
   [:body
    [:h3#clickable "Properties used in " pos " pdgms for: " ldomain]
      (let [lprefmap (read-string (slurp "pvlists/lprefs.clj"))
            langs (split ldomain #",")]
        (for [language langs]
          (let [
            lang (read-string (str ":" language))
            lpref (lang lprefmap)
            ;; send SPARQL over HTTP request
            outfile (str "pvlists/pname-" pos "-list-" language ".txt")
            query-sparql1 (cond 
                          (= pos "pro")
                          (sparql/listlgpr-sparql-pro language lpref)
                          ;;(= pos "nfv")
                          ;;(sparql/listlgpr-sparql-nfv language lpref)
                          ;;(= pos "noun")
                          ;;(sparql/listlgpr-sparql-noun language lpref)
                          :else (sparql/listlgpr-sparql-fv language lpref))
            query-sparql1-pr (replace query-sparql1 #"<" "&lt;")
            req1 (http/get aama
                          {:query-params
                           {"query" query-sparql1 ;;generated sparql
                            "format" "csv"}})
                            ;;"format" "application/sparql-results+json"}})
                            ;;"format" "text"}})
            propstring (replace (:body req1) #"\r\n" ",")
            query-sparql2 (cond 
                          (= pos "pro")
                          (sparql/listvlcl-sparql-pro language lpref propstring)
                          ;;(= pos "nfv")
                          ;;(sparql/listvlcl-sparql-nfv language lpref propstring)
                          ;;(= pos "noun")
                          ;;(sparql/listvlcl-sparql-noun language lpref propstring)
                          :else (sparql/listvlcl-sparql-fv language lpref propstring))
            query-sparql2-pr (replace query-sparql2 #"<" "&lt;")
            req2 (http/get aama
                          {:query-params
                           {"query" query-sparql2 ;;generated sparql
                            ;;"format" "application/sparql-results+json"}})]
                            "format" "csv"}})
            req2-pr1 (replace (:body req2) #",+" ",")
            req2-pr2 (replace req2-pr1 #"\B," "")
              ]
        (log/info "sparql result status: " (:status req2))
        (spit outfile req2-pr2)
          [:div
           [:h4 "Language: " language]
           [:p "File: " outfile]
           [:pre req2-pr2]
           [:hr]
           ;;[:h3#clickable "Query:"]
           ;;[:pre query-sparql2-pr]
           ])))
          [:script {:src "js/goog/base.js" :type "text/javascript"}]
          [:script {:src "js/webapp.js" :type "text/javascript"}]
          [:script {:type "text/javascript"}
           "goog.require('webapp.core');"]]))

(defroutes listvlcl-routes
  (GET "/listvlcl" [] (listvlcl))
  (POST "/listvlcl-gen" [ldomain pos] (handle-listvlcl-gen ldomain pos))
  ;;(POST "/lgvldisplay" [ldomain lval] (handle-lgvldisplay ldomain lval))
  )


