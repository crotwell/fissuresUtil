package edu.sc.seis.fissuresUtil.xml;


import edu.iris.Fissures.AuditInfo;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;

/**
 * Describe interface <code>DataSet</code> here.
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public interface DataSet extends StdDataSetParamNames {



    /**
     * Describe <code>getId</code> method here.
     *
     * @return a <code>String</code> value
     */
    public String getId();

    /**
     * Describe <code>getName</code> method here.
     *
     * @return a <code>String</code> value
     */
    public String getName();

    /**
     * Sets the owner of the dataset.
     *
     * @param name a <code>String</code> value
     */
    public void setOwner(String owner);

    /**
     *  Gets the owner of the dataset.
     */
    public String getOwner();

    /**
     * Describe <code>setName</code> method here.
     *
     * @param name a <code>String</code> value
     */
    public void setName(String name);
    /**
     * Describe <code>getParameterNames</code> method here.
     *
     * @return a <code>String[]</code> value
     */
    public String[] getParameterNames();

    /**
     * Describe <code>getParameter</code> method here.
     *
     * @param name a <code>String</code> value
     * @return an <code>Object</code> value
     */
    public Object getParameter(String name);

    /**
     * Describe <code>addParameter</code> method here.
     *
     * @param name a <code>String</code> value
     * @param audit an <code>AuditInfo[]</code> value
     */
    public void addParameter(String name, Object param, AuditInfo[] audit);

    /**
     * Describe <code>getDataSetNames</code> method here.
     *
     * @return a <code>String[]</code> value
     */
    public String[] getDataSetNames();

    /**
     * Describe <code>getDataSet</code> method here.
     *
     * @param name a <code>String</code> value
     * @return a <code>DataSet</code> value
     */
    public DataSet getDataSet(String name);

    /**
     * Describe <code>addDataSet</code> method here.
     *
     * @param dataset a <code>DataSet</code> value
     * @param audit an <code>AuditInfo[]</code> value
     */
    public void addDataSet(DataSet dataset, AuditInfo[] audit);

    /**
     * Describe <code>createChildDataSet</code> method here.
     *
     * @param id a <code>String</code> value
     * @param name a <code>String</code> value
     * @param owner a <code>String</code> value
     * @param audit an <code>AuditInfo[]</code> value
     * @return a <code>DataSet</code> value
     */
    public DataSet createChildDataSet(String id, String name, String owner, AuditInfo[] audit);

    public void addDataSetSeismogram(DataSetSeismogram dss, AuditInfo[] audit);

    public DataSetSeismogram getDataSetSeismogram(String name);

    public String[] getDataSetSeismogramNames();

    public void remove(DataSetSeismogram dss);
    
    /** Optional method to get channel id of all Channel parameters.
     *  @see StdDataSetParamNames for the prefix for these parameters. */
    public ChannelId[] getChannelIds();

    /** Optional method to get the channel from the parameters, if it exists.
     *  Should return null otherwise.
     *  @see StdDataSetParamNames for the prefix for these parameters.*/
    public Channel getChannel(ChannelId channelId);

    /** Optional method to get the event associated with this dataset. Not all
     *  datasets will have an event, return null in this case.
     *  @see StdDataSetParamNames for the prefix for these parameters.
     */
    public EventAccessOperations getEvent();
}
