package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.*;
import edu.iris.Fissures.IfEvent.*;
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

    public static void insert(Element originE, Origin origin) {
        Document doc = element.getOwnerDocument();
        originE.appendChild(XMLUtil.createTextElement(doc, 
                                                      "id", 
                                                      origin.get_id()));
        originE.appendChild(XMLUtil.createTextElement(doc, 
                                                      "catalog", 
                                                      origin.catalog));
        originE.appendChild(XMLUtil.createTextElement(doc, 
                                                      "contributor", 
                                                      origin.contributor));

        Element originTime = doc.createElement("origin_time");
        XMLTime.insert(originTime, origin.origin_time);
        originE.appendChild(originTime);

        Element location = doc.createElement("my_location");
        XMLLocation.insert(location, origin.my_location);
        originE.appendChild(location);

        Element magnitude;
        for (int i=0; i<origin.magnitudes.length; i++) {
            magnitude = doc.createElement("magnitude");
            XMLMagnitude.insert(magnitude, origin.magnitudes[i]);
            originE.appendChild(magnitude);
        } // end of for (int i=0; i<origin.magnitudes.length; i++)

    }
    

}// XMLOrigin
