(ns webapp.routes.pvprvllg
 (:refer-clojure :exclude [filter concat group-by max min replace])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [capitalize lower-case split replace join]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.form :refer :all]))

(def aama "http://localhost:3030/aama/query")

(defn pvprvllg []
  (let [langlist (slurp "pvlists/menu-langs.txt")
        languages (split langlist #"\n")
        ldomlist (slurp "pvlists/ldomainlist.txt")
        ldoms (split ldomlist #"\n")]
  (layout/common 
   [:h3 "Language-Property-Value Cooccurrences"]
   [:h4 "Choose Language Domain and Enter qstring: prop=Val,...prop=?prop,..."]
   ;;[:p "This family of queries accepts a language or group/family of languages and a comma-separated string of prop=val statements (in which case it returns the languages having that set of prop=val), combined optionally with one or more prop=?val statements (in which case it also returns the values of properties which may be associated with the specified properties)."]
   ;;[:ul [:li "[For example the query \"person=Person2,gender=Fem\" with language group \"Beja\" returns the Beja languages which have 2f forms; while the query \"person=Person2,gender=Fem,pos=?pos,number=?number\" with \"Beja\" returns a table with the language(s) having 2f forms, along with the part-of-speech values, and number values associated with these forms.]"]]
   ;; [:p error]
   ;;[:hr]
   (form-to [:post "/prvllgdisplay"]
            [:table
             [:tr [:td "Language Domain: " ]
              [:td 
               [:select#ldomain.required
               {:title "Choose a language domain.", :name "ldomain"}
                [:optgroup {:label "Languages"} 
                (for [language languages]
                  [:option {:value (lower-case language)} language])]
                [:optgroup {:label "Language Families"} 
               (for [ldom ldoms]
                (let [opts (split ldom #" ")]
               [:option {:value (last opts)} (first opts) ]))
                 [:option {:disabled "disabled"} "Other"]]]]]
             [:tr [:td "Prop=Val List: " ]
              [:td 
              (text-field 
               {:placeholder "person=Person2,gender=Fem,pos=?pos,number=?number"} 
               "qstring") ]
              ]
             ;;(submit-button "Get values")
             [:tr [:td ]
              [:td [:input#submit
                    {:value "Get language-prop-val", :name "submit", :type "submit"}]]]]))))

(defn csv2table
"Takes sorted n-col csv list with vectors of headers, and outputs n-col html table; cols are draggable and sortable."
 [heads formstr]
(let  [formrows (split formstr #"\r\n")]
  [:div
   [:table {:id "handlerTable" :class "tablesorter sar-table"}
    [:thead
     [:tr
      (for [head heads]
        [:th [:div {:class "some-handle"}  [:br] (capitalize head)]])]]
    [:tbody 
     (for [formrow formrows]
       [:tr
        (let [formcells (split formrow #",")]
          (for [formcell formcells]
            [:td formcell]))])]]]))


(defn handle-prvllgdisplay
  [ldomain qstring]
  ;; send SPARQL over HTTP request
  (let [query-sparql (sparql/prvllg-sparql ldomain qstring)
        query-sparql-pr (replace query-sparql #"<" "&lt;")
        req (http/get aama
                      {:query-params
                       {"query" query-sparql ;;generated sparql
                        ;;"format" "application/sparql-results+json"}})]
                        ;;"format" "text"}})]
                        "format" "csv"}})
        csvstring (:body req)
        csvstr (split csvstring #"\r\n" 2)
        ;; csvstr is a string of comma-separated cells
        ;; whose rows are separated by \r\n 
        ;; Take off the top header
        headers (first csvstr)
        heads (split headers #",")
        formstring (last csvstr)
        formtable (csv2table heads formstring)]
    (log/info "sparql result status: " (:status req))
    (layout/common
     [:body
      [:h3#clickable "Language-Property-Values: " ]
      [:p [:h4 "Language Domain: "]
       [:em ldomain]]
      [:p [:h4 "Query String: "]
       [:em qstring]]
      [:p "Click on column to sort (multiple sort by holding down shift key). Columns can be dragged by clicking and holding on 'drag-bar' at top of column."]
      [:hr]
      formtable
      [:hr]
      [:h3 "Parallel Display of Forms"]
      [:p "[Under development in branch 'formsearch'.]"]
           [:hr]
           [:div [:h4 "======= Debug Info: ======="]
            [:p "Query: "]
            [:p [:pre query-sparql-pr]]
            [:p "Response: "]
            [:p [:pre csvstring]]
            [:h4 "==========================="]]
           [:script {:src "js/goog/base.js" :type "text/javascript"}]
           [:script {:src "js/webapp.js" :type "text/javascript"}]
           [:script {:type "text/javascript"}
            "goog.require('webapp.core');"]])))

(defn handle-prvllgdisplay2
  "This version has form for parallel display of tokens."
  [ldomain qstring]
  ;; send SPARQL over HTTP request
  (let [query-sparql (sparql/prvllg-sparql ldomain qstring)
        query-sparql-pr (replace query-sparql #"<" "&lt;")
        req (http/get aama
                      {:query-params
                       {"query" query-sparql ;;generated sparql
                        ;;"format" "application/sparql-results+json"}})]
                        ;;"format" "text"}})]
                        "format" "csv"}})
        csvstring (:body req)
        csvstr (split csvstring #"\r\n" 2)
        ;; csvstr is a string of comma-separated cells
        ;; whose rows are separated by \r\n 
        ;; Take off the top header
        headers (first csvstr)
        heads (split headers #",")
        formstring (last csvstr)
        formtable (csv2table heads formstring)]
    (log/info "sparql result status: " (:status req))
    (layout/common
     [:body
      [:h3#clickable "Language-Property-Values: " ]
      [:p [:h4 "Language Domain: "]
       [:em ldomain]]
      [:p [:h4 "Query String: "]
       [:em qstring]]
      [:p "Click on column to sort (multiple sort by holding down shift key). Columns can be dragged by clicking and holding on 'drag-bar' at top of column."]
      [:hr]
      formtable
      [:hr]
      [:h3 "Parallel Display of Forms"]
      (form-to [:post "/formplldisplay"]
               [:table
                [:tr [:td "Header: "]
                 [:td [:select#header.required
                       {:title "Header", :name "header"}
                       [:option {:value headers} (str headers)] 
                            ]]]
                     [:tr [:td "Pivots: "]
                      [:td
                       [:div {:class "form-group"}
                        [:label 
                         (for [head heads]
                           [:span
                           (check-box {:name "pivotlist[]" :value (.indexOf heads head)} head) head])]]]]
                     [:tr [:td "FormString: "]
                     [:td [:select#forms.required
                            {:title "Forms", :name "formstring"}
                            [:option {:value formstring} "Forms (as above)"]
                            ]]]
                     [:tr [:td ]
                      [:td [:input#submit
                            {:value "Display Forms in Parallel", :name "submit", :type "submit"}]]]])
           [:hr]
           [:div [:h4 "======= Debug Info: ======="]
            [:p "Query: "]
            [:p [:pre query-sparql-pr]]
            [:p "Response: "]
            [:p [:pre csvstring]]
            [:h4 "==========================="]]
           [:script {:src "js/goog/base.js" :type "text/javascript"}]
           [:script {:src "js/webapp.js" :type "text/javascript"}]
           [:script {:type "text/javascript"}
            "goog.require('webapp.core');"]])))


(defn cleanpdgms [pdgmstr]
  (let [pdgmstr-a (clojure.string/replace pdgmstr #"\\r\\n$" "")
        pdgmstr-b (clojure.string/replace pdgmstr-a #"^\\r\\n" "")
        pdgmstr-c (clojure.string/replace pdgmstr-b #":" "_")]
    (clojure.string/replace pdgmstr-c #"\\r\\n " "")))

(defn make-pmap
  "Build up hash-map key by joining pivot-vals and val by removing pivot-vals"
  [pcell pivots]
    (let [pklist (vec (for [pivot pivots] (nth pcell pivot)))
          pkstr (join "+" pklist)]
      (hash-map  pkstr (vec (remove (set pklist) pcell)))))
        
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

(defn handle-formplldisplay
  "In this version pivot/keyset can be generalized beyond png any col (eventually any sequence of cols) between col-1 and token column. (Need to find out how to 'presort' cols before initial display?)"
  [headers formstring pivotlist]
  (let [pivots (map read-string pivotlist)
        ;;pivot (read-string pivotname)
        ;; get rid of spurious line-feeds
        ;; ??pdgmstr3 (cleanpdgms pdgmstr2)
        ;; map each 'val-string-w/o-pivot-val token' to token
        formrows (split formstring #"\\r\\n")
        formcells (for [formrow formrows] (split formrow #","))
        pivot-map (for [formcell formcells] (make-pmap formcell pivots))
        ;; group the val-tokens associated with each pivot val
        newforms (merge-matches pivot-map)
        pvalvec (vec (for [nform newforms] (str (key nform))))
        ;; e.g., ["Plural" "Singular"]
        ;; make a vector of form rows for each pivot
        vvec (for [nform newforms] (val nform))
        pmapvec (for [vgroup vvec] (for [vrow vgroup] (vec2map vrow)))
        ;; transform pmaps to hash-maps
        prmaps (for [prmap pmapvec] (for [prmp prmap] (pstring2maps prmp)))
        ;;pmaps (for [prmap prmaps] (apply conj prmap))
        pmaps (join-pmaps prmaps)
        ;;heads (split (str headerset3) #" ")
        ;;headvec (for [pivot pivotlist] (vec-remove heads pivotlist))
        pivotnames (vec (for [pivot pivots] (nth headers pivot)))
        headvec (vec (remove (set pivotnames) headers))
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
      [:p [:h3 "Parallel Display of Forms: Pivot " (str pivotnames)]]
      [:p "Click on column to sort (multiple sort by holding down shift key). Columns can be dragged by clicking and holding on 'drag-bar' at top of column."]
      [:hr]
      [:table {:id "handlerTable" :class "tablesorter sar-table"}
         [:thead
          (for [head headvec]
            [:th [:div {:class "some-handle"} [:br] (capitalize head)]])
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
               [:td (kstrkey pmap)])])]) ]]
      [:p " "]
      [:p " "]
       [:div [:h4 "======= Debug Info: ======="]
        [:p "pivotlist: " (str pivotlist)]
        [:p "pivotnames: " (str pivotnames)]
        [:p "headers: " (str headers)]
        [:p "headvec: " (str headvec)]
        [:p "formrows: "  (str formrows) [:pre formrows]]
        [:p "formcells: " (println (str formcells)) [:pre formcells]]
        [:p "pivot-map: " [:pre pivot-map]]
        [:p "newforms: " [:pre newforms]]
        [:p "newforms: " (str newforms)]
        [:p "pvalvec: " (str pvalvec)]
        [:p "formstrig: " [:pre formstring]]
        ;;[:p "vvec: " [:pre vvec]] ;;!!raises "not valid element" exception
        ;;[:p "vvec: " (str vvec)] ;; "not valid el." excp. with "!" in text
        [:p "pmapvec: " [:pre pmapvec]]
        [:p "pmapvec: " (str pmapvec)]
        [:p "prmaps: " [:pre prmaps]]
        [:p "pmaps: " [:pre pmaps]]
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

(defroutes pvprvllg-routes
  (GET "/pvprvllg" [] (pvprvllg))
  ;;(POST "/pdgmqry" [language pos] (handle-pdgmqry language pos))
  (POST "/prvllgdisplay" [ldomain qstring] (handle-prvllgdisplay ldomain qstring))
  (POST "/formplldisplay" [headers formstring pivotlist] (handle-formplldisplay headers formstring pivotlist)) 
  )


