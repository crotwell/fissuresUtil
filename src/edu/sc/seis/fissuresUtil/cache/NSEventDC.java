/**
 * NSEventDC.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.cache;
import org.apache.log4j.Logger;

import edu.iris.Fissures.IfEvent.EventChannelFinder;
import edu.iris.Fissures.IfEvent.EventDC;
import edu.iris.Fissures.IfEvent.EventFinder;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;



public class NSEventDC extends ProxyEventDC implements ServerNameDNS {

    public NSEventDC(String serverDNS,
                     String serverName,
                     FissuresNamingService fissuresNamingService) {
        this.serverDNS = serverDNS;
        this.serverName = serverName;
        this.namingService = fissuresNamingService;
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

    public synchronized EventDC getCorbaObject() {
        if ( eventDC == null) {
            try {
                try {
                    setEventDC(namingService.getEventDC(serverDNS, serverName));
                } catch (Throwable t) {
                    namingService.reset();
                    eventDC = namingService.getEventDC(serverDNS,serverName);
                }
            } catch (org.omg.CosNaming.NamingContextPackage.NotFound e) {
                repackageException(e);
            } catch (org.omg.CosNaming.NamingContextPackage.CannotProceed e) {
                repackageException(e);
            } catch (org.omg.CORBA.ORBPackage.InvalidName e) {
                repackageException(e);
            } catch (org.omg.CosNaming.NamingContextPackage.InvalidName e) {
                repackageException(e);
            } // end of try-catch
        } // end of if ()
        return (EventDC)eventDC;
    }

    protected void repackageException(org.omg.CORBA.UserException e) {
        org.omg.CORBA.TRANSIENT t =
            new org.omg.CORBA.TRANSIENT("Unable to resolve "+serverName+" "+serverDNS+" "+e.toString(),
                                        0,
                                        org.omg.CORBA.CompletionStatus.COMPLETED_NO);
        t.initCause(e);
        throw t;
    }

    public EventChannelFinder a_channel_finder() {
        try {
            return getCorbaObject().a_channel_finder();
        } catch (Throwable e) {
            // retry in case regetting from name service helps
            logger.warn("Exception in a_channel_finder(), regetting from nameservice to try again.", e);
            reset();
            return getCorbaObject().a_channel_finder();
        } // end of try-catch
    }

    public EventFinder a_finder() {
        try {
            return getCorbaObject().a_finder();
        } catch (Throwable e) {
            // retry in case regetting from name service helps
            logger.warn("Exception in a_finder(), regetting from nameservice to try again.", e);
            reset();
            return getCorbaObject().a_finder();
        } // end of try-catch
    }

    protected String serverDNS;

    protected String serverName;

    protected FissuresNamingService namingService;

    private static Logger logger =
        Logger.getLogger(NSEventDC.class);
}

