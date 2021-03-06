package edu.sc.seis.fissuresUtil.simple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadedSeismogramClient extends SimpleSeismogramClient{
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
        public void run() { seisDC.available_data(createCurrentRF()); }

        public String toString(){ return "available_data"; }
    }

    public Runnable[] createRunnables() {
        Runnable[] runnables = new Runnable[2];
        runnables[0] = new AvailableData();
        runnables[1] = new RetrieveSeismograms();
        return runnables;
    }

    private static Logger logger = LoggerFactory.getLogger(ThreadedSeismogramClient.class);

    public static void main(String[] args){
        Initializer.init(args);
        new ThreadedSeismogramClient().exercise();
    }
}
