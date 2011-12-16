package edu.sc.seis.fissuresUtil.display.drawable;

import java.awt.Dimension;
import java.awt.Graphics2D;

import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;

/**
 * CurrentTimeFlagPlotter.java
 *
 *
 * Created: Fri Oct 25 14:28:50 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class CurrentTimeFlag extends Flag {
    public CurrentTimeFlag (){
        super(ClockUtil.now(), "Current Time");
    }

    public void draw(Graphics2D canvas, Dimension size, TimeEvent timeEvent, AmpEvent ampEvent){
        setFlagTime(ClockUtil.now());
        super.draw(canvas, size, timeEvent, ampEvent);
    }

}// CurrentTimeFlagPlotter
