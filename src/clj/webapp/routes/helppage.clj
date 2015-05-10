(ns webapp.routes.helppage
 (:refer-clojure :exclude [filter concat group-by max min count])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [capitalize split]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.element :refer [link-to]]
            [hiccup.form :refer :all]))

(defn helppage []
  (layout/common 
   [:h1 "Help Page"]
   [:hr]
   [:h2 "Paradigms"]
    [:p "These pages experiment with different possibilities for display and comparison of paradigms. The comparisons for the moment are oriented to png-centered displays, and thus work reasonably well for finite verb and pronominal paradigms. Their application is less clear for non-finite verbs. Note that the present datastore contains " [:em "very "] "little material for nominal inflection."]
    [:ul
      [:li (link-to "/pdgm" "Individual Paradigms")
       [:ul [:li [:p "This query-type prompts for a \"paradigm-type\" (Finite Verb, Non-finite Verb, Pronoun, Noun) and a language; it then shows a drop-down select list of paradigms in that language of that type, and returns a table-formatted display of the selected paradigm."]]]]
      [:li (link-to "/pdgmcmp" "Paradigm Comparison")
       [:ul [:li [:p "This initial experiment with paradigm comparison prompts for a \"paradigm-type\" (Finite Verb, Non-finite Verb, Pronoun, Noun) and two language, it then shows two drop-down select lists of paradigms in those languages, and returns a csv-formatted display of the two paradigms. It then prompts for png-type (eventually will respond to specification of specific png-values), and returns a paradigm with parallel token display."]]]]
      [:li (link-to "/pdgmcheckbx"  "Checkbox: Monolingual Seq")
       [:ul [:li [:p "Use this option to pick a number of paradigms from a given language to be displayed in vertical succession."]]]]
      [:li (link-to "/pdgmcbpll"  "Checkbox: Monolingual Parallel")
       [:ul [:li [:p "Use this option to pick a number of paradigms from a given language to be displayed in vertical succession."]]]]
      [:li (link-to "/langcheckbx"  "Checkbox: Multilingual Dispaly")
       [:ul [:li [:p "Use this option to pick one or more  paradigms from a given language or set of languages to be displayed initially in vertical succession"]]]]
     ]
    [:hr]
    [:h2 "Property Value Displays"]
    [:p "These pages are designed to permit querying for arbitrary combinations of language, property, and value."]
    [:ul
     [:li (link-to "/pvlgpr" "Language-property")
      [:ul [:li [:p "This family of queries returns the values, if any, associated with a specified property in a specified language or group/family of languages."]]]]
     [:li (link-to "/pvlgvl" "Language-value")
       [:ul [:li [:p "This family of queries returns the properties, if any, associated with a specified value in a specified language or group/family of languages"]]]]
     [:li (link-to "/pvprvllg" "Language-property-value")
       [:ul [:li [:p "This family of queries accepts a language or group/family of languages and a comma-separated string of prop=val statements (in which case it returns the languages having that set of prop=val), combined optionally with one or more prop=?val statements (in which case it also returns the values of properties which may be associated with the specified properties)."]
   [:ul [:li "[For example the query \"person=Person2,gender=Fem\" with language group \"Beja\" returns the Beja languages which have 2f forms; while the query \"person=Person2,gender=Fem,pos=?pos,number=?number\" with \"Beja\" returns a table with the language(s) having 2f forms, along with the part-of-speech values, and number values associated with these forms.]"]]]]]]
    [:hr]
    [:h2 "Utilities"]
    [:div
     [:h3 "List Generation:"]
     [:p (link-to "/listlgpr"  "POS Properties")
      [:ul [:li "This set of queries lists, for one or more languages or language families in the datastore, the properties associated with the designated part of speech. "]]]
     [:p (link-to "/listvlcl" "POS Paradigm Value-Clusters")
      [:ul [:li [:p "This set of queries makes a list of the set of values ('Value Clusters', 'Paradigm Names') associated with each paradigm for a given language and part-of-speech. It then writes the list to a file pvlists/pname-POS-list-LANG.txt, where it is read-in to the various paradigm-selection menus."]
            [:p "(In the case of finite verbs these values are those shared by the default person-number-gender paradigms for pronouns and person-number-gender subject agreement paradigms for finite verbs. The relevant dimensions for noun and non-finite verb are less clear, and a suitable set of comparable dimensions remains to be worked out. Note that at present, noun paradigms are recorded only exceptionally in this archive.)"]]]]
     [:p (link-to "/listmenulpv" "Lists for drop-down menus")
      [:ul [:li [:p "This set of queries generates the cached datastore-wide lists of languages, properties, and values for use in drop-down menus. Needs to be applied whenever a new language is added to the datastore, or when property or value designations have been edited."]
            ]]]
     [:p (link-to "/listlpv" "Property-Value Indices by Language Domain")]
      [:ul [:li "This set of queries will generate for a given language, language-family, or set of languages, four tables with entries:"
       [:ol 
        [:li "lang prop: val, val, val, ..." [:br]
         "(all the vals for each prop in each lang)"]
        [:li "prop val: lang, lang, lang, ..." [:br]
         "(all the langs in which a given prop has a given val)"]
        [:li " val prop: lang, lang, lang, ... " [:br]
         "(all the langs in which a given val is associated with a given prop)"]
        [:li " prop lang: val, val, val, ..." [:br]
         "(all the vals associated with a given prop in a given language, set of languages, or language family)"]]
            [:p "These tables provide in effect a set of complete lang-prop-val indices for the language(s) in question. The script uses the lang-prop-val-list.template to generate for each lang a tsv/jason file which is essentially the schemata of the lang in question."]]]
     [:hr]
     [:h3 "Update:"]
     [:p "Procedures to update local and remote datastore after an edn file has been edited:"]
      [:ul [:li (link-to "/update" "Update Local Datastore")]
       [:li (link-to "/upload" "Upload to Remote Repository") " [Requires Access Privileges]"]]
      [:p "(NB: These two procedures have not yet been incorporated into the webapp. For the moment, only the command-line versions can be used.)"]
     [:hr]
     [:p "The following command-line versions presuppose that the edn data files are in the  ~/aama-data/data/[LANG} directories:"]
      [:ul [:li [:h4 "Datastore Update "] "Usage:" 
            [:ul 
             [:li "/bin/aama-datastore-update.sh ../aama-data/data/[LANGDOMAIN]  (from webapp dir)"]
             [:li " ~/aama-data/bin/aama-datastore-setup.sh \"data/*\" (to [re-]initiate the whole datastore from ~/aama-data dir)"]]]
       [:li [:h4 "Upload to aama/ language repository and push to origin."] "Usage:"
        [:ul 
         [:li "~/aama-data/bin/aama-cp2lngrepo.sh data/LANG (for a single language)"]
         [:li "~/aama-data/bin/aama-cp2lngrepo.sh \"data/*\" (to [re-]upload the whole datastore)"]]]]
       [:p "(Cf. the github aama-data/bin " (link-to "https://github.com/gbgg/aama-data/blob/master/bin/README.md" "README")")"]]))

(defroutes helppage-routes
  (GET "/helppage" [] (helppage)))


