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

    protected void generateGenericAmp(){
        super.generateGenericAmp();
        if(amps.length == 1){
            double offset = (genericAmp.getMaxValue() - genericAmp.getMinValue())/2;
            genericAmp = UnitDisplayUtil.getBestForDisplay(new UnitRangeImpl(-1 * offset,
                                                                             offset ,
                                                                             genericAmp.getUnit()));
        }
    }

}

