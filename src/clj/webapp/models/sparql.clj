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

;; see notes/query-ext.clj for matsu and other formats
;; and for pdgmqry-sparql-alt

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
	SELECT ?num ?pers ?gen  ?token
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
	ORDER BY DESC(?num) ?pers DESC(?gen) ")
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

(defn pdgmqry-sparql-fv-cmp [language1 lpref1 valstring1 language2 lpref2 valstring2]
    (let [values (split valstring1 #",")
          Language1 (capitalize language1)
          ]
      (str
      (tmpl/render-string 
       (str "
	PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
	PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
	PREFIX aama: <http://id.oi.uchicago.edu/aama/2013/> 
	PREFIX aamas: <http://id.oi.uchicago.edu/aama/2013/schema/> 
	PREFIX aamag:	 <http://oi.uchicago.edu/aama/2013/graph/> 
        PREFIX {{lpref1}}:   <http://id.oi.uchicago.edu/aama/2013/{{language1}}/> 
	SELECT ?lex ?num ?pers ?gen ?token  
	WHERE
        { 
	 { 
	  GRAPH aamag:{{language1}}  
          { 
	   ?s {{lpref1}}:pos {{lpref1}}:Verb .  
	   ?s aamas:lang aama:{{Language1}} . 
	   ?s aamas:lang ?lang . 
	   ?lang rdfs:label ?langLabel .  ")
       {:lpref1 lpref1
        :language1 language1
        :Language1 Language1})
      (apply str  
             (for [value values]
        (tmpl/render-string 
         (str "
           ?s ?Q{{value}}  {{lpref1}}:{{value}} .  ")
         {:value value
          :lpref1 lpref1})))
      (tmpl/render-string
       (str " 
           OPTIONAL { ?s aamas:lexeme ?lex . }  
	   OPTIONAL { ?s {{lpref1}}:number ?number .  
	   ?number rdfs:label ?num . } 
	   {   ?s {{lpref1}}:pngShapeClass ?person .}  
	   UNION  
	   {   ?s {{lpref1}}:person ?person .}  
	   ?person rdfs:label ?pers .  
	   OPTIONAL { ?s {{lpref1}}:gender ?gender .  
	   ?gender rdfs:label ?gen . } 
	   ?s {{lpref1}}:token ?token .  
	  } 
	 } 
	} 
	ORDER BY ?lex DESC(?num) ?pers DESC(?gen) ")
       {:lpref1 lpref1})
       );;str
))

(defn pdgmqry-sparql-pro-cmp [language1 lpref1 valstring1 language2 lpref2 valstring2]
    (let [values (split valstring1 #"[:,]")
          proclass (first values)
          props (vec (rest values))
          propstring (clojure.string/replace valstring1 #"^.*?:" ",")
          qpropstring (clojure.string/replace propstring #"-|," {"-" "" "," " ?"})
          ;;qprops (clojure.string/replace propstring "-" "")]
          ;;qpropstring (clojure.string/replace qprops "," " ?")
          Language1 (capitalize language1)
          ]
      (str
      (tmpl/render-string 
       (str "
	PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
	PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
	PREFIX aama: <http://id.oi.uchicago.edu/aama/2013/> 
	PREFIX aamas: <http://id.oi.uchicago.edu/aama/2013/schema/> 
	PREFIX aamag:	 <http://oi.uchicago.edu/aama/2013/graph/> 
	PREFIX {{lpref1}}:   <http://id.oi.uchicago.edu/aama/2013/{{language1}}/> 
	SELECT {{selection}}  ?token  
	WHERE
        { 
	 { 
	  GRAPH aamag:{{language1}}  
          { 
	   ?s {{lpref1}}:pos {{lpref1}}:Pronoun .  
	   ?s aamas:lang aama:{{Language1}} .
           ?s {{lpref1}}:proClass {{lpref1}}:{{proclass}} .
	   ?s aamas:lang ?lang . 
	   ?lang rdfs:label ?langLabel .  ")
       {:lpref1 lpref1
        :language1 language1
        :Language1 Language1
        :selection qpropstring
        :proclass proclass})
      (apply str  
             (for [prop props]
               (let [qprop (clojure.string/replace prop "-" "")]
               (if (re-find #"token" prop)
                 (tmpl/render-string 
                  (str "
           OPTIONAL { ?s {{lpref1}}:{{prop}} ?{{qprop}} . }")
                  {:lpref1 lpref1
                   :prop prop
                   :qprop qprop})
                 (tmpl/render-string 
                  (str "
           OPTIONAL { ?s {{lpref1}}:{{prop}} ?Q{{qprop}} .
                      ?Q{{qprop}} rdfs:label ?{{qprop}} . }") 
                  {:lpref1 lpref1
                   :prop prop
                   :qprop qprop})
                 );;if
               );;let
               ))
      (tmpl/render-string
       (str " 
	   OPTIONAL { ?s {{lpref1}}:number ?number .  
	   ?number rdfs:label ?num . } 
	   OPTIONAL {?s {{lpref1}}:person ?person .  
	   ?person rdfs:label ?pers .  }
	   OPTIONAL { ?s {{lpref1}}:gender ?gender .  
	   ?gender rdfs:label ?gen . } 
	   ?s {{lpref1}}:token ?token .  
	  } 
	 } 
	} 
	ORDER BY {{selection}} DESC(?num) ?pers DESC(?gen) ")
       {:lpref1 lpref1
        :selection qpropstring})
       );;str
))

(defn pdgmqry-sparql-nfv-cmp [language1 lpref1 valstring1 language2 lpref2 valstring2]
    (let [values (split valstring1 #"[:,]")
          morphclass (first values)
          props (vec (rest values))
          propstring (clojure.string/replace valstring1 #"^.*?:" ",")
          qpropstring (clojure.string/replace propstring #"-|," {"-" "" "," " ?"})
          ;;qprops (clojure.string/replace propstring "-" "")]
          ;;qpropstring (clojure.string/replace qprops "," " ?")
          Language1 (capitalize language1)
          ]
      (str
      (tmpl/render-string 
       (str "
	PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
	PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
	PREFIX aama: <http://id.oi.uchicago.edu/aama/2013/> 
	PREFIX aamas: <http://id.oi.uchicago.edu/aama/2013/schema/> 
	PREFIX aamag:	 <http://oi.uchicago.edu/aama/2013/graph/> 
	PREFIX {{lpref1}}:   <http://id.oi.uchicago.edu/aama/2013/{{language1}}/> 
	SELECT {{selection}}  ?token  
	WHERE
        { 
	 { 
	  GRAPH aamag:{{language1}}  
          { 
	   ?s {{lpref1}}:pos {{lpref1}}:Verb .  
           NOT EXISTS {?s {{lpref1}}:person ?person } .
	   ?s aamas:lang aama:{{Language1}} .
           ?s {{lpref1}}:morphClass {{lpref1}}:{{morphclass}} .
	   ?s aamas:lang ?lang . 
	   ?lang rdfs:label ?langLabel .  ")
       {:lpref1 lpref1
        :language1 language1
        :Language1 Language1
        :selection qpropstring
        :morphclass morphclass})
      (apply str  
             (for [prop props]
               (let [qprop (clojure.string/replace prop "-" "")]
               (if (re-find #"token" prop)
                 (tmpl/render-string 
                  (str "
           OPTIONAL { ?s {{lpref1}}:{{prop}} ?{{qprop}} . }")
                  {:lpref1 lpref1
                   :prop prop
                   :qprop qprop})
                 (tmpl/render-string 
                  (str "
           OPTIONAL { ?s {{lpref1}}:{{prop}} ?Q{{qprop}} .
                      ?Q{{qprop}} rdfs:label ?{{qprop}} . }") 
                  {:lpref1 lpref1
                   :prop prop
                   :qprop qprop})
                 );;if
               );;let
               ))
      (tmpl/render-string
       (str " 
	   ?s {{lpref1}}:token ?token .  
	  } 
	 } 
	} 
	ORDER BY {{selection}} ")
       {:lpref1 lpref1
        :selection qpropstring})
       );;str
))

(defn pdgmqry-sparql-noun-cmp [language1 lpref1 valstring1 language2 lpref2 valstring2]
    (let [values (split valstring1 #"[:,]")
          morphclass (first values)
          props (vec (rest values))
          propstring (clojure.string/replace valstring1 #"^.*?:" ",")
          qpropstring (clojure.string/replace propstring #"-|," {"-" "" "," " ?"})
          ;;qprops (clojure.string/replace propstring "-" "")]
          ;;qpropstring (clojure.string/replace qprops "," " ?")
          Language1 (capitalize language1)
          ]
      (str
      (tmpl/render-string 
       (str "
	PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
	PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
	PREFIX aama: <http://id.oi.uchicago.edu/aama/2013/> 
	PREFIX aamas: <http://id.oi.uchicago.edu/aama/2013/schema/> 
	PREFIX aamag:	 <http://oi.uchicago.edu/aama/2013/graph/> 
	PREFIX {{lpref1}}:   <http://id.oi.uchicago.edu/aama/2013/{{language1}}/> 
	SELECT {{selection}}  ?token  
	WHERE
        { 
	 { 
	  GRAPH aamag:{{language1}}  
          { 
	   ?s {{lpref1}}:pos {{lpref1}}:Noun .  
	   ?s aamas:lang aama:{{Language1}} .
           ?s {{lpref1}}:morphClass {{lpref1}}:{{morphclass}} .
	   ?s aamas:lang ?lang . 
	   ?lang rdfs:label ?langLabel .  ")
       {:lpref1 lpref1
        :language1 language1
        :Language1 Language1
        :selection qpropstring
        :morphclass morphclass})
      (apply str  
             (for [prop props]
               (let [qprop (clojure.string/replace prop "-" "")]
               (if (re-find #"token" prop)
                 (tmpl/render-string 
                  (str "
           OPTIONAL { ?s {{lpref1}}:{{prop}} ?{{qprop}} . }")
                  {:lpref1 lpref1
                   :prop prop
                   :qprop qprop})
                 (tmpl/render-string 
                  (str "
           OPTIONAL { ?s {{lpref1}}:{{prop}} ?Q{{qprop}} .
                      ?Q{{qprop}} rdfs:label ?{{qprop}} . }") 
                  {:lpref1 lpref1
                   :prop prop
                   :qprop qprop})
                 );;if
               );;let
               ))
      (tmpl/render-string
       (str " 
	   OPTIONAL { ?s {{lpref1}}:number ?number .  
	   ?number rdfs:label ?num . } 
	   OPTIONAL {?s {{lpref1}}:person ?person .  
	   ?person rdfs:label ?pers .  }
	   OPTIONAL { ?s {{lpref1}}:gender ?gender .  
	   ?gender rdfs:label ?gen . } 
	   ?s {{lpref1}}:token ?token .  
	  } 
	 } 
	} 
	ORDER BY {{selection}} DESC(?num) ?pers DESC(?gen) ")
       {:lpref1 lpref1
        :selection qpropstring})
       );;str
))

(defn lgpr-sparql [ldomain prop]
  (let [ldoms (split ldomain #",")]
  (str
    (str "
       prefix aama:	 <http://id.oi.uchicago.edu/aama/2013/>
       prefix aamas:	 <http://id.oi.uchicago.edu/aama/2013/schema/>
       prefix rdf:	 <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
       prefix rdfs:	 <http://www.w3.org/2000/01/rdf-schema#>

       SELECT DISTINCT ?language ?valuelabel
       WHERE { ")
      (apply str  
             (for [ldom ldoms]
               (tmpl/render-string 
                  (str "
         {GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{lang}}> {
          <http://id.oi.uchicago.edu/aama/2013/{{lang}}/{{type}}> rdfs:range ?Type .
          ?value rdf:type ?Type .
          ?value rdfs:label ?valuelabel .
          ?value aamas:lang ?lang .
          ?lang rdfs:label ?language .
          }}  "
                       (if (not (= (last ldoms) ldom))
                         (str " 
          UNION")))
    {:lang ldom
     :type prop})))
      (str "}
       ORDER BY ?language ?valuelabel  "))))

(defn listlgpr-fv-sparql [language lpref]
  (tmpl/render-string
   (str "
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX aama: <http://oi.uchicago.edu/aama/schema/2010#>
PREFIX aamas:	 <http://id.oi.uchicago.edu/aama/2013/schema/>
PREFIX aamag:	 <http://oi.uchicago.edu/aama/2013/graph/>
PREFIX {{lpref}}: <http://id.oi.uchicago.edu/aama/2013/{{lang}}/>
SELECT DISTINCT  ?property
WHERE {
GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{lang}}> {
	?s ?p ?o ;
	{{lpref}}:pos  {{lpref}}:Verb .
    {?s {{lpref}}:pngShapeClass ?png .}
    	UNION
    {?s {{lpref}}:person ?person .}
   ?p rdfs:label ?property .
 	FILTER (?p NOT IN ( aamas:lang, {{lpref}}:gender, {{lpref}}:number, {{lpref}}:pngShapeClass, {{lpref}}:person, {{lpref}}:pos, {{lpref}}:token, rdf:type, {{lpref}}:multiLex ) )
}
}
ORDER BY ASC(?property) ")
{:lang language
 :lpref lpref}))

(defn listlpv-sparql2 [language]
  (tmpl/render-string
   (str "
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX aama: <http://oi.uchicago.edu/aama/schema/2010#>
PREFIX aamas:	 <http://id.oi.uchicago.edu/aama/2013/schema/>
PREFIX aamag:	 <http://oi.uchicago.edu/aama/2013/graph/>

SELECT DISTINCT  ?lang ?prop ?val
WHERE {
GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{language}}> {
	?s ?p ?o ;
	aamas:lang  ?language .
	?language rdfs:label ?lang .
   ?p rdfs:label ?prop .
   ?o rdfs:label ?val .
 	FILTER (?p NOT IN ( aamas:lang ) )
}}
ORDER BY ASC(?prop) ASC(?val)
 ")
{:language language}))

(defn listlpv-sparql [ldomain]
  (let [langs (split ldomain #",")]
    (str 
     (str "
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    PREFIX aama: <http://oi.uchicago.edu/aama/schema/2010#>
    PREFIX aamas:	 <http://id.oi.uchicago.edu/aama/2013/schema/>

    SELECT DISTINCT  ?lang ?prop ?val
    WHERE { ")
     (apply str
            (for [language langs]
              (tmpl/render-string
               (str "
    {GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{language}}> {
	?s ?p ?o ;
	aamas:lang  ?language .
	?language rdfs:label ?lang .
        ?p rdfs:label ?prop .
        ?o rdfs:label ?val .
 	FILTER (?p NOT IN ( aamas:lang ) )
     }} "
                    (if (not (= (last langs) language))
                      (str " 
          UNION")))
               {:language language})))
     (str "}
   ORDER BY ASC(?lang) ASC(?prop) ASC(?val)"))))

(defn listpvl-sparql [ldomain]
  (let [langs (split ldomain #",")]
    (str 
     (str "
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    PREFIX aama: <http://oi.uchicago.edu/aama/schema/2010#>
    PREFIX aamas:	 <http://id.oi.uchicago.edu/aama/2013/schema/>

    SELECT DISTINCT  ?prop ?val ?lang
    WHERE { ")
     (apply str
            (for [language langs]
              (tmpl/render-string
               (str "
    {GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{language}}> {
	?s ?p ?o ;
	aamas:lang  ?language .
	?language rdfs:label ?lang .
        ?p rdfs:label ?prop .
        ?o rdfs:label ?val .
 	FILTER (?p NOT IN ( aamas:lang ) )
     }} "
                    (if (not (= (last langs) language))
                      (str " 
          UNION")))
               {:language language})))
     (str "}
   ORDER BY  ASC(?prop) ASC(?val) ASC(?lang)"))))

(defn listvpl-sparql [ldomain]
  (let [langs (split ldomain #",")]
    (str 
     (str "
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    PREFIX aama: <http://oi.uchicago.edu/aama/schema/2010#>
    PREFIX aamas:	 <http://id.oi.uchicago.edu/aama/2013/schema/>

    SELECT DISTINCT  ?val ?prop ?lang
    WHERE { ")
     (apply str
            (for [language langs]
              (tmpl/render-string
               (str "
    {GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{language}}> {
	?s ?p ?o ;
	aamas:lang  ?language .
	?language rdfs:label ?lang .
        ?p rdfs:label ?prop .
        ?o rdfs:label ?val .
 	FILTER (?p NOT IN ( aamas:lang ) )
     }} "
                    (if (not (= (last langs) language))
                      (str " 
          UNION")))
               {:language language})))
     (str "}
   ORDER BY  ASC(?val) ASC(?prop) ASC(?lang)"))))

(defn listplv-sparql [ldomain]
  (let [langs (split ldomain #",")]
    (str 
     (str "
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    PREFIX aama: <http://oi.uchicago.edu/aama/schema/2010#>
    PREFIX aamas:	 <http://id.oi.uchicago.edu/aama/2013/schema/>

    SELECT DISTINCT  ?prop ?lang ?val
    WHERE { ")
     (apply str
            (for [language langs]
              (tmpl/render-string
               (str "
    {GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{language}}> {
	?s ?p ?o ;
	aamas:lang  ?language .
	?language rdfs:label ?lang .
        ?p rdfs:label ?prop .
        ?o rdfs:label ?val .
 	FILTER (?p NOT IN ( aamas:lang ) )
     }} "
                    (if (not (= (last langs) language))
                      (str " 
          UNION")))
               {:language language})))
     (str "}
   ORDER BY  ASC(?prop) ASC(?lang) ASC(?val)"))))

(defn listvlcl-fv-sparql [language lpref propstring]
  (let [qpropstring1 (clojure.string/replace propstring #"^.*?," "?")
        qpropstring2 (clojure.string/replace qpropstring1 #",$" "")
        selection (clojure.string/replace qpropstring2 #"," " ?")
        propstring2 (clojure.string/replace qpropstring2 #"^\?" "")
        proplist2 (split propstring2 #",")
        Language (capitalize language)]
    (str 
     (tmpl/render-string
      (str "
       PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
       PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
       PREFIX aama: <http://id.oi.uchicago.edu/aama/2013/>
       PREFIX aamas:	 <http://id.oi.uchicago.edu/aama/2013/schema/>
       PREFIX aamag:	 <http://oi.uchicago.edu/aama/2013/graph/>
       PREFIX {{lpref}}:   <http://id.oi.uchicago.edu/aama/2013/{{language}}/>
       SELECT DISTINCT ?langLabel {{selection}}
       WHERE{
         {   
          GRAPH aamag:{{language}} {
             ?s {{lpref}}:pos {{lpref}}:Verb . 
             {?s {{lpref}}:pngShapeClass ?png .} 
             UNION 
             {?s {{lpref}}:person ?person .} 
       ?s aamas:lang aama:{{Language}} .
       ?s aamas:lang ?lang .
       ?lang rdfs:label ?langLabel . ")
      {:language language
       :Language Language
       :lpref lpref
       :selection selection})
     (apply str
            (for [prop proplist2]
              (tmpl/render-string
               (str "
	OPTIONAL { ?s {{lpref}}:{{prop}} ?Q{{prop}} . 
	 ?Q{{prop}} rdfs:label ?{{prop}} . } ")
               {:prop prop
                :lpref lpref})))
            (tmpl/render-string 
             (str "}}}
       ORDER BY ?langLabel {{selection}}  ")
             {:selection selection})
     )))

(defn lgvl-sparql [ldomain lval]
  (let [ldoms (split ldomain #",")]
  (str
    (str "
       prefix aama:	 <http://id.oi.uchicago.edu/aama/2013/>
       prefix aamas:	 <http://id.oi.uchicago.edu/aama/2013/schema/>
       prefix rdf:	 <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
       prefix rdfs:	 <http://www.w3.org/2000/01/rdf-schema#>

       SELECT DISTINCT ?language ?predlabel
       WHERE { ")
      (apply str  
             (for [ldom ldoms]
               (tmpl/render-string 
                  (str "
         {GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{lang}}> {
          ?value rdfs:label \"{{type}}\" .
          ?value rdf:type ?predexp .
          ?pred rdfs:range ?predexp .
          ?pred rdfs:label ?predlabel .
          ?pred aamas:lang ?lang .
          ?lang rdfs:label ?language .
          }}  "
                       (if (not (= (last ldoms) ldom))
                         (str " 
          UNION")))
    {:lang ldom
     :type lval})))
      (str "}
       ORDER BY ?language ?predlabel  "))))

(defn prvllg-sparql [ldomain qstring]
  (let [ldoms (split ldomain #",")
        pvals (split qstring #",")
        selection (apply str
		      (for [pval pvals]
                        (if (re-find #"\?" pval)
                          (let [qpval (clojure.string/split pval #"=")
                                qval (clojure.string/replace (last qpval) #"-" "")]
                            (str qval "Label ")))))
        ]
  (str
               (tmpl/render-string 
                  (str "
       prefix aama:	 <http://id.oi.uchicago.edu/aama/2013/>
       prefix aamas:	 <http://id.oi.uchicago.edu/aama/2013/schema/>
       prefix rdf:	 <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
       prefix rdfs:	 <http://www.w3.org/2000/01/rdf-schema#>

       SELECT DISTINCT ?language {{selection}}
       WHERE { ")
                  {:selection selection})
               (apply str 
                      (for [ldom ldoms]
                        (str
                        (tmpl/render-string 
                  (str "
         {GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{lang}}> {
          ?s ?p ?o .")
                  {:lang ldom})
      (apply str  
             (for [pval pvals]
                 (let [selpval (split pval #"=")
                       selprop (first selpval)
                       selval (last selpval)
]
               (if (re-find #"\?" selval)
                 (let [qselval (clojure.string/replace selval "-" "")
                       qselvalLabel (str qselval "Label")]
                   (tmpl/render-string 
                  (str "
         ?s <http://id.oi.uchicago.edu/aama/2013/{{lang}}/{{selprop}}> 
                {{qselval}} .
           {{qselval}} rdfs:label {{qselvalLabel}} ." )
                  {:lang ldom
                   :selprop selprop
                   :qselval qselval
                   :qselvalLabel qselvalLabel}))
                   (tmpl/render-string 
                  (str "
         ?s <http://id.oi.uchicago.edu/aama/2013/{{lang}}/{{selprop}}>
	  <http://id.oi.uchicago.edu/aama/2013/{{lang}}/{{selval}}> . ")
                  {:lang ldom
                   :selprop selprop
                   :selval selval})))))
                  (str "
        ?s  aamas:lang ?lng .
        ?lng rdfs:label ?language .
          }}  "
                       (if (not (= (last ldoms) ldom))
                         (str " 
          UNION"))))))
                   (tmpl/render-string 
                    (str "}
       ORDER BY ?language {{selection}}  ")
    {:selection selection}))))

;;(if (.contains (last pvec) "?")
;;	    (str "Q" (first pvec) " " (last pvec))
;;	    (str "NQ" (first pvec) " " (last pvec)))))
