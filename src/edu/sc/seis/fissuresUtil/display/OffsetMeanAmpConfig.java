package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.seismogramDC.*;

import java.util.*;

/**
 * OffsetMeanAmpConfig sets every seismogram to zero amplitude at its mean, and displays a user defined amount around it.  If no offset is 
 * given, 500 is used. These amplitude ranges can be set to be over the full time of the seismograms, or only a certain interval 
 * specified by a TimeRangeConfig object given to this amp config
 *
 *
 * Created: Tue May 28 14:40:39 2002
 *
 * @author Charlie Groves
 * @version 0.1
 */

public class OffsetMeanAmpConfig extends AbstractAmpRangeConfig{
    public OffsetMeanAmpConfig(DataSetSeismogram aSeis, MicroSecondTimeRange range){
	//this.ampRegistrar = registrar;
	LocalSeismogramImpl seis = (LocalSeismogramImpl)aSeis.getSeismogram();
	int beginIndex = SeisPlotUtil.getPixel(seis.getNumPoints(),
                                               seis.getBeginTime(),
                                               seis.getEndTime(),
					       range.getBeginTime());
	if (beginIndex < 0) beginIndex = 0;
	if (beginIndex > seis.getNumPoints()) beginIndex = seis.getNumPoints();
	int endIndex = SeisPlotUtil.getPixel(seis.getNumPoints(),
                                               seis.getBeginTime(),
                                               seis.getEndTime(),
                                               range.getEndTime());
        if (endIndex < 0) endIndex = 0;
        if (endIndex > seis.getNumPoints()) endIndex = seis.getNumPoints();

	if (endIndex == beginIndex) {
	   ampRange =  new UnitRangeImpl(-500, 500, UnitImpl.COUNT);
        }
        try {
	    double min = seis.getMinValue(beginIndex, endIndex).getValue();
	    double max = seis.getMaxValue(beginIndex, endIndex).getValue();
	    double mean = seis.getMeanValue(beginIndex, endIndex).getValue();
	    double meanDiff = (Math.abs(mean - min) > Math.abs(mean - max) ? Math.abs(mean - min) : Math.abs(mean - max));
	    ampRange = new UnitRangeImpl(-meanDiff, meanDiff, seis.getAmplitudeRange().getUnit());
	}catch (Exception e) {
	    ampRange = new UnitRangeImpl(-500, 500, UnitImpl.COUNT);
        }
    }

    public OffsetMeanAmpConfig(){
	ampRange = new UnitRangeImpl(-500, 500, UnitImpl.COUNT);
    }

/** Returns the OffsetMean amplitude for a given seismogram over its full time range
     */
    public UnitRangeImpl getAmpRange(DataSetSeismogram aSeis){
	return this.getAmpRange(aSeis,
				new MicroSecondTimeRange(((LocalSeismogramImpl)aSeis.getSeismogram()).getBeginTime(), 
							 ((LocalSeismogramImpl)aSeis.getSeismogram()).getEndTime()));
    }

    /** Returns the OffsetMean amplitude for a given seismogram over a set time range
     */
    public UnitRangeImpl getAmpRange(DataSetSeismogram aSeis, MicroSecondTimeRange calcIntv){
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
            // no data points in window, leave config alone
	    return ampRange;
        }
        try {
	    double mean = seis.getMeanValue(beginIndex, endIndex).getValue();
	    double bottom = this.ampRange.getMinValue() + mean;
	    double top = this.ampRange.getMaxValue() + mean;
	    return new UnitRangeImpl(bottom,
				     top,
				     seis.getAmplitudeRange().getUnit());

	    
	} catch (Exception e) {
	    ampRange = null;
        }
	return ampRange;
    }

    /** Sets this amp config to work over the given TimeRangeConfig
     */
    public void visibleAmpCalc(TimeConfigRegistrar timeRegistrar){
	if(seismos.size() < 1)
	    return;
	LocalSeismogramImpl seis = ((DataSetSeismogram)seismos.getFirst()).getSeismogram();
	int beginIndex = SeisPlotUtil.getPixel(seis.getNumPoints(),
                                               seis.getBeginTime(),
                                               seis.getEndTime(),
					       timeRegistrar.getTimeRange().getBeginTime());
	if (beginIndex < 0) beginIndex = 0;
	if (beginIndex > seis.getNumPoints()) beginIndex = seis.getNumPoints();
	int endIndex = SeisPlotUtil.getPixel(seis.getNumPoints(),
                                               seis.getBeginTime(),
                                               seis.getEndTime(),
                                               timeRegistrar.getTimeRange().getEndTime());
        if (endIndex < 0) endIndex = 0;
        if (endIndex > seis.getNumPoints()) endIndex = seis.getNumPoints();

	if (endIndex == beginIndex) {
	   ampRange =  new UnitRangeImpl(-500, 500, UnitImpl.COUNT);
        }
        try {
	    double min = seis.getMinValue(beginIndex, endIndex).getValue();
	    double max = seis.getMaxValue(beginIndex, endIndex).getValue();
	    double mean = seis.getMeanValue(beginIndex, endIndex).getValue();
	    double meanDiff = (Math.abs(mean - min) > Math.abs(mean - max) ? Math.abs(mean - min) : Math.abs(mean - max));
	    ampRange = new UnitRangeImpl(-meanDiff, meanDiff, seis.getAmplitudeRange().getUnit());
	}catch(Exception e){e.printStackTrace();}
	this.updateAmpSyncListeners();
    }
    
    public void addSeismogram(DataSetSeismogram seis){
	this.getAmpRange(seis);
	seismos.add(seis);
	this.updateAmpSyncListeners();
    }

    public int getOffset(){ return offset; }
    
    public void setOffset(int i) { 
	this.offset = i;
	ampRange = new UnitRangeImpl(-offset, offset, UnitImpl.COUNT);
    }

    protected int offset;
    
}// OffsetMeanAmpConfig
