/**
 * AbstractClient.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.fissuresUtil.simple;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.model.AllVTFactory;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;

public abstract class Initializer {

    public static void init(String[] args) {
        synchronized(initLock) {
            if(fisName == null) {
                props = loadProperties(args);
                /** Configure log4j, not required for DHI, but is useful. */
                //BasicConfigurator.configure();
                PropertyConfigurator.configure(props);
                logger.info("Logging configured");
                /*
                 * Initialize the ORB. This must be done before the corba system
                 * can be used. Parameters passed in via the args and props
                 * configure the ORB. Consult the docummentation for your orb
                 * for more information.
                 */
                orb = (org.omg.CORBA_2_3.ORB)org.omg.CORBA.ORB.init(args, props);
                logger.info("orb initialized, class="
                        + orb.getClass().getName());
                /*
                 * Valuetypes are corba objects that are sent "over the wire"
                 * and need a factory to handle the unmarshalling on the client
                 * side, so that the correct object is created locally. The
                 * AllVTFactory.register method registers factories for all of
                 * the IDL defined valuetypes found in the fissuresImpl package.
                 */
                AllVTFactory vt = new AllVTFactory();
                vt.register(orb);
                logger.info("register valuetype factories");
                /*
                 * Here we pick a name server to connect to. These are two
                 * choices for the IRIS DMC and USC SCEPP, others may exist.
                 * Port 6371 are used by both USC and the DMC, but this is not
                 * required.
                 */
                fisName = new FissuresNamingService(orb);
                logger.info("create fisName helper with orb");
                fisName.setNameServiceCorbaLoc(props.getProperty("edu.sc.seis.fissuresUtil.nameServiceCorbaLoc",
                                                                 "corbaloc:iiop:dmc.iris.washington.edu:6371/NameService"));
                fisName.getNameService();
                logger.info("got fis name service");
            }
        }
    }

    public static Properties loadProperties(String[] args) {
        String propFilename;
        Properties props = System.getProperties();
        for(int i = 0; i < args.length - 1; i++) {
            if(args[i].equals("-props")) {
                propFilename = args[i + 1];
                try {
                    FileInputStream in = new FileInputStream(propFilename);
                    props.load(in);
                    in.close();
                } catch(FileNotFoundException f) {
                    System.err.println(" file missing " + f + " using defaults");
                } catch(IOException f) {
                    System.err.println(f.toString() + " using defaults");
                }
            }
        }
        return props;
    }

    public static org.omg.CORBA_2_3.ORB getORB() {
        if(orb == null) init(EMPTY_ARGS);
        return orb;
    }

    public static FissuresNamingService getNS() {
        if(fisName == null) init(EMPTY_ARGS);
        return fisName;
    }

    public static Properties getProps() {
        return props;
    }

    private static Properties props;

    private static org.omg.CORBA_2_3.ORB orb;

    private static FissuresNamingService fisName;

    private static Object initLock = new Object();

    private static final String[] EMPTY_ARGS = {};

    private static Logger logger = Logger.getLogger(Initializer.class);

    public static final NetworkId IU = new NetworkId("IU",
                                                     new Time("19981026200000.0000GMT",
                                                              0));

    public static final StationId AMNO = new StationId(IU,
                                                       "ANMO",
                                                       new Time("19981026200000.0000GMT",
                                                                0));

    public static final ChannelId AMNOChannel = new ChannelId(IU,
                                                              "ANMO",
                                                              "00",
                                                              "BH1",
                                                              new Time("19981026200000.0000GMT",
                                                                       0));

    public static final NetworkId SP = new NetworkId("SP",
                                                     new Time("20001209T01:00:00.000Z",
                                                              22));

    public static final StationId ANDY = new StationId(SP,
                                                       "ANDY",
                                                       new Time("20001209T01:00:00.000Z",
                                                                22));

    public static final ChannelId ANDYChannel = new ChannelId(SP,
                                                              "ANDY",
                                                              "00",
                                                              "BHE",
                                                              new Time("20001209T01:00:00.000Z",
                                                                       22));

    public static final NetworkId fakeNet;

    public static StationId fakeStation;

    public static ChannelId fakeChan;
    static {
        //IRIS
        fakeNet = new NetworkId("II", new Time("19861024000000.0000GMT", 0));
        fakeStation = new StationId(fakeNet,
                                    "AAK",
                                    new Time("19901012000000.0000GMT", 0));
        fakeChan = new ChannelId(fakeNet,
                                 "AAK",
                                 "00",
                                 "BHE",
                                 new Time("19901012000000.0000GMT", 0));
    }
}