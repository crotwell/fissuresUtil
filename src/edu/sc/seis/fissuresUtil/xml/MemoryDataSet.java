/**
 * MemoryDataSet.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.xml;

import java.util.HashMap;
import java.util.LinkedList;
import edu.iris.Fissures.AuditInfo;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.network.ChannelIdUtil;



public class MemoryDataSet implements DataSet {

    public MemoryDataSet( String id, String name, String owner, AuditInfo[] audit) {
        this.id = id;
        setName(name);
        setOwner(owner);
    }

    /**
     * Describe <code>getName</code> method here.
     *
     * @return a <code>String</code> value
     */
    public String getName() {
        return name;
    }

    public String toString() {
        return getName();
    }

    /**
     * Describe <code>setName</code> method here.
     *
     * @param name a <code>String</code> value
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the owner of the dataset.
     *
     * @param name a <code>String</code> value
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     *  Gets the owner of the dataset.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Describe <code>getId</code> method here.
     *
     * @return a <code>String</code> value
     */
    public String getId() {
        return id;
    }

    /**
     * Describe <code>getDataSetNames</code> method here.
     *
     * @return a <code>String[]</code> value
     */
    public String[] getDataSetNames() {
        return (String[])datasetNames.toArray(new String[0]);
    }

    /**
     * Describe <code>getDataSet</code> method here.
     *
     * @param name a <code>String</code> value
     * @return a <code>DataSet</code> value
     */
    public DataSet getDataSet(String name) {
        return (DataSet)datasets.get(name);
    }

    /**
     * Describe <code>createChildDataSet</code> method here.
     *
     * @param id a <code>String</code> value
     * @param name a <code>String</code> value
     * @param owner a <code>String</code> value
     * @param audit an <code>AuditInfo[]</code> value
     * @return a <code>DataSet</code> value
     */
    public DataSet createChildDataSet(String id, String name, String owner, AuditInfo[] audit) {
        DataSet child = new MemoryDataSet(id, name, owner, audit);
        addDataSet(child, audit);
        return child;
    }

    /**
     * Describe <code>addDataSet</code> method here.
     *
     * @param dataset a <code>DataSet</code> value
     * @param audit an <code>AuditInfo[]</code> value
     */
    public void addDataSet(DataSet dataset, AuditInfo[] audit) {
        datasets.put(dataset.getName(), dataset);
        datasetNames.add(dataset.getName());
    }

    /**
     * Method getDataSetSeismogramNames
     *
     * @return   a String[]
     *
     */
    public String[] getDataSetSeismogramNames() {
        return (String[])datasetSeismogramNames.toArray(new String[0]);
    }

    /**
     * Method addDataSetSeismogram
     *
     * @param    dss                 a  DataSetSeismogram
     * @param    audit               an AuditInfo[]
     *
     */
    public void addDataSetSeismogram(DataSetSeismogram dss, AuditInfo[] audit) {
        if (datasetSeismogramNames.contains(dss.getName())) {
            int n = 1;
            String tmpName = dss.getName();
            while(datasetSeismogramNames.contains(tmpName)) {
                n++;
                tmpName = dss.getName()+"."+n;
            }
            // found a num that isn't used
            dss.setName(tmpName);
        }
        dss.setDataSet(this);
        datasetSeismograms.put(dss.getName(), dss);
        datasetSeismogramNames.add(dss.getName());
    }

    /**
     * Method getDataSetSeismogram
     *
     * @param    name                a  String
     *
     * @return   a DataSetSeismogram
     *
     */
    public DataSetSeismogram getDataSetSeismogram(String name) {
        return (DataSetSeismogram)datasetSeismograms.get(name);
    }

    /**
     * removes the given dataset seismogram from the dataset.
     *
     * @param    dss                 a  DataSetSeismogram
     *
     */
    public void remove(DataSetSeismogram dss) {
        if (dss != null) {
            datasetSeismograms.remove(dss.getName());
            datasetSeismogramNames.remove(dss.getName());
        }
    }

    /**
     * Describe <code>getParameterNames</code> method here.
     *
     * @return a <code>String[]</code> value
     */
    public String[] getParameterNames() {
        return (String[])parameterNames.toArray(new String[0]);
    }

    /**
     * Describe <code>getParameter</code> method here.
     *
     * @param name a <code>String</code> value
     * @return an <code>Object</code> value
     */
    public Object getParameter(String name) {
        return parameters.get(name);
    }

    /**
     * Describe <code>addParameter</code> method here.
     *
     * @param name a <code>String</code> value
     * @param audit an <code>AuditInfo[]</code> value
     */
    public void addParameter(String name, Object param, AuditInfo[] audit) {
        parameters.put(name, param);
        parameterNames.add(name);
    }

    /** Optional method to get channel id of all Channel parameters.
     *  @see StdDataSetParamNames for the prefix for these parameters. */
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

    /** Optional method to get the channel from the parameters, if it exists.
     *  Should return null otherwise.
     *  @see StdDataSetParamNames for the prefix for these parameters.*/
    public Channel getChannel(ChannelId channelId) {
        Object obj = getParameter(StdDataSetParamNames.CHANNEL+ChannelIdUtil.toString(channelId));
        return (Channel)obj;
    }

    /** Optional method to get the event associated with this dataset. Not all
     *  datasets will have an event, return null in this case.
     *  @see StdDataSetParamNames for the prefix for these parameters.
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

