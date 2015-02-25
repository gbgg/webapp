(ns webapp.routes.pdgmcbpll
 (:refer-clojure :exclude [filter concat group-by max min count])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [capitalize split]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.element :refer [link-to]]
            [hiccup.form :refer :all]))


(def aama "http://localhost:3030/aama/query")

(defn pdgmcbpll []
  (let [langlist (slurp "pvlists/langlist.txt")
        languages (split langlist #"\n")]
  (layout/common 
   [:h3 "Paradigm Checkbox"]
     [:p "Use this option to pick a number of paradigms from a given language to be displayed in vertical succession."]
   [:p "Choose Language and Type"]
   ;; [:p error]
   [:hr]
   (form-to [:post "/pdgmcbqry"]
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

(defn display-valcluster-checkbox
  [language pos]
   (let [valclusterfile (str "pvlists/pname-" pos "-list-" language ".txt")
        valclusterlist (slurp valclusterfile)
        valclusters (clojure.string/split valclusterlist #"\n")]
    (layout/common 
     [:h3 "Paradigms"]
     [:p "Choose Value Clusters For: " language "/" pos]
     ;;[:p error]
     [:hr]
     (form-to [:post "/pdgmsdisplay"]
        [:table
         [:tr [:td "PDGM Language: " ]
          ;; change language & pos selects to checked checkbox
          [:td
           (check-box {:name "language" :value language :checked "true"} language) (str language)]]
         [:tr [:td "PDGM Type: " ]
          [:td
          (check-box {:name "pos" :value pos :checked "true"} pos) (str pos)]]
         ;; Problem: if checkbox ':name  "valcluster"', only last will
         ;; be passed to pdgmsdisplay; if ':name valcluster' get error
         ;; java.lang.Character cannot be cast to java.util.Map$Entry
         [:tr [:td "PDGM Value Clusters: " ]
          [:td 
                {:title "Choose a value.", :name "valcluster"}
                (for [valcluster valclusters]
                  [:div {:class "form-group"}
                   [:label
                    (check-box {:name "valclusters[]" :value valcluster} valcluster) (str valcluster)]]
                   ;; from https://groups.google.com/forum/#!topic/compojure/5Vm8QCQLsaQ
                   ;; (check-box "valclusters[]" false valcluster) (str valcluster)]]
                )]]
         ;;(submit-button "Get pdgm")
         [:tr [:td ]
          [:td [:input#submit
                {:value "Display pdgms", :name "submit", :type "submit"}]]]]
     [:hr]))))

(defn handle-pdgmcbqry
  [language pos]
  (let [valclusterfile (str "pvlists/pname-" pos "-list-" language ".txt")]
   (try
    (slurp valclusterfile)
    (finally (println (str language " has no paradigms of type " pos))))
   (display-valcluster-checkbox language pos)))

(defn handle-pdgmsdisplay
  [language valclusters pos]
  ;; send SPARQL over HTTP request
  (let [Language (capitalize language)
        lprefmap (read-string (slurp "pvlists/lprefs.clj"))
        lang (read-string (str ":" language))
        lpref (lang lprefmap)]
        ;; here to "(layout/common"  see pdgm.clj 104-121
         (layout/common
          ;;[:body
           [:h3#clickable "Paradigm " Language " -  " pos ": "  ]
           (for [valcluster valclusters]
              (let [valclstr (clojure.string/replace valcluster #"[\n\r]" "") 
                    valstrng (clojure.string/replace valclstr #",*person|,*gender|,*number" "")
                    valstr (clojure.string/replace valstrng #":," ":")
                    query-sparql (cond 
                            (= pos "pro")
                            (sparql/pdgmqry-sparql-pro language lpref valstr)
                            (= pos "nfv")
                            (sparql/pdgmqry-sparql-nfv language lpref valclstr)
                            (= pos "noun")
                            (sparql/pdgmqry-sparql-noun language lpref valclstr)
                            :else (sparql/pdgmqry-sparql-fv language lpref valclstr))
                    query-sparql-pr (clojure.string/replace query-sparql #"<" "&lt;")
                    req (http/get aama
                            {:query-params
                             {"query" query-sparql ;;generated sparql
                              ;;"format" "application/sparql-results+json"}})]
                              "format" "text"}})]
             ;;(log/info "sparql result status: " (:status req))
             [:div
              [:hr]
              [:h4 "Valcluster: " valcluster]
              [:pre (:body req)]
              ;;[:h3#clickable "Query:"]
              ;;[:pre query-sparql-pr]
             ]))
           
           [:script {:src "js/goog/base.js" :type "text/javascript"}]
           [:script {:src "js/webapp.js" :type "text/javascript"}]
           [:script {:type "text/javascript"}
            "goog.require('webapp.core');"])))

(defroutes pdgmcbpll-routes
  (GET "/pdgmcbpll" [] (pdgmcbpll))
  (POST "/pdgmcbqry" [language pos] (handle-pdgmcbqry language pos))
  (POST "/pdgmsdisplay" [language valclusters pos] (handle-pdgmsdisplay language valclusters pos))
  )
