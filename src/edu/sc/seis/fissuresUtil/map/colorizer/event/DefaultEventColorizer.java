/**
 * DefaultEventColorizer.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.map.colorizer.event;
import com.bbn.openmap.omGraphics.OMGraphicList;
import edu.sc.seis.fissuresUtil.map.graphics.OMEvent;
import java.awt.Color;



public class DefaultEventColorizer implements EventColorizer{
	
	public static Color DEFAULT_EVENT = Color.RED;
	
	/*
	 *	@param eventList can be null
	 *  @param event cannot be null
	 */
	public void colorize(OMGraphicList eventList, OMEvent event) {
		event.setPaint(DEFAULT_EVENT);
	}
}

