package edu.sc.seis.fissuresUtil.display.drawable;

import edu.sc.seis.fissuresUtil.display.*;

import edu.iris.Fissures.IfNetwork.Sensitivity;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.apache.log4j.Logger;

/**
 * SeismogramShape.java
 *
 *
 * Created: Fri Jul 26 16:06:52 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version $Id: SeismogramShape.java 9829 2004-07-27 19:58:51Z crotwell $
 */

public class SeismogramShape implements Shape, SeismogramContainerListener{

    public SeismogramShape(SeismogramDisplay parent, DataSetSeismogram seis){
        this.parent = parent;
        this.container = new SeismogramContainer(seis);
        container.addListener(this);
    }

    public void updateData(){
        synchronized(this){
            newData = true;
        }
        parent.repaint();
    }

    private boolean newData = false;

    /**
     * Method update changes the current plot for the seismogram held by this
     * object to be over the passed in variables
     *
     * @param    time specifies the time range for the plot
     * @param    amp specifies the amp range for the plot
     * @param    size specifies the dimension of the plot
     */
    public boolean update(MicroSecondTimeRange time, UnitRangeImpl amp,
                          Dimension size){
        if(container.getSeismograms().length <= 0){
            return false;
        }else{
            synchronized(this){
                SeismogramShapeIterator newIterator = new SeismogramShapeIterator(time,
                                                                                  amp,
                                                                                  size);
                if(newIterator.isDraggedFrom(currentIterator) &&
                   newIterator.hasSimilarAmp(currentIterator) && !newData){
                    dragPlot(newIterator);
                }else{
                    plot(newIterator);
                }
                return true;
            }
        }
    }

    public String getDataStatus(){
        return container.getDataStatus();
    }

    public void getData(){ container.getSeismograms(); }

    /**
     * <code>plot</code> sets up the iterator passed in to draw the seismogram
     * held by this seismogram shape
     *
     * @param    an <code>iterator</code> that must have its time, amp range
     * and size set.  When the method is finished, it will contain the points
     * to be drawn and the range over which they will be drawn.
     *
     */
    private void plot(SeismogramShapeIterator iterator){
        boolean plotNewData = false;
        synchronized(this){
            plotNewData= newData;
        }
        iterator.setSeisPoints(DisplayUtils.getPoints(container.getIterator(),
                                                      iterator.getTime()));
        if(plotNewData){
            synchronized(this){
                newData = false;
            }
        }
        iterator.setBaseSeisPoint();
        iterator.setPointsPerPixel();
        iterator.setPoints(new int[2][iterator.getSize().width]);
        plotPixels(iterator);
        currentIterator = iterator;
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
            iterator.setTotalShift(currentIterator.getTotalShift());
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
        System.arraycopy(points[0], dragFrom, points[0],
                         dragFrom + dragAmount, length);
        System.arraycopy(points[1], dragFrom, points[1],
                         dragFrom + dragAmount, length);
        int drawStart, drawEnd;
        int[] drawnPixels = setPixels(iterator);
        if(pointsPerPixel <= 2){ //if there are less than 2 points per pixel,
            plotPixels(drawnPixels[0], drawnPixels[1], iterator);
            return;
        }else if(dragAmount < 0){
            drawStart = drawnPixels[1] + dragAmount;
            drawEnd = drawnPixels[1];
        }else{
            drawStart = drawnPixels[0];
            drawEnd = dragAmount - 1;
        }
        plotPixels(drawStart, drawEnd, iterator);
    }

    private void plotPixels(SeismogramShapeIterator iterator){
        int[] drawnPixels = setPixels(iterator);
        plotPixels(drawnPixels[0], drawnPixels[1], iterator);
    }

    private void plotPixels(int start, int end, SeismogramShapeIterator iterator){
        if(start >= end || start < 0 || start == end){
            return;
        }
        int[][] points = iterator.getPoints();
        double pointsPerPixel = iterator.getPointsPerPixel();
        UnitImpl lseisUnit = container.getIterator().getUnit();
        UnitRangeImpl ampRange = iterator.getAmp();
        // WARNING, the next line may break GEE - 6/7/2004 HPC
        ampRange = ampRange.convertTo(lseisUnit);
        double minAmp = ampRange.getMinValue();
        double maxAmp = ampRange.getMaxValue();
        double range = maxAmp - minAmp;
        int height = iterator.getSize().height;
        int totalShift = iterator.getTotalShift();
        for(int i = start; i < end; i++){
            double shift = (i-totalShift)*pointsPerPixel;
            double unroundStartPoint = iterator.getBaseSeisPoint() + shift;
            if(iterator.getPointsPerPixel() <= 2){
                plotExpansion(unroundStartPoint, points, minAmp, range, height, i);
            }else{
                plotCompression(unroundStartPoint, points, minAmp, range, height,
                                i, pointsPerPixel);
            }
        }
    }

    private void plotExpansion(double unroundStartPoint, int[][] points,
                               double minAmp, double range, int height, int point){
        int startPoint = (int)Math.floor(unroundStartPoint);
        if(startPoint < 0 && startPoint >= -3) {
            startPoint = 0;//if the base point is off a bit, fudge a little
        }
        int endPoint = startPoint + 1;
        //TODO fix point calculation such that these aren't over the number of points
        if(endPoint >= container.getIterator().getNumPoints()){
            endPoint = container.getIterator().getNumPoints() - 1;
            startPoint = endPoint - 1;
        }
        if(startPoint < 0){
            startPoint = 0;
            endPoint =1;
        }
        double firstPoint = height/2;
        double lastPoint = firstPoint;
        firstPoint = container.getIterator().getValueAt(startPoint).getValue();
        lastPoint = container.getIterator().getValueAt(endPoint).getValue();
        double difference = unroundStartPoint - startPoint;
        double value = firstPoint * (1 - difference) + (lastPoint * difference);
        if (container.getDataSetSeismogram().getAuxillaryDataKeys().contains("sensitivity") &&
                ((Sensitivity)container.getDataSetSeismogram().getAuxillaryData("sensitivity")).sensitivity_factor < 0) {
            points[0][point] = (int)((value  - minAmp)/range * height);
        } else {
            points[0][point] = height - (int)((value  - minAmp)/range * height);
        }
        points[1][point] = points[0][point];
    }

    private void plotCompression(double unroundStartPoint, int[][] points,
                                 double minAmp, double range, int height, int point,
                                 double pointsPerPixel){
        int startPoint = (int)Math.floor(unroundStartPoint);
        int endPoint = (int)Math.ceil(unroundStartPoint + pointsPerPixel);
        if(startPoint < 0){
            startPoint = 0;
        }
        SeismogramIterator it = container.getIterator();
        if(endPoint > it.getNumPoints()){
            endPoint = it.getNumPoints();
        }
        double[] minMax = it.minMaxMean(startPoint,endPoint);
        if (container.getDataSetSeismogram().getAuxillaryDataKeys().contains("sensitivity") &&
                ((Sensitivity)container.getDataSetSeismogram().getAuxillaryData("sensitivity")).sensitivity_factor < 0) {
            points[0][point] = (int)((minMax[0]  - minAmp)/range * height);
            points[1][point] = (int)((minMax[1] - minAmp)/range * height);
        } else {
            points[0][point] = height - (int)((minMax[0]  - minAmp)/range * height);
            points[1][point] = height - (int)((minMax[1] - minAmp)/range * height);
        }
    }

    private static double getShiftPercentage(MicroSecondTimeRange from,
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
    private int[] setPixels(SeismogramShapeIterator iterator){
        int[] displayPixels = new int[2];
        int[] seisPoints = iterator.getSeisPoints();
        double pointsPerPixel = iterator.getPointsPerPixel();
        int displayWidth = iterator.getSize().width;
        double seisPointRange = seisPoints[1] - seisPoints[0];
        if(seisPoints[1] < 0 ||
           seisPoints[0] >= container.getIterator().getNumPoints()){
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
        if(seisPoints[1] < container.getIterator().getNumPoints()){
            displayPixels[1] = displayWidth - 1;
        }else{
            displayPixels[1] =
                (int)Math.round((container.getIterator().getNumPoints() - seisPoints[0])/
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

    public boolean intersects(Rectangle2D r){
        return false;
    }

    public boolean contains(double x, double y, double w, double h){
        return false;
    }

    public boolean contains(Rectangle2D r){
        return false; }

    public boolean contains(double x, double y){
        return false;
    }

    public boolean contains(Point2D p){
        return false;
    }

    public Rectangle getBounds(){
        return null;
    }

    public Rectangle2D getBounds2D(){ return null; }

    public DataSetSeismogram getSeismogram() {
        return container.getDataSetSeismogram();
    }

    private SeismogramDisplay parent;

    private SeismogramShapeIterator currentIterator;

    private SeismogramContainer container;

    private static Logger logger = Logger.getLogger(SeismogramShape.class);
}// SeismogramShape

