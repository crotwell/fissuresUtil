package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfSeismogramDC.DataCenter;
import edu.iris.Fissures.IfSeismogramDC.DataCenterOperations;
import edu.sc.seis.fissuresUtil.cache.NSEventDC;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import org.apache.log4j.Logger;
import edu.iris.Fissures.IfSeismogramDC.DataCenterCallBack;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.Time;

public class NSSeismogramDC implements ProxySeismogramDC {
    public NSSeismogramDC(String serverDNS,
                          String serverName,
                          FissuresNamingService fissuresNamingService) {
        this.dns = serverDNS;
        this.serverName = serverName;
        this.nameService = fissuresNamingService;
    } // NSEventDC constructor

    public String getDns(){ return dns; }

    public String getServerName() { return serverName; }

    public synchronized void reset() {
        dc = null;
    }

    public DataCenter getCorbaObject() {
        return getDataCenter();
    }

    public synchronized DataCenter getDataCenter() {
        if ( dc == null) {
            try {
                dc = nameService.getSeismogramDC(dns, serverName);
            } catch (org.omg.CosNaming.NamingContextPackage.NotFound e) {
                throw new org.omg.CORBA.TRANSIENT("Unable to resolve "+serverName+" "+dns+" "+e.toString(),
                                                  0,
                                                  org.omg.CORBA.CompletionStatus.COMPLETED_NO);
            } catch (org.omg.CosNaming.NamingContextPackage.CannotProceed e) {
                throw new org.omg.CORBA.TRANSIENT("Unable to resolve "+serverName+" "+dns+" "+e.toString(),
                                                  0,
                                                  org.omg.CORBA.CompletionStatus.COMPLETED_NO);
            } catch (org.omg.CORBA.ORBPackage.InvalidName e) {
                throw new org.omg.CORBA.TRANSIENT("Unable to resolve "+serverName+" "+dns+" "+e.toString(),
                                                  0,
                                                  org.omg.CORBA.CompletionStatus.COMPLETED_NO);
            } catch (org.omg.CosNaming.NamingContextPackage.InvalidName e) {
                throw new org.omg.CORBA.TRANSIENT("Unable to resolve "+serverName+" "+dns+" "+e.toString(),
                                                  0,
                                                  org.omg.CORBA.CompletionStatus.COMPLETED_NO);
            } // end of try-catch
        } // end of if ()
        return dc;
    }

    public String queue_seismograms(RequestFilter[] a_filterseq) throws FissuresException {
        try {
            return getDataCenter().queue_seismograms(a_filterseq);
        } catch (Throwable e) {
            // retry in case regetting from name service helps
            logger.warn("Exception in queue_seismograms(), regetting from nameservice to try again.", e);
            reset();
            return getDataCenter().queue_seismograms(a_filterseq);
        } // end of try-catch
    }

    public LocalSeismogram[] retrieve_queue(String a_request) throws FissuresException {
        try {
            return getDataCenter().retrieve_queue(a_request);
        } catch (Throwable e) {
            // retry in case regetting from name service helps
            logger.warn("Exception in retrieve_queue(), regetting from nameservice to try again.", e);
            reset();
            return getDataCenter().retrieve_queue(a_request);
        } // end of try-catch
    }

    public RequestFilter[] available_data(RequestFilter[] a_filterseq) {
        try {
            return getDataCenter().available_data(a_filterseq);
        } catch (Throwable e) {
            // retry in case regetting from name service helps
            logger.warn("Exception in available_data(), regetting from nameservice to try again.", e);
            reset();
            return getDataCenter().available_data(a_filterseq);
        } // end of try-catch
    }

    public void cancel_request(String a_request) throws FissuresException {
        try {
            getDataCenter().cancel_request(a_request);
        } catch (Throwable e) {
            // retry in case regetting from name service helps
            logger.warn("Exception in cancel_request(), regetting from nameservice to try again.", e);
            reset();
            getDataCenter().cancel_request(a_request);
        } // end of try-catch
    }

    public String request_seismograms(RequestFilter[] a_filterseq, DataCenterCallBack a_client, boolean long_lived, Time expiration_time) throws FissuresException {
        try {
            return getDataCenter().request_seismograms(a_filterseq,
                                                       a_client,
                                                       long_lived,
                                                       expiration_time);
        } catch (Throwable e) {
            // retry in case regetting from name service helps
            logger.warn("Exception in request_seismograms(), regetting from nameservice to try again.", e);
            reset();
            return getDataCenter().request_seismograms(a_filterseq,
                                                       a_client,
                                                       long_lived,
                                                       expiration_time);
        } // end of try-catch
    }

    public String request_status(String a_request) throws FissuresException {
        try {
            return getDataCenter().request_status(a_request);
        } catch (Throwable e) {
            // retry in case regetting from name service helps
            logger.warn("Exception in request_status(), regetting from nameservice to try again.", e);
            reset();
            return getDataCenter().request_status(a_request);
        } // end of try-catch
    }

    public LocalSeismogram[] retrieve_seismograms(RequestFilter[] a_filterseq) throws FissuresException {
        try {
            return getDataCenter().retrieve_seismograms(a_filterseq);
        } catch (Throwable e) {
            // retry in case regetting from name service helps
            logger.warn("Exception in retrieve_seismograms(), regetting from nameservice to try again.", e);
            reset();
            return getDataCenter().retrieve_seismograms(a_filterseq);
        } // end of try-catch
    }


    protected String dns, serverName;

    private DataCenter dc;

    protected FissuresNamingService nameService;

    private static Logger logger = Logger.getLogger(NSSeismogramDC.class);
}

