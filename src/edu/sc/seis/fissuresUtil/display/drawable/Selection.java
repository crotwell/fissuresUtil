package edu.sc.seis.fissuresUtil.display.drawable;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import org.apache.log4j.Category;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.BasicTimeConfig;
import edu.sc.seis.fissuresUtil.display.registrar.PhaseAlignedTimeConfig;
import edu.sc.seis.fissuresUtil.display.registrar.RTTimeRangeConfig;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeListener;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;


/**
 * Selection.java
 *
 *
 * Created: Thu Jun 20 15:14:47 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class Selection implements TimeListener, Drawable{
    public Selection (MicroSecondTimeRange range, SeismogramDisplay parent, Color color){
        
        // tc needs to be the same class as the parents time config incase it is a relative time config
        TimeConfig parentConfig = parent.getTimeConfig();
        if(parentConfig instanceof RTTimeRangeConfig){
            parentConfig = ((RTTimeRangeConfig)parentConfig).getInternalConfig();
        }
        Class dispTimeConfigClass = parentConfig.getClass();
        try {
            tc = (TimeConfig)dispTimeConfigClass.newInstance();
            if(parentConfig instanceof PhaseAlignedTimeConfig){
                ((PhaseAlignedTimeConfig)tc).setPhaseName(((PhaseAlignedTimeConfig)parentConfig).getPhaseName());
            }
        } catch (IllegalAccessException e) {
            GlobalExceptionHandler.handle("Problem trying to create new TimeCongig for pick zone", e);
        } catch (InstantiationException e) {
            GlobalExceptionHandler.handle("Problem trying to create new TimeCongig for pick zone", e);
        }
        seismos = parent.getSeismograms();
        this.parent = parent;
        this.color = color;
        tc.addListener(this);
        tc.add(seismos);
        setBegin(range.getBeginTime());
        setInterval(range.getInterval());
        parent.repaint();
    }
    
    public void updateTime(TimeEvent event){
        latestTime = event;
        repaintParent();
    }
    
    public void toggleVisibility(){ visible = !visible; }
    
    public void setVisibility(boolean b){ visible = b; }
    
    public boolean isVisible(TimeEvent externalTime){
        MicroSecondTimeRange currentExternal = externalTime.getTime();
        MicroSecondTimeRange currentInternal = latestTime.getTime();
        if(!visible || currentExternal.getBeginTime().getMicroSecondTime() >= currentInternal.getEndTime().getMicroSecondTime() ||
           currentExternal.getEndTime().getMicroSecondTime() <= currentInternal.getBeginTime().getMicroSecondTime())
            return false;
        return true;
    }
    
    public void draw(Graphics2D canvas, Dimension size, TimeEvent timeEvent, AmpEvent ampEvent){
        if(isVisible(timeEvent)){
            Rectangle2D selection =
                new Rectangle2D.Float(getX(size.width, timeEvent),
                                      -1,
                                          (float)(getWidth(timeEvent) *size.width),
                                      size.height+1);
            canvas.setPaint(color);
            canvas.fill(selection);
            //canvas.draw(selection);
            canvas.setPaint(color.darker());
            canvas.setStroke(DisplayUtils.THREE_PIXEL_STROKE);
            canvas.draw(selection);
        }
    }
    
    public boolean isRemoveable(){
        if(latestTime.getTime().getInterval().getValue()/
           parent.getTimeConfig().getTime().getInterval().getValue() < .01){
            return true;
        }
        return false;
    }
    
    public void remove(){
        parent.remove(this);
        if(child != null){
            child.remove(getSeismograms());
        }
    }
    
    private void removeFromAllChildren(){
        child.remove(getSeismograms());
    }
    
    public boolean borders(MicroSecondDate selectionBegin, MicroSecondDate selectionEnd){
        double timeWidth = parent.getTimeConfig().getTime().getInterval().getValue();
        MicroSecondTimeRange currentInternal = latestTime.getTime();
        if(Math.abs(currentInternal.getEndTime().getMicroSecondTime() - selectionBegin.getMicroSecondTime())/timeWidth <.03 ||
           Math.abs(currentInternal.getBeginTime().getMicroSecondTime() - selectionEnd.getMicroSecondTime())/timeWidth < .03)
            return true;
        return false;
    }
    
    public void setParent(SeismogramDisplay parent){ this.parent = parent; }
    
    public SeismogramDisplay getParent(){ return parent; }
    
    public void setChild(SeismogramDisplay child){ this.child = child; }
    
    public SeismogramDisplay getChild(){ return child; }
    
    public void repaintParent(){
        parent.repaint();
    }
    
    public float getX(int width, TimeEvent currentExternalState){
        MicroSecondTimeRange currentExternal = currentExternalState.getTime();
        float offset = (latestTime.getTime().getBeginTime().getMicroSecondTime() -
                            currentExternal.getBeginTime().getMicroSecondTime())/(float)currentExternal.getInterval().getValue();
        return offset * width;
    }
    
    public double getWidth(TimeEvent currentExternalState){
        MicroSecondTimeRange currentInternal = latestTime.getTime();
        return ((currentInternal.getEndTime().getMicroSecondTime() - currentInternal.getBeginTime().getMicroSecondTime())/
                    currentExternalState.getTime().getInterval().getValue());
    }
    
    public Color getColor(){ return color; }
    
    public void setColor(Color color){ this.color = color; }
    
    public DataSetSeismogram[] getSeismograms(){ return seismos; }
    
    public void setTime(MicroSecondTimeRange selRange) {
        MicroSecondDate currentBegin = latestTime.getTime().getBeginTime();
        MicroSecondDate newBegin = selRange.getBeginTime();
        TimeInterval timeInt = (TimeInterval)latestTime.getTime().getInterval().convertTo(UnitImpl.MICROSECOND);
        TimeInterval newInt = selRange.getInterval();
        double currentInterval = timeInt.getValue();
        double shift = (newBegin.getMicroSecondTime() - currentBegin.getMicroSecondTime())/currentInterval;
        double scale = newInt.getValue()/currentInterval;
        tc.shaleTime(shift, scale);
    }
    
    public MicroSecondDate getBegin() {
        return latestTime.getTime().getBeginTime();
    }
    
    public void setBegin(MicroSecondDate newBegin){
        if ( latestTime.getTime().getEndTime().equals(newBegin)) {
            throw new IllegalArgumentException("Selection must not have zero width, newBegin and end are the same.");
        } // end of if ()
        MicroSecondDate currentBegin = latestTime.getTime().getBeginTime();
        TimeInterval timeInt = (TimeInterval)latestTime.getTime().getInterval().convertTo(UnitImpl.MICROSECOND);
        double currentInterval = timeInt.getValue();
        double shift = (newBegin.getMicroSecondTime() - currentBegin.getMicroSecondTime())/currentInterval;
        double scale = (currentInterval + currentBegin.subtract(newBegin).getValue())/currentInterval;
        tc.shaleTime(shift, scale);
    }
    
    public MicroSecondDate getEnd() {
        return latestTime.getTime().getEndTime();
    }
    
    public void setEnd(MicroSecondDate newEnd){
        if ( latestTime.getTime().getBeginTime().equals(newEnd)) {
            throw new IllegalArgumentException("Selection must not have zero width, begin and newEnd are the same.");
        } // end of if ()
        //logger.debug("setEnd "+newEnd);
        
        MicroSecondDate currentEnd = latestTime.getTime().getEndTime();
        TimeInterval timeInt = (TimeInterval)latestTime.getTime().getInterval().convertTo(UnitImpl.MICROSECOND);
        double currentInterval = timeInt.getValue();
        double scale = (currentInterval + newEnd.subtract(currentEnd).getValue())/currentInterval;
        tc.shaleTime(0, scale);
    }
    
    private void setInterval(TimeInterval newInterval){
        double currentInterval = latestTime.getTime().getInterval().getValue();
        double scale = newInterval.getValue()/currentInterval;
        tc.shaleTime(0, scale);
    }
    
    public void setTimeConfig(TimeConfig config){
        tc.removeListener(this);
        tc.remove(seismos);
        tc = config;
        tc.addListener(this);
        tc.add(seismos);
    }
    
    public TimeConfig getTimeConfig(){ return tc; }
    
    private SeismogramDisplay parent, child;
    
    private TimeConfig tc = new BasicTimeConfig();
    
    private DataSetSeismogram[] seismos;
    
    private Color color;
    
    private boolean selectedBegin, visible = true;
    
    private TimeEvent latestTime;
    
    private static Category logger = Category.getInstance(Selection.class.getName());
}// Selection
