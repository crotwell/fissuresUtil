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
    
}// XMLLocation
