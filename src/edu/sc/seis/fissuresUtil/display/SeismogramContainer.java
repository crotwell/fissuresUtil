package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.SeisDataChangeEvent;
import edu.sc.seis.fissuresUtil.xml.SeisDataChangeListener;
import edu.sc.seis.fissuresUtil.xml.SeisDataErrorEvent;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

/**<code>SeismogramContainer</code> Takes a DataSetSeismogram and requests its
 *data.  It holds whatever it gets in soft references so that they can be
 * garbage collected if need be.  If it gets a request for data, and some of the
 * items it has once held have been garbage collected, it will reerequest them.
 *
 */

public class SeismogramContainer implements SeisDataChangeListener{
    public SeismogramContainer(DataSetSeismogram seismogram){
        this.seismogram = seismogram;
        seismogram.addSeisDataChangeListener(this);
        seismogram.retrieveData(this);
    }

    public void finished(SeisDataChangeEvent sdce) {
        finished = true;
        addSeismograms(sdce.getSeismograms());
    }

    public synchronized SeismogramIterator getIterator(){
        return getIterator(DisplayUtils.getFullTime(getSeismograms()));
    }

    public synchronized SeismogramIterator getIterator(MicroSecondTimeRange timeRange){
        if(softIterator != null){
            SeismogramIterator it = (SeismogramIterator)softIterator.get();
            if(!changed && it != null){
                if(it.getTimeRange().equals(timeRange)){
                    return it;
                }else if(timeRange != null){
                    it.setTimeRange(timeRange);
                    return it;
                }
            }
        }
        changed = false;

        SeismogramIterator it = new SeismogramIterator(seismogram.getName(),
                                                       getSeismograms(),
                                                       timeRange);
        softIterator = new SoftReference(it);
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

    private  void addSeismograms(LocalSeismogramImpl[] seismograms){
        LocalSeismogramImpl[] currentSeis = getSeismograms();
        synchronized(this){
            for (int j = 0; j < seismograms.length; j++) {
                boolean found = false;
                for (int i = 0; i < currentSeis.length; i++){
                    if(seismograms[j].get_id().equals(currentSeis[i].get_id())){
                        found = true;
                        break;
                    }
                }
                if(!found){
                    softSeis.add(new SoftReference(seismograms[j]));
                    changed = true;
                }
            }
        }
        if(changed){
            noData = false;
            Iterator it = listeners.iterator();
            while(it.hasNext()){//let all listeners know about new data
                SeismogramContainerListener current = (SeismogramContainerListener)it.next();
                current.updateData();
            }
        }
    }

    public synchronized LocalSeismogramImpl[] getSeismograms(){
        if(softSeis.size() == 0){
            return EMPTY_ARRAY;
        }
        List existant = new ArrayList();
        Iterator it = softSeis.iterator();
        boolean callRetrieve = false;
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
        if(callRetrieve){
            seismogram.retrieveData(this);
        }
        return (LocalSeismogramImpl[])existant.toArray(new LocalSeismogramImpl[existant.size()]);
    }

    public void addListener(SeismogramContainerListener listener){
        listeners.add(listener);
    }

    public void removeListener(SeismogramContainerListener listener){
        listeners.remove(listener);
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

    private List listeners = new ArrayList();

    private DataSetSeismogram seismogram;

    private List softSeis = new ArrayList();

    private SoftReference softIterator;

    private static final LocalSeismogramImpl[] EMPTY_ARRAY = {};

    private static Logger logger = Logger.getLogger(SeismogramContainer.class);

    private boolean finished = false;

    private boolean noData = true;

    private static final String NO_DATA = "No data available";

    private static final String GETTING_DATA = "Trying to get data";

    private static final String EMPTY = "";

    private boolean changed;
}

