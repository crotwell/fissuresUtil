package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.seismogramDC.UnsupportedDataEncoding;
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
 * @version
 */

public class SeismogramShape implements Shape, Plotter {
    /**
     * 
     * @param color the color of the seismogram
     */
    public SeismogramShape (DataSetSeismogram seis, Color color){
	this.dss = seis;
	this.seis = seis.getSeismogram();
	this.color = color;
    }
    
    /**
     * Draws this <code>SeismogramShape</code>on the supplied graphics after updating it based on the TimeSnapshot and AmpSnapshot
     *
     */
    public void draw(Graphics2D canvas, Dimension size, TimeSnapshot timeState, AmpSnapshot ampState){
	setPlot(timeState.getTimeRange(dss), ampState.getAmpRange(dss), size);
	canvas.setColor(color);
	canvas.draw(this);
    }

    /**
     * Recalculates the values of this SeismogramShape based on the values passed
     *
     * @param time new time range
     * @param amp new amp range
     * @param size size of the drawing surface
     * @return this <code>SeismogramShape</code>
     */
    private Shape setPlot(MicroSecondTimeRange time, UnitRangeImpl amp, Dimension size){
	try{
	    Date plotBegin = new Date();
	    if(plotSize == null || size.width != plotSize.width || 
	       size.height != plotSize.height || time.getInterval().getValue() != plotInterval){
		getEdgeValues(time, size);
		samplesPerPixel = (seisEnd - seisStart)/(double)(endPixel - startPixel);
		maxAmp = amp.getMaxValue();
		minAmp = amp.getMinValue();
		range = maxAmp - minAmp;
		offset = 0;
		seisOffset = 0;
		points = new int[2][size.width];
		plotCompress(size.height);
	    }else{
		dragPlot(time, size);
	    }
	    Date plotEnd = new Date();
	    plotTime = time.getBeginTime().getMicroSecondTime();
	    plotInterval = time.getInterval().getValue();
	    plotAmp = amp;
	    plotSize = size;
	    //System.out.println("plot time: " + (plotEnd.getTime() - plotBegin.getTime()));
	}catch(UnsupportedDataEncoding e){ e.printStackTrace(); }
	return this;
    }     
    
    public void setVisibility(boolean b){visible = b; }

    public void toggleVisibility(){ visible = !visible; }

    public boolean getVisibility(){ return visible; }

    public DataSetSeismogram getSeismogram() { return dss; }

    /**
     * <code>dragPlot</code> shifts the array values over by the percentage change of time in the seismogram, and calculates the values
     * revealed by moving over the edges.  The offset is positive if the seismogram is scrolling right, and negative if it's scrolling
     * left
     * @param time the current time for the seismogram
     * @param size the current pixel size of the rendering surface 
     * @exception UnsupportedDataEncoding if an error occurs
     */
    private void dragPlot(MicroSecondTimeRange time, Dimension size) throws UnsupportedDataEncoding{
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
	
	System.out.println(adjustedTimeRange.getBeginTime());
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
	System.out.println("shift: " + shift + " startPixel: " + startPixel + " endPixel: " + endPixel + " start: " + start + 
			   " end: " + end + " seisStart: " + seisStart + " seisEnd: " + seisEnd); 
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

    private void plotCompress(int height) throws UnsupportedDataEncoding{
	for(int i = startPixel; i < endPixel; i++){
	    calculatePixel(i, height);
	}
    }
    
    private void getEdgeValues(MicroSecondTimeRange time, Dimension size){
	if(seis.getEndTime().before(time.getBeginTime()) || seis.getBeginTime().after(time.getEndTime()) || !visible) {
	    startPixel = 0;
	    endPixel = 0;
	    return;
	}
	seisStart = getPixel(seis.getNumPoints(), seis.getBeginTime(), seis.getEndTime(), time.getBeginTime());
	if(seisStart < 0){
	    seisStart = 0;
	}
	seisEnd = getPixel(seis.getNumPoints(), seis.getBeginTime(), seis.getEndTime(), time.getEndTime());
	if(seisEnd >= seis.getNumPoints()){
	    seisEnd = seis.getNumPoints() - 1;
	}
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
    }
    
    private void calculatePixel(int pixel, int height)throws UnsupportedDataEncoding{
	int start = (int)(seisStart + samplesPerPixel * (pixel - startPixel));
	int end = (int)(start + samplesPerPixel);
	if(start < 0 || end >= seis.getNumPoints()){
	   System.out.println("attempted to calculate outside of seismogram point boundaries");
	   return;
	}
	double minValue = seis.getValueAt(start).getValue();
	double maxValue = minValue;
	for(int j = start + 1; j < end; j++) {
	    double curValue = seis.getValueAt(j).getValue();
	    if(curValue < minValue){ 
		minValue = curValue;
	    }
	    if(curValue > maxValue){
		maxValue = curValue;
	    }
	}
	points[0][pixel] = (int)((minValue  - minAmp)/range * height);
	points[1][pixel] = (int)((maxValue - minAmp)/range * height);
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
	return new SeismogramShapeIterator(points, startPixel, endPixel); 
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

    protected double plotInterval;
       
    /** holds the fractional offset leftover from the previous drag calculation
     */
    private double offset, seisOffset;
    
    protected long plotTime;
    
    protected UnitRangeImpl plotAmp;

    protected Dimension plotSize;
   
    protected LocalSeismogramImpl seis;

    protected DataSetSeismogram dss;
    
    protected boolean visible = true;
}// SeismogramShape
