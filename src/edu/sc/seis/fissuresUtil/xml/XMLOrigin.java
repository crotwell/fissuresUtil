package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.event.*;
import edu.iris.Fissures.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.IfParameterMgr.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.apache.log4j.*;

/**
 * XMLOrigin.java
 *
 *
 * Created: Wed Jun 12 09:50:35 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class XMLOrigin {

    public static Element translate(Document doc, Origin origin){
        Element originE = doc.createElement("origin");
        insert(originE, origin);
        return originE;
    }

    public static void insert(Element element, Origin origin) {
        Document doc = element.getOwnerDocument();
        element.appendChild(XMLUtil.createTextElement(doc, 
                                                      "id", 
                                                      origin.get_id()));
        element.appendChild(XMLUtil.createTextElement(doc, 
                                                      "catalog", 
                                                      origin.catalog));
        element.appendChild(XMLUtil.createTextElement(doc, 
                                                      "contributor", 
                                                      origin.contributor));

        Element originTime = doc.createElement("origin_time");
        XMLTime.insert(originTime, origin.origin_time);
        element.appendChild(originTime);

        Element location = doc.createElement("my_location");
        XMLLocation.insert(location, origin.my_location);
        element.appendChild(location);

        Element magnitude;
        for (int i=0; i<origin.magnitudes.length; i++) {
            magnitude = doc.createElement("magnitude");
            XMLMagnitude.insert(magnitude, origin.magnitudes[i]);
            element.appendChild(magnitude);
        } // end of for (int i=0; i<origin.magnitudes.length; i++)

    }

    public static Origin getOrigin(Element base) {
	String id = XMLUtil.evalString(base, "id");
	String catalog = XMLUtil.evalString(base, "catalog");
	String contributor = XMLUtil.evalString(base, "contributor");
	NodeList originTimeNode = XMLUtil.evalNodeList(base, "origin_time");
	edu.iris.Fissures.Time origin_time = new edu.iris.Fissures.Time();
	if(originTimeNode != null && originTimeNode.getLength() != 0 ) {
		origin_time = XMLTime.getFissuresTime((Element)originTimeNode.item(0));	
	}
	NodeList locationNode = XMLUtil.evalNodeList(base, "my_location");
	Location location = null;
	if(locationNode != null && locationNode.getLength() != 0) {
		location = XMLLocation.getLocation((Element)locationNode.item(0));
	}
	NodeList magnitudeList = XMLUtil.evalNodeList(base, "magnitude");
	Magnitude[] magnitudes = new Magnitude[0];
	if(magnitudeList != null && magnitudeList.getLength() != 0) {
		magnitudes = new Magnitude[magnitudeList.getLength()];
		for(int counter = 0; counter < magnitudeList.getLength(); counter++) {
			magnitudes[counter] =
			XMLMagnitude.getMagnitude((Element)magnitudeList.item(counter));
		}
	}
	return new OriginImpl(id, 
			      catalog,
			      contributor,
			      origin_time,
			      location,
			      magnitudes,
			      new ParameterRef[0]);
    }

}// XMLOrigin
