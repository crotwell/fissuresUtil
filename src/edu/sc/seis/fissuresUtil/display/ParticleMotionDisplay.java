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
    /**
     * Creates a new <code>ParticleMotionDisplay</code> instance.
     *
     * @param hSeis a <code>LocalSeismogramImpl</code> value
     * @param vSeis a <code>LocalSeismogramImpl</code> value
     * @param hAmpRangeConfig an <code>AmpRangeConfig</code> value
     * @param vAmpRangeConfig an <code>AmpRangeConfig</code> value
     */
    public ParticleMotionDisplay (DataSetSeismogram hSeis,
				  DataSetSeismogram vSeis,
				  TimeConfigRegistrar timeConfigRegistrar,
				  AmpConfigRegistrar hAmpConfigRegistrar,
				  AmpConfigRegistrar vAmpConfigRegistrar, Color color){

	this.hAmpConfigRegistrar = hAmpConfigRegistrar;
	this.vAmpConfigRegistrar = vAmpConfigRegistrar;


	showScale(hSeis, 
		  vSeis,
		  timeConfigRegistrar,
		  hAmpConfigRegistrar,
		  vAmpConfigRegistrar,
		  color, "", false);
	particleDisplayPanel.addComponentListener(new ComponentAdapter() {
		public void componentResized(ComponentEvent e) {
		    resolveParticleMotion();
		    resize();
		}
		public void componentShown(ComponentEvent e) {
		    resize();
		}
	    });
	updateTimeRange();
    }

    public void showScale(DataSetSeismogram hSeis,
			  DataSetSeismogram vSeis,
			  TimeConfigRegistrar timeConfigRegistrar,
			  AmpConfigRegistrar hAmpConfigRegistrar,
			  AmpConfigRegistrar vAmpConfigRegistrar, Color color,
			  String  key,
			  boolean horizPlane) {
	particleDisplayPanel = new JLayeredPane();
	radioPanel = new JPanel();
	OverlayLayout overlayLayout = new OverlayLayout(particleDisplayPanel);
	this.setLayout(new BorderLayout());
	particleDisplayPanel.setLayout(overlayLayout);
	radioPanel.setLayout(new GridLayout(1, 0));
	
	
	view = new ParticleMotionView(hSeis, 
				      vSeis, 
				      timeConfigRegistrar,
				      hAmpConfigRegistrar, 
				      vAmpConfigRegistrar, 
				      this,
				      color,
				      key, 
				      horizPlane);
	if(timeConfigRegistrar != null) {
	    timeConfigRegistrar.addTimeSyncListener(this);
	}
	view.setSize(new java.awt.Dimension(300, 300));
	particleDisplayPanel.add(view, PARTICLE_MOTION_LAYER);
	hAmpScaleMap = new AmpScaleMapper(50,
                                          4,
					  hAmpConfigRegistrar.getAmpRange(hSeis));
        vAmpScaleMap = new AmpScaleMapper(50,
                                          4,
					  vAmpConfigRegistrar.getAmpRange(vSeis));
	scaleBorder = new ScaleBorder();
	scaleBorder.setBottomScaleMapper(hAmpScaleMap);
	scaleBorder.setLeftScaleMapper(vAmpScaleMap);        
        hTitleBorder = 
            new CenterTitleBorder(hSeis.getSeismogram().getName());
        vTitleBorder = 
            new CenterTitleBorder(vSeis.getSeismogram().getName());
	particleDisplayPanel.setBorder(BorderFactory.createCompoundBorder(
						     //	     BorderFactory.createCompoundBorder(
			 BorderFactory.createRaisedBevelBorder(),
			 //hTitleBorder),
			 //BorderFactory.createCompoundBorder(hTitleBorder,
			 //				    vTitleBorder)),
	    		     BorderFactory.createCompoundBorder(
								scaleBorder,
								BorderFactory.createLoweredBevelBorder()))
	  );

	
	add(particleDisplayPanel, BorderLayout.CENTER);
	add(radioPanel, BorderLayout.SOUTH);
	
    }

    public ParticleMotionDisplay (DataSetSeismogram hSeis,
				  DataSetSeismogram vSeis,
				  TimeConfigRegistrar timeConfigRegistrar,
				  AmpConfigRegistrar hAmpConfigRegistrar,
				  AmpConfigRegistrar vAmpConfigRegistrar){
	this(hSeis, vSeis, timeConfigRegistrar, hAmpConfigRegistrar, vAmpConfigRegistrar, null);
    }



    public ParticleMotionDisplay(DataSetSeismogram hseis,
				 DataSetSeismogram vseis,
				 TimeConfigRegistrar timeConfigRegistrar,
				 AmpConfigRegistrar ampRangeConfig) {

	this(hseis, vseis, timeConfigRegistrar, ampRangeConfig, ampRangeConfig);
    }

    public ParticleMotionDisplay(DataSetSeismogram hseis,
				 DataSetSeismogram vseis, 
				 TimeConfigRegistrar timeConfigRegistrar) {

	//	AmpConfigRegistrar ampConfig = new RMeanAmpConfig();
	this(hseis, vseis, timeConfigRegistrar, new AmpConfigRegistrar());
    }

    public ParticleMotionDisplay(DataSetSeismogram[] seis,
				 TimeConfigRegistrar timeConfigRegistrar,
				 AmpConfigRegistrar hAmpConfigRegistrar,
				 AmpConfigRegistrar vAmpConfigRegistrar) {
	
	
	ChannelId[] channelGroup = new ChannelId[3];
	
      	edu.iris.Fissures.Time startTime;
	edu.iris.Fissures.Time endTime;
	if(seis.length < 2) return;
	DataSetSeismogram[] seismograms = new DataSetSeismogram[3];
	if(timeConfigRegistrar != null) {
	    startTime = timeConfigRegistrar.getTimeRange().getBeginTime().getFissuresTime();
	    endTime = timeConfigRegistrar.getTimeRange().getEndTime().getFissuresTime();
	} else {
	    startTime = seis[0].getSeismogram().getBeginTime().getFissuresTime();
	    endTime = seis[0].getSeismogram().getEndTime().getFissuresTime();
	}

	for(int counter = 0; counter < seis.length; counter++) {
	    
	    seismograms[counter] = seis[counter];
	    channelGroup[counter] = seis[counter].getSeismogram().getChannelID();
	}
								     
	this.hAmpConfigRegistrar = hAmpConfigRegistrar;
	this.vAmpConfigRegistrar = vAmpConfigRegistrar;
	
	showScale(seismograms[0], 
	     seismograms[1], 
	     timeConfigRegistrar, 
	     hAmpConfigRegistrar, 
	     vAmpConfigRegistrar, 
	     null, channelGroup[0].channel_code+"-"+channelGroup[1].channel_code, false);
	formRadioSetPanel(channelGroup);
	particleDisplayPanel.addComponentListener(new ComponentAdapter() {
		public void componentResized(ComponentEvent e) {
		    resolveParticleMotion();
		    resize();
		}
		public void componentShown(ComponentEvent e) {
		    resize();
		}
	    });
	updateTimeRange();
	if(seismograms.length == 3) {
	    System.out.println(" ADDED THe first seismograme ");
	    addParticleMotionDisplay(seismograms[1], 
				     seismograms[2], 
				     timeConfigRegistrar, 
				     hAmpConfigRegistrar, 
				     vAmpConfigRegistrar, 
				     null,
				     channelGroup[1].channel_code+"-"+channelGroup[2].channel_code,false);
	    System.out.println(" ADDED he second SEismograme");
	    addParticleMotionDisplay(seismograms[0], 
				     seismograms[2], 
				     timeConfigRegistrar, 
				     hAmpConfigRegistrar, 
				     vAmpConfigRegistrar, 
				     null,
				     channelGroup[0].channel_code+"-"+channelGroup[2].channel_code, false);
	    System.out.println("Added the third display ");
	}
	    
    }


    public ParticleMotionDisplay(DataSetSeismogram hseis,
				 TimeConfigRegistrar timeConfigRegistrar,
				 AmpConfigRegistrar hAmpConfigRegistrar,
				 AmpConfigRegistrar vAmpConfigRegistrar,
				 boolean advancedOption) {
	LocalSeismogramImpl seis = hseis.getSeismogram();
	ChannelId[] channelIds = ((edu.sc.seis.fissuresUtil.xml.XMLDataSet)hseis.getDataSet()).getChannelIds();
	ChannelGrouperImpl channelProxy = new ChannelGrouperImpl();
	logger.debug("the original channel_code from the seismogram is "+seis.getChannelID().channel_code);
	ChannelId[] channelGroup = channelProxy.retrieve_grouping(channelIds, seis.getChannelID());
	System.out.println("THe length of the channel group is "+channelGroup.length);
	edu.iris.Fissures.Time startTime;
	edu.iris.Fissures.Time endTime;
	DataSetSeismogram[] seismograms = new DataSetSeismogram[3];
	if(timeConfigRegistrar != null) {
	    startTime = timeConfigRegistrar.getTimeRange().getBeginTime().getFissuresTime();
	    endTime = timeConfigRegistrar.getTimeRange().getEndTime().getFissuresTime();
	} else {
	    startTime = seis.getBeginTime().getFissuresTime();
	    endTime = seis.getEndTime().getFissuresTime();
	}
	System.out.println("Start Time is "+new MicroSecondDate(startTime));
	System.out.println("end Time is "+new MicroSecondDate(endTime));
	try {
	    for(int counter = 0; counter < channelGroup.length; counter++) {
		
		logger.debug("The **** seismogram name is "+ChannelIdUtil.toStringNoDates(channelGroup[counter]));
		
		seismograms[counter] = new DataSetSeismogram(hseis.getDataSet().
							     getSeismogram(DisplayUtils.getSeismogramName(channelGroup[counter], 
											     hseis.getDataSet(),
											     new edu.iris.Fissures.TimeRange(seis.getBeginTime().getFissuresTime(), seis.getEndTime().getFissuresTime()))), hseis.getDataSet());
		//ChannelIdUtil.toStringNoDates(channelGroup[counter]));
		timeConfigRegistrar.addSeismogram(seismograms[counter]);
		//hAmpRangeConfigRegistrar.addSeismogram(seismograms
		if(seismograms[counter] == null) 
		    logger.debug(" seismograms["+counter+"] is NULL");
		else 
		    logger.debug(" seismograms["+counter+"] is NOT NULL");
	    }
	    
	} catch(Exception e) {
	    
	    e.printStackTrace();//strack trace
	}
	this.hAmpConfigRegistrar = hAmpConfigRegistrar;
	this.vAmpConfigRegistrar = vAmpConfigRegistrar;
	boolean horizPlane = isHorizontalPlane(seismograms[0].getSeismogram().getChannelID(),
					       seismograms[1].getSeismogram().getChannelID(),
					       hseis.getDataSet());
	showScale(seismograms[0], 
		  seismograms[1], 
		  timeConfigRegistrar, 
		  hAmpConfigRegistrar, 
		  vAmpConfigRegistrar, 
		  null, 
		  channelGroup[0].channel_code+"-"+channelGroup[1].channel_code,
		  horizPlane
		  );
	if(!advancedOption) {
	    formRadioSetPanel(channelGroup);
	} else {
	    formCheckBoxPanel(channelGroup);
	}
	displayBackAzimuth(hseis.getDataSet(), channelGroup[0]);

	particleDisplayPanel.addComponentListener(new ComponentAdapter() {
		public void componentResized(ComponentEvent e) {
		    resolveParticleMotion();
		    resize();
		}
		public void componentShown(ComponentEvent e) {
		    resize();
		}
	    });
	updateTimeRange();
	System.out.println(" ADDED THe first seismograme ");
	horizPlane = isHorizontalPlane(seismograms[1].getSeismogram().getChannelID(),
				       seismograms[2].getSeismogram().getChannelID(),
				       hseis.getDataSet());
	addParticleMotionDisplay(seismograms[1], 
				 seismograms[2], 
				 timeConfigRegistrar, 
				 hAmpConfigRegistrar, 
				 vAmpConfigRegistrar, 
				 null,
				 channelGroup[1].channel_code+"-"+channelGroup[2].channel_code,
				 horizPlane);
	System.out.println(" ADDED he second SEismograme");
	horizPlane = isHorizontalPlane(seismograms[0].getSeismogram().getChannelID(),
				  seismograms[2].getSeismogram().getChannelID(),
				  hseis.getDataSet());	
	addParticleMotionDisplay(seismograms[0], 
				 seismograms[2], 
				 timeConfigRegistrar, 
				 hAmpConfigRegistrar, 
				 vAmpConfigRegistrar, 
				 null,
				 channelGroup[0].channel_code+"-"+channelGroup[2].channel_code,
				 horizPlane);
	System.out.println("Added the third display ");
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



    public void resolveParticleMotion() {

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
    public void resize() {
	    
	Dimension dim = view.getSize();
	logger.debug("view coordinates before width = "+view.getSize().width+" height = "+view.getSize().height);
	Insets insets =	view.getInsets();
	int width = particleDisplayPanel.getSize().width;
	int height = particleDisplayPanel.getSize().height;
	width = width - particleDisplayPanel.getInsets().left - particleDisplayPanel.getInsets().right;
	height = height - particleDisplayPanel.getInsets().top - particleDisplayPanel.getInsets().bottom;
	if(width < height) {
	    
	    particleDisplayPanel.setSize(new Dimension(particleDisplayPanel.getSize().width,
				       width + particleDisplayPanel.getInsets().top + particleDisplayPanel.getInsets().bottom));
	   
	    
	} else {
	    particleDisplayPanel.setSize(new Dimension(height  + particleDisplayPanel.getInsets().left + particleDisplayPanel.getInsets().right,
				       particleDisplayPanel.getSize().height));

	}
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
    }
    
    public void addParticleMotionDisplay(DataSetSeismogram hseis,
					  TimeConfigRegistrar timeConfigRegistrar,
					  AmpConfigRegistrar hAmpConfigRegistrar,
					  AmpConfigRegistrar vAmpConfigRegistrar) {
	LocalSeismogramImpl seis = hseis.getSeismogram();
	ChannelId[] channelIds = ((edu.sc.seis.fissuresUtil.xml.XMLDataSet)hseis.getDataSet()).getChannelIds();
	ChannelGrouperImpl channelProxy = new ChannelGrouperImpl();
	logger.debug("the original channel_code from the seismogram is "+ seis.getChannelID().channel_code);
	ChannelId[] channelGroup = channelProxy.retrieve_grouping(channelIds, seis.getChannelID());
	System.out.println("THe length of the channel group is "+channelGroup.length);
	edu.iris.Fissures.Time startTime;
	edu.iris.Fissures.Time endTime;
	DataSetSeismogram[] seismograms = new DataSetSeismogram[3];
	if(timeConfigRegistrar != null) {
	    startTime = timeConfigRegistrar.getTimeRange().getBeginTime().getFissuresTime();
	    endTime = timeConfigRegistrar.getTimeRange().getEndTime().getFissuresTime();
	} else {
	    startTime = seis.getBeginTime().getFissuresTime();
	    endTime = seis.getEndTime().getFissuresTime();
	}
	System.out.println("Start Time is "+new MicroSecondDate(startTime));
	System.out.println("end Time is "+new MicroSecondDate(endTime));
	try {
	    for(int counter = 0; counter < channelGroup.length; counter++) {
		String name = DisplayUtils.getSeismogramName(channelGroup[counter], hseis.getDataSet(), 
							     new edu.iris.Fissures.TimeRange(seis.getBeginTime().getFissuresTime(), 
											     seis.getEndTime().getFissuresTime()));
		seismograms[counter] = new DataSetSeismogram(hseis.getDataSet().getSeismogram(name), hseis.getDataSet());
		timeConfigRegistrar.addSeismogram(seismograms[counter]);
		//hAmpRangeConfigRegistrar.addSeismogram(seismograms
		if(seismograms[counter] == null) 
		    logger.debug(" seismograms["+counter+"] is NULL");
		else 
		    logger.debug(" seismograms["+counter+"] is NOT NULL");
	    }
								     
	 } catch(Exception e) {
	    
	     e.printStackTrace();
	 }
	boolean horizPlane = isHorizontalPlane( ((LocalSeismogramImpl)seismograms[0].getSeismogram()).getChannelID(),
					   ((LocalSeismogramImpl)seismograms[1].getSeismogram()).getChannelID(),
					   hseis.getDataSet());	
	addParticleMotionDisplay(seismograms[0], 
				 seismograms[1], 
				 timeConfigRegistrar, 
				 hAmpConfigRegistrar, 
				 vAmpConfigRegistrar, 
				 null,
				 channelGroup[0].channel_code+"-"+channelGroup[1].channel_code,
				 horizPlane);
	//updateTimeRange();
	System.out.println(" ADDED THe first seismograme ");
	horizPlane = isHorizontalPlane(((LocalSeismogramImpl)seismograms[1].getSeismogram()).getChannelID(),
				  ((LocalSeismogramImpl)seismograms[2].getSeismogram()).getChannelID(),
				  hseis.getDataSet());	
	addParticleMotionDisplay(seismograms[1], 
				 seismograms[2], 
				 timeConfigRegistrar, 
				 hAmpConfigRegistrar, 
				 vAmpConfigRegistrar, 
				 null,
				 channelGroup[1].channel_code+"-"+channelGroup[2].channel_code,
				 horizPlane);
	System.out.println(" ADDED he second SEismograme");
	horizPlane = isHorizontalPlane(((LocalSeismogramImpl)seismograms[0].getSeismogram()).getChannelID(),
				  ((LocalSeismogramImpl)seismograms[2].getSeismogram()).getChannelID(),
				  hseis.getDataSet());
	addParticleMotionDisplay(seismograms[0], 
				 seismograms[2], 
				 timeConfigRegistrar, 
				 hAmpConfigRegistrar, 
				 vAmpConfigRegistrar, 
				 null,
				 channelGroup[0].channel_code+"-"+channelGroup[2].channel_code,
				 horizPlane);
	System.out.println("Added the third display ");

    }

    public void addParticleMotionDisplay(DataSetSeismogram hseis,
					 DataSetSeismogram vseis,
					 TimeConfigRegistrar timeConfigRegistrar,
					 AmpConfigRegistrar hAmpConfigRegistrar,
					 AmpConfigRegistrar vAmpConfigRegistrar, Color color, 
					 String key,
					 boolean horizPlane) {

	view.addParticleMotionDisplay(hseis,
				      vseis,
				      timeConfigRegistrar,
				      hAmpConfigRegistrar,
				      vAmpConfigRegistrar,
				      color, key,
				      horizPlane);
	if(timeConfigRegistrar != null) {
	    timeConfigRegistrar.addTimeSyncListener(this);
	}
    }
    
    public void addParticleMotionDisplay(DataSetSeismogram hseis,
					 DataSetSeismogram vseis,
					 TimeConfigRegistrar timeConfigRegistrar,
					 AmpConfigRegistrar hAmpConfigRegistrar,
					 AmpConfigRegistrar vAmpConfigRegistrar) {
	
	addParticleMotionDisplay(hseis, vseis,
				 timeConfigRegistrar,
				 hAmpConfigRegistrar,
				 vAmpConfigRegistrar,
				 null, "", false);
    }


    public void displayBackAzimuth(edu.sc.seis.fissuresUtil.xml.DataSet dataset, ChannelId chanId) {
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
    public void setAmplitudeRange(AmpConfigRegistrar amplitudeRange) {
	
	this.hAmpConfigRegistrar = amplitudeRange;
	this.vAmpConfigRegistrar = amplitudeRange;
    }

    public void addAzimuthLine(double degrees) {

	view.addAzimuthLine(degrees);
    }

   public boolean isHorizontalPlane(ChannelId channelIdone, 
			       ChannelId channelIdtwo,
			       edu.sc.seis.fissuresUtil.xml.DataSet dataset) {

	Channel channelOne = ((edu.sc.seis.fissuresUtil.xml.XMLDataSet)dataset).getChannel(channelIdone);
	Channel channelTwo = ((edu.sc.seis.fissuresUtil.xml.XMLDataSet)dataset).getChannel(channelIdtwo);
	if(channelOne.an_orientation.dip == 90 &&
	   channelTwo.an_orientation.dip == 90)  {
	   return true; 
	}
	return false;
   }	

    public void addSector(double degreeone, double degreetwo) {

	view.addSector(degreeone, degreetwo);
    }

    public void setZoomIn(boolean value) {

	view.setZoomIn(value);
    }

    public void setZoomOut(boolean value) {

	view.setZoomOut(value);
    }

    public void fireAmpRangeEvent(AmpSyncEvent event) {

	this.hAmpConfigRegistrar.fireAmpRangeEvent(event);
    }

    public void updateTimeRange() {

	view.updateTimeRange();
    }

    public void formCheckBoxPanel(ChannelId[] channelGroup) {
	ArrayList arrayList = new ArrayList();
	for(int counter = 0; counter < channelGroup.length; counter++) {
	    for(int subcounter = counter+1; subcounter < channelGroup.length; subcounter++) {
		String labelStr = channelGroup[counter].channel_code+"-"+channelGroup[subcounter].channel_code;
		JCheckBox radioButton = new JCheckBox(labelStr);
		radioButton.setActionCommand(labelStr);
		radioButton.addItemListener(new RadioButtonListener());
		arrayList.add(radioButton);
	    }
	}
	JCheckBox[] checkBoxes = new JCheckBox[arrayList.size()];
	checkBoxes = (JCheckBox[])arrayList.toArray(checkBoxes);
	checkBoxes[0].setSelected(true);
	
	view.setDisplayKey(checkBoxes[0].getText());
	for(int counter = 0; counter < channelGroup.length; counter++) {
	    radioPanel.add(checkBoxes[counter]);
	}
    }

    public void formRadioSetPanel(ChannelId[] channelGroup) {
	ArrayList arrayList = new ArrayList();
	for(int counter = 0; counter < channelGroup.length; counter++) {
	    for(int subcounter = counter + 1; subcounter < channelGroup.length; subcounter++) {
		String labelStr = channelGroup[counter].channel_code+"-"+channelGroup[subcounter].channel_code;
		JRadioButton radioButton = new JRadioButton(labelStr);
		radioButton.setActionCommand(labelStr);
		radioButton.addItemListener(new RadioButtonListener());
		arrayList.add(radioButton);
	    }
	}

	JRadioButton[] radioButtons = new JRadioButton[arrayList.size()];
	radioButtons = (JRadioButton[])arrayList.toArray(radioButtons);
	radioButtons[0].setSelected(true);
	
	ButtonGroup buttonGroup = new ButtonGroup();
	
	view.setDisplayKey(radioButtons[0].getText());
	for(int counter = 0; counter < channelGroup.length; counter++) {
	    buttonGroup.add(radioButtons[counter]);
	    radioPanel.add(radioButtons[counter]);
	}
    }

    /**
     * Describe <code>main</code> method here.
     *
     * @param args a <code>String[]</code> value
     */
    public static void main(String[] args) {
	/* JFrame jf = new JFrame("Test Particle Motion View");
	JPanel displayPanel = new JPanel();
	JButton zoomIn = new JButton("zoomIn");
	JButton zoomOut = new JButton("zoomOut");
	JPanel buttonPanel = new JPanel();
	buttonPanel.setLayout(new FlowLayout());
	buttonPanel.add(zoomIn);
	buttonPanel.add(zoomOut);
        //        Seismogram hSeis = SeisPlotUtil.createTestData();
        //        Seismogram vSeis = SeisPlotUtil.createTestData();
        LocalSeismogramImpl hSeis =  (LocalSeismogramImpl)SeisPlotUtil.createCustomSineWave();
            //(LocalSeismogramImpl)SeisPlotUtil.createSineWave(Math.PI/2, .4, 200, -1000);
	
        LocalSeismogramImpl vSeis = 
            (LocalSeismogramImpl)SeisPlotUtil.createSineWave(Math.PI, .8, 200, 1000);	
	LocalSeismogramImpl hSeisex = 
            (LocalSeismogramImpl)SeisPlotUtil.createSineWave(30, .6, 100, -400);
        LocalSeismogramImpl vSeisex = 
            (LocalSeismogramImpl)SeisPlotUtil.createSineWave(-265, .1, 100, 400);
	
	AmpConfigRegistrar vAmpConfigRegistrar = new AmpConfigRegistrar();
       
        final ParticleMotionDisplay sv = new ParticleMotionDisplay(hSeis, hSeis,
								   null,
								   vAmpConfigRegistrar, 
								   vAmpConfigRegistrar);
        java.awt.Dimension size = new java.awt.Dimension(400, 400);
        sv.setPreferredSize(size);

	ParticleMotionDisplay svex = new ParticleMotionDisplay(hSeisex, vSeisex,
							  vAmpConfigRegistrar, 
							  vAmpConfigRegistrar);
	sv.addParticleMotionDisplay(hSeisex, vSeisex,null,
				    vAmpConfigRegistrar,
				    vAmpConfigRegistrar);
	//logger.debug("The min amp of second before "+
	//	vAmpConfigRegistrar.getAmpRange(vSeisex).getMinValue());
	//logger.debug("The max amp of second before "
	///+vAmpConfigRegistrar.getAmpRange(vSeisex).getMaxValue());
	//sv.addAzimuthLine(30);
	//sv.addAzimuthLine(60);
	//sv.addAzimuthLine(90);
	sv.addAzimuthLine(15);
	//sv.addAzimuthLine(0);
	sv.addSector(10, 20);
	//	sv.addSector(-30, -60);
	//sv.addAzimuthLine(-40);
	//	sv.addAzimuthLine(80);
	displayPanel.setLayout(new BorderLayout());
	sv.setSize(size);

        displayPanel.add(sv, java.awt.BorderLayout.CENTER);
	displayPanel.add(buttonPanel, java.awt.BorderLayout.SOUTH);
	displayPanel.add(new JPanel(), java.awt.BorderLayout.EAST);
	displayPanel.setSize(size);
	jf.getContentPane().add(displayPanel);
        jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jf.setSize(size);
        jf.setVisible(true);
        jf.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosed(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });

	zoomIn.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {

		    sv.setZoomIn(true);
		    // sv.setZoomOut(false);
		}
	    });
	zoomOut.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {

		    sv.setZoomOut(true);
		    // sv.setZoomIn(false);
		}
	    });
*/
	
    }
    

    /**
     * Describe constant <code>PARTICLE_MOTION_LAYER</code> here.
     *
     */
    public static final Integer PARTICLE_MOTION_LAYER = new Integer(2);

    protected AmpScaleMapper hAmpScaleMap, vAmpScaleMap;

    protected ScaleBorder scaleBorder;

    protected CenterTitleBorder hTitleBorder, vTitleBorder;
    
    protected ParticleMotionView view;


    private AmpConfigRegistrar hAmpConfigRegistrar, vAmpConfigRegistrar;

    private JLayeredPane particleDisplayPanel;
    private JPanel radioPanel;

    static Category logger = 
        Category.getInstance(ParticleMotionDisplay.class.getName());
    int count = 0;

    private class RadioButtonListener implements ItemListener {

	public void itemStateChanged(ItemEvent ae) {
	    if(ae.getStateChange() == ItemEvent.SELECTED) {
		System.out.println("The radiobutton selected is "+ ((AbstractButton)ae.getItem()).getText());
		view.addDisplayKey(((AbstractButton)ae.getItem()).getText());
	    } else if(ae.getStateChange() == ItemEvent.DESELECTED){
		System.out.println("The radiobutton UN SELECTED is "+ ((AbstractButton)ae.getItem()).getText());
		view.removeDisplaykey(((AbstractButton)ae.getItem()).getText());
	    }
	    //view.setDisplayKey(ae.getActionCommand());
	    repaint();
	   
	}
    }

    
}// ParticleMotionDisplay




/*********************
 public void resize() {
	    
	Dimension dim = view.getSize();
	logger.debug("view coordinates before width = "+view.getSize().width+" height = "+view.getSize().height);
	Insets insets =	view.getInsets();
	int width = super.getSize().width;
	int height = super.getSize().height;
	width = width - super.getInsets().left - super.getInsets().right;
	height = height - super.getInsets().top - super.getInsets().bottom;
	if(width < height) {
	    
	    particleDisplayPanel.setSize(new Dimension(super.getSize().width,
				       width + super.getInsets().top + super.getInsets().bottom));
	   
	    
	} else {
	    particleDisplayPanel.setSize(new Dimension(height  + super.getInsets().left + super.getInsets().right,
				       super.getSize().height));

	}
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
    }

**********/
