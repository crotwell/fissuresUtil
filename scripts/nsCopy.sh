#!/bin/bash


MAVEN=~/.maven/repository

JACORB_LIB=$MAVEN/JacORB/jars
JACORB=$JACORB_LIB/JacORB-2.1.jar
JACORB_ANTLR=$JACORB_LIB/antlr-2.7.2.jar
JACORB_AVALON=$JACORB_LIB/avalon-framework-4.1.5.jar
JACORB_CONCURRENT=$JACORB_LIB/concurrent-1.3.2.jar
JACORB_LOGKIT=$JACORB_LIB/logkit-1.2.jar

# timeout in milliseconds, use large enough number to avoid thrashing the server
JACORB_TIMEOUT=30000


SEEDCODEC=$MAVEN/SeedCodec/jars/SeedCodec-1.0Beta.jar
FISSURESUTIL=$MAVEN/fissuresUtil/jars/fissuresUtil-1.0.6beta.jar
FISSURESIMPL=$MAVEN/fissuresImpl/jars/fissuresImpl-1.1.4beta.jar
FISSURESIDL=$MAVEN/fissuresIDL/jars/fissuresIDL-1.0.jar
LOG4J=$MAVEN/log4j/jars/log4j-1.2.8.jar

JARS=${JACORB}:${JACORB_ANTLR}:${JACORB_AVALON}:${JACORB_CONCURRENT}:${JACORB_LOGKIT}:${SEEDCODEC}:${FISSURESIDL}:${FISSURESIMPL}:${FISSURESUTIL}:${LOG4J}

java -Djava.endorsed.dirs=${JACORB_LIB}  \
    -Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB \
    -Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton \
    -Djacorb.connection.client.pending_reply_timeout=${JACORB_TIMEOUT} \
    -cp ${JARS} \
    edu.sc.seis.fissuresUtil.namingService.NameServiceCopy -props nsCopy.prop

