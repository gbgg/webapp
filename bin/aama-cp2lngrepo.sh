#!/bin/bash
# usage:  ~/aama-data/bin/copy2langrepo.sh "dir"
# examples:
#    aama/$ tools/fupost "data/*" --  loads everything
#    aama/$ tools/fupost "data/alaaba" -- loads only alaaba
#    aama/$ tools/fupost "data/alaaba data/burji data/coptic" -- loads all 3 datasets
#    aama/$ tools/fupost "schema" -- loads all 3 datasets
# cumulative logfile written to logs/fupost.log


# 03/21/14: 
# 03/26/14: restricted to edn (xml now out of date)

#. bin/constants.sh


fs=`find $1 -name "*.edn"`
for f in $fs
do
    l=${f%-*.edn}
    lang=${l#../aama-data/data/*/}
    echo "$lang ********************************************"
    echo copying $f to aama/$lang
    cp ../aama-data/data/$lang/$lang-pdgms\.edn ../aama/$lang/
    cp ../aama-data/data/$lang/README.md ../aama/$lang/
    cp ../aama-data/data/$lang/$lang-pdgms\.ttl ../aama/$lang/
    cp ../aama-data/data/$lang/$lang-pdgms\.rdf ../aama/$lang/
    cp ../aama-data/data/$lang/$lang-cognates\.edn ../aama/$lang/
    cd ../aama/$lang
    git add *.edn
    # git rm *.ttl
    git add README.md
    git commit -am "aama lang repositories now have pdgm.ttl and .rdf to .edn files"
    git push origin master
    cd ../../webapp
done
