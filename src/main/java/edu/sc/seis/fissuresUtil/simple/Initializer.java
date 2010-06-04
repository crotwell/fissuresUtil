package edu.sc.seis.fissuresUtil.simple;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
                try {
                    props = loadProperties(args);
                } catch(IOException e) {
                    System.err.println("Using props as the props file can't be loaded: "
                            + e.getMessage());
                }
                if (props != null && props.containsKey("log4j.rootCategory")) {
                    /** Configure log4j, not required for DHI, but is useful. */
                    // BasicConfigurator.configure();
                    PropertyConfigurator.configure(props);
                    logger.info("Logging configured");
                }
                /*
                 * Initialize the ORB. This must be done before the corba system
                 * can be used. Parameters passed in via the args and props
                 * configure the ORB. Consult the docummentation for your orb
                 * for more information.
                 */
                orb = (org.omg.CORBA_2_3.ORB)org.omg.CORBA.ORB.init(args, props);
                logger.info("orb initialized, class="
                        + orb.getClass().getName());
                registerValuetypes(orb);
                fisName = createNamingService(orb, props);
            }
        }
    }

    public static FissuresNamingService createNamingService(org.omg.CORBA_2_3.ORB orb,
                                                            Properties props) {
        /*
         * Here we pick a name server to connect to. These are two choices for
         * the IRIS DMC and USC SCEPP, others may exist. Port 6371 are used by
         * both USC and the DMC, but this is not required.
         */
        FissuresNamingService ns = new FissuresNamingService(orb);
        logger.info("create fisName helper with orb");
        ns.setNameServiceCorbaLoc(props.getProperty(FissuresNamingService.CORBALOC_PROP,
                                                    "corbaloc:iiop:dmc.iris.washington.edu:6371/NameService"));
        ns.getNameService();
        logger.info("got fis name service");
        return ns;
    }

    public static void registerValuetypes(org.omg.CORBA_2_3.ORB orb) {
        /*
         * Valuetypes are corba objects that are sent "over the wire" and need a
         * factory to handle the unmarshalling on the client side, so that the
         * correct object is created locally. The AllVTFactory.register method
         * registers factories for all of the IDL defined valuetypes found in
         * the fissuresImpl package.
         */
        AllVTFactory vt = new AllVTFactory();
        vt.register(orb);
        logger.info("register valuetype factories");
    }

    // DESPAIR at the VERY THOUGHT of stumping APOP(Advanced Prop Option Parser)
    // Actually, despair that you have reached this juncture where 7 equivalent
    // options are accepted
    public static final String[] POSSIBLE_PROP_OPTION_NAMES = new String[] {"-p",
                                                                            "-props",
                                                                            "-prop",
                                                                            "-properties",
                                                                            "--prop",
                                                                            "--props",
                                                                            "--properties"};

    public static Properties loadProperties(String[] args) throws IOException {
        return loadProperties(args, System.getProperties());
    }

    public static Properties loadProperties(String[] args, Properties baseProps)
            throws IOException {
        return loadProperties(args, baseProps, true);
    }

    /**
     * If chatty is true, notify std out for every prop file loaded
     */
    public static Properties loadProperties(String[] args, Properties baseProps, boolean chatty)
            throws IOException {
        for(int i = 0; i < args.length - 1; i++) {
            for(int j = 0; j < POSSIBLE_PROP_OPTION_NAMES.length; j++) {
                String propFilename = args[i + 1];
                if(args[i].equals(POSSIBLE_PROP_OPTION_NAMES[j])) {
                    // override with values in local directory,
                    // but still load defaults with original name
                    loadProps(new FileInputStream(propFilename), baseProps);
                    if(chatty){
                        System.out.println("loaded props file from " + args[i + 1]);
                    }
                }
            }
        }
        return baseProps;
    }

    public static org.omg.CORBA_2_3.ORB getORB() {
        if(orb == null)
            init(EMPTY_ARGS);
        return orb;
    }

    public static FissuresNamingService getNS() {
        if(fisName == null)
            init(EMPTY_ARGS);
        return fisName;
    }

    public static Properties getProps() {
        return props;
    }

    public static void loadProps(InputStream propStream, Properties baseProps)
            throws IOException {
        baseProps.load(propStream);
        propStream.close();
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
        // IRIS
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