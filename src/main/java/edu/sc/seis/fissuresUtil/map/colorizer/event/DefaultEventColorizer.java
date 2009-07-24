/**
 * DefaultEventColorizer.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.map.colorizer.event;
import java.awt.Color;
import java.util.Iterator;
import com.bbn.openmap.omGraphics.OMGraphicList;
import edu.sc.seis.fissuresUtil.map.graphics.OMEvent;



public class DefaultEventColorizer implements EventColorizer{

    public static Color DEFAULT_EVENT = Color.RED;

    public void colorize(OMGraphicList events) {
        Iterator it = events.iterator();
        while(it.hasNext()){ ((OMEvent)it.next()).setPaint(DEFAULT_EVENT); }
    }
}

