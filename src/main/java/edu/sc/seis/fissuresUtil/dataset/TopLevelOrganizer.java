package edu.sc.seis.fissuresUtil.dataset;

import javax.swing.event.EventListenerList;

import edu.iris.Fissures.AuditInfo;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSet;
import edu.sc.seis.fissuresUtil.xml.StdDataSetParamNames;

/**
 * TopLevelOrganizer.java Created: Mon Jan 7 12:28:32 2002
 * 
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version $Id: TopLevelOrganizer.java 22072 2011-02-18 15:43:18Z crotwell $
 *          This class implements the Organizer interface and this interface can
 *          be used to organize data based on some consideration while
 *          populating the DataSetTree can be Extended to Remote DataSet also.
 */
public class TopLevelOrganizer implements Organizer {

    public TopLevelOrganizer() {
        this(new MemoryDataSet(" no id: TopLevelOrganizer",
                               "My Data",
                               "nobody",
                               new edu.iris.Fissures.AuditInfo[] {new AuditInfo(System.getProperty("user.name"),
                                                                                "created in memory.")}));
    }

    public TopLevelOrganizer(DataSet root) {
        rootDataSet = root;
    }

    public DataSet getRootDataSet() {
        return rootDataSet;
    }

    public DataSet getNoEarthquakeDataSet() {
        DataSet ds = getRootDataSet();
        DataSet noEQDS = ds.getDataSet("No Earthquake");
        if(noEQDS == null) {
            noEQDS = new MemoryDataSet("NO_EQ",
                                       "No Earthquake",
                                       System.getProperty("user.name"),
                                       new AuditInfo[0]);
            addDataSet(noEQDS);
        }
        return noEQDS;
    }

    public void addSeismogram(DataSetSeismogram seis, AuditInfo[] audit) {
        DataSet noEQDS = getNoEarthquakeDataSet();
        seis.setDataSet(noEQDS);
        noEQDS.addDataSetSeismogram(seis, audit);
        fireDataSetChanged(noEQDS);
    }

    public void addSeismogram(DataSetSeismogram seis,
                              EventAccessOperations event,
                              AuditInfo[] audit) {
        // ignore event
        addSeismogram(seis, audit);
        DataSet ds = getNoEarthquakeDataSet();
        ds.addParameter(StdDataSetParamNames.EVENT, event, audit);
    }

    public void addSeismogram(DataSetSeismogram seis,
                              DataSet dataSet,
                              AuditInfo[] audit) {
        dataSet.addDataSetSeismogram(seis, audit);
        fireDataSetChanged(dataSet);// this statement might not be needed.?????
    }

    public void addChannel(Channel chan, AuditInfo[] audit) {
        DataSet noEQDS = getNoEarthquakeDataSet();
        String channelParamName = CHANNEL
                + ChannelIdUtil.toString(chan.get_id());
        if(chan != null && noEQDS.getParameter(channelParamName) == null) {
            noEQDS.addParameter(channelParamName, chan, audit);
        }
    }

    public void addChannel(Channel chan,
                           EventAccessOperations event,
                           AuditInfo[] audit) {
        // ignore event
        addChannel(chan, audit);
    }

    public void addDataSet(DataSet dataset, AuditInfo[] audit) {
        DataSet ds = getRootDataSet();
        ds.addDataSet(dataset, audit);
        fireDataSetChanged(ds);
    }

    public void addDataSet(DataSet dataSet) {
        addDataSet(dataSet, new AuditInfo[0]);
    }

    public void addDataSetChangeListener(DataSetChangeListener l) {
        listenerList.add(DataSetChangeListener.class, l);
    }

    public void removeDataSetChangeListener(DataSetChangeListener l) {
        listenerList.remove(DataSetChangeListener.class, l);
    }

    protected void fireDataSetChanged(DataSet ds) {
        DataSetChangeEvent changeEvent = null;
        Object[] listeners = listenerList.getListenerList();
        for(int i = listeners.length - 2; i >= 0; i -= 2) {
            if(listeners[i] == (DataSetChangeListener.class)) {
                if(changeEvent == null) {
                    changeEvent = new DataSetChangeEvent(this, ds);
                }
                ((DataSetChangeListener)listeners[i + 1]).datasetChanged(changeEvent);
            }
        }
    }

    protected void fireDataSetAdded(DataSet ds) {
        DataSetChangeEvent changeEvent = null;
        Object[] listeners = listenerList.getListenerList();
        for(int i = listeners.length - 2; i >= 0; i -= 2) {
            if(listeners[i] == (DataSetChangeListener.class)) {
                if(changeEvent == null) {
                    changeEvent = new DataSetChangeEvent(this, ds);
                }
                ((DataSetChangeListener)listeners[i + 1]).datasetAdded(changeEvent);
            }
        }
    }

    protected void fireDataSetRemoved(DataSet ds) {
        DataSetChangeEvent changeEvent = null;
        Object[] listeners = listenerList.getListenerList();
        for(int i = listeners.length - 2; i >= 0; i -= 2) {
            if(listeners[i] == (DataSetChangeListener.class)) {
                if(changeEvent == null) {
                    changeEvent = new DataSetChangeEvent(this, ds);
                }
                ((DataSetChangeListener)listeners[i + 1]).datasetRemoved(changeEvent);
            }
        }
    }

    protected EventListenerList listenerList = new EventListenerList();

    DataSet rootDataSet;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TopLevelOrganizer.class);
}// TopLevelOrganizer
