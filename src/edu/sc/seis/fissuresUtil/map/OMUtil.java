/**
 * OMUtil.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.map;

import com.bbn.openmap.omGraphics.OMGraphicList;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.map.colorizer.event.EventColorizer;
import edu.sc.seis.fissuresUtil.map.graphics.OMEvent;
import java.awt.Paint;

public class OMUtil {
	
	public static void setEventColors(OMGraphicList omEvents, EventColorizer colorizer){
		synchronized(omEvents){
			Paint[] paints = colorizer.colorize(extractEvents(omEvents));
			for (int i = 0; i < paints.length; i++) {
				((OMEvent)omEvents.getOMGraphicAt(i)).setPaint(paints[i]);
			}
		}
	}
	
	public static EventAccessOperations[] extractEvents(OMGraphicList omEvents){
		EventAccessOperations[] events;
		synchronized(omEvents){
			events = new EventAccessOperations[omEvents.size()];
			for (int i = 0; i < events.length; i++) {
				events[i] = ((OMEvent)omEvents.getOMGraphicAt(i)).getEvent();
			}
		}
		return events;
	}
	
}

