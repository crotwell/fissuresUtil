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
    public UnitRangeImpl getAmpRange(LocalSeismogram aSeis){
	if(timeRegistrar == null)
	    return this.getAmpRange(aSeis,
				    new MicroSecondTimeRange(((LocalSeismogramImpl)aSeis).getBeginTime(), ((LocalSeismogramImpl)aSeis).getEndTime()));
	else
	    return this.getAmpRange(aSeis, timeRegistrar.getTimeRange(aSeis));
    }

    /** Returns the amp range for a particular seismogram over a particular time range.  If it is already in the config and a new time 
     *  range has not been set, the current ampRange is returned.  If not, then it is calculated and checked against the current ampRange
     */
    public UnitRangeImpl getAmpRange(LocalSeismogram aSeis, MicroSecondTimeRange calcIntv){
	if(seismos.contains(aSeis) && !intvCalc && ampRange != null)
	    return ampRange;
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
	    return ampRange;
        }
        try {
            
            if(this.ampRange == null){
		this.ampRange = new UnitRangeImpl(seis.getMinValue(beginIndex, endIndex).getValue(), 
							       seis.getMaxValue(beginIndex, endIndex).getValue(), 
						  seis.getAmplitudeRange().getUnit());
		return ampRange;
	    }
	if(seis.getMinValue(beginIndex, endIndex).getValue() < this.ampRange.getMinValue())
	    this.ampRange = new UnitRangeImpl(seis.getMinValue(beginIndex, endIndex).getValue(),
					      this.ampRange.getMaxValue(),
					      this.ampRange.getUnit());
	if(seis.getMaxValue(beginIndex, endIndex).getValue() > this.ampRange.getMaxValue())
	    this.ampRange = new UnitRangeImpl(this.ampRange.getMinValue(), 
					      seis.getMaxValue(beginIndex, endIndex).getValue(),
					      this.ampRange.getUnit());
	} catch (Exception e) {
	    ampRange = null;
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
	Iterator e = seismos.iterator();
	while(e.hasNext()){
	    LocalSeismogram current = (LocalSeismogram)e.next();
	    this.getAmpRange(current, timeRegistrar.getTimeRange(current));
	}
	intvCalc = false;
	if(ampRange == null)
	    ampRange = tempRange;
	this.updateAmpSyncListeners();
    }

    /** Adds a seismogram to the current config and adjusts the ranges if it defined the minimum or maximum amplitude
     */
    public void addSeismogram(LocalSeismogram aSeis){
	this.getAmpRange(aSeis);
	seismos.add(aSeis);
	this.updateAmpSyncListeners();
    }

    /** Removes a seismogram from the current config
     */
    public void removeSeismogram(LocalSeismogram aSeis){ 
	if(seismos.contains(aSeis)){
	    LocalSeismogramImpl seis = (LocalSeismogramImpl)aSeis;
	    MicroSecondTimeRange calcIntv;
	    if(timeRegistrar == null)
		calcIntv = new MicroSecondTimeRange(seis.getBeginTime(), seis.getEndTime());
	    else
		calcIntv = timeRegistrar.getTimeRange(aSeis);
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
		seismos.remove(seis);
		return;
	    }
        try {
	    if(seis.getMinValue(beginIndex, endIndex).getValue() == this.ampRange.getMinValue())
		this.ampRange = null;
	    else if(seis.getMaxValue(beginIndex, endIndex).getValue() == this.ampRange.getMaxValue())
		this.ampRange = null;
	} 
	catch (Exception e) {
	    ampRange = null;
        }
	seismos.remove(seis);
	if(ampRange == null)
	    this.updateAmpSyncListeners();
	}
    }

    public void fireAmpRangeEvent(AmpSyncEvent event) {};

}// MinMaxAmpConfig
