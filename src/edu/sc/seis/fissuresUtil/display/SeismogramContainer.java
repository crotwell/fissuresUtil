package edu.sc.seis.fissuresUtil.display;

import java.util.*;

import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
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

public class SeismogramContainer implements SeisDataChangeListener{
    public SeismogramContainer(DataSetSeismogram seismogram){
        this(null, seismogram);
    }

    public SeismogramContainer(SeismogramContainerListener initialListener,
                               DataSetSeismogram seismogram){
        if(initialListener != null){
            listeners.add(initialListener);
        }
        this.seismogram = seismogram;
        seismogram.addSeisDataChangeListener(this);
        seismogram.retrieveData(this);
    }

    public void finished(SeisDataChangeEvent sdce) {
        finished = true;
        addSeismograms(sdce.getSeismograms());
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
            if(it != null){
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
        logger.warn("Error retrieving seismograms");
    }

    public void pushData(SeisDataChangeEvent sdce) {
        addSeismograms(sdce.getSeismograms());
    }

    private void addSeismograms(LocalSeismogramImpl[] seismograms){
        boolean newData = false;
        LocalSeismogramImpl[] currentSeis = getSeismograms();
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
            synchronized(threadToIterator){
                threadToIterator.clear();
            }
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
        }
    }

    public LocalSeismogramImpl[] getSeismograms(){
        boolean callRetrieve = false;
        List existant = new ArrayList();
        synchronized(softSeis){
            if(softSeis.size() == 0){
                return EMPTY_ARRAY;
            }
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
        }
        if(callRetrieve){
            time = null;
            seismogram.retrieveData(this);
        }
        return (LocalSeismogramImpl[])existant.toArray(new LocalSeismogramImpl[existant.size()]);
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
            return EMPTY;
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

    private static final String NO_DATA = "No data available";

    private static final String GETTING_DATA = "Trying to get data";

    private static final String EMPTY = "";

    private MicroSecondTimeRange time;
}

