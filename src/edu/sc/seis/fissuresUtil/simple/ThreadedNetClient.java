package edu.sc.seis.fissuresUtil.simple;

import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.NetworkAccessHelper;
import org.apache.log4j.Logger;
import edu.iris.Fissures.IfNetwork.NetworkAccess;

public class ThreadedNetClient extends SimpleNetworkClient{
    public void exercise(){
        super.exercise();
        Tester.runAll(createRunnables());
    }

    public Runnable[] createRunnables() {
        Runnable[] runnables = new Runnable[6];
        runnables[0] = new NetworkAccessNarrow();
        runnables[1] = new RetrieveForStation();
        runnables[2] = new GetAttributes();
        runnables[3] = new IORConverter();
        runnables[4] = new RetrieveStations();
        runnables[5] = new RetrieveChannel();
        return runnables;
    }

    private class NetworkAccessNarrow implements Runnable{
        public void run() {
            String ior = Initializer.getORB().object_to_string((org.omg.CORBA.Object)net);
            org.omg.CORBA.Object obj = Initializer.getORB().string_to_object(ior);
            NetworkAccess temp = NetworkAccessHelper.narrow(obj); }

        public String toString(){ return "NetworkAccess narrow"; }
    }

    private class RetrieveForStation implements Runnable{
        public void run() { net.retrieve_for_station(Initializer.fakeStation); }

        public String toString(){ return "retrieve_for_station"; }
    }

    private class GetAttributes implements Runnable{
        public void run() { get_attributes(false); }

        public String toString(){ return "get_attributes"; }
    }

    private class IORConverter implements Runnable{
        public void run() {
            String ior = Initializer.getORB().object_to_string((org.omg.CORBA.Object)net);
            org.omg.CORBA.Object obj = Initializer.getORB().string_to_object(ior);
            NetworkAccessHelper.narrow(obj);
        }

        public String toString(){ return "IORConverter"; }
    }

    private class RetrieveStations implements Runnable{
        public void run() { retrieve_stations(false); }

        public String toString(){ return "retrieve_stations"; }
    }

    private class RetrieveChannel implements Runnable{
        public void run() {
            try {
                net.retrieve_channel(Initializer.fakeChan);
            } catch (ChannelNotFound e) {
                System.err.println("This channel should always be found");
                e.printStackTrace();
                System.exit(1);
            }
        }

        public String toString(){ return "retrieve_channel"; }
    }

    private static Logger logger = Logger.getLogger(ThreadedNetClient.class);

    public static void main(String[] args){
        Initializer.init(args);
        Tester.runAll(new ThreadedNetClient().createRunnables());
    }
}
