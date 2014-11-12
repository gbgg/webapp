(ns webapp.routes.pdgmcmp
 (:refer-clojure :exclude [filter concat group-by max min count replace])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [capitalize split replace]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.form :refer :all]))

(def aama "http://localhost:3030/aama/query")

(defn pdgmcmp []
  (let [langlist (slurp "pvlists/langlist.txt")
        languages (split langlist #"\n")]
  (layout/common 
   [:h3 "Paradigm Comparison"]
   [:p "Choose Type and Languages"]
   ;; [:p error]
   [:hr]
   (form-to [:post "/pdgmcmpqry"]
            [:table
             [:tr [:td "PDGM Type: "]
              [:td [:select#pos.required
                    {:title "Choose a pdgm type.", :name "pos"}
                    [:option {:value "fv" :label "Finite Verb"}]
                    [:option {:value "nfv" :label "Non-finite Verb"}]
                    [:option {:value "pro" :label "Pronoun"}]
                    [:option {:value "noun" :label "Noun"}]
                    ]]]
             [:tr [:td "First PDGM Language: " ]
              [:td [:select#language1.required
                    {:title "Choose a language.", :name "language1"}
                    (for [language languages]
                      (let [opts (split language #" ")]
                        [:option {:value (first opts)} (last opts) ]))]]]
             [:tr [:td "First PDGM Value Clusters: " ]
              [:td [:select#valstring1.required
                    {:title "Choose a value.", :name "valstring1"}
                    ;;(for [valcluster valclusters]
                    ;; [:option  valcluster])
                    ]]]
             [:tr [:td "Second PDGM Language: " ]
              [:td [:select#language2.required
                    {:title "Choose a language.", :name "language2"}
                    (for [language languages]
                      (let [opts (split language #" ")]
                        [:option {:value (first opts)} (last opts) ]))]]]
             [:tr [:td "Second PDGM Value Clusters: " ]
              [:td [:select#valstring2.required
                    {:title "Choose a value.", :name "valstring2"}
                    ;;(for [valcluster valclusters]
                    ;; [:option  valcluster])
                    ]]]
             ;;(submit-button "Get pdgm")
             [:tr [:td ]
              [:td [:input#submit
                    {:value "Get PDGM Value Clusters", :name "submit", :type "submit"}]]]]
            )
   [:hr])))

(defn display-valclusters
  [pos language1 language2]
   (let [valclusterfile1 (str "pvlists/pname-" pos "-list-" language1 ".txt")
        valclusterlist1 (slurp valclusterfile1)
        valclusters1 (split valclusterlist1 #"\n")
        valclusterfile2 (str "pvlists/pname-" pos "-list-" language2 ".txt")
        valclusterlist2 (slurp valclusterfile2)
        valclusters2 (split valclusterlist2 #"\n")]
    (layout/common 
      [:h3 "Paradigm Comparison"]
     [:p "Choose Value Clusters"]
     ;;[:p error]
     [:hr]
     (form-to [:post "/pdgmcmpdisplay"]
        [:table
         [:tr [:td "PDGM Type: "]
          [:td [:select#pos.required
                {:title "Choose a pdgm type.", :name "pos"}
                [:option pos]
                ]]]
         [:tr [:td "First PDGM Language: " ]
          [:td [:select#language1.required
               {:title "Choose a language.", :name "language1"}
                [:option language1]
                ]]]
         [:tr [:td "First PDGM Value Clusters: " ]
          [:td [:select#valstring1.required
                {:title "Choose a value.", :name "valstring1"}
                (for [valcluster valclusters1]
                  [:option  valcluster])
                ]]]
         [:tr [:td "Second PDGM Language: " ]
          [:td [:select#language2.required
               {:title "Choose a language.", :name "language2"}
                [:option language2]
                ]]]
         [:tr [:td "Second PDGM Value Clusters: " ]
          [:td [:select#valstring2.required
                {:title "Choose a value.", :name "valstring2"}
                (for [valcluster valclusters2]
                  [:option  valcluster])
                ]]]
         ;;(submit-button "Get pdgm")
         [:tr [:td ]
          [:td [:input#submit
                {:value "Display pdgm", :name "submit", :type "submit"}]]]]
     [:hr]))))

(defn handle-pdgmcmpqry
  [language1 language2 pos]
  (let [valclusterfile1 (str "pvlists/pname-" pos "-list-" language1 ".txt")
        valclusterfile2 (str "pvlists/pname-" pos "-list-" language2 ".txt")]
   (try
    (slurp valclusterfile1)
    (finally (println (str language1 " has paradigms of type " pos))))
   (try
    (slurp valclusterfile2)
    (finally (println (str language2 " has paradigms of type " pos))))
   (display-valclusters pos language1 language2)))

(defn handle-pdgmcmpdisplay
  [pos language1 valstring1 language2 valstring2]
  (let [pdgms (defrecord Pdgm [language valstring forms])]
  (layout/common
   [:body
    (for [langvstr [(str language1 "+" valstring1) (str language2 "+" valstring2)]
  ;; send SPARQL over HTTP request
       :let [lprefmap (read-string (slurp "pvlists/lprefs.clj"))
             lvstr (split langvstr #"\+")
             language (first lvstr)
             valstring (last lvstr)
             Language (capitalize language)
             lang (read-string (str ":" language))
             lpref (lang lprefmap)
             query-sparql (cond 
                      (= pos "pro")
                      (sparql/pdgmqry-sparql-pro language lpref valstring)
                      (= pos "nfv")
                      (sparql/pdgmqry-sparql-nfv language lpref valstring)
                      (= pos "noun")
                      (sparql/pdgmqry-sparql-noun language lpref valstring)
                      :else (sparql/pdgmqry-sparql-fv language lpref valstring))
             ;;query-sparql-pr (replace query-sparql #"<" "&lt;")
             req (http/get aama
                      {:query-params
                       {"query" query-sparql ;;generated sparql
                        "format" "tsv"}})
                        ;;"format" "application/sparql-results+json"}})
                        ;;"format" "text"}})
             req-pr (replace (:body req) #"<" "&lt;")
             pdgms (->Pdgm language valstring req-pr)
             ]]

      [:p Language ": " valstring
      [:pre req-pr "and then "(:forms pdgms)]])
    [:hr]
    [:pre "Here is the target: " (:forms pdgms)]
    [:script {:src "js/goog/base.js" :type "text/javascript"}]
    [:script {:src "js/webapp.js" :type "text/javascript"}]
    [:script {:type "text/javascript"}
            "goog.require('webapp.core');"]])))

(defn handle-pdgmcmpdisplay2
  [pos language1 valstring1 language2 valstring2]
    ;; send SPARQL over HTTP request
  (let [lprefmap (read-string (slurp "pvlists/lprefs.clj"))
        Language1 (capitalize language1)
        lang1 (read-string (str ":" language1))
        lpref1 (lang1 lprefmap)
        Language2 (capitalize language2)
        lang2 (read-string (str ":" language2))
        lpref2 (lang2 lprefmap)
        pnames (str language1":"valstring1"+"language2":"valstring2)
        query-sparql1 (cond 
                       (= pos "pro")
                       (sparql/pdgmqry-sparql-pro language1 lpref1 valstring1)
                       (= pos "nfv")
                       (sparql/pdgmqry-sparql-nfv language1 lpref1 valstring1)
                       (= pos "noun")
                       (sparql/pdgmqry-sparql-noun language1 lpref1 valstring1)
                       :else (sparql/pdgmqry-sparql-fv language1 lpref1 valstring1))
             ;;query-sparql-pr (replace query-sparql #"<" "&lt;")
        req1 (http/get aama
                       {:query-params
                        {"query" query-sparql1 ;;generated sparql
                         "format" "tsv"}})
                         ;;"format" "application/sparql-results+json"}})
                         ;;"format" "text"}})
        req-pr1 (replace (:body req1) #"<" "&lt;")
        req-pr3 ( -> (:body req1)
                     (replace #"[<>]" "\"")
                     (replace #"\n" "%%"))
        query-sparql2 (cond 
                       (= pos "pro")
                       (sparql/pdgmqry-sparql-pro language2 lpref2 valstring2)
                       (= pos "nfv")
                       (sparql/pdgmqry-sparql-nfv language2 lpref2 valstring2)
                       (= pos "noun")
                       (sparql/pdgmqry-sparql-noun language2 lpref2 valstring2)
                       :else (sparql/pdgmqry-sparql-fv language2 lpref2 valstring2))
             ;;query-sparql-pr (replace query-sparql #"<" "&lt;")
        req2 (http/get aama
                       {:query-params
                        {"query" query-sparql2 ;;generated sparql
                         "format" "tsv"}})
                         ;;"format" "application/sparql-results+json"}})
                         ;;"format" "text"}})
        req-pr2 (replace (:body req2) #"<" "&lt;")
        req-pr4 ( -> (:body req2)
                     (replace #"[<>]" "\"")
                     (replace #"\n" "%%"))
        pdgms-pr (str req-pr1  req-pr2)
        pdgms-pr2 (str req-pr3  req-pr4)
             ]
  (layout/common
   [:body
      [:h3 "PNames:"]
       [:pre pnames]
       ;;[:pre req-pr1]
       ;;[:pre req-pr2]
      [:h3 "Paradigms:"]
      [:p "(tsv format)"]
      [:pre pdgms-pr]
    [:hr]
      [:h3 "Parallel Display"]
     [:p "Choose PNG Values (comma-separated list)"]
     ;;[:p error]
     [:hr]
     (form-to [:post "/pdgmprlldisplay"]
        [:table
         [:tr [:td "PNames: "]
          [:td [:select#pnames.required
                {:title "Chosen PDGMS", :name "pnames"}
                [:option {:value pnames} "PNames"] 
                ]]]
         [:tr [:td "PDGMS: "]
          [:td [:select#pdgms-pr.required
                {:title "PDGMS", :name "pdgms-pr"}
                [:option {:value pdgms-pr2} "Paradigms"]
                ]]]
         [:tr [:td "Number: "]
          [:td [:input#num.required
                {:title "Choose Number Values.", :name "num"}
                ]]]
         [:tr [:td "Person: " ]
          [:td [:input#pers.required
               {:title "Choose Person Values.", :name "pers"}
                ]]]
         [:tr [:td "Gender: " ]
          [:td [:input#gen.required
                {:title "Choose Gender Values.", :name "gen"}
                ]]]
         ;;(submit-button "Get pdgm")
         [:tr [:td ]
          [:td [:input#submit
                {:value "Make Parallel Display", :name "submit", :type "submit"}]]]])
     [:hr]
    [:script {:src "js/goog/base.js" :type "text/javascript"}]
    [:script {:src "js/webapp.js" :type "text/javascript"}]
    [:script {:type "text/javascript"}
            "goog.require('webapp.core');"]])))

(defn handle-pdgmprlldisplay
  [pdgms-pr pnames num pers gen]
  ;; format parallel display from combined tsv + comma-separated num pers gen strings
  (let [numvals (split num #",")
        persvals (split pers #",")
        genvals (split gen #",")
         pngs (for [numval numvals] (for [persval persvals] (for [genval genvals]
          (conj [] (str numval "\t" persval "\t" genval)))))]
  (layout/common
   [:body
      [:h3 "Parallel Display"]
    [:p pnames]
    [:p (str num " + " pers " + " gen)]
    [:pre pdgms-pr]
     [:hr]
    [:p "Enumeration of pngs:"]
    (for [png pngs]
      [:p png])
    [:script {:src "js/goog/base.js" :type "text/javascript"}]
    [:script {:src "js/webapp.js" :type "text/javascript"}]
    [:script {:type "text/javascript"}
            "goog.require('webapp.core');"]])))


(defroutes pdgmcmp-routes
  (GET "/pdgmcmp" [] (pdgmcmp))
  (POST "/pdgmcmpqry" [language1 language2 pos] (handle-pdgmcmpqry language1 language2 pos))
  (POST "/pdgmcmpdisplay" [pos language1 valstring1 language2 valstring2] (handle-pdgmcmpdisplay2 pos language1 valstring1 language2 valstring2))
  (POST "/pdgmprlldisplay" [pdgms-pr pnames num pers gen] (handle-pdgmprlldisplay pdgms-pr pnames num pers gen))
  )
