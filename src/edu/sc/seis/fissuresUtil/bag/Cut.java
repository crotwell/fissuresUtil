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
 * @version $Id: Cut.java 2883 2002-11-07 02:54:56Z crotwell $
 */

public class Cut implements LocalSeismogramFunction {

    public Cut(MicroSecondDate begin,
	       MicroSecondDate end) {
	this.begin = begin;
	this.end = end;
    }

    public LocalSeismogramImpl apply(LocalSeismogramImpl seis) {
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

	TimeSeriesDataSel dataSel = new TimeSeriesDataSel();
	if (seis.can_convert_to_short()) {
	    short[] outS = new short[endIndex-beginIndex];
	    short[] inS = seis.get_as_shorts();
	    System.arraycopy(inS, beginIndex, outS, 0, endIndex-beginIndex);
	    dataSel.sht_values(outS);
	} else if (seis.can_convert_to_long()) {
	    int[] outI = new int[endIndex-beginIndex];
	    int[] inI = seis.get_as_longs();
	    System.arraycopy(inI, beginIndex, outI, 0, endIndex-beginIndex);
	    dataSel.int_values(outI);
	} else if (seis.can_convert_to_float()) {
	    float[] outF = new float[endIndex-beginIndex];
	    float[] inF = seis.get_as_floats();
	    System.arraycopy(inF, beginIndex, outF, 0, endIndex-beginIndex);
	    dataSel.flt_values(outF);
	} else {
	    double[] outD = new double[endIndex-beginIndex];
	    double[] inD = seis.get_as_doubles();
	    System.arraycopy(inD, beginIndex, outD, 0, endIndex-beginIndex);
	    dataSel.dbl_values(outD);
	} // end of else	TimeSeriesType dataType = seis.getDataType();

	return new LocalSeismogramImpl(seis, dataSel);
    }

    protected MicroSecondDate begin;
    protected MicroSecondDate end;

}// Cut
