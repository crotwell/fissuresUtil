package edu.sc.seis.fissuresUtil.xml;


import edu.iris.Fissures.*;
import edu.iris.Fissures.seismogramDC.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.apache.xpath.*;
import org.apache.xpath.objects.*;
import java.io.*;
import java.net.*;

public interface DataSetAccess {

    public String getId();

    public String getName();

    public String[] getParameterRefNames();

    public Element getParamter(String name);

    public String[] getDataSetIds();

    public DataSetAccess getDataSet(String id);

    public String[] getSeismogramNames();

    public LocalSeismogramImpl getSeismogram(String name);

}
