package edu.sc.seis.fissuresUtil.serverTest;
import edu.iris.Fissures.IfNetwork.*;

import edu.iris.Fissures.Time;
import org.apache.log4j.Logger;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

public class ThreadedNetClient extends AbstractThreadedClient{
    public ThreadedNetClient(){ this(EMPTY_ARGS); }
    
    public ThreadedNetClient(String[] args){
        init(args);
        String networkCode = "II";
        try {
            NetworkDC netDC = fisName.getNetworkDC("edu/iris/dmc",
                                                   "IRIS_NetworkDC");
            logger.info("got NetworkDC");
            NetworkFinder finder = netDC.a_finder();
            logger.info("got NetworkFinder");
            NetworkAccess[] nets = finder.retrieve_by_code(networkCode);
            logger.info("got NetworkAccess for "+networkCode);
            net = nets[0];
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
    
    public Runnable[] createRunnables() {
        Runnable[] runnables = new Runnable[5];
        runnables[0] = new Repeater(new RetrieveForStation());
        runnables[1] = new Repeater(new GetAttributes());
        runnables[2] = new Repeater(new IORConverter());
        runnables[3] = new Repeater(new RetrieveStations());
        runnables[4] = new Repeater(new RetrieveChannel());
        return runnables;
    }
    
    private class RetrieveForStation implements Runnable{
        public void run() {  net.retrieve_for_station(fakeStation); }
        
        public String toString(){ return "retrieve_for_station"; }
    }
    
    private class GetAttributes implements Runnable{
        public void run() { net.get_attributes(); }
        
        public String toString(){ return "get_attributes"; }
    }
    
    private class IORConverter implements Runnable{
        public void run() {
            String ior = orb.object_to_string((org.omg.CORBA.Object)net);
            org.omg.CORBA.Object obj = orb.string_to_object(ior);
            NetworkAccessHelper.narrow(obj);
        }
        
        public String toString(){ return "IORConverter"; }
    }
    
    private class RetrieveStations implements Runnable{
        public void run() { net.retrieve_stations(); }
        
        public String toString(){ return "retrieve_stations"; }
    }
    
    private class RetrieveChannel implements Runnable{
        public void run() {
            try {
                net.retrieve_channel(fakeChan);
            } catch (ChannelNotFound e) {
                System.err.println("This channel should always be found");
                e.printStackTrace();
                System.exit(1);
            }
        }
        
        public String toString(){ return "retrieve_channel"; }
    }
    
    private NetworkId fakeNet = new NetworkId("II", new Time("19861024000000.0000GMT", 0));
    private StationId fakeStation = new StationId(fakeNet, "AAK", new Time("19901012000000.0000GMT", 0));
    private ChannelId fakeChan = new ChannelId(fakeNet, "AAK", "  ", "BHE", new Time("19901012000000.0000GMT", 0));
    private NetworkAccess net = null;
    
    private static final String[] EMPTY_ARGS = {};
    private static Logger logger = Logger.getLogger(ThreadedNetClient.class);
    
    public static void main(String[] args){
        new ThreadedNetClient(args).runAll();
    }
}
