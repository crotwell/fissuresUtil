package edu.sc.seis.fissuresUtil.xml;

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
	    element.appendChild(XMLUtil.createTextElement(doc, 
							  "type", 
							  "SEISMIC_REGION"));
	} else {
	    element.appendChild(XMLUtil.createTextElement(doc, 
							  "type", 
							 "GEOGRAPHIC_REGION"));
	} // end of else
        element.appendChild(XMLUtil.createTextElement(doc, 
                                                      "number", 
                                                      ""+region.number));
	
	
 }
    
}// XMLFlinnEngdahlRegion
