/**
 * BackAzimuthDisplay.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.display.registrar.BackAzimuthLayoutConfig;
import edu.sc.seis.fissuresUtil.display.registrar.LayoutConfig;

public class BackAzimuthDisplay extends RecordSectionDisplay{
    
    
    protected LayoutConfig getNewLayoutConfig(){
        return new BackAzimuthLayoutConfig();
    }
}

