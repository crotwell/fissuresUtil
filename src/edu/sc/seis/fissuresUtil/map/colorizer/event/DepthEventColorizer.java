/**
 * DepthEventColorizer.java
 *
 * @author Philip Oliver-Paull
 */

package edu.sc.seis.fissuresUtil.map.colorizer.event;

import com.bbn.openmap.omGraphics.OMGraphicList;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.map.graphics.OMEvent;
import java.awt.Color;
import java.util.Iterator;

public class DepthEventColorizer implements EventColorizer{

    public void colorize(OMGraphicList events) {
        Iterator it = events.iterator();
        while(it.hasNext()){
            OMEvent cur = (OMEvent)it.next();
            cur.setPaint(getDepthColor(cur.getEvent()));
        }
    }

    public static Color getDepthColor(CacheEvent eao){
        Origin prefOrigin = eao.getOrigin();
        QuantityImpl depth = (QuantityImpl)prefOrigin.my_location.depth;
        double depthKM = depth.convertTo(UnitImpl.KILOMETER).value;
        Color color = DisplayUtils.EVENT_ORANGE;
        if (depthKM <= 40.0){ color = DisplayUtils.EVENT_RED; }
        if (depthKM >= 150.0){ color = DisplayUtils.EVENT_YELLOW; }
        return color;
    }


}

