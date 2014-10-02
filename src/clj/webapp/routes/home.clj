(ns webapp.routes.home
 (:refer-clojure :exclude [filter concat group-by max min count])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [capitalize split]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            [boutros.matsu.sparql :refer :all]
            [boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.form :refer :all]))

;; local aama sparql query endpoint
(def aama "http://localhost:3030/aama/query")

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
;;Yields correct query form, but not sure how to turn it into working
;;code.
;;[NB cf below pdgmqry-sparql on absence of: 
;;      ":Q{{value}} [:rdfs :label] :{{value}}  \\." 
;;from "for [value values] section.]
(defn pdgmqry-matsu [language lpref valstring]
    (let [values (split valstring #",")
          Language (capitalize language)
          ]
      (str
      (tmpl/render-string 
       (str
        "(defquery pdgm-qry []
         (select :lex :num :pers :gen :token)
        (where (graph [:aamag :{{language}}]
        :s [:{{lpref}} :pos] [:{{lpref}} :Verb]  \\.
	:s [:aamas :lang] [:aama :{{Language}}]  \\.
	:s [:aamas :lang] :lang  \\.
	:lang [:rdfs :label] :langLabel  \\.")
       {:lpref lpref
        :language language
        :Language Language})
      (apply str
      (for [value values]
        (tmpl/render-string 
         (str
          ":s :Q{{value}} [:{{lpref}} :{{value}}] \\.
          ")
         {:value value
          :lpref lpref})
       )
      )
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
       {:lpref lpref})
      )
))

;; canned query, sparql-format. This works
(def pdgm-qry 
"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX aama: <http://id.oi.uchicago.edu/aama/2013/>
PREFIX aamas: <http://id.oi.uchicago.edu/aama/2013/schema/>
PREFIX aamag: <http://oi.uchicago.edu/aama/2013/graph/>
PREFIX bar: <http://id.oi.uchicago.edu/aama/2013/beja-arteiga/>
SELECT ?lex ?num ?pers ?gen ?token
WHERE
{
{
GRAPH aamag:beja-arteiga
{
?s bar:pos bar:Verb .
?s aamas:lang aama:Beja-arteiga .
?s aamas:lang ?lang .
?lang rdfs:label ?langLabel .
?s ?QPrefix bar:Prefix.
?QPrefix rdfs:label ?Prefix .
?s ?QAffirmative bar:Affirmative.
?QAffirmative rdfs:label ?Affirmative .
?s ?QCCY bar:CCY.
?QCCY rdfs:label ?CCY .
?s ?QAorist bar:Aorist .
?QAorist rdfs:label ?Aorist .
OPTIONAL { ?s aamas:lexeme ?lex . }
OPTIONAL { ?s bar:number ?number .
?number rdfs:label ?num . }
{ ?s bar:pngShapeClass ?person .}
UNION
{ ?s bar:person ?person .}
?person rdfs:label ?pers .
OPTIONAL { ?s bar:gender ?gender .
?gender rdfs:label ?gen . }
?s bar:token ?token .
}
}
}
ORDER BY ?lex DESC(?num) ?pers DESC(?gen)")


;;generate standard SPARQL query using stencil/render-string. 
;;In this version the "for [value values]" section does not
;;stiplulate that the property to which the value belongs
;;have an rdfs:label. See sparql/pdgmqry-sparql-alt for
;;older version.
(defn pdgmqry-sparql [language lpref valstring]
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
          "?s ?Q{{value}}  {{lpref}}:{{value}} .  ")
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


(defn home 
  []
  (layout/common [:h1 "PDGM Display"]
                 [:p "Welcome to PDGM Display"]
                 ;; [:p error]
                 [:hr]
                 (form-to [:post "/pdgm"]
                          [:p "Language:" (text-field "language" "beja-arteiga")]
                          [:p "LangAbbrev:" (text-field "lpref" "bar")]
                          [:p "Value String:" (text-field "valstring" "Prefix,Affirmative,CCY,Aorist")]
                           (submit-button "Get pdgm"))
                          [:hr]))

(defroutes home-routes
  (GET "/" [] (home))
  (POST "/pdgm" [language lpref valstring] 
        (layout/common
        (def query-sparql (pdgmqry-sparql language lpref valstring))
        (def query-matsu (pdgmqry-matsu language lpref valstring))
        [:p "sparql = " query-sparql]
        [:p "matsu = " query-matsu])
        )
  (GET "/sparql" []
       ;;[language lpref valstring]
       ;; send SPARQL over HTTP request
       (let [req (http/get aama
                           {:query-params
                            ;;{"query" (aama-qry) ;;canned matsu
                            ;;{"query" query-matsu ;;generated matsu
                            ;;{"query" pdgm-qry ;;canned sparql
                            {"query" query-sparql ;;generated sparql
                             ;;"format" "application/sparql-results+json"}})]
                             "format" "text"}})]
         (log/info "sparql result status: " (:status req))
         (layout/common
          [:body
           [:h1 "Result with query-sparql"]
           [:pre (:body req)]
           [:p "sparql = " query-sparql]]))))
 
