#!/usr/bin/env bash
CURRENT_DIR=`pwd`
PROJECT_NAME="msg-ssdb2mysql"
TARGET_NAME=${CURRENT_DIR}/target
JAR_FILE=CURRENT_DIR/${PROJECT_NAME}-1.0-SNAPSHOT.jar
nohup java -jar ${JAR_FILE} --spring.profiles.active=dev \
> ${CURRENT_DIR}/msg_ssdb2mysql_$(date +%y%m%d%H).log &