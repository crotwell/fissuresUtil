package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.event.*;
import edu.iris.Fissures.*;
import edu.iris.Fissures.IfEvent.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.apache.log4j.*;

/**
 * XMLEvent.java
 *
 *
 * Created: Wed Jun 12 14:55:38 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class XMLEvent {

    public static void insert(Element element,
			      EventAccessOperations event) 
    {
        Document doc = element.getOwnerDocument();
        element.appendChild(XMLUtil.createTextElement(doc, 
                                                      "name", 
                                                      event.get_attributes().name));
	Element feRegion = doc.createElement("region");
        XMLFlinnEngdahlRegion.insert(feRegion, event.get_attributes().region);
        element.appendChild(feRegion);

	try {
	    Element prefOrigin = doc.createElement("preferred_origin");
	    XMLOrigin.insert(prefOrigin, event.get_preferred_origin());
	    element.appendChild(prefOrigin);
	} catch (NoPreferredOrigin e) {
	    
	} // end of try-catch

    }

    public static EventAttr getEvent(Element base) {
	String name = XMLUtil.evalString(base, "name");
	NodeList regionNode = XMLUtil.evalNodeList(base, "region");
	Element regionElement = null;
	if(regionNode != null && regionNode.getLength() != 0) {
		regionElement = (Element)regionNode.item(0);
	}
	FlinnEngdahlRegion flinnEngdahlRegion =
		XMLFlinnEngdahlRegion.getRegion(regionElement);
	return new EventAttrImpl(name, 
				 flinnEngdahlRegion);
    }

    public static Origin getPreferredOrigin(Element base) {
	  NodeList preferred_originNode = XMLUtil.evalNodeList(base,"preferred_origin");
	  Element preferred_originElement = null;
	  Origin preferred_origin;
	  if(preferred_originNode != null && preferred_originNode.getLength() != 0 ) {
		  preferred_originElement = (Element) preferred_originNode.item(0);
          	  preferred_origin =
		 	XMLOrigin.getOrigin(preferred_originElement);
	  }  else preferred_origin = null;
	return preferred_origin;
    }

}// XMLEvent
