#!/usr/bin/env bash

exec /gosu tl:tlgroup java -server ${JAVA_GC} -Xms${JAVA_MEM_LIMIT} -Xmx${JAVA_MEM_LIMIT} ${JAVA_OPTS} \
    -cp /app/resources:/app/classes:/app/libs/* \
	${MAIN_CLASS}
