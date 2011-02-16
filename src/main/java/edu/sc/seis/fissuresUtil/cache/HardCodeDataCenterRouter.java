/**
 * HardCodeDataCenterRouter.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.fissuresUtil.cache;

import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.omg.CORBA.NO_IMPLEMENT;
import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfSeismogramDC.DataCenterCallBack;
import edu.iris.Fissures.IfSeismogramDC.DataCenterOperations;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.ISOTime;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;

/**
 * NOTE this is BAD code, but configures GEE to go to the SCEPP datacenter for
 * SP requests after the June 29, 2004 data shipment to the DMC, to the DMC BUD
 * for all other requests within the last 2 months and to the DMC POND for all
 * others. This does not use anything from the configuration file, and so is the
 * wrong way to do it, but allows us to limp through our pathetic excuse for a
 * life
 */
public class HardCodeDataCenterRouter extends DataCenterRouter implements
        DataCenterOperations {

    public HardCodeDataCenterRouter(FissuresNamingService fissuresNamingService) {
        this.fissuresNamingService = fissuresNamingService;
        DCResolver sceppResolve = new DCResolver(SCEPP);
        sceppResolve.start();
        DCResolver budResolve = new DCResolver(BUD);
        budResolve.start();
        DCResolver pondResolve = new DCResolver(POND);
        pondResolve.start();
        DCResolver snepResolve = new DCResolver(SNEP);
        snepResolve.start();
    }

    static final String SCEPP = "SCEPP";

    static final String BUD = "BUD";

    static final String POND = "POND";
    
    static final String SNEP = "SNEP";

    static final int SCEPP_INDEX = 0;

    static final int BUD_INDEX = 1;

    static final int POND_INDEX = 2;

    public String queue_seismograms(RequestFilter[] p0) {
        throw new NO_IMPLEMENT();
    }

    public LocalSeismogram[] retrieve_queue(String p0) {
        throw new NO_IMPLEMENT();
    }

    public String request_status(String p0) {
        throw new NO_IMPLEMENT();
    }

    public void cancel_request(String p0) {
        throw new NO_IMPLEMENT();
    }

    public String request_seismograms(RequestFilter[] p0,
                                      DataCenterCallBack p1,
                                      boolean p2,
                                      Time p3) {
        throw new NO_IMPLEMENT();
    }

    public RequestFilter[] available_data(RequestFilter[] filters) {
        DataCenterRoute[] route = makeRoutes(filters);
        LinkedList allSeis = new LinkedList();
        for(int i = 0; i < route.length; i++) {
            RequestFilter[] rf = route[i].getRequestFilters();
            if(rf.length != 0 && route[i].getDataCenter() != null) {
                try {
                    logger.debug("Asking from " + i + " for "
                            + ChannelIdUtil.toString(rf[0].channel_id)
                            + " from " + rf[0].start_time.date_time + " to "
                            + rf[0].end_time.date_time);
                    RequestFilter[] ls = null;
                    ls = route[i].getDataCenter().available_data(rf);
                    String mesg = "Got " + ls.length + " req filter from " + i;
                    if(ls.length != 0) {
                        mesg += " for "
                                + ChannelIdUtil.toString(ls[0].channel_id)
                                + " from " + ls[0].start_time.date_time
                                + " to " + ls[0].end_time.date_time;
                    } else {
                        mesg += " for "
                                + ChannelIdUtil.toString(rf[0].channel_id);
                    }
                    logger.debug(mesg);
                    for(int j = 0; j < ls.length; j++) {
                        allSeis.add(ls[j]);
                    }
                } catch(org.omg.CORBA.SystemException e) {
                    logger.error("Problem getting available data, first= "
                            + ChannelIdUtil.toString(rf[0].channel_id), e);
                    throw e;
                }
            }
        }
        return (RequestFilter[])allSeis.toArray(new RequestFilter[0]);
    }

    public LocalSeismogram[] retrieve_seismograms(RequestFilter[] filters)
            throws FissuresException {
        DataCenterRoute[] route = makeRoutes(filters);
        LinkedList allSeis = new LinkedList();
        for(int i = 0; i < route.length; i++) {
            RequestFilter[] rf = route[i].getRequestFilters();
            if(rf.length != 0 && route[i].getDataCenter() != null) {
                try {
                    LocalSeismogram[] ls = route[i].getDataCenter()
                            .retrieve_seismograms(rf);
                    logger.debug("Got " + ls.length + " lseis from " + i
                            + " for "
                            + ChannelIdUtil.toString(rf[0].channel_id)
                            + " from " + rf[0].start_time.date_time + " to "
                            + rf[0].end_time.date_time);
                    for(int j = 0; j < ls.length; j++) {
                        allSeis.add(ls[j]);
                    }
                } catch(org.omg.CORBA.SystemException e) {
                    logger.error("Problem getting available data, first= "
                            + ChannelIdUtil.toString(rf[0].channel_id), e);
                    throw e;
                }
            } else {
                logger.warn("No filters for route " + i);
            }
        }
        return (LocalSeismogram[])allSeis.toArray(new LocalSeismogramImpl[0]);
    }

    private static final TimeInterval BUD_OFFSET = new TimeInterval(60,
                                                                    UnitImpl.DAY);

    private static final MicroSecondDate SCEPP_TO_POND = new ISOTime("20040629112106.8104GMT").getDate();

    protected DataCenterRoute[] makeRoutes(RequestFilter[] filters) {
        DataCenterRoute[] out = {new DataCenterRoute(SCEPP),
                                 new DataCenterRoute(BUD),
                                 new DataCenterRoute(POND),
                                 new DataCenterRoute(SNEP)};
        MicroSecondDate BUD_CUTOFF = ClockUtil.now().subtract(BUD_OFFSET);
        for(int i = 0; i < filters.length; i++) {
            MicroSecondDate end = new MicroSecondDate(filters[i].end_time);
            if(filters[i].channel_id.network_id.network_code.equals("SP")
                    && end.after(SCEPP_TO_POND)) {
                logger.info("Request to SCEPP"
                        + ChannelIdUtil.toStringNoDates(filters[i].channel_id));
                out[0].add(filters[i]);
            } else if (filters[i].channel_id.network_id.network_code.equals("XE") &&
                    filters[i].channel_id.station_code.startsWith("SNP")){
                logger.info("Request of SNEP" 
                            + ChannelIdUtil.toStringNoDates(filters[i].channel_id));
                out[3].add(filters[i]);
            }   else {
                if(end.after(BUD_CUTOFF)) {
                    logger.info("Request to Bud"
                            + ChannelIdUtil.toStringNoDates(filters[i].channel_id));
                    out[1].add(filters[i]);
                } else {
                    logger.info("Request to Pond"
                            + ChannelIdUtil.toStringNoDates(filters[i].channel_id));
                    out[2].add(filters[i]);
                }
            }
        }
        return out;
    }

    public List getDataCenter(String networkCode) {
        LinkedList out = new LinkedList();
        if(networkCode.equals("SP")) {
            out.add(getSceppDC());
        } else if (networkCode.equals("XE")) {
            out.add(getSnepDC());
        } else {
            out.add(getBudDC());
            out.add(getPondDC());
        }
        return out;
    }

    public ProxySeismogramDC getDC(String serverName) {
        if(serverName == SCEPP) {
            return getSceppDC();
        } else if(serverName == BUD) {
            return getBudDC();
        } else if(serverName == POND) {
            return getPondDC();
        } else if(serverName == SNEP) {
            return getSnepDC();
        }else {
            throw new IllegalArgumentException("server " + serverName
                    + " not known.");
        }
    }

    protected ProxySeismogramDC getSceppDC() {
        logger.debug("Resolving Scepp DataCenter");
        while(sceppDC == null) {
            TimeInterval delay = sceppDCLoadTime.difference(ClockUtil.now());
            delay.convertTo(UnitImpl.SECOND);
            logger.debug("Resolving Scepp DataCenter " + delay);
            if(delay.getValue() > 10) {
                // max sleep is 10 seconds
                break;
            }
            try {
                Thread.sleep(500);
            } catch(InterruptedException e) {}
        }
        return sceppDC;
    }

    protected ProxySeismogramDC loadSceppDC() {
        if(sceppDC == null) {
            sceppDC = BulletproofVestFactory.vestSeismogramDC("edu/sc/seis",
                                                              "SCEPPSeismogramDC",
                                                              fissuresNamingService,
                                                              1);
        }
        return sceppDC;
    }

    protected ProxySeismogramDC getBudDC() {
        while(budDC == null) {
            logger.debug("Resolving Bud DataCenter");
            TimeInterval delay = budDCLoadTime.difference(ClockUtil.now());
            delay.convertTo(UnitImpl.SECOND);
            logger.debug("Resolving Bud DataCenter " + delay);
            if(delay.getValue() > 10) {
                // max sleep is 10 seconds
                break;
            }
            try {
                Thread.sleep(500);
            } catch(InterruptedException e) {}
        }
        return budDC;
    }

    protected ProxySeismogramDC loadBudDC() {
        if(budDC == null) {
            budDC = BulletproofVestFactory.vestSeismogramDC("edu/iris/dmc",
                                                            "IRIS_BudDataCenter",
                                                            fissuresNamingService,
                                                            1);
        }
        return budDC;
    }
    
    protected ProxySeismogramDC getSnepDC() {
        logger.debug("Resolving SNEP DataCenter");
        while(snepDC == null) {
            TimeInterval delay = snepDCLoadTime.difference(ClockUtil.now());
            delay.convertTo(UnitImpl.SECOND);
            logger.debug("Resolving SNEP DataCenter " + delay);
            if(delay.getValue() > 10) {
                // max sleep is 10 seconds
                break;
            }
            try {
                Thread.sleep(500);
            } catch(InterruptedException e) {}
        }
        return snepDC;
    }
    
    protected ProxySeismogramDC loadSnepDC() {
        if(snepDC == null) {
            snepDC = BulletproofVestFactory.vestSeismogramDC("edu/sc/seis/internal",
                                                             "SNEP",
                                                             fissuresNamingService,
                                                             1);
        }
        return snepDC;
    }

    protected ProxySeismogramDC getPondDC() {
        while(pondDC == null) {
            logger.debug("Resolving Pond DataCenter");
            TimeInterval delay = pondDCLoadTime.difference(ClockUtil.now());
            delay.convertTo(UnitImpl.SECOND);
            logger.debug("Resolving Pond DataCenter " + delay);
            if(delay.getValue() > 10) {
                // max sleep is 10 seconds
                break;
            }
            try {
                Thread.sleep(500);
            } catch(InterruptedException e) {}
        }
        return pondDC;
    }

    protected ProxySeismogramDC loadPondDC() {
        if(pondDC == null) {
            pondDC = BulletproofVestFactory.vestSeismogramDC("edu/iris/dmc",
                                                             "IRIS_PondDataCenter",
                                                             fissuresNamingService,
                                                             1);
        }
        return pondDC;
    }

    protected ProxySeismogramDC sceppDC = null;

    protected MicroSecondDate sceppDCLoadTime = null;

    protected ProxySeismogramDC budDC = null;

    protected MicroSecondDate budDCLoadTime = null;

    protected ProxySeismogramDC pondDC = null;

    protected MicroSecondDate pondDCLoadTime = null;
    
    protected ProxySeismogramDC snepDC = null;
    
    protected MicroSecondDate snepDCLoadTime = null;

    FissuresNamingService fissuresNamingService;

    protected class DataCenterRoute {

        DataCenterRoute(String serverName) {
            this.serverName = serverName;
            this.dc = getDC(serverName);
        }

        String getServerName() {
            return serverName;
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

        void reloadDataCenter() {
            dc = null;
            DCResolver resolve = new DCResolver(serverName);
            resolve.start();
            dc = getDC(serverName);
        }

        ProxySeismogramDC dc;

        String dns;

        String serverName;

        LinkedList filterList = new LinkedList();
    }

    protected class DCResolver extends Thread {

        DCResolver(String serverName) {
            super("DCResolver" + serverName);
            this.serverName = serverName;
            if(serverName.equals(SCEPP)) {
                sceppDC = null;
                sceppDCLoadTime = ClockUtil.now();
            } else if(serverName.equals(BUD)) {
                budDC = null;
                budDCLoadTime = ClockUtil.now();
            } else if(serverName.equals(POND)) {
                pondDC = null;
                pondDCLoadTime = ClockUtil.now();
            } else if (serverName.equals(SNEP)) {
                snepDC = null;
                snepDCLoadTime = ClockUtil.now();
            }
        }

        String serverName;

        public void run() {
            if(serverName.equals(SCEPP)) {
                loadSceppDC();
            } else if(serverName.equals(BUD)) {
                loadBudDC();
            } else if(serverName.equals(POND)) {
                loadPondDC();
            } else if (serverName.equals(SNEP)) {
                loadSnepDC();
            }
        }
    }

    static Logger logger = LoggerFactory.getLogger(HardCodeDataCenterRouter.class);

    public ProxySeismogramDC getDataCenter(RequestFilter[] infilters) {
        DataCenterRoute[] routes = makeRoutes(infilters);
        for(int i = 0; i < routes.length; i++) {
            if(routes[i].filterList.size() > 0) { return routes[i].dc; }
        }
        return getBudDC();
    }
}
