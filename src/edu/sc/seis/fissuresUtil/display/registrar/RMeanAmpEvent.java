/**
 * RMeanAmpEvent.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.display.registrar;

import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.display.UnitDisplayUtil;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

public class RMeanAmpEvent extends BasicAmpEvent {

    public RMeanAmpEvent(DataSetSeismogram[] seismos, UnitRangeImpl[] amps){
        super(seismos, amps);
    }

    protected UnitRangeImpl calcGenericAmp(UnitRangeImpl inAmp, DataSetSeismogram seis) {
        UnitRangeImpl basic = super.calcGenericAmp(inAmp, seis);
        double offset = (basic.getMaxValue() - basic.getMinValue())/2;
        UnitRangeImpl out = new UnitRangeImpl(-1 * offset,
                                              offset ,
                                              basic.getUnit());
        return UnitDisplayUtil.getBestForDisplay(out);
    }

}

