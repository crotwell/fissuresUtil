package edu.sc.seis.fissuresUtil.serverTest;

import edu.sc.seis.fissuresUtil.serverTest.AbstractThreadedClient;
import edu.sc.seis.fissuresUtil.simple.AbstractClient;
import org.apache.log4j.Logger;

public abstract class AbstractThreadedClient extends AbstractClient{
    public AbstractThreadedClient(){ this.runnables = createRunnables(); }
  
    public abstract Runnable[] createRunnables();
    
    public void runAll(){
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
        
        private Runnable r;
        
        public void run() { for (int i = 0; i < 3; i++){
                logger.info("run " + (i + 1) + " of 3 for " + r);
                r.run();
            }
        }
        
        public String toString(){ return "repeater for " + r; }
    }
    
    private  Runnable[] runnables;
    
    private static Logger logger = Logger.getLogger(AbstractThreadedClient.class);
}
