package edu.sc.seis.fissuresUtil.display.particlemotion;

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.Iterator;
import java.util.LinkedList;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;

/**
 * @author hedx Created on Jul 20, 2005
 */
public class ParticleMotionShapeIterator implements PathIterator {

    ParticleMotionShapeIterator(MicroSecondTimeRange timeRange,
                                AmpEvent amp,
                                Dimension size) {
        this.timeRange = timeRange;
        this.amp = amp;
        this.size = size;
        this.index = 0;
        this.firstTime = true;
    }

    public boolean intersects(ParticleMotionShapeIterator otherIter) {
        if(otherIter == null) {
            return false;
        } else if(otherIter.getTimeRange().equals(this.timeRange)) {
            return false;
        } else {
            return otherIter.getTimeRange().intersects(this.timeRange);
        }
    }

    /**
     * finish sets the time ranges for all the points and in addition, centers
     * the drawable.
     */
    public void finish() {
        if(pointsAdded.size() == 0) {
            return;
        }
        if(currentSize == 0) {
            Iterator iter = pointsAdded.iterator();
            ParticleMotionPoint pt = (ParticleMotionPoint)iter.next();
            double xMin = pt.getXVal(), xMax = pt.getXVal();
            double yMin = pt.getYVal(), yMax = pt.getYVal();
            TimeInterval intervalPoint = (TimeInterval)timeRange.getInterval()
                    .divideBy(pointsAdded.size());
            MicroSecondDate begin = timeRange.getBeginTime();
            MicroSecondDate end = begin.add(intervalPoint);
            pt.setRange(new MicroSecondTimeRange(begin, end));
            while(iter.hasNext()) {
                pt = (ParticleMotionPoint)iter.next();
                if(pt.getXVal() > xMax) {
                    xMax = pt.getXVal();
                } else {
                    xMin = pt.getXVal();
                }
                if(pt.getYVal() > yMax) {
                    yMax = pt.getYVal();
                } else {
                    yMin = pt.getYVal();
                }
                begin = end;
                end = begin.add(intervalPoint);
                pt.setRange(new MicroSecondTimeRange(begin, end));
            }
            previousItem.setEndTime(timeRange.getEndTime());
            this.currentSize = pointsAdded.size();
            /*
             * System.out.println("xMin is: " + xMin); System.out.println("xMax
             * is: " + xMax); System.out.println("yMin is: " + yMin);
             * System.out.println("yMax is: " + yMax);
             */
        }
    }

    public void finish(MicroSecondTimeRange newRange) {
        //      now we set the time range for each of these
        if(currentSize < pointsAdded.size() //if more points have been added since last time finish() was called.  
                || currentSize == 0) {  //or if this is the first time finish is called. 
            TimeInterval intervalPoint = (TimeInterval)newRange.getInterval()
                    .divideBy(pointsAdded.size() - currentSize);  //this finds the # of new points added. 
            ParticleMotionPoint pt = (ParticleMotionPoint)pointsAdded.get(currentSize);
            MicroSecondDate begin = newRange.getBeginTime();
            MicroSecondDate end = begin.add(intervalPoint);
            pt.setRange(new MicroSecondTimeRange(begin, end));
            for(int i = currentSize + 1; i < pointsAdded.size(); i++) {
                pt = (ParticleMotionPoint)pointsAdded.get(i);
                begin = end;
                end = begin.add(intervalPoint);
                pt.setRange(new MicroSecondTimeRange(begin, end));
            }
            previousItem.setEndTime(newRange.getEndTime());
            this.currentSize = pointsAdded.size();
        }
    }

    public void add(double hVal, double vVal, byte type) {
        add(hVal, vVal, type, null);
    }

    public void add(double hVal,
                    double vVal,
                    byte type,
                    MicroSecondTimeRange range) {
        if(previousItem == null) {
            add(new ParticleMotionPoint(hVal, vVal, type, range));
        } else if(hVal != previousItem.getXVal()
                && vVal != previousItem.getYVal()) {
            add(new ParticleMotionPoint(hVal, vVal, type, range));
        }
    }

    public void add(ParticleMotionPoint pointToBeAdded) {
        pointsAdded.add(pointToBeAdded);
        this.previousItem = pointToBeAdded;
    }

   
    public ParticleMotionPoint getLastPoint() {
        return previousItem;
    }

    public void copyInfo(ParticleMotionShapeIterator newIterator) {
        this.pointsAdded = newIterator.cloneList();
    }

    public void setInitialMoveTo() {
        if(pointsAdded.size() >= 1) {
            ParticleMotionPoint pt = (ParticleMotionPoint)pointsAdded.get(0);
            pt.setType((byte)SEG_MOVETO);
        }
    }

    public void setInitialLineTo() {
        if(pointsAdded.size() >= 1) {
            ParticleMotionPoint pt = (ParticleMotionPoint)pointsAdded.get(0);
            pt.setType((byte)SEG_LINETO);
        }
    }

    /**
     * Returns a perfectly cloned copy of the Linked List that contains the data
     * points.
     */
    public LinkedList cloneList() {
        if(pointsAdded.size() == 0) {
            return null;
        }
        LinkedList copy = new LinkedList();
        for(int i = 0; i != pointsAdded.size(); i++) {
            ParticleMotionPoint pt = (ParticleMotionPoint)pointsAdded.get(i);
            copy.add(pt.clone());
        }
        return copy;
    }

    public Iterator iterator() {
        return pointsAdded.iterator();
    }

    public void reset(boolean yes) {
        if(yes) {
            pointsAdded.clear();
            currentSize = 0;
        }
    }

    public int size() {
        return pointsAdded.size();
    }

    public MicroSecondTimeRange getTimeRange() {
        return timeRange;
    }

    public AmpEvent getAmp() {
        return amp;
    }

    public Dimension getSize() {
        return size;
    }

    public void setAT(AffineTransform at) {
        this.at = at;
    }

    public AffineTransform getAt() {
        return at;
    }

    //PathIterator implementaiton
    public int getWindingRule() {
        return WIND_EVEN_ODD;
    }

    public boolean isDone() {
        if(firstTime) {
            currentPoint = (ParticleMotionPoint)pointsAdded.get(0);
            index = 1;
            firstTime = false;
        }
        if(index == pointsAdded.size()) {
            return true;
        }
        return false;
    }

   
    public void next() {
        currentPoint = (ParticleMotionPoint)pointsAdded.get(index++);
    }

    public int currentSegment(float[] coordinates) {
        coordinates[0] = (float)currentPoint.getXVal();
        coordinates[1] = (float)currentPoint.getYVal();
        if(at != null) {
            at.transform(coordinates, 0, coordinates, 0, 1);
        }
        return currentPoint.getType();
    }

    public int currentSegment(double[] coordinates) {
        float[] temp = new float[2];
        int val = currentSegment(temp);
        coordinates[0] = temp[0];
        coordinates[1] = temp[1];
        return val;
    }

    public MicroSecondTimeRange getInitialTime() {
        ParticleMotionPoint p = (ParticleMotionPoint)pointsAdded.getFirst();
        return p.getRange();
    }

    public MicroSecondTimeRange getEndTime() {
        ParticleMotionPoint p = (ParticleMotionPoint)pointsAdded.getLast();
        return p.getRange();
    }

    //end of PathIterator implementation
    private MicroSecondTimeRange timeRange;

    private AmpEvent amp;

    private Dimension size;

    private LinkedList pointsAdded = new LinkedList();

    protected AffineTransform at;

    private ParticleMotionPoint currentPoint;

    private boolean firstTime = false;

    private int index;

    private ParticleMotionPoint previousItem;

    private int currentSize;
}
