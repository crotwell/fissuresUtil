package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;

/**
 * SeismogramDisplay is the interface to allow the viewing of various SeismogramPlotters and to add widgets to them.  Every Display has 
 * a time range configuration object and an amp range configuration object to determine how the seismograms will be drawn in the 
 * display, and to configure the widgets.
 *
 * Created: Wed May 22 14:10:17 2002
 *
 * @author Charlie Groves
 * @version 0.1
 */

public interface SeismogramDisplay extends TimeSyncListener, AmpSyncListener {
    
    
    /**
     * Removes a seismogram from the display
     *
     * 
     */
    public void removeSeismogram(LocalSeismogram oldSeis);
    
    /**
     * Removes all seismograms from this display
     *
     *
     */
    
    /**
     * Returns the amp range configurator the display is using
     *
     * 
     */
    public AmpRangeConfig getAmpConfig();
    
    /**
     * Returns the time range configurator the display is using
     *
     * 
     */
    public TimeRangeConfig getTimeConfig();

    public void addBottomTimeBorder();

    public void removeBottomTimeBorder();

    public void addTopTimeBorder();

    public void removeTopTimeBorder();

    public void redraw();
   
}// SeismogramDisplay
