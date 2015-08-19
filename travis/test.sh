#!/bin/bash -ex
mkdir -p spigot
cd spigot
pwd
wget https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar
java -Xms512m -Xmx1g -jar BuildTools.jar > /dev/null
cd ..
ln -s spigot/spigot-1.8.8.jar .
lein deploy localrepo1 org.spigotmc/spigot 1.8.8 spigot-1.8.8.jar
lein test
