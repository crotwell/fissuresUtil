package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.seismogramDC.*;
import org.apache.log4j.*;
import java.util.*;
/**
 * AbstractAmpRangeConfig.java
 *
 *
 * Created: Mon Jun  3 13:16:18 2002
 *
 * @author Charlie Groves
 * @version
 */

public abstract class AbstractAmpRangeConfig implements AmpRangeConfig{
         
    public AbstractAmpRangeConfig(){}

    public AbstractAmpRangeConfig(LinkedList seismos){
	this.seismos = seismos;
	Iterator e = seismos.iterator();
	while(e.hasNext()){
	    this.addSeismogram(((LocalSeismogram)e.next()));
	}
    }

    /**
     * Returns the amplitude range for a given seismogram
     *
     * 
     */
    public abstract UnitRangeImpl getAmpRange(LocalSeismogram seis);

    /**
     * Returns the amplitude range for a given seismogram over a time range
     *
     * 
     */
    public abstract UnitRangeImpl getAmpRange(LocalSeismogram seis, MicroSecondTimeRange calcIntv);
    
    /**
     * Returns the amplitude range for the whole area being displayed.
     *
     */
    public UnitRangeImpl getAmpRange(){ return ampRange; }
    
    /**
     * Calculates the amplitudes for all seismograms currently held by the configurator based on the time range rules for them held 
     * in the TimeRangeConfig object passed
     *
     */
    public void visibleAmpCalc(TimeConfigRegistrar timeRegistrar){
	this.timeRegistrar = timeRegistrar;
	timeRegistrar.addTimeSyncListener(this);
	this.updateTimeRange();
    }

    /**
     * Adds a seismogram to the current amplitude configurator
     *
     */
    public void addSeismogram(LocalSeismogram seis){ seismos.add(seis); }

    /**
     * Removes a seismogram from this amplitude configurator
     *
     * @param seis a <code>LocalSeismogram</code> value
     */
    public void removeSeismogram(LocalSeismogram seis){ 
	if(seismos.size() == 1)
	    if(timeRegistrar != null)
		timeRegistrar.removeTimeSyncListener(this);
	seismos.remove(seis); 
    }

    /**
     * Sends a message to all AmpSyncListeners held by the configurator that the amplitude range has changed
     *
     */
    public void updateAmpSyncListeners(){
	ampRegistrar.updateAmpSyncListeners();
    }

    public void updateTimeRange(){
	UnitRangeImpl tempRange = ampRange;
	ampRange = null;
	Iterator e = seismos.iterator();
	while(e.hasNext()){
	    LocalSeismogram current = (LocalSeismogram)e.next();
	    getAmpRange(current, timeRegistrar.getTimeRange(current));
	}
	if(ampRange == null){
	    ampRange = tempRange;
	    return;
	}
	this.updateAmpSyncListeners();
    }

    public void fireAmpRangeEvent(AmpSyncEvent event){
	double begin = event.getBegin();
	double end = event.getEnd();
	if(this.ampRange == null) {

	    this.ampRange = new UnitRangeImpl(begin, end, UnitImpl.COUNT);
	} else {
	    this.ampRange = new UnitRangeImpl(begin, end, UnitImpl.COUNT);
	}
	this.updateAmpSyncListeners();
    }

    public void setRegistrar(AmpConfigRegistrar registrar){ this.ampRegistrar = registrar; }

    public AmpRangeConfig getRegistrar(){ return ampRegistrar; }
    
    public LinkedList getSeismograms(){ return seismos; }

    public void addSeismograms(LinkedList newSeismos){
	Iterator e = newSeismos.iterator();
	while(e.hasNext())
	    this.addSeismogram((LocalSeismogram)e.next());
    }

    public AmpRangeConfig getAmpConfig(){ return this; }

    protected boolean intvCalc = false;
    
    protected UnitRangeImpl ampRange;

    protected TimeConfigRegistrar timeRegistrar = null;

    protected LinkedList seismos = new LinkedList();
    
    protected AmpRangeConfig ampRegistrar;

    protected Category logger = Category.getInstance(AbstractAmpRangeConfig.class.getName());
}// AbstractAmpRangeConfig
