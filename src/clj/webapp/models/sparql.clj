(ns webapp.models.sparql
(:refer-clojure :exclude [filter concat group-by max min count])
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [capitalize split ]]

            [stencil.core :as tmpl]
            [clj-http.client :as http]
            [boutros.matsu.sparql :refer :all]
            [boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log])
  (:use [hiccup.page :only [html5]])
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

;; see notes/query-ext.clj for matsu and other formats


;;generate standard SPARQL query using stencil/render-string. 
;;In this version the "for [value values]" section does not
;;stiplulate that the property to which the value belongs
;;have an rdfs:label. See sparql/pdgmqry-sparql-alt for
;;older version.
(defn pdgmqry-sparql-fv [language lpref valstring]
    (let [values (split valstring #",")
          Language (capitalize language)
          ]
      (str
      (tmpl/render-string 
       (str "
	PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
	PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
	PREFIX aama: <http://id.oi.uchicago.edu/aama/2013/> 
	PREFIX aamas: <http://id.oi.uchicago.edu/aama/2013/schema/> 
	PREFIX aamag:	 <http://oi.uchicago.edu/aama/2013/graph/> 
	PREFIX {{lpref}}:   <http://id.oi.uchicago.edu/aama/2013/{{language}}/> 
	SELECT ?lex ?num ?pers ?gen ?token  
	WHERE
        { 
	 { 
	  GRAPH aamag:{{language}}  
          { 
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
         (str "
           ?s ?Q{{value}}  {{lpref}}:{{value}} .  ")
         {:value value
          :lpref lpref})))
      (tmpl/render-string
       (str " 
           OPTIONAL { ?s aamas:lexeme ?lex . }  
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

(defn pdgmqry-sparql-pro [language lpref valstring]
    (let [values (split valstring #"[:,]")
          proclass (first values)
          props (vec (rest values))
          propstring (clojure.string/replace valstring #"^.*?:" ",")
          qpropstring (clojure.string/replace propstring #"-|," {"-" "" "," " ?"})
          ;;qprops (clojure.string/replace propstring "-" "")]
          ;;qpropstring (clojure.string/replace qprops "," " ?")
          Language (capitalize language)
          ]
      (str
      (tmpl/render-string 
       (str "
	PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
	PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
	PREFIX aama: <http://id.oi.uchicago.edu/aama/2013/> 
	PREFIX aamas: <http://id.oi.uchicago.edu/aama/2013/schema/> 
	PREFIX aamag:	 <http://oi.uchicago.edu/aama/2013/graph/> 
	PREFIX {{lpref}}:   <http://id.oi.uchicago.edu/aama/2013/{{language}}/> 
	SELECT {{selection}}  ?token  
	WHERE
        { 
	 { 
	  GRAPH aamag:{{language}}  
          { 
	   ?s {{lpref}}:pos {{lpref}}:Pronoun .  
	   ?s aamas:lang aama:{{Language}} .
           ?s {{lpref}}:proClass {{lpref}}:{{proclass}} .
	   ?s aamas:lang ?lang . 
	   ?lang rdfs:label ?langLabel .  ")
       {:lpref lpref
        :language language
        :Language Language
        :selection qpropstring
        :proclass proclass})
      (apply str  
             (for [prop props]
               (let [qprop (clojure.string/replace prop "-" "")]
               (if (re-find #"token" prop)
                 (tmpl/render-string 
                  (str "
           OPTIONAL { ?s {{lpref}}:{{prop}} ?{{qprop}} . }")
                  {:lpref lpref
                   :prop prop
                   :qprop qprop})
                 (tmpl/render-string 
                  (str "
           OPTIONAL { ?s {{lpref}}:{{prop}} ?Q{{qprop}} .
                      ?Q{{qprop}} rdfs:label ?{{qprop}} . }") 
                  {:lpref lpref
                   :prop prop
                   :qprop qprop})
                 );;if
               );;let
               ))
      (tmpl/render-string
       (str " 
	   OPTIONAL { ?s {{lpref}}:number ?number .  
	   ?number rdfs:label ?num . } 
	   OPTIONAL {?s {{lpref}}:person ?person .  
	   ?person rdfs:label ?pers .  }
	   OPTIONAL { ?s {{lpref}}:gender ?gender .  
	   ?gender rdfs:label ?gen . } 
	   ?s {{lpref}}:token ?token .  
	  } 
	 } 
	} 
	ORDER BY {{selection}} DESC(?num) ?pers DESC(?gen) ")
       {:lpref lpref
        :selection qpropstring})
       );;str
))

(defn pdgmqry-sparql-nfv [language lpref valstring]
    (let [values (split valstring #"[:,]")
          morphclass (first values)
          props (vec (rest values))
          propstring (clojure.string/replace valstring #"^.*?:" ",")
          qpropstring (clojure.string/replace propstring #"-|," {"-" "" "," " ?"})
          ;;qprops (clojure.string/replace propstring "-" "")]
          ;;qpropstring (clojure.string/replace qprops "," " ?")
          Language (capitalize language)
          ]
      (str
      (tmpl/render-string 
       (str "
	PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
	PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
	PREFIX aama: <http://id.oi.uchicago.edu/aama/2013/> 
	PREFIX aamas: <http://id.oi.uchicago.edu/aama/2013/schema/> 
	PREFIX aamag:	 <http://oi.uchicago.edu/aama/2013/graph/> 
	PREFIX {{lpref}}:   <http://id.oi.uchicago.edu/aama/2013/{{language}}/> 
	SELECT {{selection}}  ?token  
	WHERE
        { 
	 { 
	  GRAPH aamag:{{language}}  
          { 
	   ?s {{lpref}}:pos {{lpref}}:Verb .  
           NOT EXISTS {?s {{lpref}}:person ?person } .
	   ?s aamas:lang aama:{{Language}} .
           ?s {{lpref}}:morphClass {{lpref}}:{{morphclass}} .
	   ?s aamas:lang ?lang . 
	   ?lang rdfs:label ?langLabel .  ")
       {:lpref lpref
        :language language
        :Language Language
        :selection qpropstring
        :morphclass morphclass})
      (apply str  
             (for [prop props]
               (let [qprop (clojure.string/replace prop "-" "")]
               (if (re-find #"token" prop)
                 (tmpl/render-string 
                  (str "
           OPTIONAL { ?s {{lpref}}:{{prop}} ?{{qprop}} . }")
                  {:lpref lpref
                   :prop prop
                   :qprop qprop})
                 (tmpl/render-string 
                  (str "
           OPTIONAL { ?s {{lpref}}:{{prop}} ?Q{{qprop}} .
                      ?Q{{qprop}} rdfs:label ?{{qprop}} . }") 
                  {:lpref lpref
                   :prop prop
                   :qprop qprop})
                 );;if
               );;let
               ))
      (tmpl/render-string
       (str " 
	   ?s {{lpref}}:token ?token .  
	  } 
	 } 
	} 
	ORDER BY {{selection}} ")
       {:lpref lpref
        :selection qpropstring})
       );;str
))

(defn pdgmqry-sparql-noun [language lpref valstring]
    (let [values (split valstring #"[:,]")
          morphclass (first values)
          props (vec (rest values))
          propstring (clojure.string/replace valstring #"^.*?:" ",")
          qpropstring (clojure.string/replace propstring #"-|," {"-" "" "," " ?"})
          ;;qprops (clojure.string/replace propstring "-" "")]
          ;;qpropstring (clojure.string/replace qprops "," " ?")
          Language (capitalize language)
          ]
      (str
      (tmpl/render-string 
       (str "
	PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
	PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
	PREFIX aama: <http://id.oi.uchicago.edu/aama/2013/> 
	PREFIX aamas: <http://id.oi.uchicago.edu/aama/2013/schema/> 
	PREFIX aamag:	 <http://oi.uchicago.edu/aama/2013/graph/> 
	PREFIX {{lpref}}:   <http://id.oi.uchicago.edu/aama/2013/{{language}}/> 
	SELECT {{selection}}  ?token  
	WHERE
        { 
	 { 
	  GRAPH aamag:{{language}}  
          { 
	   ?s {{lpref}}:pos {{lpref}}:Noun .  
	   ?s aamas:lang aama:{{Language}} .
           ?s {{lpref}}:morphClass {{lpref}}:{{morphclass}} .
	   ?s aamas:lang ?lang . 
	   ?lang rdfs:label ?langLabel .  ")
       {:lpref lpref
        :language language
        :Language Language
        :selection qpropstring
        :morphclass morphclass})
      (apply str  
             (for [prop props]
               (let [qprop (clojure.string/replace prop "-" "")]
               (if (re-find #"token" prop)
                 (tmpl/render-string 
                  (str "
           OPTIONAL { ?s {{lpref}}:{{prop}} ?{{qprop}} . }")
                  {:lpref lpref
                   :prop prop
                   :qprop qprop})
                 (tmpl/render-string 
                  (str "
           OPTIONAL { ?s {{lpref}}:{{prop}} ?Q{{qprop}} .
                      ?Q{{qprop}} rdfs:label ?{{qprop}} . }") 
                  {:lpref lpref
                   :prop prop
                   :qprop qprop})
                 );;if
               );;let
               ))
      (tmpl/render-string
       (str " 
	   OPTIONAL { ?s {{lpref}}:number ?number .  
	   ?number rdfs:label ?num . } 
	   OPTIONAL {?s {{lpref}}:person ?person .  
	   ?person rdfs:label ?pers .  }
	   OPTIONAL { ?s {{lpref}}:gender ?gender .  
	   ?gender rdfs:label ?gen . } 
	   ?s {{lpref}}:token ?token .  
	  } 
	 } 
	} 
	ORDER BY {{selection}} DESC(?num) ?pers DESC(?gen) ")
       {:lpref lpref
        :selection qpropstring})
       );;str
))

(defn lgpr-sparql [ldomain prop]
  (let [Ldomain (capitalize ldomain)]
  (str
   (tmpl/render-string 
    (str "
       prefix aama:	 <http://id.oi.uchicago.edu/aama/2013/>
       prefix aamas:	 <http://id.oi.uchicago.edu/aama/2013/schema/>
       prefix rdf:	 <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
       prefix rdfs:	 <http://www.w3.org/2000/01/rdf-schema#>

       SELECT DISTINCT ?valuelabel
       WHERE {
         GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{lang}}> {
          <http://id.oi.uchicago.edu/aama/2013/{{lang}}/{{type}}> rdfs:range ?Type .
          ?value rdf:type ?Type .
          ?value rdfs:label ?valuelabel .
          }
        }
       ORDER BY ?valuelabel  ")
    {:lang ldomain
     :type prop}))))

