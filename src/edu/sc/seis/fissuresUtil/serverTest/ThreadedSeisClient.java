package edu.sc.seis.fissuresUtil.serverTest;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfSeismogramDC.DataCenter;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.serverTest.ThreadedSeisClient;
import org.apache.log4j.Logger;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import edu.iris.Fissures.FissuresException;

public class ThreadedSeisClient extends AbstractThreadedClient{
    public ThreadedSeisClient(String[] args){
        init(args);
        try{
            seisDC = fisName.getSeismogramDC("edu/iris/dmc",
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
    
    private class RetrieveSeismograms implements Runnable{
        public void run() {
            try {
                seisDC.retrieve_seismograms(createRF());
            } catch (FissuresException e) {
                throw new RuntimeException("Trouble retrieving seismograms", e);
            }
        }
        
        public String toString(){ return "retrieve_seismograms"; }
    }
    
    private class AvailableData implements Runnable{
        public void run() { seisDC.available_data(createRF()); }
        
        public String toString(){ return "available_data"; }
    }
    
    
    
    private static RequestFilter[] createRF(){
        // we will get data for 1 hour ago until now
        MicroSecondDate now = ClockUtil.now();
        MicroSecondDate hourAgo = now.subtract(ONE_HOUR);
        
        // construct the request filters to send to the server
        RequestFilter[] request = { new RequestFilter(fakeChan,
                                                      hourAgo.getFissuresTime(),
                                                      now.getFissuresTime()) };
        return request;
    }
    
    public Runnable[] createRunnables() {
        Runnable[] runnables = new Runnable[2];
        runnables[0] = new Repeater(new AvailableData());
        runnables[1] = new Repeater(new RetrieveSeismograms());
        return runnables;
    }
    
    private DataCenter seisDC;
    
    
    private static final TimeInterval ONE_HOUR = new TimeInterval(1, UnitImpl.HOUR);
    private static Logger logger = Logger.getLogger(ThreadedSeisClient.class);
    
    public static void main(String[] args){
        Tester.runAll(new ThreadedSeisClient(args));
    }
}
