package edu.sc.seis.fissuresUtil.simple;

import org.apache.log4j.Logger;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import edu.iris.Fissures.IfSeismogramDC.DataCenter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.FissuresException;



public class SimpleSeismogramClient implements TestingClient{
    public SimpleSeismogramClient(){
        try {
            /* This step is not required, but sometimes helps to determine if
             *  a server is down. if this call succedes but the next fails, then
             *  the nameing service is up and functional, but the network server
             *  is not reachable for some reason.
             */
            Initializer.getNS().getSeismogramDCObject("edu/iris/dmc",
                                                      "IRIS_BudDataCenter");
            logger.info("Got SeisDC as corba object, the name service is ok");

            /* This connects to the actual server, as opposed to just getting
             *  the reference to it. The naming convention is that the first
             *  part is the reversed DNS of the organization and the second part
             *  is the individual server name. The dmc lists their servers under
             *  the edu/iris/dmc and their main network server is IRIS_EventDC.
             */
            seisDC = Initializer.getNS().getSeismogramDC("edu/iris/dmc",
                                                         "IRIS_BudDataCenter");
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
        retrieve_seismograms(true);
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

    protected DataCenter seisDC;

    private static final TimeInterval ONE_HOUR = new TimeInterval(1, UnitImpl.HOUR);
    private static Logger logger = Logger.getLogger(SimpleSeismogramClient.class);

    public static void main(String[] args) {
        /* Initializes the corba orb, finds the naming service and other startup
         * tasks. See Initializer for the code in this method. */
        Initializer.init(args);
        new SimpleSeismogramClient().exercise();
    }
}

