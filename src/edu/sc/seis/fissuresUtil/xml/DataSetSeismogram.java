package edu.sc.seis.fissuresUtil.xml;

import edu.sc.seis.fissuresUtil.database.*;

import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;

import java.sql.SQLException;
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

public abstract class DataSetSeismogram 
    implements LocalDataCenterCallBack, Cloneable {


    public DataSetSeismogram(DataSet ds,
                             String name) {
        this.dssDataListeners = new LinkedList();
        this.rfChangeListeners = new LinkedList();
        this.dataSet = ds;
        this.name = name;
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
        return getRequestFilter().start_time;
    }

    public void setBeginTime(edu.iris.Fissures.Time time) {
        getRequestFilter().start_time = time;
        fireBeginTimeChangedEvent();
    }

    public MicroSecondDate getEndMicroSecondDate() {
        return new MicroSecondDate(getEndTime());
    }

    public edu.iris.Fissures.Time getEndTime() {
        return getRequestFilter().end_time;
    }

    public void setEndTime(edu.iris.Fissures.Time time) {
        getRequestFilter().end_time = time;
        fireEndTimeChangedEvent();
    }

    /** subclass may override this if they do not wish to use the internal
     *  requestFilter field.
     */
    public RequestFilter getRequestFilter() {
        return requestFilter;
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
        synchronized(dssDataListeners){
            dssDataListeners.add(dataListener);
        }
    }

    public void removeSeisDataChangeListener(SeisDataChangeListener dataListener) {
        dssDataListeners.remove(dataListener);
    }

    protected void fireNewDataEvent(SeisDataChangeEvent event) {
        // use temp array to avoid concurrentModificationException
        LinkedList tmp;
        synchronized(dssDataListeners){
            tmp = new LinkedList(dssDataListeners);
        }
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

    protected void fireDataErrorEvent(SeisDataErrorEvent event) {
        // use temp array to avoid concurrentModificationException
        LinkedList tmp = new LinkedList(dssDataListeners);
        Iterator iterator = tmp.iterator();
        while(iterator.hasNext()) {
            SeisDataChangeListener dssDataListener = (SeisDataChangeListener) iterator.next();
            dssDataListener.error(event);
        }
    }

    public void pushData(LocalSeismogramImpl[] seismograms, SeisDataChangeListener initiator) {
        SeisDataChangeEvent event = new SeisDataChangeEvent(seismograms,
                                                            this,
                                                            initiator);
        fireNewDataEvent(event);
    }

    public void finished(SeisDataChangeListener initiator) {
        SeisDataChangeEvent event = new SeisDataChangeEvent(this,
                                                            initiator);
        fireDataFinishedEvent(event);
    }

    public void error(SeisDataChangeListener initiator, Exception e) {
        SeisDataErrorEvent event = new SeisDataErrorEvent(e,
                                                           this,
                                                           initiator);
        fireDataErrorEvent(event);
    }

    public abstract void retrieveData(SeisDataChangeListener dataListener);

    private List dssDataListeners;

    private List rfChangeListeners;

    RequestFilter requestFilter;

    private DataSet dataSet = null;

    private String name = null;

    static Category logger =
        Category.getInstance(DataSetSeismogram.class.getName());

}// DataSetSeismogram
