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
	PlotInfo info;
	BasicSeismogramDisplay.ImagePainter patron;
	int numLeft = requests.size();
	while(numLeft > 0){
	    begin = new Date();
	    synchronized(this){ 
		patron = ((BasicSeismogramDisplay.ImagePainter)requests.getFirst()); 
		info = ((PlotInfo)patrons.get(patron)); 
	    }
	    Dimension size = info.getSize();
	    if(size.width < 0){
		numLeft = requests.size();
		break;
	    }
	    TimeSnapshot timeState = info.getTimeSnapshot();
	    AmpSnapshot	ampState = info.getAmpSnapshot();
	    LinkedList plotters = info.getPlotters();
	    Image currentImage = info.getImage();
	    Graphics2D graphic = (Graphics2D)currentImage.getGraphics();
	    end = new Date();
	    Date beginPlotting = new Date();
	    graphic.setColor(Color.white);
	    graphic.fill(new Rectangle(0, 0, size.width, size.height));
	    Iterator e = plotters.iterator();
	    while(e.hasNext()){
		((Plotter)e.next()).draw(graphic, size, timeState, ampState);
	    }
	    Date endPlotting = new Date();
	    long interval = (endPlotting.getTime() - beginPlotting.getTime());
	    plottingTotals += interval;
	    synchronized(this){
		if(timeState.getTimeRange().getInterval().getValue() == 
		   patron.getTimeConfig().getTimeRange().getInterval().getValue() &&
		   requests.contains(patron)){
		    requests.removeFirst();
		    patron.setImage(currentImage, timeState);
		}
		numLeft = requests.size();
	    }
	    interval = end.getTime() - begin.getTime();
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
