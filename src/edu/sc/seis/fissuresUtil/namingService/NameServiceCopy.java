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
import edu.sc.seis.fissuresUtil.cache.ServerNameDNS;
import edu.sc.seis.fissuresUtil.simple.Initializer;

public class NameServiceCopy {

    private static void logErrExit(String msg) {
        System.err.println(msg);
        logger.error(msg);
        System.exit(1);
    }

    private static void copy(ServerNameDNS[] from, ServerNameDNS[] to)
            throws NotFound, CannotProceed, InvalidName {
        for(int i = 0; i < from.length; i++) {
            String dns = from[i].getServerDNS();
            String name = from[i].getServerName();
            logger.info("Checking on" + name);
            if(skipServerPing || !from[i].getCorbaObject()._non_existent()) {
                boolean rebind = true;
                org.omg.CORBA.Object fromObj = from[i].getCorbaObject();
                for(int j = 0; j < to.length; j++) {
                    if(to[j].getServerName().equals(name)) {
                        logger.info("Copy to name service contains " + name
                                + " as well");
                        try {
                            org.omg.CORBA.Object toObj = to[j].getCorbaObject();
                            if(!toObj._non_existent() && fromObj.equals(toObj)) {
                                logger.info("Copy to name service copy of "
                                        + name
                                        + " is the same as the one in copy from name service");
                                rebind = false;
                            }
                        } catch(RuntimeException e) {
                            //This exception came from the copy to obj, rebind
                        }
                        break;//Only one match per name possible
                    }
                }
                if(rebind) {
                    logger.info("Rebinding " + name);
                    copyFromNS.rebind(dns, name, fromObj, copyToNameContext);
                }
            } else {
                logger.info("Couldn't ping " + dns + " " + name + ", skipping");
            }
        }
    }

    private static void copyEventDC(NamingContextWithPath context)
            throws NotFound, CannotProceed, InvalidName {
        NSEventDC[] copyFromPlotDC = copyFromNS.getAllEventDC(context);
        NSEventDC[] copyToPlotDC = copyToNS.getAllEventDC(context);
        logger.info("Got " + copyFromPlotDC.length + " event datacenters");
        copy(copyFromPlotDC, copyToPlotDC);
    }

    private static void copyPlotDC(NamingContextWithPath context)
            throws NotFound, CannotProceed, InvalidName {
        NSPlottableDC[] copyFromPlotDC = copyFromNS.getAllPlottableDC(context);
        NSPlottableDC[] copyToPlotDC = copyToNS.getAllPlottableDC(context);
        logger.info("Got " + copyFromPlotDC.length + " plot datacenters");
        copy(copyFromPlotDC, copyToPlotDC);
    }

    private static void copyNetDC(NamingContextWithPath context)
            throws NotFound, CannotProceed, InvalidName {
        NSNetworkDC[] copyFromNetDC = copyFromNS.getAllNetworkDC(context);
        NSNetworkDC[] copyToNetDC = copyToNS.getAllNetworkDC(context);
        logger.info("Got " + copyToNetDC.length + " net datacenters");
        copy(copyFromNetDC, copyToNetDC);
    }

    private static void copySeisDC(NamingContextWithPath context)
            throws NotFound, CannotProceed, InvalidName {
        NSSeismogramDC[] copyFromSeisDC = copyFromNS.getAllSeismogramDC(context);
        NSSeismogramDC[] copyToSeisDC = copyToNS.getAllSeismogramDC(context);
        logger.info("Got " + copyFromSeisDC.length + " seis datacenters");
        copy(copyFromSeisDC, copyToSeisDC);
    }

    private static boolean skipServerPing = false;

    private static FissuresNamingService copyFromNS, copyToNS;

    private static NamingContextExt copyToNameContext;

    public static void main(String[] args) throws CannotProceed, InvalidName,
            NotFound {
        // this parse the args, reads properties, and inits the orb
        Initializer.init(args);
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
        String copyToNSLoc = System.getProperty("nameServiceCopy.copyTo");
        if(copyToNSLoc == null) {
            logErrExit("System property nameServiceCopy.copyTo must be set");
        }
        logger.info("Copying all from " + dnsToCopy + " to " + copyToNSLoc);
        copyFromNS = Initializer.getNS();
        if(copyFromNS.getNameService() == null) {
            logErrExit("Copy from ns is null!");
        }
        copyFromNS.addOtherNameServiceCorbaLoc(copyToNSLoc);
        ORB orb = Initializer.getORB();
        copyToNS = new FissuresNamingService(orb);
        copyToNS.setNameServiceCorbaLoc(copyToNSLoc);
        org.omg.CORBA.Object ncObj = orb.string_to_object(copyToNSLoc);
        copyToNameContext = NamingContextExtHelper.narrow(ncObj);
        NamingContextWithPath copyToContextWithPath = new NamingContextWithPath(copyToNameContext,
                                                                                copyToNSLoc);
        copySeisDC(copyToContextWithPath);
        copyNetDC(copyToContextWithPath);
        copyEventDC(copyToContextWithPath);
        copyPlotDC(copyToContextWithPath);
        logger.info("Done");
    }

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(NameServiceCopy.class);
}