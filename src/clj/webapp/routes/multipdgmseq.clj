(ns webapp.routes.multipdgmseq
 (:refer-clojure :exclude [filter concat group-by max min count])
  (:require 
            ;;[clojure.core/count :as count]
            [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [capitalize split upper-case]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.element :refer [link-to]]
            [hiccup.form :refer :all]))


(def aama "http://localhost:3030/aama/query")

(defn multipdgmseq []
  (let [langlist (slurp "pvlists/langlist.txt")
        languages (split langlist #"\n")]
  (layout/common 
   [:h3 "Multi-Paradigm: Fixed Sequential Display"]
     [:p "Use this option to pick one or more  paradigms from a given language or set of languages to be displayed in fixed format vertical succession."]
   [:p "Choose Languages and Type"]
   ;; [:p error]
   [:hr]
   (form-to [:post "/multiseqqry"]
            [:table
             [:tr [:td "PDGM Type: "]
              [:td [:select#pos.required
                    {:title "Choose a pdgm type.", :name "pos"}
                    [:option {:value "fv" :label "Finite Verb"}]
                    [:option {:value "nfv" :label "Non-finite Verb"}]
                    [:option {:value "pro" :label "Pronoun"}]
                    [:option {:value "noun" :label "Noun"}]
                    ]]]
             [:tr [:td "PDGM Language(s): " ]
             [:td 
               {:title "Choose one or more languages.", :name "language"}
               (for [language languages]
                 (let [choices (split language #" ")
                       lang (first choices)
                       Lang (last choices)]
                 [:div {:class "form-group"}
                  [:label 
                   (check-box {:name "languages[]" :value lang} lang) (str Lang)]
                 ;; from https://groups.google.com/forum/#!topic/compojure/5Vm8QCQLsaQ
                 ;; (check-box "valclusters[]" false valcluster) (str valcluster)]]
                 ]))]]
             ;;(submit-button "Get pdgm")
             [:tr [:td ]
              [:td [:input#submit
                    {:value "Get PDGM Value Clusters", :name "submit", :type "submit"}]]]]
            )
   [:hr])))

(defn handle-multiseqqry
  [languages pos]
  (layout/common 
       [:h3 "Multi-Paradigm Fixed Sequential Display"]
       [:p "Choose Value Clusters For: "]
       ;;[:p error]
       [:hr]
   (form-to [:post "/multiseqdisplay"]
            [:table
             [:tr [:td "PDGM Type: " ]
              [:td
               (check-box {:name "pos" :value pos :checked "true"} pos) (str (upper-case pos))]]
              [:tr [:td [:hr]]]
                 [:tr [:td "PDGM Language(s): " ]
                  [:td 
                   (for [language languages]
                     [:div (str (capitalize language) " ")])]]
                 [:tr [:td "PDGM Value Clusters: " ]
                  [:td 
                   {:title "Choose a value.", :name "valcluster"}
                   (for [language languages]
                     (let [valclusterfile (str "pvlists/pname-" pos "-list-" language ".txt")
                           valclusterlist (slurp valclusterfile)
                           valclusters (split valclusterlist #"\n")]
                       (for [valcluster valclusters]
                         [:div {:class "form-group"}
                          [:label
                           (check-box {:name "valclusters[]" :value (str language "," valcluster) } valcluster) (str (capitalize language) ": " valcluster)]])))]]
                 ;;(submit-button "Get pdgm")
                 [:tr [:td ]
                  [:td [:input#submit
                        {:value "Display pdgms", :name "submit", :type "submit"}]]]])))

(defn handle-multiseqdisplay
 [valclusters pos]
 (layout/common
  (let 
      ;;[vcvec (split valclusters #" ")
      [lprefmap (read-string (slurp "pvlists/lprefs.clj"))]
    (for [valcluster valclusters]
      (let [vals (split valcluster #"," 2)
            language (first vals)
            vcluster (last vals)
            lang (read-string (str ":" language))
            lpref (lang lprefmap)
            valstrng (clojure.string/replace vcluster #",*person|,*gender|,*number" "")
            valstr (clojure.string/replace valstrng #":," ":")
            query-sparql (cond 
                          (= pos "pro")
                          (sparql/pdgmqry-sparql-pro language lpref valstr)
                          (= pos "nfv")
                          (sparql/pdgmqry-sparql-nfv language lpref valcluster)
                          (= pos "noun")
                          (sparql/pdgmqry-sparql-noun language lpref valcluster)
                          :else (sparql/pdgmqry-sparql-fv language lpref vcluster))
            req (http/get aama
                      {:query-params
                       {"query" query-sparql ;;generated sparql
                        ;;"format" "application/sparql-results+json"}})]
                        "format" "text"}})
            ]
        [:div
         [:hr]
         [:h4 "Valcluster: " valcluster]
         [:pre (:body req)]])))
        [:script {:src "js/goog/base.js" :type "text/javascript"}]
        [:script {:src "js/webapp.js" :type "text/javascript"}]
        [:script {:type "text/javascript"}
         "goog.require('webapp.core');"]))

(defroutes multipdgmseq-routes
  (GET "/multipdgmseq" [] (multipdgmseq))
  (POST "/multiseqqry" [languages pos] (handle-multiseqqry languages pos))
  (POST "/multiseqdisplay" [valclusters pos] (handle-multiseqdisplay valclusters pos)))
