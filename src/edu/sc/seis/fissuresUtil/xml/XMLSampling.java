package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.*;
import edu.iris.Fissures.model.UnitImpl;
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
}// XMLSampling
