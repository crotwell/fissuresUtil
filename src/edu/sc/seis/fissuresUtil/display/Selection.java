package edu.sc.seis.fissuresUtil.display;


import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import java.awt.Color;

/**
 * Selection.java
 *
 *
 * Created: Thu Jun 20 15:14:47 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class Selection implements TimeSyncListener{
    public Selection (MicroSecondDate begin, MicroSecondDate end, TimeConfigRegistrar tr, LinkedList seismograms, 
		      BasicSeismogramDisplay parent, Color color){
	this.externalTimeConfig = tr;
	this.seismograms = seismograms;
	parents.add(parent);
	this.color = color;
	internalTimeConfig = new TimeConfigRegistrar();
	internalTimeConfig.set(begin, begin.difference(end));
	internalTimeConfig.addTimeSyncListener(this);
	parent.repaint();
    }

    public boolean isVisible(){
	MicroSecondTimeRange currentExternal = externalTimeConfig.getTimeRange();
	MicroSecondTimeRange currentInternal = internalTimeConfig.getTimeRange();
	if(currentExternal.getBeginTime().getMicroSecondTime() >= currentInternal.getEndTime().getMicroSecondTime() || 
	   currentExternal.getEndTime().getMicroSecondTime() <= currentInternal.getBeginTime().getMicroSecondTime())
	    return false;
	return true;
    }
    
    public void adjustRange(MicroSecondDate selectionBegin, MicroSecondDate selectionEnd){
	MicroSecondTimeRange currentInternal = internalTimeConfig.getTimeRange();
	double timeWidth = externalTimeConfig.getTimeRange().getInterval().getValue();
	if(released == true){
	    double beginDistance = Math.abs(currentInternal.getBeginTime().getMicroSecondTime() - 
					    selectionEnd.getMicroSecondTime())/timeWidth;
	    double endDistance = Math.abs(currentInternal.getEndTime().getMicroSecondTime() - 
					  selectionBegin.getMicroSecondTime())/timeWidth;
	    if(beginDistance < endDistance)
		selectedBegin = true;
	    else
		selectedBegin = false;
	    released = false;
	}else if(selectionBegin.getMicroSecondTime() > currentInternal.getEndTime().getMicroSecondTime() || 
		 selectionEnd.getMicroSecondTime() < currentInternal.getBeginTime().getMicroSecondTime()){
	    selectedBegin = !selectedBegin;
	}
	if(selectedBegin){
	    System.out.println(selectionBegin + " " + currentInternal.getEndTime());
	    internalTimeConfig.set(selectionBegin, new TimeInterval(selectionBegin, currentInternal.getEndTime()));
	    repaintParents();
	}else{
	    internalTimeConfig.setDisplayInterval(new TimeInterval(currentInternal.getBeginTime(), selectionEnd));
	    repaintParents();
	}
    }
    
    public boolean isRemoveable(){
	if(internalTimeConfig.getTimeRange().getInterval().getValue()/externalTimeConfig.getTimeRange().getInterval().getValue() < .01){
	    return true;
	}
	return false; 
    }

    public void remove(){ 
	if(displays.size() > 0){
	    Iterator e = displays.iterator();
	    while(e.hasNext()){
		((BasicSeismogramDisplay)e.next()).remove(); 
	    }
	}
    }
	
    public boolean borders(MicroSecondDate selectionBegin, MicroSecondDate selectionEnd){
	double timeWidth = externalTimeConfig.getTimeRange().getInterval().getValue();
	MicroSecondTimeRange currentInternal = internalTimeConfig.getTimeRange();
	if(Math.abs(currentInternal.getEndTime().getMicroSecondTime() - selectionBegin.getMicroSecondTime())/timeWidth <.03 ||
	   Math.abs(currentInternal.getBeginTime().getMicroSecondTime() - selectionEnd.getMicroSecondTime())/timeWidth < .03)
	    return true;
	return false;
    }
    
    public void addParent(BasicSeismogramDisplay newParent){ parents.add(newParent); }

    public BasicSeismogramDisplay getParent(){ return (BasicSeismogramDisplay)parents.getFirst(); }

    public LinkedList getParents(){ return parents; }

    public void repaintParents(){ 
	Iterator e = parents.iterator();
	while(e.hasNext()){
	    ((BasicSeismogramDisplay)e.next()).repaint();
	}
    }

    public float getX(int width){
	MicroSecondTimeRange currentExternal = externalTimeConfig.getTimeRange();
	float offset = (internalTimeConfig.getTimeRange().getBeginTime().getMicroSecondTime() - 
			currentExternal.getBeginTime().getMicroSecondTime())/(float)currentExternal.getInterval().getValue();
	return offset * width;	
    }

    public double getWidth(){ 
	MicroSecondTimeRange currentInternal = internalTimeConfig.getTimeRange();
	return ((currentInternal.getEndTime().getMicroSecondTime() - currentInternal.getBeginTime().getMicroSecondTime())/
		externalTimeConfig.getTimeRange().getInterval().getValue());
    }

    public Color getColor(){ return color; }

    public void setColor(Color color){ this.color = color; }

    public LinkedList getSeismograms(){ return seismograms; }

    public void addDisplay(BasicSeismogramDisplay display){ displays.add(display); }

    public void release(){ released = true; }

    public TimeConfigRegistrar getInternalConfig(){ return internalTimeConfig; }

    public void updateTimeRange(){ repaintParents(); }

    protected LinkedList  displays = new LinkedList();

    protected LinkedList parents = new LinkedList();

    protected TimeConfigRegistrar externalTimeConfig, internalTimeConfig;
    
    protected LinkedList seismograms;

    protected Color color;

    protected boolean selectedBegin, released = true;
}// Selection
