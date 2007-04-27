package edu.sc.seis.fissuresUtil.dataset;

import edu.iris.Fissures.AuditInfo;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.StdDataSetParamNames;
import edu.sc.seis.fissuresUtil.xml.URLDataSet;

/**
 * DataSetEventOrganizer.java Created: Tue Jul 16 15:09:32 2002
 * 
 * @author <a href="mailto:">Srinivasa Telukutla </a>
 * @version
 */
public class DataSetEventOrganizer extends TopLevelOrganizer implements
        StdDataSetParamNames {

    public DataSetEventOrganizer() {
        super();
    }

    public DataSetEventOrganizer(DataSet root) {
        super(root);
    }

    public void addSeismogram(DataSetSeismogram seis,
                              EventAccessOperations event,
                              AuditInfo[] audit) {
        DataSet ds = getEventDataSet(event);
        ds.addDataSetSeismogram(seis, audit);
        // adding event is done as a side effect of getEventDataSet
    }

    /**
     * it checks for a dataset which already has cacheEvent as a parameter, if
     * it finds such a dataset, adds the Channel to that dataset, else creates a
     * new dataset, adds the event, Channel to the newly created dataset.
     * 
     * @param cacheEvent
     *            a <code>CacheEvent</code> value
     * @param channel
     *            a <code>Channel</code> value
     */
    public void addChannel(Channel channel,
                           EventAccessOperations event,
                           AuditInfo[] audit) {
        addChannel(channel, getEventDataSet(event), audit);
    }

    public void addChannel(Channel channel, DataSet dataSet, AuditInfo[] audit) {
        String channelParamName = CHANNEL
                + ChannelIdUtil.toString(channel.get_id());
        if(channel != null && dataSet.getParameter(channelParamName) == null) {
            dataSet.addParameter(channelParamName, channel, audit);
        }
    }

    /**
     * if a dataset which has CacheEvent as a parameter then that particular
     * dataset is returned. If no dataset with CacheEvent as a parameter is
     * found, then a new one is created. If cacheEvent
     */
    private DataSet getEventDataSet(EventAccessOperations event) {
        if(event != null) {
            DataSet dataSet = findEventDataSet(event, getRootDataSet());
            if(dataSet == null) {
                dataSet = makeSubDataSet(event, getRootDataSet());
            }
            return dataSet;
        }
        return getRootDataSet();
    }

    /**
     * Searches for the given event in dataset and its dataset children. Only
     * local datasets are searched for the event so as not to pollute remotely
     * returned datasets with locally found event data.
     * 
     * If no matching event is found in the local datasets, null is returned
     */
    private DataSet findEventDataSet(EventAccessOperations event,
                                     DataSet dataSet) {
        EventAccessOperations dataSetEvent = dataSet.getEvent();
        CacheEvent cacheEvent;
        if(event instanceof CacheEvent) {
            cacheEvent = (CacheEvent)event;
        } else {
            cacheEvent = new CacheEvent(event);
        }
        if(dataSetEvent == null) {
            String[] dataSetNames = dataSet.getDataSetNames();
            for(int counter = 0; counter < dataSetNames.length; counter++) {
                DataSet subDataset = dataSet.getDataSet(dataSetNames[counter]);
                if(!(subDataset instanceof URLDataSet)) {// don't load remote
                    // datasets to
                    // search for events
                    DataSet rtnValue = findEventDataSet(cacheEvent, subDataset);
                    if(rtnValue != null) {
                        return rtnValue;
                    }
                }
            }
            return null;
        }
        if(cacheEvent.close(dataSetEvent)) {
            return dataSet;
        }
        return null;
    }

    protected DataSet makeSubDataSet(EventAccessOperations event,
                                     DataSet rootDataSet) {
        EventAttr eventAttr = event.get_attributes();
        String dataSetName = null;
        if(eventAttr.region.number != 0) {
            dataSetName = parseRegions.getRegionName(eventAttr.region);
        }
        String[] dataSetNames = rootDataSet.getDataSetNames();
        if(dataSetName == null || dataSetName.equals("Unknown")) {
            dataSetName = "Your Earthquake"
                    + " ("
                    + EventUtil.getEventInfo(event, EventUtil.TIME + ", "
                            + EventUtil.MAG + " )");
        }
        if(nameExists(dataSetName, dataSetNames)) {
            // try appending magnitude
            try {
                Origin origin = event.get_preferred_origin();
                if(origin.magnitudes.length > 0) {
                    dataSetName += " " + origin.magnitudes[0].value
                            + origin.magnitudes[0].type;
                }
            } catch(NoPreferredOrigin e) {
                // oh well...
            }
            int nameNumber = 1;
            String suffix = "";
            while(nameExists(dataSetName + suffix, dataSetNames)) {
                nameNumber++;
                suffix = " " + nameNumber;
            }
            dataSetName += suffix;
        }
        DataSet dataSet = rootDataSet.createChildDataSet("genid"
                                                                 + Math.round(Math.random()
                                                                         * Integer.MAX_VALUE),
                                                         dataSetName,
                                                         rootDataSet.getName(),
                                                         new AuditInfo[0]);
        fireDataSetAdded(rootDataSet);
        dataSet.addParameter(EVENT, event, new AuditInfo[0]);
        return dataSet;
    }

    protected boolean nameExists(String name, String[] existing) {
        for(int i = 0; i < existing.length; i++) {
            if(name.equals(existing[i])) {
                return true;
            }
        }
        return false;
    }

    ParseRegions parseRegions = ParseRegions.getInstance();
}// DataSetEventOrganizer
