/**
 * BackAzimuthSorter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.display.registrar.BackAzimuthLayoutConfig;

public class BackAzimuthSorter extends DistanceSeisSorter{
    public BackAzimuthSorter(){
        layoutConfig = new BackAzimuthLayoutConfig();
    }
}

