/**
 * DepthEventColorizer.java
 *
 * @author Philip Oliver-Paull
 */

package edu.sc.seis.fissuresUtil.map.colorizer.event;

import com.bbn.openmap.omGraphics.OMGraphicList;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.map.graphics.OMEvent;
import java.awt.Color;

public class DepthEventColorizer implements EventColorizer{
	
	/*
	 *	@param eventList can be null
	 *  @param event cannot be null
	 */
	public void colorize(OMGraphicList eventList, OMEvent event) {
		try {
			event.setPaint(getDepthColor(event.getEvent()));
		} catch (NoPreferredOrigin e) {
			event.setPaint(DefaultEventColorizer.DEFAULT_EVENT);
		}
	}
	
    public static Color getDepthColor(EventAccessOperations eao) throws NoPreferredOrigin{
		Origin prefOrigin = eao.get_preferred_origin();
		QuantityImpl depth = (QuantityImpl)prefOrigin.my_location.depth;
		double depthKM = depth.convertTo(UnitImpl.KILOMETER).value;
		Color color = DisplayUtils.MEDIUM_DEPTH_EVENT;
		if (depthKM <= 40.0){
			color = DisplayUtils.SHALLOW_DEPTH_EVENT;
		}
		if (depthKM >= 150.0){
			color = DisplayUtils.DEEP_DEPTH_EVENT;
		}
		return color;
    }
	
	
}

