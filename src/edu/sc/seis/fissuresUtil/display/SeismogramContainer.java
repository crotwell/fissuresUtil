package edu.sc.seis.fissuresUtil.display;

import java.util.*;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.AbstractJob;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.RequestFilterChangeListener;
import edu.sc.seis.fissuresUtil.xml.SeisDataChangeEvent;
import edu.sc.seis.fissuresUtil.xml.SeisDataChangeListener;
import edu.sc.seis.fissuresUtil.xml.SeisDataErrorEvent;
import java.lang.ref.SoftReference;
import org.apache.log4j.Logger;

/**<code>SeismogramContainer</code> Takes a DataSetSeismogram and requests its
 *data.  It holds whatever it gets in soft references so that they can be
 * garbage collected if need be.  If it gets a request for data, and some of the
 * items it has once held have been garbage collected, it will reerequest them.
 *
 */

public class SeismogramContainer implements SeisDataChangeListener, RequestFilterChangeListener{
    public SeismogramContainer(DataSetSeismogram seismogram){
        this(null, seismogram);
    }
    
    public SeismogramContainer(SeismogramContainerListener initialListener,
                               DataSetSeismogram seismogram){
        this(initialListener, seismogram, false);
    }
    
    public SeismogramContainer(SeismogramContainerListener initialListener,
                               DataSetSeismogram seismogram,
                               boolean debug){
        if(initialListener != null){
            listeners.add(initialListener);
        }
        this.seismogram = seismogram;
        seismogram.addSeisDataChangeListener(this);
        seismogram.addRequestFilterChangeListener(this);
        this.debug = debug;
    }
    
    public void finished(SeisDataChangeEvent sdce) {
        finished = true;
        addSeismograms(sdce.getSeismograms());
        //loadStatus.decrementDataRetrievers();
    }
    
    public SeismogramIterator getIterator(){
        //use circuitous route to return time to sidestep class variable time
        //getting set to null at other points in the code
        MicroSecondTimeRange fullTime = time;
        if(fullTime == null){
            fullTime = DisplayUtils.getFullTime(getSeismograms());
            time = fullTime;
        }
        return getIterator(fullTime);
    }
    
    public SeismogramIterator getIterator(MicroSecondTimeRange timeRange){
        SoftReference iteratorReference = null;
        synchronized(threadToIterator){
            iteratorReference = (SoftReference)threadToIterator.get(Thread.currentThread());
        }
        if(iteratorReference != null){
            SeismogramIterator it = (SeismogramIterator)iteratorReference.get();
            if(it != null && it.hasNext()){
                if(it.getTimeRange().equals(timeRange)){
                    return it;
                }else if(timeRange != null){
                    it.setTimeRange(timeRange);
                    return it;
                }
            }
        }
        SeismogramIterator it = new SeismogramIterator(seismogram.getName(),
                                                       getSeismograms(),
                                                       timeRange);
        synchronized(threadToIterator){
            threadToIterator.put(Thread.currentThread(),new SoftReference(it));
        }
        return it;
    }
    
    public String toString(){ return seismogram.getName() + " Container"; }
    
    /**
     * ignore in the hopes someone else is handling this
     */
    public void error(SeisDataErrorEvent sdce) {
        if (sdce.getCausalException() instanceof FissuresException) {
            FissuresException fe = (FissuresException)sdce.getCausalException();
            logger.warn("Error retrieving seismograms, "+fe.the_error.error_code+" "+fe.the_error.error_description, sdce.getCausalException());
        } else {
            logger.warn("Error retrieving seismograms", sdce.getCausalException());
        }
    }
    
    public void pushData(SeisDataChangeEvent sdce) {
        addSeismograms(sdce.getSeismograms());
    }
    
    public void endTimeChanged() {
        if(debug)
            System.out.println("END TIME CHANGED for " + this);
        seismogram.retrieveData(this);
    }
    
    public void beginTimeChanged() {
        if(debug)
            System.out.println("BEGIN TIME CHANGED for " + this);
        seismogram.retrieveData(this);
    }
    
    private void addSeismograms(LocalSeismogramImpl[] seismograms){
        boolean newData = false;
        LocalSeismogramImpl[] currentSeis = getSeismograms(false);
        synchronized(softSeis){
            for (int j = 0; j < seismograms.length; j++) {
                boolean found = false;
                for (int i = 0; i < currentSeis.length; i++){
                    //As I don't know how to tell which seismogram is right if
                    //they have exactly the same times, I just keep the first one
                    //TODO fix inability to tell between local seismograms
                    if(seismograms[j].get_id().equals(currentSeis[i].get_id()) ||
                           (seismograms[j].getBeginTime().equals(currentSeis[i].getBeginTime()) &&
                                seismograms[j].getEndTime().equals(currentSeis[i].getEndTime()))){
                        found = true;
                        break;
                    }
                }
                if(!found){
                    softSeis.add(new SoftReference(seismograms[j]));
                    newData = true;
                }
            }
        }
        if(newData){
            time = null;
            noData = false;
            
            SeismogramContainerListener[] listArray;
            synchronized(listeners){
                listArray = new SeismogramContainerListener[listeners.size()];
                listeners.toArray(listArray);
            }
            for (int i = 0; i < listArray.length; i++){
                listArray[i].updateData();
            }
            synchronized(threadToIterator){
                threadToIterator.clear();
            }
            for (int i = 0; i < listArray.length; i++){
                listArray[i].updateData();
            }
        }
    }
    
    public LocalSeismogramImpl[] getSeismograms(){
        return getSeismograms(true);
    }
    
    private LocalSeismogramImpl[] getSeismograms(boolean retrieveOnEmpty){
        boolean callRetrieve = false;
        List existant = new ArrayList();
        LocalSeismogramImpl[] seis = EMPTY_ARRAY;
        synchronized(softSeis){
            if(softSeis.size() == 0 && retrieveOnEmpty){
                callRetrieve = true;
            }else{
                Iterator it = softSeis.iterator();
                while(it.hasNext()){
                    SoftReference current = (SoftReference)it.next();
                    Object o = current.get();
                    if(o != null){
                        existant.add(o);
                    }else{
                        callRetrieve = true;
                        it.remove();
                    }
                }
                seis = new LocalSeismogramImpl[existant.size()];
                existant.toArray(seis);
            }
        }
        if(callRetrieve){
            time = null;
            seismogram.retrieveData(this);
            //JobTracker.getTracker().add(loadStatus);
            //loadStatus.incrementDataRetrievers();
        }
        return seis;
    }
    
    public void addListener(SeismogramContainerListener listener){
        synchronized(listeners){
            listeners.add(listener);
        }
    }
    
    public void removeListener(SeismogramContainerListener listener){
        synchronized(listeners){
            listeners.remove(listener);
        }
    }
    
    public String getDataStatus(){
        if(noData && finished){
            return NO_DATA;
        }else if(noData){
            return GETTING_DATA;
        }else{
            return HAVE_DATA;
        }
    }
    
    public DataSetSeismogram getDataSetSeismogram(){ return seismogram; }
    
    private List listeners = Collections.synchronizedList(new ArrayList());
    
    private DataSetSeismogram seismogram;
    
    private List softSeis = Collections.synchronizedList(new ArrayList());
    
    private Map threadToIterator = Collections.synchronizedMap(new HashMap());
    
    private static final LocalSeismogramImpl[] EMPTY_ARRAY = {};
    
    private static Logger logger = Logger.getLogger(SeismogramContainer.class);
    
    private boolean finished = false;
    
    private boolean noData = true;
    
    public static final String NO_DATA = "No data available";
    
    public static final String GETTING_DATA = "Trying to get data";
    
    public static final String HAVE_DATA = "";
    
    private MicroSecondTimeRange time;
    
    private boolean debug;
    
    // private static LoaderJob loadStatus = new LoaderJob();
    
    private static class LoaderJob extends AbstractJob{
        
        public LoaderJob(){
            super("Seismic Data Loader");
            setFinished();
            //JobTracker.getTracker().add(this);
        }
        
        public synchronized void incrementDataRetrievers(){
            outRetrieving++;
            updateStatus();
        }
        
        public synchronized void decrementDataRetrievers(){
            if(--outRetrieving <= 0){
                outRetrieving = 0;
                setFinished();
            }else{
                updateStatus();
            }
        }
        
        private void updateStatus(){
            setStatus(outRetrieving + " seismograms are attempting to get data");
        }
        
        public void run() {}
        
        private int outRetrieving = 0;
    }
}


