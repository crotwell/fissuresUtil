package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.geom.*;

/**
 * Selection.java
 *
 *
 * Created: Thu Jun 20 15:14:47 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class Selection implements TimeListener, Plotter{
    public Selection (MicroSecondDate begin, MicroSecondDate end, Registrar reg, DataSetSeismogram[] seismograms, 
		      BasicSeismogramDisplay parent, Color color){
	externalRegistrar = reg;
	seismos = seismograms;
	parents.add(parent);
	this.color = color;
	internalRegistrar = new Registrar(seismos);
	internalRegistrar.addListener(this);
	setBegin(begin);
	setInterval(new TimeInterval(begin, end));
	parent.repaint();
    }

    public void updateTime(TimeEvent event){
	latestTime = event;
	repaintParents();
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
	    Rectangle2D selection = new Rectangle2D.Float(getX(size.width, timeEvent), 0, 
							  (float)(getWidth(timeEvent) * size.width), 
							  size.height);
	    canvas.setPaint(color); 
	    canvas.fill(selection); 
	    canvas.draw(selection); 
	} 
    }
    
    public void adjustRange(MicroSecondDate selectionBegin, MicroSecondDate selectionEnd){
	MicroSecondTimeRange currentInternal = latestTime.getTime();
	double timeWidth = externalRegistrar.getLatestTime().getTime().getInterval().getValue();
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
	    setBegin(selectionBegin);
	    repaintParents();
	}else{
	    repaintParents();
	    setEnd(selectionEnd);
	}
    }
    
    public boolean isRemoveable(){
	if(latestTime.getTime().getInterval().getValue()/
	   externalRegistrar.getLatestTime().getTime().getInterval().getValue() < .01){
	    return true;
	}
	return false; 
    }

    public void removeParent(BasicSeismogramDisplay disowner){ 
	parents.remove(disowner);
	if(parents.isEmpty()){
	    Iterator e = displays.iterator();
	    while(e.hasNext()){
		((BasicSeismogramDisplay)e.next()).remove(); 
	    }
	}
    }    

    public boolean removeChild(BasicSeismogramDisplay child){
	return displays.remove(child);
    }
	
    public boolean borders(MicroSecondDate selectionBegin, MicroSecondDate selectionEnd){
	double timeWidth = externalRegistrar.getLatestTime().getTime().getInterval().getValue();
	MicroSecondTimeRange currentInternal = latestTime.getTime();
	if(Math.abs(currentInternal.getEndTime().getMicroSecondTime() - selectionBegin.getMicroSecondTime())/timeWidth <.03 ||
	   Math.abs(currentInternal.getBeginTime().getMicroSecondTime() - selectionEnd.getMicroSecondTime())/timeWidth < .03)
	    return true;
	return false;
    }
    
    public void addParent(BasicSeismogramDisplay newParent){ 
	if(!parents.contains(newParent))
	    parents.add(newParent);
    }

    public BasicSeismogramDisplay getParent(){ return (BasicSeismogramDisplay)parents.getFirst(); }

    public LinkedList getParents(){ return parents; }

    public void repaintParents(){ 
	Iterator e = parents.iterator();
	while(e.hasNext()){
	    ((BasicSeismogramDisplay)e.next()).repaint();
	}
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

    public void addDisplay(BasicSeismogramDisplay display){ displays.add(display); }

    public void release(){ released = true; }

    public Registrar getInternalRegistrar(){ return internalRegistrar; }

    private void setBegin(MicroSecondDate newBegin){
	MicroSecondDate currentBegin = latestTime.getTime().getBeginTime();
	double currentInterval = latestTime.getTime().getInterval().getValue();
	double shift = (newBegin.getMicroSecondTime() - currentBegin.getMicroSecondTime())/currentInterval;
	double scale = (currentInterval + currentBegin.subtract(newBegin).getValue())/currentInterval;
	internalRegistrar.shaleTime(shift, scale);
    }

    private void setEnd(MicroSecondDate newEnd){
	MicroSecondDate currentEnd = latestTime.getTime().getEndTime();
	double currentInterval = latestTime.getTime().getInterval().getValue();
	double scale = (currentInterval + newEnd.subtract(currentEnd).getValue())/currentInterval;
	internalRegistrar.shaleTime(0, scale);
    }

    private void setInterval(TimeInterval newInterval){
	double currentInterval = latestTime.getTime().getInterval().getValue();
	System.out.println(newInterval);
	double scale = newInterval.getValue()/currentInterval;
	internalRegistrar.shaleTime(0, scale);
    }

    private LinkedList  displays = new LinkedList();

    private LinkedList parents = new LinkedList();

    private Registrar externalRegistrar, internalRegistrar;
    
    private DataSetSeismogram[] seismos;

    private Color color;

    private boolean selectedBegin, released = true, visible = true;

    private TimeEvent latestTime;
}// Selection
