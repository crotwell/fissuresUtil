package edu.sc.seis.fissuresUtil.display;

import java.awt.*;
import java.awt.geom.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.model.*;

/**
 * SeismogramPlotter creates a seismogram based on a local seismogram, time range and an amplitude range. Generally it will be used as 
 * part of a SeismogramDisplay to add functionality beyond basic drawing.
 *
 *
 * Created: Wed May 22 14:07:07 2002
 *
 * @author Charlie Groves
 * @version
 */

public interface SeismogramPlotter{
    
    /**
     * Sets the line color for the seismogram
     *
     * 
     */
    public void setLineColor(Color c);

    /**
     * Sets the time range for the seismogram
     *
     * 
     */
    public void setTimeRange(MicroSecondTimeRange timeRange);
    
    /**
     * Returns the time range the seismogram is using
     *
     *
     */
    public MicroSecondTimeRange getTimeRange();
    
    /**
     * Sets the amplitude range for the seismogram
     *
     * 
     */
    public void setAmpRange(UnitRangeImpl ampRange);

    /**
     * Returns the amplitude range the seismogram is using
     *
     */
    public UnitRangeImpl getAmpRange();

    public void check(Rectangle rect);

    public void setRedo(boolean r);

    public LocalSeismogram getSeismogram();
}// SeismogramPlotter
