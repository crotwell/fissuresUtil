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
    
     public RMeanAmpConfig(){}

    public RMeanAmpConfig(AmpConfigRegistrar registrar){
	super.addAmpSyncListener(registrar);
    }

    public RMeanAmpConfig(AmpConfigRegistrar registrar, TimeConfigRegistrar tr){
	super.addAmpSyncListener(registrar);
	super.timeRegistrar = tr;
    }
    
    /** if this Amp Config has a TimeRangeConfig, this returns the ampRange over the timerange for that object.  Otherwise, it uses the 
     *  full time range for this seismogram.
     */
    public UnitRangeImpl getAmpRange(DataSetSeismogram aSeis){
	 LocalSeismogramImpl seis = aSeis.getSeismogram();
	 MicroSecondTimeRange pastTime = (MicroSecondTimeRange)seismoTimes.get(aSeis);
	 if(timeRegistrar == null){
	     return getAmpRange(aSeis, (MicroSecondTimeRange)seismoTimes.get(aSeis));
	 }else{
	     if (!timeRegistrar.contains(aSeis)) {
		 getAmpRange(aSeis, timeRegistrar.getTimeRange());
		 timeRegistrar.addSeismogram(aSeis);
	     } 
	     return getAmpRange(aSeis, timeRegistrar.getTimeRange(aSeis));
	 }
    }
    
    public UnitRangeImpl getAmpRange(DataSetSeismogram aSeis, MicroSecondTimeRange calcIntv){
	if(seismoTimes.get(aSeis) != null && ((MicroSecondTimeRange)seismoTimes.get(aSeis)).equals(calcIntv)){
	    return (UnitRangeImpl)seismoAmps.get(aSeis);
	}
	seismoTimes.put(aSeis, new MicroSecondTimeRange(calcIntv.getBeginTime(), calcIntv.getEndTime()));
	LocalSeismogramImpl seis = aSeis.getSeismogram();
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
	    //no data points in window, leave range alone
	    seismoAmps.put(aSeis, ampRange);
	    return ampRange;
        }
	try {
            double min = seis.getMinValue(beginIndex, endIndex).getValue();
	    double max = seis.getMaxValue(beginIndex, endIndex).getValue();
	    double mean = seis.getMeanValue(beginIndex, endIndex).getValue();
	    double meanDiff = (Math.abs(mean - min) > Math.abs(mean - max) ? Math.abs(mean - min) : Math.abs(mean - max));
	    if(ampRange == null){
		ampRange = new UnitRangeImpl(-meanDiff, meanDiff, seis.getAmplitudeRange().getUnit());
	    }else if(meanDiff > ampRange.getMaxValue()){
		ampRange.min_value = -meanDiff;
		ampRange.max_value = meanDiff;
	    }
	    double bottom = ampRange.getMinValue() + mean;
	    double top = ampRange.getMaxValue() + mean;
	    UnitRangeImpl current;
	    if(seismoAmps.get(aSeis) != null){
		current = (UnitRangeImpl)seismoAmps.get(aSeis);
		current.min_value = bottom;
		current.max_value = top;
	    }else{
		current = new UnitRangeImpl(bottom, top, seis.getAmplitudeRange().getUnit());
	    }
	    seismoAmps.put(aSeis, current);
	    return current;
	} catch (Exception e) {
	    e.printStackTrace();
        }
	    seismoAmps.put(aSeis, ampRange);
	    return ampRange;
    }

    public void addSeismogram(DataSetSeismogram seis){
	this.getAmpRange(seis);
	this.updateAmpSyncListeners();
    }
    
    public void removeSeismogram(DataSetSeismogram aSeis){ 
	if (seismoAmps.size() == 1) {
	    super.removeSeismogram(aSeis);
	    return;
	} // end of if (seismos.size() == 1)
	MicroSecondTimeRange calcIntv;
	LocalSeismogramImpl seis = aSeis.getSeismogram();
	if(this.timeRegistrar == null)
	    calcIntv = new MicroSecondTimeRange(seis.getBeginTime(), seis.getEndTime());
	else
	    calcIntv = timeRegistrar.getTimeRange(aSeis);
	if(seismoAmps.containsKey(aSeis)){
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
		seismoAmps.remove(aSeis);
		seismoTimes.remove(aSeis);
		return;
	    }
	    try {
		double min = seis.getMinValue(beginIndex, endIndex).getValue();
		double max = seis.getMaxValue(beginIndex, endIndex).getValue();
		double mean = seis.getMeanValue(beginIndex, endIndex).getValue();
		double meanDiff = (Math.abs(mean - min) > Math.abs(mean - max) ? Math.abs(mean - min) : Math.abs(mean - max));
		if(meanDiff >=  this.ampRange.getMaxValue() - 1)
		    this.ampRange = null;
	    } 
	    catch (Exception e) {
		this.ampRange = null;
	    }
	    seismoAmps.remove(aSeis);
	    seismoTimes.remove(aSeis);
	    if(ampRange == null){
		Iterator e = seismoAmps.keySet().iterator();
		logger.debug("recalculating amp range as defining seismogram was removed");
		while(e.hasNext())
		    this.getAmpRange(((DataSetSeismogram)e.next()));
		this.updateAmpSyncListeners();
	    }
	}
    }
}// RMeanAmpConfig
