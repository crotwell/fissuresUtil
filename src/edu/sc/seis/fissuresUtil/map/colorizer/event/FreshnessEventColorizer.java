/**
 * FreshnessEventColorizer.java
 *
 * @author Philip Oliver-Paull
 */

package edu.sc.seis.fissuresUtil.map.colorizer.event;

import com.bbn.openmap.omGraphics.OMGraphicList;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.map.graphics.OMEvent;
import java.awt.Color;
import java.util.Iterator;

public class FreshnessEventColorizer implements EventColorizer{

    public static Color FRESH_EVENT = DisplayUtils.EVENT_RED;
    public static Color OLD_EVENT = DisplayUtils.EVENT_ORANGE;
    public static Color REALLY_OLD_EVENT = DisplayUtils.EVENT_YELLOW;

    public void colorize(OMGraphicList events) {
        int size = events.size();
        for (int i = 0; i < size; i++) {
            OMEvent cur = (OMEvent)events.getOMGraphicAt(i);
            if (i <= size - 11){ cur.setPaint(REALLY_OLD_EVENT); }
            else if (i <= size - 6){ cur.setPaint(OLD_EVENT); }
            else if (i <= size - 1){ cur.setPaint(FRESH_EVENT); }
        }
    }

}

