package edu.sc.seis.fissuresUtil.simple;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfSeismogramDC.DataCenter;
import edu.iris.Fissures.IfSeismogramDC.DataCenterOperations;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import org.apache.log4j.Logger;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import edu.sc.seis.fissuresUtil.cache.BulletproofVestFactory;



public class SimpleSeismogramClient implements TestingClient{
    public SimpleSeismogramClient(){
        String serverDNS;
        String serverName;

        // iris
        serverDNS="edu/iris/dmc";
        serverName = "IRIS_BudDataCenter";
        // or
        //serverName = "IRIS_PondDataCenter";
        // or
        //serverName = "IRIS_ArchiveDataCenter";

        // Berkeley
        //serverDNS="edu/berkeley/geo/quake";
        //serverName = "NCEDC_DataCenter";

        // South Carolina (SCEPP)
        //serverDNS="edu/sc/seis";
        //serverName=""; <-- fix name later
        try {
            /* This step is not required, but sometimes helps to determine if
             *  a server is down. if this call succedes but the next fails, then
             *  the nameing service is up and functional, but the network server
             *  is not reachable for some reason.
             */
            Initializer.getNS().getSeismogramDCObject(serverDNS, serverName);
            logger.info("Got SeisDC as corba object, the name service is ok");

            /* This connects to the actual server, as opposed to just getting
             *  the reference to it. The naming convention is that the first
             *  part is the reversed DNS of the organization and the second part
             *  is the individual server name. The dmc lists their servers under
             *  the edu/iris/dmc and their main network server is IRIS_EventDC.
             */
            seisDC =  BulletproofVestFactory.vestSeismogramDC(serverDNS, serverName, Initializer.getNS());
            logger.info("got SeisDC");
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

    public void exercise() {
        //queuedRetrieve();
        retrieve_seismograms(true);
    }

    public void queuedRetrieve(String name) {
        try {
            String id = seisDC.queue_seismograms(createOldRF());
            logger.info("got id " + id + " for " + name);
            String status = seisDC.request_status(id);
            while(status.equals("Processing")){
                status = seisDC.request_status(id);
                logger.info("Status is " + status + " for " + name);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {}
            }
            logger.info("FINISHED " + name);
            logger.info(name + " status is " + status);
            if(status.equals("Finished")){
                LocalSeismogram[] seis = seisDC.retrieve_queue(id);
                for (int i = 0; i < seis.length; i++) {
                    System.out.println(name + " of " + i + " is " +seis[i].num_points+
                                           " points and starts at "+seis[i].begin_time.date_time);
                }
            }
        } catch (FissuresException e) {
            logger.info(name + " threw exception");
            e.printStackTrace();
        }
    }

    public LocalSeismogram[] retrieve_seismograms(){
        return retrieve_seismograms(false);
    }

    public LocalSeismogram[] retrieve_seismograms(boolean verbose){
        try {
            LocalSeismogram[] seis = seisDC.retrieve_seismograms(createRF());
            if(verbose){
                logger.info("Got "+seis.length+" seismograms.");
                for (int i = 0; i < seis.length; i++) {
                    logger.info("Seismogram "+i+" has "+seis[i].num_points+
                                    " points and starts at "+seis[i].begin_time.date_time);
                }
            }
            return seis;
        } catch (FissuresException e) {throw new RuntimeException(e);}
    }

    public static RequestFilter[] createRF(){
        // we will get data for 1 hour ago until now
        MicroSecondDate now = ClockUtil.now();
        MicroSecondDate hourAgo = now.subtract(ONE_HOUR);

        // construct the request filters to send to the server
        RequestFilter[] request = { new RequestFilter(Initializer.fakeChan,
                                                      hourAgo.getFissuresTime(),
                                                      now.getFissuresTime()) };
        return request;
    }

    public static RequestFilter[] createOldRF(){
        MicroSecondDate yearAgo = ClockUtil.now().subtract(ONE_YEAR);
        MicroSecondDate yearAgoAndADay = yearAgo.add(ONE_DAY);
        MicroSecondDate yearAgoAndAnHour = yearAgo.add(ONE_HOUR);
        MicroSecondDate usedEnd = yearAgoAndAnHour;
        logger.info("query from " + yearAgo + " to " + usedEnd);
        // construct the request filters to send to the server
        RequestFilter[] request = { new RequestFilter(Initializer.fakeChan,
                                                      yearAgo.getFissuresTime(),
                                                      usedEnd.getFissuresTime()) };
        return request;
    }

    protected DataCenterOperations seisDC;

    private static final TimeInterval ONE_YEAR = new TimeInterval(365, UnitImpl.DAY);
    private static final TimeInterval ONE_HOUR = new TimeInterval(1, UnitImpl.HOUR);
    private static final TimeInterval ONE_DAY = new TimeInterval(1, UnitImpl.DAY);
    private static Logger logger = Logger.getLogger(SimpleSeismogramClient.class);

    public static void main(String[] args) {
        /* Initializes the corba orb, finds the naming service and other startup
         * tasks. See Initializer for the code in this method. */
        Initializer.init(args);
        new SimpleSeismogramClient().exercise();
    }
}

