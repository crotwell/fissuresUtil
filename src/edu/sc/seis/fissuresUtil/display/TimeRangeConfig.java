package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;

/**
 * TimeRangeConfig configures a seismogram and the various 
 * widgets that come with it.  
 *
 * Created: Wed May 22 14:14:43 2002
 *
 * @author Charlie Groves
 * @version 0.1
 */

public interface TimeRangeConfig {
        
    /**
     * Takes the information from the passed seismogram and uses it along with the information already taken from other seismograms 
     * along with an internal set of calculations to determine the amount of time the passed seismogram will be displayed
     *
     * @param seis the seismogram to be displayed
     * @return the time it will be displayed
     */
    public MicroSecondTimeRange getTimeRange(LocalSeismogram seis);

    /**
     * Get the total display time regardless of a particular seismogram, for things such as axes
     *
     * @return the time range being displayed
     */
    public MicroSecondTimeRange getTimeRange();

    /**
     * Add the values in this seismogram to the configuration
     *
     * @param seis the seismogram to be added
     */
    public void addSeismogram(LocalSeismogram seis);

    public void addSeismogram(LocalSeismogram seis, MicroSecondDate b);

    /**
     * Remove the values from this seismogram from the configuration
     *
     * @param seis the seismogram to be removed
     */
    public void removeSeismogram(LocalSeismogram seis);
    
    
    /**
     * Fire an event to all of the time sync listeners to update their time ranges
     *
     */
    public void updateTimeSyncListeners();

    /**
     * Takes the information from the TimeSyncEvent, adjusts the time, and updates according to the information in the event
     *
     */
    public void fireTimeRangeEvent(TimeSyncEvent e);

    public void setDisplayInterval(TimeInterval t);

    public void setBeginTime(MicroSecondDate b);
    
    public void setAllBeginTime(MicroSecondDate b);
}// TimeRangeConfig
