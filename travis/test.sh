#!/bin/bash -ex
VERSION="1.8.8"
mkdir -p spigot
cd spigot
pwd
rm -f BuildTools.jar
wget https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar
java -Xms512m -Xmx1g -jar BuildTools.jar > /dev/null
cd ..
ln -s "spigot/spigot-${VERSION}.jar" .
lein deploy localrepo1 org.spigotmc/spigot "${VERSION}" "spigot-${VERSION}.jar"
lein test
