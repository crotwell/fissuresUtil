/**
 * NSEventDC.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.fissuresUtil.cache;

import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import edu.iris.Fissures.IfEvent.EventChannelFinder;
import edu.iris.Fissures.IfEvent.EventDC;
import edu.iris.Fissures.IfEvent.EventDCOperations;
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
        if(threadEventDC.get() != null) {
            ((org.omg.CORBA.Object)threadEventDC.get())._release();
        }
        threadEventDC.set(null);
    }

    public EventDCOperations getEventDC() {
        return (EventDC)getCorbaObject();
    }

    public void setEventDC(EventDCOperations eventDC) {
        threadEventDC.set(eventDC);
    }

    public synchronized org.omg.CORBA.Object getCorbaObject() {
        if(threadEventDC.get() == null) {
            try {
                try {
                    loadEventDC();
                } catch(Throwable t) {
                    namingService.reset();
                    loadEventDC();
                }
            } catch(org.omg.CosNaming.NamingContextPackage.NotFound e) {
                repackageException(e);
            } catch(org.omg.CosNaming.NamingContextPackage.CannotProceed e) {
                repackageException(e);
            } catch(org.omg.CosNaming.NamingContextPackage.InvalidName e) {
                repackageException(e);
            } // end of try-catch
        } // end of if ()
        return (org.omg.CORBA.Object)threadEventDC.get();
    }

    private void loadEventDC() throws NotFound, CannotProceed, InvalidName {
        setEventDC(namingService.getEventDC(serverDNS, serverName));
    }

    protected void repackageException(org.omg.CORBA.UserException e) {
        String msg = "Unable to resolve " + serverName + " " + serverDNS + " "
                + e.toString();
        org.omg.CORBA.TRANSIENT t = new org.omg.CORBA.TRANSIENT(msg,
                                                                0,
                                                                org.omg.CORBA.CompletionStatus.COMPLETED_NO);
        t.initCause(e);
        throw t;
    }

    public EventChannelFinder a_channel_finder() {
        try {
            return getEventDC().a_channel_finder();
        } catch(Throwable e) {
            reset();
            try {
                return getEventDC().a_channel_finder();
            } catch(RuntimeException ee) {
                reset();
                throw ee;
            }
        } // end of try-catch
    }

    public EventFinder a_finder() {
        try {
            return getEventDC().a_finder();
        } catch(Throwable e) {
            reset();
            try {
                return getEventDC().a_finder();
            } catch(RuntimeException ee) {
                reset();
                throw ee;
            }
        } // end of try-catch
    }

    protected String serverDNS;

    protected String serverName;

    protected FissuresNamingService namingService;
    
    protected ThreadLocal threadEventDC = new ThreadLocal();

}