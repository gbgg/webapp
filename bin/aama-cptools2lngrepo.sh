#!/bin/bash
# usage:  ~/webapp/bin/cptools2langrepo.sh

# 03/21/14: 
# 09/23/15: to be added to upload when aama-edn2ttl.jar has been revised

echo "tools ********************************************"
echo "copying edn2ttl jar and source files to aama/tools"
cp bin/tools/edn2ttl/project.clj ../aama/tools/clj/edn2ttl/
cp bin/tools/edn2ttl/src/edn2ttl/core.clj ../aama/tools/clj/edn2ttl/src/edn2ttl/
cp bin/tools/edn2ttl/aama-edn2ttl.jar ../aama/tools/clj/
cd ../aama/tools
git add clj/aama-edn2ttl.jar
git add clj/edn2ttl/project.clj
git add clj/edn2ttl/src/*
git commit -am "revised edn2ttl jar and source files added to aama/tools"
git push origin master
cd ../../webapp
