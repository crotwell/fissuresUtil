package edu.sc.seis.fissuresUtil.display.borders;

import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;

public class UnitRangeBorder extends AbstractUnitRangeBorder{
    public UnitRangeBorder(int side, int order, String title){
        this(side, order, title, DisplayUtils.ONE_RANGE);
    }

    public UnitRangeBorder(int side, int order, String title,
                           UnitRangeImpl initialRange){
        super(side, order);
        this.title = title;
        setRange(initialRange);
    }

    public String getTitle() { return title; }

    public UnitRangeImpl getRange(){ return range; }

    public void setRange(UnitRangeImpl range){ this.range = range; }

    private UnitRangeImpl range;

    private String title;
}
