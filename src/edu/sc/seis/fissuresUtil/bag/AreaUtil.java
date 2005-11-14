/**
 * AreaUtil.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.fissuresUtil.bag;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.StringTokenizer;
import edu.iris.Fissures.Area;
import edu.iris.Fissures.BoxArea;
import edu.iris.Fissures.GlobalArea;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.LocationType;
import edu.iris.Fissures.PointDistanceArea;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;

public class AreaUtil {

    public static Channel[] inArea(Area area, Channel[] channels) {
        LinkedList out = new LinkedList();
        // shortcut for GlobalArea
        if(area instanceof GlobalArea) {
            return channels;
        }
        for(int i = 0; i < channels.length; i++) {
            Location loc = channels[i].my_site.my_location;
            if(inArea(area, loc)) {
                out.add(channels[i]);
            }
        }
        return (Channel[])out.toArray(new Channel[0]);
    }

    public static boolean inArea(Area area, Location point) {
        if(area instanceof GlobalArea) {
            return true;
        } else if(area instanceof BoxArea) {
            BoxArea box = (BoxArea)area;
            return (point.latitude >= box.min_latitude
                    && point.latitude <= box.max_latitude
                    && point.longitude % 360 >= box.min_longitude % 360
                    && point.longitude % 360 <= box.max_longitude % 360);
        } else if(area instanceof PointDistanceArea) {
            PointDistanceArea pdArea = (PointDistanceArea)area;
            DistAz distAz = new DistAz(pdArea.latitude,
                                       pdArea.longitude,
                                       point.latitude,
                                       point.longitude);
            double minDegree, maxDegree;
            if(((UnitImpl)pdArea.min_distance.the_units).isConvertableTo(UnitImpl.DEGREE)) {
                minDegree = ((QuantityImpl)pdArea.min_distance).getValue(UnitImpl.DEGREE);
            } else {
                minDegree = DistAz.kilometersToDegrees(((QuantityImpl)pdArea.min_distance).getValue(UnitImpl.KILOMETER));
            }
            if(((UnitImpl)pdArea.max_distance.the_units).isConvertableTo(UnitImpl.DEGREE)) {
                maxDegree = ((QuantityImpl)pdArea.max_distance).getValue(UnitImpl.DEGREE);
            } else {
                maxDegree = DistAz.kilometersToDegrees(((QuantityImpl)pdArea.max_distance).getValue(UnitImpl.KILOMETER));
            }
            return (distAz.getDelta() >= minDegree && distAz.getDelta() <= maxDegree);
        }
        throw new RuntimeException("Unknown Area type: "
                + area.getClass().getName());
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
            if(check == 4) {
                return true;
            }
            inside += check;
        }
        return (inside != 0);
    }

    private static int polygonPointCheck(float lonA,
                                         float latA,
                                         float lonB,
                                         float latB) {
        if(latA * latB > 0) {
            return 0;
        }
        if((lonA * latB != lonB * latA) || (lonA * lonB > 0)) {
            if(latA * latB < 0) {
                if(latA > 0) {
                    if(latA * lonB >= lonA * latB) {
                        return 0;
                    }
                    return -2;
                }
                if(lonA * latB >= latA * lonB) {
                    return 0;
                }
                return 2;
            }
            if(latB == 0) {
                if(latA == 0) {
                    return 0;
                } else if(lonB > 0) {
                    return 0;
                } else if(latA > 0) {
                    return -1;
                }
                return 1;
            } else if(lonA > 0) {
                return 0;
            } else if(latB > 0) {
                return 1;
            }
            return -1;
        }
        return 4;
    }

    public static Location[] loadPolygon(BufferedReader in) throws IOException {
        ArrayList out = new ArrayList();
        String line;
        while((line = in.readLine()) != null && line.length() > 2) {
            StringTokenizer tokenizer = new StringTokenizer(line);
            float lat = Float.parseFloat(tokenizer.nextToken());
            float lon = Float.parseFloat(tokenizer.nextToken());
            out.add(new Location(lat, lon, null, null, LocationType.GEOGRAPHIC));
        }
        return (Location[])out.toArray(new Location[0]);
    }
}