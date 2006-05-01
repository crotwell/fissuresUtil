/**
 * AreaUtil.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.fissuresUtil.bag;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import edu.iris.Fissures.Area;
import edu.iris.Fissures.BoxArea;
import edu.iris.Fissures.GlobalArea;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.LocationType;
import edu.iris.Fissures.PointDistanceArea;
import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.model.BoxAreaImpl;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;

public class AreaUtil {

    public static BoxArea makeContainingBox(Area a) {
        if(a instanceof BoxArea) {
            return (BoxArea)a;
        } else if(a instanceof GlobalArea) {
            return new BoxAreaImpl(-90, 90, -180, 180);
        } else if(a instanceof PointDistanceArea) {
            PointDistanceArea pda = (PointDistanceArea)a;
            float maxDegree = (float)distanceToDegrees(pda.max_distance);
            float maxLong = wrapLong(pda.longitude + maxDegree);
            float minLong = wrapLong(pda.longitude - maxDegree);
            return new BoxAreaImpl(pda.latitude - maxDegree, pda.latitude
                    + maxDegree, minLong, maxLong);
        }
        throw new RuntimeException("Unknown Area: " + a.getClass().getName());
    }

    private static float wrapLong(float lon) {
        if(lon > 180) {
            return -180 + (lon - 180);
        } else if(lon < -180) {
            lon = 180 + (lon + 180);
        }
        return lon;
    }

    public static Channel[] inArea(Area area, Channel[] channels) {
        List out = new ArrayList(channels.length);
        for(int i = 0; i < channels.length; i++) {
            if(inArea(area, channels[i].my_site.my_location)) {
                out.add(channels[i]);
            }
        }
        return (Channel[])out.toArray(new Channel[out.size()]);
    }

    public static boolean inArea(Area area, Location point) {
        if(area instanceof GlobalArea) {
            return true;
        } else if(area instanceof BoxArea) {
            return inBox(point, (BoxArea)area);
        } else if(area instanceof PointDistanceArea) {
            return inDonut((PointDistanceArea)area, point);
        }
        throw new RuntimeException("Unknown Area type: "
                + area.getClass().getName());
    }

    private static boolean inDonut(PointDistanceArea a, Location p) {
        DistAz distAz = new DistAz(a.latitude,
                                   a.longitude,
                                   p.latitude,
                                   p.longitude);
        double minDegree = distanceToDegrees(a.min_distance);
        double maxDegree = distanceToDegrees(a.max_distance);
        return (distAz.getDelta() >= minDegree && distAz.getDelta() <= maxDegree);
    }

    private static double distanceToDegrees(Quantity minDist) {
        if(((UnitImpl)minDist.the_units).isConvertableTo(UnitImpl.DEGREE)) {
            return ((QuantityImpl)minDist).getValue(UnitImpl.DEGREE);
        }
        return DistAz.kilometersToDegrees(((QuantityImpl)minDist).getValue(UnitImpl.KILOMETER));
    }

    private static boolean inBox(Location point, BoxArea box) {
        return (point.latitude >= box.min_latitude
                && point.latitude <= box.max_latitude
                && point.longitude % 360 >= box.min_longitude % 360 && point.longitude % 360 <= box.max_longitude % 360);
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
            float lon = Float.parseFloat(tokenizer.nextToken());
            float lat = Float.parseFloat(tokenizer.nextToken());
            out.add(new Location(lat, lon, null, null, LocationType.GEOGRAPHIC));
        }
        return (Location[])out.toArray(new Location[0]);
    }
}