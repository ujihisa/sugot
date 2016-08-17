#!/bin/bash -ex
VERSION="1.10.2"
mkdir -p spigot
cd spigot
rm -f BuildTools.jar
wget https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar
java -Xms512m -Xmx1g -jar BuildTools.jar --rev "${VERSION}" > /dev/null
cd ..
lein deploy localrepo1 org.spigotmc/spigot "${VERSION}" "spigot/spigot-${VERSION}.jar"
lein test
