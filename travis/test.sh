#!/bin/sh
mkdir -p spigot
cd spigot
pwd
wget https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar
java -jar BuildTools.jar
cd ..
ln -s spigot/spigot-1.8.7.jar .
expect travis/deploy.expect
lein test
