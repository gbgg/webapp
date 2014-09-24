(ns webapp.routes.home
 (:refer-clojure :exclude [filter concat group-by max min count])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [capitalize split]]
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

(defn show-pdgm [language labbrev valstring]
       (let [pquery (sparql/pdgmquery language labbrev valstring)]
         [:p pquery]))

;;(def values (split valstring #","))



(defquery pdgm-qry [language lpref valstring]
        (select :lex :num :pers :gen :token)
        (where (graph [:aamag language]
        :s [lpref :pos] [lpref :Verb]  \.
	:s [:aamas :lang] [:aama (capitalize language)]  \.
	:s [:aamas :lang] :lang  \.
	:lang [:rdfs :label] :langLabel  \.
        ;;(doseq [value values]
        ;;  :s (str ":Q" value) [lpref value] \.
        ;;  (str ":Q" value) [:rdfs :label] value  \.)
        (optional :s [:aamas :lexeme] :lex)  \. 
	(optional :s [lpref :number] :number)  \.
	(optional :number [:rdfs :label] :num)  \. 
	(union (group :s [lpref :pngShapeClass] :person)  
	       (group :s [lpref :person] :person))  \.
	:person [:rdfs :label] :pers  \.
	(optional :s [lpref :gender] :gender)  \.
	(optional :gender [:rdfs :label] :gen)  \. 
	:s [lpref :token] :token  \.))
        (order-by :lex (desc :num) :pers (desc :gen)))

(defn home []
  (layout/common [:h1 "PDGM Display"]
                 [:p "Welcome to PDGM Display"]
                 ;; [:p error]
                 [:hr]
                 (form-to [:post "/pdgm"]
                          [:p "Language:" (text-field "language")]
                          [:p "LangAbbrev:" (text-field "lpref")]
                          [:p "Value String:" (text-field "valstring")]
                           (submit-button "Get pdgm"))
                          [:hr]))

(defroutes home-routes
  (GET "/" [] (home))
  (POST "/pdgm" [language lpref valstring] 
        (pdgm-qry language lpref valstring)
       ;; send SPARQL over HTTP request
       (let [req (http/get aama
                           {:query-params
                            {"query" (pdgm-qry)
                             ;;"format" "application/sparql-results+json"}})]
                             "format" "text"}})]
         (log/info "sparql result status: " (:status req))
         (layout/common
          [:body
           [:h1 "Result"]
           [:pre (:body req)]]))))
