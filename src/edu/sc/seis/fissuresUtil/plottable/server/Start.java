package edu.sc.seis.fissuresUtil.plottable.server;

import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import edu.iris.Fissures.IfPlottable.PlottableDC;
import edu.iris.Fissures.model.AllVTFactory;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import edu.sc.seis.fissuresUtil.simple.Initializer;

public class Start {

    public static void main(String[] args) {
        Properties props = Initializer.loadProperties(args);
        // configure logging from properties...
        PropertyConfigurator.configure(props);
        FissuresNamingService fisName;
        String name = props.getProperty("fisUtil.plottableDC.serverName");
        String dns = props.getProperty("fisUtil.plottableDC.serverDNS");
        String corbaLoc = props.getProperty("edu.sc.seis.fissuresUtil.nameServiceCorbaLoc");
        if(name == null || dns == null || corbaLoc == null) {
            System.out.println("Unable to load name, dns or corbaloc!");
            System.exit(1);
        }
        try {
            // Initialize the ORB.
            orb = (org.omg.CORBA_2_3.ORB)org.omg.CORBA.ORB.init(args, props);
            // register valuetype factories
            new AllVTFactory().register(orb);
            fisName = new FissuresNamingService(orb);
            fisName.setNameServiceCorbaLoc(corbaLoc);
            PlottableDC pl = new PlottableImpl()._this(orb);
            fisName.rebind(dns, name, pl);
            logger.info("Bound to Name Service");
            // Resolve Root POA
            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            // Get a reference to the POA manager
            logger.info("NOW RUN THE ORB");
            org.omg.PortableServer.POAManager manager = rootPOA.the_POAManager();
            manager.activate();
            orb.run();
        } catch(org.omg.CosNaming.NamingContextPackage.NotFound e) {
            logger.fatal("Couldn!t bind to the naming service", e);
        } catch(org.omg.PortableServer.POAManagerPackage.AdapterInactive e) {
            logger.fatal("POA problem.", e);
        } catch(org.omg.CORBA.ORBPackage.InvalidName e) {
            logger.fatal("Naming problem.", e);
        } catch(Throwable e) {
            logger.fatal("Couldn't... ", e);
        } finally {
            // unregister with name service
            try {
                if(orb != null) {
                    fisName = new FissuresNamingService(orb);
                    fisName.unbind(dns, "PlottableDC", name);
                }
            } catch(Exception e) {
                // who cares
            } // end of try-catch
        }
        logger.info("PlottableStart done.");
    }

    protected static org.omg.CORBA_2_3.ORB orb;

    private static final Logger logger = Logger.getLogger(Start.class);
} // PlottableStart
