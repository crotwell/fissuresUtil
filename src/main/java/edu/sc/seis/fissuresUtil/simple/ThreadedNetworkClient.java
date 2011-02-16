package edu.sc.seis.fissuresUtil.simple;

import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.NetworkAccessHelper;

public class ThreadedNetworkClient extends SimpleNetworkClient {

    public ThreadedNetworkClient() {
        super();
    }

    public ThreadedNetworkClient(String networkCode, String serverDNS, String serverName) {
        super(networkCode, serverDNS, serverName);
    }

    public void exercise() {
        super.exercise();
        try {
            Tester.runAll(createRunnables());
        } catch(Throwable e) {
            e.printStackTrace();
        }
    }

    public Runnable[] createRunnables() {
        Runnable[] runnables = new Runnable[7];
        runnables[0] = new RetrieveForStation();
        runnables[1] = new RetrieveStations();
        runnables[2] = new GetAttributes();
        runnables[3] = new IORConverter();
        runnables[4] = new NetworkAccessNarrow();
        runnables[5] = new RetrieveChannel();
        runnables[6] = new LocateChannel();
        return runnables;
    }

    private class NetworkAccessNarrow implements Runnable {

        public void run() {
            String ior = Initializer.getORB().object_to_string((org.omg.CORBA.Object)net);
            org.omg.CORBA.Object obj = Initializer.getORB().string_to_object(ior);
            NetworkAccessHelper.narrow(obj);
        }

        public String toString() {
            return "NetworkAccess narrow";
        }
    }

    private class RetrieveForStation implements Runnable {

        public void run() {
            net.retrieve_for_station(testStation.get_id());
        }

        public String toString() {
            return "retrieve_for_station";
        }
    }

    private class GetAttributes implements Runnable {

        public void run() {
            get_attributes(false);
        }

        public String toString() {
            return "get_attributes";
        }
    }

    private class IORConverter implements Runnable {

        public void run() {
            String ior = Initializer.getORB().object_to_string((org.omg.CORBA.Object)net);
            org.omg.CORBA.Object obj = Initializer.getORB().string_to_object(ior);
            NetworkAccessHelper.narrow(obj);
        }

        public String toString() {
            return "IORConverter";
        }
    }

    private class RetrieveStations implements Runnable {

        public void run() {
            retrieve_stations(false);
        }

        public String toString() {
            return "retrieve_stations";
        }
    }

    private class RetrieveChannel implements Runnable {

        public void run() {
            try {
                net.retrieve_channel(testChannel.get_id());
            } catch(ChannelNotFound e) {
                System.err.println("This channel should always be found");
                e.printStackTrace();
                System.exit(1);
            }
        }

        public String toString() {
            return "retrieve_channel";
        }
    }

    private class LocateChannel implements Runnable {

        public void run() {
            try {
                explorer_locate_channels(true);
            } catch(Throwable e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        public String toString() {
            return "explorer_locate_chanels";
        }
    }

    private static Logger logger = LoggerFactory.getLogger(ThreadedNetworkClient.class);

    public static void main(String[] args) {
        BasicConfigurator.configure();
        Initializer.init(args);
        Properties props = Initializer.getProps();
        try {
            ThreadedNetworkClient client;
            if(props.containsKey("serverName") && props.containsKey("serverDNS")) {
                client = new ThreadedNetworkClient(props.getProperty("netCode", "II"),
                                               props.getProperty("serverDNS"),
                                               props.getProperty("serverName"));
            } else {
                client = new ThreadedNetworkClient();
            }
            Tester.runAll(client.createRunnables());
        } catch(Throwable e) {
            e.printStackTrace();
        }
    }
}
