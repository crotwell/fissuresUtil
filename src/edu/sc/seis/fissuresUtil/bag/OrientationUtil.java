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
            //Dip = Lat, Az = Lon
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
}