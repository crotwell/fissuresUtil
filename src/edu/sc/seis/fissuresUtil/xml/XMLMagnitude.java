package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.*;
import edu.iris.Fissures.IfEvent.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.apache.log4j.*;

/**
 * XMLMagnitude.java
 *
 *
 * Created: Wed Jun 12 16:39:51 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class XMLMagnitude {


    public static void insert(Element element, Magnitude mag){
        Document doc = element.getOwnerDocument();
        element.appendChild(XMLUtil.createTextElement(doc, 
                                                      "type", 
                                                      mag.type));
        element.appendChild(XMLUtil.createTextElement(doc, 
                                                      "value", 
                                                      ""+mag.value));
        element.appendChild(XMLUtil.createTextElement(doc, 
                                                      "contributor", 
                                                      mag.contributor));
    }
    
}// XMLMagnitude
