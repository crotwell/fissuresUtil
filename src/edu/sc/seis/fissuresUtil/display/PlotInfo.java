package edu.sc.seis.fissuresUtil.display;

import java.util.LinkedList;
import java.awt.Dimension;
import java.awt.Graphics2D;
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
    public PlotInfo(Dimension size, LinkedList plotters, LinkedList selections, TimeSnapshot timeState, AmpSnapshot ampState, 
		    Graphics2D g2){
	this.size = size;
	this.plotters = plotters;
	this.selections = selections;
	this.timeState = timeState;
	this.ampState = ampState;
	this.g2 = g2;
    }

    public Dimension getSize(){ return size; }

    public LinkedList getPlotters(){ return plotters; }

    public LinkedList getSelections(){ return selections; }

    public TimeSnapshot getTimeSnapshot(){ return timeState; }

    public AmpSnapshot getAmpSnapshot(){ return ampState; }

    public Graphics2D getGraphics(){ return g2; }
    
    private Graphics2D g2;

    private Dimension size;
    
    private LinkedList plotters, selections;

    private TimeSnapshot timeState;

    private AmpSnapshot ampState;

}// PlotInfo
