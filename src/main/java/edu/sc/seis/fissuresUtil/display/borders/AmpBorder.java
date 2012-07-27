package edu.sc.seis.fissuresUtil.display.borders;

import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.UnitDisplayUtil;

public class AmpBorder extends AbstractUnitRangeBorder {

    public AmpBorder(SeismogramDisplay disp) {
        this(disp, LEFT);
    }

    public AmpBorder(SeismogramDisplay disp, int side) {
        this(disp, side, true);
    }

    public AmpBorder(SeismogramDisplay disp, int side,
            boolean includeAmplitudeLabel) {
        super(side, ASCENDING);
        this.disp = disp;
        if(includeAmplitudeLabel) {
            add(new UnchangingTitleProvider("Amplitude"));
        }
    }

    public UnitRangeImpl getRange() {
        last = disp.getAmpConfig().getAmp();
        if(SeismogramDisplay.PRINTING && last.equals(DisplayUtils.ONE_RANGE)) {
            //Since ONE_RANGE may indicate that the amp config doesn't know,
            // have
            //it calculate just in case new data has come in since our intitial
            //get amp
            last = disp.getAmpConfig().calculate().getAmp();
        }
        return last;
    }

    public String getTitle() {
        return UnitDisplayUtil.getNameForUnit(last.getUnit());
    }

    private UnitRangeImpl last;

    private SeismogramDisplay disp;
}