/**
 * SimpleEventClient.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.simple;

import edu.iris.Fissures.IfEvent.*;

import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.model.GlobalAreaImpl;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import org.apache.log4j.Logger;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

public class SimpleEventClient extends AbstractClient {

    /**
     *
     */
    public static void main(String[] args) {

        /* Initializes the corba orb, finds the naming service and other startup
         * tasks. See AbstractClient for the code in this method. */
        init(args);

        try {
            /** This step is not required, but sometimes helps to determine if
             *  a server is down. if this call succedes but the next fails, then
             *  the nameing service is up and functional, but the network server
             *  is not reachable for some reason. */
            Object obj = fisName.getEventDCObject("edu/iris/dmc",
                                                  "IRIS_EventDC");
            logger.info("Got as corba object, the name service is ok");

            /** This connectts to the actual server, as oposed to just getting
             *  the reference to it. The naming convention is that the first
             *  part is the reversed DNS of the organization and the second part
             *  is the individual server name. The dmc lists their servers under
             *  the edu/iris/dmc and their main network server is IRIS_EventDC.*/
            EventDC eventDC = fisName.getEventDC("edu/iris/dmc",
                                                 "IRIS_EventDC");
            logger.info("got EventDC");

            /** The EventFinder is one of the choices at this point. It
             *  allows you to query for individual events, and then retrieve
             *  information about them. */
            EventFinder finder = eventDC.a_finder();
            logger.info("got EventFinder");

            MicroSecondDate now = ClockUtil.now();
            MicroSecondDate yesterday = now.subtract(new TimeInterval(7, UnitImpl.DAY));
            TimeRange timeRange = new TimeRange(yesterday.getFissuresTime(),
                                                now.getFissuresTime());
            String[] magTypes = new String[1];
            magTypes[0] = "ALL";
            String[] catalogs = new String[1];
            catalogs[0] = "FINGER";
            String[] contributors = new String[1];
            contributors[0] = "NEIC";

            EventSeqIterHolder iter = new EventSeqIterHolder();
            EventAccess[] events =
                finder.query_events(new GlobalAreaImpl(),
                                    new QuantityImpl(0, UnitImpl.KILOMETER),
                                    new QuantityImpl(1000, UnitImpl.KILOMETER),
                                    timeRange,
                                    magTypes,
                                    5.0f,
                                    10.0f,
                                    catalogs,
                                    contributors,
                                    500,
                                    iter);
            logger.info("Got "+events.length+" events.");

            for (int i = 0; i < events.length; i++) {
                EventAttr attr = events[i].get_attributes();
                try {
                    Origin origin = events[i].get_preferred_origin();
                    logger.info("Event "+i+" occurred in FE region "+attr.region.number+
                                    " at "+origin.origin_time.date_time+
                                    " mag="+origin.magnitudes[0].type+" "+origin.magnitudes[0].value+
                                    " at ("+origin.my_location.latitude+", "+origin.my_location.longitude+") "+
                                    "with depth of "+origin.my_location.depth.value+" "+origin.my_location.depth.the_units);
                } catch (NoPreferredOrigin e) {
                    logger.warn("No preferred origin for event "+i, e);
                }
            }

            /** Here are someof the possible problems that can occur. */
        }catch (org.omg.CORBA.ORBPackage.InvalidName e) {
            logger.error("Problem with name service: ", e);
        }catch (org.omg.CosNaming.NamingContextPackage.InvalidName e) {
            logger.error("Problem with name service: ", e);
        }catch (NotFound e) {
            logger.error("Problem with name service: ", e);
        }catch (CannotProceed e) {
            logger.error("Problem with name service: ", e);
        }
        /** All done... */
    }

    static Logger logger = Logger.getLogger(SimpleEventClient.class);

}

