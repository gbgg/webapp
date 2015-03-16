(ns webapp.routes.utilities
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


(def aama "http://localhost:3030/aama/query")

(defn utilities []
  (layout/common [:h1 "Utilities"]
                 [:hr]
    [:div
     [:h3 "List Generation:"]
     [:p (link-to "/listlgpr"  "POS Properties")
      [:ul [:li "This set of queries lists, for one or more languages or language families in the datastore, the properties associated with the designated part of speech. "
            ]]]
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

(defroutes utilities-routes
  (GET "/utilities" [] (utilities)))
