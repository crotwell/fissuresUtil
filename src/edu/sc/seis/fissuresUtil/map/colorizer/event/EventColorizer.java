/**
 * EventColorizer.java
 *
 * @author Philip Oliver-Paull
 */

package edu.sc.seis.fissuresUtil.map.colorizer.event;

import com.bbn.openmap.omGraphics.OMGraphicList;
import edu.sc.seis.fissuresUtil.map.graphics.OMEvent;

public interface EventColorizer {
	
	/*
	 * This method takes both the list containing the OMEvent
	 * and the OMEvent itself.  The individual implementation
	 * might use only one of these or both.  The documentation
	 * for each implementation of this method should be edited
	 * to say which argment can be null.
	 */
	public void colorize(OMGraphicList eventList, OMEvent ome);
	
}

