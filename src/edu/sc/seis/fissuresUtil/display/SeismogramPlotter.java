package edu.sc.seis.fissuresUtil.display;

import java.awt.geom.GeneralPath;
import java.awt.Shape;
import java.awt.Dimension;
import java.lang.ref.SoftReference;

import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;

//import edu.iris.Fissures.model.*;

/**
 * SeismogramPlotter creates a seismogram shape from a local seismogram, time range config and an amplitude range config.
 * 
 *
 * Created: Wed May 22 14:07:07 2002
 *
 * @author Charlie Groves
 * @version 0.1
 */

public class SeismogramPlotter implements Plotter{
    public SeismogramPlotter(LocalSeismogram seis, TimeRangeConfig trc, AmpRangeConfig arc){
	this.seismogram = seis;
	this.timeConfig = trc;
	this.ampConfig = arc;
    }

    public Shape draw(Dimension size){
	System.out.println("drawing a seismogram");
	int scale = 5;
	int w = size.width, h = size.height;
	Dimension mySize = new Dimension(w, h);
	int[] xPixels = null;
	int[] yPixels = null;
	int[][] pixels;
	MicroSecondTimeRange overTimeRange = timeConfig.getTimeRange(seismogram).getOversizedTimeRange((scale -1)/2);
	try{
	    pixels = SeisPlotUtil.calculatePlottable(seismogram, 
						     ampConfig.getAmpRange(seismogram),
						     overTimeRange,
						     mySize);	    
	    xPixels = pixels[0];
	    yPixels = pixels[1];
	    SeisPlotUtil.flipArray(yPixels, mySize.height);
	    // create cache
	    SoftReference xPixelRef = new SoftReference(xPixels);
	    SoftReference yPixelRef = new SoftReference(yPixels);
	    currentShape = new GeneralPath();
	    currentShape.moveTo(xPixels[0], yPixels[0]);
	    for(int i = 1; i < xPixels.length; i++)
		    currentShape.lineTo(xPixels[i], yPixels[i]);
	}
	catch(Exception e){ e.printStackTrace(); }
	return currentShape;
    }
    
    protected LocalSeismogram seismogram;

    protected GeneralPath currentShape;

    protected TimeRangeConfig timeConfig;
    
    protected AmpRangeConfig ampConfig;
}// SeismogramPlotter
