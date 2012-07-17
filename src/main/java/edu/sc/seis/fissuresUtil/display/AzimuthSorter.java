/**
 * AzimuthSorter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.display.registrar.AzimuthLayoutConfig;

public class AzimuthSorter extends DistanceSeisSorter{
    public AzimuthSorter(){
        layoutConfig = new AzimuthLayoutConfig();
    }
}

