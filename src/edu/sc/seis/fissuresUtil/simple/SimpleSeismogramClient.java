package edu.sc.seis.fissuresUtil.simple;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.apache.log4j.Logger;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfSeismogramDC.DataCenterOperations;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.BulletproofVestFactory;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;

public class SimpleSeismogramClient implements TestingClient {

    public SimpleSeismogramClient() {
        String serverDNS;
        String serverName;
        // iris
        serverDNS = "edu/iris/dmc";
        serverName = "IRIS_BudDataCenter";
        // or
        //serverName = "IRIS_PondDataCenter";
        // or If using the archive, swtich the request filter used in exercise
        //serverName = "IRIS_ArchiveDataCenter";
        // Berkeley
        //serverDNS="edu/berkeley/geo/quake";
        //serverName = "NCEDC_DataCenter";
        // South Carolina (SCEPP)
        //serverDNS="edu/sc/seis";
        //serverName="SCEPPSeismogramDC";
        try {
            /*
             * This step is not required, but sometimes helps to determine if a
             * server is down. if this call succedes but the next fails, then
             * the nameing service is up and functional, but the network server
             * is not reachable for some reason.
             */
            Initializer.getNS().getSeismogramDCObject(serverDNS, serverName);
            logger.info("Got SeisDC as corba object, the name service is ok");
            /*
             * This connects to the actual server, as opposed to just getting
             * the reference to it. The naming convention is that the first part
             * is the reversed DNS of the organization and the second part is
             * the individual server name. The dmc lists their servers under the
             * edu/iris/dmc and their main network server is IRIS_EventDC.
             */
            seisDC = BulletproofVestFactory.vestSeismogramDC(serverDNS,
                                                             serverName,
                                                             Initializer.getNS());
            logger.info("got SeisDC");
        } catch(org.omg.CORBA.ORBPackage.InvalidName e) {
            logger.error("Problem with name service: ", e);
        } catch(org.omg.CosNaming.NamingContextPackage.InvalidName e) {
            logger.error("Problem with name service: ", e);
        } catch(NotFound e) {
            logger.error("Problem with name service: ", e);
        } catch(CannotProceed e) {
            logger.error("Problem with name service: ", e);
        }
    }

    public void exercise() {
        //If testing the archive, switch to this available_data and retrieve
        //available_data(createOldRF(), true);
        //queuedRetrieve("Wily Test");
        
        available_data(true);
        retrieve_seismograms(true);
    }

    public void queuedRetrieve(String name) {
        try {
            String id = seisDC.queue_seismograms(createOldRF());
            logger.info("got id " + id + " for " + name);
            String status = seisDC.request_status(id);
            while(status.equals("Processing")) {
                status = seisDC.request_status(id);
                logger.info("Status is " + status + " for " + name);
                try {
                    Thread.sleep(10000);
                } catch(InterruptedException e) {}
            }
            logger.info("FINISHED " + name);
            logger.info(name + " status is " + status);
            if(status.equals("Finished")) {
                LocalSeismogram[] seis = seisDC.retrieve_queue(id);
                for(int i = 0; i < seis.length; i++) {
                    System.out.println(name + " of " + i + " is "
                            + seis[i].num_points + " points and starts at "
                            + seis[i].begin_time.date_time);
                }
            }
        } catch(FissuresException e) {
            logger.info(name + " threw exception");
            e.printStackTrace();
        }
    }

    public LocalSeismogram[] retrieve_seismograms() {
        return retrieve_seismograms(false);
    }

    public LocalSeismogram[] retrieve_seismograms(boolean verbose) {
        try {
            LocalSeismogram[] seis = seisDC.retrieve_seismograms(createCurrentRF());
            if(verbose) {
                logger.info("Got " + seis.length + " seismograms.");
                for(int i = 0; i < seis.length; i++) {
                    logger.info("Seismogram " + i + " has "
                            + seis[i].num_points + " points and starts at "
                            + seis[i].begin_time.date_time);
                }
            }
            return seis;
        } catch(FissuresException e) {
            throw new RuntimeException(e);
        }
    }

    public RequestFilter[] available_data(boolean verbose) {
        return available_data(createCurrentRF(), verbose);
    }

    public RequestFilter[] available_data(RequestFilter[] request,
                                          boolean verbose) {
        RequestFilter[] rf = seisDC.available_data(request);
        if(verbose) {
            logger.info("Got " + rf.length
                    + " request filters back for available data");
        }
        return rf;
    }

    public static RequestFilter[] createCurrentRF() {
        // we will get data for 1 hour ago until now
        MicroSecondDate now = ClockUtil.now();
        MicroSecondDate hourAgo = now.subtract(ONE_HOUR);
        // construct the request filters to send to the server
        RequestFilter[] request = {new RequestFilter(Initializer.fakeChan,
                                                     hourAgo.getFissuresTime(),
                                                     now.getFissuresTime())};
        return request;
    }

    public static RequestFilter[] createOldRF() {
        RequestFilter[] rf = {new RequestFilter()};
        Time[] queryTimes = new Time[2];
        SimpleDateFormat formatter = new SimpleDateFormat("G yyyy.MM.dd hh:mm:ss z");
        try {
            queryTimes[0] = new MicroSecondDate(formatter.parse("AD 2003.07.20 06:23:25 GMT")).getFissuresTime();
            queryTimes[1] = new MicroSecondDate(formatter.parse("AD 2003.09.20 06:46:29 GMT")).getFissuresTime();
        } catch(ParseException e) {
            e.printStackTrace();
        }
        rf[0].channel_id = new ChannelId(new NetworkId("IU", queryTimes[1]),
                                         "*",
                                         "*",
                                         "BHZ",
                                         queryTimes[1]);
        rf[0].end_time = queryTimes[1];
        rf[0].start_time = queryTimes[0];
        return rf;
    }

    protected DataCenterOperations seisDC;

    private static final TimeInterval ONE_HOUR = new TimeInterval(1,
                                                                  UnitImpl.HOUR);

    private static Logger logger = Logger.getLogger(SimpleSeismogramClient.class);

    public static void main(String[] args) {
        /*
         * Initializes the corba orb, finds the naming service and other startup
         * tasks. See Initializer for the code in this method.
         */
        Initializer.init(args);
        new SimpleSeismogramClient().exercise();
    }
}