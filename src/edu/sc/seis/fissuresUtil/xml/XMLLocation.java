package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.apache.log4j.*;

/**
 * XMLLocation.java
 *
 *
 * Created: Wed Jun 12 15:22:08 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class XMLLocation {
    public static void insert(Element element, Location loc){
        Document doc = element.getOwnerDocument();
        element.appendChild(XMLUtil.createTextElement(doc, 
                                                      "latitude", 
                                                      ""+loc.latitude));
        element.appendChild(XMLUtil.createTextElement(doc, 
                                                      "longitude", 
                                                      ""+loc.longitude));
        Element elevation = doc.createElement("elevation");
        XMLQuantity.insert(elevation, loc.elevation);
        element.appendChild(elevation);
        Element depth = doc.createElement("depth");
        XMLQuantity.insert(depth, loc.depth);
        element.appendChild(depth);

        if (loc.type.equals(LocationType.GEOGRAPHIC)) {
            element.appendChild(XMLUtil.createTextElement(doc, 
                                                          "type", 
                                                          "GEOGRAPHIC"));
        } else {
            element.appendChild(XMLUtil.createTextElement(doc, 
                                                          "type", 
                                                          "GEOCENTRIC"));
        } // end of else
    }
    

public static Location getLocation(Element base) {

	float latitude = Float.parseFloat(XMLUtil.evalString(base, "latitude"));
	float longitude = Float.parseFloat(XMLUtil.evalString(base, "longitude"));
	NodeList elevationNode = XMLUtil.evalNodeList(base, "elevation");
	Quantity elevation = null;
	if(elevationNode != null && elevationNode.getLength() !=  0) {
		elevation = XMLQuantity.getQuantity((Element)elevationNode.item(0));
	}
	NodeList depthNode = XMLUtil.evalNodeList(base, "depth");
	Quantity depth = null;
	if(depthNode != null && depthNode.getLength() != 0) {
		depth = XMLQuantity.getQuantity((Element)depthNode.item(0));
	}
	LocationType locationType;
	String type = XMLUtil.evalString(base, "type");
	if(type.equals("GEOGRAPHIC")) {
		locationType = LocationType.GEOGRAPHIC;
	} else {
		locationType = LocationType.GEOCENTRIC;
	}
	return new Location(latitude,
			    longitude,
			    elevation,
			    depth,
			    locationType);
}

}//XMLLocation
