package edu.sc.seis.fissuresUtil.display;


import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.seismogramDC.*;

import java.util.*;

/**
 * RMeanAmpConfig sets a range equal to the largest difference between the minimum and maximum of a seismogram it contains.  Each seismogram
 * has its amplituded centered around its mean.  These amplitude ranges can be set to be over the full time of the seismograms, or only 
 * a certain interval specified by a TimeRangeConfig object given to this amp config
 *
 *
 * Created: Tue May 28 11:31:56 2002
 *
 * @author Charlie Groves
 * @version
 */

public class RMeanAmpConfig extends AbstractAmpRangeConfig{
    
    /** if this Amp Config has a TimeRangeConfig, this returns the ampRange over the timerange for that object.  Otherwise, it uses the 
     *  full time range for this seismogram.
     */
    public UnitRangeImpl getAmpRange(LocalSeismogram aSeis){
	if(timeConfig == null)
	    return this.getAmpRange(aSeis,new MicroSecondTimeRange(((LocalSeismogramImpl)aSeis).getBeginTime(), 
							((LocalSeismogramImpl)aSeis).getEndTime()));
	else
	    return this.getAmpRange(aSeis, this.timeConfig.getTimeRange(aSeis));
    }
    
    public UnitRangeImpl getAmpRange(LocalSeismogram aSeis, MicroSecondTimeRange calcIntv){
	LocalSeismogramImpl seis = (LocalSeismogramImpl)aSeis;
	int beginIndex = SeisPlotUtil.getPixel(seis.getNumPoints(),
                                               seis.getBeginTime(),
                                               seis.getEndTime(),
					       calcIntv.getBeginTime());
	if (beginIndex < 0) beginIndex = 0;
	if (beginIndex > seis.getNumPoints()) beginIndex = seis.getNumPoints();
	int endIndex = SeisPlotUtil.getPixel(seis.getNumPoints(),
                                               seis.getBeginTime(),
                                               seis.getEndTime(),
                                               calcIntv.getEndTime());
        if (endIndex < 0) endIndex = 0;
        if (endIndex > seis.getNumPoints()) endIndex = seis.getNumPoints();

	if (endIndex == beginIndex) {
            // no data points in window, leave config alone
	    return ampRange;
        }
        try {
            double min = seis.getMinValue(beginIndex, endIndex).getValue();
	    double max = seis.getMaxValue(beginIndex, endIndex).getValue();
	    double mean = seis.getMeanValue(beginIndex, endIndex).getValue();
	    double meanDiff = (Math.abs(mean - min) > Math.abs(mean - max) ? Math.abs(mean - min) : Math.abs(mean - max));
	    if(this.ampRange == null)
		this.ampRange = new UnitRangeImpl(-meanDiff, meanDiff, seis.getAmplitudeRange().getUnit());
	    else if(meanDiff > this.ampRange.getMaxValue())
		this.ampRange = new UnitRangeImpl(-meanDiff, meanDiff, seis.getAmplitudeRange().getUnit());
	    double bottom = this.ampRange.getMinValue() + mean;
	    double top = this.ampRange.getMaxValue() + mean;
	    return new UnitRangeImpl(bottom,
				     top,
				     seis.getAmplitudeRange().getUnit());

	    
	} catch (Exception e) {
	    e.printStackTrace();
        }
	return ampRange;
    }

    public void visibleAmpCalc(TimeRangeConfig timeConfig){
	UnitRangeImpl tempRange = ampRange;
	ampRange = null;
	this.timeConfig = timeConfig;
	Iterator e = seismos.iterator();
	while(e.hasNext()){
	    LocalSeismogram current = (LocalSeismogram)e.next();
	    this.getAmpRange(current, timeConfig.getTimeRange(current));
	}
	if(ampRange == null){
	    ampRange = tempRange;
	    return;
	}
	this.updateAmpSyncListeners();
    }
    
    public void addSeismogram(LocalSeismogram seis){
	this.getAmpRange(seis);
	seismos.add(seis);
	this.updateAmpSyncListeners();
    }
    
    public void removeSeismogram(LocalSeismogram aSeis){ 
	MicroSecondTimeRange calcIntv;
	if(this.timeConfig == null)
	    calcIntv = new MicroSecondTimeRange(((LocalSeismogramImpl)aSeis).getBeginTime(), ((LocalSeismogramImpl)aSeis).getEndTime());
	else
	    calcIntv = timeConfig.getTimeRange(aSeis);
	if(seismos.contains(aSeis)){
	    LocalSeismogramImpl seis = (LocalSeismogramImpl)aSeis;
	    int beginIndex = SeisPlotUtil.getPixel(seis.getNumPoints(),
						   seis.getBeginTime(),
						   seis.getEndTime(),
						   calcIntv.getBeginTime());
	    if (beginIndex < 0) beginIndex = 0;
	    if (beginIndex > seis.getNumPoints()) beginIndex = seis.getNumPoints();
	    int endIndex = SeisPlotUtil.getPixel(seis.getNumPoints(),
						 seis.getBeginTime(),
						 seis.getEndTime(),
						 calcIntv.getEndTime());
	    if (endIndex < 0) endIndex = 0;
	    if (endIndex > seis.getNumPoints()) endIndex = seis.getNumPoints();
	    
	    if (endIndex == beginIndex) {
		seismos.remove(aSeis);
		return;
	    }
	    try {
		double min = seis.getMinValue(beginIndex, endIndex).getValue();
		double max = seis.getMaxValue(beginIndex, endIndex).getValue();
		double mean = seis.getMeanValue(beginIndex, endIndex).getValue();
		double meanDiff = (Math.abs(mean - min) > Math.abs(mean - max) ? Math.abs(mean - min) : Math.abs(mean - max));
		if(meanDiff ==  this.ampRange.getMaxValue())
		    this.ampRange = null;
	    } 
	    catch (Exception e) {
		this.ampRange = null;
	    }
	    seismos.remove(aSeis);
	    if(ampRange == null){
		Iterator e = seismos.iterator();
		while(e.hasNext())
		    this.getAmpRange(((LocalSeismogram)e.next()), calcIntv);
		this.updateAmpSyncListeners();
	    }
	}
    }

}// RMeanAmpConfig
