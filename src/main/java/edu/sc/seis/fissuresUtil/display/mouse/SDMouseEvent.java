package edu.sc.seis.fissuresUtil.display.mouse;

import java.awt.Component;
import java.awt.event.MouseEvent;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplayProvider;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;
import edu.sc.seis.fissuresUtil.time.MicroSecondTimeRange;

public class SDMouseEvent extends MouseEvent{
    public SDMouseEvent(SeismogramDisplay sd, MouseEvent me){
        super((Component)me.getSource(), me.getID(), me.getWhen(),
              me.getModifiers(), me.getX(), me.getY(), me.getClickCount(),
              me.isPopupTrigger(), me.getButton());
        this.sd = sd;
    }

    public MicroSecondDate getTime(int pixel) {
        MicroSecondTimeRange tr =  getDisplay().getTimeConfig().getTime();
        return SimplePlotUtil.getValue(getComponent().getWidth(),
                                       tr.getBeginTime(),
                                       tr.getEndTime(),
                                       pixel);
    }

    public int getPixel(MicroSecondDate time){
        MicroSecondTimeRange tr =  getDisplay().getTimeConfig().getTime();
        return SimplePlotUtil.getPixel(getComponent().getWidth(),
                                       tr.getBeginTime(),
                                       tr.getEndTime(),
                                       time);
    }

    public MicroSecondDate getTime(){
        MicroSecondTimeRange currRange = getDisplay().getTimeConfig().getTime();
        long beginMicros = currRange.getBeginTime().getMicroSecondTime();
        double intervalMicros = currRange.getInterval().getValue();
        double xPer = getXPercent();
        return new MicroSecondDate((long)(beginMicros + intervalMicros * xPer));
    }

    public QuantityImpl getAmp(){
        UnitRangeImpl cur = getDisplay().getAmpConfig().getAmp();
        Component comp = getComponent();
        double yPercent = (comp.getHeight() - getY())/(double)comp.getHeight();
        double amp = (cur.getMaxValue() - cur.getMinValue()) * yPercent + cur.getMinValue();
        return new QuantityImpl(amp, cur.getUnit());
    }

    public static SDMouseEvent wrap(MouseEvent e){
        return new SDMouseEvent(((SeismogramDisplayProvider)e.getComponent()).provide(),
                                e);
    }

    /**
     * @returns the percentage of the display's width where the pointer is.
     * i.e. if the display is 500 pixels wide at x = 0, returns 0 at 250 returns
     * .5 and at 499 returns 1
     */
    public double getXPercent(){
        return getX()/(double)getComponent().getWidth();
    }

    public SeismogramDisplay getDisplay(){ return sd; }

    private SeismogramDisplay sd;
}

