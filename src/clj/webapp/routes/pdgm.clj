(ns webapp.routes.pdgm
 (:refer-clojure :exclude [filter concat group-by max min replace])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [capitalize lower-case replace split]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.form :refer :all]))

(def aama "http://localhost:3030/aama/query")

(defn pdgm []
  (let [langlist (slurp "pvlists/menu-langs.txt")
        languages (split langlist #"\n")]
  (layout/common 
   [:h3 "Individual Paradigms"]
   ;;[:h4 "Choose Language and Type"]
   ;;[:p  "This query-type prompts for a \"paradigm-type\" (Finite Verb, Non-finite Verb, Pronoun, Noun) and a language; it then shows a drop-down select list of paradigms in that language of that type, and returns a table-formatted display of the selected paradigm."]
   ;; [:p error]
   ;; [:hr]
   (form-to [:post "/pdgmqry"]
            [:table
             [:tr [:td "PDGM Type: "]
              [:td [:select#pos.required
                    {:title "Choose a pdgm type.", :name "pos"}
                    [:option {:value "fv" :label "Finite Verb"}]
                    [:option {:value "nfv" :label "Non-finite Verb"}]
                    [:option {:value "pro" :label "Pronoun"}]
                    [:option {:value "noun" :label "Noun"}]
                    ]]]
             [:tr [:td "PDGM Language: " ]
              [:td [:select#language.required
                    {:title "Choose a language.", :name "language"}
                    (for [language languages]
                        [:option {:value (lower-case language)} language])]]]
             ;;(submit-button "Get pdgm")
             [:tr [:td ]
              [:td [:input#submit
                    {:value "Value Clusters: ", :name "submit", :type "submit"}]]]]
            )
   [:hr])))

(defn handle-pdgmqry
  [language pos]
  (let [langlist (slurp "pvlists/menu-langs.txt")
        languages (split langlist #"\n")
        valclusterfile (str "pvlists/plexname-" pos "-list-" language ".txt")
        valclusterlist (slurp valclusterfile)
        valclusters (clojure.string/split valclusterlist #"\n")]
    (layout/common 
     ;;[:h3 "Paradigms"]
     ;;[:p "Choose Value Clusters"]
     (form-to [:post "/pdgmqry"]
              [:table
               [:tr [:td "PDGM Type: "]
                [:td [:select#pos.required
                      {:title "Choose a pdgm type.", :name "pos"}
                      [:option {:value "fv" :label "Finite Verb"}]
                      [:option {:value "nfv" :label "Non-finite Verb"}]
                      [:option {:value "pro" :label "Pronoun"}]
                      [:option {:value "noun" :label "Noun"}]
                      ]]]
               [:tr [:td "PDGM Language: " ]
                [:td [:select#language.required
                      {:title "Choose a language.", :name "language"}
                      (for [language languages]
                        [:option {:value (lower-case language)} language])]]]
               ;;(submit-button "Get pdgm")
               [:tr [:td ]
                [:td [:input#submit
                      {:value "Value Clusters: ", :name "submit", :type "submit"}]]]]
            )
     [:hr]
     (form-to [:post "/pdgmdisplay"]
        [:table
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

(defn handle-pdgmdisplay
  [language valstring pos]
  ;; send SPARQL over HTTP request
  (let [Language (capitalize language)
        lprefmap (read-string (slurp "pvlists/lprefs.clj"))
        lang (read-string (str ":" language))
        lpref (lang lprefmap)
        valstrng (clojure.string/replace valstring #",*person|,*gender|,*number" "")
        valstr (clojure.string/replace valstrng #":," ":")
        ;;5/29/15 REFORMULATE BY SPLITTING VALSTR INTO VALS AND LEX 
        ;; AND ITERATING THROUGH LEXVALS
        ;; In single pdgm query, always asking for note (9/29/15)
        query-sparql (cond 
                      (= pos "pro")
                      (sparql/pdgmqry-sparql-pro-note language lpref valstr)
                      (= pos "nfv")
                      (sparql/pdgmqry-sparql-nfv-note language lpref valstring)
                      (= pos "noun")
                      (sparql/pdgmqry-sparql-noun-note language lpref valstring)
                      :else (sparql/pdgmqry-sparql-fv-note language lpref valstring))
        query-sparql-pr (replace query-sparql #"<" "&lt;")
        req (http/get aama
                      {:query-params
                       {"query" query-sparql ;;generated sparql
                        ;;"format" "application/sparql-results+json"}})]
                        "format" "csv"}})
        ;; I have no idea why the following works; why it is necessary
        ;; to replace \r\n by something else (here &&) in order to
        ;; split (:body req).
        pdgmstr (clojure.string/replace (:body req) #"\r\n" "&&")
        psplit (split pdgmstr #"&&")
        header (first psplit)
        pdgmrows (rest psplit)
        prow (first pdgmrows)
        pvals (split prow #",")
        note (first pvals)
        pheader (split header #",")
        pnote (first pheader)
        pheads (rest pheader)
        ;;notes (atom #{})
        ]
         (log/info "sparql result status: " (:status req))
         (layout/common
          [:body
           [:h3#clickable "Paradigm: " Language " / " valstring]
           [:table {:id "handlerTable" :class "tablesorter sar-table"}
           ;;[:table
            [:thead
             [:tr
              (for [head pheads]
                [:th [:div {:class "some-handle"}] head])]]
            ;;[:th head])]]
            [:tbody 
             (for [pdgmrow pdgmrows]
               [:tr
                (let [rowcells (split pdgmrow #",")
                      note (first rowcells)
                      pcells (rest rowcells)
                      ]
                  [:div
                  (for [pcell pcells]
                    [:td pcell])])])]]
           ;; following does not work. Want to assemble all comments in
           ;; atom notes. pnote has that in last iteration, but I seem
           ;; to need to print out each iteration to get that. Following attempt
           ;; to print out only on last row raises "java.lang.Long cannot be 
           ;; cast to java.util.concurrent.Future" error.
           ;;[:div
           ;;(let [;;nrows (count pheads)
           ;;      ;;rownum (atom 0)
           ;;      notes (atom #{})]
           ;;  (for [pdgmrow pdgmrows]
           ;;    (let [rowcells (split pdgmrow #",")
           ;;          note (first rowcells)
           ;;          ;;nrow (swap! rownum inc)
           ;;          pnote (swap! notes conj note)]
           ;;      ;;(if (= nrow nrows)
           ;;      [:p "NOTE: " @pnote]
           ;;        ;;)
           ;;      )))]
           (if (re-find #"\w"  note)
             [:p "NOTE: " note  ])
           [:hr]
           [:h3 "Query Response:"]
           [:pre (:body req)]
           ;;[:pre pdgmstr]
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
