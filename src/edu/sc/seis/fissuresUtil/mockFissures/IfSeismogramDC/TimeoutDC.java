package edu.sc.seis.fissuresUtil.mockFissures.IfSeismogramDC;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;

/**
 * @author groves Created on Nov 16, 2004
 */
public class TimeoutDC extends MockDC {

    /**
     * Creates a seismogram dc that waits 10 minutes before returning and prints
     * the amount of remaining time to the console every 10 seconds
     */
    public TimeoutDC() {
        this(10 * 60 * 1000, 10 * 1000);
    }

    /**
     * Creates a seismogram dc that waits waitTime millis before returning a
     * call to available_data and prints to the console once every
     * updateInterval millis
     */
    public TimeoutDC(int waitTime, int updateInterval) {
        this.waitTime = waitTime;
        this.updateInterval = updateInterval;
    }

    public RequestFilter[] available_data(RequestFilter[] a_filterseq) {
        try {
            for(int i = waitTime; i > 0; i -= updateInterval) {
                Thread.sleep(updateInterval);
                logger.debug("Waiting another " + i / 1000
                        + " seconds before returning available data");
            }
            Thread.sleep(10 * 60 * 1000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        return super.available_data(a_filterseq);
    }

    private int waitTime;

    private int updateInterval;

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(TimeoutDC.class);
}