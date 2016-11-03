(defproject pdgmtype "0.1.0-SNAPSHOT"
  :description "Adds pdgmType to all lxterms/muterms; mark all lxterms having :Verb and :person as ':pdgmType :Finite' and all terms with ':morphemeClass :[MORPHEMECLASS]' as ':pdgmType :[MORPHEMECLASS]'"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [stencil "0.3.4"]]
  :aot [pdgmtype.core]
  :main pdgmtype.core
  ;; :target-path "target/%s"
  ;; :profiles {:uberjar {:aot :all
  )
