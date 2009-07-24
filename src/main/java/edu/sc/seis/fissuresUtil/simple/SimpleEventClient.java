package edu.sc.seis.fissuresUtil.simple;

import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfEvent.EventAccess;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.EventDC;
import edu.iris.Fissures.IfEvent.EventFinder;
import edu.iris.Fissures.IfEvent.EventSeqIterHolder;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.PointDistanceAreaImpl;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;

public class SimpleEventClient implements TestingClient {

    public SimpleEventClient() {
        this("edu/iris/dmc", "IRIS_EventDC");
    }

    /**
     * <pre>
     *      iris
     *      serverDNS=edu/iris/dmc
     *      serverName = IRIS_EventDC
     * 
     *      Berkeley
     *      serverDNS=edu/berkeley/geo/quake
     *      serverName = NCEDC_EventDC
     *      or
     *      serverName = NCSN_DataCenter
     * 
     *      South Carolina (SCEPP)
     *      serverDNS=edu/sc/seis
     *      serverName = SCEPPEventDC
     * </pre>
     */
    public SimpleEventClient(String serverDNS, String serverName) {
        try {
            this.serverDNS = serverDNS;
            this.serverName = serverName;
            /*
             * This step is not required, but sometimes helps to determine if a
             * server is down. if this call succedes but the next fails, then
             * the nameing service is up and functional, but the network server
             * is not reachable for some reason.
             */
            Initializer.getNS().getEventDCObject(serverDNS, serverName);
            logger.info("Got EventDC as corba object, the name service is ok");
            /*
             * This connectts to the actual server, as oposed to just getting
             * the reference to it. The naming convention is that the first part
             * is the reversed DNS of the organization and the second part is
             * the individual server name. The dmc lists their servers under the
             * edu/iris/dmc and their main network server is IRIS_EventDC.
             */
            EventDC eventDC = Initializer.getNS().getEventDC(serverDNS,
                                                             serverName);
            logger.info("got EventDC");
            /*
             * The EventFinder is one of the choices at this point. It allows
             * you to query for individual events, and then retrieve information
             * about them.
             */
            finder = eventDC.a_finder();
            logger.info("got EventFinder");
        } catch(org.omg.CosNaming.NamingContextPackage.InvalidName e) {
            logger.error("Problem with name service: ", e);
        } catch(NotFound e) {
            logger.error("Problem with name service: ", e);
        } catch(CannotProceed e) {
            logger.error("Problem with name service: ", e);
        }
    }

    public void exercise() {
        EventAccess[] events = query_events(true);
        for(int i = 0; i < events.length; i++) {
            get_attributes(events[i]);
            try {
                get_preferred_origin(events[i], true);
            } catch(NoPreferredOrigin e) {
                logger.warn("No preferred origin for event " + i, e);
            }
        }
    }

    /**
     * Runs a sample query against the EventFinder that is created in the
     * constructor of this class
     */
    public EventAccess[] query_events() {
        return query_events(false);
    }

    /**
     * Runs a sample query against the EventFinder that is created in the
     * constructor of this class
     * 
     * @verbose - if true, some information about the events is printed
     */
    public EventAccess[] query_events(boolean verbose) {
        EventSeqIterHolder iter = new EventSeqIterHolder();
        MicroSecondDate end = ClockUtil.now();
        MicroSecondDate start = end.subtract(ONE_DAY);

        if(serverName.equals("NCSN_EventDC") || serverName.equals("Memphis")) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            cal.set(2000, 0, 7);
            start = new MicroSecondDate(cal.getTime());
            cal.set(2000, 0, 9);
            end = new MicroSecondDate(cal.getTime());
        }
        TimeRange timeRange = new TimeRange(start.getFissuresTime(),
                                  end.getFissuresTime());
        String[] magTypes = {};
        String[] catalogs = {};
        String[] contributors = {};
        EventAccess[] events = finder.query_events(new PointDistanceAreaImpl(0, -180, new QuantityImpl(0, UnitImpl.DEGREE), 
                                                                             new QuantityImpl(10, UnitImpl.DEGREE)),
                                                   new QuantityImpl(0,
                                                                    UnitImpl.KILOMETER),
                                                   new QuantityImpl(1000,
                                                                    UnitImpl.KILOMETER),
                                                   timeRange,
                                                   magTypes,
                                                   1.0f,
                                                   10.0f,
                                                   catalogs,
                                                   contributors,
                                                   500,
                                                   iter);
        if (events.length == 0) {
            logger.warn("*** Warning, no events returned for query. ***");
        }
        if(verbose)
            logger.info("Got " + events.length + " events.");
        return events;
    }

    public Origin get_preferred_origin(EventAccess ev) throws NoPreferredOrigin {
        return get_preferred_origin(ev, false);
    }

    public Origin get_preferred_origin(EventAccess ev, boolean verbose)
            throws NoPreferredOrigin {
        Origin o = ev.get_preferred_origin();
        if(o.getMagnitudes().length == 0) {
            logger.error("Origin has zero magnitudes: "
                    + o.getOriginTime().date_time + " at ("
                    + o.getLocation().latitude + ", " + o.getLocation().longitude
                    + ") " + "with depth of " + o.getLocation().depth.value + " "
                    + o.getLocation().depth.the_units);
        } else {
            if(verbose)
                logger.info("Event occurred at " + o.getOriginTime().date_time
                        + " mag=" + o.getMagnitudes()[0].type + " "
                        + o.getMagnitudes()[0].value + " at ("
                        + o.getLocation().latitude + ", "
                        + o.getLocation().longitude + ") " + "with depth of "
                        + o.getLocation().depth.value + " "
                        + o.getLocation().depth.the_units);
        }
        return o;
    }

    public EventAttr get_attributes(EventAccess ea) {
        return ea.get_attributes();
    }

    protected EventFinder finder;
    String serverDNS;
    String serverName;
    
    private static final TimeInterval ONE_DAY = new TimeInterval(1,
                                                                 UnitImpl.DAY);

    private static Logger logger = Logger.getLogger(SimpleEventClient.class);

    public static void main(String[] args) {
        BasicConfigurator.configure();
        Initializer.init(args);
        Properties props = Initializer.getProps();
        try {
            SimpleEventClient client;
            if(props.containsKey("serverName")
                    && props.containsKey("serverDNS")) {
                client = new SimpleEventClient(props.getProperty("serverDNS"),
                                               props.getProperty("serverName"));
            } else {
                client = new SimpleEventClient();
            }
            client.exercise();
        } catch(Throwable e) {
            e.printStackTrace();
        }
    }
}
