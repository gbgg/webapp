(ns webapp.routes.listvlcl
 (:refer-clojure :exclude [filter concat group-by max min count replace])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [split replace join]]
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
   [:p "Will write list of paradigm-specifying value-clusters to file(s) pvlists/pname-POS-list-LANG.txt for selected language(s)."]   [:hr]
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
                    [:option {:value "nfv" :label "Non-finite Verb"}]
                    [:option {:value "pro" :label "Pronoun"}]
                    [:option {:value "noun" :label "Noun"}]
                    ]]]
             ;;(submit-button "Get pdgm")
             [:tr [:td ]
              [:td [:input#submit
                    {:value "Make PDGM Value-Clusters List", :name "submit", :type "submit"}]]]]))))

(defn req2vlist1
  [vlist]
  (let [vlist1 (replace vlist #"\r\n$" "")
        reqq (split vlist1 #"\r\n")
        ;;reqqa (first reqq)
        reqqb (rest reqq)
        reqqc  (replace reqqb #"\B,|[\(\)\"]" "")
        reqqd (replace reqqc #"[\]\[\"]" "")]
    (replace reqqd #" " "\n")))

(defn req2vlist2
  [vlist]
  (let [vlist1 (replace vlist #"\n$" "")
        reqq (split vlist1 #"\n")
        ;;reqqa (first reqq)
        reqqb (rest reqq)
        vvec (for [req reqqb] (split req #","))
        vmap (for [vvc vvec] (apply hash-map vvc))
        vmerge (apply merge-with str vmap)
        reqq2 (into [] (for [vm vmerge] (join "," vm)))
        ;; I have no idea why the following works
        reqq3 (for [r2 reqq2] (replace r2 #"\r" ","))]
    (join "\n" reqq3)
))

(defn handle-listvlcl-gen
  [ldomain pos]
  (layout/common
   [:body
    [:h3#clickable "Value-clusters used in " pos " pdgms for: " ldomain]
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
                          (= pos "nfv")
                          (sparql/listlgpr-sparql-nfv language lpref)
                          (= pos "noun")
                          (sparql/listlgpr-sparql-noun language lpref)
                          (= pos "fv")
                          (sparql/listlgpr-sparql-fv language lpref))
                ;;query-sparql1-pr (replace query-sparql1 #"<" "&lt;")
                req1 (http/get aama
                          {:query-params
                           {"query" query-sparql1 ;;generated sparql
                            "format" "csv"}})
                            ;;"format" "application/sparql-results+json"}})
                            ;;"format" "text"}})
                propstring (replace (:body req1) #"\r\n" ",")
                pstring (replace propstring #"property,|,$" "")
                plist (replace pstring #"," ", ")
                query-sparql2 (cond 
                          (= pos "pro")
                          (sparql/listvlcl-sparql-pro language lpref propstring)
                          (= pos "nfv")
                          (sparql/listvlcl-sparql-nfv language lpref propstring)
                          (= pos "noun")
                          (sparql/listvlcl-sparql-noun language lpref propstring)
                          :else (sparql/listvlcl-sparql-fv language lpref propstring))
                query-sparql2-pr (replace query-sparql2 #"<" "&lt;")
                req2 (http/get aama
                          {:query-params
                           {"query" query-sparql2 ;;generated sparql
                            ;;"format" "application/sparql-results+json"}})
                            "format" "csv"}})
                req2-body (replace (:body req2) #",+" ",")
                req2-out   (cond
                       (or (= pos "fv") (= pos "pro"))
                       (req2vlist1 req2-body)
                       :else (req2vlist2 req2-body))
              ]
        (log/info "sparql result status: " (:status req2))
        (spit outfile req2-out)
          [:div
           [:h4 "Language: "]
           [:li language]
           [:h4 "File: "]
           [:li outfile]
           ;;[:p "req2-body: " [:pre req2-body]]
           [:h4 "Property List: " ]
           [:li plist]
           [:h4  "Value Clusters: " ]
           [:pre req2-out]
           [:hr]
           ;;[:p "propstring: " [:pre propstring]]
           [:h3#clickable "Query:"]
           [:pre query-sparql2-pr]
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


