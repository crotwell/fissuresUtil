package edu.sc.seis.fissuresUtil.database;

import java.util.*;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfSeismogramDC.DataCenterOperations;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.AbstractJob;
import edu.sc.seis.fissuresUtil.cache.JobTracker;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.xml.SeisDataChangeListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerListener;
import java.lang.ref.SoftReference;
import javax.swing.JComponent;
import org.apache.log4j.Category;
import java.awt.event.ContainerEvent;

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
    
    public void run() {
        JobTracker.getTracker().add(job);
        job.incrementRetrievers();
        //TODO use array of rf in retrieve call
        List seismograms = new ArrayList();
        for(int counter = 0; counter <  requestFilters.length; counter++) {
            try {
                RequestFilter[] temp = { requestFilters[counter] };
                LocalSeismogram[] seis = dbDataCenter.retrieve_seismograms(temp);
                LocalSeismogramImpl[] seisImpl = castToLocalSeismogramImplArray(seis);
                synchronized(initiators){
                    Iterator it = initiators.iterator();
                    while(it.hasNext()){
                        a_client.pushData(seisImpl, ((SeisDataChangeListener)it.next()));
                    }
                }
                for (int i = 0; i < seisImpl.length; i++){
                    seismograms.add(seisImpl[i]);
                }
            } catch(FissuresException fe) {
                synchronized(initiators){
                    failed = true;
                    Iterator it = initiators.iterator();
                    while(it.hasNext()){
                        a_client.error(((SeisDataChangeListener)it.next()), fe);
                    }
                    continue;
                }
            } catch(org.omg.CORBA.SystemException fe) {
                synchronized(initiators){
                    failed = true;
                    Iterator it = initiators.iterator();
                    while(it.hasNext()){
                        a_client.error(((SeisDataChangeListener)it.next()), fe);
                    }
                    continue;
                }
            }
            LocalSeismogramImpl[] seisArray = new LocalSeismogramImpl[seismograms.size()];
            seisRef = new SoftReference(seismograms.toArray(seisArray));
        }
        synchronized(initiators){
            Iterator it = initiators.iterator();
            while(it.hasNext()){
                a_client.finished(((SeisDataChangeListener)it.next()));
            }
            finished = true;
        }
        job.decrementRetrievers();
    }
    
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
            if(retrievers == 0 && waiters == 0)
                setFinished();
            else
                setFinished(false);
        }
        
        private int retrievers = 0, waiters = 0;
        
        public void run() {}
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
            }
        }
        if(!failed){
            LocalSeismogramImpl[] seis = (LocalSeismogramImpl[])seisRef.get();
            if(seis != null){
                a_client.pushData(seis, listener);
            }
            return true;
        }else{
            return false;
        }
    }
    
    private LocalSeismogramImpl[] castToLocalSeismogramImplArray(LocalSeismogram[] seismos) {
        LocalSeismogramImpl[] rtnValues = new LocalSeismogramImpl[seismos.length];
        for(int counter = 0; counter < seismos.length; counter++) {
            rtnValues[counter] = (LocalSeismogramImpl) seismos[counter];
        }
        return rtnValues;
    }
    
    private SoftReference seisRef = new SoftReference(null);
    
    private RequestFilter[] requestFilters;
    
    private LocalDataCenterCallBack a_client;
    
    private DataCenterOperations dbDataCenter;
    
    private Set initiators = Collections.synchronizedSet(new HashSet());
    
    private static Category logger = Category.getInstance(DataCenterThread.class.getName());
    
    private boolean finished = false, failed = false;
    
}// DataCenterThread

