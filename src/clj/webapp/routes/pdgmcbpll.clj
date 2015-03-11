(ns webapp.routes.pdgmcbpll
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

(defn pdgmcbpll []
  (let [langlist (slurp "pvlists/langlist.txt")
        languages (split langlist #"\n")]
  (layout/common 
   [:h3 "Paradigm Checkbox"]
     [:p "Use this option to pick a number of paradigms from a given language to be displayed in vertical succession."]
   [:p "Choose Language and Type"]
   ;; [:p error]
   [:hr]
   (form-to [:post "/pdgmcbpllqry"]
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
        valclusters (clojure.string/split valclusterlist #"\r\n")]
    (layout/common 
     [:h3 "Paradigms"]
     [:p "Choose Value Clusters For: " language "/" pos]
     ;;[:p error]
     [:hr]
     (form-to [:post "/pdgmscmpdisplay"]
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
     [:hr]))))

(defn handle-pdgmcbpllqry
  [language pos]
  (let [valclusterfile (str "pvlists/pname-" pos "-list-" language ".txt")]
   (try
    (slurp valclusterfile)
    (finally (println (str language " has no paradigms of type " pos))))
   (display-valcluster-checkbox language pos)))

(defn vc2req
 [language valclusters pos]
  (let [Language (capitalize language)
        lprefmap (read-string (slurp "pvlists/lprefs.clj"))
        lang (read-string (str ":" language))
        lpref (lang lprefmap)
        vcvec (split valclusters #" ")
        ]
    (for [valcluster vcvec]
      (let [
            valstrng (clojure.string/replace valcluster #",*person|,*gender|,*number" "")
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

(defn handle-pdgmscmpdisplay
  [language valclusters pos]
  ;; send SPARQL over HTTP request
  (let [Language (capitalize language)
        lprefmap (read-string (slurp "pvlists/lprefs.clj"))
        lang (read-string (str ":" language))
        lpref (lang lprefmap)
        headerset (str "number " "person " "gender ")
        headers (split headerset #" ")
        pdgmvec (map #(vc2req language % pos) valclusters)
        pdgmstr1 (apply pr-str pdgmvec)
        pdgmstr2 (clojure.string/replace pdgmstr1 #"[\(\)\"]" "")
        ]
         (layout/common
           [:h3#clickable "Paradigm " Language " -  " pos ": "  ]
           [:p "(CSV Format)"]
           (for [pdgmreq pdgmvec]
                   ;; (log/info "sparql result status: " (:status req))
                    [:div
                     [:hr]
                     [:pre pdgmreq]
                     ]
                     )
           [:hr]
           [:p "pdgmvec: " [:pre pdgmvec]]
           [:h3 "Parallel Display"]
           [:p "Choose PNG Values (comma-separated list)"]
           [:hr]
           (form-to [:post "/pdgmsplldisplay"]
                    [:table
                     [:tr [:td "PNames: "]
                      [:td [:select#names.required
                            {:title "Chosen PDGMS", :name "pdgmnames"}
                            [:option {:value (str valclusters)} (str valclusters)] 
                            ]]]
                     [:tr [:td "Header: "]
                      [:td [:select#header.required
                            {:title "Header", :name "header"}
                            [:option {:value headerset} headerset] 
                            ]]]
                     [:tr [:td "PString: "]
                      [:td [:select#pdgms.required
                            {:title "PDGMS", :name "pdgmstr2"}
                            [:option {:value pdgmstr2} pdgmstr2]
                            ;;[:option {:value valclusters} (str valclusters)]
                            ]]]
                     [:tr [:td "PNG Type: "]
                      [:td [:select#pos.required
                            {:title "Choose a png type.", :name "pngtype"}
                            [:option {:value "pngbeja" :label "Beja"}]
                            [:option {:value "pngcush" :label "Cushitic"}]
                            [:option {:value "pngaa" :label "Afroasiatic"}]
                            [:option {:value "pnggen" :label "General"}]
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
      "goog.require('webapp.core');"])))

(defn pstring2map
  [pdgm pngset]
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

(defn handle-pdgmsplldisplay
  [pdgms header pdgmstr2 pngtype]
  (let [pngfile (str "pvlists/" pngtype ".clj")
        pngstring (slurp pngfile)
        pngs (split pngstring #" ")
        pngset (atom #{})
        pnamestr (clojure.string/replace pdgms #"[\[\]\"]" "")
        pnames (split pnamestr #" ")
        pstrings (split pdgmstr2 #" ")
        pmaps (for [pdgm pstrings] (pstring2map pdgm pngset))
        
        ]
    (layout/common
     [:body
      [:h3 "Parallel Display of Paradigms:" ]
      ;;[:p "pdgmstr2: " [:pre pdgmstr2]]
      ;;[:p "pheader: " [:pre pheader]]
      ;;[:p "pstrings: " [:pre pstrings]]
      ;;[:p "pdgms:"
      ;;[:ol
       ;;(for [pdgm pstrings]
         ;;[:li pdgm])]]
      ;;[:p "pmaps: " 
       ;;[:ol 
        ;;(for [pmap pmaps]
          ;;(let [pngkeys (keys map)]
            ;;(for [pngkey pngkeys]
              ;;(swap! pngset pngkey))
            ;;[:li @pngset]
            ;;[:li (keys map)]
          ;;[:li pmap]
          ;;)
        ]
      ;;[:p "pdgmnames: " [:pre pdgms]]
      [:p "Paradigms:"
      [:ol
      (for [pname pnames]
        [:li pname])]]
      [:hr]
      [:table
       (let [heads (split header #" ")
             pdgmnums (into [] (take (clojure.core/count pnames) (iterate inc 1)))]
         [:span
          (for [head heads]
           [:th  head])
       (for [pdgmnum pdgmnums]
         [:th (str "P-" pdgmnum)])])
      (for [png pngs]
        ;;(if (contains? pngset png)
        [:tr
         (let [npgs (split png #",")
               pngk (keyword png)]
           [:div
           (for [npg npgs]
             [:td npg])
             (for [pmap pmaps]
               [:td (pngk pmap)])
           ])]
        ;;)
        )]
      [:script {:src "js/goog/base.js" :type "text/javascript"}]
      [:script {:src "js/webapp.js" :type "text/javascript"}]
      [:script {:type "text/javascript"}
       "goog.require('webapp.core');"])))
    

(defroutes pdgmcbpll-routes
  (GET "/pdgmcbpll" [] (pdgmcbpll))
  (POST "/pdgmcbpllqry" [language pos] (handle-pdgmcbpllqry language pos))
  (POST "/pdgmscmpdisplay" [language valclusters pos] (handle-pdgmscmpdisplay language valclusters pos))
  (POST "/pdgmsplldisplay" [pdgmnames header pdgmstr2 pngtype] (handle-pdgmsplldisplay pdgmnames header pdgmstr2 pngtype)))
