/**
 * SimpleNetworkClient.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.fissuresUtil.simple;

import edu.iris.Fissures.IfNetwork.*;

import edu.iris.Fissures.model.AllVTFactory;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingServiceImpl;
import java.util.Properties;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

public class SimpleNetworkClient extends AbstractClient {

    /** A very simple client that shows how to connect to a DHI NetworkDC
     *  and retrieve some stations. All the code is left in the main() for
     *  readability. This does not fully exercise the features available within
     *  the Network data service, but once this example is understood, the
     *  javadocs provide information on the other methods available.
     *
     */
    public static void main(String[] args) {
        /* Initializes the corba orb, finds the naming service and other startup
         * tasks. See AbstractClient for the code in this method. */
        init(args);

        /** We will try to get data from the II network. */
        String networkCode = "II";

        try {
            /** This step is not required, but sometimes helps to determine if
             *  a server is down. if this call succedes but the next fails, then
             *  the nameing service is up and functional, but the network server
             *  is not reachable for some reason. */
            Object obj = fisName.getNetworkDCObject("edu/iris/dmc",
                                                    "IRIS_NetworkDC");
            logger.info("Got as corba object, the name service is ok");

            /** This connectts to the actual server, as oposed to just getting
             *  the reference to it. The naming convention is that the first
             *  part is the reversed DNS of the organization and the second part
             *  is the individual server name. The dmc lists their servers under
             *  the edu/iris/dmc and their main network server is IRIS_NetworkDC.*/
            NetworkDC netDC = fisName.getNetworkDC("edu/iris/dmc",
                                                   "IRIS_NetworkDC");
            logger.info("got NetworkDC");

            /** The NetworkFinder is one of the choices at this point. It
             *  allows you to find individual networks, and then retrieve
             *  information about them. */
            NetworkFinder finder = netDC.a_finder();
            logger.info("got NetworkFinder");

            /** Get the NetworkAccess for the II station. The NetworkAccess
             *  represents the II network, but is still a remote (corba) object.
             */
            NetworkAccess[] net = finder.retrieve_by_code(networkCode);
            logger.info("got NetworkAccess for "+networkCode);

            /** The NetworkAttr has basic information about the network, like
             *  its name, id, owner, etc. */
            NetworkAttr netAttr = net[0].get_attributes();
            logger.info("got NetworkAttr for "+networkCode);

            logger.info("Network "+netAttr.get_code()+
                            " retrieved, owner="+netAttr.owner+
                            " desc="+netAttr.description);

            /** We can also retrieve the actual stations for this network.
             *  The station array is composed of local objects, so there is
             *  no internet connections once they have been retrieved. */
            Station[] stations = net[0].retrieve_stations();
            logger.info("Received "+stations.length+" stations from "+networkCode);
            for (int i = 0; i < stations.length; i++) {
                logger.info(stations[i].get_code()+"  "+stations[i].name+
                                " ("+stations[i].my_location.latitude+
                                ", "+stations[i].my_location.longitude+")");
            }

            /** Here are someof the possible problems that can occur. */
        }catch (NetworkNotFound e) {
            logger.error("Network "+networkCode+" was not found", e);
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

    static Logger logger = Logger.getLogger(SimpleNetworkClient.class);
}

