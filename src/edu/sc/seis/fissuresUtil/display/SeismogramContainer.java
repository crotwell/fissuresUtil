package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.xml.*;
import java.util.*;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.AbstractJob;
import edu.sc.seis.fissuresUtil.database.DBDataCenter;
import java.lang.ref.SoftReference;
import org.apache.log4j.Logger;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.iris.dmc.seedcodec.CodecException;
import edu.iris.Fissures.network.ChannelIdUtil;

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
        if(initialListener != null){
            listeners.add(initialListener);
        }
        this.seismogram = seismogram;
        seismogram.addSeisDataChangeListener(this);
        seismogram.addRequestFilterChangeListener(this);
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

    public void error(SeisDataErrorEvent sdce) {
        GlobalExceptionHandler.handle("Trouble getting data for " + sdce.getSource(),
                                      sdce.getCausalException());
        error = true;
    }

    public void pushData(SeisDataChangeEvent sdce) {
        addSeismograms(sdce.getSeismograms());
    }

    public void endTimeChanged() {
        synchronized(softSeis){
            synchronized(threadToIterator){
                softSeis.clear();
                threadToIterator.clear();
            }
        }
    }

    public void beginTimeChanged() {
        synchronized(softSeis){
            synchronized(threadToIterator){
                softSeis.clear();
                threadToIterator.clear();
            }
        }
    }

    private void addSeismograms(LocalSeismogramImpl[] seismograms){
        boolean newData = false;
        LinkedList badSeis = null;
        synchronized(softSeis){
            for (int j = 0; j < seismograms.length; j++) {
                LocalSeismogramImpl[] curSeis = getSeismograms(false);
                RequestFilter[] needed = { seismogram.getRequestFilter() };
                RequestFilter[] uncovered = DBDataCenter.notCovered(needed, curSeis);
                boolean satisfiesUncovered = false;
                for (int i = 0; i < uncovered.length; i++) {
                    if(DCDataSetSeismogram.intersects(uncovered[i], seismograms[j])){
                        satisfiesUncovered = true;
                    }
                }
                if(satisfiesUncovered){
                    Iterator it = softSeis.iterator();
                    while(it.hasNext()){
                        LocalSeismogramImpl cur = (LocalSeismogramImpl)((SoftReference)it.next()).get();
                        if(cur != null &&
                           DataSetSeismogram.equalOrContains(cur, seismograms[j]))
                            it.remove();
                    }
                    if (seismograms[j].isDataDecodable()) {
                        softSeis.add(new SoftReference(seismograms[j]));
                        newData = true;
                    } else {
                        if (badSeis == null) {badSeis = new LinkedList();}
                        badSeis.add(seismograms[j]);
                    }
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
        if (badSeis != null) {
            GlobalExceptionHandler.handle(new CodecException("Got "+badSeis.size()+" seismograms that couldn't be decompressed for "+ChannelIdUtil.toString(getDataSetSeismogram().getRequestFilter().channel_id)));
        }
    }

    public LocalSeismogramImpl[] getSeismograms(){
        return getSeismograms(true);
    }

    private LocalSeismogramImpl[] getSeismograms(boolean retrieveOnEmpty){
        if(seismogram instanceof MemoryDataSetSeismogram){
            return ((MemoryDataSetSeismogram)seismogram).getCache();
        }
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
        if(error){
            return ERROR;
        }else if(noData && finished){
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

    private boolean error = false;

    public static final String NO_DATA = "No data available";

    public static final String GETTING_DATA = "Trying to get data";

    public static final String HAVE_DATA = "";

    public static final String ERROR = "Error encountered getting data";

    private MicroSecondTimeRange time;
}
