package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.utility.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.lang.ref.*;
import java.awt.image.*;
import java.util.*;

/**
 * SeismogramImage.java
 *
 *
 * Created: Wed Jun  5 16:14:29 2002
 *
 * @author Charlie Groves
 * @version 0.1
 */

public class SeismogramImage extends JComponent{
    
    /** Creates a new ClippableSeismogramPlotter with the color specified.
     */
    public SeismogramImage(HashMap seis, TimeRangeConfig tr, AmpRangeConfig ar){
	this.seis = seis;
	this.timeRange = tr;
	this.ampRange = ar;
    }
    
    public void createOverSizedmo(){
	Dimension d = getSize();
	int sizeScale = 5;
	int w = d.width * sizeScale, h = d.height;
	mySize = new Dimension(w, h);
	overSizedmo = createImage(w, h);
	displayInterval = timeRange.getTimeRange().getInterval();
	overSizedGraphic = (Graphics2D)overSizedmo.getGraphics();
	GeneralPath[] seismos = new GeneralPath[seis.size()];
	int j = 0;
	Iterator e = seis.keySet().iterator();
	while(e.hasNext()){
	    LocalSeismogram curr = ((LocalSeismogram)e.next());
	    int[] xPixels = null;
	    int[] yPixels = null;
	    xPixelRef = null;
	    yPixelRef = null; // redoing, so free memory if needed
	    int[][] pixels;
	    overTimeRange = timeRange.getTimeRange(curr).getOversizedTimeRange((sizeScale -1)/2);
	    try{
		pixels = SeisPlotUtil.calculatePlottable(curr, 
							 ampRange.getAmpRange(curr),
							 overTimeRange,
							 mySize);	    
		xPixels = pixels[0];
		yPixels = pixels[1];
		SeisPlotUtil.flipArray(yPixels, mySize.height);
	    
		// create cache
		xPixelRef = new SoftReference(xPixels);
		yPixelRef = new SoftReference(yPixels);
		redoPixels = false;
		seismos[j] = new GeneralPath();
		seismos[j].moveTo(xPixels[0], yPixels[0]);
		for(int i = 1; i < xPixels.length; i++)
		    seismos[j].lineTo(xPixels[i], yPixels[i]);
		overSizedGraphic.setColor((Color)(seis.get(curr)));
		overSizedGraphic.draw(seismos[j]);
		xPixels = null;
		yPixels = null;
		j++;
	    }
	    catch(Exception f){}
	}
    }

    
    public void paint(Graphics g){
	if(redoPixels ||timeRange.getTimeRange().getEndTime().getMicroSecondTime() >= overTimeRange.getEndTime().getMicroSecondTime() || 
	   timeRange.getTimeRange().getBeginTime().getMicroSecondTime() <= overTimeRange.getBeginTime().getMicroSecondTime()|| 
	   displayInterval.getValue() != timeRange.getTimeRange().getInterval().getValue())
	   this.createOverSizedmo();
	currGraphics = (Graphics2D)g;
	double offset = ((timeRange.getTimeRange().getBeginTime().getMicroSecondTime() - 
			  overTimeRange.getBeginTime().getMicroSecondTime())/
			 (double)(overTimeRange.getEndTime().getMicroSecondTime() -overTimeRange.getBeginTime().getMicroSecondTime()) * 
			 mySize.getWidth());
	currGraphics.drawImage(overSizedmo, AffineTransform.getTranslateInstance(-offset, 0.0), null);
    }   
    
    public void setRedo(boolean r){
	this.redoPixels = r; 
    }

    public void setData(TimeRangeConfig tr, AmpRangeConfig ar){
	this.timeRange = tr;
	this.ampRange = ar;
    }
    

    protected Graphics2D currGraphics;
    
    protected HashMap seis;
    
    protected TimeRangeConfig timeRange;

    protected AmpRangeConfig ampRange;
    
    protected SoftReference xPixelRef = null;

    protected SoftReference yPixelRef = null;

    protected boolean redoPixels = true;
    
    protected Image overSizedmo;
    
    protected MicroSecondTimeRange overTimeRange;
    
    protected Graphics2D overSizedGraphic;
    
    protected Dimension mySize;

    protected TimeInterval displayInterval;
}// SeismogramImage
