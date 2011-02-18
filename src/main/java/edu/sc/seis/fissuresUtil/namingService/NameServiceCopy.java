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
            if(!dns.equals(dnsToCopy)) {
                continue;
            }
            String name = from[i].getServerName();
            logger.info(name + " is in " + dnsToCopy + ".  Checking it");
            org.omg.CORBA.Object fromObj;
            try {
                fromObj = from[i].getCorbaObject();
                if(from[i].getCorbaObject()._non_existent()) {
                    logger.info("Couldn't ping " + dns + " " + name
                            + ", skipping");
                    continue;
                }
            } catch(RuntimeException e) {
                logger.warn("Exception thrown while getting "
                                    + name
                                    + " from from name service.  Not being rebound to to nameservice",
                            e);
                continue;
            }
            boolean rebind = true;
            for(int j = 0; j < to.length; j++) {
                if(to[j].getServerDNS().equals(dnsToCopy)
                        && to[j].getServerName().equals(name)) {
                    logger.info("Copy to name service contains " + name
                            + " as well");
                    try {
                        org.omg.CORBA.Object toObj = to[j].getCorbaObject();
                        if(!toObj._non_existent() && fromObj.equals(toObj)) {
                            logger.info("Not going to rebind.  Copy to name service copy of "
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
        }
    }

    private static void copyEventDC() throws NotFound, CannotProceed,
            InvalidName {
        NSEventDC[] copyFromPlotDC = copyFromNS.getAllEventDC();
        NSEventDC[] copyToPlotDC = copyToNS.getAllEventDC();
        logger.info("Got " + copyFromPlotDC.length + " event datacenters");
        copy(copyFromPlotDC, copyToPlotDC);
    }

    private static void copyPlotDC() throws NotFound, CannotProceed,
            InvalidName {
        NSPlottableDC[] copyFromPlotDC = copyFromNS.getAllPlottableDC();
        NSPlottableDC[] copyToPlotDC = copyToNS.getAllPlottableDC();
        logger.info("Got " + copyFromPlotDC.length + " plot datacenters");
        copy(copyFromPlotDC, copyToPlotDC);
    }

    private static void copyNetDC() throws NotFound, CannotProceed, InvalidName {
        NSNetworkDC[] copyFromNetDC = copyFromNS.getAllNetworkDC();
        NSNetworkDC[] copyToNetDC = copyToNS.getAllNetworkDC();
        logger.info("Got " + copyToNetDC.length + " net datacenters");
        copy(copyFromNetDC, copyToNetDC);
    }

    private static void copySeisDC() throws NotFound, CannotProceed,
            InvalidName {
        NSSeismogramDC[] copyFromSeisDC = copyFromNS.getAllSeismogramDC();
        NSSeismogramDC[] copyToSeisDC = copyToNS.getAllSeismogramDC();
        logger.info("Got " + copyFromSeisDC.length + " seis datacenters");
        copy(copyFromSeisDC, copyToSeisDC);
    }

    private static FissuresNamingService copyFromNS, copyToNS;

    private static NamingContextExt copyToNameContext;

    private static String dnsToCopy;

    public static void main(String[] args) throws CannotProceed, InvalidName,
            NotFound {
        // this parse the args, reads properties, and inits the orb
        Initializer.init(args);
        for(int i = 0; i < args.length; i++) {
            if(args[i].equals("-h") || args[i].equals("--help")) {
                System.out.println("-props propfile  Connfiguration properties");
                System.out.println("-h --help        Print this help message");
                System.exit(0);
            }
        }
        dnsToCopy = System.getProperty("nameServiceCopy.dns");
        if(dnsToCopy == null) {
            logErrExit("System property nameServiceCopy.dns must be set");
        }
        String copyToNSLoc = System.getProperty("nameServiceCopy.copyTo");
        if(copyToNSLoc == null) {
            logErrExit("System property nameServiceCopy.copyTo must be set");
        }
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
        String copyFromNSLoc = System.getProperty(FissuresNamingService.CORBALOC_PROP);
        logger.info("Copying all servers in " + dnsToCopy
                + " from a naming service at " + copyFromNSLoc
                + " to naming service at " + copyToNSLoc);
        copySeisDC();
        copyNetDC();
        copyEventDC();
        copyPlotDC();
        logger.info("Done");
    }

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NameServiceCopy.class);
}
