/**
 * NSEventDC.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.cache;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import edu.iris.Fissures.IfEvent.EventDCOperations;
import edu.iris.Fissures.IfEvent.EventDC;
import edu.iris.Fissures.IfEvent.EventFinder;
import org.apache.log4j.Logger;
import edu.iris.Fissures.IfEvent.EventChannelFinder;



public class NSEventDC implements EventDCOperations {

    public NSEventDC(String serverDNS,
                     String serverName,
                     FissuresNamingService fissuresNamingService) {
        this.serverDNS = serverDNS;
        this.serverName = serverName;
        this.fissuresNamingService = fissuresNamingService;
    } // NSEventDC constructor

    public String getServerDNS() {
        return serverDNS;
    }

    public String getServerName() {
        return serverName;
    }

    public synchronized void reset() {
        eventDC = null;
    }

    public synchronized EventDC getEventDC() {
        if ( eventDC == null) {
            try {
                eventDC = fissuresNamingService.getEventDC(serverDNS,
                                                           serverName);
            } catch (org.omg.CosNaming.NamingContextPackage.NotFound e) {
                throw new org.omg.CORBA.TRANSIENT("Unable to resolve "+serverName+" "+serverDNS+" "+e.toString(),
                                                  0,
                                                  org.omg.CORBA.CompletionStatus.COMPLETED_NO);
            } catch (org.omg.CosNaming.NamingContextPackage.CannotProceed e) {
                throw new org.omg.CORBA.TRANSIENT("Unable to resolve "+serverName+" "+serverDNS+" "+e.toString(),
                                                  0,
                                                  org.omg.CORBA.CompletionStatus.COMPLETED_NO);
            } catch (org.omg.CORBA.ORBPackage.InvalidName e) {
                throw new org.omg.CORBA.TRANSIENT("Unable to resolve "+serverName+" "+serverDNS+" "+e.toString(),
                                                  0,
                                                  org.omg.CORBA.CompletionStatus.COMPLETED_NO);
            } catch (org.omg.CosNaming.NamingContextPackage.InvalidName e) {
                throw new org.omg.CORBA.TRANSIENT("Unable to resolve "+serverName+" "+serverDNS+" "+e.toString(),
                                                  0,
                                                  org.omg.CORBA.CompletionStatus.COMPLETED_NO);
            } // end of try-catch
        } // end of if ()
        return eventDC;
    }

    public EventChannelFinder a_channel_finder() {
        try {
            return getEventDC().a_channel_finder();
        } catch (Throwable e) {
            // retry in case regetting from name service helps
            logger.warn("Exception in a_channel_finder(), regetting from nameservice to try again.", e);
            reset();
            return getEventDC().a_channel_finder();
        } // end of try-catch
    }

    public EventFinder a_finder() {
        try {
            return getEventDC().a_finder();
        } catch (Throwable e) {
            // retry in case regetting from name service helps
            logger.warn("Exception in a_finder(), regetting from nameservice to try again.", e);
            reset();
            return getEventDC().a_finder();
        } // end of try-catch
    }

    protected EventDC eventDC = null;

    protected String serverDNS;

    protected String serverName;

    protected FissuresNamingService fissuresNamingService;

    private static Logger logger =
        Logger.getLogger(NSEventDC.class);
}

