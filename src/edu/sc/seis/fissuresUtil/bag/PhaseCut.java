/**
 * PhaseCut.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.TauP.TauP_Time;
import org.apache.log4j.Logger;
import edu.iris.Fissures.FissuresException;

public class PhaseCut {

    /** warning, this class assumes that no other thread will be accessing
     the TauP_Time class while it is being used here. If another thread
     accesses it, the results will be unpredictable. */
    public PhaseCut(TauPUtil timeCalc,
                    String beginPhase, TimeInterval beginOffset,
                    String endPhase, TimeInterval endOffset) {
        this.timeCalc = timeCalc;
        this.beginPhase = beginPhase;
        this.beginOffset = beginOffset;
        this.endPhase = endPhase;
        this.endOffset = endOffset;
    }

    /** Cuts the seismogram based on offsets from the given phases.
     *
     * @throws PhaseNonExistent if either of the phases does not exist
     *    at the distance.
     */
    public LocalSeismogramImpl cut(Location stationLoc,
                                   Origin origin,
                                   LocalSeismogramImpl seis)
        throws TauModelException, PhaseNonExistent, FissuresException  {
        Arrival[] arrivals = timeCalc.calcTravelTimes(stationLoc, origin, new String[] {beginPhase, endPhase});

        MicroSecondDate beginTime = null;
        MicroSecondDate endTime = null;
        MicroSecondDate originTime = new MicroSecondDate(origin.origin_time);
        for (int i=0; i< arrivals.length; i++) {
            if (arrivals[i].getName().equals(beginPhase)) {
                beginTime = originTime.add(new TimeInterval(arrivals[i].getTime(),
                                                            UnitImpl.SECOND));
                break;
            }
        }
        if (beginTime == null) {
            DistAz distAz = new DistAz(stationLoc.latitude,
                                       stationLoc.longitude,
                                       origin.my_location.latitude,
                                       origin.my_location.longitude);
            throw new PhaseNonExistent("Phase "+beginPhase+
                                           " does not exist at this distance, "+
                                           distAz.delta+" degrees");
        }
        beginTime = beginTime.add(beginOffset);

        for (int i=0; i< arrivals.length; i++) {
            if (arrivals[i].getName().equals(endPhase)) {
                endTime = originTime.add(new TimeInterval(arrivals[i].getTime(),
                                                          UnitImpl.SECOND));
                break;
            }
        }
        if (endTime == null) {
            DistAz distAz = new DistAz(stationLoc.latitude,
                                       stationLoc.longitude,
                                       origin.my_location.latitude,
                                       origin.my_location.longitude);
            throw new PhaseNonExistent("Phase "+endPhase+
                                           " does not exist at this distance, "+
                                           distAz.delta+" degrees");
        }
        endTime = endTime.add(endOffset);
        logger.debug("Phase cut from "+beginTime+" to "+endTime);

        Cut cut = new Cut(beginTime, endTime);
        return cut.apply(seis);
    }

    TauPUtil timeCalc;

    String beginPhase;

    TimeInterval beginOffset;

    String endPhase;

    TimeInterval endOffset;

    Logger logger = Logger.getLogger(PhaseCut.class);
}

