package edu.sc.seis.fissuresUtil.display;

import java.util.*;
import java.io.IOException;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.TimeRange;
import edu.sc.seis.fissuresUtil.freq.ColoredFilter;
import edu.sc.seis.fissuresUtil.chooser.DataSetChannelGrouper;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.text.SimpleDateFormat;
import edu.sc.seis.fissuresUtil.xml.*;
import org.apache.log4j.*;
import edu.sc.seis.TauP.Arrival;

/**
 * VerticalSeismogramDisplay(VSD) is a JComponent that can contain multiple 
 * BasicSeismogramDisplays(BSD) and also controls the selection windows and 
 * particle motion windows created by its BSDs.  
 *
 *
 * Created: Tue Jun  4 10:52:23 2002
 *
 * @author Charlie Groves
 * @version 0.1
 */

public abstract class VerticalSeismogramDisplay extends JComponent{
    /**
     * Creates a <code>VerticalSeismogramDisplay</code> without a parent
     *
     * @param mouseForwarder the object every contained BSD forwards its mouse events to
     * @param motionForwarder the object every contained BSD forwards its mouse motion events to
     */
    public VerticalSeismogramDisplay(MouseForwarder mouseForwarder, MouseMotionForwarder motionForwarder){
	this(mouseForwarder, motionForwarder, null);
    }

    /**
     * Creates a <code>VerticalSeismogramDisplay</code>
     *
     * @param mouseForwarder the object every contained BSD forwards its mouse events to
     * @param motionForwarder the object every contained BSD forwards its mouse motion events to
     * @param parent the VSD that controls this VSD
     */
    public VerticalSeismogramDisplay(MouseForwarder mouseForwarder, MouseMotionForwarder motionForwarder, 
				     VerticalSeismogramDisplay parent){
	output.setTimeZone(TimeZone.getTimeZone("GMT"));
	this.mouseForwarder = mouseForwarder;
	this.motionForwarder = motionForwarder;
	super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	sorter = new SeismogramSorter();
	if(parent != null){
	    this.originalVisible = parent.getOriginalVisibility();
	    this.parent = parent;
	}else{
	    this.originalVisible = true;
	}
    }
    
    /**
     * adds the given seismograms to the VSD with their seismogram names as suggestions
     * 
     *
     * @param dss the seismograms to be added
     * @return the BSD the seismograms were added to
     */
    public abstract BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss);
    
    /**
     * adds the seismograms to the VSD with the passed timeConfig
     *
     * @param dss the seismograms to be added
     * @param tc the time config for the new seismograms
     * @return the BSD the seismograms were added to
     */
    public abstract BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, TimeConfig tc);
    
    /**
     * adds the seismograms to the VSD with the passed amp config
     * @param dss the seismograms to be added
     * @param ac the amp config for the new seismograms
     * @return the BSD the seismograms were added to
     */
    public abstract BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, AmpConfig ac);
    
     /**
     * adds the seismograms to the VSD with the passed timeConfig and ampConfig
     *
     * @param dss the seismograms for the new BSD
     * @param tc the time config for the new BSD
     * @param ac the amp config for the new BSD
     * @return the BSD the seismograms were added to
     * 
     */
    public abstract BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, TimeConfig tc, AmpConfig ac);
    
    
    public java.util.List getSeismogramNames(){
	java.util.List names = new ArrayList();
	Iterator e = basicDisplays.iterator();
	while(e.hasNext()){
	    String[] current = ((BasicSeismogramDisplay)e.next()).getNames();
	    for(int i = 0; i < current.length; i++){
		names.add(current[i]);
	    }
	}
	return names;
    }

    public DataSetSeismogram[] getSeismograms(){
	java.util.List seismogramList = new ArrayList();
	Iterator e = basicDisplays.iterator();
	while(e.hasNext()){
	    DataSetSeismogram[] seismos = ((BasicSeismogramDisplay)e.next()).getSeismograms();
	    for(int i = 0; i < seismos.length; i++){
		seismogramList.add(seismos[i]);
	    }
	}
	return ((DataSetSeismogram[])seismogramList.toArray(new DataSetSeismogram[seismogramList.size()]));
    }
	    
    /**
     * <code>getDisplays</code> returns a list of all the displays directly held by this VSD
     *
     * @return a list of all direcly held displays
     */
    public LinkedList getDisplays(){ return basicDisplays; }
    
    /**
     * <code>getAllBasicDisplays</code> returns a list of all displays held directly
     * by this VSD or its children
     *
     * @return a list of all displays in this VSD or its children
     */
    public LinkedList getAllBasicDisplays(){
	return getAllBasicDisplays(new LinkedList());
    }
    
    /**
     * <code>getAllBasicDisplays</code> returns a list of all displays held directly
     * by this VSD or its children
     *
     * @param target the linked list that will contain all the displays
     * @return a list of all displays in this VSD or its children
     */
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

    
    
    /**
     * Sets a string to be appended to the names of each seismogram added to the display.  
     * @param suffix the suffix for the seismogram names
     */
    public void setSuffix(String suffix){
	this.suffix = suffix;
    }

    
    public void print(){
	SeismogramPrinter.print(getDisplayArray());
	revalidate();
    }
    
    /**
     * <code>getDisplayArray</code> returns an array containing all of
     * the displays directly held by this VSD
     *
     * @return all displays directly held by this vsd
     */
    public BasicSeismogramDisplay[] getDisplayArray(){
	return ((BasicSeismogramDisplay[])basicDisplays.toArray(new BasicSeismogramDisplay[basicDisplays.size()]));
    }
	
    /**
     * <code>setSort/code> changes the method by which added displays are
     * ordered.  NOIMPL
     *
     * @param sorter the new sorter
     */
    public void setSort(SeismogramSorter sorter){
    } 

    public void removeAllSelections(){
	removeSelectionDisplay();
	remove3CSelectionDisplay();
    }
    
    /**
     * <code>removeSelectionDisplay</code> removes a particular VSD child of
     * this VSD
     *
     * @param display the VSD to be removed
     */
    public void removeSelectionDisplay(VerticalSeismogramDisplay display){
	if(display == selectionDisplay){
	    removeSelectionDisplay();
	}else if(display == threeSelectionDisplay){
	    remove3CSelectionDisplay();
	}
    }
    
    /**
     * <code>removeSelectionDisplay</code> removes the Tag Pick Zone window
     *
     */
    public void removeSelectionDisplay(){
	 Iterator e = basicDisplays.iterator();
	 while(e.hasNext()){
	     ((BasicSeismogramDisplay)e.next()).clearRegSelections();
	 }
	 if(selectionWindow != null){
	     selectionWindow.dispose();
	     selectionWindow = null;
	     selectionDisplay.removeAll();
	     selectionDisplay = null;
	 }
    }

    /**
     * <code>remove3CSelectionDisplay</code> removes the tag particle motion zone
     * window
     *
     */
    public void remove3CSelectionDisplay(){
	Iterator e = basicDisplays.iterator();
	while(e.hasNext()){
	    ((BasicSeismogramDisplay)e.next()).clear3CSelections();
	}
	if(threeSelectionWindow != null){
	    threeSelectionWindow.dispose();
	    threeSelectionWindow = null;
	    threeSelectionDisplay.removeAll();
	    threeSelectionDisplay = null;
	}
    }

    /**
     * <code>removeAll</code> clears this display and all of its children,
     * and if it has a parent, removes it from the parent as well
     *
     */
    public void removeAll(){
	logger.debug("removing all displays");
	if(parent != null){
	    parent.removeSelectionDisplay(this);
	}
	super.removeAll();
	basicDisplays.clear();
	sorter = new SeismogramSorter();
	globalRegistrar = null;
	this.time.setText("   Time: ");
	this.amp.setText("   Amplitude: ");
	if(selectionDisplay != null){
	    selectionDisplay.removeAll();
	    if(selectionWindow != null){
		selectionWindow.dispose();
	    }
	    selectionDisplay = null;
	}
	if(threeSelectionDisplay != null){
	    threeSelectionDisplay.removeAll();
	    if(threeSelectionWindow != null){
		threeSelectionWindow.dispose();
	    }
	    threeSelectionDisplay = null;
	}
	if(particleDisplay != null){
	    particleWindow.dispose();
	    particleDisplays--;
	    particleDisplay.removeAll();
	    particleDisplay = null;
	    }
	repaint();
    }

    /**
     * <code>removeDisplay</code> removes a BSD from the VSD
     *
     * @param display the BSD to be removed
     * @return true if the display is removed
     */
    public boolean removeDisplay(BasicSeismogramDisplay display){
	if(basicDisplays.contains(display)){
	    if(basicDisplays.size() == 1){
		this.removeAll();
		return true;
	    }
	    super.remove(display);
	    basicDisplays.remove(display);
	    sorter.remove(display.getSeismograms()[0].toString());
	    ((BasicSeismogramDisplay)basicDisplays.getFirst()).addTopTimeBorder();
	    ((BasicSeismogramDisplay)basicDisplays.getLast()).addBottomTimeBorder();
	    super.revalidate();
	    display.destroy();
	    repaint();
	    return true;
	}
	return false;
    }
	
    
    /**
     * <code>setLabels</code> sets the time and amp labels for all VSDs 
     *
     * @param time the new label time
     * @param amp the new label amp
     */
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

    /**
     * <code>setOriginalDisplay</code> sets the display of the unfiltered
     * seismogram in all BSDs held by this VSD or its children
     *
     * @param visible the new visibility of the unfiltered seismogram
     */
    public void setOriginalDisplay(boolean visible){
	Iterator e = getAllBasicDisplays().iterator();
	while(e.hasNext()){
	    ((BasicSeismogramDisplay)e.next()).setUnfilteredDisplay(visible);
	}
	originalVisible = visible;
    }

    /**
     * 
     * @return true if the unfiltered seismogram is visible
     */
    public boolean getOriginalVisibility(){ return originalVisible; }

    /**
     * <code>setUnfilteredDisplay</code> sets the display of the filtered
     * seismograms in all the BSDs held by this VSD and its children
     *
     * @param visible the new visibility of the filtered seismogram
     */
    public void setUnfilteredDisplay(boolean visible){
	Iterator e = getAllBasicDisplays().iterator();
	while(e.hasNext())
	    ((BasicSeismogramDisplay)e.next()).setUnfilteredDisplay(visible);
    }

    /**
     * <code>applyFilter</code> applies a new filter to all the BSDs held
     * by this VSD and its children
     *
     * @param filter a <code>ColoredFilter</code> value
     */
    public void applyFilter(ColoredFilter filter){
	Iterator e = getAllBasicDisplays().iterator();
	while(e.hasNext()){
	    ((BasicSeismogramDisplay)e.next()).applyFilter(filter);
	}
    }

    public Registrar getGlobalRegistrar(){ return globalRegistrar; }

    public void setGlobalRegistrar(Registrar newGlobal){
	globalRegistrar = newGlobal;
    }

    /**
     * this merely calls reset on the globalRegistrar
     */	
    public void resetGlobalRegistrar(){
	if(globalRegistrar != null){
	    globalRegistrar.reset();
	}
    }

    /**
     * <code>globalizeAmpRange</code> sets the amp configs of every
     * BSD held directly by this VSD to be the global amp config
     * for this VSD
     *
     */
    public void globalizeAmpRange(){
	Iterator e = basicDisplays.iterator();
	while(e.hasNext())
	    ((BasicSeismogramDisplay)e.next()).getRegistrar().setAmpConfig(globalRegistrar);
    }
    
    /**
     * <code>createParticleDisplay</code> adds the seismograms of creator to the Particle
     * display held by this VSD, and if the Particle display has not been created, it does so
     * if particleAllowed is true.  If not, it displays a message saying particle motion windows
     * are not allowed off of this display
     *
     * @param creator the BSD whose seismograms will be added to the particle display
     * @param advancedOption if true, the particle display will allow display of all 3 components
     * individuall instead of in only N-E E-Z and N-Z pairs
     */
    public void createParticleDisplay(BasicSeismogramDisplay creator, boolean advancedOption){
	if(particleAllowed){
	    if(particleDisplay == null){
		logger.debug("creating particle display");
		particleWindow = new JFrame(particleWindowName);
		particleWindow.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
			    particleDisplay.removeAll();
			    particleDisplays--;
			}
		    });
		particleDisplay = new ParticleMotionDisplay((DataSetSeismogram)creator.getSeismograms()[0],
							    creator.getRegistrar(),
							    advancedOption);
		JPanel displayPanel = new JPanel();
		JButton zoomIn = new JButton("Zoom In");
		JButton zoomOut = new JButton("Zoom Out");
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
	    }else {
		particleDisplay.addParticleMotionDisplay((DataSetSeismogram)creator.getSeismograms()[0], 
							 creator.getRegistrar());
		particleWindow.toFront();
	    } // end of else
	}else{
	    JOptionPane.showMessageDialog(null,
					  "Particle motion isn't allowed from this display!",
					  "Particle Motion Display Creation",
					  JOptionPane.ERROR_MESSAGE);
	}
    }
    
    

    /**
     * <code>createSelectionDisplay</code> adds the seismogram in the Selection argument
     * to this VSD's selection display.  If the selection display does not exist, it creates it
     * then adds them
     *
     * @param creator the selection whose seismograms will be added
     */
    public void createSelectionDisplay(Selection creator){
	if(selectionDisplay == null){
	    logger.debug("creating selection display");
	    selectionDisplay = new MultiSeismogramWindowDisplay(mouseForwarder, motionForwarder, this);
	    addSelection(creator, selectionDisplay);
	    selectionWindow = new JFrame(tagWindowName);
	    selectionWindow.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
			removeSelectionDisplay();
		    }
		});
	    selectionWindow.setSize(400, 200);
	    selectionWindow.getContentPane().add(new JScrollPane(selectionDisplay));
	    Toolkit tk = Toolkit.getDefaultToolkit();
	    selectionWindow.setLocation((tk.getScreenSize().width - selectionWindow.getSize().width)/2,
					 (tk.getScreenSize().height - selectionWindow.getSize().height)/2);
	    selectionWindow.setVisible(true);
	}else{
	    logger.debug("adding another selection");
	    addSelection(creator,selectionDisplay);
	    selectionWindow.toFront();
	}
    }

    private void addSelection(Selection creator, VerticalSeismogramDisplay reaper){
	DataSetSeismogram[] creatorSeismos = creator.getSeismograms();
	DataSetSeismogram[] newSeismos = new DataSetSeismogram[creatorSeismos.length];
	for(int i = 0; i < creatorSeismos.length; i++){
	    newSeismos[i] = new DataSetSeismogram(creatorSeismos[i], creatorSeismos[i] + "." + creator.getColor());
	}
	BasicSeismogramDisplay selectionDisplay = reaper.addDisplay(newSeismos, (TimeConfig)creator.getInternalRegistrar());
	creator.addDisplay(selectionDisplay);
	//makes amp scale correctly.... pick displays show up with an unexpanded amplitude.   
	//remove when the real bug causing the amplitude not to scale when created is fixed
	creator.getInternalRegistrar().shaleTime(0, 1);
	Arrival[] parentArrivals = creator.getParent().getArrivals();
	if(parentArrivals != null){
	    selectionDisplay.addFlags(parentArrivals);
	}
    }

    /**
     * <code>createThreeSelectionDisplay</code> adds the seismograms in creator along
     * with the additional two components to this VSD's 3 selection display.  If it does
     * not exist, the 3 Selection display is created here
     *
     * @param creator the selection whose seismograms will be added to the 3 selection
     * display
     */
    public void createThreeSelectionDisplay(Selection creator){
	if(threeSelectionDisplay == null){
	    logger.debug("creating 3C selection display");
	    threeSelectionDisplay = new MultiSeismogramWindowDisplay(mouseForwarder, motionForwarder, this);
	    addGroupedSelection(creator, threeSelectionDisplay);
	    if(threeSelectionDisplay.getDisplays().size() > 0){
		threeSelectionWindow = new JFrame(particleTagWindowName);
		threeSelectionWindow.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
			    remove3CSelectionDisplay();
			}
		    });
		threeSelectionWindow.setSize(400, 400);
		Toolkit tk = Toolkit.getDefaultToolkit();
		threeSelectionWindow.setLocation((tk.getScreenSize().width - threeSelectionWindow.getSize().width)/2,
						 (tk.getScreenSize().height - threeSelectionWindow.getSize().height)/2);
		threeSelectionWindow.getContentPane().add(new JScrollPane(threeSelectionDisplay));
		threeSelectionWindow.setVisible(true);
	    }else{
		remove3CSelectionDisplay();
		 JOptionPane.showMessageDialog(null, 
					  "The other components weren't found to create the Particle Motion Zone, so it can't be created.",
					       "Other Components not found",
					  JOptionPane.WARNING_MESSAGE);
	    }
	}else{
	    logger.debug("adding another 3Cselection");
	    addGroupedSelection(creator, threeSelectionDisplay);
	    threeSelectionWindow.toFront();
	}
    }

    private void addGroupedSelection(Selection creator, VerticalSeismogramDisplay reaper){
	DataSetSeismogram[] creatorSeismos = creator.getSeismograms();
	DataSetSeismogram[][] componentSorted = DisplayUtils.getComponents(creatorSeismos, "." + creator.getColor());
	Arrival[] parentArrivals = creator.getParent().getArrivals();
	for(int i = 0; i < componentSorted.length; i++){
	    if(componentSorted[i].length > 0){
		((TimeConfig)creator.getInternalRegistrar()).add(componentSorted[i]);
		BasicSeismogramDisplay newDisplay = threeSelectionDisplay.addDisplay(componentSorted[i], (TimeConfig)creator.getInternalRegistrar());	    
		creator.addDisplay(newDisplay);
		if(parentArrivals != null){
		    newDisplay.addFlags(parentArrivals);
		}
	    }
	}
	Iterator g = basicDisplays.iterator();
	while(g.hasNext()){
	    BasicSeismogramDisplay current = ((BasicSeismogramDisplay)g.next());
	    DataSetSeismogram[] basicDisplaySeismos = current.getSeismograms();
	    for(int  i = 0; i < componentSorted.length; i++){
		for(int j = 0; j < basicDisplaySeismos.length; j++){
		    for(int k = 0; k < componentSorted[i].length; k++){
			if(componentSorted[i][k].getSeismogram() == basicDisplaySeismos[j].getSeismogram()){
			    current.add3CSelection(creator);
			    creator.addParent(current);
			}
		    }
		}
	    }
	}
	//makes amp scale correctly.... pick displays show up with an unexpanded amplitude.   
	//remove when the real bug causing the amplitude not to scale when created is fixed
	creator.getInternalRegistrar().shaleTime(0, 1);
    }

    public static JLabel getTimeLabel(){ return time; }

    public static JLabel getAmpLabel(){ return amp; }

    public static void setTagWindowName(String name){
	tagWindowName = name;
    }
    
    public static String getTagWindowName(){ return tagWindowName; }


    public static void setParticleTagWindowName(String name){
	particleTagWindowName = name;
    }
    
    public static String getParticleTagWindowName(){ return particleTagWindowName; }


    public static void setParticleWindowName(String name){
	particleWindowName = name;
    }
    
    public static String getParticleWindowName(){ return particleWindowName; }

    public void setParticleAllowed(boolean allowed){
	particleAllowed = allowed;
    }

    public boolean getParticleAllowed(){ return particleAllowed; }


    protected String suffix = "";
    
    protected static int particleDisplays = 0, selectionDisplays = 0;
    
    protected boolean originalVisible;

    protected JFrame selectionWindow, particleWindow, threeSelectionWindow;

    protected SeismogramSorter sorter;

    protected Registrar globalRegistrar;

    protected ParticleMotionDisplay particleDisplay;
    
    protected LinkedList basicDisplays = new LinkedList();

    protected MouseForwarder mouseForwarder;

    protected MouseMotionForwarder motionForwarder;

    protected boolean particleAllowed = true;

    public static JLabel time = new JLabel("   Time:                        ");
    
    public static JLabel amp = new JLabel("   Amplitude:       ");

    protected SimpleDateFormat output = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");

    protected static Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

    protected VerticalSeismogramDisplay selectionDisplay, threeSelectionDisplay, parent;

    private static Category logger = Category.getInstance(VerticalSeismogramDisplay.class.getName());

    private static String tagWindowName = "Pick Zone";

    private static String particleTagWindowName = "Particle Motion Zone";

    private static String particleWindowName = "Particle Motion";

}// VerticalSeismogramDisplay
