
package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.seismogramDC.*;
import java.util.*;
import javax.swing.table.*;

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
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		}
	    };
	privateThread = new Thread(r);
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

} // SeismogramBackgroundLoader
