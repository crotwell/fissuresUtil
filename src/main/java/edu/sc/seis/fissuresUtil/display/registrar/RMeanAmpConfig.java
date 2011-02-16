package edu.sc.seis.fissuresUtil.display.registrar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.SeismogramIterator;
import edu.sc.seis.fissuresUtil.display.UnitDisplayUtil;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

/**
 * RMeanAmpConfig.java
 * 
 * 
 * Created: Thu Oct 3 09:46:23 2002
 * 
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */
public class RMeanAmpConfig extends BasicAmpConfig {

    public RMeanAmpConfig() {}

    public RMeanAmpConfig(DataSetSeismogram[] seismos) {
        super(seismos);
    }

    public AmpEvent recalculate() {
        AmpConfigData[] ad = getAmpData();
        double range = Double.NEGATIVE_INFINITY;
        for(int i = 0; i < ad.length; i++) {
            UnitRangeImpl current = ad[i].getRange();
            if(current != null
                    && current.getMaxValue() - current.getMinValue() > range) {
                range = current.getMaxValue() - current.getMinValue();
            }
        }
        UnitRangeImpl[] amps = new UnitRangeImpl[ad.length];
        for(int i = 0; i < ad.length; i++) {
            amps[i] = setRange(ad[i].getRange(), range);
        }
        UnitRangeImpl genericAmp = DisplayUtils.ONE_RANGE;
        if(ad.length == 1 || AmpConfigData.isAllFromSameSite(ad)) {
            genericAmp = UnitDisplayUtil.getRealWorldUnitRange(amps[0],
                                                               ad[0].getDSS());
        }
        return new BasicAmpEvent(AmpConfigData.getSeismograms(ad),
                                 amps,
                                 genericAmp);
    }

    protected boolean setAmpRange(AmpConfigData data) {
        SeismogramIterator it = data.getIterator();
        if(!it.hasNext()) {
            return data.setRange(DisplayUtils.ONE_RANGE);
        }
        double[] minMaxMean = it.minMaxMean();
        double meanDiff;
        double maxToMeanDiff = Math.abs(minMaxMean[2] - minMaxMean[1]);
        double minToMeanDiff = Math.abs(minMaxMean[2] - minMaxMean[0]);
        if(maxToMeanDiff > minToMeanDiff) {
            meanDiff = maxToMeanDiff;
        } else {
            meanDiff = minToMeanDiff;
        }
        double min = minMaxMean[2] - meanDiff;
        double max = minMaxMean[2] + meanDiff;
        return data.setRange(new UnitRangeImpl(min, max, it.getUnit()));
    }

    private UnitRangeImpl setRange(UnitRangeImpl currRange, double range) {
        double middle = currRange.getMaxValue()
                - (currRange.getMaxValue() - currRange.getMinValue()) / 2;
        return new UnitRangeImpl(middle - range / 2,
                                 middle + range / 2,
                                 currRange.getUnit());
    }

    private static final Logger logger = LoggerFactory.getLogger(RMeanAmpConfig.class);
}// RMeanAmpConfig
