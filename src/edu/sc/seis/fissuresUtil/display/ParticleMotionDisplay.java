package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.chooser.*;
import edu.sc.seis.fissuresUtil.namingService.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.model.*;

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

public class ParticleMotionDisplay extends JLayeredPane implements AmpSyncListener, TimeSyncListener {
    /**
     * Creates a new <code>ParticleMotionDisplay</code> instance.
     *
     * @param hSeis a <code>LocalSeismogramImpl</code> value
     * @param vSeis a <code>LocalSeismogramImpl</code> value
     * @param hAmpRangeConfig an <code>AmpRangeConfig</code> value
     * @param vAmpRangeConfig an <code>AmpRangeConfig</code> value
     */
    public ParticleMotionDisplay (LocalSeismogramImpl hSeis,
				  LocalSeismogramImpl vSeis,
				  TimeRangeConfig timeRangeConfig,
				  AmpRangeConfig hAmpRangeConfig,
				  AmpRangeConfig vAmpRangeConfig, Color color){

	this.hAmpRangeConfig = hAmpRangeConfig;
	this.vAmpRangeConfig = vAmpRangeConfig;

	this.setLayout(new OverlayLayout(this));
	showScale(hSeis, 
		  vSeis,
		  timeRangeConfig,
		  hAmpRangeConfig,
		  vAmpRangeConfig,
		  color);
	this.addComponentListener(new ComponentAdapter() {
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

    public void showScale(LocalSeismogramImpl hSeis,
			  LocalSeismogramImpl vSeis,
			  TimeRangeConfig timeRangeConfig,
			  AmpRangeConfig hAmpRangeConfig,
			  AmpRangeConfig vAmpRangeConfig, Color color) {
	view = new ParticleMotionView(hSeis, 
				      vSeis, 
				      timeRangeConfig,
				      hAmpRangeConfig, 
				      vAmpRangeConfig, 
				      this,
				      color);
	if(timeRangeConfig != null) {
	    timeRangeConfig.addTimeSyncListener(this);
	}
	view.setSize(new java.awt.Dimension(300, 300));
	add(view, PARTICLE_MOTION_LAYER);
	hAmpScaleMap = new AmpScaleMapper(50,
                                          4,
					  hAmpRangeConfig.getAmpRange(hSeis));
        vAmpScaleMap = new AmpScaleMapper(50,
                                          4,
					  vAmpRangeConfig.getAmpRange(vSeis));
	scaleBorder = new ScaleBorder();
	scaleBorder.setBottomScaleMapper(hAmpScaleMap);
	scaleBorder.setLeftScaleMapper(vAmpScaleMap);        
        hTitleBorder = 
            new CenterTitleBorder(((LocalSeismogramImpl)hSeis).getName());
        vTitleBorder = 
            new CenterTitleBorder(((LocalSeismogramImpl)vSeis).getName());
	setBorder(BorderFactory.createCompoundBorder(
						     //	     BorderFactory.createCompoundBorder(
			 BorderFactory.createRaisedBevelBorder(),
			 //hTitleBorder),
			 //BorderFactory.createCompoundBorder(hTitleBorder,
			 //				    vTitleBorder)),
	    		     BorderFactory.createCompoundBorder(
								scaleBorder,
								BorderFactory.createLoweredBevelBorder()))
	  );

	
	
	
    }

    public ParticleMotionDisplay (LocalSeismogramImpl hSeis,
				  LocalSeismogramImpl vSeis,
				  TimeRangeConfig timeRangeConfig,
				  AmpRangeConfig hAmpRangeConfig,
				  AmpRangeConfig vAmpRangeConfig){
	this(hSeis, vSeis, timeRangeConfig, hAmpRangeConfig, vAmpRangeConfig, null);
    }



    public ParticleMotionDisplay(LocalSeismogramImpl hseis,
				 LocalSeismogramImpl vseis,
				 TimeRangeConfig timeRangeConfig,
				 AmpRangeConfig ampRangeConfig) {

	this(hseis, vseis, timeRangeConfig, ampRangeConfig, ampRangeConfig);
    }

    public ParticleMotionDisplay(LocalSeismogramImpl hseis,
				 LocalSeismogramImpl vseis, 
				 TimeRangeConfig timeRangeConfig) {

	//	AmpRangeConfig ampConfig = new RMeanAmpConfig();
	this(hseis, vseis, timeRangeConfig, new RMeanAmpConfig());
    }


    public ParticleMotionDisplay(LocalSeismogramImpl hseis,
				 TimeRangeConfig timeRangeConfig,
				 org.omg.CORBA_2_3.ORB orb) {

	ChannelProxy channelProxy = new ChannelProxy();
	Channel[] channelGroup = channelProxy.retrieve_grouping(orb, hseis.getChannelID());
	FissuresNamingServiceImpl fissuresNamingServiceImpl = null;
	edu.iris.Fissures.Time startTime;
	edu.iris.Fissures.Time endTime;
	LocalSeismogram[] seismograms = new LocalSeismogram[3];
	if(timeRangeConfig != null) {
	    startTime = timeRangeConfig.getTimeRange().getBeginTime().getFissuresTime();
	    endTime = timeRangeConfig.getTimeRange().getEndTime().getFissuresTime();
	} else {
	    startTime = hseis.getBeginTime().getFissuresTime();
	    endTime = hseis.getEndTime().getFissuresTime();
	}
	try {
	    fissuresNamingServiceImpl = new FissuresNamingServiceImpl(orb);
	    DataCenter dataCenter = fissuresNamingServiceImpl.getSeismogramDC("edu/sc/seis", "SCEPPSeismogramDC");
	    for(int counter = 0; counter < channelGroup.length; counter++) {
		LocalSeismogram[] localSeismograms = retreiveSeismograms(startTime,
									 endTime,
									 channelGroup[counter].get_id(),
									 dataCenter);
		seismograms[counter] = localSeismograms[0];
	    }
								     
	 } catch(Exception e) {
	    
	     e.printStackTrace();
	}
	AmpRangeConfig ampRangeConfig = new RMeanAmpConfig();
	this.hAmpRangeConfig = ampRangeConfig;
	this.vAmpRangeConfig = ampRangeConfig;
	showScale((LocalSeismogramImpl)seismograms[0], 
	     (LocalSeismogramImpl)seismograms[1], 
	     timeRangeConfig, 
	     ampRangeConfig, 
	     ampRangeConfig, 
	     null);
	this.addComponentListener(new ComponentAdapter() {
		public void componentResized(ComponentEvent e) {
		    resolveParticleMotion();
		    resize();
		}
		public void componentShown(ComponentEvent e) {
		    resize();
		}
	    });
	updateTimeRange();
	addParticleMotionDisplay((LocalSeismogramImpl)seismograms[1], 
	     (LocalSeismogramImpl)seismograms[2], 
	     timeRangeConfig, 
	     ampRangeConfig, 
	     ampRangeConfig, 
	     null);
	addParticleMotionDisplay((LocalSeismogramImpl)seismograms[0], 
				 (LocalSeismogramImpl)seismograms[2], 
				 timeRangeConfig, 
				 ampRangeConfig, 
				 ampRangeConfig, 
				 null);

	
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
	this.hAmpScaleMap.setUnitRange(hAmpRangeConfig.getAmpRange());
    }
    
    public void updateVerticalAmpRange() {
	this.vAmpScaleMap.setUnitRange(vAmpRangeConfig.getAmpRange());
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
	int width = super.getSize().width;
	int height = super.getSize().height;
	width = width - super.getInsets().left - super.getInsets().right;
	height = height - super.getInsets().top - super.getInsets().bottom;
	if(width < height) {
	    
	    this.setSize(new Dimension(super.getSize().width,
				       width + super.getInsets().top + super.getInsets().bottom));
	   
	    
	} else {
	    this.setSize(new Dimension(height  + super.getInsets().left + super.getInsets().right,
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
    


    public void addParticleMotionDisplay(LocalSeismogramImpl hseis,
					 LocalSeismogramImpl vseis,
					 TimeRangeConfig timeRangeConfig,
					 AmpRangeConfig hAmpRangeConfig,
					 AmpRangeConfig vAmpRangeConfig, Color color) {

	view.addParticleMotionDisplay(hseis,
				      vseis,
				      timeRangeConfig,
				      hAmpRangeConfig,
				      vAmpRangeConfig,
				      color);
	if(timeRangeConfig != null) {
	    timeRangeConfig.addTimeSyncListener(this);
	}
    }
    
    public void addParticleMotionDisplay(LocalSeismogramImpl hseis,
					 LocalSeismogramImpl vseis,
					 TimeRangeConfig timeRangeConfig,
					 AmpRangeConfig hAmpRangeConfig,
					 AmpRangeConfig vAmpRangeConfig) {
	
	addParticleMotionDisplay(hseis, vseis,
				 timeRangeConfig,
				 hAmpRangeConfig,
				 vAmpRangeConfig,
				 null);
    }
    /**
     * sets the AmplitudeRange of the ParticleMotionDisplay.
     *
     * @param amplitudeRange an <code>AmpRangeConfig</code> value
     */
    public void setAmplitudeRange(AmpRangeConfig amplitudeRange) {
	
	this.hAmpRangeConfig = amplitudeRange;
	this.vAmpRangeConfig = amplitudeRange;
    }

    public void addAzimuthLine(double degrees) {

	view.addAzimuthLine(degrees);
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

	this.hAmpRangeConfig.fireAmpRangeEvent(event);
    }

    public void updateTimeRange() {

	view.updateTimeRange();
    }

    /**
     * Describe <code>main</code> method here.
     *
     * @param args a <code>String[]</code> value
     */
    public static void main(String[] args) {
        JFrame jf = new JFrame("Test Particle Motion View");
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
	
	RMeanAmpConfig vAmpRangeConfig = new RMeanAmpConfig();
       
        final ParticleMotionDisplay sv = new ParticleMotionDisplay(hSeis, hSeis,
								   null,
								   vAmpRangeConfig, 
								   vAmpRangeConfig);
        java.awt.Dimension size = new java.awt.Dimension(400, 400);
        sv.setPreferredSize(size);

	/*ParticleMotionDisplay svex = new ParticleMotionDisplay(hSeisex, vSeisex,
							  vAmpRangeConfig, 
							  vAmpRangeConfig);*/
	sv.addParticleMotionDisplay(hSeisex, vSeisex,null,
				    vAmpRangeConfig,
				    vAmpRangeConfig);
	//logger.debug("The min amp of second before "+
	//	vAmpRangeConfig.getAmpRange(vSeisex).getMinValue());
	//logger.debug("The max amp of second before "
	///+vAmpRangeConfig.getAmpRange(vSeisex).getMaxValue());
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


    private AmpRangeConfig hAmpRangeConfig, vAmpRangeConfig;


    static Category logger = 
        Category.getInstance(ParticleMotionDisplay.class.getName());
    int count = 0;
    
}// ParticleMotionDisplay
