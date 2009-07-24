/**
 * EventColorizer.java
 *
 * @author Philip Oliver-Paull
 */

package edu.sc.seis.fissuresUtil.map.colorizer.event;

import com.bbn.openmap.omGraphics.OMGraphicList;

public interface EventColorizer {
    public void colorize(OMGraphicList events);
}

