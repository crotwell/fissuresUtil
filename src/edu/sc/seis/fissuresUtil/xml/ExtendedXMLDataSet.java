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
		return (String[])dssNames.toArray(new String[0]);
    }
	
    public void addDataSetSeismogram(DataSetSeismogram dss) {
		String name;
		name = dss.getName();
		if ( name == null || name.length() == 0) {
			name = ChannelIdUtil.toStringNoDates(dss.getRequestFilter().channel_id);
		} // end of if ()
		
		name = getUniqueName(getDataSetSeismogramNames(), name);
		
		if ( ! name.equals(dss.getName()) ) {
			dss.setName(name);
		} // end of if ()
		
		dssNames.add(name);
		dataSetSeismograms.put(name,
							   dss);
    }
    
    public DataSetSeismogram getDataSetSeismogram(String name) {
		
		DataSetSeismogram dss = (DataSetSeismogram)dataSetSeismograms.get(name);
		return dss;
    }
	
	
    Map dataSetSeismograms = new HashMap();
    List dssNames = new LinkedList();
    
}// ExtendedXMLDataSet
