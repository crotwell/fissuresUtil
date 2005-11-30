package edu.sc.seis.fissuresUtil.bag;

import java.util.ArrayList;
import java.util.List;
import edu.iris.Fissures.Orientation;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.OrientationRange;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.TauP.SphericalCoords;

/**
 * @author groves Created on Oct 7, 2004
 */
public class OrientationUtil {

    public static Channel[] inOrientation(OrientationRange orient,
                                          Channel[] chans) {
        double centerAZ = orient.center.azimuth;
        double centerDip = orient.center.dip;
        double degDist = QuantityImpl.createQuantityImpl(orient.angular_distance)
                .convertTo(UnitImpl.DEGREE).value;
        List results = new ArrayList();
        for(int i = 0; i < chans.length; i++) {
            Channel chan = chans[i];
            Orientation chanOrient = chan.an_orientation;
            // Dip = Lat, Az = Lon
            double dist = SphericalCoords.distance(centerDip,
                                                   centerAZ,
                                                   chanOrient.dip,
                                                   chanOrient.azimuth);
            if(dist <= degDist) {
                results.add(chan);
            }
        }
        return (Channel[])results.toArray(new Channel[results.size()]);
    }

    public static boolean areOrthogonal(Orientation one, Orientation two) {
        double A1 = Math.toRadians(one.azimuth);
        double A2 = Math.toRadians(two.azimuth);
        double D1 = Math.toRadians(one.dip);
        double D2 = Math.toRadians(two.dip);
        if(one.dip == two.dip) {
            if(Math.abs(one.dip) == 90) {
                return false;
            } else if(one.dip == 0) {
                return isOddInteger(TWO_OVER_PI * (A1 - A2));
            } else {
                return isOddInteger(TWO_OVER_PI * (D1 - D2));
            }
        }
        return (Math.cos(A1 - A2) * Math.cos(D1) * Math.cos(D2) + Math.sin(D1))
                * Math.sin(D2) == 0;
    }

    private static boolean isOddInteger(double d) {
        return d == (int)d && d % 2 != 0;
    }

    private static final double TWO_OVER_PI = 2d / Math.PI;

    public static String toString(Orientation orientation) {
        return "az="+orientation.azimuth+", dip="+orientation.dip;
    }
}