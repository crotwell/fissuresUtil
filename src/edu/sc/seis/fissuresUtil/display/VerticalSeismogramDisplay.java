package edu.sc.seis.fissuresUtil.display;

import java.util.*;
import java.io.IOException;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.freq.ColoredFilter;
import edu.sc.seis.fissuresUtil.chooser.ChannelGrouperImpl;
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
	this(mouseForwarder, motionForwarder, null);
    }

    public VerticalSeismogramDisplay(MouseForwarder mouseForwarder, MouseMotionForwarder motionForwarder, 
				     VerticalSeismogramDisplay parent){
	output.setTimeZone(TimeZone.getTimeZone("GMT"));
	this.mouseForwarder = mouseForwarder;
	this.motionForwarder = motionForwarder;
	seismograms = new JLayeredPane();
	seismograms.setLayout(new BoxLayout(seismograms, BoxLayout.Y_AXIS));
	this.getViewport().add(seismograms);
	globalTimeRegistrar = new TimeConfigRegistrar();
	globalAmpRegistrar = new AmpConfigRegistrar(new RMeanAmpConfig());
	sorter = new AlphaSeisSorter();
	if(parent != null){
	    this.originalVisible = parent.getOriginalVisibility();
	    this.parent = parent;
	}else{
	    this.originalVisible = true;
	}
    }
    
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram dss, String name){
	return addDisplay(dss, globalTimeRegistrar, new AmpConfigRegistrar(),  name);
    }

    public BasicSeismogramDisplay addDisplay(DataSetSeismogram dss, TimeConfigRegistrar tr, String name){
	return addDisplay(dss, tr, new AmpConfigRegistrar(), name);
    }

    public BasicSeismogramDisplay addDisplay(DataSetSeismogram dss, AmpConfigRegistrar ar, String name){
	return addDisplay(dss, globalTimeRegistrar, ar, name);
    }
    
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram dss, TimeConfigRegistrar tr, AmpConfigRegistrar ar, String name){
	if(sorter.contains(name)){
	    return null;
	}
	BasicSeismogramDisplay disp = new BasicSeismogramDisplay(dss, (TimeRangeConfig)tr, name, this);
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
    
    public BasicSeismogramDisplay[] getComponentGroup(BasicSeismogramDisplay creator){
	BasicSeismogramDisplay[] groupDisplays = new BasicSeismogramDisplay[2];
	int i = 0;
	Iterator e = creator.getSeismograms().iterator();
	DataSetSeismogram first = ((DataSetSeismogram)creator.getSeismograms().getFirst());
	LocalSeismogramImpl seis = first.getSeismogram();
	XMLDataSet dataSet = (XMLDataSet)first.getDataSet();
	ChannelId[] channelIds = dataSet.getChannelIds();
	ChannelGrouperImpl channelProxy = new ChannelGrouperImpl();
	ChannelId[] channelGroup = channelProxy.retrieve_grouping(channelIds, seis.getChannelID());
	DataSetSeismogram[] seismograms = new DataSetSeismogram[3];
	try{
	    for(int counter = 0; counter < channelGroup.length; counter++) {
		String name = DisplayUtils.getSeismogramName(channelGroup[counter], dataSet, 
						   new edu.iris.Fissures.TimeRange(seis.getBeginTime().getFissuresTime(), 
										   seis.getEndTime().getFissuresTime()));
		seismograms[counter] = new DataSetSeismogram(dataSet.getSeismogram(name), dataSet);
		if(seismograms[counter] != null && seis != seismograms[counter].getSeismogram()){
		    int j = i;
		    Iterator g = basicDisplays.iterator();
		    while(g.hasNext() && j == i){
			BasicSeismogramDisplay current = ((BasicSeismogramDisplay)g.next());
			Iterator h = current.getSeismograms().iterator();
			while(h.hasNext()&& j == i){
			    if(((DataSetSeismogram)h.next()).getSeismogram() == seismograms[counter].getSeismogram()){
				groupDisplays[i] = current;
				i++;
			    }
			}
		    }
		}
	    }
	}catch(Exception f){
	    f.printStackTrace();
	}
	return groupDisplays;
    }

    public LinkedList  getAllBasicDisplays(LinkedList target){ 
	target.addAll(basicDisplays);
	if(selectionDisplay != null){
	    selectionDisplay.getAllBasicDisplays(target);
	}
	if(threeSelectionDisplay != null){
	    threeSelectionDisplay.getAllBasicDisplays(target);
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

    public void removeSelectionDisplay(VerticalSeismogramDisplay display){
	if(display == selectionDisplay){
	    removeSelectionDisplay();
	}else{
	    remove3CSelectionDisplay();
	}
    }
    
    public void removeSelectionDisplay(){
	if(selectionDisplay != null){
	    Iterator e = basicDisplays.iterator();
	    while(e.hasNext()){
		((BasicSeismogramDisplay)e.next()).clearRegSelections();
	    }
	    selectionWindow.dispose();
	    //selectionDisplays -= selectionWindow.getSize().height;
	    selectionDisplay = null;
	}
    }
    public void remove3CSelectionDisplay(){
	if(threeSelectionDisplay != null){
	    Iterator e = basicDisplays.iterator();
	    while(e.hasNext()){
		((BasicSeismogramDisplay)e.next()).clear3CSelections();
	    }
	    threeSelectionWindow.dispose();
	    //selectionDisplays -= threeSelectionWindow.getSize().height;
	    threeSelectionDisplay = null;
	}
    }

    public void removeAll(){
	logger.debug("removing all displays");
	if(parent != null){
	    parent.removeSelectionDisplay(this);
	}
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
	    //selectionDisplays -= selectionWindow.getSize().height;
	    selectionDisplay = null;
	}
	if(threeSelectionDisplay != null){
	    threeSelectionDisplay.removeAll();
	    threeSelectionWindow.dispose();
	    //selectionDisplays -= threeSelectionWindow.getSize().height;
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

    public void setOriginalDisplay(boolean visible){
	if(selectionDisplay != null){
	    selectionDisplay.setOriginalDisplay(visible);
	}
	if(threeSelectionDisplay != null){
	    threeSelectionDisplay.setOriginalDisplay(visible);
	}
	Iterator e = basicDisplays.iterator();
	while(e.hasNext()){
	    ((BasicSeismogramDisplay)e.next()).setUnfilteredDisplay(visible);
	}
	originalVisible = visible;
    }

    public boolean getOriginalVisibility(){ return originalVisible; }

    public void setUnfilteredDisplay(boolean visible){
	Iterator e = basicDisplays.iterator();
	while(e.hasNext())
	    ((BasicSeismogramDisplay)e.next()).setUnfilteredDisplay(visible);
    }

    public void applyFilter(ColoredFilter filter){
	Iterator e = basicDisplays.iterator();
	while(e.hasNext()){
	    System.out.println("applying filter");
	    ((BasicSeismogramDisplay)e.next()).applyFilter(filter);
	}
	if(selectionDisplay != null){
	    selectionDisplay.applyFilter(filter);
	}
	if(threeSelectionDisplay != null){
	    threeSelectionDisplay.applyFilter(filter);
	}
    }

    public Set getCurrentFilters(){ return currentFilters; }

    public void globalizeAmpRange(){
	Iterator e = basicDisplays.iterator();
	while(e.hasNext())
	    ((BasicSeismogramDisplay)e.next()).getAmpRegistrar().setRegistrar(globalAmpRegistrar);
    }
    
    public void createParticleDisplay(BasicSeismogramDisplay creator, boolean advancedOption){
	if(particleDisplay == null){
	    logger.debug("creating particle display");
	    particleWindow = new JFrame("Particle Motion");
	    particleDisplay = new ParticleMotionDisplay((DataSetSeismogram)creator.getSeismograms().getFirst(), 
							(TimeConfigRegistrar)creator.getTimeConfig(), 
							creator.getAmpRegistrar(), 
							creator.getAmpRegistrar(),
							advancedOption);
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
	    particleWindow.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
			particleWindow.dispose();
			particleDisplay = null;
		    }
		});
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
	if(particleWindow.getSize().width*particleDisplays < tk.getScreenSize().width){
	    particleWindow.setLocation(particleWindow.getSize().width * particleDisplays, tk.getScreenSize().height - 
				       particleWindow.getSize().width);
	}else{
	    particleWindow.setLocation(tk.getScreenSize().width - particleWindow.getSize().width, 
				       tk.getScreenSize().height - particleWindow.getSize().height);
				       }
	particleDisplays++;
	particleWindow.setVisible(true);
	}
    }
    
    

    public void createSelectionDisplay(Selection creator){
	if(selectionDisplay == null){
	    logger.debug("creating selection display");
	    selectionWindow = new JFrame();
	    selectionWindow.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
			selectionDisplay.removeAll();
		    }
		});
	    selectionWindow.setSize(400, 200);
	    Iterator e = creator.getSeismograms().iterator();
	    TimeConfigRegistrar tr = creator.getInternalConfig();
	    DataSetSeismogram first = ((DataSetSeismogram)e.next());
	    AmpConfigRegistrar ar = new AmpConfigRegistrar(new OffsetMeanAmpConfig());
	    selectionDisplay = new VerticalSeismogramDisplay(mouseForwarder, motionForwarder, this);
	    creator.addDisplay(selectionDisplay.addDisplay(first, tr, ar, 
							   creator.getParent().getName() + "." + creator.getColor()));
	    ar.visibleAmpCalc(tr);
	    while(e.hasNext()){
		selectionDisplay.addSeismogram(((DataSetSeismogram)e.next()), 0);
	    }
	    selectionWindow.getContentPane().add(selectionDisplay);
	    Toolkit tk = Toolkit.getDefaultToolkit();
	    selectionWindow.setLocation((tk.getScreenSize().width - selectionWindow.getSize().width)/2,
					 (tk.getScreenSize().height - selectionWindow.getSize().height)/2);
	    /*if(selectionDisplays + selectionWindow.getSize().height < tk.getScreenSize().height){
		selectionWindow.setLocation(tk.getScreenSize().width - selectionWindow.getSize().width, tk.getScreenSize().height - 
					    (selectionDisplays + selectionWindow.getSize().height));
	    }else{
		selectionWindow.setLocation(tk.getScreenSize().width - selectionWindow.getSize().width, 0);
	    }
	    selectionDisplays += selectionWindow.getSize().height;*/
	    selectionWindow.setVisible(true);	
	}else{
	    logger.debug("adding another selection");
	    Iterator e = creator.getSeismograms().iterator();
	    TimeConfigRegistrar tr = creator.getInternalConfig();
	    DataSetSeismogram first = ((DataSetSeismogram)e.next());
	    AmpConfigRegistrar ar = new AmpConfigRegistrar(new OffsetMeanAmpConfig());
	    ar.visibleAmpCalc(tr);
	    creator.addDisplay(selectionDisplay.addDisplay(first, tr, ar, 
							   creator.getParent().getName() + "." + creator.getColor()));
	    while(e.hasNext()){
		selectionDisplay.addSeismogram(((DataSetSeismogram)e.next()), 0);
	    }
	}
    }

    public void createThreeSelectionDisplay(Selection creator){
	if(threeSelectionDisplay == null){
	    logger.debug("creating 3C selection display");
	    threeSelectionWindow = new JFrame();
	    threeSelectionWindow.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
			threeSelectionDisplay.removeAll();
		    }
		});
	    threeSelectionDisplay = new VerticalSeismogramDisplay(mouseForwarder, motionForwarder, this);
	    Iterator e = creator.getSeismograms().iterator();
	    TimeConfigRegistrar tr = creator.getInternalConfig();
	    AmpConfigRegistrar ar = new AmpConfigRegistrar(new OffsetMeanAmpConfig());
	    DataSetSeismogram first = ((DataSetSeismogram)creator.getSeismograms().getFirst());
	    LocalSeismogramImpl seis = first.getSeismogram();
	    XMLDataSet dataSet = (XMLDataSet)first.getDataSet();
	    ChannelId[] channelIds = dataSet.getChannelIds();
	    ChannelGrouperImpl channelProxy = new ChannelGrouperImpl();
	    ChannelId[] channelGroup = channelProxy.retrieve_grouping(channelIds, seis.getChannelID());
	    DataSetSeismogram[] seismograms = new DataSetSeismogram[3];
	    try{
		for(int counter = 0; counter < channelGroup.length; counter++) {
		    String name = DisplayUtils.getSeismogramName(channelGroup[counter], dataSet, 
						       new edu.iris.Fissures.TimeRange(seis.getBeginTime().getFissuresTime(), 
										       seis.getEndTime().getFissuresTime()));
		    seismograms[counter] = new DataSetSeismogram(dataSet.getSeismogram(name), dataSet);
		    if(seismograms[counter].getSeismogram() != null){
			Iterator g = basicDisplays.iterator();
			while(g.hasNext()){
			    BasicSeismogramDisplay current = ((BasicSeismogramDisplay)g.next());
			    Iterator h = current.getSeismograms().iterator();
			    while(h.hasNext()){
				if(((DataSetSeismogram)h.next()).getSeismogram() == seismograms[counter].getSeismogram()){
				    current.add3CSelection(creator);
				    creator.addParent(current);
				}
			    }
			}
			creator.addDisplay(threeSelectionDisplay.addDisplay(seismograms[counter], tr, ar, 
									    seismograms[counter].getSeismogram().getName() + "." 
									    + creator.getColor()));
		    }
		}
	    }catch(Exception f){ 
		f.printStackTrace();
	    }	
	    ar.visibleAmpCalc(tr);
	    threeSelectionWindow.getContentPane().add(threeSelectionDisplay);
	    threeSelectionWindow.setSize(400, 400);
	    Toolkit tk = Toolkit.getDefaultToolkit();
	    threeSelectionWindow.setLocation((tk.getScreenSize().width - threeSelectionWindow.getSize().width)/2,
					     (tk.getScreenSize().height - threeSelectionWindow.getSize().height)/2);
	    threeSelectionWindow.setVisible(true);	
	    /*if((selectionDisplays + threeSelectionWindow.getSize().height) < tk.getScreenSize().height){
	      threeSelectionWindow.setLocation(tk.getScreenSize().width - threeSelectionWindow.getSize().width, 
	      tk.getScreenSize().height - 
	      (selectionDisplays + threeSelectionWindow.getSize().height));
	      }else{
	      threeSelectionWindow.setLocation(tk.getScreenSize().width - threeSelectionWindow.getSize().width, 0);
	      }
	      selectionDisplays+= threeSelectionWindow.getSize().height;*/
			    
			    }else{
	    logger.debug("adding another selection");
	    Iterator e = creator.getSeismograms().iterator();
	    TimeConfigRegistrar tr = creator.getInternalConfig();
	    DataSetSeismogram first = ((DataSetSeismogram)e.next());
	    AmpConfigRegistrar ar = new AmpConfigRegistrar(new OffsetMeanAmpConfig());
	    ar.visibleAmpCalc(tr);
	    creator.addDisplay(threeSelectionDisplay.addDisplay(first, tr, ar, 
								creator.getParent().getName() + "." + creator.getColor()));
	    while(e.hasNext()){
		threeSelectionDisplay.addSeismogram(((DataSetSeismogram)e.next()), 0);
	    }
	}
    }

    
	
    
    protected static int particleDisplays = 0, selectionDisplays = 0;
    
    protected boolean originalVisible;

    protected JFrame selectionWindow, particleWindow, threeSelectionWindow;

    protected SeismogramSorter sorter;

    protected TimeConfigRegistrar globalTimeRegistrar;

protected AmpConfigRegistrar globalAmpRegistrar;
    
protected HashMap selectionDisplayMap = new HashMap();

protected ParticleMotionDisplay particleDisplay;
    
    protected Set currentFilters = new HashSet();

    protected LinkedList basicDisplays = new LinkedList();

    protected LinkedList names = new LinkedList();

    protected MouseForwarder mouseForwarder;

    protected MouseMotionForwarder motionForwarder;

    protected JComponent seismograms;

    public static JLabel time = new JLabel("   Time:                        ");
    
    public static JLabel amp = new JLabel("   Amplitude:       ");

    protected SimpleDateFormat output = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.S");

    protected Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

    protected VerticalSeismogramDisplay selectionDisplay, threeSelectionDisplay, parent;

    private static Category logger = Category.getInstance(VerticalSeismogramDisplay.class.getName());
}// VerticalSeismogramDisplay
