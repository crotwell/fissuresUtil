/**
 * TauPUtil.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.bag;

import java.util.HashMap;
import java.util.Map;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.TauModel;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.TauP.TauP_Time;

public class TauPUtil {

    private TauPUtil(String modelName) throws TauModelException {
        taup_time = new TauP_Time(modelName);
    }

    public Arrival[] calcTravelTimes(Station station, Origin origin, String[] phaseNames) throws TauModelException {
        return calcTravelTimes(station.my_location, origin, phaseNames);
    }

    public synchronized Arrival[] calcTravelTimes(Location stationLoc, Origin origin, String[] phaseNames) throws TauModelException {
        QuantityImpl depth = (QuantityImpl)origin.my_location.depth;
        depth = depth.convertTo(UnitImpl.KILOMETER);
        DistAz distAz = new DistAz(stationLoc, origin.my_location);
        taup_time.setSourceDepth(depth.getValue());
        taup_time.clearPhaseNames();
        for (int i = 0; i < phaseNames.length; i++) {
            taup_time.appendPhaseName(phaseNames[i]);
        }
        taup_time.calculate(distAz.getDelta());
        Arrival[] arrivals = taup_time.getArrivals();
        return arrivals;
    }

    public TauModel getTauModel() {
        return taup_time.getTauModel();
    }

    public synchronized static TauPUtil getTauPUtil() {
        try {
            return getTauPUtil("prem");
        } catch(TauModelException e) {
            throw new RuntimeException("Should never happen as prem is bundled with TauP", e);
        }
    }

    public synchronized static TauPUtil getTauPUtil(String modelName) throws TauModelException {
        if ( ! taupUtilMap.containsKey(modelName)) {
            taupUtilMap.put(modelName, new TauPUtil(modelName));
        }
        return (TauPUtil)taupUtilMap.get(modelName);
    }

    static Map taupUtilMap = new HashMap();

    TauP_Time taup_time;
}

