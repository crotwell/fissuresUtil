/**
 * NameServiceCopy.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.namingService;

import edu.sc.seis.fissuresUtil.cache.NSSeismogramDC;
import org.omg.CORBA_2_3.ORB;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import edu.sc.seis.fissuresUtil.simple.Initializer;
import edu.sc.seis.fissuresUtil.cache.NSNetworkDC;
import edu.sc.seis.fissuresUtil.cache.NSEventDC;
import edu.sc.seis.fissuresUtil.cache.NSPlottableDC;

public class NameServiceCopy {

    /**
     *
     */
    public static void main(String[] args) throws CannotProceed, InvalidName, NotFound, org.omg.CORBA.ORBPackage.InvalidName {

        // this parse the args, reads properties, and inits the orb
        Initializer.init(args);

        String dnsToCopy = System.getProperty("nameServiceCopy.dns");
        if (dnsToCopy == null) {
            System.err.println("System property nameServiceCopy.dns must be set");
            System.exit(1);
        }
        String copyToNS = System.getProperty("nameServiceCopy.copyTo");
        if (copyToNS == null) {
            System.err.println("System property nameServiceCopy.copyTo must be set");
            System.exit(1);
        }

        System.out.println("Copying all from "+dnsToCopy+" to "+copyToNS);

        FissuresNamingService fisNS = Initializer.getNS();
        if (fisNS.getNameService()== null) {
            System.out.println("Problem, ns is null");
            System.exit(1);
        }
        fisNS.addOtherNameServiceCorbaLoc(copyToNS);
        //"corbaloc:iiop:dmc.iris.washington.edu:6371/NameService");
        //fisNS.addOtherNameServiceCorbaLoc("corbaloc:iiop:roo.seis.sc.edu:6371/NameService");

        NSSeismogramDC[] seisDC = fisNS.getAllSeismogramDC();
        System.out.println("Got "+seisDC.length+" seis datacenters");
        for (int i = 0; i < seisDC.length; i++) {
            if (seisDC[i].getServerDNS().equals(dnsToCopy)) {
                if ( ! seisDC[i].getCorbaObject()._non_existent()) {
                    System.out.println("Rebind "+seisDC[i].getServerDNS()+", "+seisDC[i].getServerName());
                    fisNS.rebind(seisDC[i].getServerDNS(), seisDC[i].getServerName(), seisDC[i].getCorbaObject());
                } else {
                    System.out.println("Couldn't ping "+seisDC[i].getServerDNS()+" "+seisDC[i].getServerName()+", skipping");
                }
            } else {
                System.out.println("Doesn't match "+seisDC[i].getServerDNS()+", "+seisDC[i].getServerName());
            }
        }

        NSNetworkDC[] networkDC = fisNS.getAllNetworkDC();
        System.out.println("Got "+networkDC.length+" network datacenters");
        for (int i = 0; i < networkDC.length; i++) {
            if (networkDC[i].getServerDNS().equals(dnsToCopy)) {
                if ( ! networkDC[i].getCorbaObject()._non_existent()) {
                    System.out.println("Rebind "+networkDC[i].getServerDNS()+", "+networkDC[i].getServerName());
                    fisNS.rebind(networkDC[i].getServerDNS(), networkDC[i].getServerName(), networkDC[i].getNetworkDC());
                } else {
                    System.out.println("Couldn't ping "+networkDC[i].getServerDNS()+" "+networkDC[i].getServerName()+", skipping");
                }
            } else {
                System.out.println("Doesn't match "+networkDC[i].getServerDNS()+", "+networkDC[i].getServerName());
            }
        }

        NSEventDC[] eventDC = fisNS.getAllEventDC();
        System.out.println("Got "+eventDC.length+" event datacenters");
        for (int i = 0; i < eventDC.length; i++) {
            if (eventDC[i].getServerDNS().equals(dnsToCopy)) {
                // only copy if can ping orginal
                if (! eventDC[i].getCorbaObject()._non_existent()) {
                    System.out.println("Rebind "+eventDC[i].getServerDNS()+", "+eventDC[i].getServerName());
                    fisNS.rebind(eventDC[i].getServerDNS(), eventDC[i].getServerName(), eventDC[i].getCorbaObject());
                } else {
                    System.out.println("Couldn't ping "+eventDC[i].getServerDNS()+" "+eventDC[i].getServerName()+", skipping");
                }
            } else {
                System.out.println("Doesn't match "+eventDC[i].getServerDNS()+", "+eventDC[i].getServerName());
            }
        }

        NSPlottableDC[] plotDC = fisNS.getAllPlottableDC();
        System.out.println("Got "+plotDC.length+" seis datacenters");
        for (int i = 0; i < plotDC.length; i++) {
            if (plotDC[i].getServerDNS().equals(dnsToCopy)) {
                // only copy if can ping orginal
                if (! plotDC[i].getCorbaObject()._non_existent()) {
                    System.out.println("Rebind "+plotDC[i].getServerDNS()+", "+plotDC[i].getServerName());
                    fisNS.rebind(plotDC[i].getServerDNS(), plotDC[i].getServerName(), plotDC[i].getPlottableDC());
                } else {
                    System.out.println("Couldn't ping "+plotDC[i].getServerDNS()+" "+plotDC[i].getServerName()+", skipping");
                }
            } else {
                System.out.println("Doesn't match "+plotDC[i].getServerDNS()+", "+plotDC[i].getServerName());
            }
        }

        System.out.println("Done");
    }
}


