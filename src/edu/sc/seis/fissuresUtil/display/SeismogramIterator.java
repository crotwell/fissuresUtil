/**
 * SeismogramIterator.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.display;

import java.util.*;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.fissuresUtil.bag.Statistics;
import edu.sc.seis.fissuresUtil.exceptionHandlerGUI.GlobalExceptionHandler;
import java.lang.ref.SoftReference;
import org.apache.log4j.Category;

/** Takes an array of LocalSeismograms and iterates through them, point by point
 */
public class SeismogramIterator implements Iterator{
    public SeismogramIterator(LocalSeismogramImpl[] seismograms){
        this(seismograms, DisplayUtils.getFullTime(seismograms));
    }

    public SeismogramIterator(LocalSeismogramImpl[] seismograms,
                              MicroSecondTimeRange timeRange){
        this.seismograms = DisplayUtils.sortByDate(seismograms);
        MicroSecondDate startTime = seismograms[0].getBeginTime();
        MicroSecondDate endTime = seismograms[seismograms.length - 1].getEndTime();
        TimeInterval sampling = seismograms[0].getSampling().getPeriod();
        seisTimeRange = new MicroSecondTimeRange(startTime,
                                                 endTime);
        addToIterateList(seismograms[0], 0, seismograms[0].getNumPoints());
        for(int i = 1; i < seismograms.length; i++){
            LocalSeismogramImpl current = seismograms[i];
            LocalSeismogramImpl prev = seismograms[i-1];
            if(DisplayUtils.areOverlapping(prev,current)){
                MicroSecondDate currentStartTime = prev.getEndTime().add(sampling);
                MicroSecondDate currentEndTime = current.getEndTime();
                MicroSecondTimeRange currentTR = new MicroSecondTimeRange(currentStartTime,
                                                                          currentEndTime);
                int[] points = DisplayUtils.getSeisPoints(current, currentTR);
                addToIterateList(current, points[0], current.getNumPoints());
            }else if(DisplayUtils.areContiguous(prev, current)){
                addToIterateList(current, 0, current.getNumPoints());
            }else{//are seperated
                TimeInterval difference = current.getBeginTime().difference(prev.getEndTime());
                TimeInterval convSampling = (TimeInterval)sampling.convertTo(UnitImpl.MICROSECOND);
                double gapPoints = difference.divideBy(convSampling).getValue();
                addToIterateList(new Gap((int)gapPoints), 0, (int)gapPoints);
                addToIterateList(current, 0, current.getNumPoints());
            }
        }
        setTimeRange(timeRange);
    }

    public QuantityImpl getValueAt(int position){
        try{
            Object[] seisAndPoint = getSeisAtWithInternal(position);
            LocalSeismogramImpl seis = (LocalSeismogramImpl)seisAndPoint[0];
            Integer point = (Integer)seisAndPoint[1];
            return seis.getValueAt(point.intValue());
        }catch(CodecException e){
            GlobalExceptionHandler.handleStatic(e);
        }
        return null;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration.
     * @exception NoSuchElementException iteration has no more elements.
     */
    public Object next() {
        if(currentPoint < lastPoint){
            return getValueAt(currentPoint++);
        }
        throw new NoSuchElementException();
    }

    /**
     * Returns <tt>true</tt> if the iteration has more elements. (In other
     * words, returns <tt>true</tt> if <tt>next</tt> would return an element
     * rather than throwing an exception.)
     *
     * @return <tt>true</tt> if the iterator has more elements.
     */
    public boolean hasNext() {
        if(currentPoint < lastPoint){
            return true;
        }
        return false;
    }

    /**
     *  Optional part of the iterator interface that does not make sense for
     * iterating over seismograms.  This method does nothing.
     */
    public void remove() {}

    public int getNumPoints(){ return numPoints; }

    public MicroSecondTimeRange getTimeRange(){ return timeRange; }

    public MicroSecondTimeRange getSeisTime() { return seisTimeRange; }

    public void setTimeRange(MicroSecondTimeRange timeRange){
        this.timeRange = timeRange;
        if(timeRange.getBeginTime().equals(seisTimeRange.getBeginTime())){
            currentPoint = 0;
        }else{
            currentPoint = (int)DisplayUtils.linearInterp(seisTimeRange.getBeginTime().getMicroSecondTime(),
                                                          seisTimeRange.getEndTime().getMicroSecondTime(),
                                                          numPoints,
                                                          timeRange.getBeginTime().getMicroSecondTime());

        }
        if(timeRange.getEndTime().equals(seisTimeRange.getEndTime())){
            lastPoint = numPoints;
        }else{
            lastPoint = (int)DisplayUtils.linearInterp(seisTimeRange.getBeginTime().getMicroSecondTime(),
                                                       seisTimeRange.getEndTime().getMicroSecondTime(),
                                                       numPoints,
                                                       timeRange.getEndTime().getMicroSecondTime());
        }
    }

    public LocalSeismogramImpl[] getSeismograms(){ return seismograms; }

    public double[] minMaxMean(int startPoint, int endPoint){
        double max = Double.NEGATIVE_INFINITY;
        double min = Double.POSITIVE_INFINITY;
        double meanStore = 0;
        for(int i = startPoint; i < endPoint; i++){
            Object[] array = getSeisAtWithInternal(startPoint);
            LocalSeismogramImpl current = (LocalSeismogramImpl)array[0];
            int internalStartPoint = ((Integer)array[1]).intValue();
            if(!(current instanceof Gap)){
                int lastPoint = ((int[])points.get(current))[1];
                if((lastPoint - internalStartPoint) + i >= endPoint){
                    lastPoint = internalStartPoint + (endPoint - i);
                }
                Statistics curStat = getStatistics(current);
                double[] curMinMaxMean = curStat.minMaxMean(internalStartPoint,
                                                            lastPoint);
                if(curMinMaxMean[0] < min){
                    min = curMinMaxMean[0];
                }
                if(curMinMaxMean[1] > max){
                    max = curMinMaxMean[1];
                }
                meanStore += curMinMaxMean[2]*(lastPoint-i);
                i +=lastPoint - internalStartPoint;
            }
        }
        double[] minMaxMean = {min, max, meanStore/(endPoint - startPoint)};
        return minMaxMean;
    }

    public boolean equals(Object other){
        if(this == other) return true;
        if (getClass() != other.getClass()) return false;
        SeismogramIterator otherIterator = (SeismogramIterator)other;
        if(timeRange.equals(otherIterator.getTimeRange())){
            LocalSeismogramImpl[] otherSeismograms = otherIterator.getSeismograms();
            if(otherSeismograms.length == seismograms.length){
                for(int i = 0; i < seismograms.length; i++){
                    if(!seismograms[i].get_id().equals(otherSeismograms[i].get_id())){
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public int hashCode(){
        int result = 52;
        result = 37*result + timeRange.hashCode();
        for(int i = 0; i < seismograms.length; i++){
            result = 37*result + seismograms[i].get_id().hashCode();
        }
        return result;
    }

    private Object[] getSeisAtWithInternal(int position){
        Iterator it = iterateList.iterator();
        int curPosition = 0;
        while(it.hasNext()){
            Object current = it.next();
            int[] curPoints = (int[])points.get(current);
            if((curPoints[1] - curPoints[0]) + curPosition > position){
                Object[] seisWithInternal = { current, new Integer(position - curPosition) };
                return seisWithInternal;
            }
            curPosition += curPoints[1] - curPoints[0];
        }
        return null;

    }

    private void addToIterateList(LocalSeismogramImpl toBeAdded, int firstPoint,
                                  int lastPoint){
        iterateList.add(toBeAdded);
        if(firstPoint < 0){
            firstPoint = 0;
        }
        if(lastPoint > toBeAdded.getNumPoints()){
            lastPoint = toBeAdded.getNumPoints();
        }
        int[] addPoints = {firstPoint, lastPoint};
        points.put(toBeAdded,addPoints);
        numPoints += lastPoint - firstPoint;
    }



    private Statistics getStatistics(LocalSeismogramImpl seis){
        SoftReference softStat = (SoftReference)statisticsMap.get(seis);
        Statistics stat;
        if(softStat == null){
            stat = new Statistics(seis);
            statisticsMap.put(seis, new SoftReference(stat));
        }else{
            stat = (Statistics)softStat.get();
            if(stat == null){
                stat = new Statistics(seis);
                statisticsMap.put(seis, new SoftReference(stat));
            }
        }
        return stat;
    }

    private Map statisticsMap = new HashMap();

    private static Category logger =
        Category.getInstance(SeismogramIterator.class.getName());

    private MicroSecondTimeRange timeRange;

    private List iterateList = new ArrayList();

    private Map points = new HashMap();

    private int currentPoint = 0;

    private int lastPoint = 0;

    private int numPoints = 0;

    private LocalSeismogramImpl[] seismograms;

    private MicroSecondTimeRange seisTimeRange;

    public static QuantityImpl NOT_A_NUMBER = new QuantityImpl(Double.NaN,
                                                               UnitImpl.COUNT);

    private class Gap extends LocalSeismogramImpl{
        private Gap(int length){
            this.length = length;
        }

        public QuantityImpl getValueAt(int position){ return NOT_A_NUMBER; }

        public int getNumPoints(){ return length; }

        private int length;
    }
}
