package edu.sc.seis.fissuresUtil.serverTest;

import edu.iris.Fissures.IfEvent.*;

import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.model.GlobalAreaImpl;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.serverTest.ThreadedEventClient;
import org.apache.log4j.Logger;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

public class ThreadedEventClient extends AbstractThreadedClient{
    public ThreadedEventClient(String[] args){
        init(args);
        try{
            EventDC eventDC = fisName.getEventDC("edu/iris/dmc","IRIS_EventDC");
            logger.info("got EventDC");
            finder = eventDC.a_finder();
            logger.info("got EventFinder");
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
    
    public Runnable[] createRunnables() {
        Runnable[] runnables = new Runnable[4];
        runnables[0]  = new Repeater(new QueryEvents());
        runnables[1] = new Repeater(new KnownCatalogs());
        runnables[2] = new Repeater(new KnownContributors());
        runnables[3] = new Repeater(new GetByName());
        return runnables;
    }
    
    private class QueryEvents implements Runnable{
        public void run() {
            EventSeqIterHolder iter = new EventSeqIterHolder();
            MicroSecondDate now = ClockUtil.now();
            MicroSecondDate yesterday = now.subtract(ONE_DAY);
            TimeRange oneDay = new TimeRange(yesterday.getFissuresTime(),
                                             now.getFissuresTime());
            String[] magTypes = {"%"};
            String[] catalogs = {"FINGER"};
            String[] contributors = {"NEIC"};
            finder.query_events(new GlobalAreaImpl(),
                                new QuantityImpl(0, UnitImpl.KILOMETER),
                                new QuantityImpl(1000, UnitImpl.KILOMETER),
                                oneDay, magTypes, 5.0f, 10.0f, catalogs,
                                contributors, 500, iter);
        }
        
        public String toString(){ return "query_events"; }
    }
    
    private class KnownCatalogs implements Runnable{
        public void run() { finder.known_catalogs(); }
        
        public String toString(){ return "known_catalogs"; }
    }
    
    private class KnownContributors implements Runnable{
        public void run() { finder.known_contributors(); }
        
        public String toString(){ return "known_contributors"; }
    }
    
    private class GetByName implements Runnable{
        public void run() { finder.get_by_name("11481542");}
        
        public String toString(){ return "get_by_name"; }
    }
    
    private EventFinder finder;
    
    private static final TimeInterval ONE_DAY = new TimeInterval(1, UnitImpl.DAY);
    
    private static Logger logger = Logger.getLogger(ThreadedEventClient.class);
    
    public static void main(String[] args){
        Tester.runAll(new ThreadedEventClient(args));
    }
}
