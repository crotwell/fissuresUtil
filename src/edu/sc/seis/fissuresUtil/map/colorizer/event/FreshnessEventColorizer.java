/**
 * FreshnessEventColorizer.java
 *
 * @author Philip Oliver-Paull
 */

package edu.sc.seis.fissuresUtil.map.colorizer.event;

import com.bbn.openmap.omGraphics.OMGraphicList;
import edu.sc.seis.fissuresUtil.map.graphics.OMEvent;
import java.awt.Color;

public class FreshnessEventColorizer implements EventColorizer{
	
	public static Color FRESH_EVENT = Color.RED;
	public static Color OLD_EVENT = Color.ORANGE;
	public static Color REALLY_OLD_EVENT = Color.YELLOW;
	
	/*
	 *	@param eventList can not be null
	 *  @param event can be null
	 */
	public void colorize(OMGraphicList eventList, OMEvent event) {
		synchronized(eventList){
			for (int i = 0; i < eventList.size(); i++) {
				OMEvent ome = (OMEvent)eventList.getOMGraphicAt(i);
				if (i == eventList.size() - 11){
					ome.setPaint(REALLY_OLD_EVENT);
				}
				else if (i == eventList.size() - 6){
					ome.setPaint(OLD_EVENT);
				}
				else if (i == eventList.size() - 1){
					ome.setPaint(FRESH_EVENT);
				}
			}
		}
	}
	
}

