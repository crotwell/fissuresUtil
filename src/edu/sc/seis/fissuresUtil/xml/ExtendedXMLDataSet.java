package edu.sc.seis.fissuresUtil.xml	;

import edu.iris.Fissures.network.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.net.*;
import java.util.*;

/**
 * ExtendedXMLDataSet.java
 *
 *
 * Created: Tue Feb 18 09:24:23 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class ExtendedXMLDataSet extends XMLDataSet{
   
    public ExtendedXMLDataSet(DocumentBuilder docBuilder, URL datasetURL) {
	super(docBuilder, datasetURL);
    }

    public ExtendedXMLDataSet(DocumentBuilder docBuilder, 
		      URL base, 
		      String id, 
		      String name,
		      String owner) {
	super(docBuilder,
	      base,
	      id,
	      name,
	      owner);
    }
    
    public ExtendedXMLDataSet(DocumentBuilder docBuilder, URL base, Element config) {
	super(docBuilder, base, config);
    }

    public String[] getDataSetSeismogramNames() {
	//	System.out.println("IN the method get seismogram names");
	Object[] names = dssNames.toArray();
	String[] rtnValues = new String[names.length];
	for(int counter = 0; counter < names.length; counter++) {
	    rtnValues[counter] = (String) names[counter];
	    System.out.println("The name of the seismogram is "+rtnValues[counter]);
	}
	return rtnValues;
      }

    public void addDataSetSeismogram(DataSetSeismogram dss) {
	//	System.out.println("In add dataset seismogram method");
	dssNames.add(ChannelIdUtil.toStringNoDates(dss.getRequestFilter().channel_id));
	
	dataSetSeismograms.put(ChannelIdUtil.toStringNoDates(dss.getRequestFilter().channel_id),
			       dss);
    }
    
    public DataSetSeismogram getDataSetSeismogram(String name) {
	
	DataSetSeismogram dss = (DataSetSeismogram)dataSetSeismograms.get(name);
	return dss;
    }
    

    Map dataSetSeismograms = new HashMap();
    List dssNames = new LinkedList();
    
}// ExtendedXMLDataSet
