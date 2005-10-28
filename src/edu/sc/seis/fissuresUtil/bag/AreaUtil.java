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
        if(area instanceof GlobalArea) {
            return channels;
        } else if(area instanceof BoxArea) {
            BoxArea box = (BoxArea)area;
            for(int i = 0; i < channels.length; i++) {
                Location loc = channels[i].my_site.my_location;
                if(loc.latitude >= box.min_latitude && loc.latitude <= box.max_latitude
                        && loc.longitude % 360 >= box.min_longitude % 360
                        && loc.longitude % 360 <= box.max_longitude % 360) {
                    out.add(channels[i]);
                }
            }
        } else if(area instanceof PointDistanceArea) {
            throw new IllegalArgumentException("Doesn't support PointDistance areas");
        }
        return (Channel[])out.toArray(new Channel[0]);
    }

    public static boolean inArea(Location[] bounds, Location point) {
        float lonA, latA, lonB, latB;
        int inside = 0;
        for(int i = 0; i < bounds.length; i++) {
            lonA = bounds[i].longitude - point.longitude;
            latA = bounds[i].latitude - point.latitude;
            lonB = bounds[(i + 1) % bounds.length].longitude - point.longitude;
            latB = bounds[(i + 1) % bounds.length].latitude - point.latitude;
            int check = polygonPointCheck(lonA, latA, lonB, latB);
            if (check == 4) {
                return true;
            }
            inside += check;
        }
        return (inside != 0);
    }

    private static int polygonPointCheck(float lonA, float latA, float lonB, float latB) {
        if(latA * latB > 0) {
            return 0;
        }
        if((lonA * latB != lonB * latA) || (lonA * lonB > 0)) {
            if(latA * latB < 0) {
                if(latA > 0) {
                    if(latA * lonB >= lonA * latB) {
                        return 0;
                    } else {
                        return -2;
                    }
                } else {
                    if(lonA * latB >= latA * lonB) {
                        return 0;
                    } else {
                        return 2;
                    }
                }
            } else {
                if(latB == 0) {
                    if(latA == 0) {
                        return 0;
                    }
                    if(lonB > 0) {
                        return 0;
                    }
                    if(latA > 0) {
                        return -1;
                    } else {
                        return 1;
                    }
                } else if(lonA > 0) {
                    return 0;
                } else if(latB > 0) {
                    return 1;
                } else {
                    return -1;
                }
            }
        } else {
            return 4;
        }
    }
}