/**
 * NameServiceCopy.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.fissuresUtil.namingService;

import org.omg.CORBA_2_3.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import edu.sc.seis.fissuresUtil.cache.NSEventDC;
import edu.sc.seis.fissuresUtil.cache.NSNetworkDC;
import edu.sc.seis.fissuresUtil.cache.NSPlottableDC;
import edu.sc.seis.fissuresUtil.cache.NSSeismogramDC;
import edu.sc.seis.fissuresUtil.simple.Initializer;

public class NameServiceCopy {

    private static void logErrExit(String msg) {
        System.err.println(msg);
        logger.error(msg);
        System.exit(1);
    }

    public static void main(String[] args) throws CannotProceed, InvalidName,
            NotFound {
        // this parse the args, reads properties, and inits the orb
        Initializer.init(args);
        boolean skipServerPing = false;
        for(int i = 0; i < args.length; i++) {
            if(args[i].equals("--noping")) {
                logger.info("Skipping server ping");
                skipServerPing = true;
            } else if(args[i].equals("-h") || args[i].equals("--help")) {
                System.out.println("-props propfile  Connfiguration properties");
                System.out.println("--noping         Do not ping servers before copy");
                System.out.println("-h --help        Print this help message");
                System.exit(0);
            }
        }
        String dnsToCopy = System.getProperty("nameServiceCopy.dns");
        if(dnsToCopy == null) {
            logErrExit("System property nameServiceCopy.dns must be set");
        }
        String copyToNS = System.getProperty("nameServiceCopy.copyTo");
        if(copyToNS == null) {
            logErrExit("System property nameServiceCopy.copyTo must be set");
        }
        logger.info("Copying all from " + dnsToCopy + " to " + copyToNS);
        FissuresNamingService fisNS = Initializer.getNS();
        if(fisNS.getNameService() == null) {
            logErrExit("Problem, ns is null");
        }
        fisNS.addOtherNameServiceCorbaLoc(copyToNS);
        ORB orb = Initializer.getORB();
        org.omg.CORBA.Object ncObj = orb.string_to_object(copyToNS);
        NamingContextExt copyToNameContext = NamingContextExtHelper.narrow(ncObj);
        NSSeismogramDC[] seisDC = fisNS.getAllSeismogramDC();
        logger.info("Got " + seisDC.length + " seis datacenters");
        for(int i = 0; i < seisDC.length; i++) {
            String dns = seisDC[i].getServerDNS();
            String name = seisDC[i].getServerName();
            if(dns.equals(dnsToCopy)) {
                if(skipServerPing
                        || !seisDC[i].getCorbaObject()._non_existent()) {
                    fisNS.rebind(dns,
                                 name,
                                 seisDC[i].getCorbaObject(),
                                 copyToNameContext);
                } else {
                    logger.info("Couldn't ping " + dns + " " + name
                            + ", skipping");
                }
            } else {
                logger.info("Doesn't match " + dns + ", " + name);
            }
        }
        NSNetworkDC[] networkDC = fisNS.getAllNetworkDC();
        logger.info("Got " + networkDC.length + " network datacenters");
        for(int i = 0; i < networkDC.length; i++) {
            String dns = networkDC[i].getServerDNS();
            String name = networkDC[i].getServerName();
            if(dns.equals(dnsToCopy)) {
                if(skipServerPing
                        || !networkDC[i].getCorbaObject()._non_existent()) {
                    logger.info("Rebind " + dns + ", " + name);
                    fisNS.rebind(dns,
                                 name,
                                 networkDC[i].getNetworkDC(),
                                 copyToNameContext);
                } else {
                    logger.info("Couldn't ping " + dns + " " + name
                            + ", skipping");
                }
            } else {
                logger.info("Doesn't match " + dns + ", " + name);
            }
        }
        NSEventDC[] eventDC = fisNS.getAllEventDC();
        logger.info("Got " + eventDC.length + " event datacenters");
        for(int i = 0; i < eventDC.length; i++) {
            String dns = eventDC[i].getServerDNS();
            String name = eventDC[i].getServerName();
            if(dns.equals(dnsToCopy)) {
                // only copy if can ping orginal
                if(skipServerPing
                        || !eventDC[i].getCorbaObject()._non_existent()) {
                    logger.info("Rebind " + dns + ", " + name);
                    fisNS.rebind(dns,
                                 name,
                                 eventDC[i].getCorbaObject(),
                                 copyToNameContext);
                } else {
                    logger.info("Couldn't ping " + dns + " " + name
                            + ", skipping");
                }
            } else {
                logger.info("Doesn't match " + dns + ", " + name);
            }
        }
        NSPlottableDC[] plotDC = fisNS.getAllPlottableDC();
        logger.info("Got " + plotDC.length + " seis datacenters");
        for(int i = 0; i < plotDC.length; i++) {
            String dns = plotDC[i].getServerDNS();
            String name = plotDC[i].getServerName();
            if(dns.equals(dnsToCopy)) {
                // only copy if can ping orginal
                if(skipServerPing
                        || !plotDC[i].getCorbaObject()._non_existent()) {
                    logger.info("Rebind " + dns + ", " + name);
                    fisNS.rebind(dns,
                                 name,
                                 plotDC[i].getPlottableDC(),
                                 copyToNameContext);
                } else {
                    logger.info("Couldn't ping " + dns + " " + name
                            + ", skipping");
                }
            } else {
                logger.info("Doesn't match " + dns + ", " + name);
            }
        }
        logger.info("Done");
    }

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(NameServiceCopy.class);
}