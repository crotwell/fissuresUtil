package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.bag.Statistics;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.apache.log4j.Category;

/**
 * SeismogramShape.java
 *
 *
 * Created: Fri Jul 26 16:06:52 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version $Id: SeismogramShape.java 3324 2003-02-21 19:21:43Z groves $
 */

public class SeismogramShape implements Shape{
    public SeismogramShape(DataSetSeismogram seis){
        this.dss = seis;
        this.stat = new Statistics(dss.getSeismogram());
    }
    
    /**
     * Method update changes the current plot for the seismogram held by this
     * object to be over the passed in variables
     *
     * @param    time specifies the time range for the plot
     * @param    amp specifies the amp range for the plot
     * @param    size specifies the dimension of the plot
     *
     */
    public void update(MicroSecondTimeRange time,
                       UnitRangeImpl amp,
                       Dimension size){
        //System.out.println(amp);
        SeismogramShapeIterator newIterator = new SeismogramShapeIterator(time,
                                                                          amp,
                                                                          size);
        if(newIterator.isDraggedFrom(currentIterator) &&
           newIterator.hasSimilarAmp(currentIterator)){
            dragPlot(newIterator);
            return;
        }else{
            plot(newIterator);
        }
    }
    
    /**
     * <code>plot</code> sets up the iterator passed in to draw the seismogram
     * held by this seismogram shape
     *
     * @param    an <code>iterator</code> that must have its time, amp range
     * and size set.  When the method is finished, it will contain the points
     * to be drawn and the range over which they will be drawn.
     *
     */
    public void plot(SeismogramShapeIterator iterator){
        iterator.setSeisPoints(DisplayUtils.getSeisPoints(dss.getSeismogram(),
                                                          iterator.getTime()));
        iterator.setBaseSeisPoint();
        iterator.setPointsPerPixel();
        if(iterator.getPointsPerPixel() <= 1){
            compressPlot(iterator);
        }else{
            iterator.setPoints(new int[2][iterator.getSize().width]);
            plotPixels(iterator);
        }
        currentIterator = iterator;
    }
    
    private void compressPlot(SeismogramShapeIterator iterator){
        System.out.println("Compress Plot");
        
    }
    
    private void dragPlot(SeismogramShapeIterator iterator){
        iterator.copyBasicInfo(currentIterator);
        double shiftPercentage =  getShiftPercentage(currentIterator.getTime(),
                                                     iterator.getTime());
        double pixels = currentIterator.getSize().width * shiftPercentage +
        currentIterator.getLeftoverPixels();
        //checks if the pixel shift is within 1/1000 of being an even pixel
        pixels *= 1000;
        pixels = Math.round(pixels);
        pixels /= 1000;
        int shift = 0;
        if(pixels >= 1){
            shift = (int)Math.floor(pixels);
            drag(shift, 0, iterator);
        }else if(pixels <= -1){
            shift = (int)Math.ceil(pixels);
            drag(shift, -shift, iterator);
        }else{
            iterator.setSeisPoints(currentIterator.getSeisPoints());
            iterator.setDrawnPixels(currentIterator.getDrawnPixels());
            iterator.setTotalShift(currentIterator.getTotalShift() + shift);
        }
        iterator.setLeftoverPixels(pixels - shift);
        currentIterator = iterator;
    }
    
    
    private void drag(int dragAmount, int dragFrom,
                      SeismogramShapeIterator iterator){
        iterator.setTotalShift(currentIterator.getTotalShift() + dragAmount);
        double pointsPerPixel = iterator.getPointsPerPixel();
        int[] seisPoints = currentIterator.getSeisPoints();
        seisPoints[0] =(int)-(iterator.getTotalShift() * pointsPerPixel) +
        iterator.getBaseSeisPoint();
        seisPoints[1] = seisPoints[0] +
            (int)(iterator.getSize().width * pointsPerPixel);
        iterator.setSeisPoints(seisPoints);
        int[][] points = currentIterator.getPoints();
        int length = points[0].length - Math.abs(dragAmount);
        System.arraycopy(points[0], dragFrom, points[0], dragFrom + dragAmount, length);
        System.arraycopy(points[1], dragFrom, points[1], dragFrom + dragAmount, length);
        int[] drawnPixels = getPixels(iterator);
        if(drawnPixels[0] < 0|| drawnPixels[1] < 0){
            return;
        }
        int drawStart, drawEnd;
        if(dragAmount < 0){
            drawStart = drawnPixels[1] + dragAmount;
            drawEnd = drawnPixels[1];
        }else{
            drawStart = drawnPixels[0];
            drawEnd = dragAmount--;
            ++dragAmount;
        }
        /* System.out.println("DragAmount: " + dragAmount + " reDrawStart: " + drawStart +
         " reDrawEnd: " + drawEnd + " DragFrom: " + dragFrom +
         " DrawStart: " + drawnPixels[0] + " DrawEnd: " + drawnPixels[1]+
         " totalShift: " + iterator.getTotalShift() +
         " SeismogramPoints: " + seisPoints[0] + ", " + seisPoints[1]);*/
        plotPixels(drawStart, drawEnd, iterator);
    }
    
    public void plotPixels(SeismogramShapeIterator iterator){
        int[] drawnPixels = getPixels(iterator);
        iterator.setDrawnPixels(drawnPixels);
        plotPixels(drawnPixels[0], drawnPixels[1], iterator);
    }
    
    private void plotPixels(int start, int end, SeismogramShapeIterator iterator){
        if(start >= end || start < 0){
            return;
        }
        int[][] points = iterator.getPoints();
        double pointsPerPixel = iterator.getPointsPerPixel();
        double minAmp = iterator.getAmp().getMinValue();
        double maxAmp = iterator.getAmp().getMaxValue();
        double range = maxAmp - minAmp;
        int height = iterator.getSize().height;
        int totalShift = iterator.getTotalShift();
        for(int i = start; i < end; i++){
            double shift = (i-totalShift)*pointsPerPixel;
            double unroundStartPoint = iterator.getBaseSeisPoint() + shift;
            int startPoint = (int)Math.floor(unroundStartPoint);
            int endPoint = (int)Math.ceil(unroundStartPoint + pointsPerPixel);
            if(startPoint < 0){
                startPoint = 0;
            }
            if(endPoint > dss.getSeismogram().getNumPoints()){
                endPoint = dss.getSeismogram().getNumPoints();
            }
            double[] minMax = stat.minMaxMean(startPoint,
                                              endPoint);
            points[0][i] = (int)((minMax[0]  - minAmp)/range * height);
            points[1][i] = (int)((minMax[1] - minAmp)/range * height);
        }
    }
    
    public static double getShiftPercentage(MicroSecondTimeRange from,
                                            MicroSecondTimeRange to){
        long fromBeginTime = from.getBeginTime().getMicroSecondTime();
        long toBeginTime = to.getBeginTime().getMicroSecondTime();
        double toInterval = to.getInterval().getValue();
        return (fromBeginTime - toBeginTime)/toInterval;
    }
    
    
    /**
     * Method getPixels finds the pixels that should be drawn based on the
     * seismogram points in the iterator along with the points per pixel it
     * contains and its pixel width.  The drawn pixels are set in the passed
     * in iterator in addition to being returned
     *
     * @param    iterator containing the information listed above needed to get
     *  the pixels to be drawn to
     *
     * @return   an int[] of length 2 containing a start pixel and an end pixel.
     * The pixels are equal to the point to be drawn if the seismogram is on
     * screen or both equal to -1 otherwise.
     *
     */
    public int[] getPixels(SeismogramShapeIterator iterator){
        int[] displayPixels = new int[2];
        iterator.setDrawnPixels(displayPixels);
        int[] seisPoints = iterator.getSeisPoints();
        double pointsPerPixel = iterator.getPointsPerPixel();
        int displayWidth = iterator.getSize().width;
        double seisPointRange = seisPoints[1] - seisPoints[0];
        if(seisPoints[1] < 0 ||
           seisPoints[0] >= dss.getSeismogram().getNumPoints()){
            return SeismogramShape.setArrayToNegativeOne(displayPixels);
        }
        if(seisPoints[0] >= 0){
            displayPixels[0] = 0;
        }else{
            displayPixels[0] =(int)Math.round(-seisPoints[0]/pointsPerPixel);
            if(displayPixels[0] == displayWidth){
                return SeismogramShape.setArrayToNegativeOne(displayPixels);
            }
        }
        if(seisPoints[1] < dss.getSeismogram().getNumPoints()){
            displayPixels[1] = displayWidth - 1;
        }else{
            displayPixels[1] =
                (int)Math.round((dss.getSeismogram().getNumPoints() - seisPoints[0])/
                                seisPointRange * displayWidth);
        }
        iterator.setDrawnPixels(displayPixels);
        return displayPixels;
    }
    
    private static int[] setArrayToNegativeOne(int[] setee){
        setee[0] = -1;
        setee[1] = -1;
        return setee;
    }
    
    // SHAPE IMPL
    
    public PathIterator getPathIterator(AffineTransform at){
        return getPathIterator(at, 0);
    }
    
    public PathIterator getPathIterator(AffineTransform at, double flatness){
        currentIterator.setAT(at);
        return currentIterator;
    }
    
    public boolean intersects(double x, double y, double w, double h){
        return false;
    }
    
    public boolean intersects(Rectangle2D r){ return false; }
    public boolean contains(double x, double y, double w, double h){
        return false;
    }
    
    public boolean contains(Rectangle2D r){  return false; }
    
    public boolean contains(double x, double y){ return false; }
    
    public boolean contains(Point2D p){ return false; }
    
    public Rectangle getBounds(){ return null;  }
    
    public Rectangle2D getBounds2D(){ return null; }
    
    public DataSetSeismogram getSeismogram() { return dss; }
    
    private SeismogramShapeIterator currentIterator;
    
    protected DataSetSeismogram dss;
    
    protected Statistics stat;
    
    private static Category logger =
    Category.getInstance(SeismogramShape.class.getName());
}// SeismogramShape



