package edu.sc.seis.fissuresUtil.xml;


import edu.iris.Fissures.*;
import edu.iris.Fissures.seismogramDC.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.apache.xpath.*;
import org.apache.xpath.objects.*;
import java.io.*;
import java.net.*;

/**
 * Describe interface <code>DataSet</code> here.
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public interface DataSet {



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

    /**
     * Describe <code>getSeismogramNames</code> method here.
     *
     * @return a <code>String[]</code> value
     */
    public String[] getSeismogramNames();

    /**
     * Describe <code>getSeismogram</code> method here.
     *
     * @param name a <code>String</code> value
     * @return a <code>LocalSeismogramImpl</code> value
     */
    public LocalSeismogramImpl getSeismogram(String name);

    /**
     * Describe <code>addSeismogram</code> method here.
     *
     * @param seis a <code>LocalSeismogramImpl</code> value
     * @param audit an <code>AuditInfo[]</code> value
     */
    public void addSeismogram(LocalSeismogramImpl seis, AuditInfo[] audit);


    public void addDataSetSeismogram(DataSetSeismogram dss);
    
    public DataSetSeismogram getDataSetSeismogram(String name);

}
