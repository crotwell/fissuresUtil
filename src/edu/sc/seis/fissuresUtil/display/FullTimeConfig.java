package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.seismogramDC.*;

import java.util.*;

/**
 * FullTimeConfig is a TimeRangeConfig that displays the full time range of all of the seismograms it is passed
 *
 *
 * Created: Thu May 23 15:03:59 2002
 *
 * @author Charlie Groves
 * @version
 */

public class FullTimeConfig extends AbstractTimeRangeConfig{
    
    /** Gets the MicroSecondTimeRange for a particular seismogram, and checks if the seismogram has an earlier begin time or later end
     *  time than the one being used by this config.  It also checks for null for this beginTime and display interval so that when a 
     *  seismogram is removed, calling an update works correctly to redefine any values that may have changed.
     *
     */
    public MicroSecondTimeRange getTimeRange(LocalSeismogram seis){
	if(beginTime == null || beginTime.getMicroSecondTime() > ((LocalSeismogramImpl)seis).getBeginTime().getMicroSecondTime())
	    this.beginTime = ((LocalSeismogramImpl)seis).getBeginTime();
	if(displayInterval == null || 
	   beginTime.add(displayInterval).getMicroSecondTime() < ((LocalSeismogramImpl)seis).getEndTime().getMicroSecondTime())
	    this.displayInterval = new TimeInterval(((LocalSeismogramImpl)seis).getBeginTime(), 
						    ((LocalSeismogramImpl)seis).getEndTime());
	return new MicroSecondTimeRange(this.beginTime, this.beginTime.add(displayInterval));
    }
    
    /** Simply returns the current time range
     *
     */
    public MicroSecondTimeRange getTimeRange(){
	return new MicroSecondTimeRange(this.beginTime, this.beginTime.add(displayInterval));
    }
    
    /** Adds a seismogram to the list of those currently contained in the config.  It obtains the time range for this seismogram so that
     *  its values will be added to those already being used.
     *  
     *
     */
    public void addSeismogram(LocalSeismogram seis){
	this.getTimeRange(seis);
	seismos.put(seis, ((LocalSeismogramImpl)seis).getBeginTime());
	this.updateTimeSyncListeners();
    }	
    
    
    /** Removes a seismogram from the config.  It checks to see if the seismogram being used is either the greatest or least in begin or
     *  end time, and if so, causes the time to be reset to the values now in the config.
     */
    public void removeSeismogram(LocalSeismogram seis){
	if(seismos.containsKey(seis)){
	    if(beginTime.getMicroSecondTime() == ((LocalSeismogramImpl)seis).getBeginTime().getMicroSecondTime())
		beginTime = null;
	    if(beginTime.add(displayInterval).getMicroSecondTime() == ((LocalSeismogramImpl)seis).getEndTime().getMicroSecondTime())
		displayInterval = null;
	    seismos.remove(seis);
	    if(beginTime == null || displayInterval == null){
		Iterator e = seismos.keySet().iterator();
		while(e.hasNext())
		    this.getTimeRange(((LocalSeismogram)e.next()));
		this.updateTimeSyncListeners();
	    }
	}
    }  
    /** A time sync event does nothing to this config, as you can't zoom in on a full time display
     */
    public void fireTimeRangeEvent(TimeSyncEvent e){}
    
    }// FullTimeConfig
