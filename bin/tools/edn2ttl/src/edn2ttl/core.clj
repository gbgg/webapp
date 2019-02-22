(ns edn2ttl.core
  (require [clojure.edn :as edn]
           [stencil.core :as tmpl ])
  (:gen-class :main true))

;; 10/21/15:  provision for inclusion of :note  and :termcluster in ttl file.
;; 11/10/15: provision for inclusion of :pclass and its rdfs:label in ttl file.
;; println output directed to output-file by either:
;;   1) lein run [FILE].edn > [FILE].ttl {from edn2ttl directory}
;;   2) java -jar path/to/aama-edn2ttl.jar [FILE].edn > [FILE].ttl
;;   3) bin/aama-edn2rdf.sh ../aama-data/[LANG]/ {finds edn file and (re)places corresponding ttl file in same directory

(defn uuid
  "Generates random UUID for pdgm terms"
  []
  (str (java.util.UUID/randomUUID))
  )

(defn do-prelude
  [inputfile pdgm-map]
  (let [lang (name (pdgm-map  :lang))
        Lang (clojure.string/capitalize lang)
        subfamily (pdgm-map :subfamily)
        sgpref (pdgm-map :sgpref)
        dsource (pdgm-map :datasource)
        dsourcenotes (pdgm-map :datasourceNotes)
        webref (pdgm-map :geodemoURL)
        desc (pdgm-map :geodemoTXT)
        ;;because csv sparql req will be split by ","
        description (clojure.string/replace desc #"," "%%")
        ]
    (println
     (tmpl/render-string 
      (str "#TTL FROM INPUT FILE:\n#{{inputfile}}\n\n"
           "@prefix rdf:	 <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
           "@prefix rdfs:	 <http://www.w3.org/2000/01/rdf-schema#> .\n"
           "@prefix dc:    <http://purl.org/dc/elements>.\n"
           "@prefix dcterms:    <http://purl.org/dc/terms>.\n"
           "@prefix aama:	 <http://id.oi.uchicago.edu/aama/2013/> .\n"
           "@prefix aamas:	 <http://id.oi.uchicago.edu/aama/2013/schema/> .\n"
           "@prefix {{pfx}}:   <http://id.oi.uchicago.edu/aama/2013/{{lang}}/> .\n\n"
           "#LANG INFO:\n\n"
           "aama:{{Lang}} a aamas:Language .\n"
           "aama:{{Lang}} rdfs:label \"{{Lang}}\" .\n"
           "aama:{{Lang}} aamas:subfamily \"{{subfam}}\" .\n"
           "aama:{{Lang}} aamas:lpref \"{{pfx}}\" .\n"
           "aama:{{Lang}} aamas:dataSource \"{{dsource}}\" .\n"
           "aama:{{Lang}} aamas:dataSourceNotes \"{{dsourcenotes}}\" .\n"
           "aama:{{Lang}} aamas:geodemoURL \"{{webref}}\" .\n"
           "aama:{{Lang}} aamas:geodemoTXT \"{{desc}}\" .\n")
      {:pfx sgpref
       :dsource dsource
       :dsourcenotes dsourcenotes
       :webref webref
       :desc description
       :lang lang
       :Lang Lang
       :subfam subfamily
       :inputfile inputfile})
     )
    )
  )

(defn do-props
  "Complexity of this function owing to decision to distiguish pdgmType as pdgm property, vs. other token properties. Must be revisited!"
  [schemata sgpref Lang]
  (doseq [[property valuelist] schemata]
    (let [prop (name property)
          Prop (clojure.string/capitalize prop)]
      ;; NB clojure.string/capitalize gives  wrong output with
      ;; terms like conjClass: =>Conjclass rather than ConjClass
      (if (=  prop "pdgmType")
        (println
         (tmpl/render-string 
          (str
           (newline)
           "#SCHEMATA: {{prop}}\n"
           "aamas:{{prop}} aamas:lang aama:{{Lang}} .\n"
           "aamas:{{Prop}} aamas:lang aama:{{Lang}} .\n"
           "aamas:{{prop}} rdfs:domain aamas:Termcluster .\n"
           "aamas:{{Prop}} rdfs:label \"{{prop}} exponents\" .\n"
           "aamas:{{prop}} rdfs:label \"{{prop}}\" .\n"
           "aamas:{{prop}} rdfs:range aamas:{{Prop}} .\n"
           "aamas:{{Prop}} rdfs:subClassOf {{pfx}}:MuExponent .\n"
           "aamas:{{prop}} rdfs:subPropertyOf {{pfx}}:muProperty .")
          {:pfx sgpref
           :Lang Lang
           :prop prop
           :Prop Prop}))
        (println
         (tmpl/render-string 
          (str
           (newline)
           "#SCHEMATA: {{prop}}\n"
           "{{pfx}}:{{prop}} aamas:lang aama:{{Lang}} .\n"
           "{{pfx}}:{{Prop}} aamas:lang aama:{{Lang}} .\n"
           "{{pfx}}:{{prop}} rdfs:domain aamas:Term .\n"
           "{{pfx}}:{{Prop}} rdfs:label \"{{prop}} exponents\" .\n"
           "{{pfx}}:{{prop}} rdfs:label \"{{prop}}\" .\n"
           "{{pfx}}:{{prop}} rdfs:range {{pfx}}:{{Prop}} .\n"
           "{{pfx}}:{{Prop}} rdfs:subClassOf {{pfx}}:MuExponent .\n"
           "{{pfx}}:{{prop}} rdfs:subPropertyOf {{pfx}}:muProperty .")
          {:pfx sgpref
           :Lang Lang
           :prop prop
           :Prop Prop})))
      (doseq [value valuelist]
        (let [val (name value)]
          (if (= prop "pdgmType")
            (println
             (tmpl/render-string 
              (str
               "{{pfx}}:{{val}} aamas:lang aama:{{Lang}} .\n"
               "{{pfx}}:{{val}} rdf:type aamas:{{Prop}} .\n"
               "{{pfx}}:{{val}} rdfs:label \"{{val}}\" .")
              {:pfx sgpref
               :Lang Lang
               :Prop Prop
               :val val}))
            (println
             (tmpl/render-string 
              (str
               "{{pfx}}:{{val}} aamas:lang aama:{{Lang}} .\n"
               "{{pfx}}:{{val}} rdf:type {{pfx}}:{{Prop}} .\n"
               "{{pfx}}:{{val}} rdfs:label \"{{val}}\" .")
              {:pfx sgpref
               :Lang Lang
               :Prop Prop
               :val val}))))))))

(defn do-pclass
  [pclass sgpref]
  (println  "\n#PCLASSES:\n")
  (doseq [[propclass proplist] pclass]
    (let [prpclass (name propclass)]
      (doseq [prop proplist]
        (let [property (name prop)]
          (println
           (tmpl/render-string 
            (str
             "{{pfx}}:{{prop}} aamas:pclass aamas:{{prpclass}} .\n"
             "aamas:{{prpclass}} rdfs:label \"{{prpclass}}\" .")
            {:pfx sgpref
             :prop property
             :prpclass prpclass})))))))


(defn do-lexprops
  [lexprops sgpref Lang]
  (println  "\n#LEXEMES:")
  (doseq [property lexprops]
    (let [prop (name property)
          Prop (clojure.string/capitalize prop)]
      ;; NB clojure.string/capitalize gives  wrong output with
      ;; terms like conjClass: =>Conjclass rather than ConjClass
      (println
       (tmpl/render-string 
        (str
         (newline)
         "#LexSchema: {{prop}}\n"
         "aamas:{{prop}} aamas:lang aama:{{Lang}} .\n"
         "aamas:{{Prop}} aamas:lang aama:{{Lang}} .\n"
         "aamas:{{prop}} rdfs:domain aamas:Lexeme .\n"
         "aamas:{{Prop}} rdfs:label \"{{prop}} exponents\" .\n"
         "aamas:{{prop}} rdfs:label \"{{prop}}\" .\n"
         "aamas:{{prop}} rdfs:range aamas:{{Prop}} .\n"
         "aamas:{{Prop}} rdfs:subClassOf {{pfx}}:LexExponent .\n"
         "aamas:{{prop}} rdfs:subPropertyOf {{pfx}}:lexProperty .")
        {:pfx sgpref
         :Lang Lang
         :prop prop
         :Prop Prop})))))

(defn do-lexemes
  [lexemes sgpref Lang]
  (println "\n##LexItems")
  (doseq [[lexeme featurelist] lexemes]
    (let [lex (name lexeme)]
      (println
       (tmpl/render-string 
        (str
         "aama:{{Lang}}-{{lex}} a aamas:Lexeme ;\n" 
         "\taamas:lang aama:{{Lang}} ;\n" 
         "\trdfs:label \"{{lex}}\" ;")
        {:Lang Lang
         :lex lex})
       )
      )
    (doseq [[feature value] featurelist]
      (let [lprop (name feature)
            lval (name value)]
        (println
         (cond (= lprop "gloss")
               (tmpl/render-string 
                (str "\taamas:{{lprop}} \"{{lval}}\" ;") 
                {:lprop lprop :lval lval})
               (= lprop "lemma")
               (tmpl/render-string 
                (str "\taamas:{{lprop}} \"{{lval}}\" ;" )
                {:lprop lprop :lval lval})
               (re-find #"^token" lprop)
               (tmpl/render-string 
                (str "\t{{pfx}}:{{lprop}} \"{{lval}}\" ;"  )
                {:pfx sgpref :lprop lprop :lval lval})
               (re-find #"^note" lprop)
               (tmpl/render-string 
                (str "\t{{pfx}}:{{lprop}} \"{{lval}}\" ;" )
                {:pfx sgpref :lprop lprop :lval lval})
               :else
               (tmpl/render-string 
                (str "\t{{pfx}}:{{lprop}} {{pfx}}:{{lval}} ;" )
                {:pfx sgpref :lprop lprop :lval lval})
               )
         )
        )
      )
    (println "\t.")
    ) ;; (doseq [[lexeme featurelist] lexemes
  ) ;; (do-lexemes)

(defn do-termclusters
  [termclusters sgpref Lang]
  (doseq [termcluster termclusters]
    (let [label (:label termcluster)
          terms (:terms termcluster)
          ;;because csv sparql req will be split by ","
          note (clojure.string/replace (str (:note termcluster)) #"," "%%")
          ;;note (:note termcluster)
          schema (first terms)
          data (next terms)
          common (:common termcluster)]
      (println "\n#TERMCLUSTER: " label)
      (println
       (tmpl/render-string 
        (str (newline)
             "{{pfx}}:{{label}} a aamas:Termcluster ;\n"
             "\trdfs:label \"{{label}}\" ;\n"
             "\taamas:lang aama:{{Lang}} ;\n"
             "\trdfs:comment \"{{note}}\" \n"
             "\t.")
        {:pfx sgpref
         :label label
         :Lang Lang
         :note note}))
      ;; Need to build up string which can then be println-ed with each term of cluster
      (doseq [term data]
        (let [termid (uuid)]
          (println
           (tmpl/render-string 
            (str (newline)
                 "aama:ID{{uuid}} a aamas:Term ;\n"
                 "\taamas:lang aama:{{Lang}} ;\n"
                 "\taamas:memberOf {{pfx}}:{{label}} ;"
                 )
            {:Lang Lang
             :uuid termid
             :pfx sgpref
             :label label})
           )
          )
        (doseq [[feature value] common]
          (let [cprop (name feature)
                cval (clojure.string/replace (str (name value)) #"," "%%")]
            (println
             (cond (= cprop "lexeme")
                   (tmpl/render-string 
                    (str "\taamas:{{cprop}} aama:{{Lang}}-{{cval}} ;") 
                    {:cprop cprop :Lang Lang :cval cval})
                   ;;(re-find #"^\"" cval)
                   (re-find #"^token" cprop)
                   (tmpl/render-string 
                    (str "\t{{pfx}}:{{cprop}} \"{{cval}}\" ;")
                    {:pfx sgpref :cprop cprop :cval cval} )
                   ;;(re-find #"^note" cprop)
                   ;;(tmpl/render-string 
                   ;;(str "\t{{pfx}}:{{cprop}} \"{{cval}}\" ;")
                   ;;{:pfx sgpref :cprop cprop :cval cval} )
                   :else
                   (tmpl/render-string 
                    (str "\t{{pfx}}:{{cprop}} {{pfx}}:{{cval}} ;")
                    {:pfx sgpref :cprop cprop :cval cval} )
                   )
             )
            )
          )
        (let [termmap (apply assoc {} (interleave schema term))]
          (doseq [tpropval termmap]
            (let [tprop (name (key tpropval))
                  tval (clojure.string/replace (str (name (val tpropval))) #"," "%%")]
              (println
               (cond (re-find #"^\"" tval)
                     (tmpl/render-string 
                      (str "\t{{pfx}}:{{tprop}} \"{{tval}}\" ;" )
                      {:pfx sgpref :tprop tprop :tval tval})
                     ;;first condition does not seem to work, need following
                     (re-find #"^token" tprop)
                     (tmpl/render-string 
                      (str "\t{{pfx}}:{{tprop}} \"{{tval}}\" ;" )
                      {:pfx sgpref :tprop tprop :tval tval})
                     (= tprop "lexeme")
                     (tmpl/render-string 
                      (str "\taamas:{{tprop}} aama:{{Lang}}-{{tval}} ;")
                      {:tprop tprop :Lang Lang :tval tval})
                     :else
                     (tmpl/render-string 
                      (str "\t{{pfx}}:{{tprop}} {{pfx}}:{{tval}} ;")
                      {:pfx sgpref :tprop tprop :tval tval})
                     )
               )
              )
            )
          ) ;; (let [termmap apply assoc {}
        (println "\t.")
        ) ;;(doseq [term data]
      ) ;;(let [terms (:terms termcluster)
    ) ;;(doseq [termcluster lexterms]
  ) ;;(defn do-termclusters


(defn -main
  "Calls the functions that transform the keyed maps of a pdgms.edn to a pdgms.ttl"
  [& file]

  (let [inputfile (first file)
        pdgmstring (slurp inputfile)
        pdgm-map (edn/read-string pdgmstring)
        lang (name (pdgm-map  :lang))
        Lang (clojure.string/capitalize lang)
        sgpref (pdgm-map :sgpref)
        ]

    (do-prelude inputfile pdgm-map)

    (do-props (pdgm-map :schemata) sgpref Lang)

    (do-pclass (pdgm-map :pclass) sgpref)

    (do-lexprops (pdgm-map :lexprops)  sgpref Lang)

    (do-lexemes  (pdgm-map :lexemes) sgpref Lang)

    (do-termclusters (pdgm-map :termclusters) sgpref Lang)

    )
  )
