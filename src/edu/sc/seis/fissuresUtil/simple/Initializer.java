/**
 * AbstractClient.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.simple;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.model.AllVTFactory;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public abstract class Initializer{
    public static void init(String[] args) {
        synchronized(initLock){
            if(fisName == null){
                loadProperties(args);

                /** Configure log4j, not required for DHI, but is useful. */
                //  BasicConfigurator.configure();
                PropertyConfigurator.configure(props);
                logger.info("Logging configured");

                /* Initialize the ORB. This must be done before the corba system
                 * can be used. Parameters passed in via the args and props
                 * configure the ORB. Consult the docummentation for your orb
                 *for more information.
                 */
                orb = (org.omg.CORBA_2_3.ORB)org.omg.CORBA.ORB.init(args, props);
                logger.info("orb initialized, class="+orb.getClass().getName());

                /* Valuetypes are corba objects that are sent "over the wire"
                 *and need a factory to handle the unmarshalling on the client
                 * side, so that the correct object is created locally. The
                 * AllVTFactory.register method registers factories for all of
                 *the IDL defined valuetypes found in the fissuresImpl package.
                 */
                AllVTFactory vt = new AllVTFactory();
                vt.register(orb);
                logger.info("register valuetype factories");

                /* Here we pick a name server to connect to. These are two
                 * choices for the IRIS DMC and USC SCEPP, others may exist.
                 * Port 6371 are used by both USC and the DMC, but this is not
                 * required.
                 */
                fisName = new FissuresNamingService(orb);
                logger.info("create fisName helper with orb");

                //fisName.setNameServiceCorbaLoc("corbaloc:iiop:dmc.iris.washington.edu:6371/NameService");
                fisName.setNameServiceCorbaLoc(props.getProperty("edu.sc.seis.fissuresUtil.nameServiceCorbaLoc",
                                                                 "corbaloc:iiop:pooh.seis.sc.edu:6371/NameService"));
                //fisName.setNameServiceCorbaLoc("corbaloc:iiop:sob.iris.washington.edu:6371/NameService");
                fisName.getNameService();
                logger.info("got fis name service");
            }
        }
    }

    private static void loadProperties(String[] args) {
        String propFilename;
        props = System.getProperties();
        for (int i=0; i<args.length-1; i++) {
            if (args[i].equals("-props")) {
                propFilename = args[i+1];
                try {
                    FileInputStream in = new FileInputStream(propFilename);
                    props.load(in);
                    in.close();
                } catch (FileNotFoundException f) {
                    System.err.println(" file missing "+f+" using defaults");
                } catch (IOException f) {
                    System.err.println(f.toString()+" using defaults");
                }
            }
        }
    }

    public static org.omg.CORBA_2_3.ORB getORB(){
        if(orb == null) init(EMPTY_ARGS);
        return orb;
    }

    public static FissuresNamingService getNS(){
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

    public static final NetworkId fakeNet = new NetworkId("II", new Time("19861024000000.0000GMT", 0));
    public static final StationId fakeStation = new StationId(fakeNet, "AAK", new Time("19901012000000.0000GMT", 0));
    public static final ChannelId fakeChan = new ChannelId(fakeNet, "AAK", "  ", "BHE", new Time("19901012000000.0000GMT", 0));
}
