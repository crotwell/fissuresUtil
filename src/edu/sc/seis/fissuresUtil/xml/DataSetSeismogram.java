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
    
    public DataSetSeismogram(RequestFilter rf, 
			     DataCenterOperations dco) {
	this(rf, dco, null);
    }

    public DataSetSeismogram(RequestFilter rf, 
			     DataCenterOperations dco, 
			     DataSet ds) {
	this(rf, dco, ds, null);
    }

    public DataSetSeismogram(RequestFilter rf, 
			     DataCenterOperations dco, 
			     DataSet ds,
			     String name) {
	this.requestFilter = rf;
	this.dataCenterOps = dco;
	this.dataSet = ds;
	this.name = name;
	this.dssDataListeners = new LinkedList();
	this.rfChangeListeners = new LinkedList();
    }

    public DataSet getDataSet(){ return dataSet; }

    public String getName(){ return name; }

    public void setName(String name) { 
	this.name = name;
    }

    public String toString(){ return name; }

    public void setBeginTime(edu.iris.Fissures.Time time) {

	this.requestFilter.start_time = time;
	fireBeginTimeChangedEvent();
    }

    public void setEndTime(edu.iris.Fissures.Time time) {

	this.requestFilter.end_time = time;
	fireEndTimeChangedEvent();
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

     public void pushData(LocalSeismogramImpl[] seismograms, SeisDataChangeListener initiator) {
	 SeisDataChangeEvent event = new SeisDataChangeEvent(seismograms,
							     this, 
							     initiator);
	 fireNewDataEvent(event);
     }

    public void finished(SeisDataChangeListener initiator) {
	SeisDataChangeEvent event = new SeisDataChangeEvent(new LocalSeismogramImpl[0],
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

    private DataSet dataSet = null;

    private String name = null;

    static Category logger = 
        Category.getInstance(DataSetSeismogram.class.getName());

    
    
}// DataSetSeismogram
