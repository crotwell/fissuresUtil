package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.TauP.*;
import edu.sc.seis.fissuresUtil.chooser.*;
import edu.sc.seis.fissuresUtil.namingService.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.network.*;

import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

import org.apache.log4j.*;


/**
 * ParticleMotionDisplay.java
 *
 *
 * Created: Tue Jun 11 15:22:30 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class ParticleMotionDisplay extends JPanel implements AmpSyncListener, TimeSyncListener {

    public ParticleMotionDisplay(DataSetSeismogram hseis,
				 TimeConfigRegistrar timeConfigRegistrar,
				 AmpConfigRegistrar hAmpConfigRegistrar,
				 AmpConfigRegistrar vAmpConfigRegistrar,
				 Color color,
				 boolean advancedOption) {
	
	

	JFrame displayFrame = new JFrame();
	JPanel informationPanel = new JPanel();
	String message = " Please Wait ....For the Particle Motion Window";
 	JLabel jLabel = new JLabel(message);
	JTextArea textArea = new JTextArea("LKJDLKJFDJFLKDJFLKDJFKLDJFLKDJFKLDJLKJDFL", 80,40);
	textArea.setVisible(true);
	informationPanel.setLayout(new BorderLayout());
	informationPanel.add(textArea, BorderLayout.CENTER);
	informationPanel.setSize(new java.awt.Dimension(500, 300));

	displayFrame.getContentPane().setLayout(new BorderLayout());
	displayFrame.getContentPane().add(jLabel, BorderLayout.CENTER);

	displayFrame.setSize(new java.awt.Dimension(500, 300));
	informationPanel.setVisible(true);
	displayFrame.pack();
	displayFrame.setVisible(true);

	createGUI(timeConfigRegistrar,
		  hAmpConfigRegistrar,
		  vAmpConfigRegistrar);

	ParticleMotionDisplayThread t = new ParticleMotionDisplayThread(hseis,
							      timeConfigRegistrar,
							      hAmpConfigRegistrar,
							      vAmpConfigRegistrar,
							      advancedOption, 
							      true,
							      this);
	
	t.execute();
	displayFrame.dispose();
					  
    }

    public ParticleMotionDisplay(DataSetSeismogram[] seismograms,
				 TimeConfigRegistrar timeConfigRegistrar,
				 AmpConfigRegistrar hAmpConfigRegistrar,
				 AmpConfigRegistrar vAmpConfigRegistrar,
				 Color color,
				 boolean advancedOption) {
	
	

	JFrame displayFrame = new JFrame();
	JPanel informationPanel = new JPanel();
	String message = " Please Wait ....For the Particle Motion Window";
 	JLabel jLabel = new JLabel(message);
	JTextArea textArea = new JTextArea("SDJSLDJDKJDKJSKJDJDSKJKLSDJSJD");
	informationPanel.setLayout(new BorderLayout());
	informationPanel.add(textArea, BorderLayout.CENTER);
	informationPanel.setSize(new java.awt.Dimension(500, 300));

	displayFrame.getContentPane().setLayout(new BorderLayout());
	displayFrame.getContentPane().add(informationPanel, BorderLayout.CENTER);
	displayFrame.setSize(new java.awt.Dimension(500, 300));
	displayFrame.pack();
	displayFrame.show();

	
	createGUI(timeConfigRegistrar,
		  hAmpConfigRegistrar,
		  vAmpConfigRegistrar);

	ParticleMotionDisplayThread t = new ParticleMotionDisplayThread(seismograms,
							      timeConfigRegistrar,
							      hAmpConfigRegistrar,
							      vAmpConfigRegistrar,
							      advancedOption, 
							      true,
							      this);
	
	t.execute();
	displayFrame.dispose();
					  
    }

    public void createGUI(TimeConfigRegistrar timeConfigRegistrar,
			  AmpConfigRegistrar hAmpConfigRegistrar,
			  AmpConfigRegistrar vAmpConfigRegistrar) {

		this.hAmpConfigRegistrar = hAmpConfigRegistrar;
	this.vAmpConfigRegistrar = vAmpConfigRegistrar;
	particleDisplayPanel = new JLayeredPane();
	OverlayLayout overlayLayout = new OverlayLayout(particleDisplayPanel);

	radioPanel = new JPanel();
	this.setLayout(new BorderLayout());
	particleDisplayPanel.setLayout(overlayLayout);
	radioPanel.setLayout(new GridLayout(1, 0));

	if(timeConfigRegistrar != null) {
	    timeConfigRegistrar.addTimeSyncListener(this);
	}

	view = new ParticleMotionView(this);
	view.setSize(new java.awt.Dimension(300, 300));
	particleDisplayPanel.add(view, PARTICLE_MOTION_LAYER);

	hAmpScaleMap = new AmpScaleMapper(50,
                                          4,
					  new UnitRangeImpl(-100,
							    100,
							    UnitImpl.COUNT)
					  );
        vAmpScaleMap = new AmpScaleMapper(50,
                                          4,
					  new UnitRangeImpl(-100,
							    100,
							    UnitImpl.COUNT)
					  );
					  
	scaleBorder = new ScaleBorder();
	scaleBorder.setBottomScaleMapper(hAmpScaleMap);
	scaleBorder.setLeftScaleMapper(vAmpScaleMap);        
        hTitleBorder = 
            new BottomTitleBorder("X - axis Title");
        vTitleBorder = 
            new LeftTitleBorder("Y - axis Title");

	particleDisplayPanel.setBorder(
				       BorderFactory.createCompoundBorder(
									  BorderFactory.createCompoundBorder(
													     BorderFactory.createRaisedBevelBorder(),
													     // hTitleBorder),
									  
									  BorderFactory.createCompoundBorder(hTitleBorder,
													     vTitleBorder)),
									  BorderFactory.createCompoundBorder(
								scaleBorder,
								BorderFactory.createLoweredBevelBorder()))
	  );

	
	add(particleDisplayPanel, BorderLayout.CENTER);
	radioPanel.setVisible(false);
	add(radioPanel, BorderLayout.SOUTH);
		
	this.addComponentListener(new ComponentAdapter() {
		public void componentResized(ComponentEvent e) {
		 
		    resize();
		}
		public void componentShown(ComponentEvent e) {
		    resize();
		}
	    });


	updateTimeRange();


    }
	

    public ParticleMotionDisplay (DataSetSeismogram hSeis,
				  TimeConfigRegistrar timeConfigRegistrar,
				  AmpConfigRegistrar hAmpConfigRegistrar,
				  AmpConfigRegistrar vAmpConfigRegistrar, boolean advancedOption){
	this(hSeis,
	     timeConfigRegistrar,
	     hAmpConfigRegistrar,
	     vAmpConfigRegistrar,
	     null,
	     advancedOption); //setting the advanced option to be false;
    }

    
    /**
     * Creates a new <code>ParticleMotionDisplay</code> instance.
     *
     * @param hSeis a <code>LocalSeismogramImpl</code> value
     * @param vSeis a <code>LocalSeismogramImpl</code> value
     * @param hAmpRangeConfig an <code>AmpRangeConfig</code> value
     * @param vAmpRangeConfig an <code>AmpRangeConfig</code> value
     */
    public ParticleMotionDisplay (DataSetSeismogram hSeis,
				  TimeConfigRegistrar timeConfigRegistrar,
				  AmpConfigRegistrar hAmpConfigRegistrar,
				  AmpConfigRegistrar vAmpConfigRegistrar, Color color){
	this(hSeis,
	     timeConfigRegistrar,
	     hAmpConfigRegistrar,
	     vAmpConfigRegistrar,
	     color,
	     false); //setting the advanced option to be false;
    }


    public ParticleMotionDisplay (DataSetSeismogram hSeis,
				  TimeConfigRegistrar timeConfigRegistrar,
				  AmpConfigRegistrar hAmpConfigRegistrar,
				  AmpConfigRegistrar vAmpConfigRegistrar){
	this(hSeis,
	     timeConfigRegistrar,
	     hAmpConfigRegistrar,
	     vAmpConfigRegistrar,
	     null,
	     false); //setting the advanced option to be false;
    }


  public ParticleMotionDisplay(DataSetSeismogram hseis,
			       TimeConfigRegistrar timeConfigRegistrar,
			       AmpConfigRegistrar ampRangeConfig) {

      this(hseis, timeConfigRegistrar, ampRangeConfig, ampRangeConfig);
    }

    public ParticleMotionDisplay(DataSetSeismogram hseis,
				 TimeConfigRegistrar timeConfigRegistrar) {
	this(hseis,timeConfigRegistrar, new AmpConfigRegistrar());
    }

 

		 
    public LocalSeismogram[] retreiveSeismograms(edu.iris.Fissures.Time startTime, 
						 edu.iris.Fissures.Time endTime, 
						 ChannelId channelId,
						 DataCenter dataCenter) throws edu.iris.Fissures.FissuresException
    {
	RequestFilter[] filters;
	filters = new RequestFilter[1];
	filters[0] = 
	    new RequestFilter(channelId,
			      startTime,
			      endTime
			      );
	
	SeisTimeFilterSelector selector = new SeisTimeFilterSelector();
	LocalSeismogram[] localSeismograms;
	localSeismograms = selector.getFromGivenFilters(dataCenter, filters);
	return localSeismograms;
    }



    public void updateAmpRange() {
	this.hAmpScaleMap.setUnitRange(hAmpConfigRegistrar.getAmpRange());
    }
    
    public void updateVerticalAmpRange() {
	this.vAmpScaleMap.setUnitRange(vAmpConfigRegistrar.getAmpRange());
    }
    public void updateHorizontalAmpScale(UnitRangeImpl r) {
	//logger.debug("The amplitudeRange is being updated ");
	//logger.debug("The minimum value of the updated value is "+r.getMinValue());
	//logger.debug("The maximum value of the updated vlue is "+r.getMaxValue());
	this.hAmpScaleMap.setUnitRange(r);
	resize();
    }

    public void updateVerticalAmpScale(UnitRangeImpl r) {

	this.vAmpScaleMap.setUnitRange(r);
	resize();
    }

    /**
     * Describe <code>resize</code> method here.
     *
     */
    public synchronized void resize() {
	if(getSize().width == 0 || getSize().height == 0) return;  
	Dimension dim = view.getSize();

	Insets insets =	view.getInsets();
	int width = particleDisplayPanel.getSize().width;
	int height = particleDisplayPanel.getSize().height;
	width = width - particleDisplayPanel.getInsets().left - particleDisplayPanel.getInsets().right;
	height = height - particleDisplayPanel.getInsets().top - particleDisplayPanel.getInsets().bottom;
	if(width < height) {
	    logger.debug("Before particleDisplayPanel.setSize() ");
	    //particleDisplayPanel = null;
	    //particleDisplayPanel.getSize();
	    particleDisplayPanel.setSize(new Dimension(particleDisplayPanel.getSize().width,
	 			       width + particleDisplayPanel.getInsets().top + particleDisplayPanel.getInsets().bottom));
	    
	    logger.debug("After particleDisplayPanel. setSize()");
	    
	} else {
	    logger.debug("Before particleDisplayPanel.setSize() ");
	    particleDisplayPanel.setSize(new Dimension(height  + particleDisplayPanel.getInsets().left + particleDisplayPanel.getInsets().right,
				       particleDisplayPanel.getSize().height));

	    logger.debug("After particleDiplayPanel. setSize()");

	}
	logger.debug("Before view .resize()##############################");
	view.resize();
	logger.debug("view coordinates are  width = "+view.getSize().width+" height = "+view.getSize().height);
	logger.debug("view insets left = "+insets.left+" right = "+insets.right);
	logger.debug("view insets top = "+insets.top+" bottom = "+insets.bottom);
	logger.debug("display after width = "+getSize().width+" height = "+getSize().height);
	if(hAmpScaleMap != null) {
	    hAmpScaleMap.setTotalPixels(dim.width  - insets.left - insets.right);
	    vAmpScaleMap.setTotalPixels(dim.height  - insets.top - insets.bottom);
	}
	repaint();
    }//
    
    public synchronized void addParticleMotionDisplay(DataSetSeismogram hseis,
					  TimeConfigRegistrar timeConfigRegistrar,
					  AmpConfigRegistrar hAmpConfigRegistrar,
					  AmpConfigRegistrar vAmpConfigRegistrar) {

	this.hAmpConfigRegistrar = hAmpConfigRegistrar;
	this.vAmpConfigRegistrar = vAmpConfigRegistrar;
	boolean buttonPanel = this.displayButtonPanel;
	this.displayButtonPanel = false;
	ParticleMotionDisplayThread t = new ParticleMotionDisplayThread(hseis,
									timeConfigRegistrar,
									hAmpConfigRegistrar,
									vAmpConfigRegistrar,
									buttonPanel, 
									false,
									this);
	t.execute();

    }

    public synchronized void displayBackAzimuth(edu.sc.seis.fissuresUtil.xml.DataSet dataset, ChannelId chanId) {
	edu.sc.seis.fissuresUtil.cache.CacheEvent cacheEvent = 
	    ((edu.sc.seis.fissuresUtil.xml.XMLDataSet)dataset).getEvent();
	Channel channel = ((edu.sc.seis.fissuresUtil.xml.XMLDataSet)dataset).getChannel(chanId);
	if(cacheEvent != null) {
	    try {
		Origin origin = cacheEvent.get_preferred_origin();
		Station station = channel.my_site.my_station;
		double azimuth = SphericalCoords.azimuth(station.my_location.latitude,
							 station.my_location.longitude,
							 origin.my_location.latitude, 
							 origin.my_location.longitude);
		double angle = 90 - azimuth;
		
		addAzimuthLine(angle);
		addSector(angle+5, angle-5);
	    } catch(NoPreferredOrigin npoe) {
		logger.debug("no preferred origin");
	    }
	}
    }

   

    /**
     * sets the AmplitudeRange of the ParticleMotionDisplay.
     *
     * @param amplitudeRange an <code>AmpConfigRegistrar</code> value
     */
    public synchronized void setAmplitudeRange(AmpConfigRegistrar amplitudeRange) {
	
	this.hAmpConfigRegistrar = amplitudeRange;
	this.vAmpConfigRegistrar = amplitudeRange;
    }

    public synchronized void addAzimuthLine(double degrees) {

	view.addAzimuthLine(degrees);
    }

    public synchronized void addSector(double degreeone, double degreetwo) {

	view.addSector(degreeone, degreetwo);
    }

    public synchronized void setZoomIn(boolean value) {

	view.setZoomIn(value);
    }

    public synchronized void setZoomOut(boolean value) {

	view.setZoomOut(value);
    }


    public synchronized ParticleMotionView getView() {
	return this.view;
    }

    public void fireAmpRangeEvent(AmpSyncEvent event) {

	this.hAmpConfigRegistrar.fireAmpRangeEvent(event);
    }

    public synchronized void updateTimeRange() {

	view.updateTimeRange();
    }

    public void formCheckBoxPanel(ChannelId[] channelGroup) {
	//radioPanel.removeAll();
	ArrayList arrayList = new ArrayList();
	for(int counter = 0; counter < channelGroup.length; counter++) {
	    for(int subcounter = counter+1; subcounter < channelGroup.length; subcounter++) {
		String labelStr = getOrientationName(channelGroup[counter].channel_code)+"-"+
		    getOrientationName(channelGroup[subcounter].channel_code);
		JCheckBox radioButton = new JCheckBox(labelStr);
		radioButton.setActionCommand(labelStr);
		radioButton.addItemListener(new RadioButtonListener());
		arrayList.add(radioButton);
	    }
	}
	JCheckBox[] checkBoxes = new JCheckBox[arrayList.size()];
	checkBoxes = (JCheckBox[])arrayList.toArray(checkBoxes);
	//	checkBoxes[0].setSelected(true);
	initialButton = checkBoxes[0];
	view.setDisplayKey(checkBoxes[0].getText());
	for(int counter = 0; counter < channelGroup.length; counter++) {
	    radioPanel.add(checkBoxes[counter]);
	}
	radioPanel.setVisible(true);
	
	//	view.repaint();
   }

    public void formRadioSetPanel(ChannelId[] channelGroup) {
	//radioPanel.removeAll();
	ArrayList arrayList = new ArrayList();
	for(int counter = 0; counter < channelGroup.length; counter++) {
	    for(int subcounter = counter + 1; subcounter < channelGroup.length; subcounter++) {
		String labelStr = getOrientationName(channelGroup[counter].channel_code)+"-"+
		    getOrientationName(channelGroup[subcounter].channel_code);
		JRadioButton radioButton = new JRadioButton(labelStr);
		radioButton.setActionCommand(labelStr);
		radioButton.addItemListener(new RadioButtonListener());
		arrayList.add(radioButton);
	    }
	}

	JRadioButton[] radioButtons = new JRadioButton[arrayList.size()];
	radioButtons = (JRadioButton[])arrayList.toArray(radioButtons);
	//	radioButtons[0].setSelected(true);
	initialButton = radioButtons[0];
	
	ButtonGroup buttonGroup = new ButtonGroup();
	view.setDisplayKey(radioButtons[0].getText());
	for(int counter = 0; counter < channelGroup.length; counter++) {
	    buttonGroup.add(radioButtons[counter]);
	    radioPanel.add(radioButtons[counter]);
	}
	radioPanel.setVisible(true);
	
	//view.repaint();
  }

    public void setInitialButton() {
	initialButton.setSelected(true);
    }


    public String getOrientationName(String orientation) {

	char ch = orientation.charAt(2);
	if(ch == 'E' || ch == '1' || ch == 'U') return "East";
	else if(ch == 'N' || ch == '2' || ch == 'V') return "North";
	else return "Up";
    }

    public void setHorizontalTitle(String name) {
	hTitleBorder.setTitle(name);
   }
    
    public void setVerticalTitle(String name) {
	vTitleBorder.setTitle(name);
  }
  

    /**
     * Describe constant <code>PARTICLE_MOTION_LAYER</code> here.
     *
     */
    public static final Integer PARTICLE_MOTION_LAYER = new Integer(2);

    protected AmpScaleMapper hAmpScaleMap, vAmpScaleMap;

    protected ScaleBorder scaleBorder;

    protected LeftTitleBorder vTitleBorder;

    protected BottomTitleBorder hTitleBorder;
    
    protected ParticleMotionView view;


    private AmpConfigRegistrar hAmpConfigRegistrar, vAmpConfigRegistrar;

    private JLayeredPane particleDisplayPanel;
    private JPanel radioPanel;
    private boolean displayButtonPanel = true;
    private AbstractButton initialButton;

    static Category logger = 
        Category.getInstance(ParticleMotionDisplay.class.getName());
    int count = 0;

    private class RadioButtonListener implements ItemListener {

	public void itemStateChanged(ItemEvent ae) {
	    if(ae.getStateChange() == ItemEvent.SELECTED) {
		System.out.println("$$&$&%$&%$&$&&$$&$&$&$&%^&$%&$^$&%&$^&$^%&$%^&$The radiobutton selected is "+ ((AbstractButton)ae.getItem()).getText());
		view.addDisplayKey(((AbstractButton)ae.getItem()).getText());
		
		setHorizontalTitle(view.getSelectedParticleMotion()[0].hseis.getSeismogram().getName());
		setVerticalTitle(view.getSelectedParticleMotion()[0].vseis.getSeismogram().getName());
		view.updateTimeRange();
	
	    } else if(ae.getStateChange() == ItemEvent.DESELECTED){
		System.out.println("The radiobutton UN SELECTED is "+ ((AbstractButton)ae.getItem()).getText());
		view.removeDisplaykey(((AbstractButton)ae.getItem()).getText());
	    }
	    //view.setDisplayKey(ae.getActionCommand());
	    repaint();
	   
	}
    }

    
}// ParticleMotionDisplay




