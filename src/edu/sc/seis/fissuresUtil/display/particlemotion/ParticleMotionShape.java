package edu.sc.seis.fissuresUtil.display.particlemotion;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.LinkedList;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.SeismogramContainer;
import edu.sc.seis.fissuresUtil.display.SeismogramContainerFactory;
import edu.sc.seis.fissuresUtil.display.SeismogramContainerListener;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.SeismogramIterator;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;

/**
 * @author hedx Created on Jul 19, 2005
 */
public class ParticleMotionShape implements Shape, SeismogramContainerListener {

    ParticleMotionShape(SeismogramDisplay parent, SeisContainer cont) {
        this.parent = parent;
        horiz = SeismogramContainerFactory.create(this, cont.getHorz());
        vert = SeismogramContainerFactory.create(this, cont.getVert());
    }

    public void updateData() {
        parent.repaint();
    }

    public boolean update(TimeEvent currentTime,
                          AmpEvent currentAmp,
                          Dimension size) {
        
        if(horiz.getIterator(currentTime.getTime()).numPointsLeft() <= 0
                || vert.getIterator(currentTime.getTime()).numPointsLeft() <= 0) {
            return false;
        } else {
            synchronized(this) {
                ParticleMotionShapeIterator newIterator = new ParticleMotionShapeIterator(currentTime.getTime(),
                                                                                          currentAmp,
                                                                                          size);
                if(currentIterator == null) {
                    plotNew(newIterator, false);
                } else if(newIterator.intersects(currentIterator)) {
                    handleIntersection(newIterator);
                    parent.repaint();
                } else { //this is a repeat of the same drawing.
                    plotNew(newIterator, true);
                }
                currentIterator = newIterator;
            }
        }
        return true;
    }

    /**
     * @param By
     *            the time this method is done, the Iterator would have all the
     *            points that need to be plotted.
     */
    private synchronized void plotNew(ParticleMotionShapeIterator iterator,
                                      boolean reset) {
        iterator.reset(reset);
        MicroSecondTimeRange currentTime = iterator.getTimeRange();
        AmpEvent currentAmp = iterator.getAmp();
        Dimension size = iterator.getSize();
        SeismogramIterator hIt = horiz.getIterator(currentTime);
        UnitImpl horizUnit = hIt.getUnit();
        UnitRangeImpl horizRange = currentAmp.getAmp(horiz.getDataSetSeismogram())
                .convertTo(horizUnit);
        hMin = horizRange.getMinValue();
        hMax = horizRange.getMaxValue();
        SeismogramIterator vIt = vert.getIterator(currentTime);
        UnitImpl vertUnit = vIt.getUnit();
        UnitRangeImpl vertRange = currentAmp.getAmp(vert.getDataSetSeismogram())
                .convertTo(vertUnit);
        
        System.out.println("horizontal range for " + horiz.getDataSetSeismogram().getName() + " is: " + currentAmp.getAmp(horiz.getDataSetSeismogram()));
        System.out.println("vertical range for " + vert.getDataSetSeismogram().getName() + " is: " + currentAmp.getAmp(vert.getDataSetSeismogram()));
        vMin = vertRange.getMinValue();
        vMax = vertRange.getMaxValue();
        boolean prevPointBad = true;//first point needs to be moved
        int count = 0;
//        double minRatio = hMin/vMin;
//        double maxRatio = hMax/vMax;
//        System.out.println("min ratio is: " + minRatio);
//        System.out.println("max ratio is: " + maxRatio);
        
        while(hIt.hasNext() && vIt.hasNext()) {
            double hVal = getVal(hIt.next(), hMin, hMax, size.height);
            double vVal = getVal(vIt.next(), vMin, vMax, size.height);
//            if(cout<5){
//                System.out.println("new ratio is: " + hVal/vVal);
//            }
            if(hVal == Integer.MAX_VALUE || vVal == Integer.MAX_VALUE) {
                prevPointBad = true;
            } else {
                vVal *= -1;
                vVal += size.height;
                if(prevPointBad) {
                    iterator.add(hVal, vVal, SEG_MOVETO);
                    prevPointBad = false;
                } else {
                    iterator.add(hVal, vVal, SEG_LINETO);
                }
            }
            cout++;
        }
        iterator.finish();
    }

    private double getVal(Object obj, double minAmp, double maxAmp, int height) {
        double itVal = ((QuantityImpl)obj).getValue();
        if(Double.isNaN(itVal)) {//Gap in trace
            itVal = Integer.MAX_VALUE;
        } else {
            itVal = Math.round(SimplePlotUtil.linearInterp(minAmp,
                                                           0,
                                                           maxAmp,
                                                           height,
                                                           itVal));
        }
        return itVal;
    }

    private void handleIntersection(ParticleMotionShapeIterator iterator) {
        System.out.println("we are handling intersection");
        MicroSecondTimeRange curTime = iterator.getTimeRange();
        MicroSecondTimeRange prevTime = currentIterator.getTimeRange();
        if(curTime.equals(prevTime)) {
            throw new RuntimeException("The Two Time Ranges are Equal!");
        }
        MicroSecondDate beginDate, endDate;
        //first we determine if the user has zoomed out
        if(curTime.getBeginTime().before(prevTime.getBeginTime())
                && curTime.getEndTime().after(prevTime.getEndTime())) {
            System.out.println("we zommed out. ");
//            iterator.copyInfo(currentIterator);
            System.out.println("points in iterator before: " + iterator.size());
            beginDate = curTime.getBeginTime();
            endDate = prevTime.getBeginTime().subtract(ONE_MILLI_SECOND);
            appendBefore(iterator, new MicroSecondTimeRange(beginDate, endDate));
            beginDate = prevTime.getEndTime().add(ONE_MILLI_SECOND);
            endDate = curTime.getEndTime();
            appendAfter(iterator, new MicroSecondTimeRange(beginDate, endDate));
            System.out.println("points in iterator after: " + iterator.size());
        } else if(curTime.getBeginTime().after(prevTime.getBeginTime())
                && curTime.getEndTime().before(prevTime.getEndTime())) {
            System.out.println("we zoomed in");
            iterator.copyInfo(currentIterator);
            System.out.println("points in iterator before: " + iterator.size());
            dropBeginTime(iterator, curTime.getBeginTime());
            dropEndTime(iterator, curTime.getEndTime());
            System.out.println("points in iterator after: " + iterator.size());
        } else if(curTime.getBeginTime().before(prevTime.getBeginTime())
                && curTime.getEndTime().before(prevTime.getEndTime())) {
            System.out.println("we dragged right");
//            iterator.copyInfo(currentIterator);
            System.out.println("points in iterator before: " + iterator.size());
            beginDate = curTime.getBeginTime();
            endDate = prevTime.getBeginTime().subtract(ONE_MILLI_SECOND);
            appendBefore(iterator, new MicroSecondTimeRange(beginDate, endDate));
            dropEndTime(iterator, curTime.getEndTime());
            System.out.println("points in iterator after: " + iterator.size());
        } else if(curTime.getBeginTime().after(prevTime.getBeginTime())
                && curTime.getEndTime().after(prevTime.getEndTime())) {
            System.out.println("we dragged left");
            iterator.copyInfo(currentIterator);
            System.out.println("points in iterator before: " + iterator.size());
            dropBeginTime(iterator, curTime.getBeginTime());
            beginDate = prevTime.getEndTime().add(ONE_MILLI_SECOND);
            endDate = curTime.getEndTime();
            appendAfter(iterator, new MicroSecondTimeRange(beginDate, endDate));
            System.out.println("points in iterator after: " + iterator.size());
        }
        //by now all the points have been updated and are ready to draw
    }


    private void appendBefore(ParticleMotionShapeIterator iterator,
                              MicroSecondTimeRange currentTime) {
        AmpEvent currentAmp = iterator.getAmp();
//        iterator.reset(true); //this clears the list
        SeismogramIterator hIt = horiz.getIterator(currentTime);
        UnitImpl horizUnit = hIt.getUnit();
        UnitRangeImpl horizRange = currentAmp.getAmp(horiz.getDataSetSeismogram())
                .convertTo(horizUnit);
        double hMin = horizRange.getMinValue();
        double hMax = horizRange.getMaxValue();
        SeismogramIterator vIt = vert.getIterator(currentTime);
        UnitImpl vertUnit = vIt.getUnit();
        UnitRangeImpl vertRange = currentAmp.getAmp(vert.getDataSetSeismogram())
                .convertTo(vertUnit);
        double vMin = vertRange.getMinValue();
        double vMax = vertRange.getMaxValue();
        boolean prevPointBad = true;
        while(hIt.hasNext() && vIt.hasNext()) {
            double hVal = getVal(hIt.next(),
                                 hMin,
                                 hMax,
                                 iterator.getSize().height);
            double vVal = getVal(vIt.next(),
                                 vMin,
                                 vMax,
                                 iterator.getSize().height);
            if(hVal == Integer.MAX_VALUE || vVal == Integer.MAX_VALUE) {
                prevPointBad = true;
            } else {
                vVal *= -1;
                vVal += iterator.getSize().height;
                if(prevPointBad) {
                    iterator.add(hVal, vVal, SEG_MOVETO);
                    prevPointBad = false;
                } else {
                    iterator.add(hVal, vVal, SEG_LINETO);
                }
            }
        }
        iterator.finish(currentTime);
        //now we add back the old data.
        //the items in currentIterator should not be tempered. So we 
        // make a clone instead.
        Iterator iter = currentIterator.cloneList().iterator();
        if(iter.hasNext()) {
            ParticleMotionPoint pt = (ParticleMotionPoint)iter.next();
            //since the initial point in the old dataset is Seg_MoveTo, we need
            // to change it to Seg_Line as to eliminate the gap.
            pt.setType(SEG_LINETO);
            while(iter.hasNext()) {
                iterator.add((ParticleMotionPoint)iter.next());
            }
        }
    }

    private void appendAfter(ParticleMotionShapeIterator iterator,
                             MicroSecondTimeRange currentTime) {
        AmpEvent currentAmp = iterator.getAmp();
        boolean prevPointBad = false;
        SeismogramIterator hIt = horiz.getIterator(currentTime);
        LinkedList additPoints = new LinkedList();
        UnitImpl horizUnit = hIt.getUnit();
        UnitRangeImpl horizRange = currentAmp.getAmp(horiz.getDataSetSeismogram())
                .convertTo(horizUnit);
        double hMin = horizRange.getMinValue();
        double hMax = horizRange.getMaxValue();
        SeismogramIterator vIt = vert.getIterator(currentTime);
        UnitImpl vertUnit = vIt.getUnit();
        UnitRangeImpl vertRange = currentAmp.getAmp(vert.getDataSetSeismogram())
                .convertTo(vertUnit);
        double vMin = vertRange.getMinValue();
        double vMax = vertRange.getMaxValue();
        while(hIt.hasNext() && vIt.hasNext()) {
            double hVal = getVal(hIt.next(),
                                 hMin,
                                 hMax,
                                 iterator.getSize().height);
            double vVal = getVal(vIt.next(),
                                 vMin,
                                 vMax,
                                 iterator.getSize().height);
            if(hVal == Integer.MAX_VALUE || vVal == Integer.MAX_VALUE) {
                prevPointBad = true;
            } else {
                vVal *= -1;
                vVal += iterator.getSize().height;
                if(prevPointBad) {
                    iterator.add(hVal, vVal, SEG_MOVETO);
                    prevPointBad = false;
                } else {
                    iterator.add(hVal, vVal, SEG_LINETO);
                }
            }
        }
        iterator.finish(currentTime);
    }

    private void dropBeginTime(ParticleMotionShapeIterator iterator,
                               MicroSecondDate curTime) {
        Iterator iter = iterator.iterator();
        while(iter.hasNext()) {
            ParticleMotionPoint pt = (ParticleMotionPoint)iter.next();
            if(pt.getRange().getBeginTime().before(curTime)) {
                iter.remove();
            }
        }
        iterator.setInitialMoveTo();
    }

    private void dropEndTime(ParticleMotionShapeIterator iterator,
                             MicroSecondDate curTime) {
        Iterator iter = iterator.iterator();
        while(iter.hasNext()) {
            ParticleMotionPoint pt = (ParticleMotionPoint)iter.next();
            if(pt.getRange().getEndTime().after(curTime)) {
                iter.remove();
            }
        }
    }

    //start of shape implementation
    public Rectangle getBounds() {
        // TODO Auto-generated method stub
        return null;
    }

    public Rectangle2D getBounds2D() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean contains(double arg0, double arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean contains(Point2D arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean intersects(double arg0, double arg1, double arg2, double arg3) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean intersects(Rectangle2D arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean contains(double arg0, double arg1, double arg2, double arg3) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean contains(Rectangle2D arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public PathIterator getPathIterator(AffineTransform at) {
        return getPathIterator(at, 0);
    }

    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        currentIterator.setAT(at);
        return currentIterator;
    }

    //end of shape implementation
    private static final byte SEG_LINETO = (byte)PathIterator.SEG_LINETO;

    private static final byte SEG_MOVETO = (byte)PathIterator.SEG_MOVETO;

    private SeismogramDisplay parent;

    private SeismogramContainer horiz, vert;

    private ParticleMotionShapeIterator currentIterator;

    private TimeInterval ONE_MILLI_SECOND = new TimeInterval(1,
                                                             UnitImpl.MILLISECOND);

    private double hMin, hMax, vMin, vMax;
    
    int cout = 0;

}
