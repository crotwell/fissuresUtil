package edu.sc.seis.fissuresUtil.xml;

import edu.sc.seis.fissuresUtil.database.*;

import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;

import java.util.*;
import org.apache.log4j.*;

/**
 * DataSetSeismogram.java
 *
 *
 * Created: Tue Feb 11 10:08:37 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class DataSetSeismogram implements LocalDataCenterCallBack {
    
    public DataSetSeismogram(RequestFilter rf, DataCenterOperations dco) {
	this.requestFilter = rf;
	this.dataCenterOps = dco;
	this.dssDataListeners = new LinkedList();
	this.rfChangeListeners = new LinkedList();
    }

    public void setEndTime(edu.iris.Fissures.Time time) {

	this.requestFilter.start_time = time;
    }

    public void setBeginTime(edu.iris.Fissures.Time time) {

	this.requestFilter.end_time = time;
    }

    public RequestFilter getRequestFilter() {
	return this.requestFilter;
    }


    
    public void addRequestFilterChangeListener(RequestFilterChangeListener listener) {
	rfChangeListeners.add(listener);
    }

    public void removeRequestFilterChangeListener(RequestFilterChangeListener listener) {
	rfChangeListeners.remove(listener);
    }

    public void fireEndTimeChangedEvent() {
	Iterator iterator = rfChangeListeners.iterator();
	while(iterator.hasNext()) {
	    RequestFilterChangeListener listener = (RequestFilterChangeListener) iterator.next();
	    listener.endTimeChanged();
	}
    }
    
    public void fireBeginTimeChangedEvent() {
	Iterator iterator = rfChangeListeners.iterator();
	while(iterator.hasNext()) {
	    RequestFilterChangeListener listener = (RequestFilterChangeListener) iterator.next();
	    listener.beginTimeChanged();
	}
    }

    public void addSeisDataChangeListener(SeisDataChangeListener dataListener) {
	dssDataListeners.add(dataListener);

    }

    public void removeSeisDataChangeListener(SeisDataChangeListener dataListener) {
	dssDataListeners.remove(dataListener);
    }

    public void fireNewDataEvent(SeisDataChangeEvent event) {
	Iterator iterator = dssDataListeners.iterator();
	while(iterator.hasNext()) {
	    SeisDataChangeListener dssDataListener = (SeisDataChangeListener) iterator.next();
	    dssDataListener.pushData(event);
	}
    }

    public void fireDataFinishedEvent(SeisDataChangeEvent event) {

	Iterator iterator = dssDataListeners.iterator();
	while(iterator.hasNext()) {
	    SeisDataChangeListener dssDataListener = (SeisDataChangeListener) iterator.next();
	    dssDataListener.finished(event);
	}
    }

     public void pushData(LocalSeismogram[] seismograms, SeisDataChangeListener initiator) {
	 SeisDataChangeEvent event = new SeisDataChangeEvent(seismograms,
							     this, 
							     initiator);
	 fireNewDataEvent(event);
     }

    public void finished(SeisDataChangeListener initiator) {
	SeisDataChangeEvent event = new SeisDataChangeEvent(new LocalSeismogram[0],
							    this,
							    initiator);
	fireDataFinishedEvent(event);
    }

    public void retrieveData(SeisDataChangeListener dataListener) {

	RequestFilter[] temp = new RequestFilter[1];
	temp[0] = requestFilter;
	try {
	    //System.out.println("Calling the request_seismograms method of the datacenter");
	    DBDataCenter.getDataCenter().request_seismograms(temp,
							     (LocalDataCenterCallBack)this,
							     dataListener,
							     false,
							     new MicroSecondDate().getFissuresTime());
	} catch(FissuresException fe) {
	    logger.debug("Exception occurred while using DataCenter to get Data",fe); 
	}
    }
    
    private List dssDataListeners;

    private List rfChangeListeners;
    
    private RequestFilter requestFilter;
    
    private DataCenterOperations dataCenterOps;

    static Category logger = 
        Category.getInstance(DataSetSeismogram.class.getName());

    
    
}// DataSetSeismogram
