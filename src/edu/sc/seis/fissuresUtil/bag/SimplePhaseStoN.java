/**
 * SimplePhaseStoN.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.TauModelException;

/** Calculates a signal to noise ration around a phase. The short time window
 * (numerator of the ratio) is given by the standard deviation of the section of the seismogram
 * from phase + shortOffsetBegin to phase + shortOffsetEnd. The long time
 * window (demominator of the ratio) is similar. The first arriving phase of
 * the calculated arrivals is used. */
public class SimplePhaseStoN {

    public SimplePhaseStoN(String phase,
                           TimeInterval shortOffsetBegin,
                           TimeInterval shortOffsetEnd,
                           TimeInterval longOffsetBegin,
                           TimeInterval longOffsetEnd,
                           String modelName,
                           TauPUtil taup) throws TauModelException {
        this.phase = phase;
        this.shortOffsetBegin = shortOffsetBegin;
        this.shortOffsetEnd = shortOffsetEnd;
        this.longOffsetBegin = longOffsetBegin;
        this.longOffsetEnd = longOffsetEnd;

        if (shortOffsetBegin == null) {
            throw new NullPointerException("shortOffsetBegin cannot be null");
        }
        if (shortOffsetEnd == null) {
            throw new NullPointerException("shortOffsetBegin cannot be null");
        }
        if (longOffsetBegin == null) {
            throw new NullPointerException("shortOffsetBegin cannot be null");
        }
        if (longOffsetEnd == null) {
            throw new NullPointerException("shortOffsetBegin cannot be null");
        }
        this.taup = taup;
        shortCut = new PhaseCut(taup, phase, shortOffsetBegin, phase, shortOffsetEnd);
        longCut = new PhaseCut(taup, phase, longOffsetBegin, phase, longOffsetEnd);
    }


    public SimplePhaseStoN(String phase,
                           TimeInterval shortOffsetBegin,
                           TimeInterval shortOffsetEnd,
                           TimeInterval longOffsetBegin,
                           TimeInterval longOffsetEnd) throws TauModelException {
        this(phase,
             shortOffsetBegin,
             shortOffsetEnd,
             longOffsetBegin,
             longOffsetEnd,
             "prem",
             TauPUtil.getTauPUtil("prem"));
    }

    /** Defaults to plus and minues 5 seconds around the phase for the short
     * time interval, and the preceeded 100 seconds before that for the long
     * time interval. */
    public SimplePhaseStoN(String phase) throws TauModelException {
        this(phase,
             new TimeInterval(-5, UnitImpl.SECOND),
             new TimeInterval(+5, UnitImpl.SECOND),
             new TimeInterval(-100, UnitImpl.SECOND),
             new TimeInterval(-5, UnitImpl.SECOND));
    }

    /** Calculates the trigger value for the given wondows. Returns null if
     * either of the windows have no data in them. */
    public LongShortTrigger process(Location stationLoc,
                                    Origin origin,
                                    LocalSeismogramImpl seis) throws FissuresException, TauModelException, PhaseNonExistent {
        LocalSeismogramImpl shortSeis = shortCut.cut(stationLoc, origin, seis);
        LocalSeismogramImpl longSeis = longCut.cut(stationLoc, origin, seis);
        if (shortSeis == null || longSeis == null) { return null; }

        Statistics shortStat = new Statistics(shortSeis);
        double numerator = shortStat.stddev();
        Statistics longStat = new Statistics(longSeis);
        double denominator = longStat.stddev();

        Arrival[] arrivals = taup.calcTravelTimes(stationLoc, origin, new String[] {phase});
        MicroSecondDate phaseTime = null;
        MicroSecondDate originTime = new MicroSecondDate(origin.origin_time);
        if (arrivals.length != 0) {
            phaseTime = originTime.add(new TimeInterval(arrivals[0].getTime(),
                                                        UnitImpl.SECOND));
        }

        TimeInterval sampPeriod = (TimeInterval)seis.getSampling().getPeriod().convertTo(UnitImpl.SECOND);
        int phaseIndex = (int)seis.getBeginTime().subtract(phaseTime).convertTo(UnitImpl.SECOND).divideBy(sampPeriod).get_value();
        float ratio = (float)(numerator/denominator);
        return new LongShortTrigger(seis, phaseIndex, ratio, (float)numerator, (float)denominator);
    }

    protected String phase;
    protected TimeInterval shortOffsetBegin;
    protected TimeInterval shortOffsetEnd;
    protected TimeInterval longOffsetBegin;
    protected TimeInterval longOffsetEnd;
    protected PhaseCut shortCut;
    protected PhaseCut longCut;
    protected TauPUtil taup;
}

