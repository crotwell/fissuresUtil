/**
 * DistanceBorder.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.display.borders;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.display.RecordSectionDisplay;



public class DistanceBorder extends AbstractUnitRangeBorder{
    public DistanceBorder(RecordSectionDisplay rsd){
        this(rsd,Border.DESCENDING);
    }
    public DistanceBorder(RecordSectionDisplay rsd, int order){
        super(LEFT,order);
        this.rsd=rsd;
        displayNegatives = false;
    }

    public String getTitle() { return "Distance (Degrees)"; }

    public UnitRangeImpl getRange() {
        return rsd.getLayoutConfig().getLayout().getDistance();
    }

    private RecordSectionDisplay rsd;
}

