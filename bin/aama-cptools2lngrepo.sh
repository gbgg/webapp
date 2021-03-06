#!/bin/bash
# usage:  ~/webapp/bin/aama-cptools2langrepo.sh

# 03/21/14: 
# 09/23/15: to be added to upload when aama-edn2ttl.jar has been revised

echo "tools ********************************************"
echo "Copying edn2ttl jar and source files to aama/tools"
echo "NB: Shell scripts for aama/tools/bin need to be compied by hand"
echo "    and adapted because of directory adjustments"
cp bin/tools/edn2ttl/project.clj ../aama/tools/clj/edn2ttl/
cp bin/tools/edn2ttl/src/edn2ttl/core.clj ../aama/tools/clj/edn2ttl/src/edn2ttl/
# cp bin/tools/ednsort/project.clj ../aama/tools/clj/ednsort/
# cp bin/tools/ednsort/src/ednsort/core.clj ../aama/tools/clj/ednsort/src/ednsort/
cp bin/tools/edn2ttl/aama-edn2ttl.jar ../aama/tools/clj/
cp bin/tools/edn2ttl/aama-edn2ttl.jar ../aama/jar/
# cp bin/*.sh ../aama/tools/bin
cd ../aama/tools
git add clj/aama-edn2ttl.jar
git add clj/edn2ttl/project.clj
git add clj/edn2ttl/src/*
# git add clj/ednsort/project.clj
# git add clj/ednsort/src/*
# git add bin/*.sh
git commit -am "revised edn2ttl added to aama/tools/clj and aama/jar"
git push origin master
cd ../../webapp
