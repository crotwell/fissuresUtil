package edu.sc.seis.fissuresUtil.display;


import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
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
    public Selection (MicroSecondDate begin, MicroSecondDate end, TimeRangeConfig tr){
	this.begin = begin;
	this.end = end;
	this.timeConfig = tr;
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
		return true;
	    }else{
		this.end = selectionEnd;
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

    public void setBegin(MicroSecondDate newBegin){ this.begin = newBegin; } 
    
    public void setEnd(MicroSecondDate newEnd){ this.end = newEnd; }
    
    protected MicroSecondDate begin, end;

    protected TimeRangeConfig timeConfig;
}// Selection
