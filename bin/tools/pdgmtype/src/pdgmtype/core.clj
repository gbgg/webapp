(ns pdgmtype.core
  (require [clojure.edn :as edn]
           [clojure.set :as set]
           [clojure.string :as str])
  (:gen-class :main true))

;; adapted from ednsort.core
;; println output directed to [LANG]-pdgms.edn output-file by either:
;;   1) lein run [LANG]-pdgms.edn > [LANG]-pdgms-typed.edn {from pdgmtype directory}
;;   2) java -jar path/to/aama-pdgmtype.jar [LANG]-pdgms.edn > [LANG]-pdgms-typed.edn
;; :lx/muterms sections then to be replaced by hand after CAREFUL inspection 
;; ensures that all data preserved
;;
;; General case -
;; 1) for each termcluster of lxterms/muterms: #{}
;;    IF: :common has ':morphemeClass :X'
;;    THEN: add ':pdgmType :X' to termcluster
;;    ELSE:
;;    IF: :common has ':pos :Verb' AND: either :common has :person 
;;                                 OR: (first :terms)e has :person 
;;    THEN: add ':pdgmType :Finite' to termcluster
;; 2) add new term to lxterms/muterms
;; 3) println to [LANG]-sorted-lexterms.edn

(def pdgmTypeSet (atom #{}))

(defn pformat1
  [psection]
  (clojure.string/replace psection #"\], :" "],\n          :"))

(defn pformat2
  [psection]
  (clojure.string/replace psection #"}, " "},\n           "))

(defn do-preliminary
  [pdgm-map]
  (println "{")
  (let [lang (name (pdgm-map  :lang))
        subfamily (pdgm-map :subfamily)
        sgpref (pdgm-map :sgpref)
        dsource (pdgm-map :datasource)
        dsourcenotes (pdgm-map :datasourceNotes)
        webref (pdgm-map :geodemoURL)
        desc (pdgm-map :geodemoTXT)
        schemata (pdgm-map :schemata)
        pclass  (pdgm-map :pclass)
        morphemes (pdgm-map :morphemes)
        lexemes (pdgm-map :lexemes)]
    (println (str ":lang :" lang))
    (println (str ":subfamily \"" subfamily "\""))
    (println (str ":sgpref \"" sgpref "\""))
    (println (str ":datasource \"" dsource "\""))
    (if (pdgm-map :datasourceNotes )
        (println (str ":datasourceNotes \"" dsourcenotes "\"")))
    (println (str ":geodemoURL \"" webref "\""))
    (println (str ":geodemoTXT \"" desc "\""))
    (println (str ":schemata" (pformat1 schemata)))
    (println (str ":pclass " (pformat1 pclass )))
    (println (str ":morphemes"  (pformat2 morphemes)))
    (println (str ":lexemes" (pformat2 lexemes)))))


(defn findtype [common schema]
  (cond
   (common :morphClass)
   (str (common :morphClass))
   (common :pdgmType)
   (str (common :pdgmType))
   (and (= (common :pos) :Verb)
        (or (common :person)
            (.indexOf schema :person)))
   (str ":Finite")
   :else (str ":NA")))

(defn pformat3
  [psection]
  (clojure.string/replace psection #", :" ",\n             :"))

(defn pformat4
  [psection]
  (clojure.string/replace psection #"\] \[" "],\n            ["))

(defn do-lexterms
  [lexterms]
  (println ":lxterms [")
  (doseq [lexterm lexterms]
    (let [label (lexterm :label)
          terms (lexterm :terms)
          ;;because csv sparql req will be split by ","
          ;;note (str/replace (str (:note lexterm)) #"," "%%")
          note (lexterm :note)
          schema (first terms)
          data (next terms)
          common (lexterm :common)
          pdgmtype (findtype common schema) 
          ]
      (println (str "    {:label \"" label"\""))
      (if (:note lexterm)
        (println (str "    :note \"" note "\"")))
      (if (string?  pdgmtype)
        (println (str "    :pdgmType " pdgmtype)))
      (println (str "    :common " (pformat3 common)))
      (println (str "    :terms " (pformat4 terms)))
      (println "    }")
      (swap! pdgmTypeSet conj pdgmtype)
      ))
  (println "]"))

(defn do-muterms
  [muterms]
  (println ":muterms [")
  (doseq [muterm muterms]
    (let [label (:label muterm)
          terms (:terms muterm)
          ;;because csv sparql req will be split by ","
          ;;note (str/replace (str (:note muterm)) #"," "%%")
          note (:note muterm)
          schema (first terms)
          data (next terms)
          common (:common muterm)
          pdgmtype (findtype common schema) 
          ]
      (println (str "    {:label \"" label"\""))
      (if (:note muterm)
        (println (str "    :note \"" note "\"")))
      (if (string? pdgmtype)
        (println (str "    :pdgmType " pdgmtype)))
      (println (str "    :common " (pformat3 common)))
      (println (str "    :terms " (pformat4 terms)))
      (println "    }")
      (swap! pdgmTypeSet conj pdgmtype)
      ))
  (println "]")
  (println "}"))


  (defn -main
   "Calls the functions that inserts pdgmType prop and val into  the :lexterm maps of a pdgms.edn "
    [& file]

    (let [inputfile (first file)
          pdgmstring (slurp inputfile)
          pdgm-map (edn/read-string pdgmstring)
          lang (name (pdgm-map  :lang))
          sgpref (pdgm-map :sgpref)
          ]

;;      println (str "pdgm-map: " pdgm-map)
      (do-preliminary pdgm-map)
;;      (do-prelude inputfile pdgm-map)
;;      (do-props (pdgm-map :schemata) sgpref Lang)
;;      (do-pclass (pdgm-map :pclass) sgpref)
;;      (do-morphemes (pdgm-map :morphemes) sgpref Lang)
;;      (do-lexemes (pdgm-map :lexemes) sgpref Lang)
      (do-lexterms (pdgm-map :lxterms))
      (do-muterms (pdgm-map :muterms))
      (println " ")
      (println " ")
      (println  ":pdgmType [" (sorted-set pdgmTypeSet) "]")
      )
     )
