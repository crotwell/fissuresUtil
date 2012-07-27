#!/bin/sh

MAVEN=~/.maven/repository


SEEDCODEC=$MAVEN/SeedCodec/jars/SeedCodec-1.0Beta.jar
FISSURESUTIL=$MAVEN/fissuresUtil/jars/fissuresUtil-1.0.2beta.jar
FISSURESIMPL=$MAVEN/fissuresImpl/jars/fissuresImpl-1.1.1beta.jar
FISSURESIDL=$MAVEN/fissuresIDL/jars/fissuresIDL-1.0.jar
LOG4J=$MAVEN/log4j/jars/log4j-1.2.8.jar

# run with JacORB
#~/External/JacORB/bin/jaco -cp {LOG4J}:${SEEDCODEC}:${FISSURESIDL}:${FISSURESIMPL}:${FISSURESUTIL} edu.sc.seis.fissuresUtil.simple.MultiThreadNetworkClient -props ./alpha.prop


# run with built in java orb
java -cp ${LOG4J}:${SEEDCODEC}:${FISSURESIDL}:${FISSURESIMPL}:${FISSURESUTIL} edu.sc.seis.fissuresUtil.simple.MultiThreadNetworkClient -props ./alpha.prop


echo done.

