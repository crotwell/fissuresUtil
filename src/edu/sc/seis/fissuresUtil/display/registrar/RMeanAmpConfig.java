package edu.sc.seis.fissuresUtil.display.registrar;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.SeismogramIterator;
import edu.sc.seis.fissuresUtil.display.UnitDisplayUtil;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
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
    public RMeanAmpConfig(){}

    public RMeanAmpConfig(DataSetSeismogram[] seismos){
        super(seismos);
    }

    protected AmpEvent recalculateAmp(){
        AmpConfigData[] ad = getAmpData();
        double range = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < ad.length; i++){
            UnitRangeImpl current = ad[i].getRange();
            if(current != null &&
               current.getMaxValue() - current.getMinValue() > range){
                range = current.getMaxValue() - current.getMinValue();
            }
        }
        UnitRangeImpl[] amps = new UnitRangeImpl[ad.length];
        for(int i = 0; i < ad.length; i++){
            amps[i] = setRange(ad[i].getRange(),range);
        }
        return new BasicAmpEvent(AmpConfigData.getSeismograms(ad), amps);
    }

    protected boolean setAmpRange(AmpConfigData data){
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
        return data.setRange(UnitDisplayUtil.getBestForDisplay(new UnitRangeImpl(min, max, data.getUnit())));
    }

    private UnitRangeImpl setRange(UnitRangeImpl currRange, double range){
        double middle = currRange.getMaxValue() - (currRange.getMaxValue() - currRange.getMinValue())/2;
        return new UnitRangeImpl(middle - range/2, middle + range/2, currRange.getUnit());
    }

    private static final Logger logger = Logger.getLogger(RMeanAmpConfig.class);
}// RMeanAmpConfig
