package edu.sc.seis.fissuresUtil.simple;

import org.apache.log4j.Logger;

public class ThreadedSeisClient extends SimpleSeismogramClient{
    public void exercise(){
        super.exercise();
        try {
            Tester.runAll(createRunnables());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private class RetrieveSeismograms implements Runnable{
        public void run() { retrieve_seismograms(false);}

        public String toString(){ return "retrieve_seismograms"; }
    }

    private class AvailableData implements Runnable{
        public void run() { seisDC.available_data(createRF()); }

        public String toString(){ return "available_data"; }
    }

    public Runnable[] createRunnables() {
        Runnable[] runnables = new Runnable[2];
        runnables[0] = new AvailableData();
        runnables[1] = new RetrieveSeismograms();
        return runnables;
    }

    private static Logger logger = Logger.getLogger(ThreadedSeisClient.class);

    public static void main(String[] args){
        Initializer.init(args);
        new ThreadedSeisClient().exercise();
    }
}
