/**
 * BackAzimuthDisplay.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.display.registrar.BackAzimuthLayoutConfig;

public class BackAzimuthDisplay extends RecordSectionDisplay{
    
    public BackAzimuthDisplay() {
        this(false);
    }

    public BackAzimuthDisplay(boolean swapAxes) {
        super(swapAxes);
        setLayout(new BackAzimuthLayoutConfig());
    }
    
}

