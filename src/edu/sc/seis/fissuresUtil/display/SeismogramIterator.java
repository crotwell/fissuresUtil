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
import org.apache.log4j.Logger;

/** Takes an array of LocalSeismograms and iterates through them, point by point
 */
public class SeismogramIterator implements Iterator{
    public SeismogramIterator(String name, LocalSeismogramImpl[] seismograms){
        this(name, seismograms, DisplayUtils.getFullTime(seismograms));
    }

    public SeismogramIterator(String name,
                              LocalSeismogramImpl[] seismograms,
                              MicroSecondTimeRange timeRange){
        this.name = name;
        if(seismograms.length > 0){
            this.seismograms = DisplayUtils.sortByDate(seismograms);
            MicroSecondDate startTime = this.seismograms[0].getBeginTime();
            MicroSecondDate endTime = this.seismograms[seismograms.length - 1].getEndTime();
            TimeInterval sampling = this.seismograms[0].getSampling().getPeriod();
            seisTimeRange = new MicroSecondTimeRange(startTime,
                                                     endTime);
            addToIterateList(this.seismograms[0], 0, this.seismograms[0].getNumPoints());
            for(int i = 1; i < this.seismograms.length; i++){
                LocalSeismogramImpl current = this.seismograms[i];
                LocalSeismogramImpl prev = this.seismograms[i-1];
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
        }else{
            this.seismograms = seismograms;
            this.timeRange = DisplayUtils.ZERO_TIME;
            this.seisTimeRange = DisplayUtils.ZERO_TIME;
        }

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
        if(timeRange == DisplayUtils.ZERO_TIME){
            currentPoint = 0;
            lastPoint = 0;
        }else if(seisTimeRange == DisplayUtils.ZERO_TIME){
            currentPoint = 0;
            lastPoint = 0;
        }else{
            long seisBegin = seisTimeRange.getBeginTime().getMicroSecondTime();
            long seisEnd = seisTimeRange.getEndTime().getMicroSecondTime();
            long timeBegin = timeRange.getBeginTime().getMicroSecondTime();
            long timeEnd = timeRange.getEndTime().getMicroSecondTime();
            currentPoint = (int)DisplayUtils.linearInterp(seisBegin, seisEnd,
                                                          numPoints, timeBegin);
            lastPoint = (int)DisplayUtils.linearInterp(seisBegin, seisEnd,
                                                       numPoints, timeEnd);
        }
    }

    public LocalSeismogramImpl[] getSeismograms(){ return seismograms; }

    public double[] minMaxMean(){ return minMaxMean(currentPoint, lastPoint); }

    public double[] minMaxMean(int startPoint, int endPoint){
        double[] minMaxMean ={Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY,0};
        if(startPoint < numPoints && endPoint > 0){
            for(int i = startPoint; i < endPoint; i++){
                Object[] array = getSeisAtWithInternal(startPoint);
                LocalSeismogramImpl current = (LocalSeismogramImpl)array[0];
                int internalStartPoint = ((Integer)array[1]).intValue();
                if(current != null && !(current instanceof Gap)){
                    int shift = 0;
                    if(internalStartPoint < 0){
                        shift = Math.abs(internalStartPoint);
                        internalStartPoint = 0;
                    }
                    int lastPoint = ((int[])points.get(current))[1];
                    if((lastPoint - internalStartPoint) + i + shift >= endPoint){
                        lastPoint = internalStartPoint + (endPoint - (i + shift));
                    }
                    Statistics curStat = getStatistics(current);
                    double[] curMinMaxMean = curStat.minMaxMean(internalStartPoint,
                                                                lastPoint);
                    if(curMinMaxMean[0] < minMaxMean[0]){
                        minMaxMean[0] = curMinMaxMean[0];
                    }
                    if(curMinMaxMean[1] > minMaxMean[1]){
                        minMaxMean[1] = curMinMaxMean[1];
                    }
                    minMaxMean[2] += curMinMaxMean[2]*(lastPoint-i);
                    i +=lastPoint - internalStartPoint + shift;
                }
            }
            minMaxMean[2] = minMaxMean[2]/(endPoint - startPoint);
        }
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

    public String toString(){ return name + " iterator"; }

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
        Statistics stat = (Statistics)statisticsMap.get(seis);
        if(stat == null){
            stat = new Statistics(seis);
            statisticsMap.put(seis, stat);
        }
        return stat;
    }

    private Map statisticsMap = new HashMap();

    private static Logger logger = Logger.getLogger(SeismogramIterator.class);

    private List iterateList = new ArrayList();

    private Map points = new HashMap();

    private int currentPoint = 0;

    private int lastPoint = 0;

    private int numPoints = 0;

    private LocalSeismogramImpl[] seismograms;

    private MicroSecondTimeRange timeRange;

    private MicroSecondTimeRange seisTimeRange;

    private String name;

    //TODO Make SeismogramShape understand drawing NOT_A_NUMBER
    public static final QuantityImpl NOT_A_NUMBER = new QuantityImpl(Double.NaN,
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
