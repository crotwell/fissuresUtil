/**
 * DepthEventColorizer.java
 *
 * @author Philip Oliver-Paull
 */

package edu.sc.seis.fissuresUtil.map.colorizer.event;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import java.awt.Color;
import java.awt.Paint;

public class DepthEventColorizer implements EventColorizer{

    public Paint[] colorize(EventAccessOperations[] events) {
        Paint[] paints = new Color[events.length];
        for (int i = 0; i < events.length; i++) {
            try {
                paints[i] = getDepthColor(events[i]);
            } catch (NoPreferredOrigin e) {
                paints[i] = DefaultEventColorizer.DEFAULT_EVENT;
            }
        }
        return paints;
    }

    public static Color getDepthColor(EventAccessOperations eao) throws NoPreferredOrigin{
        Origin prefOrigin = eao.get_preferred_origin();
        QuantityImpl depth = (QuantityImpl)prefOrigin.my_location.depth;
        double depthKM = depth.convertTo(UnitImpl.KILOMETER).value;
        Color color = DisplayUtils.EVENT_ORANGE;
        if (depthKM <= 40.0){
            color = DisplayUtils.EVENT_RED;
        }
        if (depthKM >= 150.0){
            color = DisplayUtils.EVENT_YELLOW;
        }
        return color;
    }


}

