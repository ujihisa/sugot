#!/bin/bash -ex
VERSION="1.9.4"
mkdir -p spigot
cd spigot
pwd
rm -f BuildTools.jar
wget https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar
java -Xms512m -Xmx1g -jar BuildTools.jar > /dev/null
cd ..
lein deploy localrepo1 org.spigotmc/spigot "${VERSION}" "spigot/spigot-${VERSION}.jar"
lein test
