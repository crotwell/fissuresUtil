#!/bin/sh

JAVA=java
LIB=./lib
PROP=nsCopy.prop

ISTI_UTIL=${LIB}/isti.util-1.0.1USC.jar
ISTI_UTIL_TOPLEVEL=${LIB}/isti.util.toplevel-1.0USC.jar
JDOM=${LIB}/jdom-b9.jar
JUNIT_ADDONS=${LIB}/junit-addons-1.3.jar
JAI_CORE=${LIB}/jai_core.jar
JAI_CODEC=${LIB}/jai_codec.jar
J3DCORE=${LIB}/j3dcore.jar
J3DUTILS=${LIB}/j3dutils.jar
VECMATH=${LIB}/vecmath.jar
HSQLDB=${LIB}/hsqldb-1.7.2-rc6d.jar
JAVAMAIL=${LIB}/javamail-1.3.1.jar
ACTIVATION=${LIB}/activation-1.0.2.jar
FISSURESIMPL=${LIB}/fissuresImpl-1.1.5beta.jar
FISSURESIDL=${LIB}/fissuresIDL-1.0.jar
SEEDCODEC=${LIB}/SeedCodec-1.0Beta2.jar
TAUP=${LIB}/TauP-1.1.4.jar
XERCES=${LIB}/xerces-2.4.0.jar
XML_APIS=${LIB}/xml-apis-1.0.b2.jar
XALAN=${LIB}/xalan-2.5.1.jar
LOG4J=${LIB}/log4j-1.2.6.jar
OPENMAP=${LIB}/openmap-4.6.jar
ITEXT=${LIB}/itext-0.99.jar
MOCKFISSURES=${LIB}/mockFissures-0.2.jar
JACORB=${LIB}/JacORB-2.1.jar
IDL=${LIB}/idl-2.1.jar
ANTLR=${LIB}/antlr-2.7.2.jar
AVALON_FRAMEWORK=${LIB}/avalon-framework-4.1.5.jar
CONCURRENT=${LIB}/concurrent-1.3.2.jar
LOGKIT=${LIB}/logkit-1.2.jar
JAX_QNAME=${LIB}/jax-qname-1.0.jar
JSR173_API=${LIB}/jsr173_api-1.0.jar
JSR173_RI=${LIB}/jsr173_ri-1.0.jar
NAMESPACE=${LIB}/namespace-1.0.jar
FISSURESUTIL=${LIB}/fissuresUtil-1.0.7beta.jar

CLASSPATH=${ISTI_UTIL}:${ISTI_UTIL_TOPLEVEL}:${JDOM}:${JUNIT_ADDONS}:${JAI_CORE}:${JAI_CODEC}:${J3DCORE}:${J3DUTILS}:${VECMATH}:${HSQLDB}:${JAVAMAIL}:${ACTIVATION}:${FISSURESIMPL}:${FISSURESIDL}:${SEEDCODEC}:${TAUP}:${XERCES}:${XML_APIS}:${XALAN}:${LOG4J}:${OPENMAP}:${ITEXT}:${MOCKFISSURES}:${JACORB}:${IDL}:${ANTLR}:${AVALON_FRAMEWORK}:${CONCURRENT}:${LOGKIT}:${JAX_QNAME}:${JSR173_API}:${JSR173_RI}:${NAMESPACE}:${FISSURESUTIL}


${JAVA} -Xmx128m \
-Djacorb.connection.client.pending_reply_timeout=120000 \
-Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton \
-Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB \
-Djava.endorsed.dirs=${LIB}/JacOrb/jars \
-cp ${CLASSPATH} edu.sc.seis.fissuresUtil.namingService.NameServiceCopy -props ${PROP} $*