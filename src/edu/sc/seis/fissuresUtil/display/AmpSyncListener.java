package edu.sc.seis.fissuresUtil.display;

import java.util.EventListener;


/**
 * AmpSyncListener is an interface for objects that would like to be made aware of amp range changes
 *
 *
 * Created: Thu May 23 13:47:59 2002
 *
 * @author Charlie Groves
 * @version 0.1
 */

public interface AmpSyncListener extends EventListener{
    /**Informs an amp range listener that a change has been made to the amp range and that it should update
     */
    public void updateAmpRange();
}
