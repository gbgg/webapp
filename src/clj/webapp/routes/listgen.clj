(ns webapp.routes.listgen
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

(defn listgen []
  (layout/common [:h1 "Utilities"]
                 [:hr]
    [:div
     [:h3 "List Generation:"]
     [:p (link-to "/listlgpr"  "POS Properties")
      [:ul [:li "This set of queries lists, for one or more languages or language families in the datastore, the properties associated with the designated part of speech. "
            ]]]
     [:p (link-to "/listvlcl" "POS Paradigm Value-Clusters")
      [:ul [:li "This set of queries makes a list of the set of values ('Value Clusters', 'Paradigm Names') associated with each paradigm for a given language and part-of-speech. In the case of finite verbs these are the default person-number-gender subject agreement paradigms -- person-number-gender-case typically for pronoun. The relevant dimensions for noun and non-finite verb are less clear, and a suitable set of comparable dimensions remains to be worked out. Note that at present, noun paradigms are recorded only exceptionally in this archive."
            ]]]
     [:p (link-to "/listlpv" "Property-Value Indices by Language Domain")
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
       "These tables provide in effect a set of complete lang-prop-val
    indices for the language(s) in question. The script uses the lang-prop-val-list.template
    to generate for each lang a tsv/jason file which is essentially the schemata
    of the lang in question."
            ]]]
     [:hr]
     [:h3 "Update:"]
     [:p "Update procedures are still in the process of being integrated into the current application. For the moment, note the following command-line versions:"
      [:ul [:li [:em "Datastore Update "] "after a data/LANG/LANG-pdgms.edn has been edited. Usage:" 
            [:ul 
             [:li "bin/aama-datastore-update.sh data/LANG (for a single language)"]
             [:li " bin/aama-datastore-setup.sh \"data/*\" (to [re-]initiate the whole datastore)"]]]
       [:li [:em "Upload "] "to aama/ language repository and push to origin. Usage:"
        [:ul [:li "bin/aama-cp2lngrepo.sh data/LANG (for a single language)"]
             [:li " bin/aama-datastore-setup.sh \"data/*\" (to [re-]upload the whole datastore)"]]]]
      "(cf. the github aama-data/bin " (link-to "https://github.com/gbgg/aama-data/blob/master/bin/README.md" "README")")"]]))

(defroutes listgen-routes
  (GET "/listgen" [] (listgen))
  ;;(POST "/pdgmqry" [language pos] (handle-pdgmqry language pos))
  ;;(POST "/pdgmdisplay" [language valstring] (handle-pdgmdisplay language valstring))
  )
