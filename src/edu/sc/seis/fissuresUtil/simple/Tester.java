package edu.sc.seis.fissuresUtil.simple;


import edu.sc.seis.fissuresUtil.simple.Initializer;
import org.apache.log4j.Logger;

public class Tester{
    private  class Repeater implements Runnable{
        public Repeater(Runnable r, int repeats){
            this.r = r;
            this.repeats = repeats;
        }
        
        public void run() {
            for (int i = 0; i < 3; i++){
                logger.info("run " + (i + 1) + " of " + repeats + " for " + r);
                r.run();
            }
        }
        
        public String toString(){ return "" + r; }
        
        private Runnable r;
        private int repeats;
    }
    
    /**Runs all the runnables 3 times against each other pair wise with one
     * thread per runnable
     */
    public static void runAll(Runnable[] runnables){ runAll(runnables, 3, 1); }
    
    public static void runAll(Runnable[] runnables, int runsPerThread,
                              int threadsPerRunnable){
        runnables = wrapInRepeaters(runnables, runsPerThread);
        for (int i = 0; i < runnables.length; i++) {
            for (int j = i; j < runnables.length; j++) {
                logger.info("Starting " + runnables[i] + " against " + runnables[j]);
                Thread[] threads = new Thread[threadsPerRunnable * 2];
                for (int k = 0; k < threads.length; k += 2){
                    threads[k] = new Thread(runnables[i], runnables[i] + " thread" + k/2);
                    threads[k + 1] = new Thread(runnables[j], runnables[j] + " thread" + k/2);
                }
                for (int k = 0; k < threads.length; k++) threads[k].start();
                boolean joined = false;
                int k = 0;
                while(!joined){
                    try{
                        for(; k < threads.length; k++) threads[k].join();
                        joined = true;
                    }catch(InterruptedException e){}//start checking joins again
                }
            }
        }
    }
    
    private static Runnable[] wrapInRepeaters(Runnable[] runnables,int repeats){
        Tester t = new Tester();
        for (int i = 0; i < runnables.length; i++) {
            runnables[i] = t.new Repeater(runnables[i], repeats);
        }
        return runnables;
    }
    
    private static Logger logger = Logger.getLogger(Tester.class);
    
    public static void main(String[] args){
        Initializer.init(args);
        new ThreadedSeisClient().exercise();
        new ThreadedNetClient().exercise();
        new ThreadedEventClient().exercise();
    }
}
