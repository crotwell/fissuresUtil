package edu.sc.seis.fissuresUtil.xml;

import edu.sc.seis.fissuresUtil.database.*;

import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.network.*;
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

public class DataSetSeismogram implements LocalDataCenterCallBack, Cloneable {



    public DataSetSeismogram() {
        this.dssDataListeners = new LinkedList();
        this.rfChangeListeners = new LinkedList();
    }

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

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // can't happen
            logger.error("Caught clone not supported, but this cannot happen!");
        } // end of try-catch
        return null;
    }

    public boolean equals(Object other) {
        if ( ! (other instanceof DataSetSeismogram)) {
            return false;
        } // end of if ()
        if (super.equals(other)) {
            return true;
        } // end of if ()

        // objects are not the same, but may be cloned check request filter
        DataSetSeismogram otherDSS = (DataSetSeismogram)other;
        if ( ! otherDSS.getName().equals(getName())) {
            return false;
        } // end of if ()


        if ( ! ChannelIdUtil.areEqual(otherDSS.getRequestFilter().channel_id,
                                      getRequestFilter().channel_id)) {
            return false;
        } // end of if ()
        MicroSecondDate otherB = otherDSS.getBeginMicroSecondDate();
        MicroSecondDate thisB = getBeginMicroSecondDate();
        if ( ! otherB.equals(thisB)) {
            return false;
        } // end of if ()

        MicroSecondDate otherE = otherDSS.getEndMicroSecondDate();
        MicroSecondDate thisE = getEndMicroSecondDate();
        if ( ! otherE.equals(thisE)) {
            return false;
        } // end of if ()

        if ( otherDSS.getDataSet() != getDataSet()) {
            return false;
        } // end of if ()

        return true;
    }

    /** gets the dataset to which this seismogram belongs. May be null if
     *  it does not belong to a dataset. */
    public DataSet getDataSet(){ return dataSet; }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public String getName(){ return name; }

    public void setName(String name) {
        this.name = name;
    }

    public String toString(){ return name; }

    public MicroSecondDate getBeginMicroSecondDate() {
        return new MicroSecondDate(getBeginTime());
    }

    public edu.iris.Fissures.Time getBeginTime() {
        return requestFilter.start_time;
    }

    public void setBeginTime(edu.iris.Fissures.Time time) {
        this.requestFilter.start_time = time;
        fireBeginTimeChangedEvent();
    }

    public MicroSecondDate getEndMicroSecondDate() {
        return new MicroSecondDate(getEndTime());
    }

    public edu.iris.Fissures.Time getEndTime() {
        return requestFilter.end_time;
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

    protected void fireEndTimeChangedEvent() {
        // use temp array to avoid concurrentModificationException
        Collection tmp = new LinkedList(rfChangeListeners);
        Iterator iterator = tmp.iterator();
        while(iterator.hasNext()) {
            RequestFilterChangeListener listener = (RequestFilterChangeListener) iterator.next();
            listener.endTimeChanged();
        }
    }

    protected void fireBeginTimeChangedEvent() {
        // use temp array to avoid concurrentModificationException
        Collection tmp = new LinkedList(rfChangeListeners);
        Iterator iterator = tmp.iterator();
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

    protected void fireNewDataEvent(SeisDataChangeEvent event) {
        // use temp array to avoid concurrentModificationException
        LinkedList tmp = new LinkedList(dssDataListeners);
        Iterator iterator = tmp.iterator();
        while(iterator.hasNext()) {
            SeisDataChangeListener dssDataListener = (SeisDataChangeListener) iterator.next();
            dssDataListener.pushData(event);
        }
    }

    protected void fireDataFinishedEvent(SeisDataChangeEvent event) {
        // use temp array to avoid concurrentModificationException
        LinkedList tmp = new LinkedList(dssDataListeners);
        Iterator iterator = tmp.iterator();
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
            if(this.dataCenterOps instanceof DBDataCenter) {

                ((DBDataCenter)this.dataCenterOps).request_seismograms(temp,
                                                                       (LocalDataCenterCallBack)this,
                                                                       dataListener,
                                                                       false,
                                                                       new MicroSecondDate().getFissuresTime());

            } else {
                DBDataCenter.getDataCenter(this.dataCenterOps).request_seismograms(temp,
                                                                                   (LocalDataCenterCallBack)this,
                                                                                   dataListener,
                                                                                   false,
                                                                                   new MicroSecondDate().getFissuresTime());
            }
        } catch(FissuresException fe) {
            logger.debug("Exception occurred while using DataCenter to get Data",fe);
        }
    }

    private List dssDataListeners;

    private List rfChangeListeners;

    RequestFilter requestFilter;

    private DataCenterOperations dataCenterOps;

    private DataSet dataSet = null;

    private String name = null;

    static Category logger =
        Category.getInstance(DataSetSeismogram.class.getName());



}// DataSetSeismogram
