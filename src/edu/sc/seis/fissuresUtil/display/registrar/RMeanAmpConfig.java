package edu.sc.seis.fissuresUtil.display.registrar;
import edu.sc.seis.fissuresUtil.display.*;

import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.util.Iterator;
import org.apache.log4j.Logger;

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

    protected synchronized AmpEvent recalculateAmp(){
        Iterator e = ampData.keySet().iterator();
        double range = Double.NEGATIVE_INFINITY;
        while(e.hasNext()){
            UnitRangeImpl current =
                ((AmpConfigData)ampData.get(e.next())).getRange();
            if(current != null &&
               current.getMaxValue() - current.getMinValue() > range){
                range = current.getMaxValue() - current.getMinValue();
            }
        }
        DataSetSeismogram[] seismos = getSeismograms();
        UnitRangeImpl[] amps = new UnitRangeImpl[seismos.length];
        for(int i = 0; i < seismos.length; i++){
            amps[i] = setRange(((AmpConfigData)ampData.get(seismos[i])).getRange(),range);
        }
        return new AmpEvent(seismos, amps);
    }

    protected boolean setAmpRange(DataSetSeismogram seismo){
        AmpConfigData data = (AmpConfigData)ampData.get(seismo);
        SeismogramIterator it = data.getIterator();
        if ( !it.hasNext()) {
            return data.setRange(DisplayUtils.ZERO_RANGE);
        }

        double[] minMaxMean = it.minMaxMean();
        double meanDiff;
        double maxToMeanDiff = Math.abs(minMaxMean[2] - minMaxMean[1]);
        double minToMeanDiff = Math.abs(minMaxMean[2] - minMaxMean[0]);
        if(maxToMeanDiff > minToMeanDiff){
            meanDiff = maxToMeanDiff;
        }else{
            meanDiff = minToMeanDiff;
        }
        double min = minMaxMean[2] - meanDiff;
        double max = minMaxMean[2] + meanDiff;
        UnitImpl seisUnit = it.getSeismograms()[0].getUnit();
        return data.setRange(new UnitRangeImpl(min, max, seisUnit));
    }

    private UnitRangeImpl setRange(UnitRangeImpl currRange, double range){
        double middle = currRange.getMaxValue() - (currRange.getMaxValue() - currRange.getMinValue())/2;
        return new UnitRangeImpl(middle - range/2, middle + range/2, currRange.getUnit());
    }

    private static final Logger logger = Logger.getLogger(RMeanAmpConfig.class);
}// RMeanAmpConfig


