/**
 * DefaultEventColorizer.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.map.colorizer.event;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import java.awt.Color;
import java.awt.Paint;



public class DefaultEventColorizer implements EventColorizer{
	
	public static Color DEFAULT_EVENT = Color.RED;
	
	/*
	 *	@param eventList can be null
	 *  @param event cannot be null
	 */
	public Paint[] colorize(EventAccessOperations[] events) {
		Paint[] paints = new Color[events.length];
		for (int i = 0; i < events.length; i++) {
			paints[i] = DEFAULT_EVENT;
		}
		return paints;
	}
}

