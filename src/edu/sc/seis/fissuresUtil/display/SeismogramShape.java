package edu.sc.seis.fissuresUtil.display;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.fissuresUtil.bag.Statistics;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.SeisDataChangeEvent;
import edu.sc.seis.fissuresUtil.xml.SeisDataChangeListener;
import edu.sc.seis.fissuresUtil.xml.SeisDataErrorEvent;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.apache.log4j.Category;

/**
 * SeismogramShape.java
 *
 *
 * Created: Fri Jul 26 16:06:52 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version $Id: SeismogramShape.java 3774 2003-04-29 19:56:54Z groves $
 */

public class SeismogramShape implements Shape, SeisDataChangeListener{
    public SeismogramShape(JComponent parent, DataSetSeismogram seis){
        this.parent = parent;
        this.dss = seis;
        dss.addSeisDataChangeListener(this);
        dss.retrieveData(this);
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
    public boolean update(MicroSecondTimeRange time,
                          UnitRangeImpl amp,
                          Dimension size){
        if(seis.length <= 0){
            return false;
        }else{
            SeismogramShapeIterator newIterator = new SeismogramShapeIterator(time,
                                                                              amp,
                                                                              size);
            if(newIterator.isDraggedFrom(currentIterator) &&
               newIterator.hasSimilarAmp(currentIterator)){
                dragPlot(newIterator);
            }else{
                plot(newIterator);
            }
            return true;
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
        iterator.setSeisPoints(DisplayUtils.getSeisPoints(seis[0],
                                                          iterator.getTime()));
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
        System.arraycopy(points[0], dragFrom, points[0], dragFrom + dragAmount, length);
        System.arraycopy(points[1], dragFrom, points[1], dragFrom + dragAmount, length);
        int[] drawnPixels = getPixels(iterator);
        if(drawnPixels[0] < 0|| drawnPixels[1] < 0){
            return;
        }
        int drawStart, drawEnd;
        if(pointsPerPixel <= 2){ //if there are less than 2 points per pixel,
            drawStart = drawnPixels[0];//just replot the whole thing
            drawEnd = drawnPixels[1];
        }else if(dragAmount < 0){
            drawStart = drawnPixels[1] + dragAmount;
            drawEnd = drawnPixels[1];
        }else{
            drawStart = drawnPixels[0];
            drawEnd = dragAmount--;
            ++dragAmount;
        }
        plotPixels(drawStart, drawEnd, iterator);
    }

    public void plotPixels(SeismogramShapeIterator iterator){
        int[] drawnPixels = getPixels(iterator);
        iterator.setDrawnPixels(drawnPixels);
        plotPixels(drawnPixels[0], drawnPixels[1], iterator);
    }

    public synchronized void pushData(SeisDataChangeEvent sdce) {
        LocalSeismogramImpl[] sdceSeis = sdce.getSeismograms();
        List newUnique = new ArrayList();
        for(int i = 0; i < sdceSeis.length; i++){
            boolean matched = false;
            for(int j = 0; j < seis.length; j++){
                if(sdceSeis[i].get_id().equals(seis[j].get_id())){
                    matched = true;
                    break;
                }
            }
            if(!matched){
                newUnique.add(sdceSeis[i]);
            }
        }
        LocalSeismogramImpl[] tmp =
            new LocalSeismogramImpl[seis.length+newUnique.size()];
        Statistics[] tmpStat = new Statistics[tmp.length];
        System.arraycopy(seis, 0, tmp, 0, seis.length);
        System.arraycopy(stat, 0, tmpStat, 0, stat.length);
        for ( int i=0; i<newUnique.size(); i++) {
            tmp[seis.length+i] = (LocalSeismogramImpl)newUnique.get(i);
            tmpStat[seis.length+i] = new Statistics(tmp[seis.length+i]);
            noData = false;
        } // end of for ()
        seis = tmp;
        stat = tmpStat;
        if ( parent != null) {
            parent.repaint();
        } // end of if ()
    }

    public void finished(SeisDataChangeEvent sdce) {
        pushData(sdce);
        finished = true;
    }

    public void error(SeisDataErrorEvent sdce) {
        //do nothing as someone else should handle error notification to user
        logger.warn("Error with data retrieval.", sdce.getCausalException());
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
        double firstPoint = 0;
        double lastPoint = 0;
        try{
            firstPoint = seis[0].getValueAt(startPoint).getValue();
            lastPoint = seis[0].getValueAt(endPoint).getValue();
        }catch(CodecException e){
            logger.debug("Error getting a point from a local seismogram");
            e.printStackTrace();
        }
        double difference = unroundStartPoint - startPoint;
        double value = firstPoint * (1 - difference) + (lastPoint * difference);
        points[0][point] = (int)((value  - minAmp)/range * height);
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
        if(endPoint > seis[0].getNumPoints()){
            endPoint = seis[0].getNumPoints();
        }
        double[] minMax = stat[0].minMaxMean(startPoint,
                                             endPoint);
        points[0][point] = (int)((minMax[0]  - minAmp)/range * height);
        points[1][point] = (int)((minMax[1] - minAmp)/range * height);
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
           seisPoints[0] >= seis[0].getNumPoints()){
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
        if(seisPoints[1] < seis[0].getNumPoints()){
            displayPixels[1] = displayWidth - 1;
        }else{
            displayPixels[1] =
                (int)Math.round((seis[0].getNumPoints() - seisPoints[0])/
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

    public DataSetSeismogram getSeismogram() { return dss; }

    private boolean finished = false;

    private boolean noData = true;

    private static final String NO_DATA = "No data available";

    private static final String GETTING_DATA = "Trying to get data";

    private static final String EMPTY = "";

    private JComponent parent;

    private SeismogramShapeIterator currentIterator;

    protected DataSetSeismogram dss;

    private LocalSeismogramImpl[] seis = new LocalSeismogramImpl[0];

    protected Statistics[] stat = new Statistics[0];

    private static Category logger =
        Category.getInstance(SeismogramShape.class.getName());
}// SeismogramShape


