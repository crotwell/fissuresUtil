
package edu.sc.seis.anhinga.event;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import edu.iris.Fissures.IfEvent.EventDC;
import edu.iris.Fissures.IfEvent.EventFactory;
import edu.iris.Fissures.IfEvent.EventFinder;
import edu.iris.Fissures.model.AllVTFactory;
import edu.sc.seis.anhinga.database.JDBCCatalog;
import edu.sc.seis.anhinga.database.JDBCContributor;
import edu.sc.seis.anhinga.database.JDBCEventAttr;
import edu.sc.seis.anhinga.database.JDBCFlinnEngdahl;
import edu.sc.seis.anhinga.database.JDBCLocation;
import edu.sc.seis.anhinga.database.JDBCLocator;
import edu.sc.seis.anhinga.database.JDBCMagnitude;
import edu.sc.seis.anhinga.database.JDBCOrigin;
import edu.sc.seis.anhinga.database.JDBCParameterRef;
import edu.sc.seis.anhinga.database.JDBCPick;
import edu.sc.seis.anhinga.database.JDBCPredictedArrival;
import edu.sc.seis.anhinga.database.JDBCQuantity;
import edu.sc.seis.anhinga.database.JDBCQuitTable;
import edu.sc.seis.anhinga.database.JDBCUnit;
import edu.sc.seis.anhinga.database.QuitChecker;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;

/**
 * EventAcessStart.java
 *
 *
 * Created: Fri Mar 30 11:34:15 2001
 *
 * @author Srinivasa Telukutla
 * @version
 */

public class EventStart  {

    public EventStart() {

    }
    public static void main(String[] args) {

        org.omg.CORBA_2_3.ORB orb = null;
        EventDCImpl ac_impl = null;
        EventDC ac = null;
        Connection dcConn = null;
        Connection checkQuitConn = null;
        JDBCQuitTable quit = null;
        QuitChecker quitChecker = null;
        FissuresNamingService fissuresNamingService = null;
        try {
            Properties props = System.getProperties();

            // get some defaults
            String propFilename=
                "event.prop";
            String defaultsFilename=
                "edu/sc/seis/anhinga/event/"+propFilename;

            for (int i=0; i<args.length-1; i++) {
                if (args[i].equals("-props")) {
                    // override with values in local directory,
                    // but still load defaults with original name
                    propFilename = args[i+1];
                }
            }

            try {
                props.load((EventStart.class).getClassLoader().getResourceAsStream( defaultsFilename ));
            } catch (IOException e) {
                System.err.println("Could not load defaults. "+e);
            }
            try {
                FileInputStream in = new FileInputStream(propFilename);
                props.load(in);
                in.close();
            } catch (FileNotFoundException f) {
                System.err.println(" file missing "+f+" using defaults");
            } catch (IOException f) {
                System.err.println(f.toString()+" using defaults");
            }

            // configure logging from properties...
            PropertyConfigurator.configure(props);
            logger.info("Logging configured");

            Class.forName("org.postgresql.Driver");

            String databaseURL = props.getProperty("DatabaseURL",
                                                   "jdbc:postgresql:sceppevents");
            String databaseName = props.getProperty("DatabaseName",
                                                    "scepp");
            String databasePassword = props.getProperty("DatabasePassword",
                                                        "");

            String quitDatabaseURL = props.getProperty("QuitDatabaseURL",
                                                       "jdbc:postgresql:sceppdata");
            String quitDatabaseName = props.getProperty("QuitDatabaseName",
                                                        "scepp");
            String quitDatabasePassword = props.getProperty("QuitDatabasePassword",
                                                            "");

            dcConn =
                DriverManager.getConnection(databaseURL,
                                            databaseName,
                                            databasePassword);
            checkQuitConn =
                DriverManager.getConnection(quitDatabaseURL,
                                            quitDatabaseName,
                                            quitDatabasePassword);

            if (dcConn == null) {
                throw new SQLException("connection is null");
            }

            JDBCParameterRef jdbcParameterRef = new JDBCParameterRef(dcConn);
            JDBCFlinnEngdahl jdbcFlinnEngdahl = new JDBCFlinnEngdahl(dcConn);
            JDBCEventAttr jdbcEventAttr = new JDBCEventAttr(dcConn,
                                                            jdbcParameterRef,
                                                            jdbcFlinnEngdahl);
            JDBCUnit jdbcUnit = new JDBCUnit(dcConn);
            JDBCQuantity jdbcQuantity = new JDBCQuantity(jdbcUnit);
            JDBCLocation jdbcLocation = new JDBCLocation(dcConn,jdbcQuantity);
            JDBCContributor jdbcContributor = new JDBCContributor(dcConn);
            JDBCMagnitude jdbcMagnitude = new JDBCMagnitude(dcConn, jdbcContributor);
            JDBCCatalog jdbcCatalog = new JDBCCatalog(dcConn, jdbcContributor);

            JDBCOrigin jdbcOrigin = new JDBCOrigin(dcConn,
                                                   jdbcLocation,
                                                   jdbcEventAttr,
                                                   jdbcParameterRef,
                                                   jdbcMagnitude,
                                                   jdbcCatalog);

            jdbcEventAttr.setJDBCOrigin(jdbcOrigin);


            JDBCPick jdbcPick = new JDBCPick(dcConn, jdbcQuantity);
            JDBCPredictedArrival jdbcPredictedArrival =
                new JDBCPredictedArrival(dcConn,
                                         jdbcParameterRef,
                                         jdbcQuantity);
            JDBCLocator jdbcLocator = new JDBCLocator(dcConn,
                                                      jdbcOrigin,
                                                      jdbcPick,
                                                      jdbcPredictedArrival);

            orb =
                (org.omg.CORBA_2_3.ORB)ORB.init(args, props);




            // register valuetype factories
            edu.iris.Fissures.model.AllVTFactory vt = new AllVTFactory();
            vt.register(orb);

            //
            // Resolve Root POA
            //
            org.omg.PortableServer.POA rootPOA =
                org.omg.PortableServer.POAHelper.narrow(
                orb.resolve_initial_references("RootPOA"));

            //
            // Get a reference to the POA manager
            //
            org.omg.PortableServer.POAManager manager =
                rootPOA.the_POAManager();
            org.omg.CORBA.Policy[] policy_list = new org.omg.CORBA.Policy[5];

            policy_list[0] = rootPOA.create_lifespan_policy( org.omg.PortableServer.LifespanPolicyValue.PERSISTENT);

            policy_list[1] = rootPOA.create_id_assignment_policy( org.omg.PortableServer.IdAssignmentPolicyValue.USER_ID);
            policy_list[2] = rootPOA.create_implicit_activation_policy(
                org.omg.PortableServer.ImplicitActivationPolicyValue.
                    NO_IMPLICIT_ACTIVATION);
            policy_list[3] = rootPOA.create_request_processing_policy(
                org.omg.PortableServer.RequestProcessingPolicyValue.
                    USE_SERVANT_MANAGER);
            policy_list[4] = rootPOA.create_servant_retention_policy(
                org.omg.PortableServer.ServantRetentionPolicyValue.NON_RETAIN);

            // Create a POA for the EventFinder_impl.
            org.omg.PortableServer.POA finder_poa =
                rootPOA.create_POA("finder", manager, policy_list);
            //NetworkExplorer explorer =
            // explorerImpl._this(orb);
            //  NetworkFinder finder =
            //    finderImpl._this(orb);

            ac_impl = new EventDCImpl();



            EventFactoryImpl factoryImpl = new EventFactoryImpl( jdbcParameterRef,
                                                                jdbcEventAttr,
                                                                jdbcFlinnEngdahl,
                                                                finder_poa
                                                               );

            EventFinderImpl finderImpl = new EventFinderImpl( jdbcEventAttr,
                                                             jdbcOrigin,
                                                             jdbcLocator,
                                                             jdbcLocation,
                                                             jdbcCatalog,
                                                             finder_poa
                                                            );

            EventFinder finder = finderImpl._this(orb);
            EventFactory factory = factoryImpl._this(orb);
            finderImpl.setEventFinder(finder);
            finderImpl.setEventFactory(factory);
            factoryImpl.setEventFactory(factory);
            factoryImpl.setEventFinder(finder);
            //ac_impl.setEventFactory(factoryImpl._this(orb));
            ac_impl.setEventFinder(finder);

            EventLocator_impl eimpl = new EventLocator_impl(jdbcEventAttr, 2,
                                                            jdbcOrigin, jdbcLocator,
                                                            jdbcLocation,
                                                            jdbcCatalog,
                                                            finder_poa);
            org.omg.PortableServer.ServantManager locator = eimpl._this(orb);

            // Set servant locator.
            finder_poa.set_servant_manager(locator);

            ac = ac_impl._this(orb);

            serviceName = System.getProperty("anhinga.eventDC.serverName", serviceName);
            serviceDNS = System.getProperty("anhinga.eventDC.serverDNS", serviceDNS);

            fissuresNamingService = new FissuresNamingService(orb);

            String addNS = System.getProperty("anhinga.additionalNameService");
            if (addNS != null) {
                fissuresNamingService.addOtherNameServiceCorbaLoc(addNS);
            }

            fissuresNamingService.rebind(serviceDNS, serviceName, ac);

            // register under old alternate name for backwards
            // compatibility
            serviceNameAlt = System.getProperty("anhinga.eventDC.serverNameAlt", serviceName);
            if (serviceNameAlt != null) {
                fissuresNamingService.rebind(serviceDNS, serviceNameAlt, ac);
            }

            logger.info("Bound to Name Service");

            logger.info("will run until quit.val is set to true in DB. ");
            quit = new JDBCQuitTable(checkQuitConn,
                                     serviceDNS+"/"+serviceName+":"+edu.iris.Fissures.VERSION.value);
            quitChecker = new QuitChecker(quit, orb);
            quitChecker.start();

            //
            // Run implementation
            //
            manager.activate();
            orb.run();

        } catch (org.omg.PortableServer.POAManagerPackage.AdapterInactive e) {
            logger.error("POA problem.", e);
        } catch (org.omg.CORBA.ORBPackage.InvalidName e) {
            logger.error("Naming problem.", e);
        } catch (org.omg.CosNaming.NamingContextPackage.InvalidName e) {
            logger.error("Naming problem.", e);
        } catch (org.omg.CosNaming.NamingContextPackage.NotFound e) {
            logger.error("Naming problem.", e);
        } catch (CannotProceed e) {
            logger.error("Naming problem.", e);
        } catch (SQLException e) {
            logger.error("SQL problem with connection??? ", e);
        } catch (ClassNotFoundException e) {
            logger.error("Couldn't load postgres driver. ", e);
        } catch(Exception e) {

            logger.error("Couldn't load postgres driver. ", e);

        }finally {
            // unregister with name service
            try {
                if( orb != null && fissuresNamingService != null) {
                    fissuresNamingService.unbind(serviceDNS, "EventDC", serviceName);
                    if (serviceNameAlt != null) {
                        fissuresNamingService.unbind(serviceDNS, "EventDC", serviceNameAlt);
                    }
                }
            } catch (Exception e) {
                // who cares
            } // end of try-catch


            // make sure to stop the servant
            if (quitChecker != null) {
                try {
                    quit.setQuitTime();
                } catch (SQLException e) {
                    // who cares
                }
                quitChecker.stopThread();
            }

            try {
                if (dcConn != null) dcConn.close();
            } catch (SQLException e) {
                // who cares???
            }
            try {
                if (checkQuitConn != null) checkQuitConn.close();
            } catch (SQLException e) {
                // who cares???
            }
        }
        logger.info("EventStart done.");
    }

    static String serviceName = "SCEPPEventDC";
    static String serviceNameAlt = null;
    static String serviceDNS = "edu/sc/seis/example";

    private static Category logger =
        Category.getInstance(EventStart.class.getName());

} // EventAcessStart
