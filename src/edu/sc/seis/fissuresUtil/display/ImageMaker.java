package edu.sc.seis.fissuresUtil.display;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Iterator;
import java.lang.Runnable;
import java.lang.Thread;
import java.awt.*;
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
	PlotInfo currentRequirements;
	BasicSeismogramDisplay.ImagePainter currentPatron;
	int numLeft;
	Graphics2D graphic;
	Image currentImage; 
	numLeft = requests.size();
	while(numLeft > 0){
	    synchronized(this){ 
		currentPatron = ((BasicSeismogramDisplay.ImagePainter)requests.getFirst()); 
		currentRequirements = ((PlotInfo)patrons.get(currentPatron)); 
	    }
	    HashMap plotters = currentRequirements.getPlotters();
	    Dimension size = currentRequirements.getSize();
	    if(size.height <= 0 || size.width <= 0){
		numLeft = requests.size();
		break;
	    }	    
	    synchronized(this){
		if(requests.contains(currentPatron)){
		    currentImage = currentPatron.createImage(size.width, size.height);
		    graphic = (Graphics2D)currentImage.getGraphics();
		}else{
		    numLeft = requests.size();
		    break;
		}
	    }
	    Iterator e = plotters.keySet().iterator();
	    while(e.hasNext()){
		Plotter current = ((Plotter)e.next());
		graphic.setColor((Color)plotters.get(current));
		graphic.draw(current.draw(size));
	    }
	    synchronized(this){
		if(currentRequirements.getDisplayInterval().getValue() == 
		   currentPatron.getTimeConfig().getTimeRange().getInterval().getValue() &&
		   requests.contains(currentPatron)){
		    requests.removeFirst();
		    currentPatron.setImage(currentImage);
		}
	    }
	    numLeft = requests.size();
	}
    }

    public synchronized void remove(BasicSeismogramDisplay.ImagePainter imagePainter){
	requests.remove(imagePainter);	
	patrons.remove(imagePainter);
    }
    
    protected Thread imageCreation;

    protected LinkedList requests = new LinkedList();
    
    protected HashMap patrons = new HashMap();

    protected Category logger = Category.getInstance(BasicSeismogramDisplay.class.getName());
}// ImageMaker
