package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.*;
import edu.iris.Fissures.model.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.apache.log4j.*;
/**
 * XMLSampling.java
 *
 *
 * Created: Mon Jul  1 14:33:15 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class XMLSampling {
    public static void insert(Element element, Sampling sampling){
	Document doc = element.getOwnerDocument();
	element.appendChild(XMLUtil.createTextElement(doc,
						      "numPoints",
						      Integer.toString(sampling.numPoints)));
	Element interval = doc.createElement("interval");
 
	XMLQuantity.insert(interval, sampling.interval);
	element.appendChild(interval);
						      
    }

    public static Sampling getSampling(Element base) {
	
	//get numPoints
	int numPoints = Integer.parseInt(XMLUtil.getText(XMLUtil.getElement(base, "numPoints")));
	
	//get interval
	Quantity interval = null;
	Element interval_node = XMLUtil.getElement(base, "interval");
	if(interval_node != null) {
	    interval = XMLQuantity.getQuantity(interval_node);
	}
	
	return new SamplingImpl(numPoints, new TimeInterval(interval));
    }
}// XMLSampling
