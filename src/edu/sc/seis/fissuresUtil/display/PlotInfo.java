package edu.sc.seis.fissuresUtil.display;

import java.util.LinkedList;
import java.awt.Dimension;
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
    public PlotInfo(Dimension size, LinkedList plotters, TimeSnapshot timeState, AmpSnapshot ampState){
	this.size = size;
	this.plotters = plotters;
	this.timeState = timeState;
	this.ampState = ampState;
    }

    public Dimension getSize(){ return size; }

    public LinkedList getPlotters(){ return plotters; }

    public TimeSnapshot getTimeSnapshot(){ return timeState; }

    public AmpSnapshot getAmpSnapshot(){ return ampState; }

    private Dimension size;
    
    private LinkedList plotters;

    private TimeSnapshot timeState;

    private AmpSnapshot ampState;

}// PlotInfo
