#!/bin/bash

JAVA_HOME=/usr/lib/jvm/adoptopenjdk-11-hotspot-amd64

export -p JAVA_HOME

mvn clean install -Pfat-jar
