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
                        "format" "csv"}})
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
        query-sparql1 (cond 
                       (= pos "pro")
                       (sparql/pdgmqry-sparql-pro language1 lpref1 valstring1)
                       (= pos "nfv")
                       (sparql/pdgmqry-sparql-nfv language1 lpref1 valstring1)
                       (= pos "noun")
                       (sparql/pdgmqry-sparql-noun language1 lpref1 valstring1)
                       :else (sparql/pdgmqry-sparql-fv language1 lpref1 valstring1))
        req1 (http/get aama
                       {:query-params
                        {"query" query-sparql1 ;;generated sparql
                         "format" "csv"}})
                         ;;"format" "application/sparql-results+json"}})
                         ;;"format" "text"}})
        pdgm1 (replace (:body req1) #"\r\n" "%%")
        query-sparql2 (cond 
                       (= pos "pro")
                       (sparql/pdgmqry-sparql-pro language2 lpref2 valstring2)
                       (= pos "nfv")
                       (sparql/pdgmqry-sparql-nfv language2 lpref2 valstring2)
                       (= pos "noun")
                       (sparql/pdgmqry-sparql-noun language2 lpref2 valstring2)
                       :else (sparql/pdgmqry-sparql-fv language2 lpref2 valstring2))
        req2 (http/get aama
                       {:query-params
                        {"query" query-sparql2 ;;generated sparql
                         "format" "csv"}})
                         ;;"format" "application/sparql-results+json"}})
                         ;;"format" "text"}})
        pdgm2 (replace (:body req2) #"\r\n" "%%")
        pdgm3 (replace (:body req2) #"," "\t\t")
        pdgmnames (str language1":"valstring1"+"language2":"valstring2)
        pdgmstr1 (str pdgm1 pdgm2)
        pdgmstr2 (replace pdgmstr1 #"," "_")
             ]
  (layout/common
   [:body
      [:h3 "PNames:"]
    [:ol
     [:li (str language1":"valstring1)]
     [:li (str language2":"valstring2)]]
      [:h3 "Paradigms:"]
      [:p "(csv format)"]
      [:pre (:body req1)]
      [:pre (:body req2)]
    [:hr]
      [:h3 "Parallel Display"]
     [:p "Choose PNG Values (comma-separated list)"]
     ;;[:p error]
     [:hr]
     (form-to [:post "/pdgmprlldisplay"]
        [:table
         [:tr [:td "PNames: "]
          [:td [:select#pdgmnames.required
                {:title "Chosen PDGMS", :name "pdgmnames"}
                [:option {:value pdgmnames} "PdgmNames"] 
                ]]]
         [:tr [:td "PDGMS: "]
          [:td [:select#pdgmstr.required
                {:title "PDGMS", :name "pdgmstr"}
                ;;[:option {:value pdgmstr2} "Paradigms"]
                [:option {:value pdgmstr2} pdgmstr2]
                ]]]
         [:tr [:td "Number: "]
          [:td [:input#num.required
                {:title "Choose Number Values.", :name "nmbr"}
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

(defn pstring2map
  [pdgm]
  (let [pdgm1  (replace pdgm #"(.*?_.*?_.*?)_(.*?%%)" "$1 $2")
        pdgm2 (replace pdgm1 #"%%" " ")
        plist (split pdgm2 #" ")
        pmap (apply hash-map plist)
        ]
    (clojure.walk/keywordize-keys pmap)))

;; The following is an attempt (unsuccessful so far) to directly translate
;; into clojure the perl PrintCompPdgm subroutine from 
;; aama-data/pl/pdgm-fv-bil-tsv2table2.pl
(defn handle-pdgmprlldisplay2
  [pdgmstr pdgmnames]
  (let [
        pngstring (slurp "pvlists/npg.clj")
        pngs (split pngstring #" ")
        pnames (split pdgmnames #"\+")
        pdgms-sp (split pdgmstr #"%%" 2)
        header (first pdgms-sp)
        pbody (last pdgms-sp)
        pdgms (split pbody (re-pattern (str header "%%")))
        pmaps (for [pdgm pdgms] (pstring2map pdgm))
        ]
    (layout/common
     [:body
      [:h3 "Parallel Display" ]
      (for [pname pnames]
        [:pre pname])
      [:p "Header: "
      [:pre header]]
      [:hr]
      [:p "Body: "
      [:pre pbody]]
      [:hr]  
      [:p "PMaps: "]
      (for [pmap pmaps]
        [:div
         [:p pmap "PMap"]
         ;; following prints map in terminal window (lein repl server) or
         ;; *nrepl server webapp* buffer (emacs)
         (println pmap )
         [:hr]])
;;      [:p "Enumeration of pngs and pmaps:"]
;;      (for [png pngs]
;;        [:pre png])
;;      [:hr]
      (def npgprint #{})
      [:table
      (for [png pngs]
        [:tr
         (let [pngk (keyword png)]
           (def pngprint false)
          (def pnum 0)
          (for [pmap pmaps] 
            [:div
;;             (def pngprint false)
           (println pngprint)
;;            (let [pnum (inc pnum)]
            (if (pngk pmap)
               (if (false? pngprint)
  ;;               [:div
                (let [npgs (split png #"_")]
                  [:div
                  (for [npg npgs]
                    [:td npg])
;;                   (def pngprint true)
                  ;;needs to be 'while' for pnum > 2
;;                  (if (> pnum 1)
;;                    [:div
;;                    [:td ]
;;                   [:td  (pngk pmap)]])
                [:td (str "a: " (pngk pmap))]
                   (def pngprint true)
                ]
                  )
;;                  ]
               [:td (str "b: " (pngk pmap))]))
;;            )
             ]))])]
;;              (if (true? pgnprint)
                ;;[:td ])]))])))
      [:script {:src "js/goog/base.js" :type "text/javascript"}]
      [:script {:src "js/webapp.js" :type "text/javascript"}]
      [:script {:type "text/javascript"}
       "goog.require('webapp.core');"]])))
    
;; The following is a work-around, limiting the number of pdgms to 2;
;; It will be necessary to generalize this (perhaps along the lines 
;; initiated in handle-pdgmprlldisplay2) to n pdgms.
;; To generate cartesian product of npgs (instead of reading from cache)
;; cf. https://clojuredocs.org/clojure.core/doseq
(defn handle-pdgmprlldisplay3
  [pdgmstr pdgmnames]
  (let [
        pngstring (slurp "pvlists/npg.clj")
        pngs (split pngstring #" ")
        pnames (split pdgmnames #"\+")
        pdgms-sp (split pdgmstr #"%%" 2)
        header (first pdgms-sp)
        pbody (last pdgms-sp)
        pdgms (split pbody (re-pattern (str header "%%")))
        pmaps (for [pdgm pdgms] (pstring2map pdgm))
        pmap1 (first pmaps)
        pmap2 (last pmaps)
        ]
    (layout/common
     [:body
      [:h3 "Parallel Display of Paradigms:" ]
      [:ol
      (for [pname pnames]
        [:li pname])]
      [:hr]
      [:table
       (let [heads (split header #"_")]
         (for [head heads]
           [:th head]))
      (for [png pngs]
        [:tr
         (let [pngk (keyword png)]
            (if (pngk pmap1)
                  [:div
                   (let [npgs (split png #"_")]
                     (for [npg npgs]
                       [:td npg]))
                   [:td (pngk pmap1)]
                   [:td (pngk pmap2)]]
              (if (pngk pmap2)
                  [:div
                   (let [npgs (split png #"_")]
                     (for [npg npgs]
                       [:td npg]))
                   [:td (pngk pmap1)]
                   [:td (pngk pmap2)]]
                  )))])]
      [:script {:src "js/goog/base.js" :type "text/javascript"}]
      [:script {:src "js/webapp.js" :type "text/javascript"}]
      [:script {:type "text/javascript"}
       "goog.require('webapp.core');"]])))
    
(defroutes pdgmcmp-routes
  (GET "/pdgmcmp" [] (pdgmcmp))
  (POST "/pdgmcmpqry" [language1 language2 pos] (handle-pdgmcmpqry language1 language2 pos))
  (POST "/pdgmcmpdisplay" [pos language1 valstring1 language2 valstring2] (handle-pdgmcmpdisplay2 pos language1 valstring1 language2 valstring2))
  (POST "/pdgmprlldisplay" [pdgmstr pdgmnames] (handle-pdgmprlldisplay3 pdgmstr pdgmnames))
  )
