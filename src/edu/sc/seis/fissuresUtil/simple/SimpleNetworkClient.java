/**
 * SimpleNetworkClient.java
 *
 * @author Philip Crotwell
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
    
    /** A very simple client that shows how to connect to a DHI NetworkDC
     *  and retrieve some stations. All the code is left in the main() for
     *  readability. This does not fully exercise the features available within
     *  the Network data service, but once this example is understood, the
     *  javadocs provide information on the other methods available.
     *
     */
    public static void main(String[] args) {
        Properties props = System.getProperties();
        
        /** Configure log4j, not required for DHI, but is useful. */
        BasicConfigurator.configure();
        logger.info("Logging configured");
        
        /* Initialize the ORB. This must be done before the corba system can
         * be used. Parameters passed in via the args and props configure the
         * ORB. Consult the docummentation for your orb for more information. */
        org.omg.CORBA_2_3.ORB orb =
            (org.omg.CORBA_2_3.ORB)org.omg.CORBA.ORB.init(args, props);
        logger.info("orb initialized");
        
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
        FissuresNamingService fisName = new FissuresNamingServiceImpl(orb);
        //fisName.setNameServiceCorbaLoc("corbaloc:iiop:dmc.iris.washington.edu:6371/NameService");
        fisName.setNameServiceCorbaLoc("corbaloc:iiop:pooh.seis.sc.edu:6371/NameService");
        logger.info("got fis name service");
        
        /** We will try to get data from the II network. */
        String networkCode = "II";
        
        try {
            /** This step is not required, but sometimes helps to determine if
             *  a server is down. if this call succedes but the next fails, then
             *  the nameing service is up and functional, but the network server
             *  is not reachable for some reason. */
            Object obj = fisName.getNetworkDCObject("edu/iris/dmc",
                                                    "IRIS_NetworkDC");
            logger.info("Got as corba object, the name service is ok");
            
            /** This connectts to the actual server, as oposed to just getting
             *  the reference to it. The naming convention is that the first
             *  part is the reversed DNS of the organization and the second part
             *  is the individual server name. The dmc lists their servers under
             *  the edu/iris/dmc and their main network server is IRIS_NetworkDC.*/
            NetworkDC netDC = fisName.getNetworkDC("edu/iris/dmc",
                                                   "IRIS_NetworkDC");
            logger.info("got NetworkDC");
            
            /** The NetworkFinder is one of the choices at this point. It
             *  allows you to find individual networks, and then retrieve
             *  information about them. */
            NetworkFinder finder = netDC.a_finder();
            logger.info("got NetworkFinder");
            
            /** Get the NetworkAccess for the II station. The NetworkAccess
             *  represents the II network, but is still a remote (corba) object.
             */
            NetworkAccess[] net = finder.retrieve_by_code(networkCode);
            logger.info("got NetworkAccess for "+networkCode);
            
            /** The NetworkAttr has basic information about the network, like
             *  its name, id, owner, etc. */
            NetworkAttr netAttr = net[0].get_attributes();
            logger.info("got NetworkAttr for "+networkCode);
            
            logger.info("Network "+netAttr.get_code()+
                            " retrieved, owner="+netAttr.owner+
                            " desc="+netAttr.description);
            
            /** We can also retrieve the actual stations for this network.
             *  The station array is composed of local objects, so there is
             *  no internet connections once they have been retrieved. */
            Station[] stations = net[0].retrieve_stations();
            logger.info("Received "+stations.length+" stations from "+networkCode);
            for (int i = 0; i < stations.length; i++) {
                logger.info(stations[i].get_code()+"  "+stations[i].name+
                                " ("+stations[i].my_location.latitude+
                                ", "+stations[i].my_location.longitude+")");
            }
            
            /** Here are someof the possible problems that can occur. */
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
        /** All done... */
    }
    
    static Logger logger = Logger.getLogger(SimpleNetworkClient.class);
}

