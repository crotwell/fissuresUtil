package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.UnitRangeImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.UnsupportedDataEncoding;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.Statistics;
import java.util.Iterator;
import org.apache.log4j.*;

/**
 * RMeanAmpConfig.java
 *
 *
 * Created: Thu Oct  3 09:46:23 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class RMeanAmpConfig extends BasicAmpConfig {
    public RMeanAmpConfig(DataSetSeismogram[] seismos){
	super(seismos);
    }
    
    protected AmpEvent calculateAmp(){
	Iterator e = ampData.keySet().iterator();
	boolean changed = false;
	while(e.hasNext()){
	    AmpConfigData current = (AmpConfigData)ampData.get(e.next());
	    if(current.setTime(getTime(current.getSeismogram()))){ //checks for the time update equaling the old time
		if(setAmpRange(current.getSeismogram())){ //checks if the new time changes the amp range
		    changed = true;// only generates a new amp event if the amp ranges have changed
		}
	    }
	}
	if(changed || currentAmpEvent == null){
	    recalculateAmp();
	}
	return currentAmpEvent;
    }

    protected AmpEvent recalculateAmp(){
	Iterator e = ampData.keySet().iterator();
	double range = Double.NEGATIVE_INFINITY;
	while(e.hasNext()){
	    UnitRangeImpl current = ((AmpConfigData)ampData.get(e.next())).getShaledRange();
	    if(current != null && current.getMaxValue() - current.getMinValue() > range){
		range = current.getMaxValue() - current.getMinValue();
	    }
	}
	DataSetSeismogram[] seismos = getSeismograms();
	UnitRangeImpl[] amps = new UnitRangeImpl[seismos.length];
	for(int i = 0; i < seismos.length; i++){
	    amps[i] = setRange(((AmpConfigData)ampData.get(seismos[i])).getShaledRange(),range);
	}
	currentAmpEvent = new AmpEvent(seismos, amps);
	return currentAmpEvent;
    }
    
    private boolean setAmpRange(DataSetSeismogram seismo){
	AmpConfigData data = (AmpConfigData)ampData.get(seismo);
	LocalSeismogramImpl seis = (LocalSeismogramImpl)seismo.getSeismogram();
	int[] seisIndex = DisplayUtils.getSeisPoints(seis, data.getTime());
	if(seisIndex[0] == seisIndex[1]) {
	    //no data points in window, set range to 0
	    data.setCalcIndex(seisIndex);
	    return data.setCleanRange(DisplayUtils.ZERO_RANGE);
	}
	double[] minMaxMean = ((Statistics)DisplayUtils.statCache.get(seismo)).minMaxMean(seisIndex[0], seisIndex[1]);
	double meanDiff = (Math.abs(minMaxMean[2] - minMaxMean[0]) > Math.abs(minMaxMean[2] - minMaxMean[1]) ? 
			   Math.abs(minMaxMean[2] - minMaxMean[0]) : 
			   Math.abs(minMaxMean[2] - minMaxMean[1]));
	data.setCalcIndex(seisIndex);
	return data.setCleanRange(new UnitRangeImpl(minMaxMean[2] - meanDiff, minMaxMean[2] + meanDiff, UnitImpl.COUNT));
    }

    private UnitRangeImpl setRange(UnitRangeImpl currRange, double range){
	double middle = currRange.getMaxValue() - (currRange.getMaxValue() - currRange.getMinValue())/2;
	return new UnitRangeImpl(middle - range/2, middle + range/2, UnitImpl.COUNT);
    }

    private static Category logger = Category.getInstance(RMeanAmpConfig.class.getName());

}// RMeanAmpConfig
