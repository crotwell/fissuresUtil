/**
 * EventColorizer.java
 *
 * @author Philip Oliver-Paull
 */

package edu.sc.seis.fissuresUtil.map.colorizer.event;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import java.awt.Paint;

public interface EventColorizer {
	
	public Paint[] colorize(EventAccessOperations[] events);

}

