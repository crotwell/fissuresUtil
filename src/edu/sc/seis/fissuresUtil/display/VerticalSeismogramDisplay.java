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
import edu.sc.seis.fissuresUtil.xml.*;
import org.apache.log4j.*;

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
	output.setTimeZone(TimeZone.getTimeZone("GMT"));
	this.mouseForwarder = mouseForwarder;
	this.motionForwarder = motionForwarder;
	seismograms = new JLayeredPane();
	seismograms.setLayout(new BoxLayout(seismograms, BoxLayout.Y_AXIS));
	this.getViewport().add(seismograms);
	globalTimeRegistrar = new TimeConfigRegistrar();
	globalAmpRegistrar = new AmpConfigRegistrar(new RMeanAmpConfig());
	sorter = new AlphaSeisSorter();
    }
    
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram dss, String name){
	return addDisplay(dss, globalTimeRegistrar, name);
    }

    public BasicSeismogramDisplay addDisplay(DataSetSeismogram dss, TimeConfigRegistrar tr, String name){
	if(sorter.contains(name)){
	    return null;
	}
	BasicSeismogramDisplay disp = new BasicSeismogramDisplay(dss, tr, name, this);
	int i = sorter.sort(dss, name);
	seismograms.add(disp, i);
	disp.addMouseMotionListener(motionForwarder);
	disp.addMouseListener(mouseForwarder);
	if(basicDisplays.size() > 0){
	    ((SeismogramDisplay)basicDisplays.getLast()).removeBottomTimeBorder();
	    ((SeismogramDisplay)basicDisplays.getFirst()).removeTopTimeBorder();
	}
	basicDisplays.add(i, disp);
	((SeismogramDisplay)basicDisplays.getLast()).addBottomTimeBorder();
	((SeismogramDisplay)basicDisplays.getFirst()).addTopTimeBorder();
	disp.revalidate();
	return disp;
    }

    public void addSeismogram(DataSetSeismogram seis, int index){
	((BasicSeismogramDisplay)basicDisplays.get(index)).addSeismogram(seis);
    }

    public LinkedList getDisplays(){ return basicDisplays; }
    
    public LinkedList  getAllBasicDisplays(LinkedList target){ 
	target.addAll(basicDisplays);
	if(selectionDisplay != null){
	    selectionDisplay.getAllBasicDisplays(target);
	}
	return target;
    }

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
	logger.debug("removing all displays");
	this.stopImageCreation();
	seismograms.removeAll();
	remove(seismograms);
	basicDisplays = new LinkedList();
	sorter = new AlphaSeisSorter();
	globalTimeRegistrar = new TimeConfigRegistrar();
	globalAmpRegistrar = new AmpConfigRegistrar(new RMeanAmpConfig());
	this.time.setText("   Time: ");
	this.amp.setText("   Amplitude: ");
	if(selectionDisplay != null){
	    selectionDisplay.removeAll();
	    selectionWindow.dispose();
	    selectionDisplays--;
	    selectionDisplay = null;
	}
	if(particleDisplay != null){
	    particleWindow.dispose();
	    particleDisplays--;
	    particleDisplay = null;
	}
	repaint();
    }

    public void removeSeismogram(MouseEvent me){
	BasicSeismogramDisplay clicked = ((BasicSeismogramDisplay)me.getComponent());
	clicked.remove();
	seismograms.remove(clicked);
	basicDisplays.remove(clicked);
	((SeismogramDisplay)basicDisplays.getFirst()).addTopTimeBorder();
	((SeismogramDisplay)basicDisplays.getLast()).addBottomTimeBorder();
    }

    public void removeDisplay(BasicSeismogramDisplay display){
	if(basicDisplays.size() == 1){
	    this.removeAll();
	    return;
	}
	seismograms.remove(display);
	basicDisplays.remove(display);
	sorter.remove(display.getName());
	if(basicDisplays.size() > 1){
	    ((SeismogramDisplay)basicDisplays.getFirst()).addTopTimeBorder();
	    ((SeismogramDisplay)basicDisplays.getLast()).addBottomTimeBorder();
	}else if(basicDisplays.size() == 1){
	    ((SeismogramDisplay)basicDisplays.getLast()).addBottomTimeBorder();
	}
	seismograms.revalidate();
	repaint();
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
		this.amp.setText("   Amplitude: 000" + Math.round(amp));
	    else if(Math.abs(amp) < 100)
		this.amp.setText("   Amplitude: 00" + Math.round(amp));
	    else if(Math.abs(amp) < 1000)
		this.amp.setText("   Amplitude: 0" + Math.round(amp));
	    else
		this.amp.setText("   Amplitude: " + Math.round(amp));
    }

    public void setUnfilteredDisplay(boolean visible){
	Iterator e = basicDisplays.iterator();
	while(e.hasNext())
	    ((BasicSeismogramDisplay)e.next()).setUnfilteredDisplay(visible);
    }

    public void applyFilter(ButterworthFilter filter, boolean visible, LinkedList currentFilters){
	this.currentFilters = currentFilters;
	Iterator e = basicDisplays.iterator();
	while(e.hasNext())
	    ((BasicSeismogramDisplay)e.next()).setFilter(filter, visible);
    }

    public LinkedList getCurrentFilters(){ return currentFilters; }

    public void globalizeAmpRange(){
	Iterator e = basicDisplays.iterator();
	while(e.hasNext())
	    ((BasicSeismogramDisplay)e.next()).getAmpRegistrar().setRegistrar(globalAmpRegistrar);
    }
    
    public void createParticleDisplay(BasicSeismogramDisplay creator){
	if(particleDisplay == null){
	    logger.debug("creating particle display");
	    particleWindow = new JFrame();
	    LocalSeismogramImpl seis = (((LocalSeismogramImpl)((DataSetSeismogram)creator.getSeismograms().getFirst()).getSeismogram()));
	    particleDisplay = new ParticleMotionDisplay(seis, creator.getTimeRegistrar(), creator.getAmpRegistrar(), 
							creator.getAmpRegistrar(), 
							((DataSetSeismogram)creator.getSeismograms().getFirst()).getDataSet());
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
	java.awt.Dimension size = new java.awt.Dimension(400, 400);
	displayPanel.setSize(size);
	particleWindow.getContentPane().add(displayPanel);
	//particleWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
	Toolkit tk = Toolkit.getDefaultToolkit();
	if(400*particleDisplays < tk.getScreenSize().width){
	    particleWindow.setLocation(400 * particleDisplays, tk.getScreenSize().height - 400);
	}else{
	    particleWindow.setLocation(tk.getScreenSize().width - 400, tk.getScreenSize().height - 400);
	}
	particleDisplays++;
	particleWindow.setVisible(true);
	}
    }

    public void createSelectionDisplay(BasicSeismogramDisplay creator){
	if(selectionDisplay == null){
	    logger.debug("creating selection display");
	    selectionWindow = new JFrame();
	    //selectionWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    selectionWindow.setSize(400, 220);
	    JToolBar infoBar = new JToolBar();
	    infoBar.add(new FilterSelection(selectionDisplay));
	    infoBar.setFloatable(false);
	    selectionWindow.getContentPane().add(infoBar, BorderLayout.SOUTH);
	    Iterator e = creator.getSeismograms().iterator();
	    TimeConfigRegistrar tr = creator.getCurrentSelection().getInternalConfig();
	    DataSetSeismogram first = ((DataSetSeismogram)e.next());
	    AmpConfigRegistrar ar = new AmpConfigRegistrar(new OffsetMeanAmpConfig(((LocalSeismogramImpl)first.getSeismogram()), 
										   tr.getTimeRange(first.getSeismogram())));
	    ar.visibleAmpCalc(tr);
	    selectionDisplay = new VerticalSeismogramDisplay(mouseForwarder, motionForwarder);
	    creator.getCurrentSelection().setDisplay(selectionDisplay.addDisplay(first, tr, creator.getName() + "." +
										 creator.getCurrentSelection().getColor()));
	    while(e.hasNext()){
		selectionDisplay.addSeismogram(((DataSetSeismogram)e.next()), 0);
	    }
	    selectionWindow.getContentPane().add(selectionDisplay);
	    Toolkit tk = Toolkit.getDefaultToolkit();
	    if((selectionDisplays + 1) * 220 < tk.getScreenSize().height){
		selectionWindow.setLocation(tk.getScreenSize().width - 400, tk.getScreenSize().height - (selectionDisplays + 1) * 220);
	    }else{
		selectionWindow.setLocation(tk.getScreenSize().width - 400, 0);
	    }
	    selectionDisplays++;
	    selectionWindow.setVisible(true);	
	}else{
	    logger.debug("adding another selection");
	    Iterator e = creator.getSeismograms().iterator();
	    TimeConfigRegistrar tr = creator.getCurrentSelection().getInternalConfig();
	    DataSetSeismogram first = ((DataSetSeismogram)e.next());
	    AmpConfigRegistrar ar = new AmpConfigRegistrar(new OffsetMeanAmpConfig(((LocalSeismogramImpl)first.getSeismogram()),
										   tr.getTimeRange(first.getSeismogram())));
	    ar.visibleAmpCalc(tr);
	    creator.getCurrentSelection().setDisplay(selectionDisplay.addDisplay(first, tr, creator.getName() + "." +  
										 creator.getCurrentSelection().getColor()));
	    while(e.hasNext()){
		selectionDisplay.addSeismogram(((DataSetSeismogram)e.next()), 0);
	    }
	}
    }
	
    
    protected static int particleDisplays = 0, selectionDisplays = 0;
    
    protected JFrame selectionWindow, particleWindow;

    protected SeismogramSorter sorter;

    protected TimeConfigRegistrar globalTimeRegistrar;

    protected AmpConfigRegistrar globalAmpRegistrar;
    
    protected HashMap selectionDisplayMap = new HashMap();

    protected LocalSeismogramImpl waitingSeismo;

    protected ParticleMotionDisplay particleDisplay;
    
    protected LinkedList currentFilters = new LinkedList();

    protected LinkedList basicDisplays = new LinkedList();

    protected LinkedList names = new LinkedList();

    protected MouseForwarder mouseForwarder;

    protected MouseMotionForwarder motionForwarder;

    protected JComponent seismograms;

    public static JLabel time = new JLabel("   Time:                        ");
    
    public static JLabel amp = new JLabel("   Amplitude:       ");

    protected SimpleDateFormat output = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.S");

    protected Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

    protected VerticalSeismogramDisplay selectionDisplay;

    private static Category logger = Category.getInstance(VerticalSeismogramDisplay.class.getName());
}// VerticalSeismogramDisplay
