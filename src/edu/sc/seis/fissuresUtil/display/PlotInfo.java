package edu.sc.seis.fissuresUtil.display;

import java.util.HashMap;
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
    public PlotInfo(Dimension size, HashMap plotters, TimeInterval displayInterval){
	this.size = size;
	this.plotters = plotters;
	this.displayInterval = displayInterval;
    }

    public Dimension getSize(){ return size; }

    public HashMap getPlotters(){ return plotters; }

    public TimeInterval getDisplayInterval(){ return displayInterval; }

    private Dimension size;
    
    private HashMap plotters;

    private TimeInterval displayInterval;
}// PlotInfo
