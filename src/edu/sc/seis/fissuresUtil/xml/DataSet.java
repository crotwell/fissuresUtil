package edu.sc.seis.fissuresUtil.xml;


import edu.iris.Fissures.*;
import edu.iris.Fissures.seismogramDC.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.apache.xpath.*;
import org.apache.xpath.objects.*;
import java.io.*;
import java.net.*;

public interface DataSet {

    public String getId();

    public String getName();

    public void setName(String name);

    public String[] getParameterNames();

    public Element getParamter(String name);

    public void addParameter(Element param);

    public String[] getDataSetNames();

    public DataSet getDataSet(String name);

    public void addDataSet(DataSet dataset);

    public DataSet createChildDataSet(String id, String name, String owner);

    public String[] getSeismogramNames();

    public LocalSeismogramImpl getSeismogram(String name);

    public void addSeismogram(LocalSeismogramImpl seis);

}
