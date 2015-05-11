(ns webapp.routes.pdgm
 (:refer-clojure :exclude [filter concat group-by max min count replace])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [capitalize replace split]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.form :refer :all]))

(def aama "http://localhost:3030/aama/query")

(defn pdgm []
  (let [langlist (slurp "pvlists/langlist.txt")
        languages (split langlist #"\n")]
  (layout/common 
   [:h3 "Individual Paradigms"]
   [:h4 "Choose Language and Type"]
   [:p  "This query-type prompts for a \"paradigm-type\" (Finite Verb, Non-finite Verb, Pronoun, Noun) and a language; it then shows a drop-down select list of paradigms in that language of that type, and returns a table-formatted display of the selected paradigm."]
   ;; [:p error]
   [:hr]
   (form-to [:post "/pdgmqry"]
            [:table
             [:tr [:td "PDGM Language: " ]
              [:td [:select#language.required
                    {:title "Choose a language.", :name "language"}
                    (for [language languages]
                      (let [opts (split language #" ")]
                        [:option {:value (first opts)} (last opts) ]))]]]
             [:tr [:td "PDGM Type: "]
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
                    {:value "Get PDGM Value Clusters", :name "submit", :type "submit"}]]]]
            )
   [:hr])))

(defn display-valclusters
  [language pos]
   (let [valclusterfile (str "pvlists/pname-" pos "-list-" language ".txt")
        valclusterlist (slurp valclusterfile)
        valclusters (clojure.string/split valclusterlist #"\n")]
    (layout/common 
     [:h3 "Paradigms"]
     [:p "Choose Value Clusters"]
     ;;[:p error]
     [:hr]
     (form-to [:post "/pdgmdisplay"]
        [:table
         [:tr [:td "PDGM Language: " ]
          [:td [:select#language.required
               {:title "Choose a language.", :name "language"}
                [:option {:value language :label (clojure.string/capitalize language)}]
                ]]]
         [:tr [:td "PDGM Type: "]
          [:td [:select#pos.required
                {:title "Choose a pdgm type.", :name "pos"}
                [:option {:value pos :label (clojure.string/upper-case pos)}]
                ]]]
         [:tr [:td "PDGM Value Clusters: " ]
          [:td [:select#valstring.required
                {:title "Choose a value.", :name "valstring"}
                (for [valcluster valclusters]
                  [:option  valcluster])
                ]]]
         ;;(submit-button "Get pdgm")
         [:tr [:td ]
          [:td [:input#submit
                {:value "Display pdgm", :name "submit", :type "submit"}]]]]
     [:hr]))))

(defn handle-pdgmqry
  [language pos]
  (let [valclusterfile (str "pvlists/pname-" pos "-list-" language ".txt")]
   (try
    (slurp valclusterfile)
    (finally (println (str language " has no paradigms of type " pos))))
   (display-valclusters language pos)))

(defn handle-pdgmdisplay
  [language valstring pos]
  ;; send SPARQL over HTTP request
  (let [Language (capitalize language)
        lprefmap (read-string (slurp "pvlists/lprefs.clj"))
        lang (read-string (str ":" language))
        lpref (lang lprefmap)
        valstrng (clojure.string/replace valstring #",*person|,*gender|,*number" "")
        valstr (clojure.string/replace valstrng #":," ":")
        query-sparql (cond 
                      (= pos "pro")
                      (sparql/pdgmqry-sparql-pro language lpref valstr)
                      (= pos "nfv")
                      (sparql/pdgmqry-sparql-nfv language lpref valstring)
                      (= pos "noun")
                      (sparql/pdgmqry-sparql-noun language lpref valstring)
                      :else (sparql/pdgmqry-sparql-fv language lpref valstring))
        query-sparql-pr (replace query-sparql #"<" "&lt;")
        req (http/get aama
                      {:query-params
                       {"query" query-sparql ;;generated sparql
                        ;;"format" "application/sparql-results+json"}})]
                        "format" "text"}})]
         (log/info "sparql result status: " (:status req))
         (layout/common
          [:body
           [:h3#clickable "Paradigm: " Language " / " valstring]
           [:pre (:body req)]
           [:hr]
           [:h3#clickable "Query:"]
           [:pre query-sparql-pr]

           [:script {:src "js/goog/base.js" :type "text/javascript"}]
           [:script {:src "js/webapp.js" :type "text/javascript"}]
           [:script {:type "text/javascript"}
            "goog.require('webapp.core');"]])))

(defroutes pdgm-routes
  (GET "/pdgm" [] (pdgm))
  (POST "/pdgmqry" [language pos] (handle-pdgmqry language pos))
  (POST "/pdgmdisplay" [language valstring pos] (handle-pdgmdisplay language valstring pos))
  )
