/**
 * DistanceBorder.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.fissuresUtil.display.borders;

import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;

public class DistanceBorder extends AbstractUnitRangeBorder {

    public DistanceBorder(SeismogramDisplay rsd) {
        this(rsd, Border.LEFT, Border.DESCENDING);
    }

    public DistanceBorder(SeismogramDisplay rsd, int side, int order) {
        super(side, order);
        this.rsd = rsd;
        this.clipTicks = true;
        this.minTickValue = 0;
        this.maxTickValue = 180;
    }

    public String getTitle() {
        return "Distance (Degrees)";
    }

    public UnitRangeImpl getRange() {
        return rsd.getDistance();
    }

    private SeismogramDisplay rsd;
}