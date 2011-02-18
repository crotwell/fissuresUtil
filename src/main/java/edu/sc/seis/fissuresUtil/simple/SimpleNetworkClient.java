package edu.sc.seis.fissuresUtil.simple;

import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.iris.Fissures.Area;
import edu.iris.Fissures.Orientation;
import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.Sampling;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelIdIterHolder;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.NetworkDC;
import edu.iris.Fissures.IfNetwork.NetworkFinder;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.IfNetwork.OrientationRange;
import edu.iris.Fissures.IfNetwork.SamplingRange;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.model.BoxAreaImpl;
import edu.iris.Fissures.model.GlobalAreaImpl;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.OrientationRangeImpl;
import edu.iris.Fissures.network.SamplingRangeImpl;
import edu.iris.Fissures.network.StationIdUtil;

public class SimpleNetworkClient implements TestingClient {

    //IRIS
    public SimpleNetworkClient() {
        this("II", "edu/iris/dmc", "IRIS_NetworkDC");
    }

    // Berkeley
    //public SimpleNetworkClient(){ this("NC", "edu/berkeley/geo/quake",
    // "NCEDC_NetworkDC"); }
    //South Carolina (SCEPP)
    //public SimpleNetworkClient() {
    //    this("SP", "edu/sc/seis", "SCEPPNetworkDC");
    //}
    public SimpleNetworkClient(String networkCode, String serverDNS,
            String serverName) {
        try {
            /*
             * This step is not required, but sometimes helps to determine if a
             * server is down. if this call succedes but the next fails, then
             * the nameing service is up and functional, but the network server
             * is not reachable for some reason.
             */
            Initializer.getNS().getNetworkDCObject(serverDNS, serverName);
            logger.info("Got network as corba object, the name service is ok");
            /*
             * This connectts to the actual server, as oposed to just getting
             * the reference to it. The naming convention is that the first part
             * is the reversed DNS of the organization and the second part is
             * the individual server name. The dmc lists their servers under the
             * edu/iris/dmc and their main network server is IRIS_NetworkDC.
             */
            netDC = Initializer.getNS().getNetworkDC(serverDNS,
                                                               serverName);
            logger.info("got NetworkDC");
            /*
             * The NetworkFinder is one of the choices at this point. It allows
             * you to find individual networks, and then retrieve information
             * about them.
             */
            NetworkFinder finder = netDC.a_finder();
            logger.info("got NetworkFinder");
            /*
             * Get the NetworkAccess for the II station. The NetworkAccess
             * represents the II network, but is still a remote (corba) object.
             */
            NetworkAccess[] nets = finder.retrieve_by_code(networkCode);
            net = nets[0];
            /**
             * get all stations and print their codes. Note that their might be
             * several stations with the same code, but different effective
             * times
             */
                        Station[] stations = net.retrieve_stations();
            logger.info("Threre are " + stations.length + " stations in "
                    + networkCode);
                        testStation = stations[0];
            /**
             * Get all the channels for the first station and print their codes.
             * Note that there may be several channels with the same code but
             * different effective times.
             */
                        Channel[] channels =
             net.retrieve_for_station(stations[0].get_id());
                        logger.info("There are " + channels.length + " channels for "
                                + stations[0].get_code());
                        testChannel = channels[0];
            /** get all the networks and print their code. */
            //nets = finder.retrieve_all();
            //logger.info("There are "+nets.length+" networks");
            //for (int i = 0; i < nets.length; i++) {
            //    logger.info("net "+i+"
            // "+NetworkIdUtil.toString(nets[i].get_attributes().get_id()));
            //}
        } catch(NetworkNotFound e) {
            logger.error("Network " + networkCode + " was not found", e);
        }catch(org.omg.CosNaming.NamingContextPackage.InvalidName e) {
            logger.error("Problem with name service: ", e);
        } catch(NotFound e) {
            logger.error("Problem with name service: ", e);
        } catch(CannotProceed e) {
            logger.error("Problem with name service: ", e);
        }
    }

    public void exercise() {
        get_attributes(true);
        retrieve_stations(true);
        locate_channels(true);
        explorer_locate_channels(true);
    }

    /**
     * This retrieves the attributes for the network gotten in the constructor.
     * The attributes contain basic information about the network, like its
     * name, id, owner, etc.
     */
    public NetworkAttr get_attributes() {
        return get_attributes(false);
    }

    public NetworkAttr get_attributes(boolean verbose) {
        NetworkAttr attr = net.get_attributes();
        if(verbose) logger.info("Network " + attr.get_code()
                + " retrieved, owner=" + attr.getOwner() + " desc="
                + attr.getDescription());
        return attr;
    }

    /**
     * We can also retrieve the actual stations for this network. The station
     * array is composed of local objects, so there is no internet connections
     * once they have been retrieved.
     */
    public Station[] retrieve_stations() {
        return retrieve_stations(false);
    }

    public Station[] retrieve_stations(boolean verbose) {
        Station[] stations = net.retrieve_stations();
        if(verbose) {
            logger.info("Received " + stations.length + " stations");
            for(int i = 0; i < stations.length; i++) {
                logger.info(stations[i].get_code() + "  " + stations[i].getName()
                        + " (" + stations[i].getLocation().latitude + ", "
                        + stations[i].getLocation().longitude + ")");
            }
        }
        Channel[] chan = net.retrieve_for_station(stations[0].get_id());
        logger.info("retrieve_for_station("+StationIdUtil.toString(stations[0].get_id())+")");
        return stations;
    }

    public Channel[] locate_channels(boolean verbose) {
        TimeInterval oneSecond = new TimeInterval(1, UnitImpl.SECOND);
        Sampling minSample = new SamplingImpl(1, oneSecond);
        Sampling maxSample = new SamplingImpl(100, oneSecond);
        SamplingRange sampleRange = new SamplingRangeImpl(minSample, maxSample);
        Orientation orient = new Orientation(0, -90);
        Quantity angle = new QuantityImpl(45, UnitImpl.DEGREE);
        OrientationRange orientationRange = new OrientationRangeImpl(orient,
                                                                     angle);
        Area area = new GlobalAreaImpl();
        area = new BoxAreaImpl(0, 90, 0, 90);
        Channel[] chans = net.locate_channels(area,
                                              sampleRange,
                                              orientationRange);
        if(verbose) {
            logger.info("Received " + chans.length + " channels by location");
        }
        return chans;
    }

    public ChannelId[] explorer_locate_channels(boolean verbose) {
        TimeInterval oneSecond = new TimeInterval(1, UnitImpl.SECOND);
        Sampling minSample = new SamplingImpl(1, oneSecond);
        Sampling maxSample = new SamplingImpl(100, oneSecond);
        SamplingRange sampleRange = new SamplingRangeImpl(minSample, maxSample);
        Orientation orient = new Orientation(0, -90);
        Quantity angle = new QuantityImpl(45, UnitImpl.DEGREE);
        OrientationRange orientationRange = new OrientationRangeImpl(orient,
                                                                     angle);
        Area area = new GlobalAreaImpl();
        area = new BoxAreaImpl(0, 90, -130, -90);
        ChannelId[] chans = netDC.a_explorer().locate_channels(area,
                                              sampleRange,
                                              orientationRange,
                                              1000,
                                              new ChannelIdIterHolder());
        if(verbose) {
            logger.info("Received " + chans.length + " channels by explorer.locate_channels");
        }
        return chans;
    }


    protected Channel testChannel;

    protected Station testStation;

    protected NetworkAccess net;

    protected NetworkDC netDC;
    
    private static Logger logger = LoggerFactory.getLogger(SimpleNetworkClient.class);

    /**
     * A very simple client that shows how to connect to a DHI NetworkDC and
     * retrieve some stations. The constructor connects to a single network.
     * Calling exercise on the constructed object runs a few methods on that
     * network to show some of its functionality.
     */
    public static void main(String[] args) {
        /*
         * Initializes the corba orb, finds the naming service and other startup
         * tasks. See Initializer for the code in this method.
         */
        Initializer.init(args);
        new SimpleNetworkClient().exercise();
    }
}
