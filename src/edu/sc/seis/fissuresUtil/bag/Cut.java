package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.model.*;

/**
 * Cut.java
 *
 *
 * Created: Tue Oct  1 21:23:44 2002
 *
 * @author Philip Crotwell
 * @version $Id: Cut.java 2661 2002-10-02 01:41:35Z crotwell $
 */

public class Cut {

    public static LocalSeismogramImpl cut(LocalSeismogramImpl seis,
					  MicroSecondDate begin,
					  MicroSecondDate end) {
	TimeInterval sampPeriod = seis.getSampling().getPeriod();
	QuantityImpl beginShift = seis.getBeginTime().subtract(begin);
	beginShift = beginShift.divideBy(sampPeriod);
	QuantityImpl endShift = seis.getEndTime().subtract(end);
	endShift = endShift.divideBy(sampPeriod);

	return new LocalSeismogramImpl(seis, seis.getData());
    }

    
}// Cut
