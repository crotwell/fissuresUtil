#!/bin/sh

MAVEN=~/.maven/repository


JACORB=$MAVEN/JacORB/jars/JacORB-1.4.1.jar
# for orbacus
OB=$MAVEN/OB/jars/OB-4.1.0.jar
OBNAMING=$MAVEN/OBNaming/jars/OBNaming-4.1.0.jar
# openorb
OPENORB=$MAVEN/OpenORB/jars/OpenORB-1.3.1.jar

HSQLDB=$MAVEN/hsqldb/jars/hsqldb-1.7.1.jar

SEEDCODEC=$MAVEN/SeedCodec/jars/SeedCodec-1.0Beta.jar
FISSURESUTIL=$MAVEN/fissuresUtil/jars/fissuresUtil-1.0Beta.jar
FISSURESIMPL=$MAVEN/fissuresImpl/jars/fissuresImpl-1.1Beta.jar
FISSURESIDL=$MAVEN/fissuresIDL/jars/fissuresIDL-1.0.jar
LOG4J=$MAVEN/log4j/jars/log4j-1.2.6.jar
TAUP=$MAVEN/TauP/jars/TauP-SNAPSHOT.jar
XALAN=$MAVEN/xalan/jars/xalan-2.4.1.jar
XERCES=$MAVEN/xerces/jars/xerces-2.0.2.jar
XMLAPI=$MAVEN/xml-apis/jars/xml-apis-1.0.b2.jar
JAICORE=$MAVEN/jars/jai_core.jar
JAICODEC=$MAVEN/jars/jai_codec.jar

java -cp ${HSQLDB}:${XMLAPI}:${XERCES}:${XALAN}:${TAUP}:${LOG4J}:${SEEDCODEC}:${FISSURESIDL}:${FISSURESIMPL}:${FISSURESUTIL}:${JAICORE}:${JAICODEC}:${CLASSPATH} edu.sc.seis.fissuresUtil.simple.SimpleNetworkClient -props ./alpha.prop


echo done.

