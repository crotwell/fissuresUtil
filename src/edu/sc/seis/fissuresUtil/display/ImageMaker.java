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
	int numLeft;
	Graphics2D graphic;
	Image currentImage = null;
	Dimension size; 
	HashMap filterPlotters, seisPlotters, flagPlotters;
	numLeft = requests.size();
	while(numLeft > 0){
	    begin = new Date();
	    //logger.debug("creating an image with " + numLeft + " in the queue");
	    synchronized(this){ 
		currentPatron = ((BasicSeismogramDisplay.ImagePainter)requests.getFirst()); 
		currentRequirements = ((PlotInfo)patrons.get(currentPatron)); 
		size = currentRequirements.getSize();
		seisPlotters = ((HashMap)currentRequirements.getSeisPlotters().clone());
		filterPlotters = ((HashMap)currentRequirements.getFilterPlotters().clone());
		flagPlotters = ((HashMap)currentRequirements.getFlagPlotters().clone());
	    	if(requests.contains(currentPatron) && size.width > 0){
		    currentImage = currentPatron.createImage(size.width, size.height);
		    graphic = (Graphics2D)currentImage.getGraphics();
		}
		else{
		    numLeft = requests.size();
		    break;
		}
	    }
	    Iterator e = seisPlotters.keySet().iterator();
	    graphic.setColor(Color.white);
	    graphic.fill(new Rectangle(0, 0, size.width, size.height));
	    Date beginPlotting = new Date();
	    while(e.hasNext()){
		Plotter current = ((Plotter)e.next());
		graphic.setColor((Color)seisPlotters.get(current));
		graphic.draw(current.draw(size));
	    }
	    e = filterPlotters.keySet().iterator();
	    while(e.hasNext()){
		Plotter current = ((Plotter)e.next());
		graphic.setColor((Color)filterPlotters.get(current));
		graphic.draw(current.draw(size));
	    }
	    e = flagPlotters.keySet().iterator();
	    while(e.hasNext()){
		FlagPlotter current = ((FlagPlotter)e.next());
		graphic.setColor((Color)flagPlotters.get(current));
		graphic.fill(current.draw(size));
		graphic.setColor(Color.black);
		graphic.drawString(current.getName(), current.getStringX(), 10);
	    }
	    Date endPlotting = new Date();
	    long interval = (endPlotting.getTime() - beginPlotting.getTime());
	    //logger.debug("plotting: " + interval + "ms");
	    plottingTotals += interval;
	    synchronized(this){
		if(currentRequirements.getDisplayInterval().getValue() == 
		   currentPatron.getTimeConfig().getTimeRange().getInterval().getValue() &&
		   requests.contains(currentPatron)){
		    requests.removeFirst();
		    currentPatron.setImage(currentImage);
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
