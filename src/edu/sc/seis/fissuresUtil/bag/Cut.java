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
 * @version $Id: Cut.java 2673 2002-10-04 01:34:58Z crotwell $
 */

public class Cut {

    public static LocalSeismogramImpl cut(LocalSeismogramImpl seis,
					  MicroSecondDate begin,
					  MicroSecondDate end) {
	TimeInterval sampPeriod = seis.getSampling().getPeriod();
	QuantityImpl beginShift = begin.subtract(seis.getBeginTime());
	beginShift = beginShift.divideBy(sampPeriod);
	int beginIndex = (int)Math.ceil(beginShift.value);
	if (beginIndex < 0) {
	    beginIndex = 0;
	} // end of if (beginIndex < 0)
	
	QuantityImpl endShift = seis.getEndTime().subtract(end);
	endShift = endShift.divideBy(sampPeriod);
	int endIndex = seis.getNumPoints() - (int)Math.floor(endShift.value);



	return new LocalSeismogramImpl(seis, seis.getData());
    }

    
}// Cut
