/**
 * AreaUtil.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.bag;

import java.util.LinkedList;
import edu.iris.Fissures.Area;
import edu.iris.Fissures.BoxArea;
import edu.iris.Fissures.GlobalArea;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.PointDistanceArea;
import edu.iris.Fissures.IfNetwork.Channel;

public class AreaUtil {

    public static Channel[] inArea(Area area, Channel[] channels) {
        LinkedList out = new LinkedList();
        if (area instanceof GlobalArea) {
            return channels;
        } else if (area instanceof BoxArea) {
            BoxArea box = (BoxArea)area;
            for (int i = 0; i < channels.length; i++) {
                Location loc = channels[i].my_site.my_location;
                if (loc.latitude >= box.min_latitude &&
                    loc.latitude <= box.max_latitude &&
                    loc.longitude % 360 >= box.min_longitude % 360  &&
                    loc.longitude % 360  <= box.max_longitude % 360 ) {
                    out.add(channels[i]);
                }
            }
        } else if (area instanceof PointDistanceArea) {

        }
        return (Channel[])out.toArray(new Channel[0]);
    }
}

