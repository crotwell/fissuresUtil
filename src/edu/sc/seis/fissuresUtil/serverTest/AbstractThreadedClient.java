package edu.sc.seis.fissuresUtil.serverTest;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.Time;
import edu.sc.seis.fissuresUtil.simple.AbstractClient;
import org.apache.log4j.Logger;

public abstract class AbstractThreadedClient extends AbstractClient{
    public abstract Runnable[] createRunnables();
    
    /**Runs all the runnables returned by client.createRunnables() against each
     * other pair wise
     */
    public static void runAll(AbstractThreadedClient client){
        Runnable[] runnables = client.createRunnables();
        for (int i = 0; i < runnables.length; i++) {
            for (int j = i; j < runnables.length; j++) {
                Thread one = new Thread(runnables[i], runnables[i] + " thread");
                Thread two = new Thread(runnables[j], runnables[j] + " thread");
                System.out.println("Starting " + runnables[i] + " against " + runnables[j]);
                one.start();
                two.start();
                boolean joined = false;
                while(!joined){
                    try{
                        one.join();
                        two.join();
                        joined = true;
                    }catch(InterruptedException e){}
                }
            }
        }
    }
    
    protected class Repeater implements Runnable{
        public Repeater(Runnable r){ this.r = r; }
        
        public void run() {
            for (int i = 0; i < 3; i++){
                logger.info("run " + (i + 1) + " of 3 for " + r);
                r.run();
            }
        }
        
        public String toString(){ return "" + r; }
        
        private Runnable r;
    }
    
    protected static NetworkId fakeNet = new NetworkId("II", new Time("19861024000000.0000GMT", 0));
    protected static StationId fakeStation = new StationId(fakeNet, "AAK", new Time("19901012000000.0000GMT", 0));
    protected static ChannelId fakeChan = new ChannelId(fakeNet, "AAK", "  ", "BHE", new Time("19901012000000.0000GMT", 0));
    
    private static Logger logger = Logger.getLogger(AbstractThreadedClient.class);
}
