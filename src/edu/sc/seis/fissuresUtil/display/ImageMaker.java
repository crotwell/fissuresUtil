package edu.sc.seis.fissuresUtil.display;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Date;
import java.lang.Runnable;
import java.lang.Thread;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import org.apache.log4j.*;

/**
 * ImageMaker.java
 *
 *
 * Created: Wed Jun 12 11:42:04 2002
 *
 * @author Charlie Groves
 * @version
 */

public class ImageMaker implements Runnable  {
    public ImageMaker(){
	imageCreation = new Thread(this, "Image Maker");
	imageCreation.setPriority(3);
    }
    
    public synchronized void createImage(BasicSeismogramDisplay.ImagePainter patron, PlotInfo requirements){
	patrons.put(patron, requirements);
	if(!requests.contains(patron))
	    requests.add(patron);
	if(!imageCreation.isAlive()){
	    logger.debug("Starting image creation thread");
	    imageCreation = new Thread(this, "Image Maker");
	    imageCreation.start();
	}
    }

    /**
     *
     */
    public void run(){
	plottingTotals = 0;
	imageTotals = 0;
	PlotInfo currentRequirements;
	BasicSeismogramDisplay.ImagePainter currentPatron;
	int numLeft = requests.size();
	Graphics2D graphic;
	Image currentImage;
	Dimension size; 
	LinkedList plotters;
	TimeSnapshot timeState;
	AmpSnapshot ampState;
	while(numLeft > 0){
	    begin = new Date();
	    //logger.debug("creating an image with " + numLeft + " in the queue");
	    synchronized(this){ 
		currentPatron = ((BasicSeismogramDisplay.ImagePainter)requests.getFirst()); 
		currentRequirements = ((PlotInfo)patrons.get(currentPatron)); 
		size = currentRequirements.getSize();
		plotters = ((LinkedList)currentRequirements.getPlotters().clone());
		timeState = currentRequirements.getTimeSnapshot();
		ampState = currentRequirements.getAmpSnapshot();
	    	if(requests.contains(currentPatron) && size.width > 0){
		    currentImage = currentPatron.createImage(size.width, size.height);
		    graphic = (Graphics2D)currentImage.getGraphics();
		}
		else{
		    numLeft = requests.size();
		    break;
		}
	    }
	    Iterator e = plotters.iterator();
	    graphic.setColor(Color.white);
	    graphic.fill(new Rectangle(0, 0, size.width, size.height));
	    Date beginPlotting = new Date();
	    while(e.hasNext()){
		((Plotter)e.next()).draw(graphic, size, timeState, ampState);
	    }
	    Date endPlotting = new Date();
	    long interval = (endPlotting.getTime() - beginPlotting.getTime());
	    //logger.debug("plotting: " + interval + "ms");
	    plottingTotals += interval;
	    synchronized(this){
		if(timeState.getTimeRange().getInterval().getValue() == 
		   currentPatron.getTimeConfig().getTimeRange().getInterval().getValue() &&
		   requests.contains(currentPatron)){
		    requests.removeFirst();
		    currentPatron.setImage(currentImage, timeState);
		}
		numLeft = requests.size();
	    }
	    end = new Date();
	    interval = end.getTime() - begin.getTime();
	    //logger.debug("image creation: " + interval + "ms");
	    imageTotals += interval;
	}
	logger.debug("image creation thread is finished. image total " + imageTotals + " plot total " + plottingTotals);
    }

    public synchronized void remove(BasicSeismogramDisplay.ImagePainter imagePainter){
	requests.remove(imagePainter);	
	patrons.remove(imagePainter);
    }
    
    protected Date begin, end;

    protected static long plottingTotals, imageTotals;

    protected Thread imageCreation;

    protected LinkedList requests = new LinkedList();
    
    protected HashMap patrons = new HashMap();

    protected Category logger = Category.getInstance(ImageMaker.class.getName());
}// ImageMaker
