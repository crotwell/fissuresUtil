package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfTimeSeries.*;

/**
 * Cut.java
 *
 *
 * Created: Tue Oct  1 21:23:44 2002
 *
 * @author Philip Crotwell
 * @version $Id: Cut.java 2675 2002-10-04 17:22:16Z crotwell $
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
	if (beginIndex >=  seis.getNumPoints()) {
	    beginIndex = seis.getNumPoints()-1;
	}

	QuantityImpl endShift = seis.getEndTime().subtract(end);
	endShift = endShift.divideBy(sampPeriod);
	int endIndex = seis.getNumPoints() - (int)Math.floor(endShift.value);
	if (endIndex < 0) {
	    endIndex = 0;
	}
	if (endIndex >  seis.getNumPoints()) {
	    endIndex = seis.getNumPoints();
	}

	TimeSeriesType dataType = seis.getDataType();
	TimeSeriesDataSel dataSel = new TimeSeriesDataSel();
	switch (dataType.value()) {
	case TimeSeriesType._TYPE_LONG:
	    int[] outI = new int[endIndex-beginIndex];
	    int[] inI = seis.get_as_longs();
	    System.arraycopy(inI, beginIndex, outI, 0, endIndex-beginIndex);
	    dataSel.int_values(outI);
	    break;
	case TimeSeriesType._TYPE_SHORT:
	    short[] outS = new short[endIndex-beginIndex];
	    short[] inS = seis.get_as_shorts();
	    System.arraycopy(inS, beginIndex, outS, 0, endIndex-beginIndex);
	    dataSel.sht_values(outS);
	    break;
	case TimeSeriesType._TYPE_FLOAT:
	    float[] outF = new float[endIndex-beginIndex];
	    float[] inF = seis.get_as_floats();
	    System.arraycopy(inF, beginIndex, outF, 0, endIndex-beginIndex);
	    dataSel.flt_values(outF);
	    break;
	case TimeSeriesType._TYPE_DOUBLE:
	    double[] outD = new double[endIndex-beginIndex];
	    double[] inD = seis.get_as_doubles();
	    System.arraycopy(inD, beginIndex, outD, 0, endIndex-beginIndex);
	    dataSel.dbl_values(outD);
	    break;
	    
	default:
	    // must be encoded?

	    break;
	} // end of switch (dataType.value())
	
	return new LocalSeismogramImpl(seis, dataSel);
    }

    
}// Cut
