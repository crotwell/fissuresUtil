/**
 * TauPUtil.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.TauModel;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.TauP.TauP_Time;

public class TauPUtil {

    public TauPUtil(String modelName) throws TauModelException {
        taup_time = new TauP_Time(modelName);
    }

    public Arrival[] calcTravelTimes(Station station, Origin origin, String[] phaseNames) throws TauModelException {
        return calcTravelTimes(station.my_location, origin, phaseNames);
    }

    public synchronized Arrival[] calcTravelTimes(Location stationLoc, Origin origin, String[] phaseNames) throws TauModelException {
        QuantityImpl depth = (QuantityImpl)origin.my_location.depth;
        depth = depth.convertTo(UnitImpl.KILOMETER);
        DistAz distAz = calcDistAz(stationLoc, origin);
        taup_time.setSourceDepth(depth.getValue());
        taup_time.clearPhaseNames();
        for (int i = 0; i < phaseNames.length; i++) {
            taup_time.appendPhaseName(phaseNames[i]);
        }
        taup_time.calculate(distAz.delta);
        Arrival[] arrivals = taup_time.getArrivals();
        return arrivals;
    }

    public DistAz calcDistAz(Location stationLoc, Origin origin) {
        return new DistAz(stationLoc.latitude,
                          stationLoc.longitude,
                          origin.my_location.latitude,
                          origin.my_location.longitude);
    }

    public DistAz calcTravelTimes(Station station, Origin origin) {
        return calcDistAz(station.my_location, origin);
    }

    public TauModel getTauModel() {
        return taup_time.getTauModel();
    }

    TauP_Time taup_time;
}

