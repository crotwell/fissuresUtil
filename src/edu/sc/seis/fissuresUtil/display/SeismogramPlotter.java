package edu.sc.seis.fissuresUtil.display;

import java.awt.*;
import java.awt.geom.GeneralPath;
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
    public SeismogramPlotter(DataSetSeismogram seis, Color color){
	this.seismogram = seis;
	this.color = color;
    }

    public void draw(Graphics2D canvas, Dimension size, TimeSnapshot timeState, AmpSnapshot ampState){
	if(visible){
	    MicroSecondTimeRange overTimeRange = timeState.getTimeRange(seismogram).
		getOversizedTimeRange(BasicSeismogramDisplay.OVERSIZED_SCALE);
	    try{
		int[][] pixels = SimplePlotUtil.compressYvalues(seismogram.getSeismogram(), overTimeRange, 
								ampState.getAmpRange(seismogram), size);
		SimplePlotUtil.scaleYvalues(pixels, seismogram.getSeismogram(), overTimeRange, ampState.getAmpRange(seismogram), size); 
		SimplePlotUtil.flipArray(pixels[1], size.height);
		int[] xPixels = pixels[0];
		int[] yPixels = pixels[1];
		GeneralPath currentShape = new GeneralPath();
		if(xPixels.length >= 2){
		    currentShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD, xPixels.length - 1);
		    currentShape.moveTo(xPixels[0], yPixels[0]);
		    for(int i = 1; i < xPixels.length; i++)
			currentShape.lineTo(xPixels[i], yPixels[i]);
		}else if(xPixels.length == 1){
		    currentShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 2);
		    currentShape.moveTo(0, yPixels[0]);
		    currentShape.lineTo(size.width, yPixels[0]);
		}
		canvas.setColor(color);
		canvas.draw(currentShape);
	    }
	    catch(Exception e){ e.printStackTrace(); }
	}
    }

    protected Color color;

    static Category logger = Category.getInstance(SeismogramPlotter.class.getName());
}// SeismogramPlotter
