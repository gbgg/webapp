(ns webapp.routes.pdgmmultmod
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

(defn pdgmmultmod []
  (let [langlist (slurp "pvlists/menu-langs.txt")
        languages (split langlist #"\n")]
    (layout/common 
     [:h3 "Multiparadigm Modifiable Display"]
     [:p "Use this option to pick one or more  paradigms from a given language or set of languages to be displayed as a single paradigm. (NB: For the moment, will only combine paradigms with identical headers in identical order. Future version should allow for blank cols in one or more of the languages, different column orders, and identification of different terminologies.)"]
     [:p "Choose Languages"]
     (form-to [:post "/pdgmmultmodqry"]
              [:table
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

(defn handle-pdgmmultmodqry
  [languages]
  (layout/common
   [:h3 "PDGM Property List"]
   [:p "NB: For the moment, in order to combine paradigms, the columns must contain values of identical properties (altough not necessarily identical terminology), and be in the same order ."] 
   [:p "Choose PDGM"] 
   (form-to [:post "/pdgmmultmoddisplay"]
            [:table
             [:tr [:td "PDGM Language(s): " ]
              (for [language languages]
                [:td 
                 [:div (str (capitalize language) " ")]])]
             [:tr [:td "PDGM Value Clusters: " ]
                (for [language languages]
                  [:td 
                   {:title "Choose a value.", :name "valcluster", :width "10"}
                   (let [valclusterfile (str "pvlists/pdgm-index-" language ".txt")
                         valclusterlist (slurp valclusterfile)
                         ;;valclusterlst (clojure.string/replace valclusterlist #":.*?\n" "\n")
                         valclusterset (into (sorted-set) (clojure.string/split valclusterlist #"\n"))]
                     (if (re-find #"EmptyList" valclusterlist)
                       [:div (str "There are no  paradigms in the " language " archive.")]
                       (for [valcluster valclusterset]
                         [:div {:class "form-group"}
                          [:label
                           (check-box {:class "checkbox1" :name "lvalclusters[]" :value (str language "," valcluster) } valcluster) valcluster]]))
                     )])]
             ;;(submit-button "Get pdgm")
             [:tr [:td ]
              [:td [:input#submit
                    {:value "Display pdgms", :name "submit", :type "submit"}]]]])))


(defn vc2req
"Makes the requests that output a vector of csv string representing each of the pdgms."
  [pdgmclusters]
  (let [lprefmap (read-string (slurp "pvlists/lprefs.clj"))]
    (for [pdgmcluster pdgmclusters]
      (let [vals (split pdgmcluster #"-" 2)
            pnum (first vals)
            ;;pnlng (split plang #"-", 2)
            ;;pnum (first pnlng)
            lvalcluster (last vals)
            query-sparql (sparql/pdgmqry-sparql-gen-vrbs lvalcluster)
            req (http/get aama
                          {:query-params
                           {"query" query-sparql ;;generated sparql
                            ;;"format" "application/sparql-results+json"}})]
                            "format" "csv"}})
            pbody1 (:body req)
            pbody1a (clojure.string/replace pbody1 #"\r\"" "")
            pbody2 (str "Pdgm," (clojure.string/replace pbody1a #" " "_"))
            ]
        ;; add pdgm number to each row of pbody as first value
        (clojure.string/replace pbody2 #"\r\n(\S)" (str "\r\n" pnum ",$1"))))))

(defn csv2pmap
  "Takes vector of pdgm strings and returns unified pmap of vector by splitting off header from rows, and then interleaving headervec and rowvec."
    [pdgmstrvec]
  (for [pdgmstr pdgmstrvec]
    (let [;;headerstr (first (split pdgmstr #"\n" 2))
          headerstr (first (split pdgmstr #"\r\n" 2))
          headervec (for [header (split headerstr #",")] (keyword header))
          rows (last (split pdgmstr  #"\r\n" 2))
          rowvec (split rows #"\r\n")]
       (for [row rowvec] (apply assoc {} (interleave headervec (split row #",")))))))

(defn csv2pdgm
  "Takes sorted n-col csv list with vectors of pnames and headers, and outputs n+1-col html table with first col for pname ref; cols are draggable and sortable."
  [pdgmvecstr pnamestr2 pmap]
  (let  [;; pdgmstr2 is a string of space-separated pdgmstrings, whose rows are
         ;; separated by \r\n and cells separated by ","
         pnames (split pnamestr2 #" ")
         pdgmvec (split pdgmvecstr #" ")
         ;; Pool headers into set
         headerrows  (join #"," (for [pdgm pdgmvec] (first (split pdgm #"\\r\\n" 2))))
         headerset (into (sorted-set) (split headerrows #","))
         pheads (into [] headerset)
         ]
    [:div
     [:p "Paradigm Names:"
      [:ul
       (for [pname pnames]
         [:li pname])]]
     ;;[:p "pmap: " [:pre pmap]]
     ;;[:p "pdgmmap: " [:pre pdgmmap]]
     ;;[:p "keyvec: " (for [key keyvec] [:pre key])]
     [:p "Paradigm Heads:"
      [:ul
       (for [pdgm pdgmvec]
         [:li(first (split pdgm #"\\r\\n" 2))])]]
     [:p "pdgmvec: " [:p pdgmvec]]
     [:p "Headerset: " [:pre headerset]]
     [:hr]
     [:table {:id "handlerTable" :class "tablesorter sar-table"}
      [:thead
       [:tr 
        ;;[:th [:div {:class "some-handle"}  [:br] "Pdgm"]]
        (for [head pheads]
          [:th [:div {:class "some-handle"}  [:br] (capitalize head)]])
        ]]
      [:tbody 
       (for [map pmap]
         (for [submap map]
         [:tr
          (for [key keyvec]
            [:td 
             (if (key submap )
               (key submap)
               (str "_"))])]))]
      ]]))

(defn addpnum
"Provides for convenience of reference an index number for each of the pdgms." 
  [pnames]
  (for [pname pnames]
    (str "P" (.indexOf pnames pname) "-" pname)))

(defn handle-pdgmmultmoddisplay
  "Makes single combo pdgm with headers of headerset1/2. Need to make it work with arbitrary headers ('vheader' below)."
  [lvalclusters]
  (let [headerset1 (str "Paradigm " "Number " "Person " "Gender " "Token ")
        headerset2 (str "pdgm " "num " "pers " "gen ")
        headers (split headerset2 #" ")
        ;;pnames (split pnamestr2 #" ")
        ;; problem: addpnum gives LazySequence
        pdgmclusters (addpnum lvalclusters)
        pdgmvec (vc2req pdgmclusters)
        ;;vheader (first pdgmvec)
        pdgmstrvec1 (apply pr-str pdgmvec)
        pdgmstrvec2 (clojure.string/replace pdgmstrvec1 #"[\(\)\"]" "")
        pmap (csv2pmap pdgmvec)
        pmapstr (into [] (for [pm pmap] (apply str pm)))
        pdgmnames (apply pr-str pdgmclusters)
        pnamestr1 (clojure.string/replace pdgmnames #"[\[\]\"]" "")
        pnamestr2 (clojure.string/replace pnamestr1 #"%" ".")
        pdgmtable (csv2pdgm pdgmstrvec2 pnamestr2 pmap)
        ;; following needs to be migrated from csv2pdgm, which then is (csv2pdgm pheads pnamestr2 pmap)
         pdgmvec2 (split pdgmstrvec2 #" ")
         ;; Pool headers into set
         headerrows  (join #"," (for [pdgm pdgmvec2] (first (split pdgm #"\\r\\n" 2))))
         headerset (into (sorted-set) (split headerrows #","))
         pheads (into [] headerset)
        keyvec (for [head pheads] (keyword head))
        pdgmtable (pheads pmap keyvec)
        pivots (pop pheads)
        ]
    (layout/common
     [:h3#clickable "Paradigms: Sequential Display " ]
     [:p "Click on column to sort (multiple sort by holding down shift key). Columns can be dragged by clicking and holding on 'drag-bar' at top of column."]
     [:hr]
     [:p "lvalclusters: " [:p pnamestr2]]
     [:p "pdgmclusters: " [:p pdgmclusters]]
     ;;[:p "pnames: " [:pre pnames]]
     ;;[:p "pnames2: " [:pre pnames2]]
     [:p "pdgmvec: " [:pre pdgmvec]]
     ;;[:p "pdgmst1: " [:pre pdgmstrvec1]]
     ;;[:p "pdgmstr2: " [:pre pdgmstrvec2]]
     [:p "pmap: " [:pre pmap]]
     [:p "pmapstr: " [:p pmapstr]]
     [:p "pivots: " [:pre pivots]]
     [:hr]
     pdgmtable
     [:hr]
     [:h3 "Parallel Display of Paradigms"]
     [:p "At present only accommodates parallel display of pronominal and verbval paradigms where merged paradigms have same number of columns -- to be generalized."]         
     [:hr]
     [:h3 "PDGM Value List"]
     [:p "Choose Parallel Display Format"]
     (form-to [:post "/pdgmmultmodplldisplay"]
              [:table
               [:tr [:td "PNames: "]
                [:td 
                 [:select#names.required
                  {:title "Chosen PDGMS", :name "pdgmnames"}
                  [:option {:value (do (apply str pnamestr2))} "Paradigm Names (as above)"]]]]
               [:tr [:td "Header: "]
                [:td [:select#header.required
                      {:title "Header", :name "header"}
                      [:option {:value pivots} (str pivots)] 
                      ]]]
               [:tr [:td "Pivots: "]
                [:td
                 [:div {:class "form-group"}
                 [:label 
                   (for [head pivots]
                     [:span
                      (check-box {:name "pivotlist[]" :value (.indexOf pivots head)} head) head])]]]]
               [:tr [:td "PString: "]
                [:td [:select#pdgms.required
                      {:title "PDGMS", :name "pdgmstrvec2"}
                      [:option {:value pdgmstrvec2} "Paradigm Forms (as above)"]]]]
               [:tr [:td "PMap: "]
                [:td [:select#pdgms.required
                      {:title "PMAP", :name "pmapstr"}
                      [:option {:value pmapstr} "Paradigm Map (as above)"]
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
     [:div [:h4 "======= Debug Info: ======="]
      [:p "pdgmvec: " [:pre pdgmvec]]
      ;;[:p "pdgmvec2: " [:pre pnames2]]
      ;;[:p "pos: " [:pre pos]]
      [:p "valclusters: " [:pre pdgmnames]]
      [:p "headerset2: " [:pre headerset2]]
      [:p "pdgmstrvec2: " [:pre pdgmstrvec2]]
      [:h4 "==========================="]]
     [:script {:src "js/goog/base.js" :type "text/javascript"}]
     [:script {:src "js/webapp.js" :type "text/javascript"}]
     [:script {:type "text/javascript"}
      "goog.require('webapp.core');"])))

(defn cleanpdgms [pdgmstr]
  "This version also gets rid of initial header row in each pdgm substring"
  (let [pdgmstr-a (clojure.string/replace pdgmstr #"\\r\\n$" "")
        pdgmstr-b (clojure.string/replace pdgmstr-a #"^.*?\\r\\n" "")
        pdgmstr-c (clojure.string/replace pdgmstr-b #":" "_")]
    ;; get rid of initial header row of each member pdgm
    (clojure.string/replace pdgmstr-c #"\\r\\n .*?(\\r\\n)" "$1")))

(defn make-pmap
  "Build up hash-map key by joining pivot-vals and val by removing pivot-vals"
  [pcell pivots]
  (let [pklist (vec (for [pivot pivots] (nth pcell pivot)))
        pkstr (join "+" pklist)]
    (hash-map  pkstr (vec (remove (set pklist) pcell)))))

;; from http://stackoverflow.com/questions/1394991
;; doesn't seem to be used
(defn vec-remove
  "remove elem in coll"
  [coll pos]
  (vec (clojure.core/concat (subvec coll 0 pos) (subvec coll (inc pos)))))

;;conj-in & merge-matches from http://stackoverflow.com/questions/2203213/
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

(defn pstring2maps
  "Used in handle-multimodplldisplay4. Takes pivot property out of comma-separated properties in pdgm string, and arranges the rest as a set of {:property-list 'token'} maps" 
  [prmp] 
  (let [hmap1 (split prmp #" ")
        hmap2 (apply hash-map hmap1)]
    (clojure.walk/keywordize-keys hmap2)))

(defn join-pmaps
  "Join individual '{:values token}' maps into single map"
  [prmaps]
  (for [prmap prmaps]
    (if (> (count prmap) 1)
      (apply conj prmap)
      (apply conj (conj prmap {}))
      )))
;;(prmap))))

(defn handle-pdgmmultmodplldisplay
  "In this version pivot/keyset can be generalized beyond png any col (eventually any sequence of cols) between col-1 and token column. (Need to find out how to 'presort' cols before initial display?) [Current version has very ugly string=>list pdgms=>pnames. Simplify?]"
  [pdgms headers pdgmstr2 pivotlist pmapstr]
  (let [pnamestr1 (clojure.string/replace pdgms #"[\[\]\"]" "")
        pnamestr2 (clojure.string/replace pnamestr1 #"%" ".")
        pnamestr3 (clojure.string/replace  pnamestr2 #"\w(P\d+-)" " $1")
        ;;pnames (clojure.string/split-lines pnamestr3)
        pnames (split pnamestr3 #" " )
        ;;pnames (map read-string pdgms)
        pivots (map read-string pivotlist)
        ;;pivot (read-string pivotname)
        ;; get rid of spurious line-feeds
        pdgmstr3 (cleanpdgms pdgmstr2)
        ;; map each 'val-string-w/o-pivot-val token' to token
        prows (split pdgmstr3 #"\\r\\n")
        pcells (for [prow prows] (split prow #","))
        pivot-map (for [pcell pcells] (make-pmap pcell pivots))
        ;; group the val-tokens associated with each pivot val
        newpdgms (merge-matches pivot-map)
        pvalvec (vec (for [npdgm newpdgms] (str (key npdgm))))
        ;; e.g., ["Plural" "Singular"]
        ;; make a vector of pdgm rows for each pivot
        vvec (for [npdgm newpdgms] (val npdgm))
        pmapvec (for [vgroup vvec] (for [vrow vgroup] (vec2map vrow)))
        ;; transform pmaps to hash-maps
        prmaps (for [prmap pmapvec] (for [prmp prmap] (pstring2maps prmp)))
        ;;pmaps (for [prmap prmaps] (apply conj prmap))
        pmaps (join-pmaps prmaps)
        headers2 (clojure.string/replace headers #"[\[\]\"]" "")
        heads (split (str headers2) #" ")
        ;;headvec = headerset minus pivot namesn
        pivotnames (vec (for [pivot pivots] (nth heads pivot)))
        headvec (vec (remove (set pivotnames) heads))
        ;; set of lists of vaue-combination-terms
        keylists (vec (for [pmap pmaps] (keys pmap)))
        ;;keylists (set (keys pmap))
        ;; replace seems to be ad hoc cluj; 
        ;; only '[' and ']' appear in one-line paradigm keyvec (but should not)
        ;; other deleted chars should not be in keys in the first place
        keystring (clojure.string/replace (str keylists) #"[#(){}\[\]]" "")
        keyvec (split keystring #" ")
        ;; set of all value combinations, as strings, in the combined pdgms
        keyset (set keyvec)
        ]
    (layout/common
     [:body
      [:p [:h3 "Paradigms: Parallel Display --  Pivot " (str pivotnames)]]
      [:p "Click on column to sort (multiple sort by holding down shift key). Columns can be dragged by clicking and holding on 'drag-bar' at top of column."]
      [:p "Paradigms:"
       [:ul
        (for [pname pnames]
          [:li pname])]
       ]
      [:hr]
      [:table {:id "handlerTable" :class "tablesorter sar-table"}
       [:thead
        (for [head headvec]
          [:th [:div {:class "some-handle"} [:br] head]])
        (for [pval pvalvec]
          ;;[:div 
          (let [pvals (split pval #"\+")]
            [:th [:div {:class "some-handle"} [:br]
                  (for [pv pvals]
                    [:div  [:em pv] ])]]))]
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
                ;; following creates problems for forms w/o '_'
                ;;(let [pmap1 (clojure.string/replace (kstrkey pmap) #"_" " ")] 
                [:td (kstrkey pmap)])])]) ]]
      [:p " "]
      [:p " "]
      [:div [:h4 "======= Debug Info: ======="]
       [:p "pdgms: " [:pre pdgms]]
       [:p "pnamestr3: " [:pre pnamestr3]]
       [:p "pnames: " [:pre pnames]]
       [:p "pivotlist: " (str pivotlist)]
       [:p "prows: "  (str prows) [:pre prows]]
       [:p "pcells: " (apply str pcells) [:pre pcells]]
       [:p "pivot-map: " [:pre pivot-map]]
       [:p "newpdgms: " [:pre newpdgms]]
       [:p "newpdgms: " (str newpdgms)]
       [:p "pvalvec: " (str pvalvec)]
       [:p "pdgmstr2: " [:pre pdgmstr2]]
       [:p "pdgmstr3: " [:pre pdgmstr3]]  
       [:p "pmapstr: " [:p pmapstr]]
       ;;[:p "vvec: " [:pre vvec]] ;;!!raises "not valid element" exception
       ;;[:p "vvec: " (str vvec)] ;; "not valid el." excp. with "!" in text
       [:p "pmapvec: " [:pre pmapvec]]
       [:p "pmapvec: " (str pmapvec)]
       [:p "prmaps: " [:pre prmaps]]
       [:p "pmaps: " [:pre pmaps]]
       [:p "headers: " [:pre headers]]
       [:p "headers2: " [:pre headers2]] 
       [:p "heads: " (str heads)]
       [:p "pivotnames: " (str pivotnames)]
       [:p "headvec: " (str headvec)]
       [:p "keylists: " (str keylists)]
       [:p "keystring: " [:pre keystring]]
       [:p "keyvec: " [:pre keyvec]]
       [:p "keyvec: " (str keyvec)]
       [:p "keyset: " [:pre keyset]]
       [:p "==========================="]]
      [:script {:src "js/goog/base.js" :type "text/javascript"}]
      [:script {:src "js/webapp.js" :type "text/javascript"}]
      [:script {:type "text/javascript"}
       "goog.require('webapp.core');"]])))


(defroutes pdgmmultmod-routes
  (GET "/pdgmmultmod" [] (pdgmmultmod))
  (POST "/pdgmmultmodqry" [languages] (handle-pdgmmultmodqry languages))
  (POST "/pdgmmultmoddisplay" [lvalclusters] (handle-pdgmmultmoddisplay lvalclusters))
  (POST "/pdgmmultmodplldisplay" [pdgmnames header pdgmstrvec2 pivotlist pmapstr] (handle-pdgmmultmodplldisplay pdgmnames header pdgmstrvec2 pivotlist pmapstr)))
