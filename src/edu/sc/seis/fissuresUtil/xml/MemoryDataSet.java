package edu.sc.seis.fissuresUtil.xml;

import java.util.HashMap;
import java.util.LinkedList;
import edu.iris.Fissures.AuditInfo;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.network.ChannelIdUtil;

public class MemoryDataSet implements DataSet {

    public MemoryDataSet(String id, String name, String owner, AuditInfo[] audit) {
        this.id = id;
        setName(name);
        setOwner(owner);
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return getName();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    public String getId() {
        return id;
    }

    /**
     * @return all the names of this DataSet's children
     */
    public String[] getDataSetNames() {
        return (String[])datasetNames.toArray(new String[0]);
    }

    /**
     * @return a DataSet with the given name
     */
    public DataSet getDataSet(String dsName) {
        return (DataSet)datasets.get(dsName);
    }

    /**
     * Creates a new data set and adds it to this one as a child
     */
    public DataSet createChildDataSet(String newId,
                                      String newName,
                                      String newOwner,
                                      AuditInfo[] audit) {
        DataSet child = new MemoryDataSet(newId, newName, newOwner, audit);
        addDataSet(child, audit);
        return child;
    }

    /**
     * adds the given DataSet as a child of this one
     */
    public void addDataSet(DataSet dataset, AuditInfo[] audit) {
        datasets.put(dataset.getName(), dataset);
        datasetNames.add(dataset.getName());
    }

    /**
     * @return the names of all directly held DataSetSeismograms
     */
    public String[] getDataSetSeismogramNames() {
        return (String[])datasetSeismogramNames.toArray(new String[0]);
    }

    /**
     * adds the DataSetSeismogram to this DataSet. If a seismogram of the same
     * name is already in the data set, dss's name has a number appended to it
     * until it's unique
     */
    public void addDataSetSeismogram(DataSetSeismogram dss, AuditInfo[] audit) {
        if(datasetSeismogramNames.contains(dss.getName())) {
            int n = 1;
            String tmpName = dss.getName();
            while(datasetSeismogramNames.contains(tmpName)) {
                n++;
                tmpName = dss.getName() + "." + n;
            }
            // found a num that isn't used
            dss.setName(tmpName);
        }
        dss.setDataSet(this);
        datasetSeismograms.put(dss.getName(), dss);
        datasetSeismogramNames.add(dss.getName());
    }

    /**
     * @return the DataSetSeismogram inserted with this name
     */
    public DataSetSeismogram getDataSetSeismogram(String seismogramName) {
        return (DataSetSeismogram)datasetSeismograms.get(seismogramName);
    }

    /**
     * removes the given dataset seismogram from the dataset.
     */
    public void remove(DataSetSeismogram dss) {
        if(dss != null) {
            datasetSeismograms.remove(dss.getName());
            datasetSeismogramNames.remove(dss.getName());
        }
    }

    public String[] getParameterNames() {
        return (String[])parameterNames.toArray(new String[0]);
    }

    public Object getParameter(String paramName) {
        return parameters.get(paramName);
    }

    public void addParameter(String paramName, Object param, AuditInfo[] audit) {
        parameters.put(paramName, param);
        parameterNames.add(paramName);
    }

    /**
     * Optional method to get channel id of all Channel parameters.
     * 
     * @see StdDataSetParamNames for the prefix for these parameters.
     */
    public ChannelId[] getChannelIds() {
        String[] paramNames = getParameterNames();
        LinkedList out = new LinkedList();
        for(int counter = 0; counter < paramNames.length; counter++) {
            if(paramNames[counter].startsWith(StdDataSetParamNames.CHANNEL)) {
                Channel channel = (Channel)getParameter(paramNames[counter]);
                out.add(channel.get_id());
            }
        }
        ChannelId[] channelIds = new ChannelId[out.size()];
        channelIds = (ChannelId[])out.toArray(channelIds);
        return channelIds;
    }

    /**
     * Optional method to get the channel from the parameters, if it exists.
     * Should return null otherwise.
     * 
     * @see StdDataSetParamNames for the prefix for these parameters.
     */
    public Channel getChannel(ChannelId channelId) {
        Object obj = getParameter(StdDataSetParamNames.CHANNEL
                + ChannelIdUtil.toString(channelId));
        return (Channel)obj;
    }

    /**
     * Optional method to get the event associated with this dataset. Not all
     * datasets will have an event, return null in this case.
     * 
     * @see StdDataSetParamNames for the prefix for these parameters.
     */
    public EventAccessOperations getEvent() {
        return (EventAccessOperations)getParameter(StdDataSetParamNames.EVENT);
    }

    protected String name;

    protected String owner;

    protected String id;

    protected LinkedList datasetSeismogramNames = new LinkedList();

    protected LinkedList parameterNames = new LinkedList();

    protected LinkedList datasetNames = new LinkedList();

    protected HashMap datasetSeismograms = new HashMap();

    protected HashMap parameters = new HashMap();

    protected HashMap datasets = new HashMap();
}