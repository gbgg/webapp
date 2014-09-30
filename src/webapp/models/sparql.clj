(ns webapp.models.sparql
(:refer-clojure :exclude [filter concat group-by max min count])
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [capitalize split]]

            [stencil.core :as tmpl]
            [clj-http.client :as http]
            [boutros.matsu.sparql :refer :all]
            [boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log])
  (:use [hiccup.page :only [html5]])
)

;; some common prefixes
(register-namespaces {:rdfs "<http://www.w3.org/2000/01/rdf-schema#>"
                      :aama "<http://id.oi.uchicago.edu/aama/2013/>"
                      :aamas "<http://id.oi.uchicago.edu/aama/2013/schema/>"
                      :aamag "<http://oi.uchicago.edu/aama/2013/graph/>"
                      :bar "<http://id.oi.uchicago.edu/aama/2013/beja-arteiga/>"})

;;canned query matsu-format. This works
(defquery aama-qry []
(select :lex :num :pers :gen :token)
(where (graph [:aamag :beja-arteiga]
        :s [:bar :pos] [:bar :Verb]  \.
	:s [:aamas :lang] [:aama :Beja-arteiga]  \.
	:s [:aamas :lang] :lang  \.
	:lang [:rdfs :label] :langLabel  \.
	:s :QPrefix [:bar :Prefix] \.
	:QPrefix [:rdfs :label] :Prefix  \.
	:s :QAffirmative [:bar :Affirmative] \.
	:QAffirmative [:rdfs :label] :Affirmative  \.
	:s :QCCY [:bar :CCY] \.
	:QCCY [:rdfs :label] :CCY  \.
	:s :QAorist [:bar :Aorist]  \.
	:QAorist [:rdfs :label] :Aorist  \.
	(optional :s [:aamas :lexeme] :lex)  \. 
	(optional :s [:bar :number] :number)  \.
	(optional :number [:rdfs :label] :num)  \. 
	(union (group :s [:bar :pngShapeClass] :person)  
	       (group :s [:bar :person] :person))  \.
	:person [:rdfs :label] :pers  \.
	(optional :s [:bar :gender] :gender)  \.
	(optional :gender [:rdfs :label] :gen)  \. 
	:s [:bar :token] :token  \.))
         (order-by :lex (desc :num) :pers (desc :gen)))

;;attempt to generate query in matsu-format using stencil/render-string. 
;;If println uncommented, prints correct query as side-effect,
;;But:
;; 1) will not return doseq section w/o println
;; 2) even if doseq worked, don't know how to turn this into 
;;    a working "(pdgm-qry)"
(defn pdgmqry-matsu [language lpref valstring]
    (let [values (split valstring #",")
          Language (capitalize language)
          ]
      (str
       ;;(println
      (tmpl/render-string 
       (str
        ;;"(defquery pdgm-qry []
        "(select :lex :num :pers :gen :token)
        (where (graph [:aamag :{{language}}]
        :s [:{{lpref}} :pos] [:{{lpref}} :Verb]  \\.
	:s [:aamas :lang] [:aama :{{Language}}]  \\.
	:s [:aamas :lang] :lang  \\.
	:lang [:rdfs :label] :langLabel  \\.")
       {:lang lang
        :language language
        :Language Language})
       ;;);;println
      (doseq [value values]
       ;;(println
        (tmpl/render-string 
         (str
          ":s :Q{{value}} [:{{lpref}} :{{value}}] \\.
          :Q{{value}} [:rdfs :label] :{{value}}  \\.")
         {:value value
          :lang lang})
        ;;);;println
       )
       (println
      (tmpl/render-string
       (str
	"(optional :s [:aamas :lexeme] :lex)  \\. 
	(optional :s [:{{lpref}} :number] :number)  \\.
	(optional :number [:rdfs :label] :num)  \\. 
	(union (group :s [:{{lpref}} :pngShapeClass] :person)  
	       (group :s [:{{lpref}} :person] :person))  \\.
	:person [:rdfs :label] :pers  \\.
	(optional :s [:{{lpref}} :gender] :gender)  \\.
	(optional :gender [:rdfs :label] :gen)  \\. 
	:s [:{{lpref}} :token] :token  \\.))
        (order-by :lex (desc :num) :pers (desc :gen)))")
       {:lang lang})
       );;println
      )
))

(defn pdgmquery [language lang valstring]
    (let [values (split valstring #",")
          Language (capitalize language)
          ]
      (str
      ;;(println
      (tmpl/render-string 
       (str
        ;;"(defquery pdgm-qry []
        "(select :lex :num :pers :gen :token)
        (where (graph [:aamag :{{language}}]
        :s [:{{lang}} :pos] [:{{lang}} :Verb]  \\.
	:s [:aamas :lang] [:aama :{{Language}}]  \\.
	:s [:aamas :lang] :lang  \\.
	:lang [:rdfs :label] :langLabel  \\.")
       {:lang lang
        :language language
        :Language Language})
        ;;)
        (doseq [value values]
       ;;(println
        (tmpl/render-string 
         (str
          ":s :Q{{value}} [:{{lang}} :{{value}}] \\.
          :Q{{value}} [:rdfs :label] :{{value}}  \\.")
         {:value value
          :lang lang}))
         ;;)
       ;;(println
      (tmpl/render-string
       (str
	"(optional :s [:aamas :lexeme] :lex)  \\. 
	(optional :s [:{{lang}} :number] :number)  \\.
	(optional :number [:rdfs :label] :num)  \\. 
	(union (group :s [:{{lang}} :pngShapeClass] :person)  
	       (group :s [:{{lang}} :person] :person))  \\.
	:person [:rdfs :label] :pers  \\.
	(optional :s [:{{lang}} :gender] :gender)  \\.
	(optional :gender [:rdfs :label] :gen)  \\. 
	:s [:{{lang}} :token] :token  \\.))
        (order-by :lex (desc :num) :pers (desc :gen))))")
       {:lang lang})
        ;;)
      )
))


;; Breaking up pdgmqry-sparql into pdgmgrq-sparqlABC
;; is inelegant, but works
(defn pdgmqry-sparqlA [language lpref]
    (let [;;values (split valstring #",")
          Language (capitalize language)
          ]
      (str
       ;;(println
      (tmpl/render-string 
       (str
	"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
	PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
	PREFIX aama: <http://id.oi.uchicago.edu/aama/2013/> 
	PREFIX aamas: <http://id.oi.uchicago.edu/aama/2013/schema/> 
	PREFIX aamag: <http://oi.uchicago.edu/aama/2013/graph/> 
	PREFIX {{lpref}}: <http://id.oi.uchicago.edu/aama/2013/{{language}}/> 
	SELECT ?lex ?num ?pers ?gen ?token  
	WHERE\n{ 
	 { 
	  GRAPH aamag:{{language}}\n  { 
	   ?s {{lpref}}:pos {{lpref}}:Verb .  
	   ?s aamas:lang aama:{{Language}} . 
	   ?s aamas:lang ?lang . 
	   ?lang rdfs:label ?langLabel .  ")
       {:lpref lpref
        :language language
        :Language Language})
      ;;);;println
      );;str
      )
)

(defn pdgmqry-sparqlB [lpref  valstring]
    (let [values (split valstring #",")]
        (for [value values]
        ;;(println
         (str
          "?s ?Q" value " " lpref ":" value " ."  
	  "?Q" value " rdfs:label ?" value " .  ")
         ;;);;println
         )
 ))

(defn pdgmqry-sparqlC [lpref]
  (str
        (tmpl/render-string
       (str
       "   OPTIONAL { ?s aamas:lexeme ?lex . }  
	   OPTIONAL { ?s {{lpref}}:number ?number .  
	   ?number rdfs:label ?num . } 
	   {   ?s {{lpref}}:pngShapeClass ?person .}  
	   UNION  
	   {   ?s {{lpref}}:person ?person .}  
	   ?person rdfs:label ?pers .  
	   OPTIONAL { ?s {{lpref}}:gender ?gender .  
	   ?gender rdfs:label ?gen . } 
	   ?s {{lpref}}:token ?token .  
	  } 
	 } 
	} 
	ORDER BY ?lex DESC(?num) ?pers DESC(?gen) ")
       {:lpref lpref})
        );;str
)


;;generate standard SPARQL query using stencil/render-string. 
;;This version contains in the "for [value values]" section
;;the triple "?Q{{value}} rdfs:label ?{{value}} .", which
;;essentially stipulates that the property to which the value
;;in question belongs has in fact an rdfs:label. At present
;;not certain why this seemed necessary at some point.
(defn pdgmqry-sparql-alt [language lpref valstring]
    (let [values (split valstring #",")
          Language (capitalize language)
          ]
      (str
      (tmpl/render-string 
       (str
	"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
	PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
	PREFIX aama: <http://id.oi.uchicago.edu/aama/2013/> 
	PREFIX aamas: <http://id.oi.uchicago.edu/aama/2013/schema/> 
	PREFIX aamag:	 <http://oi.uchicago.edu/aama/2013/graph/> 
	PREFIX {{lpref}}:   <http://id.oi.uchicago.edu/aama/2013/{{language}}/> 
	SELECT ?lex ?num ?pers ?gen ?token  
	WHERE\n{ 
	 { 
	  GRAPH aamag:{{language}}\n  { 
	   ?s {{lpref}}:pos {{lpref}}:Verb .  
	   ?s aamas:lang aama:{{Language}} . 
	   ?s aamas:lang ?lang . 
	   ?lang rdfs:label ?langLabel .  ")
       {:lpref lpref
        :language language
        :Language Language})
      (apply str  
             (for [value values]
        (tmpl/render-string 
         (str
          "?s ?Q{{value}}  {{lpref}}:{{value}} .  
	   ?Q{{value}} rdfs:label ?{{value}} .  
          ")
         {:value value
          :lpref lpref})
          )
      )
      (tmpl/render-string
       (str
       "   OPTIONAL { ?s aamas:lexeme ?lex . }  
	   OPTIONAL { ?s {{lpref}}:number ?number .  
	   ?number rdfs:label ?num . } 
	   {   ?s {{lpref}}:pngShapeClass ?person .}  
	   UNION  
	   {   ?s {{lpref}}:person ?person .}  
	   ?person rdfs:label ?pers .  
	   OPTIONAL { ?s {{lpref}}:gender ?gender .  
	   ?gender rdfs:label ?gen . } 
	   ?s {{lpref}}:token ?token .  
	  } 
	 } 
	} 
	ORDER BY ?lex DESC(?num) ?pers DESC(?gen) ")
       {:lpref lpref})
       );;str
))
