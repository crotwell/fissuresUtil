package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;
import edu.iris.Fissures.model.UnitImpl;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.apache.log4j.*;

/**
 * XMLFlinnEngdahlRegion.java
 *
 *
 * Created: Thu Jun 13 11:06:22 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class XMLFlinnEngdahlRegion {

    public static void insert(Element element, FlinnEngdahlRegion region){
	Document doc = element.getOwnerDocument();
	if (region.type.equals(FlinnEngdahlType.SEISMIC_REGION)) {
	    //System.out.println("SEISMOGRAHPHIC");      
	    element.appendChild(XMLUtil.createTextElement(doc, 
							  "type", 
							  "SEISMIC_REGION"));
	} else {
	    //System.out.println("GEOGRAPHIC");
	    element.appendChild(XMLUtil.createTextElement(doc, 
							  "type", 
							 "GEOGRAPHIC_REGION"));
	} // end of else
	//System.out.println("Insert the region number");
        element.appendChild(XMLUtil.createTextElement(doc, 
                                                      "number", 
                                                      ""+region.number));
	
	
 }

 public static FlinnEngdahlRegion getRegion(Element base) {

	String type = XMLUtil.evalString(base, "type");
	int value = Integer.parseInt(XMLUtil.evalString(base, "number"));
	FlinnEngdahlRegion flinnEngdahlRegion = null;
	if(type.equals("SEISMIC_REGION")) {
		flinnEngdahlRegion = new FlinnEngdahlRegionImpl(FlinnEngdahlType.SEISMIC_REGION,
								value);
	} else if(type.equals("GEOGRAPHIC_REGION")) {
		flinnEngdahlRegion = new FlinnEngdahlRegionImpl(FlinnEngdahlType.GEOGRAPHIC_REGION,
								value);
	}
	return flinnEngdahlRegion;
 }
    
}// XMLFlinnEngdahlRegion
