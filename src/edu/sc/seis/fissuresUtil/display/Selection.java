package edu.sc.seis.fissuresUtil.display;


import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import java.util.HashMap;
import java.util.Iterator;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

/**
 * Selection.java
 *
 *
 * Created: Thu Jun 20 15:14:47 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class Selection {
    public Selection (MicroSecondDate begin, MicroSecondDate end, TimeRangeConfig tr, HashMap plotters){
	this.begin = begin;
	this.end = end;
	this.timeConfig = tr;
	this.plotters = plotters;
	internalTimeConfig = new BoundedTimeConfig();
	Iterator e = plotters.keySet().iterator();
	while(e.hasNext())
	    internalTimeConfig.addSeismogram(((SeismogramPlotter)e.next()).getSeismogram(), begin);
	internalTimeConfig.setDisplayInterval(begin.difference(end));
    }

    public boolean isVisible(){
	MicroSecondTimeRange current = timeConfig.getTimeRange();
	if(current.getBeginTime().getMicroSecondTime() >= end.getMicroSecondTime() || 
	   current.getEndTime().getMicroSecondTime() <= begin.getMicroSecondTime())
	    return false;
	return true;
    }
    
    public boolean adjustRange(MicroSecondDate selectionBegin, MicroSecondDate selectionEnd){
	double timeWidth = timeConfig.getTimeRange().getInterval().getValue();
	double beginDistance = Math.abs(begin.getMicroSecondTime() - selectionEnd.getMicroSecondTime())/timeWidth;
	double endDistance = Math.abs(end.getMicroSecondTime() - selectionBegin.getMicroSecondTime())/timeWidth;
	if(beginDistance < .05 || endDistance < .05){
	    if(beginDistance < endDistance){
		this.begin = selectionBegin;
		internalTimeConfig.setAllBeginTime(selectionBegin);
		internalTimeConfig.setDisplayInterval(end.difference(begin));
		return true;
	    }else{
		this.end = selectionEnd;
		internalTimeConfig.setDisplayInterval(end.difference(begin));
		return true;
	    }
	}
	return false;
    }

    public boolean remove(){
	if((end.getMicroSecondTime() - begin.getMicroSecondTime())/timeConfig.getTimeRange().getInterval().getValue() < .01)
	    return true;
	return false; 
    }
	
    public boolean borders(MicroSecondDate selectionBegin, MicroSecondDate selectionEnd){
	double timeWidth = timeConfig.getTimeRange().getInterval().getValue();
	if(Math.abs(end.getMicroSecondTime() - selectionBegin.getMicroSecondTime())/timeWidth <.03 ||
	   Math.abs(begin.getMicroSecondTime() - selectionEnd.getMicroSecondTime())/timeWidth < .03)
	    return true;
	return false;
    }
    
    public float getX(int width){
	MicroSecondTimeRange current = timeConfig.getTimeRange();
	float offset = (begin.getMicroSecondTime() - current.getBeginTime().getMicroSecondTime())/
	    (float)current.getInterval().getValue();
	return offset * width;	
    }

    public double getWidth(){ 
	return ((end.getMicroSecondTime() - begin.getMicroSecondTime())/timeConfig.getTimeRange().getInterval().getValue());
    }

    public MicroSecondDate getBegin(){ return begin; } 

    public MicroSecondDate getEnd(){ return end; }

    public void setBegin(MicroSecondDate newBegin){ 
	internalTimeConfig.setBeginTime(newBegin);
	this.begin = newBegin; } 
    
    public void setEnd(MicroSecondDate newEnd){ 
	internalTimeConfig.setDisplayInterval(newEnd.difference(begin));
	this.end = newEnd; 
    }

    public LocalSeismogramImpl getSeismogram(){ 
	Iterator e = plotters.keySet().iterator();
	return ((LocalSeismogramImpl)((SeismogramPlotter)e.next()).getSeismogram());
    }

    public TimeRangeConfig getInternalConfig(){ 
	System.out.println(internalTimeConfig.getTimeRange().getBeginTime());
	return internalTimeConfig; }

    protected MicroSecondDate begin, end;

    protected TimeRangeConfig timeConfig, internalTimeConfig;

    protected HashMap plotters;
}// Selection
