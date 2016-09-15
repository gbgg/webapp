#!/bin/bash
# usage:  ~/aama/tools/bin/aama-pulldata.sh "dir"
# examples:


# 09/15/16

#. bin/constants.sh


fs=`find $1 -name "*.edn"`
for f in $fs
do
    l=${f%-pdgms.edn}
    lang=${l#data/*/}
    echo "$lang ********************************************"
    echo pulling data to aama/$lang
    cd $lang
    git pull
    git commit -am "most recent data pulled"
    cd ../
done
