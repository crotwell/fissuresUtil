/**
 * SimpleNetworkClient.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.simple;

import edu.iris.Fissures.IfNetwork.*;

import edu.iris.Fissures.model.AllVTFactory;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingServiceImpl;
import java.util.Properties;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

public class SimpleNetworkClient {
    
    /**
     *
     */
    public static void main(String[] args) {
        Properties props = System.getProperties();
        BasicConfigurator.configure();
        logger.info("Logging configured");
        
        // Initialize the ORB.
        org.omg.CORBA_2_3.ORB orb =
            (org.omg.CORBA_2_3.ORB)org.omg.CORBA.ORB.init(args, props);
        logger.info("orb initialized");
        
        // register valuetype factories
        AllVTFactory vt = new AllVTFactory();
        vt.register(orb);
        logger.info("register valuetype factories");
        
        FissuresNamingService fisName = new FissuresNamingServiceImpl(orb);
        //fisName.setNameServiceCorbaLoc("corbaloc:iiop:dmc.iris.washington.edu:6371/NameService");
        fisName.setNameServiceCorbaLoc("corbaloc:iiop:pooh.seis.sc.edu:6371/NameService");
        logger.info("got fis name service");
        
        try {
            Object obj = fisName.getNetworkDCObject("edu/iris/dmc",
                                                    "IRIS_NetworkDC");
            logger.info("Got as corba object");
            
            NetworkDC netDC = fisName.getNetworkDC("edu/iris/dmc",
                                                   "IRIS_NetworkDC");
            logger.info("got NetworkDC");
            
            NetworkFinder finder = netDC.a_finder();
            logger.info("got NetworkFinder");
            
            String networkCode = "II";
            NetworkAccess[] net = finder.retrieve_by_code(networkCode);
            logger.info("got NetworkAccess for "+networkCode);
            
            NetworkAttr netAttr = net[0].get_attributes();
            logger.info("got NetworkAttr for "+networkCode);
            
            logger.info("Network "+netAttr.get_code()+
                            " retrieved, owner="+netAttr.owner+
                            " desc="+netAttr.description);
            
            Station[] stations = net[0].retrieve_stations();
            logger.info("Received "+stations.length+" stations from "+networkCode);
            for (int i = 0; i < stations.length; i++) {
                logger.info(stations[i].get_code()+"  "+stations[i].name);
            }
        }catch (NetworkNotFound e) {
            logger.error("Network II was not found", e);
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
    
    static Logger logger = Logger.getLogger(SimpleNetworkClient.class);
}

