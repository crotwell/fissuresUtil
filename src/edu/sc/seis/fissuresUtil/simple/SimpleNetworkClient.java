package edu.sc.seis.fissuresUtil.simple;

import edu.iris.Fissures.IfNetwork.*;

import org.apache.log4j.Logger;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.iris.Fissures.network.NetworkIdUtil;

public class SimpleNetworkClient implements TestingClient {
    public SimpleNetworkClient(){
        // We will try to get data from the II network.
        String networkCode = "II";

        try {
            /* This step is not required, but sometimes helps to determine if
             *  a server is down. if this call succedes but the next fails, then
             *  the nameing service is up and functional, but the network server
             *  is not reachable for some reason.
             */
            Initializer.getNS().getNetworkDCObject("edu/iris/dmc",
                                                   "IRIS_NetworkDC");
            logger.info("Got network as corba object, the name service is ok");

            /* This connectts to the actual server, as oposed to just getting
             *  the reference to it. The naming convention is that the first
             *  part is the reversed DNS of the organization and the second part
             *  is the individual server name. The dmc lists their servers under
             *  the edu/iris/dmc and their main network server is IRIS_NetworkDC.
             */
            NetworkDC netDC = Initializer.getNS().getNetworkDC("edu/iris/dmc",
                                                               "IRIS_NetworkDC");
            logger.info("got NetworkDC");

            /* The NetworkFinder is one of the choices at this point. It
             *  allows you to find individual networks, and then retrieve
             *  information about them.
             */
            NetworkFinder finder = netDC.a_finder();
            logger.info("got NetworkFinder");

            /* Get the NetworkAccess for the II station. The NetworkAccess
             *  represents the II network, but is still a remote (corba) object.
             */
            NetworkAccess[] nets = finder.retrieve_by_code(networkCode);
            logger.info("got NetworkAccess for "+networkCode);
            net = nets[0];

            /** get all stations and print their codes. Note that their might
                be several stations with the same code, but different effective
                times*/
            Station[] stations = net.retrieve_stations();
            logger.info("Threre are "+stations.length+" stations in "+networkCode);
            for (int i = 0; i < stations.length; i++) {
                logger.info("got station "+stations[i].get_code());
            }

            /** Get all the channels for the first station and print their codes.
             *  Note that there may be several channels with the same code but
             *  different effective times.*/
            Channel[] channels = net.retrieve_for_station(stations[0].get_id());
            logger.info("There are "+channels.length+" channels for "+stations[0].get_code());
            for (int i = 0; i < channels.length; i++) {
                logger.info("got channel "+channels[i].my_site.get_code()+"."+channels[i].get_code());
            }

            /** get all the networks and print their code. */
            nets = finder.retrieve_all();
            logger.info("There are "+nets.length+" networks");
            for (int i = 0; i < nets.length; i++) {
                logger.info("net "+i+" "+NetworkIdUtil.toString(nets[i].get_attributes().get_id()));
            }

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

    public void exercise() {
        get_attributes(true);
        retrieve_stations(true);
    }

    /** This retrieves the attributes for the network gotten in the constructor.
     * The attributes contain basic information about the network, like its
     * name, id, owner, etc.
     */
    public NetworkAttr get_attributes(){ return get_attributes(false); }

    public NetworkAttr get_attributes(boolean verbose){
        NetworkAttr attr = net.get_attributes();
        if(verbose) logger.info("Network "+attr.get_code()+
                                    " retrieved, owner="+attr.owner+
                                    " desc="+attr.description);
        return attr;
    }

    /** We can also retrieve the actual stations for this network.
     *  The station array is composed of local objects, so there is
     *  no internet connections once they have been retrieved. */
    public Station[] retrieve_stations(){ return retrieve_stations(false); }

    public Station[] retrieve_stations(boolean verbose){
        Station[] stations = net.retrieve_stations();
        if(verbose){
            logger.info("Received "+stations.length+" stations");
            for (int i = 0; i < stations.length; i++) {
                logger.info(stations[i].get_code()+"  "+stations[i].name+
                                " ("+stations[i].my_location.latitude+
                                ", "+stations[i].my_location.longitude+")");
            }
        }
        return stations;
    }

    protected NetworkAccess net;
    private static Logger logger = Logger.getLogger(SimpleNetworkClient.class);

    /** A very simple client that shows how to connect to a DHI NetworkDC
     *  and retrieve some stations.  The constructor connects to a single
     * network.  Calling exercise on the constructed object runs a few methods
     * on that network to show some of its functionality.
     */
    public static void main(String[] args) {
        /* Initializes the corba orb, finds the naming service and other startup
         * tasks. See Initializer for the code in this method. */
        Initializer.init(args);
        new SimpleNetworkClient().exercise();
    }
}
