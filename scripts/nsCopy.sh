#!/bin/bash


MAVEN=~/.maven/repository

JACO=~/External/JacORB-2.1/bin/jaco


SEEDCODEC=$MAVEN/SeedCodec/jars/SeedCodec-1.0Beta.jar
FISSURESUTIL=$MAVEN/fissuresUtil/jars/fissuresUtil-1.0.6beta.jar
FISSURESIMPL=$MAVEN/fissuresImpl/jars/fissuresImpl-1.1.4beta.jar
FISSURESIDL=$MAVEN/fissuresIDL/jars/fissuresIDL-1.0.jar
LOG4J=$MAVEN/log4j/jars/log4j-1.2.8.jar

JARS=${SEEDCODEC}:${FISSURESIDL}:${FISSURESIMPL}:${FISSURESUTIL}:${LOG4J}


$JACO \
-cp ${JARS} \
edu.sc.seis.fissuresUtil.namingService.NameServiceCopy -props nsCopy.prop

