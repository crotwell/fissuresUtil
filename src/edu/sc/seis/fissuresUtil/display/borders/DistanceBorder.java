/**
 * DistanceBorder.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.fissuresUtil.display.borders;

import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.display.RecordSectionDisplay;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;

public class DistanceBorder extends AbstractUnitRangeBorder {

    public DistanceBorder(RecordSectionDisplay rsd) {
        this(rsd, Border.LEFT, Border.DESCENDING);
    }

    public DistanceBorder(RecordSectionDisplay rsd, int side, int order) {
        super(side, order);
        this.rsd = rsd;
        this.minTickValue = 0;
    }

    public String getTitle() {
        return rsd.getLayoutConfig().getLabel();
    }

    public UnitRangeImpl getRange() {
        return rsd.getDistance();
    }

    private RecordSectionDisplay rsd;
}