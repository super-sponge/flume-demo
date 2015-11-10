#! /usr/bin/env bash

PRG="${0}"

while [ -h "${PRG}" ]; do
  ls=`ls -ld "${PRG}"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "${PRG}"`/"$link"
  fi
done

BASEDIR=`dirname ${PRG}`
BASEDIR=`cd ${BASEDIR}/..;pwd`
SCRIPT=`basename ${PRG}`

export APP_JAR_DIR=$BASEDIR/lib

function add_to_classpath() {
  dir=$1
  for f in $dir/*.jar; do
    EXT_CLASSPATH=${EXT_CLASSPATH}:$f;
  done

  export APP_CLASSPATH
}

add_to_classpath ${APP_JAR_DIR}

export APP_CONF_DIR="${BASEDIR}/conf"
export APP_CLASSPATH=${EXT_CLASSPATH}

#java -cp ${APP_CLASSPATH} com.example.client.MyRpcClientFacade  -c ../conf/flume-sdk-send.conf  -f ../../../data/data.txt
java -cp ${APP_CLASSPATH} com.example.client.MyRpcClientFacade $*
