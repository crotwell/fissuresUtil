package edu.sc.seis.fissuresUtil.display;

import java.util.*;
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
	this(new RMeanAmpConfig());
    }   

    public AmpConfigRegistrar(AmpRangeConfig ar){
	this(ar, null);
    }   

    public AmpConfigRegistrar(AmpSyncListener creator){
	this(new RMeanAmpConfig(), creator);
    }

    public AmpConfigRegistrar (AmpRangeConfig ampConfig, AmpSyncListener creator){
	this.ampConfig = ampConfig;
	if(creator != null){
	    this.addAmpSyncListener(creator);
	}
	ampConfig.addAmpSyncListener(this);
	seismograms = ampConfig.getSeismograms();
	snapshot = new AmpSnapshot(seismograms, null);
    }	
     
    public void setAmpConfig(AmpRangeConfig ampConfig){ 
	ampConfig.removeAmpSyncListener(this);
	Iterator e = seismograms.keySet().iterator();
	while(e.hasNext()){
	    DataSetSeismogram current = (DataSetSeismogram)e.next();
	    ampConfig.addSeismogram(current);
	    seismograms.put(current, ampConfig.getAmpRange(current));	    
	}
	ampConfig.addAmpSyncListener(this);
	this.ampConfig = ampConfig;
	seismograms = ampConfig.getSeismograms();
	updateAmpSyncListeners();
    }

    public AmpRangeConfig getRegistrar(){ return ampConfig; }

    /**
     * Add the values in this seismogram to the configuration
     *
     * @param seis the seismogram to be added
     */
    public void addSeismogram(DataSetSeismogram seis){
	ampConfig.addSeismogram(seis);
	//	seismograms.put(seis, ampConfig.getAmpRange(seis));
    }

    /**
     * Remove the values from this seismogram from the configuration
     *
     * @param seis the seismogram to be removed
     */
    public void removeSeismogram(DataSetSeismogram seis){ 
	ampConfig.removeSeismogram(seis);
	//seismograms.remove(seis);
    }

    public HashMap getSeismograms(){ return seismograms; }

    public void unregister(){
	seismograms.clear();
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

    public void updateAmpRange(){ 
	if(!taken){
	    snapshot.update(seismograms, ampConfig.getAmpRange());
	}
	this.updateAmpSyncListeners(); 
    }

    public void updateTimeRange(){ ampConfig.updateTimeRange(); } 

    public void individualizeAmpConfig(TimeConfigRegistrar timeRegistrar){
	AmpRangeConfig newConfig = new RMeanAmpConfig(this);
	Iterator e = seismograms.keySet().iterator();
	while(e.hasNext())
	    newConfig.addSeismogram(((DataSetSeismogram)e.next()));
	AmpRangeConfig oldConfig = ampConfig;
	this.ampConfig = newConfig;
	e = seismograms.keySet().iterator();
	while(e.hasNext())
	    oldConfig.removeSeismogram(((DataSetSeismogram)e.next()));
	if(oldConfig instanceof AmpConfigRegistrar)
	    ((AmpConfigRegistrar)oldConfig).removeAmpSyncListener(this);
	this.ampConfig.visibleAmpCalc(timeRegistrar);
    }
    
    public synchronized AmpSnapshot takeSnapshot(){
	taken = true;
	return snapshot;
    }

    public void returnSnapshot(){
	taken = false;
	snapshot.update(seismograms, ampConfig.getAmpRange());
    }

    protected boolean taken = false;
    
    protected AmpSnapshot snapshot;

    protected AmpRangeConfig ampConfig;

    protected HashMap seismograms;// = new HashMap();
    
    protected Set ampListeners = new HashSet();

    protected Category logger = Category.getInstance(AmpConfigRegistrar.class.getName());
}// AmpConfigRegistrar
