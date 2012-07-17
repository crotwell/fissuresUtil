package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.model.TimeInterval;
import edu.sc.seis.TauP.TauModelException;

/** Generalizes SimplePhaseStoN by allowing the phase for the short time window (numerator) to be
 * different from the phase for the long time window (denominator). This is useful when comparing the signal
 * of a later arriving phase, say SKS, with the noise level before the first arriving P.
 * @author crotwell
 *
 */
public class CompoundPhaseStoN extends SimplePhaseStoN {

    public CompoundPhaseStoN(String shortPhase, String longPhase) throws TauModelException {
        super(shortPhase);
        longCut = new PhaseCut(taup, longPhase, longOffsetBegin, longPhase, longOffsetEnd);
    }

    public CompoundPhaseStoN(String shortPhase,
                             TimeInterval shortOffsetBegin,
                             TimeInterval shortOffsetEnd,
                             String longPhase,
                             TimeInterval longOffsetBegin,
                             TimeInterval longOffsetEnd,
                             TauPUtil taup) throws TauModelException {
        super(shortPhase, shortOffsetBegin, shortOffsetEnd, longOffsetBegin, longOffsetEnd, taup);
        longCut = new PhaseCut(taup, longPhase, longOffsetBegin, longPhase, longOffsetEnd);
    }

    public CompoundPhaseStoN(String shortPhase,
                             TimeInterval shortOffsetBegin,
                             TimeInterval shortOffsetEnd,
                             String longPhase,
                             TimeInterval longOffsetBegin,
                             TimeInterval longOffsetEnd) throws TauModelException {
        super(shortPhase, shortOffsetBegin, shortOffsetEnd, longOffsetBegin, longOffsetEnd);
        longCut = new PhaseCut(taup, longPhase, longOffsetBegin, longPhase, longOffsetEnd);
    }
    
}
