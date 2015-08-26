#!/bin/bash

spigot_version=1.8.8

if [ ! -f BuildTools.jar ]; then
    curl -o BuildTools.jar https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar
fi

if [ ! -f spigot-${spigot_version}.jar ]; then
    java -jar BuildTools.jar
fi

lein deploy localrepo1 org.spigotmc/spigot ${spigot_version} spigot-${spigot_version}.jar

echo 'Ready to run the server, you have to change eula.txt'
