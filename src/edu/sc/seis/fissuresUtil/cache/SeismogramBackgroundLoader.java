
package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.IfSeismogramDC.SeismogramAttr;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

/**
 * SeismogramBackgroundLoader.java
 *
 *
 * Created: Fri Feb 23 22:25:35 2001
 *
 * @author Philip Crotwell
 * @version
 */

public class SeismogramBackgroundLoader {

    public SeismogramBackgroundLoader(SeismogramBackgroundLoaderPool pool) {
        this.pool = pool;

        Runnable r = new Runnable() {
            public void run() {
                try {
                    runWork();
                } catch (Throwable e) {
                    GlobalExceptionHandler.handle(e);
                }
            }
        };
        privateThread = new Thread(seisLoaderThreadGroup,
                                   r,
                                   "Seismogram Loader"+getThreadNum());
        privateThread.start();
    }

    public void runWork() {

        SeismogramQueueElement q;
        SeismogramAttr attr;
        LocalSeismogram[] seis;
        while (noStopThread) {
            try {
                q = pool.getFromQueue();
                RequestFilter[] rf = new RequestFilter[1];
                rf[0] = q.getRequest();
                try {
                    seis = q.getDataCenter().retrieve_seismograms(rf);
                    q.getListener().seismogramLoaded(rf[0], seis);
                    pool.fireSeismogramLoaded(rf[0], seis);
                } catch (edu.iris.Fissures.FissuresException e) {
                    q.getListener().seismogramError(rf[0], e);
                    pool.fireSeismogramError(rf[0], e);
                }
            } catch (InterruptedException e) {
            }
        }
    }

    public void stopThread() {
        noStopThread = false;
    }

    private Thread privateThread;

    private volatile boolean noStopThread = true;

    private SeismogramBackgroundLoaderPool pool;

    private static int threadNum = 0;

    private synchronized static int getThreadNum() {
        return threadNum++;
    }

    private ThreadGroup seisLoaderThreadGroup =
        new ThreadGroup("Seismogram Loader");

} // SeismogramBackgroundLoader
