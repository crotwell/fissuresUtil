/**
 * MultiThreadNetworkClient.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.simple;

import edu.iris.Fissures.IfNetwork.*;

import org.apache.log4j.Logger;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

public class MultiThreadNetworkClient extends AbstractClient {



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
            NetworkAccess[] nets = finder.retrieve_by_code(networkCode);
            logger.info("got NetworkAccess for "+networkCode);

            MultiThreadNetworkClient client = new MultiThreadNetworkClient();
            net = nets[0];
            client.go();

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

    }

    void go() {
        int numThreads = 2;
        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < threads.length; i++) {
            Runnable doit = new MyRun(i);
            threads[i] = new Thread(doit);
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }
    }

    class MyRun implements Runnable {
        public MyRun(int i) {
            this.runNum = i;
            stations = net.retrieve_stations();
        }
        int runNum;
        Station[] stations;
        public void run() {

            for (int i = 0; i < 20; i++) {
                //                String ior = orb.object_to_string((org.omg.CORBA.Object)net);
                //                org.omg.CORBA.Object obj = orb.string_to_object(ior);
                //                NetworkAccess tmp = NetworkAccessHelper.narrow(obj);
                //NetworkAttr attr = net.get_attributes();
                //Station[] stations = net.retrieve_stations();
                StationId fake = stations[0].get_id();
                Channel[] chan = net.retrieve_for_station(fake);
                //for (int j = 0; j < stations.length; j++) {
                //  Channel[] chan = net.retrieve_for_station(stations[j].get_id());
                //}
                logger.debug("run "+runNum+" getting  "+i+"th time ");
            }
        }
    }

    static NetworkAccess net = null;

    private static Logger logger = Logger.getLogger(MultiThreadNetworkClient.class);

}

