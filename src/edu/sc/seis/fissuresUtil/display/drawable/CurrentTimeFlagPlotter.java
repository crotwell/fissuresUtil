package edu.sc.seis.fissuresUtil.display.drawable;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.RTTimeRangeConfig;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;
import java.awt.Dimension;
import java.awt.Graphics2D;

/**
 * CurrentTimeFlagPlotter.java
 *
 *
 * Created: Fri Oct 25 14:28:50 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class CurrentTimeFlagPlotter extends FlagPlotter {
    public CurrentTimeFlagPlotter (){
        super(new MicroSecondDate().add(RTTimeRangeConfig.serverTimeOffset), "Current Time");
    }

    public void draw(Graphics2D canvas, Dimension size, TimeEvent timeEvent, AmpEvent ampEvent){
        setFlagTime(new MicroSecondDate().add(RTTimeRangeConfig.serverTimeOffset));
        super.draw(canvas, size, timeEvent, ampEvent);
    }

}// CurrentTimeFlagPlotter
