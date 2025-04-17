#!/bin/sh
set -e
JAVA_OPTS_DEFAULT="${JAVA_OPTS_DEFAULT:=-Duser.country=US -Duser.language=en -Duser.timezone=UTC}"
JAVA_OPTS_ADDITIONAL="${JAVA_OPTS_ADDITIONAL:=}"
JAVA_OPTS="${JAVA_OPTS:=${JAVA_OPTS_DEFAULT} ${JAVA_OPTS_ADDITIONAL}}"
JAVA_ARGS="${JAVA_ARGS:=}"
exec java ${JAVA_OPTS} -jar sleep.jar ${JAVA_ARGS}
