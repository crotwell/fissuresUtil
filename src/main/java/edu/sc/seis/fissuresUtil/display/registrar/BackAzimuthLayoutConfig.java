/**
 * BackAzimuthLayoutConfig.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.display.registrar;

import edu.iris.Fissures.model.QuantityImpl;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

public class BackAzimuthLayoutConfig extends BasicLayoutConfig{
    public void add(DataSetSeismogram[] seismos){
        boolean someAdded = false;
        for (int i = 0; i < seismos.length; i++){
            if(!(valueMap.containsKey(seismos[i]))){
                QuantityImpl baz = DisplayUtils.calculateBackAzimuth(seismos[i]);
                seis.add(seismos[i]);
                valueMap.put(seismos[i], baz);
                someAdded = true;
            }
        }
        if(someAdded){
            fireLayoutEvent();
        }
    }
    
    public String getLabel(){ return "Back Azimuth (Degrees)"; }
}

