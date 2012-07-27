package edu.sc.seis.fissuresUtil.display.borders;

import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.UnitDisplayUtil;
import edu.sc.seis.fissuresUtil.time.MicroSecondTimeRange;

public class TimeElapsedBorder extends UnitRangeBorder {

    public TimeElapsedBorder(SeismogramDisplay disp) {
        this(disp, UnitImpl.SECOND);
    }

    public TimeElapsedBorder(SeismogramDisplay disp, UnitImpl units) {
        this(disp, units, TOP);
    }

    public TimeElapsedBorder(SeismogramDisplay disp, UnitImpl units, int side) {
        super(side, ASCENDING, "Time (" + UnitDisplayUtil.getNameForUnit(units)
                + ")");
        this.disp = disp;
        this.units = units;
    }

    public UnitRangeImpl getRange() {
        MicroSecondTimeRange timeRange = disp.getTimeConfig().getTime();
        UnitRangeImpl ur = new UnitRangeImpl(0,
                                             timeRange.getInterval().value,
                                             timeRange.getInterval().the_units);
        return ur.convertTo(units);
    }

    private SeismogramDisplay disp;

    private UnitImpl units;
}
