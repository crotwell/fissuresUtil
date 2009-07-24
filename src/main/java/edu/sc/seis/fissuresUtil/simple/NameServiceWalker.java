/**
 * NameServiceWalker.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.simple;

import org.apache.log4j.Logger;
import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.cache.NSEventDC;
import edu.sc.seis.fissuresUtil.cache.NSNetworkDC;
import edu.sc.seis.fissuresUtil.cache.NSSeismogramDC;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;

public class NameServiceWalker {

    public NameServiceWalker() {
        FissuresNamingService fisName = Initializer.getNS();
        NSNetworkDC[] nets = fisName.getAllNetworkDC();
        System.out.println("Got "+nets.length+" net dcs");
        NSEventDC[] eventdcs = fisName.getAllEventDC();
        System.out.println("Got "+eventdcs.length+" event dcs");
        NSSeismogramDC[] seisdcs = fisName.getAllSeismogramDC();
        System.out.println("Got "+seisdcs.length+" seis dcs");
        RequestFilter[] rf = SimpleSeismogramClient.createCurrentRF();
        rf[0].channel_id = Initializer.AMNOChannel;
        for (int i = 0; i < seisdcs.length; i++) {
            try {
                LocalSeismogram[] seis = seisdcs[i].retrieve_seismograms(rf);
                System.out.println("Got "+seis.length+" seismograms from "+seisdcs[i].getServerName()+" server");
            } catch (FissuresException e) {
                logger.error("FissuresException with "+i+" server: "+e.the_error.error_description, e);
            } catch (Throwable e) {
                logger.error("Problem with "+i+"th seis dc", e);
            }
        }
    }


    private static Logger logger = Logger.getLogger(NameServiceWalker.class);

    public static void main(String[] args) {
        /* Initializes the corba orb, finds the naming service and other startup
         * tasks. See Initializer for the code in this method. */
        Initializer.init(args);
        new NameServiceWalker();
    }
}

