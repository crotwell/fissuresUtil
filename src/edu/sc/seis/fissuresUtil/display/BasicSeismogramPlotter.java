package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.utility.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.lang.ref.*;

/**
 * BasicSeismogramPlotter is a basic implementation of a seismogram drawing widget.  It takes a seismogram, time range, and amplitude 
 * range and paints the seismogram based on that information.
 *
 *
 * Created: Wed May 22 15:26:07 2002
 *
 * @author Charlie Groves
 * @version
 */

public class BasicSeismogramPlotter extends JComponent implements SeismogramPlotter{
    /** Creates a new BasicSeismogramPlotter with the default color, black.
     */
    public BasicSeismogramPlotter(LocalSeismogram seis, MicroSecondTimeRange timeRange, UnitRangeImpl ampRange){
	this(seis, timeRange, ampRange, Color.black);
    }

    /** Creates a new BasicSeismogramPlotter with the color specified.
     */
    public BasicSeismogramPlotter(LocalSeismogram seis, MicroSecondTimeRange timeRange, UnitRangeImpl ampRange, Color c){
	this.seis = seis;
	this.timeRange = timeRange;
	this.ampRange = ampRange;
	this.lineColor = c;
    }
    
    public void paint(Graphics g){
	currGraphics = (Graphics2D)g;
	Insets insets = getInsets();
	if(currGraphics != null){
	    try{
		AffineTransform insetMove = AffineTransform.getTranslateInstance(insets.left, insets.top);
		currGraphics.transform(insetMove);
		int[] xPixels = null;
                int[] yPixels = null;
                
		if (xPixelRef != null && yPixelRef != null) {
                    xPixels = (int[])xPixelRef.get();
                    yPixels = (int[])yPixelRef.get();
                }
		
		if (redoPixels || xPixels == null || yPixels == null) {
                    xPixelRef = null;
                    yPixelRef = null; // redoing, so free memory if needed

                    Dimension mySize = getSize();
                    mySize.height -= insets.top + insets.bottom;
                    mySize.width -= insets.left + insets.right;
                    int[][] pixels;
                    pixels = SeisPlotUtil.calculatePlottable(seis, 
                                                             ampRange,
							     timeRange,
                                                             mySize);
                    xPixels = pixels[0];
                    yPixels = pixels[1];
                    SeisPlotUtil.flipArray(yPixels, mySize.height);

                    // create cache
                    xPixelRef = new SoftReference(xPixels);
                    yPixelRef = new SoftReference(yPixels);
                    redoPixels = false;
                }
		
		currShape = new GeneralPath();
		currShape.moveTo(xPixels[0], yPixels[0]);
		for(int i = 1; i < xPixels.length; i++)
		    currShape.lineTo(xPixels[i], yPixels[i]);
		currGraphics.setPaint(this.lineColor);
		currGraphics.draw(currShape);
                xPixels = null;
                yPixels = null;
	    } catch (Exception e){
                // problem with the data format, can't display
                redoPixels = true;
                
            } 
        }
    }


    public void setData(UnitRangeImpl a, MicroSecondTimeRange t) 
	throws IncompatibleUnit 
    {
	Assert.isNotNull(a, "Configuration must not be null");
	Assert.isNotNull(t, "LocalSeismogram must not be null");
	this.ampRange = a;
	this.timeRange = t;
	xPixelRef = null; // free memory
	yPixelRef = null; 
	redoPixels = true;
	repaint();

    }

    public void setData(MicroSecondTimeRange t){
	Assert.isNotNull(t, "LocalSeismogram must not be null");
	this.timeRange = t;
	xPixelRef = null; // free memory
	yPixelRef = null; 
	redoPixels = true;
	repaint();
    }
    
    public void setData(UnitRangeImpl a)
	throws IncompatibleUnit 
    {
	Assert.isNotNull(a, "Configuration must not be null");
	this.ampRange = a;
	xPixelRef = null; // free memory
	yPixelRef = null; 
	redoPixels = true;
	repaint();
    } 

    public void check(Rectangle rect){
	if(currGraphics.hit(rect, currShape, false))
	    this.setLineColor(Color.black);
    }
    public void setLineColor(Color c){ this.lineColor = c;}

    public void setTimeRange(MicroSecondTimeRange newTimeRange){ this.timeRange = newTimeRange;}
    
    public MicroSecondTimeRange getTimeRange() { return this.timeRange; }
    
    public void setAmpRange(UnitRangeImpl newAmpRange){ this.ampRange = newAmpRange;}

    public UnitRangeImpl getAmpRange() { return this.ampRange; }

    public LocalSeismogram getSeismogram(){ return this.seis; }
    
    public void setRedo(boolean r){ this.redoPixels = r; }

    protected Graphics2D currGraphics;
    
    protected GeneralPath currShape;
    
    protected LocalSeismogram seis;
    
    protected MicroSecondTimeRange timeRange;

    protected UnitRangeImpl ampRange;
    
    protected Color lineColor;

    protected SoftReference xPixelRef = null;

    protected SoftReference yPixelRef = null;

    protected boolean redoPixels = true;
}// BasicSeismogramPlotter
