/**
 * LongShortStoN.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

public class LongShortStoN
{

    LongShortStoN(TimeInterval longTime, TimeInterval shortTime, float threshold) {
        this.longTime = longTime;
        this.shortTime = shortTime;
        this.threshold = threshold;
    }

    public float ratio(DataSetSeismogram seis, MicroSecondDate mark) {

        return 1;
    }

    protected TimeInterval longTime;
    protected TimeInterval shortTime;
    protected float threshold;
}

