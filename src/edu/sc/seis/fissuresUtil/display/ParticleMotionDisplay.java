package edu.sc.seis.fissuresUtil.display;


import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.model.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;


/**
 * ParticleMotionDisplay.java
 *
 *
 * Created: Tue Jun 11 15:22:30 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class ParticleMotionDisplay extends JLayeredPane implements AmpSyncListener {
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
				  AmpRangeConfig hAmpRangeConfig,
				  AmpRangeConfig vAmpRangeConfig){
	
	this.hAmpRangeConfig = hAmpRangeConfig;
	this.vAmpRangeConfig = vAmpRangeConfig;
	
	this.setLayout(new OverlayLayout(this));
	view = new ParticleMotionView(hSeis, vSeis, hAmpRangeConfig, vAmpRangeConfig, this);
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

	this.addComponentListener(new ComponentAdapter() {
		public void componentResized(ComponentEvent e) {
		    resolveParticleMotion();
		    resize();
		}
		public void componentShown(ComponentEvent e) {
		    resize();
		}
	    });
   
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
	//System.out.println("The amplitudeRange is being updated ");
	//System.out.println("The minimum value of the updated value is "+r.getMinValue());
	//System.out.println("The maximum value of the updated vlue is "+r.getMaxValue());
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
	Insets insets =	view.getInsets();
	hAmpScaleMap.setTotalPixels(dim.width  - insets.left - insets.right);
        vAmpScaleMap.setTotalPixels(dim.height  - insets.top - insets.bottom);
        repaint();
    }
    


    public void addParticleMotionDisplay(LocalSeismogramImpl hseis,
					 LocalSeismogramImpl vseis,
					 AmpRangeConfig hAmpRangeConfig,
					 AmpRangeConfig vAmpRangeConfig) {
	view.addParticleMotionDisplay(hseis,
				      vseis,
				      hAmpRangeConfig,
				      vAmpRangeConfig);
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
        LocalSeismogramImpl hSeis = 
            (LocalSeismogramImpl)SeisPlotUtil.createSineWave(Math.PI/2, .4, 200, -1000);
        LocalSeismogramImpl vSeis = 
            (LocalSeismogramImpl)SeisPlotUtil.createSineWave(Math.PI, .8, 200, 1000);	
	LocalSeismogramImpl hSeisex = 
            (LocalSeismogramImpl)SeisPlotUtil.createSineWave(30, .6, 100, -400);
        LocalSeismogramImpl vSeisex = 
            (LocalSeismogramImpl)SeisPlotUtil.createSineWave(-265, .1, 100, 400);
	
	RMeanAmpConfig vAmpRangeConfig = new RMeanAmpConfig();
       
        final ParticleMotionDisplay sv = new ParticleMotionDisplay(hSeis, vSeis,
							  vAmpRangeConfig, 
							  vAmpRangeConfig);
        java.awt.Dimension size = new java.awt.Dimension(400, 400);
        sv.setPreferredSize(size);

	/*ParticleMotionDisplay svex = new ParticleMotionDisplay(hSeisex, vSeisex,
							  vAmpRangeConfig, 
							  vAmpRangeConfig);*/
	sv.addParticleMotionDisplay(hSeisex, vSeisex,
				    vAmpRangeConfig,
				    vAmpRangeConfig);
	//System.out.println("The min amp of second before "+
	//	vAmpRangeConfig.getAmpRange(vSeisex).getMinValue());
	//System.out.println("The max amp of second before "
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
        displayPanel.add(sv, java.awt.BorderLayout.CENTER);
	displayPanel.add(buttonPanel, java.awt.BorderLayout.SOUTH);
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
    
    
}// ParticleMotionDisplay
