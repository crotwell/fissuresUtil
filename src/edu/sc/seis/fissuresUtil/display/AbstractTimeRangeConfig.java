package edu.sc.seis.fissuresUtil.display;


import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.seismogramDC.*;

import java.util.*;
/**
 * AbstractTimeRangeConfig.java
 *
 *
 * Created: Mon Jun  3 12:51:35 2002
 *
 * @author Charlie Groves
 * @version
 */

public abstract class AbstractTimeRangeConfig implements TimeRangeConfig{
    

    /**
     * Takes the information from the passed seismogram and uses it along with the information already taken from other seismograms 
     * along with an internal set of calculations to determine the amount of time the passed seismogram will be displayed
     *
     * @param seis the seismogram to be displayed
     * @return the time it will be displayed
     */
    public abstract MicroSecondTimeRange getTimeRange(LocalSeismogram seis);

    /**
     * Get the total display time regardless of a particular seismogram, for things such as axes
     *
     * @return the time range being displayed
     */
    public abstract MicroSecondTimeRange getTimeRange();

    /**
     * Add the values in this seismogram to the configuration
     *
     * @param seis the seismogram to be added
     */
    public void addSeismogram(LocalSeismogram seis){ seismos.add(seis); }

    /**
     * Remove the values from this seismogram from the configuration
     *
     * @param seis the seismogram to be removed
     */
    public void removeSeismogram(LocalSeismogram seis){ seismos.remove(seis); }
    
    /**
     * Adds a time sync listener to the list to be informed when a time sync event occurs
     * 
     * @param t the time sync listener to be added
     */
    public void addTimeSyncListener(TimeSyncListener t){ timeListeners.add(t); }

    /**
     * Removes a TimeSyncListener from the update list
     *
     * @param t the time sync listener to be removed
     */
    public void removeTimeSyncListener(TimeSyncListener t){ timeListeners.remove(t); }
	
    
    /**
     * Fire an event to all of the time sync listeners to update their time ranges
     *
     */
    public void updateTimeSyncListeners(){
	Iterator e = timeListeners.iterator();
	while(e.hasNext())
	    ((TimeSyncListener)e.next()).updateTimeRange();
    }
    
    /**
     * Takes the information from the TimeSyncEvent, adjusts the MicroSecondTimeRange, and updates according to the information in the event
     *
     */
    public abstract void fireTimeRangeEvent(TimeSyncEvent e);

    protected MicroSecondDate beginTime, endTime;

    protected LinkedList seismos = new LinkedList();
    
    protected LinkedList timeListeners = new LinkedList();

}// AbstractTimeRangeConfig
