package edu.sc.seis.fissuresUtil.database;

import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;

import edu.sc.seis.fissuresUtil.xml.*;

import java.util.*;

/**
 * DBDataSet.java
 *
 *
 * Created: Fri Feb  7 09:33:51 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class DBDataSet implements DataSet{
    public DBDataSet (String name){
	this.name = name;
	childDataSets = new HashMap();
	seismogramNames = new LinkedList();
    }

    /**
     * Describe <code>getId</code> method here.
     *
     * @return a <code>String</code> value
     */
    public String getId() {
	return null;
    }

    /**
     * Describe <code>getName</code> method here.
     *
     * @return a <code>String</code> value
     */
    public String getName() {
	return this.name;
    }

    /**
     * Describe <code>setName</code> method here.
     *
     * @param name a <code>String</code> value
     */
    public void setName(String name) {

    }

    /**
     * Describe <code>getParameterNames</code> method here.
     *
     * @return a <code>String[]</code> value
     */
    public String[] getParameterNames() {
	return new String[0];
    }

    /**
     * Describe <code>getParameter</code> method here.
     *
     * @param name a <code>String</code> value
     * @return an <code>Object</code> value
     */
    public Object getParameter(String name) {
	return null;
    }

    /**
     * Describe <code>addParameter</code> method here.
     *
     * @param name a <code>String</code> value
     * @param audit an <code>AuditInfo[]</code> value
     */
    public void addParameter(String name, Object param, AuditInfo[] audit) {
	
    }

    /**
     * Describe <code>getDataSetNames</code> method here.
     *
     * @return a <code>String[]</code> value
     */
    public String[] getDataSetNames() {
	Object[] datasetNames = childDataSets.keySet().toArray();
	String[] rtnValues = new String[datasetNames.length];
	
	for(int counter = 0; counter < datasetNames.length; counter++) {
	    rtnValues[counter] = (String) datasetNames[counter];
	    System.out.println("The name of the dataset is "+rtnValues[counter]);
	}
	System.out.println("The lenght of the datasetnames is "+rtnValues.length);
	return rtnValues;
    }

    /**
     * Describe <code>getDataSet</code> method here.
     *
     * @param name a <code>String</code> value
     * @return a <code>DataSet</code> value
     */
    public DataSet getDataSet(String name) {
	System.out.println("IN the Method getDataset");
	DataSet dataset = (DataSet)childDataSets.get(name);
	return dataset;
    }
    /** <code>addDataSet</code> method here.
     *
     * @param dataset a <code>DataSet</code> value
     * @param audit an <code>AuditInfo[]</code> value
     */
    public void addDataSet(DataSet dataset, AuditInfo[] audit) {
	childDataSets.put(dataset.getName(), dataset);
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
	return new DBDataSet(name);
    }

    /**
     * Describe <code>getSeismogramNames</code> method here.
     *
     * @return a <code>String[]</code> value
     */
    public String[] getSeismogramNames() {
	System.out.println("IN the method get seismogram names");
 	Object[] names = seismogramNames.toArray();
	String[] rtnValues = new String[names.length];
	for(int counter = 0; counter < names.length; counter++) {
	    rtnValues[counter] = (String) names[counter];
	    System.out.println("The name of the seismogram is "+rtnValues[counter]);
	}
	return rtnValues;
    }

    /**
     * Describe <code>getSeismogram</code> method here.
     *
     * @param name a <code>String</code> value
     * @return a <code>LocalSeismogramImpl</code> value
     */
    public LocalSeismogramImpl getSeismogram(String name) {
	//return null;
	System.out.println("The name of the seismogram asked is "+name);
	String fileids = SeisInfoDb.getSeisInfoDb().getFileIds(name);
	System.out.println("The value of the fileids is "+fileids);
	return (LocalSeismogramImpl)DBDataCenter.getDataCenter().getSeismogram(fileids);
    }

    /**
     * Describe <code>addSeismogram</code> method here.
     *
     * @param seis a <code>LocalSeismogramImpl</code> value
     * @param audit an <code>AuditInfo[]</code> value
     */
    public void addSeismogram(LocalSeismogramImpl seis, AuditInfo[] audit) {
	
	String name =seis.getProperty(seisNameKey);
        if (name == null || name.length() == 0) {
	    name = seis.channel_id.network_id.network_code+"."+
		seis.channel_id.station_code+"."+
		seis.channel_id.channel_code;
	}
	name = getUniqueName(getSeismogramNames(), name);
	seis.setName(name);
	seismogramNames.add(seis.getName());
	String fileids = DBDataCenter.getDataCenter().getFileIds(seis.getChannelID(),
						seis.getBeginTime(),
						seis.getEndTime());
	SeisInfoDb.getSeisInfoDb().insert(name, 
					  fileids);
	
    }

    /**
     * Describe <code>getUniqueName</code> method here.
     *
     * @param nameList a <code>String[]</code> value
     * @param name a <code>String</code> value
     * @return a <code>String</code> value
     */
    public String getUniqueName(String[] nameList, String name) {
	int counter = 0;
	for(int i = 0; i < nameList.length; i++) {
	    if(nameList[i].indexOf(name) != -1) counter++;
	}
	if(counter == 0) return name;
	return name+"_"+(counter+1);
    }

    public void addDataSetSeismogram(DataSetSeismogram dss) {

    }
    
    public DataSetSeismogram getDataSetSeismogram(String name) {
	return null;
    }

    public String[] getDataSetSeismogramNames() {
	return new String[0];
    }


    private String name;

    private HashMap childDataSets;// = new HashMap();
    
    private LinkedList seismogramNames;// = new LinkedList();

    private static final String seisNameKey = "Name";

    
}// DBDataSet
