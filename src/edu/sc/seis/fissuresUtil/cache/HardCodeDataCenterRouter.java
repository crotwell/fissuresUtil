/**
 * HardCodeDataCenterRouter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfSeismogramDC.DataCenterCallBack;
import edu.iris.Fissures.IfSeismogramDC.DataCenterOperations;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.exceptionHandlerGUI.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 NOTE this is BAD code, but configures GEE to go to the SCEPP datacenter
 for SP requests and to the DMC BUD for all other requests within the
 last 2 months and to the DMC POND for all others.
 
 This does not use anything from the configuration file, and so is the wrong
 way to do it, but allows us to limp through the dlese workshop.
 */
public class HardCodeDataCenterRouter extends DataCenterRouter implements DataCenterOperations {
    
    public HardCodeDataCenterRouter(FissuresNamingService fissuresNamingService) {
        this.fissuresNamingService = fissuresNamingService;
    }
    
    
    
    /**
     * Method queue_seismograms
     *
     * @param    p                   a  RequestFilter[]
     *
     * @return   a String
     *
     * @throws   FissuresException
     *
     */
    public String queue_seismograms(RequestFilter[] p0) throws FissuresException {
        // TODO
        return null;
    }
    
    /**
     * Method retrieve_queue
     *
     * @param    p                   a  String
     *
     * @return   a LocalSeismogram[]
     *
     * @throws   FissuresException
     *
     */
    public LocalSeismogram[] retrieve_queue(String p0) throws FissuresException {
        // TODO
        return null;
    }
    
    /**
     * Method available_data
     *
     * @param    p                   a  RequestFilter[]
     *
     * @return   a RequestFilter[]
     *
     */
    public RequestFilter[] available_data(RequestFilter[] filters) {
        DataCenterRoute[] route = makeRoutes(filters);
        LinkedList allSeis = new LinkedList();
        for (int i = 0; i < route.length; i++) {
            RequestFilter[] rf = route[i].getRequestFilters();
            if (rf.length != 0) {
                try {
                    logger.debug("Asking from "+i+" for "+
                                     ChannelIdUtil.toString(rf[0].channel_id));
                    RequestFilter[] ls =
                        route[i].getDataCenter().available_data(rf);
                    for (int j = 0; j < ls.length; j++) {
                        allSeis.add(ls[j]);
                    }
                } catch (org.omg.CORBA.SystemException e) {
                    logger.error("Problem getting available data, first= "+
                                     ChannelIdUtil.toString(rf[0].channel_id), e);
                    throw e;
                }
            }
        }
        return (RequestFilter[])allSeis.toArray(new RequestFilter[0]);
    }
    
    /**
     * Method cancel_request
     *
     * @param    p                   a  String
     *
     * @throws   FissuresException
     *
     */
    public void cancel_request(String p0) throws FissuresException {
        // TODO
    }
    
    /**
     * Method retrieve_seismograms
     *
     * @param    p                   a  RequestFilter[]
     *
     * @return   a LocalSeismogram[]
     *
     * @throws   FissuresException
     *
     */
    public LocalSeismogram[] retrieve_seismograms(RequestFilter[] filters) throws FissuresException {
        DataCenterRoute[] route = makeRoutes(filters);
        LinkedList allSeis = new LinkedList();
        for (int i = 0; i < route.length; i++) {
            RequestFilter[] rf = route[i].getRequestFilters();
            if (rf.length != 0) {
                LocalSeismogram[] ls =
                    route[i].getDataCenter().retrieve_seismograms(rf);
                for (int j = 0; j < ls.length; j++) {
                    allSeis.add(ls[j]);
                }
            }
        }
        return (LocalSeismogram[])allSeis.toArray(new LocalSeismogramImpl[0]);
    }
    
    /**
     * Method request_status
     *
     * @param    p                   a  String
     *
     * @return   a String
     *
     * @throws   FissuresException
     *
     */
    public String request_status(String p0) throws FissuresException {
        // TODO
        return null;
    }
    
    /**
     * Method request_seismograms
     *
     * @param    p                   a  RequestFilter[]
     * @param    p                   a  DataCenterCallBack
     * @param    p                   a  boolean
     * @param    p                   a  Time
     *
     * @return   a String
     *
     * @throws   FissuresException
     *
     */
    public String request_seismograms(RequestFilter[] p0, DataCenterCallBack p1, boolean p2, Time p3) throws FissuresException {
        // TODO
        return null;
    }
    
    protected DataCenterRoute[] makeRoutes(RequestFilter[] filters) {
        DataCenterRoute[] out = new DataCenterRoute[3];
        out[0] = new DataCenterRoute(getSceppDC());
        out[1] = new DataCenterRoute(getBudDC());
        out[2] = new DataCenterRoute(getPondDC());
        
        TimeInterval BUD_OFFSET = new TimeInterval(60, UnitImpl.DAY);
        MicroSecondDate BUD_CUTOFF = ClockUtil.now().subtract(BUD_OFFSET);
        for (int i = 0; i < filters.length; i++) {
            if (filters[i].channel_id.network_id.network_code.equals("SP")) {
                logger.info("Request to SCEPP"+ChannelIdUtil.toStringNoDates(filters[i].channel_id));
                out[0].add(filters[i]);
            } else {
                MicroSecondDate end = new MicroSecondDate(filters[i].end_time);
                if (end.after(BUD_CUTOFF)) {
                    logger.info("Request to Bud"+ChannelIdUtil.toStringNoDates(filters[i].channel_id));
                    out[1].add(filters[i]);
                } else {
                    logger.info("Request to Pond"+ChannelIdUtil.toStringNoDates(filters[i].channel_id));
                    out[2].add(filters[i]);
                }
            }
        }
        return out;
    }
    
    public List getDataCenter(String networkCode) {
        LinkedList out = new LinkedList();
        if (networkCode.equals("SP")) {
            out.add(getSceppDC());
        } else {
            out.add(getBudDC());
            out.add(getPondDC());
        }
        return out;
    }
    
    protected DataCenterOperations getSceppDC() {
        String dsname = "SCEPP";
        if (sceppDC == null) {
            try {
                sceppDC = fissuresNamingService.getSeismogramDC("edu/sc/seis",
                                                                "SCEPPSeismogramDC");
            } catch (org.omg.CORBA.ORBPackage.InvalidName e) {
                GlobalExceptionHandler.handleStatic("Can't get DataCenter for "+dsname, e);
            } catch (org.omg.CosNaming.NamingContextPackage.NotFound e) {
                GlobalExceptionHandler.handleStatic("Can't get DataCenter for "+dsname, e);
            } catch (org.omg.CosNaming.NamingContextPackage.CannotProceed e) {
                GlobalExceptionHandler.handleStatic("Can't get DataCenter for "+dsname, e);
            } catch (org.omg.CosNaming.NamingContextPackage.InvalidName e) {
                GlobalExceptionHandler.handleStatic("Can't get DataCenter for "+dsname, e);
            }
        }
        return sceppDC;
    }
    
    protected DataCenterOperations getBudDC() {
        String dsname = "BUD";
        if (budDC == null) {
            try {
                budDC = fissuresNamingService.getSeismogramDC("edu/iris/dmc",
                                                              "IRIS_BudDataCenter");
            } catch (org.omg.CORBA.ORBPackage.InvalidName e) {
                GlobalExceptionHandler.handleStatic("Can't get DataCenter for "+dsname, e);
            } catch (org.omg.CosNaming.NamingContextPackage.NotFound e) {
                GlobalExceptionHandler.handleStatic("Can't get DataCenter for "+dsname, e);
            } catch (org.omg.CosNaming.NamingContextPackage.CannotProceed e) {
                GlobalExceptionHandler.handleStatic("Can't get DataCenter for "+dsname, e);
            } catch (org.omg.CosNaming.NamingContextPackage.InvalidName e) {
                GlobalExceptionHandler.handleStatic("Can't get DataCenter for "+dsname, e);
            }
        }
        return budDC;
    }
    
    protected DataCenterOperations getPondDC() {
        String dsname = "Pond";
        if (pondDC == null) {
            try {
                pondDC = fissuresNamingService.getSeismogramDC("edu/iris/dmc",
                                                               "IRIS_PondDataCenter");
            } catch (org.omg.CORBA.ORBPackage.InvalidName e) {
                GlobalExceptionHandler.handleStatic("Can't get DataCenter for "+dsname, e);
            } catch (org.omg.CosNaming.NamingContextPackage.NotFound e) {
                GlobalExceptionHandler.handleStatic("Can't get DataCenter for "+dsname, e);
            } catch (org.omg.CosNaming.NamingContextPackage.CannotProceed e) {
                GlobalExceptionHandler.handleStatic("Can't get DataCenter for "+dsname, e);
            } catch (org.omg.CosNaming.NamingContextPackage.InvalidName e) {
                GlobalExceptionHandler.handleStatic("Can't get DataCenter for "+dsname, e);
            }
        }
        return pondDC;
    }
    
    protected DataCenterOperations sceppDC = null;
    
    protected DataCenterOperations budDC = null;
    
    protected DataCenterOperations pondDC = null;
    
    FissuresNamingService fissuresNamingService;
    
    protected class DataCenterRoute {
        DataCenterRoute(DataCenterOperations dc) {
            this.dc = dc;
        }
        
        void add(RequestFilter filter) {
            filterList.add(filter);
        }
        
        RequestFilter[] getRequestFilters() {
            return (RequestFilter[])filterList.toArray(new RequestFilter[0]);
        }
        
        DataCenterOperations getDataCenter() {
            return dc;
        }
        
        DataCenterOperations dc;
        LinkedList filterList = new LinkedList();
    }
    
    static Logger logger = Logger.getLogger(HardCodeDataCenterRouter.class);
}

