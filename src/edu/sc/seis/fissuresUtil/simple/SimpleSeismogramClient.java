/**
 * SimpleSeismogramClient.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.simple;
import org.apache.log4j.Logger;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import edu.iris.Fissures.IfSeismogramDC.DataCenter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.FissuresException;



public class SimpleSeismogramClient extends AbstractClient {


    /**
     *
     */
    public static void main(String[] args) throws FissuresException {

        /* Initializes the corba orb, finds the naming service and other startup
         * tasks. See AbstractClient for the code in this method. */
        init(args);

        try {
            /** This step is not required, but sometimes helps to determine if
             *  a server is down. if this call succedes but the next fails, then
             *  the nameing service is up and functional, but the network server
             *  is not reachable for some reason. */
            Object obj = fisName.getSeismogramDCObject("edu/iris/dmc",
                                                       "IRIS_BudDataCenter");
            logger.info("Got as corba object, the name service is ok");

            /** This connectts to the actual server, as oposed to just getting
             *  the reference to it. The naming convention is that the first
             *  part is the reversed DNS of the organization and the second part
             *  is the individual server name. The dmc lists their servers under
             *  the edu/iris/dmc and their main network server is IRIS_EventDC.*/
            DataCenter seisDC = fisName.getSeismogramDC("edu/iris/dmc",
                                                        "IRIS_BudDataCenter");
            logger.info("got SeisDC");

            // we will get data for 1 hour ago until now
            MicroSecondDate now = ClockUtil.now();
            MicroSecondDate hourAgo = now.subtract(new TimeInterval(1, UnitImpl.HOUR));

            // ideally the channel id would come from the network server, but this
            // one will work with the Bud server, unless that station is down
            ChannelId channelId = new ChannelId(new NetworkId("IU", now.getFissuresTime()),
                                                "ANMO",
                                                "00",
                                                "BHZ",
                                                now.getFissuresTime());

            // construct the request filters to send to the server
            RequestFilter[] request = new RequestFilter[1];
            request[0] = new RequestFilter(channelId,
                                           hourAgo.getFissuresTime(),
                                           now.getFissuresTime());

            // retrieve the data
            LocalSeismogram[] seis = seisDC.retrieve_seismograms(request);

            // print out some simple information about the data
            logger.info("Got "+seis.length+" seismograms.");
            for (int i = 0; i < seis.length; i++) {
                logger.info("Seismogram "+i+" has "+seis[i].num_points+
                                " points and starts at "+seis[i].begin_time.date_time);
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

    static Logger logger = Logger.getLogger(SimpleSeismogramClient.class);

}

