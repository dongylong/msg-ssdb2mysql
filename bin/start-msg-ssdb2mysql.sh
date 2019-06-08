#!/usr/bin/env bash
CURRENT_DIR=`pwd`
echo "-- ${CURRENT_DIR} "
PROJECT_NAME="msg-ssdb2mysql"
TARGET_NAME=${CURRENT_DIR}/target
PROPERTIES = "--spring.profiles.active=dev"
JAR_FILE=${TARGET_NAME}/${PROJECT_NAME}-1.0-SNAPSHOT.jar
echo "--JAR_FILE: ${JAR_FILE} "
echo "-- java -jar ${JAR_FILE} ${PROPERTIES} > ${CURRENT_DIR}/msg_ssdb2mysql_$(date +%y%m%d%H).log &"
nohup java -jar ${JAR_FILE} ${PROPERTIES} > ${CURRENT_DIR}/msg_ssdb2mysql_$(date +%y%m%d%H).log &