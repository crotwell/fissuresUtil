package edu.sc.seis.fissuresUtil.display;

import java.util.EventListener;
import edu.iris.Fissures.model.*;

/**
 * TimeSyncListener is an interface for objects that would like to be made aware
 * of TimeSyncEvents
 *
 * @author Charlie Groves
 * @version 0.1
 */

public interface TimeSyncListener extends EventListener{
    /**
     *  Tells the object that the time range has changed so that it may update
     *  accordingly.
     */
    public void updateTimeRange();
    
    /**
     *  Tells the object that the time range has changed so that it may update
     *  accordingly.
     */
    public void updateTimeRange(ConfigEvent event);
    
}// TimeSyncListener
