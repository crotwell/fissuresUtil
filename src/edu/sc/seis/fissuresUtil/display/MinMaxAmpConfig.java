package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.seismogramDC.*;

import java.util.*;

/**
 * MinMaxAmpConfig returns the minimum and maximum amplitude of the seismograms it holds either over the entire time for each object, or over
 * the time specified for them in a TimeRangeConfig object
 *
 *
 * Created: Thu May 23 16:02:44 2002
 *
 * @author Charlie Groves
 * @version 0.1
 */

public class MinMaxAmpConfig extends AbstractAmpRangeConfig{
   
    /** Returns the amp range for a particular seismogram over its complete time.
     */
    public UnitRangeImpl getAmpRange(DataSetSeismogram aSeis){
	if(timeRegistrar == null)
	    return this.getAmpRange(aSeis,
				    new MicroSecondTimeRange(((LocalSeismogramImpl)aSeis.getSeismogram()).getBeginTime(), 
							     ((LocalSeismogramImpl)aSeis.getSeismogram()).getEndTime()));
	else
	    return this.getAmpRange(aSeis, timeRegistrar.getTimeRange(aSeis));
    }

    /** Returns the amp range for a particular seismogram over a particular time range.  If it is already in the config and a new time 
     *  range has not been set, the current ampRange is returned.  If not, then it is calculated and checked against the current ampRange
     */
    public UnitRangeImpl getAmpRange(DataSetSeismogram aSeis, MicroSecondTimeRange calcIntv){
	boolean update = false;
	if(seismoTimes.get(aSeis) != null && ((MicroSecondTimeRange)seismoTimes.get(aSeis)).equals(calcIntv)){
	    return (UnitRangeImpl)seismoAmps.get(aSeis);
	}
	seismoTimes.put(aSeis, new MicroSecondTimeRange(calcIntv.getBeginTime(), calcIntv.getEndTime()));
	LocalSeismogramImpl seis = (LocalSeismogramImpl)aSeis.getSeismogram();
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
	    return ampRange;
        }
        try {
	    double minValue = seis.getMinValue(beginIndex, endIndex).getValue();
	    double maxValue = seis.getMaxValue(beginIndex, endIndex).getValue();
	    if(ampRange == null){
		ampRange = new UnitRangeImpl(minValue, maxValue, seis.getAmplitudeRange().getUnit());
		update = true;
	    }else{
		if(minValue < ampRange.getMinValue()){
		    ampRange.min_value = minValue;
		    update = true;
		}
		if(maxValue > ampRange.getMaxValue()){
		    ampRange.max_value = maxValue;
		update = true;
		}
	    }
	    seismoAmps.put(aSeis, ampRange);
	} catch (Exception e) {
	    ampRange = null;
        }
	if(update){
	    updateAmpSyncListeners();
	}
	return ampRange;
    }

    /** Sets the config to get amplitude ranges based on a particular TimeRangeConfig
     */
    public void visibleAmpCalc(TimeConfigRegistrar timeRegistrar){
	UnitRangeImpl tempRange = ampRange;
	ampRange = null;
	this.timeRegistrar = timeRegistrar;
	intvCalc = true;
	Iterator e = seismoAmps.keySet().iterator();
	while(e.hasNext()){
	    DataSetSeismogram current = (DataSetSeismogram)e.next();
	    this.getAmpRange(current, timeRegistrar.getTimeRange(current));
	}
	intvCalc = false;
	if(ampRange == null)
	    ampRange = tempRange;
	this.updateAmpSyncListeners();
    }

    /** Adds a seismogram to the current config and adjusts the ranges if it defines the minimum or maximum amplitude
     */
    public void addSeismogram(DataSetSeismogram aSeis){
	this.getAmpRange(aSeis);
    }

    /** Removes a seismogram from the current config and adjusts the amp range if it is the defining seismogram
     */
    public void removeSeismogram(DataSetSeismogram aSeis){ 
	if(seismoAmps.containsKey(aSeis)){
	    LocalSeismogramImpl seis = (LocalSeismogramImpl)aSeis.getSeismogram();
	    MicroSecondTimeRange calcIntv;
	    if(timeRegistrar == null){
		calcIntv = new MicroSecondTimeRange(seis.getBeginTime(), seis.getEndTime());
	    }
	    else{
		calcIntv = timeRegistrar.getTimeRange(aSeis);
	    }
	    try {
		UnitRangeImpl current;
		if(calcIntv.equals(seismoTimes.get(aSeis))){
		    current = (UnitRangeImpl)seismoAmps.get(aSeis);
		}else{
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
			seismoAmps.remove(seis);
			seismoTimes.remove(seis);
			return;
		    }
		    current = new UnitRangeImpl(seis.getMinValue(beginIndex, endIndex).getValue(), 
						seis.getMaxValue(beginIndex, endIndex).getValue(), 
						seis.getAmplitudeRange().getUnit());
		}
		
		if(current.getMinValue() == ampRange.getMinValue())
		    this.ampRange = null;
		else if(current.getMaxValue() == ampRange.getMaxValue())
		    this.ampRange = null;
	    } 
	    catch (Exception e) {
		ampRange = null;
	    }
	    seismoAmps.remove(seis);
	    seismoTimes.remove(seis);
	    if(ampRange == null)
		this.updateAmpSyncListeners();
	}
    }

    public void fireAmpRangeEvent(AmpSyncEvent event) {};

}// MinMaxAmpConfig
