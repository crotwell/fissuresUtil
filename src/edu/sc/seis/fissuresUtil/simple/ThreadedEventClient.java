package edu.sc.seis.fissuresUtil.simple;

import org.apache.log4j.Logger;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;

public class ThreadedEventClient extends SimpleEventClient{
    public void exercise(){
        super.exercise();
        try {
            Tester.runAll(createRunnables());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public Runnable[] createRunnables() {
        Runnable[] runnables = new Runnable[4];
        runnables[0]  = new QueryEvents();
        runnables[1] = new KnownCatalogs();
        runnables[2] = new KnownContributors();
        runnables[3] = new GetByName();
        return runnables;
    }

    private class QueryEvents implements Runnable{
        public void run() { query_events(); }

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

    private static final TimeInterval ONE_DAY = new TimeInterval(1, UnitImpl.DAY);

    private static Logger logger = Logger.getLogger(ThreadedEventClient.class);

    public static void main(String[] args){
        Initializer.init(args);
        try {
            Tester.runAll(new ThreadedEventClient().createRunnables());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
