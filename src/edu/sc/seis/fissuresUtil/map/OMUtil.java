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
import java.util.Iterator;

public class OMUtil {

    public static void setEventColors(OMGraphicList omEvents, EventColorizer colorizer){
        synchronized(omEvents){
            Paint[] paints = colorizer.colorize(extractEvents(omEvents));
            Iterator it = omEvents.iterator();
            OMEvent cur = (OMEvent)it.next();
            for (int i = 0;
                 i < omEvents.size() && it.hasNext();
                 i++, cur = (OMEvent)it.next()) {

                cur.setPaint(paints[i]);
            }
            reverseGraphicOrder(omEvents);
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


    public static void reverseGraphicOrder(OMGraphicList graphics){
        synchronized(graphics){
            for (int i = 0; i < graphics.size(); i++) {
                graphics.moveIndexedToFirst(i);
            }
        }
    }
}

