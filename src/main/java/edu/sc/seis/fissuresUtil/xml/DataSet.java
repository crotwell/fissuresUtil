package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.AuditInfo;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;

/**
 * @author <a href="mailto:">Srinivasa Telukutla </a>
 */
public interface DataSet extends StdDataSetParamNames {

    public String getId();

    public String getName();

    public void setOwner(String owner);

    public String getOwner();

    public void setName(String name);

    public String[] getParameterNames();

    public Object getParameter(String name);

    public void addParameter(String name, Object param, AuditInfo[] audit);

    /**
     * Can be used in conjunction with getDataSet to get all of the datasets
     * held by this one
     * 
     * @return the names of all directly held DataSetSeismograms
     */
    public String[] getDataSetNames();

    /**
     * @return a DataSet with the given name
     */
    public DataSet getDataSet(String name);

    /**
     * adds the given DataSet as a child of this one
     */
    public void addDataSet(DataSet dataset, AuditInfo[] audit);

    /**
     * Creates a new data set and adds it to this one as a child
     */
    public DataSet createChildDataSet(String id,
                                      String name,
                                      String owner,
                                      AuditInfo[] audit);

    /**
     * adds the DataSetSeismogram to this DataSet.
     */
    public void addDataSetSeismogram(DataSetSeismogram dss, AuditInfo[] audit);

    /**
     * @return the DataSetSeismogram inserted with this name
     */
    public DataSetSeismogram getDataSetSeismogram(String seismogramName);

    /**
     * @return the names of all directly held DataSetSeismograms
     */
    public String[] getDataSetSeismogramNames();

    public void remove(DataSetSeismogram dss);

    /**
     * Optional method to get channel id of all Channel parameters.
     * 
     * @see StdDataSetParamNames for the prefix for these parameters.
     */
    public ChannelId[] getChannelIds();

    /**
     * Optional method to get the channel from the parameters, if it exists.
     * Should return null otherwise.
     * 
     * @see StdDataSetParamNames for the prefix for these parameters.
     */
    public Channel getChannel(ChannelId channelId);

    /**
     * Optional method to get the event associated with this dataset. Not all
     * datasets will have an event, return null in this case.
     * 
     * @see StdDataSetParamNames for the prefix for these parameters.
     */
    public EventAccessOperations getEvent();
}