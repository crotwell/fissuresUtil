package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.fissuresUtil.bag.Statistics;
import java.awt.Dimension;
import org.apache.log4j.*;
import java.util.Date;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * SeismogramShape.java
 *
 *
 * Created: Fri Jul 26 16:06:52 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version $Id: SeismogramShape.java 3025 2002-12-19 14:57:41Z groves $
 */

public class SeismogramShape implements Shape, NamedPlotter {
    public SeismogramShape(DataSetSeismogram seis, Color color){
	this(seis, color, seis.toString());
    }

    /**
     * 
     * @param color the color of the seismogram
     */
    public SeismogramShape (DataSetSeismogram seis, Color color, String name){
	this.dss = seis;
	this.seis = seis.getSeismogram();
	this.color = color;
	this.name = name;
	this.stat = new Statistics(this.seis);
    }
    
    public void setVisibility(boolean b){visible = b; }

    public void toggleVisibility(){ visible = !visible; }

    public boolean getVisibility(){ return visible; }

    public DataSetSeismogram getSeismogram() { return dss; }
    
    public String getName(){ return name; }
    
    /**
     * Draws this <code>SeismogramShape</code>on the supplied graphics after updating it based on the TimeEvent and AmpEvent
     *
     */

    public void draw(Graphics2D canvas, Dimension size, TimeEvent tEvent, AmpEvent aEvent){
	if(visible){
	    setPlot(canvas, tEvent.getTime(dss), aEvent.getAmp(dss), size);
	}
    }

    public boolean drawName(Graphics2D canvas, int xPosition, int yPosition){
	if(visible){
	    canvas.setPaint(color);
	    canvas.drawString(name, xPosition, yPosition);
	    return true;
	}
	return false;
    }

    /**
     * Recalculates the values of this SeismogramShape based on the values passed
     *
     * @param time new time range
     * @param amp new amp range
     * @param size size of the drawing surface
     * @return this <code>SeismogramShape</code>
     */
    private Shape setPlot(Graphics2D canvas, MicroSecondTimeRange time, UnitRangeImpl amp, Dimension size){
	try{
	    if(!size.equals(plotSize) || !time.getInterval().equals(plotInterval) || amp.getMaxValue() != maxAmp ||
	       amp.getMinValue() != minAmp){
		getEdgeValues(time, size);
		maxAmp = amp.getMaxValue();
		minAmp = amp.getMinValue();
		range = maxAmp - minAmp;
		offset = 0;
		seisOffset = 0;
		if(samplesPerPixel < 0){
		    plotAll = true;
		    return null;
		}
		if(samplesPerPixel < 1){
		    points = new int[seisEnd - seisStart][2];
		    plotAll(size);
		    plotAll = true;
		}else{
		    points = new int[2][size.width];
		    plotCompress(size.height);
		    plotAll = false;
		}
	    }else if(samplesPerPixel < 1){
		getEdgeValues(time, size);
		plotAll(size);
	    }else{
		dragPlot(time, size);
	    }
	    plotTime = time.getBeginTime().getMicroSecondTime();
	    plotInterval = time.getInterval();
	    plotAmp = amp;
	    plotSize = size;
	    canvas.setColor(color);
	    canvas.draw(this);
	}catch(CodecException e){ e.printStackTrace(); }
	return this;
    } 

    private void plotAll(Dimension size) throws CodecException{
	if(seisEnd <= seis.getNumPoints() && seisStart >= 0){
	    if(seisStart != 0){
		startPixel = 0;
	    }
	    if(seisEnd < seis.getNumPoints()){
		endPixel = size.width;
		seisEnd++;		
	    }
	    points = new int[seisEnd - seisStart][2];
	    samplesPerPixel = (seisEnd - seisStart - 1)/(double)(endPixel - startPixel);
	    for(int i = 0; i < points.length; i++){
		points[i][0] = (int)(i/samplesPerPixel) + startPixel;
		points[i][1] = (int)((seis.getValueAt(i + seisStart).getValue()  - minAmp)/range * size.height);
	    }
	}
	
	
    }
    
    private void plotCompress(int height) throws CodecException{
	for(int i = startPixel; i < endPixel; i++){
	    calculatePixel(i, height);
	}
    }    

    /**
     * <code>dragPlot</code> shifts the array values over by the percentage change of time in the seismogram, and calculates the values
     * revealed by moving over the edges.  The offset is positive if the seismogram is scrolling right, and negative if it's scrolling
     * left
     * @param time the current time for the seismogram
     * @param size the current pixel size of the rendering surface 
     * @exception CodecException if an error occurs
     */
    private void dragPlot(MicroSecondTimeRange time, Dimension size) throws CodecException{
	int start = 0;
	int end = 0;
	offset += ((plotTime - time.getBeginTime().getMicroSecondTime())/
		   time.getInterval().getValue())*size.width;
	int shift = (int)offset;
	offset -= shift;
	double timePixelPercentage = offset/size.width;
	MicroSecondTimeRange adjustedTimeRange = new MicroSecondTimeRange(new MicroSecondDate(time.getBeginTime().getMicroSecondTime() -
											      (long)(timePixelPercentage * 
											      time.getInterval().getValue())),
									  new MicroSecondDate(time.getEndTime().getMicroSecondTime() - 
											      (long)(timePixelPercentage * 
											      time.getInterval().getValue())));
	
	getEdgeValues(adjustedTimeRange, size);
    	if(shift < 0){
	    for(int i = startPixel; i < endPixel + shift; i++){
		points[0][i] = points[0][i - shift];
		points[1][i] = points[1][i - shift];
	    }
	    start = endPixel + shift;
	    end = endPixel;
	}else if(shift > 0){
	    for(int i =  endPixel - 1; i >= startPixel + shift; i--){
		points[0][i] = points[0][i - shift];
		points[1][i] = points[1][i - shift];
	    }
	    start = startPixel;
	    end = startPixel + shift;
	}
	//System.out.println("shift: " + shift + " startPixel: " + startPixel + " endPixel: " + endPixel + " start: " + start + 
	//		   " end: " + end + " seisStart: " + seisStart + " seisEnd: " + seisEnd); 
	if(start < 0){
	    start = 0;
	}
	if(end > endPixel){
	    end = endPixel;
	}
	for(int i = start; i < end; i++){
	    calculatePixel(i, size.height);
	}
    }
    
    private void getEdgeValues(MicroSecondTimeRange time, Dimension size){
	if(seis.getEndTime().before(time.getBeginTime()) || seis.getBeginTime().after(time.getEndTime())) {
	    startPixel = 0;
	    endPixel = 0;
	    return;
	}
	int[] points = DisplayUtils.getSeisPoints(seis, time);
	seisStart = points[0];
	seisEnd = points[1];
	MicroSecondDate temp = getValue(seis.getNumPoints(), seis.getBeginTime(), seis.getEndTime(), seisStart);
	if(temp.getMicroSecondTime() < time.getBeginTime().getMicroSecondTime()){
	    temp = time.getBeginTime();
	}
	startPixel = getPixel(size.width, time.getBeginTime(), time.getEndTime(), temp);
	temp = getValue(seis.getNumPoints(), seis.getBeginTime(), seis.getEndTime(), seisEnd);
	if(temp.getMicroSecondTime() > time.getEndTime().getMicroSecondTime()){
	    temp = time.getEndTime();
	}
	endPixel = getPixel(size.width, time.getBeginTime(), time.getEndTime(), temp);
	samplesPerPixel = (seisEnd - seisStart)/(double)(endPixel - startPixel);
	//System.out.println("Samples: " + (seisEnd - seisStart) + " Pixels: " + (endPixel - startPixel) + " SPP: " + samplesPerPixel);
    }
    
    private void calculatePixel(int pixel, int height)throws CodecException{
	int start = (int)Math.floor(seisStart + samplesPerPixel * (pixel - startPixel));
	if(start < 0){
	    start = 0;
	}
	int end = (int)Math.ceil(start + samplesPerPixel);
	if(end >= seis.getNumPoints()){
	    end = seis.getNumPoints() - 1;
	}
	double[] minMax = stat.minMaxMean(start, end);
	points[0][pixel] = (int)((minMax[0]  - minAmp)/range * height);
	points[1][pixel] = (int)((minMax[1] - minAmp)/range * height);
    }

    /** solves the equation <pre>(yb-ya)/(xb-xa) = (y-ya)/(x-xa)</pre>
     *  for y given x. Useful for finding the pixel for a value given the
     *  dimension of the area and the range of values it is supposed to
     *  cover. Note, this does not check for xa == xb, in which case a
     *  divide by zero would occur.
     */
    public static final double linearInterp(double xa, double ya,
                                      double xb, double yb,
                                      double x) {
        if (x == xa) return ya;
        if (x == xb) return yb;
        return (yb - ya)*(x-xa)/(xb-xa) + ya;
    }

    public static final int getPixel(int totalPixels, 
                               MicroSecondDate begin,
                               MicroSecondDate end,
                               MicroSecondDate value) {
        return (int)Math.round(linearInterp(begin.getMicroSecondTime(),
					    0,
                                            end.getMicroSecondTime(),
                                            totalPixels, 
                                            value.getMicroSecondTime()));
    }

    public static final MicroSecondDate getValue(int totalPixels, 
                                           MicroSecondDate begin,
                                           MicroSecondDate end,
                                           int pixel) {
        double value = 
            linearInterp(0,
                         0,
                         totalPixels,
                         end.getMicroSecondTime()-begin.getMicroSecondTime(),
                         pixel);
        return new MicroSecondDate(begin.getMicroSecondTime() +
                                   Math.round(value));
    }

    public static final int getPixel(int totalPixels, 
                               UnitRangeImpl range,
                               QuantityImpl value) {
        QuantityImpl converted = value.convertTo(range.getUnit());
        return getPixel(totalPixels, range, converted.getValue());
    }

    public static final int getPixel(int totalPixels, 
                               UnitRangeImpl range,
                               double value) {
        return (int)Math.round(linearInterp(range.getMinValue(), 0,
                                            range.getMaxValue(), totalPixels, 
                                            value));
    }

    public static final QuantityImpl getValue(int totalPixels, 
                                    UnitRangeImpl range,
                                    int pixel) {
        double value = linearInterp(0, 
                                    range.getMinValue(),
                                    totalPixels, 
                                    range.getMaxValue(),
                                    pixel);
        return new QuantityImpl(value, range.getUnit());
    }    

    public boolean contains(double x, double y){ return false; }

    public boolean contains(double x, double y, double w, double h){ return false; }

    public boolean contains(Point2D p){ return false; }

    public boolean contains(Rectangle2D r){ return false; }

    public Rectangle getBounds(){ return null; }

    public Rectangle2D getBounds2D(){ return null; }

    public PathIterator getPathIterator(AffineTransform at){ 
	if(plotAll){
	    return new PlotAllIterator(points, at);
	}
	return new PlotCompressIterator(points, startPixel, endPixel, at); 
    }

    public PathIterator getPathIterator(AffineTransform at, double flatness){ 
	return getPathIterator(at); 
    }

    public boolean intersects(double x, double y, double w, double h){ return false; }

    public boolean intersects(Rectangle2D r){ return false; }

    protected int startPixel, endPixel, seisStart, seisEnd;
    
    protected double maxAmp, minAmp, samplesPerPixel, range;

    protected Color color;
    
    protected int[][] points;

    protected TimeInterval plotInterval;
       
    /** holds the fractional offset leftover from the previous drag calculation
     */
    private double offset, seisOffset;
    
    private String name;

    protected long plotTime;
    
    protected UnitRangeImpl plotAmp;

    protected Dimension plotSize;
   
    protected LocalSeismogramImpl seis;

    protected Statistics stat;

    protected DataSetSeismogram dss;
    
    protected boolean visible = true, plotAll;
}// SeismogramShape
