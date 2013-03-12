package edu.sc.seis.fissuresUtil.simple;

import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import edu.iris.Fissures.Dimension;
import edu.iris.Fissures.Plottable;
import edu.iris.Fissures.IfPlottable.PlottableDCOperations;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.RequestFilterUtil;
import edu.sc.seis.fissuresUtil.cache.BulletproofVestFactory;

public class SimplePlottableClient implements TestingClient {

    public SimplePlottableClient() {
        String serverDNS;
        String serverName;
        // Delilah BUD Plottable Server
        serverDNS = "edu/sc/seis";
        // serves only cached plottable data
        
        
        serverName = "DelilahCache";
        // goes to BUD to get any non-cached plottable data
        // serverName = "Delilah";
        try {
            /*
             * This step is not required, but sometimes helps to determine if a
             * server is down. if this call succedes but the next fails, then
             * the nameing service is up and functional, but the network server
             * is not reachable for some reason.
             */
            Initializer.getNS().getPlottableDCObject(serverDNS, serverName);
            logger.info("Got PlottableDC as corba object. the name service is ok");
            /*
             * This connects to the actual server, as opposed to just getting
             * the reference to it. The naming convention is that the first part
             * is the reversed DNS of the organization and the second part is
             * the individual server name. The dmc lists their servers under the
             * edu/iris/dmc and their main network server is IRIS_EventDC.
             */
            plottableDC = BulletproofVestFactory.vestPlottableDC(serverDNS,
                                                                 serverName,
                                                                 Initializer.getNS());
            logger.info("got PlottableDC "+(plottableDC!=null));
        } catch(org.omg.CosNaming.NamingContextPackage.InvalidName e) {
            logger.error("Problem with name service: ", e);
        } catch(NotFound e) {
            logger.error("Problem with name service: ", e);
        } catch(CannotProceed e) {
            logger.error("Problem with name service: ", e);
        }
    }

    public void exercise() {
        retrieve_plottables(true);
    }

    public void retrieve_plottables() {
        retrieve_plottables(false);
    }

    public Plottable[] retrieve_plottables(boolean verbose) {
        try {
            RequestFilter rf = createLast24HoursRF();
            Dimension[] widths = plottableDC.get_whole_day_sizes();
            if(verbose) {
                logger.info("plottableDC has the following whole-day sizes:");
                for(int i = 0; i < widths.length; i++) {
                    logger.info("" + widths[i].width);
                }
                logger.info("requesting plottable at " + widths[0].width
                        + " pixels wide " + RequestFilterUtil.toString(rf));
            }
            Plottable[] plots = plottableDC.get_plottable(rf, widths[0]);
            if(verbose) {
                printPlotResults(plots);
            }
            return plots;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void printPlotResults(Plottable[] plots) {
        logger.info("got " + plots.length + " plottables from plottableDC");
        for(int i = 0; i < plots.length; i++) {
            logger.info("plottable " + i + " has " + plots[i].x_coor.length
                    + " pixels " + "and spans between pixels "
                    + plots[i].x_coor[0] + " and "
                    + plots[i].x_coor[plots[i].x_coor.length - 1]);
        }
    }

    public RequestFilter createLast24HoursRF() {
        MicroSecondDate now = new MicroSecondDate();
        MicroSecondDate yesterday = now.subtract(new TimeInterval(1.0,
                                                                  UnitImpl.DAY));
        return SimpleSeismogramClient.createRF(yesterday, now)[0];
    }

    public static void main(String[] args) {
        /*
         * Initializes the corba orb, finds the naming service and other startup
         * tasks. See Initializer for the code in this method.
         */
        Initializer.init(args);
        new SimplePlottableClient().exercise();
    }

    protected PlottableDCOperations plottableDC;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SimplePlottableClient.class);
}
