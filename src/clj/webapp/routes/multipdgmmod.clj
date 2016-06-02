(ns webapp.routes.multipdgmmod
 (:refer-clojure :exclude [filter concat group-by max min])
  (:require 
            ;;[clojure.core/count :as count]
            [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            ;;[clojure.string :as str]
            [clojure.string :refer [capitalize lower-case split join upper-case]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.element :refer [link-to]]
            [hiccup.form :refer :all]))


(def aama "http://localhost:3030/aama/query")

(defn multipdgmmod []
  (let [langlist (slurp "pvlists/menu-langs.txt")
        languages (split langlist #"\n")]
  (layout/common 
   [:h3 "Checkbox: Multilingual Display"]
   ;;[:p "Use this option to pick one or more  paradigms from a given language or set of languages to be displayed as a single paradigm. (NB: Will only combine paradigms with identical headers.)"]
   [:p "Choose Languages and Type"]
   ;; [:p error]
   ;;[:hr]
   (form-to [:post "/multimodqry"]
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
                 ;;[:option {:value (lower-case language)} language])]]]
                 [:div {:class "form-group"}
                  [:label 
                   (check-box {:name "languages[]" :value (lower-case language)} language) language]])]]
                 ;; from https://groups.google.com/forum/#!topic/compojure/5Vm8QCQLsaQ
                 ;; (check-box "valclusters[]" false valcluster) (str valcluster)]]
             ;;(submit-button "Get pdgm")
             [:tr [:td ]
              [:td [:input#submit
                    {:value "Get PDGM Value Clusters", :name "submit", :type "submit"}]]]]
            )
   [:hr])))

(defn handle-multimodqry
  [languages pos]
  (layout/common 
       ;;[:h3 "Paradigms"]
       ;;[:p "Choose Value Clusters For: " language "/" pos]
       ;;[:p error]
       ;;[:hr]
   (form-to [:post "/multimoddisplay"]
            [:table
             [:tr [:td "PDGM Type: " ]
              [:td
               (check-box {:name "pos" :value pos :checked "true"} pos) (str (upper-case pos))]]
              [:tr [:td ]]
                 [:tr [:td "PDGM Language(s): " ]
                   (for [language languages]
                  [:td 
                   [:div (str (capitalize language) " ")]])]
                 [:tr [:td "PDGM Value Clusters: " ]
                   (for [language languages]
                  [:td 
                   {:title "Choose a value.", :name "valcluster"}
                     (let [valclusterfile (str "pvlists/plexname-" pos "-list-" language ".txt")
                           valclusterlist (slurp valclusterfile)
                           valclusters (split valclusterlist #"\n")]
                       ;; For pdgm checkboxes, if pos is 'fv', there will be a
                       ;; label for the valcluster, then actual checkboxes will be 
                       ;; placed at different lexitems having the same valcluster. 
                       ;; Otherwise each valcluster will be a separate checkbox.
                       ;; The 'fv' type may be extended to other kinds of pdgms
                       ;; showing identical valclusters with different lex items
                       ;; (e.g., nominal paradigms with inflectional case of the
                       ;; Latin or Greek type).
                       (for [valcluster valclusters]
                         (if (= pos "fv")
                           (let [clusters (split valcluster #":")
                                 clustername (first clusters)
                                 plex (last clusters)
                                 lexitems (split plex #",")]
                             [:div {:class "form-group"}
                              [:label (str clustername ": ")
                               (for [lex lexitems]
                                 [:span 
                                  (check-box {:name "valclusters[]" :value (str language "," clustername ":" lex) } lex) lex])]])
                           [:div {:class "form-group"}
                            [:label
                             (check-box {:class "checkbox1" :name "valclusters[]" :value (str language "," valcluster) } valcluster) valcluster]])))])]
                 ;;(submit-button "Get pdgm")
                 [:tr [:td ]
                  [:td [:input#submit
                        {:value "Display pdgms", :name "submit", :type "submit"}]]]])))

(defn addpnum
  [valclusters]
  (let [pnum (atom 0)]
    (for [valcluster valclusters]
      (let [pnum (swap! pnum inc)]
        (clojure.string/replace valcluster #"\r\n(\S)" (str "\r\nP-"  pnum  ",$1"))))))

(defn vc2req
 [valclusters pos]
  (let [vcvec (split valclusters #" ")
        lprefmap (read-string (slurp "pvlists/lprefs.clj"))
        pmnum (atom 0)
        pdgmnums (into [] (take (count vcvec) (iterate inc 1)))
        ]
    (for [valcluster vcvec]
      (let [;;valcluster (nth vcvec (dec pdgmnum))
            vals (split valcluster #"," 2)
            language (first vals)
            vcluster (last vals)
            ;; all three of following fail to recalculate after 1st iteration
            pnum (.indexOf valclusters valcluster)
            pmnum (swap! pmnum inc)
            pdnum (nth pdgmnums pnum)
            lang (read-string (str ":" language))
            lpref (lang lprefmap)
            valstrng (clojure.string/replace vcluster #",*person|,*gender|,*number" "")
            valstr (clojure.string/replace valstrng #":," ":")
            vcstring (clojure.string/replace valstr #"," "_")
            query-sparql (cond 
                          (= pos "pro")
                          (sparql/pdgmqry-sparql-pro language lpref valstr)
                          (= pos "nfv")
                          (sparql/pdgmqry-sparql-nfv language lpref vcluster)
                          (= pos "noun")
                          (sparql/pdgmqry-sparql-noun language lpref vcluster)
                          :else (sparql/pdgmqry-sparql-fv language lpref vcluster))
            req (http/get aama
                      {:query-params
                       {"query" query-sparql ;;generated sparql
                        ;;"format" "application/sparql-results+json"}})]
                        "format" "csv"}})
            ;; get rid of header
            pbody (clojure.string/replace (:body req) #"^.*?\r\n" "\r\n")
            ]
        ;; add pdgm name to each row of pbody as first value
        (clojure.string/replace pbody #"\r\n(\S)" (str "\r\n" vcstring  ",$1"))))))

(defn csv2pdgm
"Takes sorted 4-col csv list with vectors of pnames and headers, and outputs 5-col html table with first col for pname ref; cols are draggable and sortable."
 [pdgmstr2 valclusters]
(let  [pdgms (str valclusters)
       pnamestr (clojure.string/replace pdgms #"[\[\]\"]" "")
       pnames (split pnamestr #" ")
       ;; pdgmstr2 is a string of space-separated pdgmstrings, whose rows are
       ;; separated by \r\n and cells separated by ","
       ;; Take off the top header
       ;; If pdgms are to be comparable
       ;; all header strings will be same
       ;; so far (5/2/16) can't get header off of csv --
       ;; following does not work
       ;; psplit (split pdgmstr2 #"\r\n" 2)
       ;; header2 (first psplit)
       header (str "num,pers,gen,token")
       header2 (str "pdgm," header)
       pheads (split header2 #",")
       pstrings (split pdgmstr2 #" ")
       pdgmnum (atom 0)
       ]
  [:div
   [:p "Paradigms:"
    [:ol
     (for [pname pnames]
       [:li pname])]]
   [:hr]
   [:table {:id "handlerTable" :class "tablesorter sar-table"}
    [:thead
     [:tr
      (for [head pheads]
        [:th [:div {:class "some-handle"}] head])]]
    [:tbody 
     (for [pdgm pstrings]
       (let [pdgm-sp (split pdgm #"\\r\\n" 2)
             pheader (first pdgm-sp)
             pbody (last pdgm-sp)
             pdgmrows (split pbody #"\\r\\n")
             ;;pnum (swap! pdgmnum inc)
             ]
         ;;(if (= header pheader)
           (for [pdgmrow pdgmrows]
             [:tr
              ;;[:td (str "P-" pnum)]
              (let [pdgmrow2 (clojure.string/replace pdgmrow #"_" " ")
                    pdgmcells (split pdgmrow2 #",")]
                (for [pdgmcell pdgmcells]
                  [:td pdgmcell]))])
           ;;([:tr [:td (str "P-" pnum " does not have the header: " header)]])
           ;;)
       ))]]]))
        
(defn handle-multimoddisplay
  [valclusters pos]
  ;; send SPARQL over HTTP request
  (let [headerset1 (str "Paradigm " "Number " "Person " "Gender " "Token ")
        headerset2 (str "pdgm " "num " "pers " "gen ")
        headers (split headerset2 #" ")
        ;;valclusters2 (addpnum valclusters)
        pdgmvec (map #(vc2req  % pos) valclusters)
        header (first pdgmvec)
        pdgmstr1 (apply pr-str pdgmvec)
        pdgmstr2 (clojure.string/replace pdgmstr1 #"[\(\)\"]" "")
        pdgmtable (csv2pdgm pdgmstr2 valclusters)
        pdgms (str valclusters)
        pnamestr (clojure.string/replace pdgms #"[\[\]\"]" "")
        pnames (split pnamestr #" ")
        ]
         (layout/common
           [:h3#clickable "Paradigms " pos ": "  ]
           [:p "Click on column to sort (multiple sort by holding down shift key). Columns can be dragged by clicking and holding on 'drag-bar' at top of column."]
           [:hr]
           pdgmtable
           [:hr]
           [:div [:h4 "======= Debug Info: ======="]
            [:p "pdgmvec: " [:pre pdgmvec]]
            [:p "valclusters: " [:pre pdgms]]
            [:p "headerset2: " [:pre headerset2]]
            [:p "pdgmstr2: " [:pre pdgmstr2]]
            [:h4 "==========================="]]
           [:h3 "Parallel Display of Paradigms (Personal Pronoun and Finite Verb Only)"]
           [:p "At present only accommodates parallel display of paradigms with columns 'Number Person Gender Token' -- to be generalized."]         
           [:hr]
           (form-to [:post "/multimodplldisplay"]
                    [:table
                     [:tr [:td "PNames: "]
                      [:td 
                       ;;[:ol
                       ;; (for [pname pnames]
                       ;;   [:li pname])]]]
                            [:select#names.required
                            {:title "Chosen PDGMS", :name "pdgmnames"}
                            [:option {:value (str valclusters)} "Paradigm Names (as above)"]]]]
                     [:tr [:td "Header: "]
                      [:td [:select#header.required
                            {:title "Header", :name "header"}
                            [:option {:value headers} (str headers)] 
                            ]]]
                     [:tr [:td "Pivots: "]
                      [:td
                       [:div {:class "form-group"}
                        [:label 
                         (for [head headers]
                           [:span
                           (check-box {:name "pivotlist[]" :value (.indexOf headers head)} head) head])]]]]
                     [:tr [:td "PString: "]
                     [:td [:select#pdgms.required
                            {:title "PDGMS", :name "pdgmstr2"}
                            [:option {:value pdgmstr2} "Paradigm Forms (as above)"]
                            ;;[:option {:value valclusters} (str valclusters)]
                            ]]]
                     ;; current algorithm combines actual png val configs
                     ;; into png vector made on the fly;
                     ;; next step is to allow choice of png vals as per
                     ;; text input fields below (for now can be left blank)
                     ;;[:tr [:td "Number: "]
                     ;; [:td [:input#num.required
                     ;;       {:title "Choose Number Values.", :name "nmbr"}
                     ;;       ]]]
                     ;;[:tr [:td "Person: " ]
                     ;; [:td [:input#pers.required
                     ;;       {:title "Choose Person Values.", :name "pers"}
                     ;;       ]]]
                     ;;[:tr [:td "Gender: " ]
                     ;; [:td [:input#gen.required
                     ;;       {:title "Choose Gender Values.", :name "gen"}
                     ;;       ]]]
                     ;;(submit-button "Get pdgm")
                     [:tr [:td ]
                      [:td [:input#submit
                            {:value "Display Paradigms in Parallel", :name "submit", :type "submit"}]]]])
     [:hr]
     [:script {:src "js/goog/base.js" :type "text/javascript"}]
     [:script {:src "js/webapp.js" :type "text/javascript"}]
     [:script {:type "text/javascript"}
      "goog.require('webapp.core');"])))

(defn pstring2map
  "Used in handle-multimodplldisplay2. Takes out default pivot, pdgm; splits features from token and makes hash-map."
  [pdgm]
  (let [pdgm1a (clojure.string/replace pdgm #"^\\r\\n.*?," "") ;; initial line-feed & pdgm label out
        pdgm1b (clojure.string/replace pdgm1a #"\\r\\n$" "") ;; final line-feed out
        pdgmstring (clojure.string/replace pdgm1b #"\\r\\n.*?," "%%") ;; delimit rows (and other pdgms labels out) 
        pdgmstr (clojure.string/replace pdgmstring #",([^,]*%%)" "&$1") ;;delimit tokens
        pdgmstr2 (clojure.string/replace pdgmstr #",([^,]*)$" "&$1") ;; last token
        pdgm2 (clojure.string/replace pdgmstr2 #"&" " ") ;; set up for pmap
        pdgm3  (clojure.string/replace pdgm2 #"%%" " ")
        plist (split pdgm3 #" ") ;; change to vector of pairs
        pmap (apply hash-map plist)
        ]
    (clojure.walk/keywordize-keys pmap)
    ))

(defn handle-multimodplldisplay2
  "This version does not rely on external png file for sort order; keyset should be generalized beyond png to any sequence of cols between col-1 ('pivot', here limited to paradigms) and token column. Also need to 'presort' cols before initial display."
  [pdgmnames headerset2 pdgmstr2]
  (let [pnamestr (clojure.string/replace pdgmnames #"[\[\]\"]" "")
        pnames (split pnamestr #" ")
        pstrings (split pdgmstr2 #" ")
        pmaps (for [pdgm pstrings] (pstring2map pdgm))
        ;; headerset should be derived from pdgm query (=headerset2)
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
      [:p "Click on column to sort (multiple sort by holding down shift key). Columns can be dragged by clicking and holding on 'drag-bar' at top of column."]
     [:p "Paradigms:"
      [:ol
      (for [pname pnames]
        [:li pname])]]
      [:hr]
      [:table {:id "handlerTable" :class "tablesorter sar-table"}
         [:thead
          (for [head heads]
            [:th [:div {:class "some-handle"}] head])
          (for [pdgmnum pdgmnums]
            [:th [:div {:class "some-handle"}] (str "P-" pdgmnum)])]
       [:tbody
        (for [keys keyset]
          [:tr
           (let [kstring (clojure.string/replace keys #"^:" "")
                 npgs (split kstring #",")
                 kstrkey (keyword kstring)]
               [:div
                (for [npg npgs]
                  [:td npg])
                (for [pmap pmaps]
               [:td (kstrkey pmap)])])])]]
      [:div 
       [:h4 "======= Debug Info: ======="]
       [:p "pdgmstr2: " [:pre pdgmstr2]]
       [:p "pmaps: " [:pre pmaps]]
       [:p "keylists: " [:pre keylists]]
       [:p "keystring: " [:pre keystring]]
       [:p "keyset: " [:pre keyset]]
       [:h4 "======================="]]
      [:script {:src "js/goog/base.js" :type "text/javascript"}]
      [:script {:src "js/webapp.js" :type "text/javascript"}]
      [:script {:type "text/javascript"}
       "goog.require('webapp.core');"]])))
    
(defn pstring2map2 
  "Used in handle-multimodplldisplay4. Takes pivot property out of comma-separated properties in pdgm string, and arranges the rest as a set of {:property-list 'token'} maps" 
  [pdgm pivot] 
  (let [pdgm1 (clojure.string/replace pdgm #"^\\r\\n.*?," "") ;; initial pivot out 
        pdgm2 (clojure.string/replace pdgm1 #"(\\r\\n).*?," "$1") ;; other pivots out 
        pdgmstring (clojure.string/replace pdgm2 #"\\r\\n" "%%") ;; delimit rows 
        pdgmstr (clojure.string/replace pdgmstring #",([^,]*?%%)" "&$1") ;; delimit tokens 
        pdgm3 (clojure.string/replace pdgmstr #"&" " ") ;; setup for pmap 
        pdgm4 (clojure.string/replace pdgm3 #"%%" " ") 
        ;;pdgm4 (clojure.string/replace pdgm3 #"\s*$" "") ;; final line-feed out 
        plist (split pdgm4 #" ") 
        pmap (apply hash-map plist) ]
    (clojure.walk/keywordize-keys pmap) ))

(defn cleanpdgms [pdgmstr]
  (let [pdgmstr-a (clojure.string/replace pdgmstr #"\\r\\n$" "")
        pdgmstr-b (clojure.string/replace pdgmstr-a #"^\\r\\n" "")
        pdgmstr-c (clojure.string/replace pdgmstr-b #":" "_")]
    (clojure.string/replace pdgmstr-c #"\\r\\n " "")))

;; from http://stackoverflow.com/questions/1394991
(defn vec-remove
  "remove elem in coll"
   [coll pos]
   (vec (clojure.core/concat (subvec coll 0 pos) (subvec coll (inc pos)))))

;; conj-in & merge-matches from http://stackoverflow.com/questions/2203213/
(defn conj-in [m map-entry]
  (update-in m [(key map-entry)] (fnil conj []) (val map-entry)))
(defn merge-matches [property-map-list]
  (reduce conj-in {} (apply clojure.core/concat property-map-list)))
(defn vec2map 
  "join all elements of vector but last with ','; last with ' '"
  [row] 
  (let [prow (clojure.string/join "," row)]
   (clojure.string/replace prow #"(.*),(.*?$)" "$1 $2")))
;;(clojure.string/replace (join "," row) #"(.*),(.*?$)" "$1 $2"))

(defn pstring2map3 
  "Used in handle-multimodplldisplay4. Takes pivot property out of comma-separated properties in pdgm string, and arranges the rest as a set of {:property-list 'token'} maps" 
  [prmp] 
  (let [hmap1 (split prmp #" ")
        hmap2 (apply hash-map hmap1)]
    (clojure.walk/keywordize-keys hmap2)))

(defn make-pmap
  "Build up hash-map key by joining pivot-vals and val by removing pivot-vals"
  [pcell pivots]
    (let [pklist (vec (for [pivot pivots] (nth pcell pivot)))
          pkstr (join "+" pklist)]
      (hash-map  pkstr (vec (remove (set pklist) pcell)))))
        
(defn handle-multimodplldisplay4
  "In this version pivot/keyset can be generalized beyond png any col (eventually any sequence of cols) between col-1 and token column. (Need to find out how to 'presort' cols before initial display?)"
  [pdgms headerset2 pdgmstr2 pivotlist]
  (let [pnamestr (clojure.string/replace pdgms #"[\[\]\"]" "")
        pnames (split pnamestr #" ")
        pivots (map read-string pivotlist)
        ;;pivot (read-string pivotname)
        ;; get rid of spurious line-feeds
        ;;pdgmstr2a (str pdgmstr2)
        pdgmstr3 (cleanpdgms pdgmstr2)
        ;; map each 'val-string-w/o-pivot-val token' to token
        prows (split pdgmstr3 #"\\r\\n")
        pcells (for [prow prows] (split prow #","))
        pivotlist2 (str "pdgm,num")
        ;;pivots (split pivotlist2 #",")
        pivot-map (for [pcell pcells] (make-pmap pcell pivots))
        ;; group the val-tokens associated with each pivot val
        newpdgms (merge-matches pivot-map)
        pdgmnums (into [] (take (clojure.core/count pnames) (iterate inc 1)))
        ;; make a vector of pivot vals
        ;;pvalvec (vec (for [pdgmnum pdgmnums] (str "P-" pdgmnum)))
        ;;pnames2 (vec (for [npdgm newpdgms] (str (key npdgm))))
        pvalvec (vec (for [npdgm newpdgms] (str (key npdgm))))
        ;; e.g., ["Plural" "Singular"]
        ;; make a vector of pdgm rows for each pivot
        vvec (for [npdgm newpdgms] (val npdgm))
        pmapvec (for [vgroup vvec] (for [vrow vgroup] (vec2map vrow)))
        ;; transform pmaps to hash-maps
        prmaps (for [prmap pmapvec] (for [prmp prmap] (pstring2map3 prmp)))
        ;;prmaps (for [prmap pmapvec] (for [prmp prmap]  (clojure.walk/keywordize-keys (apply hash-map (split prmp #" ")))))
        pmaps (for [prmap prmaps] (apply conj prmap))
        headerset3 (clojure.string/replace headerset2 #"[\[\]\"]" "")
        heads (split (str headerset3) #" ")
        ;;headvec (for [pivot pivotlist] (vec-remove heads pivotlist))
        pivotnames (vec (for [pivot pivots] (nth heads pivot)))
        headvec (vec (remove (set pivotnames) heads))
        ;; set of lists of vaue-combination-terms
        keylists (set (for [pmap pmaps] (keys pmap)))
        ;;keylists (set (keys pmap))
        ;; why is replace necessary; none of deleted chars should be in keys
        keystring (clojure.string/replace (str keylists) #"[#(){}]" "")
        keyvec (split keystring #" ")
        ;; set of all value combinations, as strings, in the combined pdgms
        keyset (set keyvec)
        ]
    (layout/common
     [:body
      [:h3 "Parallel Display of Paradigms:" ]
      [:p "Click on column to sort (multiple sort by holding down shift key). Columns can be dragged by clicking and holding on 'drag-bar' at top of column."]
     [:p "Paradigms:"
      [:ol
      (for [pname pnames]
        [:li pname])]]
      [:hr]
      [:table {:id "handlerTable" :class "tablesorter sar-table"}
         [:thead
          (for [head headvec]
            [:th [:div {:class "some-handle"}] head])
          (for [pval pvalvec]
            [:th [:div {:class "some-handle"}] pval])]
       [:tbody
               (for [keys keyset]
          [:tr
           (let [kstring (clojure.string/replace keys #"^:" "")
                 npgs (split kstring #",")
                 kstrkey (keyword kstring)]
               [:div
                (for [npg npgs]
                  [:td npg])
                (for [pmap pmaps]
               [:td (kstrkey pmap)])])]) ]]
       [:div [:h4 "======= Debug Info: ======="]
        [:p "pdgms: " [:pre pdgms]]
        [:p "pnames: " (str pnames)]
        [:p "headerset2: " [:pre headerset2]]
        [:p "pivotlist: " (str pivotlist)]
        [:p "pivotnames: " (str pivotnames)]
        [:p "heads: " (str heads)]
        [:p "headvec: " (str headvec)]
        [:p "prows: "  (str prows) [:pre prows]]
        [:p "pcells: " (println (str pcells)) [:pre pcells]]
        [:p "pivot-map: " [:pre pivot-map]]
        [:p "newpdgms: " [:pre newpdgms]]
        [:p "pvalvec: " (str pvalvec) [:pre pvalvec]]
        [:p "pdgmstr2: " [:pre pdgmstr2]]
        ;;[:p "vvec: " [:pre vvec]]
        [:p "vvec: " (str vvec)]
        [:p "pmapvec: " [:pre pmapvec]]
        [:p "prmaps: " [:pre prmaps]]
        [:p "pmaps: " [:pre pmaps]]
        [:p "keylists: " [:pre keylists]]
        [:p "keystring: " [:pre keystring]]
        [:p "keyset: " [:pre keyset]]
        [:p "==========================="]]
      [:script {:src "js/goog/base.js" :type "text/javascript"}]
      [:script {:src "js/webapp.js" :type "text/javascript"}]
      [:script {:type "text/javascript"}
       "goog.require('webapp.core');"]])))


(defroutes multipdgmmod-routes
  (GET "/multipdgmmod" [] (multipdgmmod))
  (POST "/multimodqry" [languages pos] (handle-multimodqry languages pos))
  (POST "/multimoddisplay" [valclusters pos] (handle-multimoddisplay valclusters pos))
  ;;(POST "/multimodplldisplay" [pdgmnames header pdgmstr2] (handle-multimodplldisplay2 pdgmnames header pdgmstr2)))  
(POST "/multimodplldisplay" [pdgmnames header pdgmstr2 pivotlist] (handle-multimodplldisplay4 pdgmnames header pdgmstr2 pivotlist)))
