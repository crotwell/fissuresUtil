package edu.sc.seis.fissuresUtil.display;

import java.awt.geom.GeneralPath;
import java.awt.Shape;
import java.awt.Dimension;
import java.lang.ref.SoftReference;
import org.apache.log4j.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;

/**
 * SeismogramPlotter creates a seismogram shape from a local seismogram, time range config and an amplitude range config.
 * 
 *
 * Created: Wed May 22 14:07:07 2002
 *
 * @author Charlie Groves
 * @version 0.1
 */

public class SeismogramPlotter extends AbstractSeismogramPlotter{
    public SeismogramPlotter(LocalSeismogram seis,  AmpConfigRegistrar arc){
	this.seismogram = seis;
	this.ampConfig = arc;
    }

    public Shape draw(Dimension size, TimeSnapshot imageState){
	if(visible){
	    MicroSecondTimeRange overTimeRange = imageState.getTimeRange(seismogram).
		getOversizedTimeRange(BasicSeismogramDisplay.OVERSIZED_SCALE);
	    try{
		int[][] pixels = SimplePlotUtil.compressYvalues(seismogram, overTimeRange, ampConfig.getAmpRange(seismogram), size);
		SimplePlotUtil.scaleYvalues(pixels, seismogram, overTimeRange, ampConfig.getAmpRange(seismogram), size); 
		SimplePlotUtil.flipArray(pixels[1], size.height);
		int[] xPixels = pixels[0];
		int[] yPixels = pixels[1];
		if(xPixels.length >= 2){
		    GeneralPath currentShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD, xPixels.length - 1);
		    currentShape.moveTo(xPixels[0], yPixels[0]);
		    for(int i = 1; i < xPixels.length; i++)
			currentShape.lineTo(xPixels[i], yPixels[i]);
		    return currentShape;
		}else if(xPixels.length == 1){
		    GeneralPath currentShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 2);
		    currentShape.moveTo(0, yPixels[0]);
		    currentShape.lineTo(size.width, yPixels[0]);
		    return currentShape;		
		}
	    }
	    catch(Exception e){ e.printStackTrace(); }
	}
	return new GeneralPath();
    }

    static Category logger = Category.getInstance(SeismogramPlotter.class.getName());
}// SeismogramPlotter
