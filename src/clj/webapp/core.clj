(ns webapp.core
 (:refer-clojure :exclude [filter concat group-by max min count])
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clj-http.client :as http]
            [boutros.matsu.sparql :refer :all]
            [boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log])
  (:use [hiccup.page :only [html5]]
            ))

;; local aama sparql query endpoint
(def aama "http://localhost:3030/aama/query")

;; some common prefixes
(register-namespaces {:rdfs "<http://www.w3.org/2000/01/rdf-schema#>"
                      :aama "<http://id.oi.uchicago.edu/aama/2013/>"
                      :aamas "<http://id.oi.uchicago.edu/aama/2013/schema/>"
                      :aamag "<http://oi.uchicago.edu/aama/2013/graph/>"
                      :bar "<http://id.oi.uchicago.edu/aama/2013/beja-arteiga/>"})

;; Query generated by qstring-fv-pname2query.pl
;; from input: Beja-arteiga,Prefix,Affirmative,CCY,Aorist
;;PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
;;PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
;;PREFIX aama: <http://id.oi.uchicago.edu/aama/2013/>
;;PREFIX aamas: <http://id.oi.uchicago.edu/aama/2013/schema/>
;;PREFIX aamag: <http://oi.uchicago.edu/aama/2013/graph/>
;;PREFIX bar: <http://id.oi.uchicago.edu/aama/2013/beja-arteiga/>
;;SELECT ?lex ?num ?pers ?gen ?token
;;WHERE
;;{
;;   {
;;      GRAPH aamag:beja-arteiga
;;      {
;;	?s bar:pos bar:Verb .
;;	?s aamas:lang aama:Beja-arteiga .
;;	?s aamas:lang ?lang .
;;	?lang rdfs:label ?langLabel .
;;	?s ?QPrefix bar:Prefix.
;;	?QPrefix rdfs:label ?Prefix .
;;	?s ?QAffirmative bar:Affirmative.
;;	?QAffirmative rdfs:label ?Affirmative .
;;	?s ?QCCY bar:CCY.
;;	?QCCY rdfs:label ?CCY .
;;	?s ?QAorist bar:Aorist .
;;	?QAorist rdfs:label ?Aorist .
;;	OPTIONAL { ?s aamas:lexeme ?lex . }
;;	OPTIONAL { ?s bar:number ?number .
;;		 ?number rdfs:label ?num . }
;;	{ ?s bar:pngShapeClass ?person .}
;;	  UNION
;;	{ ?s bar:person ?person .}
;;	?person rdfs:label ?pers .
;;	OPTIONAL { ?s bar:gender ?gender .
;;		 ?gender rdfs:label ?gen . }
;;	?s bar:token ?token .
;;      }
;;   }
;;}
;;ORDER BY ?lex DESC(?num) ?pers DESC(?gen)

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

(defroutes app-routes
  (GET "/" 
       []
       ;; demo clojurescript
       (html5
        [:body
         [:p#clickable "Click me!"] ;; clickable: see core.cljs
         [:script {:src "js/goog/base.js" :type "text/javascript"}]
         [:script {:src "js/webapp.js" :type "text/javascript"}]
         [:script {:type "text/javascript"}
          "goog.require('webapp.core');"]]))
   (GET "/sparql"
       []
       ;; send SPARQL over HTTP request
       (let [req (http/get aama
                           {:query-params
                            {"query" (aama-qry)
                             ;;"format" "application/sparql-results+json"}})]
                             "format" "text"}})]
         (log/info "sparql result status: " (:status req))
         (html5
          [:body
           [:h1#clickable "Result"] ;; clickable: see core.cljs
           [:pre (:body req)]
           [:script {:src "js/goog/base.js" :type "text/javascript"}]
           [:script {:src "js/webapp.js" :type "text/javascript"}]
           [:script {:type "text/javascript"}
            "goog.require('webapp.core');"]])))

  (GET "/beja-arteiga2" 
       []
       ;; demo clojurescript
       (html5 
  [:body
   [:h2 "Property-Value Query: All Archive Properties (cooccur)"]
   [:h3
    [:a
     {:href "aama-form2query-langProps.xhtml"}
     "(For query over individual language property-value pairs)"]]
   [:h3
    "Some pointers about the use of the following table can be found in a preliminary "
    [:a
     {:href "aama-query-formation-guide.xhtml"}
     "Query Formation Guide"]
    "."]
   [:hr]
   [:form#oromo-beja-arteiga
    {:name "beja-arteiga",
     :method "post",
     :action "form2query-cooccur.php"}
    [:h2 [:label.label "Language: Beja"]]
    [:h2 [:label.label "Language Variant: Arteiga"]]
    [:table
     [:thead [:tr [:th [:h3 "Property"]] [:th [:h3 "Value"]]]]
     [:tbody
      [:tr
       [:td [:label.label {:for "augment"} "augment"]]
       [:td
        [:select#augment.required
         {:title "Choose a value.", :name "augment"}
         [:option {:value "~augment"} "-- Select Value --"]
         [:option {:value "?augment"} "?augment"]
         [:option {:value "intensive"} "intensive"]
         [:option {:value "simple"} "simple"]]]]
      [:tr
       [:td [:label.label {:for "conjClass"} "conjClass"]]
       [:td
        [:select#conjClass.required
         {:title "Choose a value.", :name "conjClass"}
         [:option {:value "~conjClass"} "-- Select Value --"]
         [:option {:value "?conjClass"} "?conjClass"]
         [:option {:value "prefix"} "prefix"]
         [:option {:value "suffix"} "suffix"]]]]
      [:tr
       [:td [:label.label {:for "conjType"} "conjType"]]
       [:td
        [:select#conjType.required
         {:title "Choose a value.", :name "conjType"}
         [:option {:value "~conjType"} "-- Select Value --"]
         [:option {:value "?conjType"} "?conjType"]
         [:option {:value "irregular"} "irregular"]]]]
      [:tr
       [:td [:label.label {:for "dervStem"} "dervStem"]]
       [:td
        [:select#dervStem.required
         {:title "Choose a value.", :name "dervStem"}
         [:option {:value "~dervStem"} "-- Select Value --"]
         [:option {:value "?dervStem"} "?dervStem"]
         [:option {:value "B"} "B"]
         [:option {:value "M"} "M"]
         [:option {:value "R"} "R"]
         [:option {:value "S"} "S"]
         [:option {:value "S2"} "S2"]
         [:option {:value "SM"} "SM"]
         [:option {:value "T"} "T"]]]]
      [:tr
       [:td [:label.label {:for "gender"} "gender"]]
       [:td
        [:select#gender.required
         {:title "Choose a value.", :name "gender"}
         [:option {:value "~gender"} "-- Select Value --"]
         [:option {:value "?gender"} "?gender"]
         [:option {:value "f"} "f"]
         [:option {:value "m"} "m"]]]]
      [:tr
       [:td [:label.label {:for "lang"} "lang"]]
       [:td
        [:select#lang.required
         {:title "Choose a value.", :name "lang"}
         [:option {:value "beja"} "beja"]]]]
      [:tr
       [:td [:label.label {:for "langVar"} "langVar"]]
       [:td
        [:select#langVar.required
         {:title "Choose a value.", :name "langVar"}
         [:option {:value "arteiga"} "arteiga"]]]]
      [:tr
       [:td [:label.label {:for "number"} "number"]]
       [:td
        [:select#number.required
         {:title "Choose a value.", :name "number"}
         [:option {:value "~number"} "-- Select Value --"]
         [:option {:value "?number"} "?number"]
         [:option {:value "pl"} "pl"]
         [:option {:value "sg"} "sg"]]]]
      [:tr
       [:td [:label.label {:for "participle"} "participle"]]
       [:td
        [:select#participle.required
         {:title "Choose a value.", :name "participle"}
         [:option {:value "~participle"} "-- Select Value --"]
         [:option {:value "?participle"} "?participle"]
         [:option {:value "conjunctive"} "conjunctive"]
         [:option {:value "future"} "future"]
         [:option {:value "negative"} "negative"]
         [:option {:value "past"} "past"]
         [:option {:value "present"} "present"]]]]
      [:tr
       [:td [:label.label {:for "person"} "person"]]
       [:td
        [:select#person.required
         {:title "Choose a value.", :name "person"}
         [:option {:value "~person"} "-- Select Value --"]
         [:option {:value "?person"} "?person"]
         [:option {:value "p1"} "p1"]
         [:option {:value "p2"} "p2"]
         [:option {:value "p3"} "p3"]]]]
      [:tr
       [:td [:label.label {:for "polarity"} "polarity"]]
       [:td
        [:select#polarity.required
         {:title "Choose a value.", :name "polarity"}
         [:option {:value "~polarity"} "-- Select Value --"]
         [:option {:value "?polarity"} "?polarity"]
         [:option {:value "affirmative"} "affirmative"]
         [:option {:value "negative"} "negative"]]]]
      [:tr
       [:td [:label.label {:for "rootClass"} "rootClass"]]
       [:td
        [:select#rootClass.required
         {:title "Choose a value.", :name "rootClass"}
         [:option {:value "~rootClass"} "-- Select Value --"]
         [:option {:value "?rootClass"} "?rootClass"]
         [:option {:value "CCC"} "CCC"]
         [:option {:value "CCY"} "CCY"]
         [:option {:value "CVC"} "CVC"]
         [:option {:value "invariable"} "invariable"]]]]
      [:tr
       [:td [:label.label {:for "tam"} "tam"]]
       [:td
        [:select#tam.required
         {:title "Choose a value.", :name "tam"}
         [:option {:value "~tam"} "-- Select Value --"]
         [:option {:value "?tam"} "?tam"]
         [:option {:value "aorist"} "aorist"]
         [:option {:value "future"} "future"]
         [:option {:value "imperative"} "imperative"]
         [:option {:value "jussive"} "jussive"]
         [:option {:value "optative"} "optative"]
         [:option {:value "past"} "past"]
         [:option {:value "present"} "present"]]]]
      [:tr
       [:td [:label.label {:for "tamStemClass"} "tamStemClass"]]
       [:td
        [:select#tamStemClass.required
         {:title "Choose a value.", :name "tamStemClass"}
         [:option {:value "~tamStemClass"} "-- Select Value --"]
         [:option {:value "?tamStemClass"} "?tamStemClass"]
         [:option {:value "aorClass"} "aorClass"]
         [:option {:value "modClass"} "modClass"]
         [:option {:value "negClass"} "negClass"]
         [:option {:value "partClass"} "partClass"]
         [:option {:value "pastClass"} "pastClass"]
         [:option {:value "presClass"} "presClass"]
         [:option {:value "presSgClass"} "presSgClass"]]]]
      [:tr
       [:td [:label.label {:for "tokenType"} "tokenType"]]
       [:td
        [:select#tokenType.required
         {:title "Choose a value.", :name "tokenType"}
         [:option {:value "~tokenType"} "-- Select Value --"]
         [:option {:value "?tokenType"} "?tokenType"]
         [:option {:value "affix"} "affix"]
         [:option {:value "compound"} "compound"]
         [:option {:value "stem"} "stem"]]]]
      [:tr
       [:td [:label.label {:for "verbClass"} "verbClass"]]
       [:td
        [:select#verbClass.required
         {:title "Choose a value.", :name "verbClass"}
         [:option {:value "~verbClass"} "-- Select Value --"]
         [:option {:value "?verbClass"} "?verbClass"]
         [:option {:value "copula"} "copula"]
         [:option {:value "participle"} "participle"]]]]]]
    [:div
     [:input#submit
      {:value "Submit", :name "submit", :type "submit"}]]]
   [:hr]]
  ))

  (GET "/oromo" 
       []
       ;; demo clojurescript
       (html5 
  [:body
   [:h2 "Property-Value Query: All Archive Properties (oromo)"]
   [:hr]
   [:form#oromo
    {:name "oromo", :method "post", :action "form2query2.php"}
    [:h2 [:label.label "Language: Oromo"]]
    [:div.labelBlock [:h3 "Simple or chained query:"]]
    [:div.indent
     [:input#oromo-show.required
      {:title "Please select an option",
       :value "show",
       :name "queryType",
       :type "radio"}]
     [:label {:for "oromo-show"} "Show Output"]
     [:input#oromo-chain
      {:value "chain", :name "queryType", :type "radio"}]
     [:label
      {:for "oromo-chain"}
      "Combine output with following query"]]
    [:div.labelBlock [:h3 "Output forms or paradigm labels:"]]
    [:div.indent
     [:input#oromo-forms.required
      {:title "Please select an option",
       :value "forms",
       :name "outputType",
       :type "radio"}]
     [:label {:for "forms"} "Output forms"]
     [:input#oromo-pdgms
      {:value "pdgms", :name "outputType", :type "radio"}]
     [:label {:for "pdgms"} "Output Paradigm Labels"]]
    [:div.labelBlock [:h3 "Choose:"]]
    [:table
     [:thead [:tr [:th "Property"] [:th "Value"]]]
     [:tbody
      [:tr
       [:td [:label.label {:for "oromo-clauseType"} "clauseType"]]
       [:td
        [:select#oromo-clauseType.required
         {:title "Choose a value.", :name "clauseType"}
         [:option {:value "?clauseType"} "-- Select Value --"]
         [:option {:value "main"} "main"]
         [:option {:value "subordinate"} "subordinate"]]]]
      [:tr
       [:td [:label.label {:for "oromo-dervStem"} "dervStem"]]
       [:td
        [:select#oromo-dervStem.required
         {:title "Choose a value.", :name "dervStem"}
         [:option {:value "?dervStem"} "-- Select Value --"]
         [:option {:value "base"} "base"]
         [:option {:value "causative"} "causative"]
         [:option {:value "middle"} "middle"]
         [:option {:value "passive"} "passive"]]]]
      [:tr
       [:td [:label.label {:for "oromo-gender"} "gender"]]
       [:td
        [:select#oromo-gender.required
         {:title "Choose a value.", :name "gender"}
         [:option {:value "?gender"} "-- Select Value --"]
         [:option {:value "f"} "f"]
         [:option {:value "m"} "m"]]]]
      [:tr
       [:td [:label.label {:for "oromo-lang"} "lang"]]
       [:td
        [:select#oromo-lang.required
         {:title "Choose a value.", :name "lang"}
         [:option {:value "?lang"} "-- Select Value --"]
         [:option {:value "oromo"} "oromo"]]]]
      [:tr
       [:td [:label.label {:for "oromo-number"} "number"]]
       [:td
        [:select#oromo-number.required
         {:title "Choose a value.", :name "number"}
         [:option {:value "?number"} "-- Select Value --"]
         [:option {:value "pl"} "pl"]
         [:option {:value "sg"} "sg"]]]]
      [:tr
       [:td [:label.label {:for "oromo-person"} "person"]]
       [:td
        [:select#oromo-person.required
         {:title "Choose a value.", :name "person"}
         [:option {:value "?person"} "-- Select Value --"]
         [:option {:value "p1"} "p1"]
         [:option {:value "p2"} "p2"]
         [:option {:value "p3"} "p3"]]]]
      [:tr
       [:td [:label.label {:for "oromo-polarity"} "polarity"]]
       [:td
        [:select#oromo-polarity.required
         {:title "Choose a value.", :name "polarity"}
         [:option {:value "?polarity"} "-- Select Value --"]
         [:option {:value "affirmative"} "affirmative"]
         [:option {:value "negative"} "negative"]]]]
      [:tr
       [:td [:label.label {:for "oromo-tam"} "tam"]]
       [:td
        [:select#oromo-tam.required
         {:title "Choose a value.", :name "tam"}
         [:option {:value "?tam"} "-- Select Value --"]
         [:option {:value "conditional"} "conditional"]
         [:option {:value "futureDefinite"} "futureDefinite"]
         [:option {:value "futureIndefinite"} "futureIndefinite"]
         [:option {:value "futurePerfect"} "futurePerfect"]
         [:option {:value "imperative"} "imperative"]
         [:option {:value "jussive"} "jussive"]
         [:option {:value "nonPast-nonPres"} "nonPast-nonPres"]
         [:option {:value "past"} "past"]
         [:option {:value "pastProgressive"} "pastProgressive"]
         [:option {:value "perfect"} "perfect"]
         [:option {:value "pluperfect"} "pluperfect"]
         [:option
          {:value "pluperfectProgressive"}
          "pluperfectProgressive"]
         [:option {:value "present"} "present"]
         [:option
          {:value "presentProgressive"}
          "presentProgressive"]]]]]]
    [:div
     [:input#oromo-submit
      {:value "Submit", :name "submit", :type "submit"}]]]]
        ))

  (GET "/guide" 
       []
       ;; demo clojurescript
       (html5 
  [:body
   [:h2 "Querying the Archive: An Introduction"]
   [:p
    "\n\t\t  The properties, values, and forms of the Afroasiatic Morphological Archive, as a linked (RDF) datastore, are  designed to be explored by the query language SPARQL 1.1. SPARQL queries can either be drawn up by hand, or through a user interface allowing graphic selection of the desired properties, values, and display parameters of the query, which is then assembled/generated automatically into canonical SPARQL. \n\t\t  "]
   [:p
    [:a
     {:href "aama-form2query-langProps.xhtml"}
     "AAMA Form-to-Query: Individual Language Properties"]
    " and "
    [:a
     {:href "aama-form2query-allArchiveProps.xhtml"}
     "AAMA Form-to-Query: All Archive Properties"]
    " are prototype implementations of this user interface. The following are some preliminary, non-systematic remarks on some aspects of the complex topic of AAMA datastore query-formation -- to be filled out as application documentation is developed.\n\t\t  "]
   [:h3 "Preliminary remarks on the Form-to-Query Interface"]
   [:ol
    [:li
     [:p
      " \n\t\t\tIn the following tables, the right-column drop-lists enable selection of combinations of properties and values of interest. A property preceded by \"?\" (e.g., ?polarity) stands for a variable over all values of the property in question. A property preceded by \"#\" will simply show that some value of the property in question is relevant to the tokens of interest. \n\t\t\t"]]
    [:li
     [:p
      "\n\t\t\tWith no further display parameters (left-column drop-lists) requested, the query will simply say whether any terms occur with the combination in question (more useful for archive-wide queries than for indiviudal languages). \"PNG Marking?\" = \"png\" will show a complete list of the tokens in question -- if they are finite verbs or pronouns; \"Token?\" = \"token\" will simply give the token without PNG marking. \"PDGM Name?\" = \"pdgm\" will give the default archive paradigm names where the combination in question occurs. Finally \"Cooccurring Prop-Vals?\" will show with what other properties (with or without values) the specified combination occurs.\n\t\t\t"]]
    [:li
     [:p
      " \n\t\t\tFor example, selecting \"lang=oromo\", \"polarity=?polarity\", and \"tam=present\" together with with \"PNG Marking?=png\" will yield all the PNG forms, affirmative and negative, for the Oromo present tense; selecting \"oromo\", \"present\", \"affirmative\", \"singular\" \"png\" will yield only the affirmative singular present forms. Similarly, selecting \"beja\", \"arteiga\", \"aorist\" \"png\" will yield all PNG forms for the Arteiga-Beja aorist; but selecting \"beja\", \"arteiga\", \"aorist\", \"negative\" \"png\" will yield will yield no forms, since the Arteiga-Beja aorist exists only in the affirmative, and no term is marked both \"aorist\" and \"negative\" -- a fact which could have been learned by asking for \"beja\", \"arteiga\", \"?polarity\", and \"?tam\" (without \"png\").\n\t\t\t"]]
    [:li
     [:p
      "\n\t\t\t A \"Combine output with following query\" option is being developed to get combined output for queries specifying more than one, but not all, of the values of a property: for example, both \"past\" and \"present\" values for the Oromo \"tam\" (\"tense-aspect-mode\") property. By definition, queries involving more than one language or language variety (i.e., more that one value of the \"lang\" or \"langVar\" property\") must be entered as chained queries: for example, a request for \"oromo\" \"present\" to be combined with \"beja\" \"arteiga\"\n\t\t\t \"present\".\n\t\t\t "]]]
   [:hr]
   [:h3 "Technical Note"]
   [:p
    [:em "Prototype Form-to-SPARQL-query generation."]
    " \n\t\tCurrent approach involves html forms with action=\"form-2-query-output.php\" (will switch to CPPredicateEditor and objective-j script as soon as feasible). The documentation/web directory contains archive copies of the php file which does the form2query transformation, form-2-query-output.php, and of the html pages: aama-query-formation-guide.xhtml,  aama-form2query-langProps.xhtml,  and aama-form2query-allArchive Props.xhtml. Running versions of course have to be kept in the (wamp) server [on my machine, c:/wamp/www/].\n\t\t"]
   [:p
    "\n\t\tGeneration and periodic updating of the property-value lists involve the following steps:\n\t\t"]
   [:ol
    [:li
     [:p
      "Generate html forms with property-value table for individual languages:\n\t\t"]
     [:ol
      [:li
       "\n\t\t\tUse SPARQL query tools/aamaTest/rq-ru/output-pvlist.rq to generate tsv file of property-value pairs in individual language:  [LANG]/[LANG]-pvlist.tsv\n\t\t\t"]
      [:li
       "\n\t\t\tUse script tools/pl/pdgmpvlisttsv2htmlForm.pl to transform [LANG]/[LANG]-pvlist.tsv  to an html form contained in documentation/web/[LANG]-pvlist.xhtml\n\t\t\t"]
      [:li
       "\n\t\t\tManually insert form from [LANG]-pvlist.xhtml into aama-form2query-langProps.xhtml\n\t\t\t"]]]
    [:li
     [:p
      "Generate html form with property-value table for datastore as a whole:\n\t\t"]
     [:ol
      [:li
       "\n\t\t\tUse SPARQL query tools/aamaTest/rq-ru/output-pvlist-all.rq to generate tsv file of property-value pairs in datastore as whole:  afroasiatic/afroasiatic-pvlist.tsv\n\t\t\t"]
      [:li
       "\n\t\t\tUse script tools/pl/pdgmpvlisttsv2htmlForm.pl to transform afroasiatic/afroasiatic-pvlist.tsv  to an html form contained in documentation/web/afroasiatic-pvlist.xhtml\n\t\t\t"]
      [:li
       "\n\t\t\tManually insert form from afroasaitic-pvlist.xhtml into aama-form2query-allArchiveProps.xhtml\n\t\t\t"]]]]]
  ))
   (route/resources "/")
  (route/not-found "Not Found"))


(def app
  (handler/site app-routes))
