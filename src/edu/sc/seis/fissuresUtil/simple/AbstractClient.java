/**
 * AbstractClient.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.simple;

import edu.iris.Fissures.model.AllVTFactory;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingServiceImpl;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

public abstract class AbstractClient {

    public static void init(String[] args) {
        Properties props = initProperties(args);

        /* Check for properties on the command line and load them if they exist. */

        /** Configure log4j, not required for DHI, but is useful. */
        BasicConfigurator.configure();
        logger.info("Logging configured");

        /* Initialize the ORB. This must be done before the corba system can
         * be used. Parameters passed in via the args and props configure the
         * ORB. Consult the docummentation for your orb for more information. */
        orb = (org.omg.CORBA_2_3.ORB)org.omg.CORBA.ORB.init(args, props);
        logger.info("orb initialized, class="+orb.getClass().getName());

        /* Valuetypes are corba objects that are sent "over the wire" and need
         * a factory to handle the unmarshalling on the client side, so that the
         * correct object is created locally. The AllVTFactory.register method
         * registers factories for all of the IDL defined valuetypes found in
         * the fissuresImpl package. */
        AllVTFactory vt = new AllVTFactory();
        vt.register(orb);
        logger.info("register valuetype factories");

        /* Here we pick a name server to connect to. These are two choices for
         * the IRIS DMC and USC SCEPP, others may exist. Port 6371 are used by
         * both USC and the DMC, but this is not required.*/
        fisName = new FissuresNamingServiceImpl(orb);
        logger.info("create fisName helper with orb");

        //fisName.setNameServiceCorbaLoc("corbaloc:iiop:dmc.iris.washington.edu:6371/NameService");
        fisName.setNameServiceCorbaLoc("corbaloc:iiop:pooh.seis.sc.edu:6371/NameService");
        //try {
        NamingContext nc = fisName.getNameService();

        logger.info("got fis name service");
    }

    static Properties initProperties(String[] args) {
        String propFilename;
        Properties props = System.getProperties();
        for (int i=0; i<args.length-1; i++) {
            if (args[i].equals("-props")) {
                propFilename = args[i+1];
                try {
                    FileInputStream in = new FileInputStream(propFilename);
                    props.load(in);
                    in.close();
                } catch (FileNotFoundException f) {
                    //logger.warn
                    System.err.println(" file missing "+f+" using defaults");
                } catch (IOException f) {
                    //logger.warn
                    System.err.println(f.toString()+" using defaults");
                }
            }
        }
        return props;
    }

    static org.omg.CORBA_2_3.ORB orb;

    static FissuresNamingServiceImpl fisName;

    static Logger logger = Logger.getLogger(AbstractClient.class);

}

