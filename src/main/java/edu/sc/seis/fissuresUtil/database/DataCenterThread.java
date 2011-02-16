package edu.sc.seis.fissuresUtil.database;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfSeismogramDC.DataCenterOperations;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.AbstractJob;
import edu.sc.seis.fissuresUtil.cache.JobTracker;
import edu.sc.seis.fissuresUtil.xml.SeisDataChangeListener;

/**
 * DataCenterThread.java
 *
 *
 * Created: Mon Feb 17 15:12:15 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class DataCenterThread implements Runnable{
    public DataCenterThread (RequestFilter[] requestFilters,
                             LocalDataCenterCallBack a_client,
                             SeisDataChangeListener initiator,
                             DataCenterOperations dbDataCenter){
        this.requestFilters = requestFilters;
        this.a_client = a_client;
        synchronized(initiators){
            initiators.add(initiator);
        }
        this.dbDataCenter = dbDataCenter;
        if(job == null)
            job = new RetrievalJob();
    }

    /**
     * Sets max number of times to retry on corba System exceptions
     *
     * @param    RetryNum            an int
     */
    public void setRetryNum(int retryNum) {
        this.retryNum = retryNum;
    }

    /**
     * Returns max number of times to retry on corba System exceptions
     *
     * @return    an int
     */
    public int getRetryNum() {
        return retryNum;
    }

    public void run() {
        JobTracker.getTracker().add(job);
        job.incrementRetrievers();
        try {
            LocalSeismogram[] seis = retrieveSeis();
            synchronized(initiators){
                Iterator it = initiators.iterator();
                while(it.hasNext()){
                    SeisDataChangeListener cur = ((SeisDataChangeListener)it.next());
                    a_client.pushData(castToLocalSeismogramImplArray(seis), cur);
                    a_client.finished(cur);
                }
                finished = true;
            }
        } catch(Throwable e) {
            passExceptionToListeners(e);
        }
        job.decrementRetrievers();
    }

    private LocalSeismogram[] retrieveSeis()throws FissuresException{
        RuntimeException lastException = null;
        int count = 0;
        while (count < retryNum) {
            try {
                return dbDataCenter.retrieve_seismograms(requestFilters);
            } catch(org.omg.CORBA.SystemException fe) {
                lastException = fe;
            }
            count++;
        }
        throw lastException;
    }

    private void passExceptionToListeners(Throwable t){
        synchronized(initiators){
            failed = true;
            Iterator it = initiators.iterator();
            while(it.hasNext()){
                SeisDataChangeListener cur = (SeisDataChangeListener)it.next();
                a_client.error(cur, t);
                a_client.finished(cur);
            }
        }
    }

    int retryNum = 3;

    private class RetrievalJob extends AbstractJob{
        public RetrievalJob(){
            super("Data Retriever");
            setFinished();
        }

        private synchronized void incrementWaiters(){
            setFinished(false);
            setStatus(retrievers + " retrieving data " + ++waiters + " waiting to retreive");
        }

        private synchronized void incrementRetrievers(){
            setFinished(false);
            setStatus(++retrievers + " retrieving data " + --waiters + " waiting to retrieve");
        }

        private synchronized void decrementRetrievers(){
            setStatus(--retrievers + " retrieving data " + waiters + " waiting to retrieve");
            if(retrievers == 0 && waiters == 0){setFinished();
            }else{ setFinished(false); }
        }

        private int retrievers = 0, waiters = 0;

        public void runJob() {}
    }

    public static void incrementWaiters(){
        job.incrementWaiters();
    }

    private static RetrievalJob job;

    public boolean getData(SeisDataChangeListener listener,
                           RequestFilter[] requestFilters){
        for (int i = 0; i < requestFilters.length; i++){
            boolean found = false;
            for (int j = 0; j < this.requestFilters.length && !found; j++){
                if(requestFilters[i] == this.requestFilters[j]){
                    found = true;
                }
            }
            if(!found){
                return false;
            }
        }
        synchronized(initiators){
            if(!finished){
                initiators.add(listener);
                return true;
            }
        }
        if(finished){
            LocalSeismogramImpl[] seis = (LocalSeismogramImpl[])seisRef.get();
            if(seis != null){
                a_client.pushData(seis, listener);
                a_client.finished(listener);
                return true;
            }
        }
        return false;
    }

    private LocalSeismogramImpl[] castToLocalSeismogramImplArray(LocalSeismogram[] seismos) {
        LocalSeismogramImpl[] rtnValues = new LocalSeismogramImpl[seismos.length];
        for(int counter = 0; counter < seismos.length; counter++) {
            rtnValues[counter] = (LocalSeismogramImpl) seismos[counter];
        }
        return rtnValues;
    }

    private SoftReference seisRef  = new SoftReference(null);

    private RequestFilter[] requestFilters;

    private LocalDataCenterCallBack a_client;

    private DataCenterOperations dbDataCenter;

    private Set initiators = Collections.synchronizedSet(new HashSet());

    private static Logger logger = LoggerFactory.getLogger(DataCenterThread.class.getName());

    private boolean finished = false, failed = false;

}// DataCenterThread


