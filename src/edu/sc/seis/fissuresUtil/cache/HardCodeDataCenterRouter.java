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
        DCResolver sceppResolve = new DCResolver("SCEPP");
        sceppResolve.start();
        DCResolver budResolve = new DCResolver("BUD");
        budResolve.start();
        DCResolver pondResolve = new DCResolver("POND");
        pondResolve.start();
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
            if (rf.length != 0 && route[i].getDataCenter() != null) {
                try {
                    logger.debug("Asking from "+i+" for "+
                                     ChannelIdUtil.toString(rf[0].channel_id)+
                                " from "+rf[0].start_time.date_time+" to "+rf[0].end_time.date_time);
                    RequestFilter[] ls =
                        route[i].getDataCenter().available_data(rf);
                    String mesg =
                        "Got "+ls.length+" req filter from "+i;
                    if (ls.length != 0) {
                        mesg += " for "+
                                     ChannelIdUtil.toString(ls[0].channel_id)+
                                " from "+ls[0].start_time.date_time+" to "+ls[0].end_time.date_time;
                    } else {
                        mesg += " for "+
                                     ChannelIdUtil.toString(rf[0].channel_id);
                    }
                    logger.debug(mesg);
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
            if (rf.length != 0 && route[i].getDataCenter() != null) {
                try {
                    LocalSeismogram[] ls =
                        route[i].getDataCenter().retrieve_seismograms(rf);
                    logger.debug("Got "+ls.length+" lseis from "+i+" for "+
                                     ChannelIdUtil.toString(rf[0].channel_id)+
                                " from "+rf[0].start_time.date_time+" to "+rf[0].end_time.date_time);
                    for (int j = 0; j < ls.length; j++) {
                        allSeis.add(ls[j]);
                    }

                } catch (org.omg.CORBA.SystemException e) {
                    logger.error("Problem getting available data, first= "+
                                     ChannelIdUtil.toString(rf[0].channel_id), e);
                    throw e;
                }
            } else {
                logger.warn("No filters for route "+i);
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
            logger.debug("Resolving Scepp DataCenter");
        while (sceppDC == null) {
            TimeInterval delay = sceppDCLoadTime.difference(ClockUtil.now());
            delay.convertTo(UnitImpl.SECOND);
            logger.debug("Resolving Scepp DataCenter "+delay);
            if (delay.getValue() > 10) {
                // max sleep is 10 seconds
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }
        return sceppDC;
    }

    protected DataCenterOperations loadSceppDC() {
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
        while (budDC == null) {
            logger.debug("Resolving Bud DataCenter");
            TimeInterval delay = budDCLoadTime.difference(ClockUtil.now());
            delay.convertTo(UnitImpl.SECOND);
            logger.debug("Resolving Bud DataCenter "+delay);
            if (delay.getValue() > 10) {
                // max sleep is 10 seconds
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }
        return budDC;
    }

    protected DataCenterOperations loadBudDC() {
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
        while (pondDC == null) {
            logger.debug("Resolving Pond DataCenter");
            TimeInterval delay = pondDCLoadTime.difference(ClockUtil.now());
            delay.convertTo(UnitImpl.SECOND);
            logger.debug("Resolving Pond DataCenter "+delay);
            if (delay.getValue() > 10) {
                // max sleep is 10 seconds
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }
        return pondDC;
    }

    protected DataCenterOperations loadPondDC() {
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
    protected MicroSecondDate sceppDCLoadTime = null;

    protected DataCenterOperations budDC = null;
    protected MicroSecondDate budDCLoadTime = null;

    protected DataCenterOperations pondDC = null;
    protected MicroSecondDate pondDCLoadTime = null;

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

    protected class DCResolver extends Thread {
        DCResolver(String serverName) {
            super("DCResolver"+serverName);
            this.serverName = serverName;
            if (serverName.equals("SCEPP")) {
                sceppDCLoadTime = ClockUtil.now();
            } else if (serverName.equals("BUD")) {
                budDCLoadTime = ClockUtil.now();
            } else if (serverName.equals("POND")) {
                pondDCLoadTime = ClockUtil.now();
            }
        }

        String serverName;

        public void run() {
            if (serverName.equals("SCEPP")) {
                loadSceppDC();
            } else if (serverName.equals("BUD")) {
                loadBudDC();
            } else if (serverName.equals("POND")) {
                loadPondDC();
            }
        }

    }


    static Logger logger = Logger.getLogger(HardCodeDataCenterRouter.class);
}

