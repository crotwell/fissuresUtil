/**
 * SeismogramIterator.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.display;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.apache.log4j.Logger;
import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.fissuresUtil.bag.Statistics;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.time.RangeTool;
import edu.sc.seis.fissuresUtil.time.SortTool;

/** Takes an array of LocalSeismograms and iterates through them, point by point
 */
public class SeismogramIterator implements Iterator{
    public SeismogramIterator(String name, LocalSeismogramImpl[] seismograms){
        this(name, seismograms, RangeTool.getFullTime(seismograms));
    }

    public SeismogramIterator(String name,
                              LocalSeismogramImpl[] seismograms,
                              MicroSecondTimeRange timeRange){
        this.name = name;
        if(seismograms.length > 0){
            LocalSeismogramImpl[] seis = this.seismograms = SortTool.sortByDate(seismograms);
            MicroSecondDate startTime = seis[0].getBeginTime();
            MicroSecondDate endTime = seis[seis.length - 1].getEndTime();
            TimeInterval samplingInterval = seis[0].getSampling().getPeriod();
            sampling = seis[0].getSampling();
            unit = seis[0].getUnit();
            seisTimeRange = new MicroSecondTimeRange(startTime, endTime);
            addToIterateList(seis[0], 0, seis[0].getNumPoints());
            for(int i = 1; i < seis.length; i++){
                LocalSeismogramImpl current = seis[i];
                LocalSeismogramImpl prev = seis[i-1];
                if(RangeTool.areOverlapping(prev,current)){
                    MicroSecondDate currentStartTime = prev.getEndTime().add(samplingInterval);
                    MicroSecondDate currentEndTime = current.getEndTime();
                    MicroSecondTimeRange currentTR = new MicroSecondTimeRange(currentStartTime,
                                                                              currentEndTime);
                    int[] points = DisplayUtils.getSeisPoints(current, currentTR);
                    addToIterateList(current, points[0], current.getNumPoints());
                }else if(RangeTool.areContiguous(prev, current)){
                    addToIterateList(current, 0, current.getNumPoints());
                }else{//are seperated
                    TimeInterval difference = current.getBeginTime().difference(prev.getEndTime());
                    TimeInterval convSampling = (TimeInterval)samplingInterval.convertTo(UnitImpl.MICROSECOND);
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
            GlobalExceptionHandler.handle(e);
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
        if(currentPoint < endPoint){
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
        if(currentPoint < endPoint){
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

    public int numPointsLeft(){ return endPoint - currentPoint; }

    public int getStartPoint(){ return currentPoint; }

    public MicroSecondTimeRange getTimeRange(){ return timeRange; }

    public MicroSecondTimeRange getSeisTime() { return seisTimeRange; }

    public void setTimeRange(MicroSecondTimeRange timeRange){
        this.timeRange = timeRange;
        if(timeRange == DisplayUtils.ZERO_TIME){
            currentPoint = numPoints;
        }else if(seisTimeRange == DisplayUtils.ZERO_TIME){
            currentPoint = numPoints;
        }else{
            long seisBegin = seisTimeRange.getBeginTime().getMicroSecondTime();
            long seisEnd = seisTimeRange.getEndTime().getMicroSecondTime();
            long timeBegin = timeRange.getBeginTime().getMicroSecondTime();
            long timeEnd = timeRange.getEndTime().getMicroSecondTime();
            currentPoint = (int)DisplayUtils.linearInterp(seisBegin, seisEnd,
                                                          numPoints, timeBegin);
            endPoint = (int)DisplayUtils.linearInterp(seisBegin, seisEnd,
                                                      numPoints, timeEnd);
            if(currentPoint < 0){
                currentPoint = 0;
            }
            if(endPoint > numPoints){
                endPoint = numPoints;
            }
        }
    }

    public LocalSeismogramImpl[] getSeismograms(){ return seismograms; }

    public double[] minMaxMean() {
        return minMaxMean(currentPoint, endPoint);
    }

    public double[] minMaxMean(int startPoint, int endPoint) {
        if(startPoint >= endPoint){
            double[] zeros = { Double.NaN,Double.NaN,Double.NaN};
            return zeros;
        }
        int currentPoint = startPoint;
        double[] minMaxMean ={Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY,0};
        int totalNumCalculated = 0;//number of points over which values have been taken
        if(currentPoint < numPoints && endPoint > 0){
            double meanStore = 0;
            while(currentPoint < endPoint){
                Object[] array = getSeisAtWithInternal(currentPoint);
                LocalSeismogramImpl curSeis = (LocalSeismogramImpl)array[0];
                if(curSeis == null){
                    break;
                }
                if(curSeis != null && !(curSeis instanceof Gap)){
                    int internalStartPoint = ((Integer)array[1]).intValue();
                    int shift = 0;
                    if(internalStartPoint < 0){
                        shift = Math.abs(internalStartPoint);
                        internalStartPoint = 0;
                    }
                    int lastPoint = ((int[])points.get(curSeis))[1];
                    if((lastPoint - internalStartPoint) + currentPoint + shift >= endPoint){
                        lastPoint = internalStartPoint + (endPoint - (currentPoint + shift));
                    }
                    double[] curMinMaxMean = getStatistics(curSeis).minMaxMean(internalStartPoint,
                                                                               lastPoint);
                    if(curMinMaxMean[0] < minMaxMean[0]){
                        minMaxMean[0] = curMinMaxMean[0];
                    }
                    if(curMinMaxMean[1] > minMaxMean[1]){
                        minMaxMean[1] = curMinMaxMean[1];
                    }
                    int curNumCalculated = lastPoint - internalStartPoint;
                    meanStore += curMinMaxMean[2]*curNumCalculated;
                    totalNumCalculated += curNumCalculated;
                    currentPoint += curNumCalculated + shift;
                }else if (curSeis instanceof Gap){
                    currentPoint += ((Gap)curSeis).getNumPoints() - ((Integer)array[1]).intValue();
                }
                minMaxMean[2] = meanStore/totalNumCalculated;
            }
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
        return new Object[2];

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

    public SamplingImpl getSampling(){
        return sampling;
    }

    private Statistics getStatistics(LocalSeismogramImpl seis) {
        Statistics stat = (Statistics)statisticsMap.get(seis);
        if(stat == null){
            try {
            stat = new Statistics(seis);
            statisticsMap.put(seis, stat);
            } catch (FissuresException e) {
                // this should never happen
                throw new RuntimeException("Should never happen becuase this only gets thrown for bad data and SeismogramCOntainer should check for that.", e);
            }
        }
        return stat;
    }

    public UnitImpl getUnit() {
        return unit;
    }

    private Map statisticsMap = new HashMap();

    private static Logger logger = Logger.getLogger(SeismogramIterator.class);

    private List iterateList = new ArrayList();

    private Map points = new HashMap();

    private int currentPoint, numPoints,endPoint;

    private LocalSeismogramImpl[] seismograms;

    private MicroSecondTimeRange timeRange;

    private MicroSecondTimeRange seisTimeRange;

    private String name;

    private SamplingImpl sampling;

    private UnitImpl unit;

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

