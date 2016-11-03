#!/bin/sh

# rev 10/27/16

# Script to run pdgmtype/core.clj to order to add pdgmType property to 
# edn files of pdgms in aama-data/data

cd bin/tools/pdgmtype

echo "Start:"

languages="egyptian-middle syriac"

#languages="akkadian-ob arabic burunge geez hebrew iraqw kambaata koorete maale oromo shinassha sidaama wolaytta yemsa burji coptic-sahidic dahalo hadiyya gawwada tsamakko yaaku dizi alaaba gedeo"

for lang in $languages
do
    echo "language: ${lang}"
    lein run ../../../../aama-data/data/${lang}/${lang}-pdgms.edn > ../../../notes/pdgmtype/${lang}-pdgms-type.edn
    echo "======================="
done
