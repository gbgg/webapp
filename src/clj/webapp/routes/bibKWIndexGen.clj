(ns webapp.routes.bibKWIndexGen
 (:refer-clojure :exclude [filter concat group-by max min count replace])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [split lower-case upper-case replace]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.form :refer :all]))

(def aama "http://localhost:3030/aama/query")

(defn bibKWIndexGen []
  (let [bibrefs (read-string (slurp "pvlists/bibrefs.edn"))]
    (layout/common 
     ;;[:h1#clickable "Afroasiatic Morphological Archive"]
     [:h3 "Generate Bibliography Key Word Index"]
     ;;[:p "(This option  enables the user to (re-)generate bibkwindex.edn, an index of key words, from bibrefs.edn, the general bibliography file, after that file has been modified.)"]
     [:hr]
     (form-to [:post "/bibKWIndexGen"]
              [:table
               [:tr [:td "Bibliography File: " ]
                [:td [:select#bibrefs.required
                      {:title "Choose a bibliography.", :name "bibrefs"}
                      [:option {:value bibrefs :label "bibrefs.edn"} bibrefs]]]]
               [:tr 
                [:td {:colspan "2"} [:input#submit
                                     {:value "Choose Bibliography: ", :name "submit", :type "submit"}]]]]))))


(defn make-kwindex [bibrefs listatom]
  (for [bibref bibrefs]
    (let [kref (key bibref)
          kws (split (last (val bibref)) #" ")]
      (for [kw kws]
        (swap! listatom conj (str kw "," kref))))))

;; transformation of csv2map, above
(defn make-klist
  [bibrefs]
  (let [kwlist (atom [])]
    (make-kwindex bibrefs kwlist)
    (into [] (sort @kwlist))))

(defn compact-list
"Takes string representing sorted bipartite list of bibrefID  keywords, with divider ',', and builds up list with single mention of each keyword paired with space-separated sting  of bibrefIDs."
 [kwlist]
 (let  [curpart1 (atom "")]
   (for [kwentry kwlist]
         (let [partmap (zipmap [:part1 :part2] (split kwentry #"," 2))]
           (if (= (:part1 partmap) @curpart1)
               (str " " (:part2 partmap))
             (do (reset! curpart1 (:part1 partmap))
                 (str ", " @curpart1 " " (:part2 partmap))))))))

;; repl doesn't think conj has right arity, even though this works as separate statement -- will it work in program? [as it does in listvlclplabel.clj]
(defn bibrefs2kmap
  [bibrefs]
  (let [klist (make-klist bibrefs)
        kwcompact (apply str (compact-list klist))
        kwcomp (clojure.string/replace kwcompact #"^, " "")
        kwvec (split kwcomp #", ")
        kwmap (for [kw kwvec] (hash-map (first (split kw #"," 2)) (last (split kw #" " 2))))]
  (into (sorted-map) (apply conj (clojure.walk/keywordize-keys kwmap)))))

(defn handle-bibKWIndexGen
  [bibrefs]
  (let [kmap (bibrefs2kmap bibrefs)]
    (spit "pvlists/bibkwindex.edn" kmap)
    (layout/common
     [:body
      ;;[:h1#clickable "Afroasiatic Morphological Archive"]
      [:h3 "New Key Word Index Written to pvlists/bibkwindex.edn"]
      [:pre kmap]
      [:script {:src "js/goog/base.js" :type "text/javascript"}]
      [:script {:src "js/webapp.js" :type "text/javascript"}]
      [:script {:type "text/javascript"}
       "goog.require('webapp.core');"]])))

(defroutes bibKWIndexGen-routes
  (GET "/bibKWIndexGen" [] (bibKWIndexGen))
  (POST "/bibKWIndexGen" [bibrefs] (handle-bibKWIndexGen bibrefs)))
