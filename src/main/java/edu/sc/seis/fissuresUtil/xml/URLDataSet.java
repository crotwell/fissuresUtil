/**
 * URLDataSetjava.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.fissuresUtil.xml;

import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.iris.Fissures.AuditInfo;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

public class URLDataSet implements DataSet {

    public URLDataSet(String name, URL url) {
        this.name = name;
        this.url = url;
    }

    /**
     * Optional method to get channel id of all Channel parameters.
     * 
     * @see StdDataSetParamNames for the prefix for these parameters.
     */
    public ChannelId[] getChannelIds() {
        return getCache().getChannelIds();
    }

    /**
     * Describe <code>getDataSetNames</code> method here.
     * 
     * @return a <code>String[]</code> value
     */
    public String[] getDataSetNames() {
        return getCache().getDataSetNames();
    }

    /**
     * Describe <code>setName</code> method here.
     * 
     * @param name
     *            a <code>String</code> value
     */
    public void setName(String name) {
        this.name = name;
        getCache().setName(name);
    }

    /**
     * Method getDataSetSeismogram
     * 
     * @param name
     *            a String
     * 
     * @return a DataSetSeismogram
     * 
     */
    public DataSetSeismogram getDataSetSeismogram(String name) {
        return getCache().getDataSetSeismogram(name);
    }

    /**
     * Sets the owner of the dataset.
     * 
     * @param name
     *            a <code>String</code> value
     */
    public void setOwner(String owner) {
        getCache().setOwner(owner);
    }

    /**
     * Method addDataSetSeismogram
     * 
     * @param dss
     *            a DataSetSeismogram
     * @param audit
     *            an AuditInfo[]
     * 
     */
    public void addDataSetSeismogram(DataSetSeismogram dss, AuditInfo[] audit) {
        getCache().addDataSetSeismogram(dss, audit);
    }

    /**
     * Describe <code>createChildDataSet</code> method here.
     * 
     * @param id
     *            a <code>String</code> value
     * @param name
     *            a <code>String</code> value
     * @param owner
     *            a <code>String</code> value
     * @param audit
     *            an <code>AuditInfo[]</code> value
     * @return a <code>DataSet</code> value
     */
    public DataSet createChildDataSet(String id,
                                      String name,
                                      String owner,
                                      AuditInfo[] audit) {
        return getCache().createChildDataSet(id, name, owner, audit);
    }

    /**
     * Describe <code>getParameterNames</code> method here.
     * 
     * @return a <code>String[]</code> value
     */
    public String[] getParameterNames() {
        return getCache().getParameterNames();
    }

    /**
     * Method remove
     * 
     * @param dss
     *            a DataSetSeismogram
     * 
     */
    public void remove(DataSetSeismogram dss) {
        getCache().remove(dss);
    }

    /**
     * Describe <code>addDataSet</code> method here.
     * 
     * @param dataset
     *            a <code>DataSet</code> value
     * @param audit
     *            an <code>AuditInfo[]</code> value
     */
    public void addDataSet(DataSet dataset, AuditInfo[] audit) {
        getCache().addDataSet(dataset, audit);
    }

    /**
     * Describe <code>getDataSet</code> method here.
     * 
     * @param name
     *            a <code>String</code> value
     * @return a <code>DataSet</code> value
     */
    public DataSet getDataSet(String name) {
        return getCache().getDataSet(name);
    }

    /**
     * Describe <code>getParameter</code> method here.
     * 
     * @param name
     *            a <code>String</code> value
     * @return an <code>Object</code> value
     */
    public Object getParameter(String name) {
        return getCache().getParameter(name);
    }

    /**
     * Optional method to get the event associated with this dataset. Not all
     * datasets will have an event, return null in this case.
     * 
     * @see StdDataSetParamNames for the prefix for these parameters.
     */
    public EventAccessOperations getEvent() {
        return getCache().getEvent();
    }

    /**
     * Describe <code>addParameter</code> method here.
     * 
     * @param name
     *            a <code>String</code> value
     * @param audit
     *            an <code>AuditInfo[]</code> value
     */
    public void addParameter(String name, Object param, AuditInfo[] audit) {
        getCache().addParameter(name, param, audit);
    }

    /**
     * Method getDataSetSeismogramNames
     * 
     * @return a String[]
     * 
     */
    public String[] getDataSetSeismogramNames() {
        return getCache().getDataSetSeismogramNames();
    }

    /**
     * Optional method to get the channel from the parameters, if it exists.
     * Should return null otherwise.
     * 
     * @see StdDataSetParamNames for the prefix for these parameters.
     */
    public Channel getChannel(ChannelId channelId) {
        return getCache().getChannel(channelId);
    }

    /**
     * Describe <code>getId</code> method here.
     * 
     * @return a <code>String</code> value
     */
    public String getId() {
        return getCache().getId();
    }

    /**
     * Gets the owner of the dataset.
     */
    public String getOwner() {
        return getCache().getOwner();
    }

    /**
     * Describe <code>getName</code> method here.
     * 
     * @return a <code>String</code> value
     */
    public String getName() {
        return name;
    }

    protected DataSet getCache() {
        if(cache == null) {
            try {
                cache = DataSetToXML.load(url);
            } catch(Exception e) {
                GlobalExceptionHandler.handle(e);
            }
        }
        return cache;
    }

    String name;

    URL url;

    private DataSet cache;

    static Logger logger = LoggerFactory.getLogger(URLDataSet.class);
}
