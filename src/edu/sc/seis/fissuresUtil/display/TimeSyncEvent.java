package edu.sc.seis.fissuresUtil.display;

/**
 * TimeSyncEvent creates an object that encapsulates the change the user
 * requested for the time range
 *
 * 
 * @author Charlie Groves
 * @version 0.1
 */

public class TimeSyncEvent {
    /**
     * Creates a new <code>TimeSyncEvent</code> instance.
     *
     * @param beginShift the percentage of the shift towards the time beginning
     * @param endShift the percentage of the shift towards the end time
     * @param isAdjusting indicates if the adjustment is still occuring
     */
    public TimeSyncEvent (double beginShift, double endShift, boolean isAdjusting){
	this.beginShift = beginShift;
	this.endShift = endShift;
	this.isAdjusting = isAdjusting;
    }

    /**
     * Accessor for the begin shift
     *
     * @return the begin shift
     */
    public double getBegin(){ return beginShift; }
    
    /**
     * Accessor for the end shift
     *
     * @return the end shift
     */
    public double getEnd(){ return endShift; }
    
    /**
     * Accessor to check if it's still being adjusted
     *
     */
    public boolean getAdjusting(){ return isAdjusting; }

    private double beginShift, endShift; //percentages indicating the amount of movement
    private boolean isAdjusting; //indicates if the time scale is still being adjusted
    
}// TimeSyncEvent
