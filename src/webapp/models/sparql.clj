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

;;(defn query-form [language lpref valstring]
;;  (let [values (split valstring #",")
;;        valsection (doseq [value values]
;;            (str ":s :Q" value " ["lpref" "value"] \\.")
;;            (str ":Q" value " [:rdfs :label] "value" \\."))]
;;    (pdgm-qry language lpref valstring)))

(defn pdgmquery [language lang valstring]
    ;;(str language " : " valstring)
    (let [values (split valstring #",")
          Language (capitalize language)
          ]
      ;;(println
      (tmpl/render-string 
       (str
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
        ;;  (println
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
        (order-by :lex (desc :num) :pers (desc :gen)))")
       {:lang lang})
      ;;)
))


;; matsu version
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


