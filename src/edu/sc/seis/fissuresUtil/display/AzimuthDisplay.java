/**
 * AzimuthDisplay.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.display.registrar.AzimuthLayoutConfig;
import edu.sc.seis.fissuresUtil.display.registrar.LayoutConfig;


public class AzimuthDisplay extends RecordSectionDisplay{
    
    
    protected LayoutConfig getNewLayoutConfig(){
        return new AzimuthLayoutConfig();
    }
}

