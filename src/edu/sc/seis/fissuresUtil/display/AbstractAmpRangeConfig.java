package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.seismogramDC.*;

import java.util.*;
/**
 * AbstractAmpRangeConfig.java
 *
 *
 * Created: Mon Jun  3 13:16:18 2002
 *
 * @author Charlie Groves
 * @version
 */

public abstract class AbstractAmpRangeConfig implements AmpRangeConfig{
         
    /**
     * Returns the amplitude range for a given seismogram
     *
     * 
     */
    public abstract UnitRangeImpl getAmpRange(LocalSeismogram seis);

    /**
     * Returns the amplitude range for a given seismogram over a time range
     *
     * 
     */
    public abstract UnitRangeImpl getAmpRange(LocalSeismogram seis, MicroSecondTimeRange calcIntv);
    
    /**
     * Returns the amplitude range for the whole area being displayed.
     *
     */
    public UnitRangeImpl getAmpRange(){ return ampRange; }
    
    /**
     * Calculates the amplitudes for all seismograms currently held by the configurator based on the time range rules for them held 
     * in the MicroSecondTimeRangeConfig object passed
     *
     */
    public abstract void visibleAmpCalc(TimeRangeConfig timeConfig);

    /**
     * Adds a seismogram to the current amplitude configurator
     *
     */
    public void addSeismogram(LocalSeismogram seis){ seismos.add(seis); }

    /**
     * Removes a seismogram from this amplitude configurator
     *
     * @param seis a <code>LocalSeismogram</code> value
     */
    public void removeSeismogram(LocalSeismogram seis){ seismos.remove(seis); }

    /**
     * Adds an amplitude sync listener to the current configurator
     *
     */
    public void addAmpSyncListener(AmpSyncListener a){ ampListeners.add(a); }

    /**
     * Removes an amplitude sync listener from the current configurator
     *
     * 
     */
    public void removeAmpSyncListener(AmpSyncListener a){ ampListeners.remove(a); }

    /**
     * Sends a message to all AmpSyncListeners held by the configurator that the amplitude range has changed
     *
     */
    public void updateAmpSyncListeners(){
	Iterator e = ampListeners.iterator();
	while(e.hasNext())
	    ((AmpSyncListener)e.next()).updateAmpRange();
    }
    
    protected boolean intvCalc = false;
    
    protected UnitRangeImpl ampRange;

    protected TimeRangeConfig timeConfig = null;

    protected LinkedList seismos = new LinkedList();
    
    protected LinkedList ampListeners = new LinkedList();
}// AbstractAmpRangeConfig
