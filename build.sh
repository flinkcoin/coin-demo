#!/bin/bash

JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64

export -p JAVA_HOME

mvn clean install -Pfat-jar
