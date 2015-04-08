(ns webapp.routes.langcheckbx
 (:refer-clojure :exclude [filter concat group-by max min count])
  (:require 
            ;;[clojure.core/count :as count]
            [compojure.core :refer :all]
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

(defn langcheckbx []
  (let [langlist (slurp "pvlists/langlist.txt")
        languages (split langlist #"\n")]
  (layout/common 
   [:h3 "Paradigm Checkbox"]
     [:p "Use this option to pick one or more  paradigms from a given language or set of languages to be displayed initially in vertical succession."]
   [:p "Choose Languages and Type"]
   ;; [:p error]
   [:hr]
   (form-to [:post "/langcbpllqry"]
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

(defn handle-langcbpllqry
  [languages pos]
  (layout/common 
   (for [language languages]
     (let [valclusterfile (str "pvlists/pname-" pos "-list-" language ".txt")
           valclusterlist (slurp valclusterfile)
           valclusters (clojure.string/split valclusterlist #"\r\n")]
       ;;[:h3 "Paradigms"]
       [:p "Choose Value Clusters For: " language "/" pos]
       ;;[:p error]
       [:hr]
       (form-to [:post "/langcbcmpdisplay"]
                [:table
                 [:tr [:td "PDGM Language: " ]
                  ;; change language & pos selects to checked checkbox
                  [:td
                   (check-box {:name "language" :value language :checked "true"} language) (str language)]]
                 [:tr [:td "PDGM Type: " ]
                  [:td
                   (check-box {:name "pos" :value pos :checked "true"} pos) (str pos)]]
                 [:tr [:td "PDGM Value Clusters: " ]
                  [:td 
                   {:title "Choose a value.", :name "valcluster"}
                   (for [valcluster valclusters]
                     [:div {:class "form-group"}
                      [:label 
                       (check-box {:name "valclusters[]" :value valcluster} valcluster) (str valcluster)]
                      ]
                     ;; from https://groups.google.com/forum/#!topic/compojure/5Vm8QCQLsaQ
                     ;; (check-box "valclusters[]" false valcluster) (str valcluster)]]
                     )]]
                 ;;(submit-button "Get pdgm")
                 [:tr [:td ]
                  [:td [:input#submit
                        {:value "Display pdgms", :name "submit", :type "submit"}]]]]
                [:hr])))))

(defn vc2req
 [valclusters pos]
  (let [vcvec (split valclusters #" ")
        lprefmap (read-string (slurp "pvlists/lprefs.clj"))]
    (for [valcluster vcvec]
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
                          :else (sparql/pdgmqry-sparql-fv language lpref valcluster))
            req (http/get aama
                      {:query-params
                       {"query" query-sparql ;;generated sparql
                        ;;"format" "application/sparql-results+json"}})]
                        "format" "csv"}})
            ]
        ;;(str (:body req))))
        (clojure.string/replace (:body req) #" " ""))
    )))

(defn csv2pdgm
"Takes sorted 4-col csv list with vectors of pnames and headers, and outputs 5-col html table with first col for pname ref."
 [pdgmstr2 valclusters headers]
(let  [pdgms (str valclusters)
       pnamestr (clojure.string/replace pdgms #"[\[\]\"]" "")
       pnames (split pnamestr #" ")
       pstrings (split pdgmstr2 #" ")
       pdgmnum (atom 0)
       ]
  [:div
      [:p "Paradigms:"
      [:ol
      (for [pname pnames]
        [:li pname])]]
      [:hr]
      ;; For visible borders set {:border "1"}.
      [:table {:border "0"}
       [:tr
       (for [header headers]
         [:th header])]
       (for [pdgm pstrings]
         (let [pdgm-sp (split pdgm #"\\r\\n" 2) 
              pbody (last pdgm-sp)
              pdgmrows (split pbody #"\\r\\n")
               pnum (swap! pdgmnum inc)
               ]
           (for [pdgmrow pdgmrows]
             [:tr
              [:td (str "P-" pnum)]
             (let [pdgmcells (split pdgmrow #",")]
               (for [pdgmcell pdgmcells]
                 [:td pdgmcell]))])))]]))

(defn handle-langcbcmpdisplay
  [language valclusters pos]
  ;; send SPARQL over HTTP request
  (let [headerset1 (str "Paradigm " "Number " "Person " "Gender " "Token ")
        headerset2 (str "Number " "Person " "Gender " "Token ")
        headers (split headerset1 #" ")
        pdgmvec (map #(vc2req  % pos) valclusters)
        pdgmstr1 (apply pr-str pdgmvec)
        pdgmstr2 (clojure.string/replace pdgmstr1 #"[\(\)\"]" "")
        pdgmtable (csv2pdgm pdgmstr2 valclusters headers)
        ]
         (layout/common
           [:h3#clickable "Paradigms " pos ": "  ]
           ;;[:p "(CSV Format)"]
           ;;(for [pdgmreq pdgmvec]
                   ;; (log/info "sparql result status: " (:status req))
             ;;       [:div
               ;;      [:hr]
                 ;;    [:pre pdgmreq]
                   ;;  ]
                     ;;)
           ;;[:hr]
           ;;[:p "pdgmvec: " [:pre pdgmvec]]
           [:hr]
           pdgmtable
           [:hr]
           [:h3 "Parallel Display"]
           [:p "Choose PNG Values (comma-separated list)"]
           [:hr]
           (form-to [:post "/langcbplldisplay"]
                    [:table
                     [:tr [:td "PNames: "]
                      [:td [:select#names.required
                            {:title "Chosen PDGMS", :name "pdgmnames"}
                            [:option {:value (str valclusters)} (str valclusters)] 
                            ]]]
                     [:tr [:td "Header: "]
                      [:td [:select#header.required
                            {:title "Header", :name "header"}
                            [:option {:value headerset2} headerset2] 
                            ]]]
                     [:tr [:td "PString: "]
                      [:td [:select#pdgms.required
                            {:title "PDGMS", :name "pdgmstr2"}
                            [:option {:value pdgmstr2} pdgmstr2]
                            ;;[:option {:value valclusters} (str valclusters)]
                            ]]]
                     ;; current algorithm will simply take png enumerations
                     ;; already listed in pvlists/npg.clj
                     ;; [MAKE SURE EVERYTHING RELEVANT IS THERE!]
                     ;; second step will be to combine actual png val configs
                     ;; into png vector made on the fly
                     ;; third step is to allow choice of png vals as per
                     ;; text input fields below (for now can be left blank)
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
      "goog.require('webapp.core');"])))

(defn pstring2map
  [pdgm]
  (let [pdgm1 (clojure.string/replace pdgm #"^.*?\\r\\n" "") ;; header out
        pdgmstring (clojure.string/replace pdgm1 #"\\r\\n" "%%") 
        pdgmstr (clojure.string/replace pdgmstring #",([^,]*%%)" "&$1")
        pdgm2 (clojure.string/replace pdgmstr #"&" " ")
        pdgm3  (clojure.string/replace pdgm2 #"%%" " ")
        plist (split pdgm3 #" ")
        pmap (apply hash-map plist)
        ]
    (clojure.walk/keywordize-keys pmap)
    ))

(defn handle-langcbplldisplay
  [pdgms headerset2 pdgmstr2]
  (let [pngstring (slurp "pvlists/npg.clj")
        pngs (split pngstring #" ")
        pnamestr (clojure.string/replace pdgms #"[\[\]\"]" "")
        pnames (split pnamestr #" ")
        pstrings (split pdgmstr2 #" ")
        pmaps (for [pdgm pstrings] (pstring2map pdgm))
        headerset (str "Number " "Person " "Gender ")
        heads (split headerset #" ")
        pdgmnums (into [] (take (clojure.core/count pnames) (iterate inc 1)))
        ;; There has to be an easier way to get to keyset!
        keylists (set (for [pmap pmaps] (keys pmap)))
        keystring (clojure.string/replace (str keylists) #"[#(){}]" "")
        keyvec (split keystring #" ")
        keyset (set keyvec)
        ]
    (layout/common
     [:body
      [:h3 "Parallel Display of Paradigms:" ]
      ;;[:p "keylists: " [:pre keylists]]
      ;;[:p "keystring: " [:pre keystring]]
      ;;[:p "keyset: " [:pre keyset]]
     [:p "Paradigms:"
      [:ol
      (for [pname pnames]
        [:li pname])]]
      [:hr]
      [:table
         [:span
          (for [head heads]
            [:th  head])
          (for [pdgmnum pdgmnums]
            [:th (str "P-" pdgmnum)])]
       (for [png pngs]
         (if (contains? keyset (str (keyword png)))
        [:tr
         (let [npgs (split png #",")
               pngk (keyword png)]
           [:div
           (for [npg npgs]
             [:td npg])
             (for [pmap pmaps]
               [:td (pngk pmap)])])]))]
      [:script {:src "js/goog/base.js" :type "text/javascript"}]
      [:script {:src "js/webapp.js" :type "text/javascript"}]
      [:script {:type "text/javascript"}
       "goog.require('webapp.core');"]])))
    

(defroutes langcheckbx-routes
  (GET "/langcheckbx" [] (langcheckbx))
  (POST "/langcbpllqry" [languages pos] (handle-langcbpllqry languages pos))
  (POST "/langcbcmpdisplay" [language valclusters pos] (handle-langcbcmpdisplay language valclusters pos))
  (POST "/langcbplldisplay" [pdgmnames header pdgmstr2 pngtype] (handle-langcbplldisplay pdgmnames header pdgmstr2 pngtype)))
