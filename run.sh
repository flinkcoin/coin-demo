#!/bin/bash

JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64

export -p JAVA_HOME
java -Dio.netty.tryReflectionSetAccessible=false \
      --add-opens=java.base/java.nio=ALL-UNNAMED \
      --add-opens=java.base/jdk.internal.misc=ALL-UNNAMED \
      --add-opens=java.base/java.lang=ALL-UNNAMED \
      --add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
      -jar ./main/target/node-0.0.0.jar daemon

