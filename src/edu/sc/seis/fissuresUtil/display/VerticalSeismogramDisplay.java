package edu.sc.seis.fissuresUtil.display;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Calendar;
import java.util.TimeZone;
import java.io.IOException;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.freq.ButterworthFilter;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.text.SimpleDateFormat;

/**
 * VerticalSeismogramDisplay.java
 *
 *
 * Created: Tue Jun  4 10:52:23 2002
 *
 * @author Charlie Groves
 * @version 0.1
 */

public class VerticalSeismogramDisplay extends JScrollPane{
    
    public VerticalSeismogramDisplay(MouseForwarder mouseForwarder, MouseMotionForwarder motionForwarder){
	this.mouseForwarder = mouseForwarder;
	this.motionForwarder = motionForwarder;
	seismograms = new JLayeredPane();
	seismograms.setLayout(new BoxLayout(seismograms, BoxLayout.Y_AXIS));
	this.getViewport().add(seismograms);
    }
    
    public void addDisplay(LocalSeismogram seis, String name){
	this.addDisplay((LocalSeismogramImpl)seis, name);
    }
    
    public void addDisplay(LocalSeismogramImpl seis, String name){
	if(basicDisplays.size() > 0)
	    this.addDisplay(seis,((BasicSeismogramDisplay)basicDisplays.getFirst()).getTimeRegistrar(), 
			new AmpConfigRegistrar(), name);
	else{	    
	    //ar.visibleAmpCalc(tr);
	    this.addDisplay(seis, new TimeConfigRegistrar(), new AmpConfigRegistrar(), name);
	    }	    
    }
    
    public BasicSeismogramDisplay addDisplay(LocalSeismogramImpl seis, TimeConfigRegistrar tr, AmpConfigRegistrar ar, String name){
	BasicSeismogramDisplay disp = new BasicSeismogramDisplay((LocalSeismogram)seis, tr,
								 ar, true, name, this);
	seismograms.add(disp);
	disp.addMouseMotionListener(motionForwarder);
	disp.addMouseListener(mouseForwarder);
	if(basicDisplays.size() > 0){
	    ((SeismogramDisplay)basicDisplays.getLast()).removeBottomTimeBorder();
	    ((SeismogramDisplay)basicDisplays.getFirst()).addTopTimeBorder();
	}
	basicDisplays.add(disp);
	disp.revalidate();
	return disp;
    }

    public void addSeismogram(LocalSeismogramImpl seis, int index){
	((SeismogramDisplay)basicDisplays.get(index)).addSeismogram((LocalSeismogram)seis);
    }

    public LinkedList getDisplays(){ return basicDisplays; }
    
    public void redraw(){
	Iterator e = basicDisplays.iterator();
	while(e.hasNext())
	    ((SeismogramDisplay)e.next()).redraw();
    }

    public void stopImageCreation(){
	Iterator e = basicDisplays.iterator();
	while(e.hasNext())
	    ((BasicSeismogramDisplay)e.next()).stopImageCreation();
    }

    public void removeAll(){
	seismograms.removeAll();
	remove(seismograms);
	basicDisplays = new LinkedList();
	repaint();
    }

    public void removeSeismogram(MouseEvent me){
	BasicSeismogramDisplay clicked = ((BasicSeismogramDisplay)me.getComponent());
	clicked.remove(me);
	seismograms.remove(clicked);
	basicDisplays.remove(clicked);
	((SeismogramDisplay)basicDisplays.getFirst()).addTopTimeBorder();
	((SeismogramDisplay)basicDisplays.getLast()).addBottomTimeBorder();
    }

    public void removeDisplay(BasicSeismogramDisplay display){
	seismograms.remove(display);
	basicDisplays.remove(display);
	((SeismogramDisplay)basicDisplays.getFirst()).addTopTimeBorder();
	((SeismogramDisplay)basicDisplays.getLast()).addBottomTimeBorder();
	seismograms.revalidate();
    }
    
    
    public void setLabels(MicroSecondDate time, double amp){
	calendar.setTime(time);
	if(output.format(calendar.getTime()).length() == 21)
	    this.time.setText("   Time: " + output.format(calendar.getTime()) + "00");
	else if(output.format(calendar.getTime()).length() == 22)
	    this.time.setText("   Time: " + output.format(calendar.getTime()) + "0");
	else
	    this.time.setText("   Time: " + output.format(calendar.getTime()));
	if(amp < 0)
	    if(Math.abs(amp) < 10)
		this.amp.setText("   Amplitude:-000" + Math.abs(Math.round(amp)));
	    else if(Math.abs(amp) < 100)
		this.amp.setText("   Amplitude:-00" + Math.abs(Math.round(amp)));
	    else if(Math.abs(amp) < 1000)
		this.amp.setText("   Amplitude:-0" + Math.abs(Math.round(amp)));
	    else
		this.amp.setText("   Amplitude:-" + Math.abs(Math.round(amp)));
	else
	    if(Math.abs(amp) < 10)
		this.amp.setText("   Amplitude:  000" + Math.round(amp));
	    else if(Math.abs(amp) < 100)
		this.amp.setText("   Amplitude:  00" + Math.round(amp));
	    else if(Math.abs(amp) < 1000)
		this.amp.setText("   Amplitude:  0" + Math.round(amp));
	    else
		this.amp.setText("   Amplitude:  " + Math.round(amp));
    }
    
   
    
    public void removeAllDisplays(){
	this.removeAll();
	this.time.setText("   Time: ");
	this.amp.setText("   Amplitude: ");
	repaint();
     }

    public void toggleUnfilteredDisplay(){
	Iterator e = basicDisplays.iterator();
	while(e.hasNext())
	    ((BasicSeismogramDisplay)e.next()).toggleUnfilteredDisplay();
    }

    public void applyFilter(ButterworthFilter filter, boolean visible, LinkedList currentFilters){
	this.currentFilters = currentFilters;
	Iterator e = basicDisplays.iterator();
	while(e.hasNext())
	    ((BasicSeismogramDisplay)e.next()).setFilter(filter, visible);
    }

    public LinkedList getCurrentFilters(){ return currentFilters; }

    protected HashMap selectionDisplayMap = new HashMap();

    protected LocalSeismogramImpl waitingSeismo;

    protected ParticleMotionDisplay particleDisplay;
    
    protected LinkedList currentFilters = new LinkedList();

    protected LinkedList basicDisplays = new LinkedList();

    protected MouseForwarder mouseForwarder;

    protected MouseMotionForwarder motionForwarder;

    protected JComponent seismograms;

    public JLabel time = new JLabel("   Time:                        ");
    
    public JLabel amp = new JLabel("   Amplitude:       ");

    protected SimpleDateFormat output = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.S");

    protected Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
}// VerticalSeismogramDisplay
