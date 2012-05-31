package edu.sc.seis.fissuresUtil.display;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.time.CoverageTool;
import edu.sc.seis.fissuresUtil.time.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.time.RangeTool;
import edu.sc.seis.fissuresUtil.xml.DCDataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSetSeismogram;

/**
 * @author groves Created on Mar 28, 2005
 */
public class SoftRefSeismogramContainer extends AbstractSeismogramContainer {

    public SoftRefSeismogramContainer(DataSetSeismogram seismogram) {
        this(null, seismogram);
    }

    public SoftRefSeismogramContainer(SeismogramContainerListener initialListener,
                                      DataSetSeismogram seismogram) {
        super(initialListener, seismogram);
    }

    public SeismogramIterator getIterator() {
        // use circuitous route to return time to sidestep class variable time
        // getting set to null at other points in the code
        MicroSecondTimeRange fullTime = time;
        if(fullTime == null) {
            fullTime = RangeTool.getFullTime(getSeismograms());
            time = fullTime;
        }
        return getIterator(fullTime);
    }

    public SeismogramIterator getIterator(MicroSecondTimeRange timeRange) {
        SoftReference iteratorReference = null;
        synchronized(threadToIterator) {
            iteratorReference = (SoftReference)threadToIterator.get(Thread.currentThread());
        }
        if(iteratorReference != null) {
            SeismogramIterator it = (SeismogramIterator)iteratorReference.get();
            if(it != null && it.hasNext()) {
                if(it.getTimeRange().equals(timeRange)) {
                    return it;
                } else if(timeRange != null) {
                    it.setTimeRange(timeRange);
                    return it;
                }
            }
        }
        SeismogramIterator it = new SeismogramIterator(getDataSetSeismogram().getName(),
                                                       getSeismograms(),
                                                       timeRange);
        synchronized(threadToIterator) {
            threadToIterator.put(Thread.currentThread(), new SoftReference(it));
        }
        return it;
    }

    public void endTimeChanged() {
        synchronized(softSeis) {
            synchronized(threadToIterator) {
                softSeis.clear();
                threadToIterator.clear();
            }
        }
    }

    public void beginTimeChanged() {
        synchronized(softSeis) {
            synchronized(threadToIterator) {
                softSeis.clear();
                threadToIterator.clear();
            }
        }
    }

    protected void addSeismograms(LocalSeismogramImpl[] seismograms) {
        boolean newData = false;
        LinkedList badSeis = null;
        synchronized(softSeis) {
            for(int j = 0; j < seismograms.length; j++) {
                LocalSeismogramImpl[] curSeis = getSeismograms(false);
                RequestFilter[] needed = {getDataSetSeismogram().getRequestFilter()};
                RequestFilter[] uncovered = CoverageTool.notCovered(needed,
                                                                    curSeis);
                boolean satisfiesUncovered = false;
                for(int i = 0; i < uncovered.length; i++) {
                    if(DCDataSetSeismogram.intersects(uncovered[i],
                                                      seismograms[j])) {
                        satisfiesUncovered = true;
                    }
                }
                if(satisfiesUncovered) {
                    Iterator it = softSeis.iterator();
                    while(it.hasNext()) {
                        LocalSeismogramImpl cur = (LocalSeismogramImpl)((SoftReference)it.next()).get();
                        if(cur != null
                                && DataSetSeismogram.equalOrContains(cur,
                                                                     seismograms[j]))
                            it.remove();
                    }
                    if(seismograms[j].isDataDecodable()) {
                        softSeis.add(new SoftReference(seismograms[j]));
                        newData = true;
                    } else {
                        if(badSeis == null) {
                            badSeis = new LinkedList();
                        }
                        badSeis.add(seismograms[j]);
                    }
                }
            }
        }
        if(newData) {
            time = null;
            noData = false;
            SeismogramContainerListener[] listArray;
            synchronized(listeners) {
                listArray = new SeismogramContainerListener[listeners.size()];
                listeners.toArray(listArray);
            }
            for(int i = 0; i < listArray.length; i++) {
                listArray[i].updateData();
            }
            synchronized(threadToIterator) {
                threadToIterator.clear();
            }
            for(int i = 0; i < listArray.length; i++) {
                listArray[i].updateData();
            }
        }
        if(badSeis != null) {
            GlobalExceptionHandler.handle(new CodecException("Got "
                    + badSeis.size()
                    + " seismograms that couldn't be decompressed for "
                    + ChannelIdUtil.toString(getDataSetSeismogram().getRequestFilter().channel_id)));
        }
    }

    public LocalSeismogramImpl[] getSeismograms() {
        return getSeismograms(true);
    }

    private LocalSeismogramImpl[] getSeismograms(boolean retrieveOnEmpty) {
        if(getDataSetSeismogram() instanceof MemoryDataSetSeismogram) {
            noData = false;
            return ((MemoryDataSetSeismogram)getDataSetSeismogram()).getCache();
        }
        boolean callRetrieve = false;
        List existant = new ArrayList();
        LocalSeismogramImpl[] seis = EMPTY_ARRAY;
        synchronized(softSeis) {
            if(softSeis.size() == 0 && retrieveOnEmpty) {
                if(retrievedTime == null || (!retrievedTime.equals(getDSSTime()) && getDataStatus() != NO_DATA)) {
                    callRetrieve = true;
                }
            } else {
                Iterator it = softSeis.iterator();
                while(it.hasNext()) {
                    SoftReference current = (SoftReference)it.next();
                    Object o = current.get();
                    if(o != null) {
                        existant.add(o);
                    } else {
                        callRetrieve = true;
                        it.remove();
                    }
                }
                seis = new LocalSeismogramImpl[existant.size()];
                existant.toArray(seis);
            }
        }
        if(callRetrieve) {
            time = null;
            retrievedTime = getDSSTime();
            getDataSetSeismogram().retrieveData(this);
        }
        return seis;
    }

    private MicroSecondTimeRange getDSSTime() {
        return new MicroSecondTimeRange(getDataSetSeismogram().getBeginMicroSecondDate(),
                                        getDataSetSeismogram().getEndMicroSecondDate());
    }

    private List softSeis = Collections.synchronizedList(new ArrayList());

    private Map threadToIterator = Collections.synchronizedMap(new HashMap());

    private static final LocalSeismogramImpl[] EMPTY_ARRAY = {};

    private MicroSecondTimeRange time, retrievedTime;
}