/**
 * LongShortStoN.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.util.LinkedList;

/** Adapted from reftrg.f from Tom Owens.
 * c
 c     routine to apply the reftek trigger algorithm
 c     to a designated SAC file
 c     LTA is initialized to STA after 2 STA time constants
 c     Trigger detection begins after trgdly seconds
 c
 c     compile with: f77 reftrg.f $SACDIR/lib/sac.a -f68881
 c
 c     Written by T.J. Owens, August 16, 1988
 c
 */
public class LongShortStoN {

    /**
     * @param longTime Time Interval for the long term average
     * @param shortTime Time Interval for the short term average
     * @param threshold ration of short to long termaverages above which a trigger is declared
     **/
    public LongShortStoN(TimeInterval longTime, TimeInterval shortTime, float threshold) {
        this(longTime, shortTime, threshold, (TimeInterval)shortTime.multiplyBy(2));
    }


    /**
     * @param longTime Time Interval for the long term average
     * @param shortTime Time Interval for the short term average
     * @param threshold ration of short to long termaverages above which a trigger is declared
     **/
    public LongShortStoN(TimeInterval longTime, TimeInterval shortTime, float threshold, TimeInterval delay) {
        if (longTime.lessThanEqual(shortTime)) {
            throw new IllegalArgumentException("longTime must be longer than shortTime, longTime="+longTime+
                                                   "  shortTime="+shortTime);
        }
        if (delay.lessThan(shortTime)) {
            throw new IllegalArgumentException("delay must be longer than shortTime, shortTime="+shortTime+
                                                   "  delay="+delay);
        }
        this.longTime = longTime;
        this.shortTime = shortTime;
        this.threshold = threshold;
        this.delay = delay;
    }

    public LongShortTrigger[] calcTriggers(LocalSeismogramImpl seis) throws FissuresException {
        LinkedList out = new LinkedList();
        float[] seisData = seis.get_as_floats();

        //   establish number of points in LTA and STA windows
        //    as well as in trgdly

        float dt = (float)seis.getSampling().getPeriod().convertTo(UnitImpl.SECOND).get_value();
        int nlta=(int)(longTime.divideBy(dt).convertTo(UnitImpl.SECOND).getValue()) + 1;
        int nsta=(int)(shortTime.divideBy(dt).convertTo(UnitImpl.SECOND).getValue()) + 1;
        int ntdly=(int)(delay.divideBy(dt).convertTo(UnitImpl.SECOND).getValue()) + 1;

        if (seis.getEndTime().subtract(seis.getBeginTime()).lessThan(delay) || nsta > ntdly || ntdly > seis.getNumPoints()) {
            // seis is too short, so no trigger possible
            return new LongShortTrigger[0];
        }

        //  n100 is number of data points in 100 second window
        //      (needed for data mean calculation)

        int n100=(int)(100./dt) + 1;

        //     clta and csta are constants in trigger algoritms

        float clta=1.0f/nlta;
        float csta=1.0f/nsta;

        float xmean=0.0f;

        float ylta=0;
        float prevylta=0;
        float ysta=0;
        float prevysta=0;

        // initialize STA, start at delay and sum backwards
        for (int j = 0; j < nsta && j < ntdly; j++) {
            ysta += seisData[ntdly-j-1];
        }
        // initialize LTA, start at delay and sum backwards
        for (int j = 0; j < nlta && j < ntdly; j++) {
            ylta += seisData[ntdly-j-1];
        }
        int nmean = 0;
        for (nmean = 0; nmean < n100 && nmean < ntdly; nmean++) {
            xmean += seisData[ntdly-nmean-1];
        }

        //    start the triggering process
        for (int i = ntdly; i < seisData.length; i++) {
            //    after 100 seconds, data mean is mean of previous 100 seconds only
            if (nmean == n100) {
                xmean -= seisData[i-n100];
            } else {
                nmean++;
            }
            xmean += seisData[i];

            //    LTA value calculated as per REFTEK algorithm
            prevylta = ylta;
            ylta = clta*Math.abs(seisData[i] - xmean/nmean)
                + (1-clta)*prevylta;

            //    STA value calculated as per REFTEK algorithm
            prevysta = ysta;
            ysta = csta*Math.abs(seisData[i] - xmean/nmean)
                + (1-csta)*prevysta;

            //   rat is STA/LTA at each time point
            float ratio;
            if (ylta != 0) {
                ratio=ysta/ylta;
            } else {
                // in this case, declare a trigger if ysta != 0, otherwise not
                if (ysta != 0) {
                    ratio = threshold;
                } else {
                    ratio = 0;
                }
            }
            if (ratio >= threshold) {
                LongShortTrigger trigger = new LongShortTrigger(seis,
                                                                i,
                                                                ratio);
                out.add(trigger);
            }
        }
        LongShortTrigger[] trigger = (LongShortTrigger[])out.toArray(new LongShortTrigger[0]);
        return trigger;
    }

    protected TimeInterval longTime;
    protected TimeInterval shortTime;
    protected TimeInterval delay;
    protected float threshold;
}

