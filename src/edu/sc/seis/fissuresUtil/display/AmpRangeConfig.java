package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import java.util.HashMap;

/**
 * AmpRangeConfig configures an amplitude range for display based on an internal ruleset and seismogram data
 *
 *
 * Created: Wed May 22 14:12:43 2002
 *
 * @author Charlie Groves
 * @version 0.1
 */

public interface AmpRangeConfig extends TimeSyncListener {
        
    /**
     * Returns the amplitude range for a given seismogram
     *
     * 
     */
    public UnitRangeImpl getAmpRange(DataSetSeismogram seis);

    /**
     * Returns the amplitude range for a given seismogram over a time range
     *
     * 
     */
    public UnitRangeImpl getAmpRange(DataSetSeismogram seis, MicroSecondTimeRange calcIntv);
    
    /**
     * Returns the amplitude range for the whole area being displayed.
     *
     */
    public UnitRangeImpl getAmpRange();
    
    /**
     * Calculates the amplitudes for all seismograms currently held by the configurator based on the time range rules for them held 
     * in the TimeRangeConfig object passed
     *
     */
    public void visibleAmpCalc(TimeConfigRegistrar timeConfig);

    /**
     * Adds a seismogram to the current amplitude configurator
     *
     */
    public void addSeismogram(DataSetSeismogram seis);

    /**
     * Removes a seismogram from this amplitude configurator
     *
     * @param seis a <code>DataSetSeismogram</code> value
     */
    public void removeSeismogram(DataSetSeismogram seis);
    
    public HashMap getSeismograms();
    
    public void addAmpSyncListener(AmpSyncListener a);

    public void removeAmpSyncListener(AmpSyncListener a);

    /**
     * Sends a message to all AmpSyncListeners held by the configurator that the amplitude range has changed
     *
     */
    public void updateAmpSyncListeners();

    public void fireAmpRangeEvent(AmpSyncEvent e);

    public AmpSnapshot takeSnapshot();

}// AmpRangeConfig
