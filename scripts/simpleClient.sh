#!/bin/bash

MAVEN=/Users/crotwell/.maven/repository


# timeout in milliseconds, use large enough number to avoid thrashing the server
# here we use 30 seconds, but it should probalby be more like 900,000 (15 min)
JACORB_TIMEOUT=30000

JACORB_LIB=${MAVEN}/JacORB/jars
JACORB=${JACORB_LIB}/JacORB-2.1.jar
JACORB_ANTLR=${JACORB_LIB}/antlr-2.7.2.jar
JACORB_AVALON=${JACORB_LIB}/avalon-framework-4.1.5.jar
JACORB_CONCURRENT=${JACORB_LIB}/concurrent-1.3.2.jar
JACORB_LOGKIT=${JACORB_LIB}/logkit-1.2.jar

JACORBJARS=${JACORB}:${JACORB_CONCURRENT}:${JACORB_AVALON}:${JACORB_LOGKIT}


SEEDCODEC=$MAVEN/SeedCodec/jars/SeedCodec-1.0Beta.jar
FISSURESUTIL=$MAVEN/fissuresUtil/jars/fissuresUtil-1.0.6beta.jar
FISSURESIMPL=$MAVEN/fissuresImpl/jars/fissuresImpl-1.1.4beta.jar
FISSURESIDL=$MAVEN/fissuresIDL/jars/fissuresIDL-1.0.jar
LOG4J=$MAVEN/log4j/jars/log4j-1.2.8.jar

JARS=${JACORBJARS}:${SEEDCODEC}:${FISSURESIDL}:${FISSURESIMPL}:${FISSURESUTIL}:${LOG4J}

JACORBPARAMS="-Djava.endorsed.dirs=${JACORB_LIB} -Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB -Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton -Djacorb.connection.client.pending_reply_timeout=${JACORB_TIMEOUT} -classpath ${JARS} "

java ${JACORBPARAMS}  edu.sc.seis.fissuresUtil.simple.SimpleNetworkClient -props ./simpleClient.prop

java ${JACORBPARAMS} edu.sc.seis.fissuresUtil.simple.SimpleEventClient -props ./simpleClient.prop

java ${JACORBPARAMS}  edu.sc.seis.fissuresUtil.simple.SimpleSeismogramClient -props ./simpleClient.prop

echo done.

