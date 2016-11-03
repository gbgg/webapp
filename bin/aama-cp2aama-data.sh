#!/bin/sh

# rev 10/27/16

# Script to copy LANG-pdgms-type.edn files to aama-data LANG-pdgms.edn 
# edn files of pdgms in aama-data/data


echo "Start:"

#languages="alaaba gedeo"

languages="akkadian-ob arabic burunge geez hebrew iraqw kambaata koorete maale oromo shinassha sidaama wolaytta yemsa burji coptic-sahidic dahalo hadiyya gawwada tsamakko yaaku dizi alaaba gedeo"

for lang in $languages
do
    echo "language: ${lang}"
    cp  notes/pdgmtype/${lang}-pdgms-type.edn ../aama-data/data/${lang}/${lang}-pdgms.edn
    echo "======================="
done
