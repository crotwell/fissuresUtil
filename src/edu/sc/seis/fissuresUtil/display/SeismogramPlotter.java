package edu.sc.seis.fissuresUtil.display;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.lang.ref.SoftReference;
import org.apache.log4j.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import java.util.Date;

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
	plotUtil = new PlotUtil(seis.getSeismogram());
    }

    public void draw(Graphics2D canvas, Dimension size, TimeSnapshot timeState, AmpSnapshot ampState){
	if(visible){
	    MicroSecondTimeRange timeRange = timeState.getTimeRange(seismogram);
	    try{
		/*int[][] pixels = SimplePlotUtil.compressYvalues(seismogram.getSeismogram(), timeRange, 
								ampState.getAmpRange(seismogram), size);
		SimplePlotUtil.scaleYvalues(pixels, seismogram.getSeismogram(), timeRange, ampState.getAmpRange(seismogram), size); 
		SimplePlotUtil.flipArray(pixels[1], size.height);
		int[] xPixels = pixels[0];
		int[] yPixels = pixels[1];*/
		GeneralPath currentShape = new GeneralPath();
		/*if(xPixels.length >= 2){
		    currentShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD, xPixels.length - 1);
		    currentShape.moveTo(xPixels[0], yPixels[0]);
		    for(int i = 1; i < xPixels.length; i++)
			currentShape.lineTo(xPixels[i], yPixels[i]);
		}else if(xPixels.length == 1){
		    currentShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 2);
		    currentShape.moveTo(0, yPixels[0]);
		    currentShape.lineTo(size.width, yPixels[0]);
		    }*/
		int[][] pixels = plotUtil.getPlot(timeRange, ampState.getAmpRange(seismogram), size);
		Date shapeBegin = new Date();
		currentShape.moveTo(plotUtil.getStart(), pixels[0][plotUtil.getStart()]);
		currentShape.lineTo(plotUtil.getStart(), pixels[1][plotUtil.getStart()]);
		for(int i = plotUtil.getStart() + 1; i < plotUtil.getEnd(); i++){
		    currentShape.lineTo(i, pixels[0][i]);
		    currentShape.lineTo(i, pixels[1][i]);
		    }
		Date shapeEnd = new Date();
		Date beginDraw = new Date();
		canvas.setColor(color);
		canvas.draw(currentShape);
		Date endDraw = new Date();
		shapeCreate += shapeEnd.getTime() - shapeBegin.getTime();
		drawShape += endDraw.getTime() - beginDraw.getTime();
		shapeCreateTimes++;
		drawShapeTimes++;
		System.out.println("SEISMOGRAMPLOTTER Shape creation: " + shapeCreate/shapeCreateTimes + " Shape drawing: " + drawShape/drawShapeTimes);
	    }
	    catch(Exception e){ e.printStackTrace(); }
	}
    }

    public int shapeCreate, shapeCreateTimes, drawShape, drawShapeTimes;
    
    protected Color color;

    protected PlotUtil plotUtil;

    static Category logger = Category.getInstance(SeismogramPlotter.class.getName());
}// SeismogramPlotter
