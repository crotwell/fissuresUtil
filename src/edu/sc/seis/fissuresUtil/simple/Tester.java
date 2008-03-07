package edu.sc.seis.fissuresUtil.simple;


import java.io.PrintWriter;
import org.apache.log4j.Logger;

public class Tester{
    private  class Repeater extends Thread{
        public Repeater(Runnable r, int repeats){
            this.r = r;
            this.repeats = repeats;
        }

        public void run() {
            int i = 0;
            try {
                for (i = 0; i < repeats; i++){
                    logger.info("run " + (i + 1) + " of " + repeats + " for " + r);
                    r.run();
                    logger.info("finished " + (i + 1) + " of " + repeats + " for " + r);
                }
            } catch (Throwable e) {
                t = e;
                logger.warn("fail " + (i + 1) + " of " + repeats + " for " + r, e);
            }
            logger.info("leave thread for "+r);
        }

        public Throwable getThrown(){ return t; }

        public String toString(){ return "" + r; }

        public Runnable getRunnable(){ return r; }

        private Throwable t;
        private Runnable r;
        private int repeats;
    }

    /**Runs all the runnables 10 times against each other pair wise with one
     * thread per runnable
     */
    public static void runAll(Runnable[] runnables)throws Throwable{
        runAll(runnables, 10, null);
    }

    public static void runAll(Runnable[] runnables, int runsPerThread, PrintWriter out)throws Throwable{
        Tester t = new Tester();
        for (int i = 0; i < runnables.length; i++) {
            for (int j = i; j < runnables.length; j++) {
                Repeater iRepeater = t.new Repeater(runnables[i], runsPerThread);
                Repeater jRepeater = t.new Repeater(runnables[j], runsPerThread);
                logger.info("Starting " + iRepeater + " against " + jRepeater);
                if(out != null){
                    out.println("Starting <b>" + iRepeater + "</b> against <b>" + jRepeater+"</b><br/>");
                }
                iRepeater.start();
                jRepeater.start();
                boolean joined = false;
                while(!joined){
                    try{
                        iRepeater.join();
                        if(iRepeater.getThrown() != null){
                            throw iRepeater.getThrown();
                        }
                        jRepeater.join();
                        if(jRepeater.getThrown() != null){
                            throw jRepeater.getThrown();
                        }
                        joined = true;
                    }catch(InterruptedException e){}//start checking joins again
                }
                if(out != null){
                    out.println("<b>" +iRepeater + "</b> against <b>" + jRepeater + "</b> is successful<br/>");
                }
            }
        }
    }


    private static Repeater[] wrapInRepeaters(Runnable[] runnables,int repeats){
        Tester t = new Tester();
        Repeater[] threads = new Repeater[runnables.length];
        for (int i = 0; i < runnables.length; i++) {
            threads[i] = t.new Repeater(runnables[i], repeats);
        }
        return threads;
    }

    private static Logger logger = Logger.getLogger(Tester.class);

    public static void main(String[] args){
        Initializer.init(args);
        new ThreadedSeismogramClient().exercise();
        new ThreadedNetworkClient().exercise();
        new ThreadedEventClient().exercise();
    }
}
