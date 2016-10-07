#!/bin/bash
# usage:  ~/webapp $ bin/copy2aama-data.sh "dir"
# for copying, e.g., README.md from aama/[LANG] to aama-data/data/[LANG]
# examples:
#    aama/$ tools/fupost "data/*" --  loads everything
#    aama/$ tools/fupost "data/alaaba" -- loads only alaaba
#    aama/$ tools/fupost "data/alaaba data/burji data/coptic" -- loads all 3 datasets
#    aama/$ tools/fupost "schema" -- loads all 3 datasets
# cumulative logfile written to logs/fupost.log


# 09/29/16: 
# 03/26/14: restricted to edn (xml now out of date)

#. bin/constants.sh


fs=`find $1 -name "*.edn"`
for f in $fs
do
    l=${f%-pdgms.edn}
    lang=${l#../aama-data/data/*/}
    echo "$lang ********************************************"
    echo copying README.md to aama-data/data/$lang
    cp ../aama/$lang/README.md ../aama-data/data/$lang/
done
cd ../aama-data
#git add *.md
git add data/*/README.md
git commit -am "aama-data lang repositories now have README.md"
git push origin master
cd ../webapp
