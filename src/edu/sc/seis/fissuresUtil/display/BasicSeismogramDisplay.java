package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.*;
import edu.iris.Fissures.utility.Logger;
import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * BasicSeismogramDisplay.java
 *
 *
 * Created: Thu Jun  6 09:52:51 2002
 *
 * @author Charlie Groves
 * @version 0.1
 */

public class BasicSeismogramDisplay extends JComponent implements SeismogramDisplay{
    
    public BasicSeismogramDisplay(LocalSeismogram seis, boolean timeBorder){
	this(seis, new BoundedTimeConfig(), new MinMaxAmpConfig(), timeBorder);
    }
   
    public BasicSeismogramDisplay(LocalSeismogram seis, TimeRangeConfig tr, boolean timeBorder){
	this(seis, tr, new MinMaxAmpConfig(), timeBorder);
    }
    
    public BasicSeismogramDisplay(LocalSeismogram seis, AmpRangeConfig ar, boolean timeBorder){
	this(seis, new BoundedTimeConfig(), ar, timeBorder);
    }

    public BasicSeismogramDisplay(LocalSeismogram seis, TimeRangeConfig tr, AmpRangeConfig ar, boolean timeBorder){
	super();
	this.setLayout(new OverlayLayout(this));
	this.addComponentListener(new ComponentAdapter() {
		public void componentResized(ComponentEvent e) {
		    resize();
		}
		public void componentShown(ComponentEvent e) {
		    resize();
		}
	    });
        setMinimumSize(new Dimension(100, 50));
        setPreferredSize(new Dimension(200, 100));
	seismogramImage = new SeismogramImage(seismos, tr, ar);
	tr.addTimeSyncListener(this);
	ar.addAmpSyncListener(this);
	this.timeConfig = tr;
	this.ampConfig = ar;
	this.addSeismogram(seis);
	scaleBorder = new ScaleBorder();
	if(timeBorder)
	    scaleBorder.setBottomScaleMapper(timeScaleMap);
	scaleBorder.setLeftScaleMapper(ampScaleMap);        
        titleBorder = 
            new LeftTitleBorder("");
	setBorder(BorderFactory.createCompoundBorder(
		  BorderFactory.createCompoundBorder(
		  BorderFactory.createRaisedBevelBorder(),
		  titleBorder),
						     BorderFactory.createCompoundBorder(
											scaleBorder,
											BorderFactory.createLoweredBevelBorder())));
	this.add(seismogramImage);
    }

    /**
     * Adds a seismogram to the display
     *
     */
    public void addSeismogram(LocalSeismogram newSeismogram){
	seismos.put(newSeismogram, colors[seismos.size()%colors.length]);
	timeConfig.addSeismogram(newSeismogram); 
	ampConfig.addSeismogram(newSeismogram);
	seismogramImage.setRedo(true);
    }
    
    /**
     * Removes a seismogram from the display
     *
     * 
     */
    public void removeSeismogram(LocalSeismogram oldSeis){}
    
    /**
     * Returns the amp range configurator the display is using
     *
     * 
     */
    public AmpRangeConfig getAmpConfig(){ return ampConfig; }
    
    public void updateAmpRange(){
	this.seismogramImage.setRedo(true);
	this.ampScaleMap.setUnitRange(ampConfig.getAmpRange());
	repaint();
    }

    /**
     * Returns the time range configurator the display is using
     *
     * 
     */
    public TimeRangeConfig getTimeConfig(){ return timeConfig; }

    public void updateTimeRange(){

	this.timeScaleMap.setTimes(timeConfig.getTimeRange().getBeginTime(), 
				   timeConfig.getTimeRange().getEndTime());
	repaint();
    }

    public void addBottomTimeBorder(){	scaleBorder.setBottomScaleMapper(timeScaleMap); }

    public void removeBottomTimeBorder(){scaleBorder.clearBottomScaleMapper(); }

    public void addTopTimeBorder(){ scaleBorder.setTopScaleMapper(timeScaleMap); }

    public void removeTopTimeBorder(){ scaleBorder.clearTopScaleMapper(); }

    
    protected void resize() {
	Dimension dim = getSize();
        Insets insets = getInsets();
	timeScaleMap.setTotalPixels(dim.width-insets.left-insets.right);
        ampScaleMap.setTotalPixels(dim.height-insets.top-insets.bottom);
	seismogramImage.setRedo(true);
	repaint();
    }

    protected SeismogramImage seismogramImage;

    protected HashMap seismos = new HashMap();
    
    protected AmpRangeConfig ampConfig;
    
    protected TimeRangeConfig timeConfig;

    protected ScaleBorder scaleBorder;

    protected TimeScaleMapper timeScaleMap = new TimeScaleCalc(200, new MicroSecondDate(0), new MicroSecondDate(50000000));//placeholder
    
    protected AmpScaleMapper ampScaleMap = new AmpScaleMapper(50, 4, new UnitRangeImpl(0, 500, UnitImpl.COUNT));//placeholder
   
    protected LeftTitleBorder titleBorder;

    private Color[] colors = { Color.blue, Color.red, Color.yellow, Color.green, Color.black };
    
}// BasicSeismogramDisplay
