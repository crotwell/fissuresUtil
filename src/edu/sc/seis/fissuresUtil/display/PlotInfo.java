package edu.sc.seis.fissuresUtil.display;

import java.util.LinkedList;
import java.awt.Dimension;
import java.awt.Image;
import edu.iris.Fissures.model.TimeInterval;

/**
 * PlotInfo.java
 *
 *
 * Created: Wed Jun 12 11:49:01 2002
 *
 * @author Charlie Groves
 * @version
 */

public class PlotInfo {
    public PlotInfo(Dimension size, LinkedList plotters, TimeSnapshot timeState, AmpSnapshot ampState, Image image){
	this.size = size;
	this.plotters = plotters;
	this.timeState = timeState;
	this.ampState = ampState;
	this.image = image;
    }

    public Dimension getSize(){ return size; }

    public LinkedList getPlotters(){ return plotters; }

    public TimeSnapshot getTimeSnapshot(){ return timeState; }

    public AmpSnapshot getAmpSnapshot(){ return ampState; }

    public Image getImage(){ return image; }
    
    private Image image;

    private Dimension size;
    
    private LinkedList plotters;

    private TimeSnapshot timeState;

    private AmpSnapshot ampState;

}// PlotInfo
