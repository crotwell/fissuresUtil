package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.display.registrar.LayoutConfig;
import edu.sc.seis.fissuresUtil.display.registrar.LayoutEvent;
import edu.sc.seis.fissuresUtil.display.registrar.LayoutListener;

public class DistanceScaleMapper extends UnitRangeMapper implements LayoutListener{
    public DistanceScaleMapper(int totalPixels, int hintPixels,
                               LayoutConfig config){
        super(totalPixels, hintPixels, false);
        setConfig(config);
    }

    public void updateLayout(LayoutEvent e) {
        setUnitRange(e.getDistance());
    }

    public void setConfig(LayoutConfig config){
        if(this.config != null){
            this.config.removeListener(this);
        }
        config.addListener(this);
        this.config = config;
    }

    public String getAxisLabel() {
        return config.getLabel();
    }

    private LayoutConfig config;
}
