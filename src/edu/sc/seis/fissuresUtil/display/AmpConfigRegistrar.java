package edu.sc.seis.fissuresUtil.display;

import java.util.LinkedList;
import java.util.Iterator;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import org.apache.log4j.*;

/**
 * AmpConfigRegistrar.java
 *
 *
 * Created: Mon Jul  1 13:55:56 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class AmpConfigRegistrar implements AmpRangeConfig, AmpSyncListener{
    public AmpConfigRegistrar(){
	ampConfig = new RMeanAmpConfig();
	ampConfig.setRegistrar(this);
    }

    public AmpConfigRegistrar (AmpRangeConfig ampConfig){
	this.ampConfig = ampConfig;
	ampConfig.setRegistrar(this);
    }

    public AmpConfigRegistrar(AmpConfigRegistrar ampRegistrar){
	setRegistrar(ampRegistrar);
    }
	
     
    public void setRegistrar(AmpConfigRegistrar ampRegistrar){ 
	if(ampConfig instanceof AmpConfigRegistrar)
	    ((AmpConfigRegistrar)ampConfig).removeAmpSyncListener(this);
	Iterator e = seismograms.iterator();
	while(e.hasNext())
	    ampRegistrar.addSeismogram(((DataSetSeismogram)e.next()));
	ampRegistrar.addAmpSyncListener(this);
	ampConfig = ampRegistrar;
	updateAmpSyncListeners();
    }

    public AmpRangeConfig getRegistrar(){ return ampConfig; }

    /**
     * Add the values in this seismogram to the configuration
     *
     * @param seis the seismogram to be added
     */
    public void addSeismogram(DataSetSeismogram seis){
	if(seismograms.contains(seis))
	    return;
	seismograms.add(seis);
	ampConfig.addSeismogram(seis);
    }

    /**
     * Remove the values from this seismogram from the configuration
     *
     * @param seis the seismogram to be removed
     */
    public void removeSeismogram(DataSetSeismogram seis){ 
	seismograms.remove(seis);
	ampConfig.removeSeismogram(seis);
    }

    public UnitRangeImpl getAmpRange(DataSetSeismogram seis){
	return ampConfig.getAmpRange(seis); 
    }

    /**
     * Returns the amplitude range for a given seismogram over a time range
     *
     * 
     */
    public UnitRangeImpl getAmpRange(DataSetSeismogram seis, MicroSecondTimeRange calcIntv){ 
	return ampConfig.getAmpRange(seis, calcIntv); 
    }
    
    /**
     * Returns the amplitude range for the whole area being displayed.
     *
     */
    public UnitRangeImpl getAmpRange(){ return ampConfig.getAmpRange(); }
    
    /**
     * Calculates the amplitudes for all seismograms currently held by the configurator based on the time range rules for them held 
     * in the TimeRangeConfig object passed
     *
     */
    public void visibleAmpCalc(TimeConfigRegistrar timeRegistrar){ ampConfig.visibleAmpCalc(timeRegistrar); }

    public void addAmpSyncListener(AmpSyncListener a){ 	ampListeners.add(a); }

    /**
     * Removes an amplitude sync listener from the current configurator
     *
     * 
     */
    public void removeAmpSyncListener(AmpSyncListener a){ ampListeners.remove(a); }

    public void updateAmpSyncListeners(){
	Iterator e = ampListeners.iterator();
	while(e.hasNext())
	    ((AmpSyncListener)e.next()).updateAmpRange();
    }

    public void fireAmpRangeEvent(AmpSyncEvent event){
	ampConfig.fireAmpRangeEvent(event);
    }

    public void updateAmpRange(){ this.updateAmpSyncListeners(); }

    public void updateTimeRange(){ ampConfig.updateTimeRange(); } 

    public AmpRangeConfig getAmpConfig(){ return ampConfig.getAmpConfig(); }

    public void individualizeAmpConfig(TimeConfigRegistrar timeRegistrar){
	AmpRangeConfig newConfig = new RMeanAmpConfig(this);
	Iterator e = seismograms.iterator();
	while(e.hasNext())
	    newConfig.addSeismogram(((DataSetSeismogram)e.next()));
	AmpRangeConfig oldConfig = ampConfig;
	this.ampConfig = newConfig;
	e = seismograms.iterator();
	while(e.hasNext())
	    oldConfig.removeSeismogram(((DataSetSeismogram)e.next()));
	if(oldConfig instanceof AmpConfigRegistrar)
	    ((AmpConfigRegistrar)oldConfig).removeAmpSyncListener(this);
	this.ampConfig.visibleAmpCalc(timeRegistrar);
    }

    protected AmpRangeConfig ampConfig;

    protected LinkedList seismograms = new LinkedList();
    
    protected LinkedList ampListeners = new LinkedList();

    protected Category logger = Category.getInstance(AmpConfigRegistrar.class.getName());
}// AmpConfigRegistrar
