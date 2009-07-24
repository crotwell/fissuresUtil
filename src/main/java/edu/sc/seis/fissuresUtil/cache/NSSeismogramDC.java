package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfSeismogramDC.DataCenter;
import edu.iris.Fissures.IfSeismogramDC.DataCenterCallBack;
import edu.iris.Fissures.IfSeismogramDC.DataCenterOperations;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;

public class NSSeismogramDC implements ServerNameDNS, ProxySeismogramDC {

    public NSSeismogramDC(String serverDNS,
                          String serverName,
                          FissuresNamingService fissuresNamingService) {
        this.serverDNS = serverDNS;
        this.serverName = serverName;
        this.nameService = fissuresNamingService;
    }

    public DataCenterOperations getWrappedDC() {
        return getDataCenter();
    }

    public DataCenterOperations getWrappedDC(Class wrappedClass) {
        if(wrappedClass.equals(DataCenter.class)) {
            return getDataCenter();
        } else {
            throw new IllegalArgumentException("NSSeismogramDCs only contain DataCenters, so it can't contain a ProxyDC of class "
                    + wrappedClass);
        }
    }

    public String getServerDNS() {
        return serverDNS;
    }

    public String getServerName() {
        return serverName;
    }
    
    public String getFullName(){
        return getServerDNS() + "/" + getServerName();
    }


    public String getServerType() {
        return SEISDC_TYPE;
    }

    public synchronized void reset() {
        if(dc.get() != null){
            ((DataCenter)dc.get())._release();
        }
        dc.set(null);
    }

    public org.omg.CORBA.Object getCorbaObject() {
        return getDataCenter();
    }

    public synchronized DataCenter getDataCenter() {
        if(dc.get() == null) {
            try {
                try {
                    dc.set(nameService.getSeismogramDC(serverDNS, serverName));
                } catch(Throwable t) {
                    nameService.reset();
                    dc.set(nameService.getSeismogramDC(serverDNS, serverName));
                }
            } catch(org.omg.CosNaming.NamingContextPackage.NotFound e) {
                repackageException(e);
            } catch(org.omg.CosNaming.NamingContextPackage.CannotProceed e) {
                repackageException(e);
            } catch(org.omg.CosNaming.NamingContextPackage.InvalidName e) {
                repackageException(e);
            } // end of try-catch
        } // end of if ()
        return (DataCenter)dc.get();
    }

    protected void repackageException(org.omg.CORBA.UserException e) {
        String msg = "Unable to resolve " + getServerPath() + " "
                + e.toString();
        org.omg.CORBA.TRANSIENT t = new org.omg.CORBA.TRANSIENT(msg,
                                                                0,
                                                                org.omg.CORBA.CompletionStatus.COMPLETED_NO);
        t.initCause(e);
        throw t;
    }

    public String queue_seismograms(RequestFilter[] a_filterseq)
            throws FissuresException {
        try {
            return getDataCenter().queue_seismograms(a_filterseq);
        } catch(Throwable e) {
            reset();
            try {
                return getDataCenter().queue_seismograms(a_filterseq);
            } catch(RuntimeException ee) {
                reset();
                throw ee;
            }
        } // end of try-catch
    }

    public LocalSeismogram[] retrieve_queue(String a_request)
            throws FissuresException {
        try {
            return getDataCenter().retrieve_queue(a_request);
        } catch(Throwable e) {
            reset();
            try {
                return getDataCenter().retrieve_queue(a_request);
            } catch(RuntimeException ee) {
                reset();
                throw ee;
            }
        } // end of try-catch
    }

    public RequestFilter[] available_data(RequestFilter[] a_filterseq) {
        try {
            return getDataCenter().available_data(a_filterseq);
        } catch(Throwable e) {
            reset();
            try {
                return getDataCenter().available_data(a_filterseq);
            } catch(RuntimeException ee) {
                reset();
                throw ee;
            }
            
        } // end of try-catch
    }

    public void cancel_request(String a_request) throws FissuresException {
        try {
            getDataCenter().cancel_request(a_request);
        } catch(Throwable e) {
            reset();
            try {
                getDataCenter().cancel_request(a_request);
            } catch(RuntimeException ee) {
                reset();
                throw ee;
            }
        } // end of try-catch
    }

    public String request_seismograms(RequestFilter[] a_filterseq,
                                      DataCenterCallBack a_client,
                                      boolean long_lived,
                                      Time expiration_time)
            throws FissuresException {
        try {
            return getDataCenter().request_seismograms(a_filterseq,
                                                       a_client,
                                                       long_lived,
                                                       expiration_time);
        } catch(Throwable e) {
            reset();
            try {
                return getDataCenter().request_seismograms(a_filterseq,
                                                           a_client,
                                                           long_lived,
                                                           expiration_time);
            } catch(RuntimeException ee) {
                reset();
                throw ee;
            }
        } // end of try-catch
    }

    public String request_status(String a_request) throws FissuresException {
        try {
            return getDataCenter().request_status(a_request);
        } catch(Throwable e) {
            reset();
            try {
                return getDataCenter().request_status(a_request);
            } catch(RuntimeException ee) {
                reset();
                throw ee;
            }
        } // end of try-catch
    }

    public LocalSeismogram[] retrieve_seismograms(RequestFilter[] a_filterseq)
            throws FissuresException {
        try {
            return getDataCenter().retrieve_seismograms(a_filterseq);
        } catch(Throwable e) {
            reset();
            try {
                return getDataCenter().retrieve_seismograms(a_filterseq);
            } catch(RuntimeException ee) {
                reset();
                throw ee;
            }
        } // end of try-catch
    }

    public String toString() {
        return "NSSeismogramDC " + getServerPath();
    }

    private String getServerPath() {
        return serverDNS + "/" + serverName;
    }

    protected String serverDNS, serverName;

    private ThreadLocal dc = new ThreadLocal();

    protected FissuresNamingService nameService;
    
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(NSSeismogramDC.class);
}