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
	    this.addDisplay(seis,((SeismogramDisplay)basicDisplays.getFirst()).getTimeConfig(), 
			((SeismogramDisplay)basicDisplays.getFirst()).getAmpConfig(), name);
	else{	    
	    AmpRangeConfig ar = new RMeanAmpConfig();
	    TimeRangeConfig tr = new BoundedTimeConfig();
	    //ar.visibleAmpCalc(tr);
	    this.addDisplay(seis, tr, ar, name);
	}	    
    }
    
    public BasicSeismogramDisplay addDisplay(LocalSeismogramImpl seis, TimeRangeConfig tr, AmpRangeConfig ar, String name){
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
	clicked.removeAllSeismograms();
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
	this.time.setText("   Time: " + output.format(calendar.getTime()));
	if(amp < 0)
	    if(Math.abs(amp) < 10)
		this.amp.setText("   Amplitude: -000" + Math.abs(Math.round(amp)));
	    else if(Math.abs(amp) < 100)
		this.amp.setText("   Amplitude: -00" + Math.abs(Math.round(amp)));
	    else if(Math.abs(amp) < 1000)
		this.amp.setText("   Amplitude: -0" + Math.abs(Math.round(amp)));
	    else
		this.amp.setText("   Amplitude: -" + Math.abs(Math.round(amp)));
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
    
   public void selectionUpdateFinished(BasicSeismogramDisplay display, Selection selection){
       if(!selectionDisplayMap.containsKey(selection)){
	    if(selectionDisplay == null){
		makeSelectionWindow(selection);
	    }else{
		LinkedList seis = selection.getSeismograms();
		TimeRangeConfig tr = selection.getInternalConfig();
		LocalSeismogramImpl firstSeis = ((LocalSeismogramImpl)seis.getFirst());
		AmpRangeConfig ar =  new OffsetMeanAmpConfig(firstSeis, tr.getTimeRange((LocalSeismogram)firstSeis));
		ar.visibleAmpCalc(tr); 
		selectionDisplayMap.put(selection, selectionDisplay.addDisplay(firstSeis, tr, ar, firstSeis.getName()));
		Iterator e = seis.iterator();
		e.next();
		while(e.hasNext())
		    selectionDisplay.addSeismogram(((LocalSeismogramImpl)e.next()), selectionDisplayMap.size() - 1);
		int numToDo, i;
		if(particleDisplay != null){
		    Color selectionColor = selection.getColor();
		    Color newColor = new Color(selectionColor.getRed(), selectionColor.getGreen(), selectionColor.getBlue());
		    if(waitingSeismo != null){
			numToDo = (seis.size() + 1)/2;
			i = 1;
			particleDisplay.addParticleMotionDisplay(((LocalSeismogramImpl)seis.get(0)), waitingSeismo, tr, ar, ar, 
								 newColor);
			if(seis.size() % 2 == 0)
			    waitingSeismo = ((LocalSeismogramImpl)seis.getLast());
		    }else{
			i = 0;
			numToDo = seis.size()/2;
			if(seis.size() % 2 == 1)
			    waitingSeismo = ((LocalSeismogramImpl)seis.getLast());
		    }
		    for(int j = 7; i < numToDo; i++)
			particleDisplay.addParticleMotionDisplay(((LocalSeismogramImpl)seis.get(i)), 
								 ((LocalSeismogramImpl)seis.get(i + 1)), tr, ar, ar, 
								 newColor);
		}else
		    makeParticleWindow(seis, tr, ar, selection);
	    }
       }
       releaseCurrentSelection(display);
   }

    public void releaseCurrentSelection(BasicSeismogramDisplay released){
	Selection removed = released.releaseCurrentSelection();
	if(removed != null){
	    selectionDisplay.removeDisplay((BasicSeismogramDisplay)selectionDisplayMap.get(removed));
	    selectionDisplayMap.remove(removed);
	}
    }
    
    public void makeSelectionWindow(Selection selection){
	LinkedList seismos = selection.getSeismograms();
	LocalSeismogramImpl seis = ((LocalSeismogramImpl)seismos.getFirst());
	TimeRangeConfig tr = selection.getInternalConfig();
	AmpRangeConfig ar =  new OffsetMeanAmpConfig(seis, tr.getTimeRange((LocalSeismogram)seis));
	ar.visibleAmpCalc(tr);
	String name = seis.getName();
	JFrame selectionWindow = new JFrame("selection Display");
	selectionWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	selectionWindow.setSize(400, 200);
	selectionDisplay = new VerticalSeismogramDisplay(mouseForwarder, motionForwarder);
	selectionDisplayMap.put(selection, selectionDisplay.addDisplay(seis, tr, ar, name));
	JToolBar infoBar = new JToolBar();
	infoBar.add(selectionDisplay.amp);
	infoBar.add(selectionDisplay.time);
	infoBar.add(new FilterSelection(selectionDisplay));
	infoBar.setFloatable(false);
	selectionWindow.getContentPane().add(infoBar, BorderLayout.SOUTH);
	Iterator e = seismos.iterator();
	e.next();
	while(e.hasNext())
	    selectionDisplay.addSeismogram(((LocalSeismogramImpl)e.next()), 0);
	selectionWindow.getContentPane().add(selectionDisplay);
	selectionWindow.setLocation(getSize().width, getSize().height);
	selectionWindow.setVisible(true);
	if(seismos.size() >= 2)
	    makeParticleWindow(seismos, tr, ar, selection);
	else
	    waitingSeismo = ((LocalSeismogramImpl)seismos.getFirst());
    }

    public void makeParticleWindow(LinkedList seismos, TimeRangeConfig tr, AmpRangeConfig ar, Selection selection){
	int numToDo = seismos.size()/2;
	JFrame particleWindow = new JFrame("Particle Display");
	Color selectionColor = selection.getColor();
	Color newColor = new Color(selectionColor.getRed(), selectionColor.getGreen(), selectionColor.getBlue());
	if(waitingSeismo == null && seismos.size() >= 2){
	    for(int i = 0; i < numToDo; i++)
		particleDisplay = new ParticleMotionDisplay(((LocalSeismogramImpl)seismos.get(i)), 
											     ((LocalSeismogramImpl)seismos.get(i + 1)), 
											     tr, ar, ar, newColor);	    
	
	    if(seismos.size()%2 == 1)
		waitingSeismo = ((LocalSeismogramImpl)seismos.getLast());
	}else if(waitingSeismo != null){
	    particleDisplay = new ParticleMotionDisplay(waitingSeismo,
											 ((LocalSeismogramImpl)seismos.get(0)), tr, ar,
											 ar, newColor);
	    for(int i = 1; i < numToDo; i++)
		particleDisplay = new ParticleMotionDisplay(((LocalSeismogramImpl)seismos.get(i)), 
											     ((LocalSeismogramImpl)seismos.get(i + 1)), 
											     tr, ar, ar, newColor);
	    if(seismos.size()%2 == 0)
		waitingSeismo = ((LocalSeismogramImpl)seismos.getLast());
	    else
		waitingSeismo = null;
	}else{
	    waitingSeismo = ((LocalSeismogramImpl)seismos.getFirst());
	    return;
	}	    
	particleDisplay.addAzimuthLine(15);
	particleDisplay.addSector(10, 20);
	JPanel displayPanel = new JPanel();
	JButton zoomIn = new JButton("zoomIn");
	JButton zoomOut = new JButton("zoomOut");
	JPanel buttonPanel = new JPanel();
	buttonPanel.setLayout(new FlowLayout());
	buttonPanel.add(zoomIn);
	buttonPanel.add(zoomOut);
	displayPanel.setLayout(new BorderLayout());
	displayPanel.add(particleDisplay, java.awt.BorderLayout.CENTER);
	displayPanel.add(buttonPanel, java.awt.BorderLayout.SOUTH);
	Dimension size = new Dimension(400, 400);
	displayPanel.setSize(size);
	particleWindow.getContentPane().add(displayPanel);
	particleWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	particleWindow.setSize(size);
	zoomIn.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {

		    particleDisplay.setZoomIn(true);
		    // particleDisplay.setZoomOut(false);
		}
	    });
	zoomOut.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {

		    particleDisplay.setZoomOut(true);
		    // particleDisplay.setZoomIn(false);
		}
	    });
	particleWindow.setLocation(0, getSize().height + 200);
	particleWindow.setVisible(true);
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

    protected VerticalSeismogramDisplay selectionDisplay;
    
    protected ParticleMotionDisplay particleDisplay;
    
    protected LinkedList currentFilters = new LinkedList();

    protected LinkedList basicDisplays = new LinkedList();

    protected MouseForwarder mouseForwarder;

    protected MouseMotionForwarder motionForwarder;

    protected JComponent seismograms;

    public JLabel time = new JLabel("   Time: ");
    
    public JLabel amp = new JLabel("   Amplitude:       ");

    protected SimpleDateFormat output = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.S");

    protected Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

    public static void main(String[] args){
	try{
	    JFrame jf = new JFrame("Test Seismogram View");
	    LocalSeismogram test1 = SeisPlotUtil.createSineWave();
	    LocalSeismogram test2 = SeisPlotUtil.createTestData();
	    LocalSeismogram test3 = SeisPlotUtil.createSineWave();
	    LocalSeismogram test4 = SeisPlotUtil.createTestData();
	    VerticalSeismogramDisplay sv = new VerticalSeismogramDisplay(new MouseForwarder(), new MouseMotionForwarder());
	    sv.addDisplay((LocalSeismogramImpl)test1, new BoundedTimeConfig(), new RMeanAmpConfig(), "");
	    sv.addDisplay((LocalSeismogramImpl)test2, "");
	    sv.addDisplay((LocalSeismogramImpl)test3, "");
	    sv.addDisplay((LocalSeismogramImpl)test4, "");
	    sv.addSeismogram((LocalSeismogramImpl)test2, 0);
	    Dimension size = new Dimension(400, 400);
	    sv.setPreferredSize(size);
	    jf.getContentPane().add(sv);
	    jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    jf.setSize(size);
	    jf.setVisible(true);
	    jf.addWindowListener(new WindowAdapter() {
		    public void windowClosed(WindowEvent e) {
			System.exit(0);
		    }
		});
	}
	catch(Exception e){ e.printStackTrace(); }
    }
}// VerticalSeismogramDisplay
