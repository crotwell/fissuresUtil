package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.*;
import edu.iris.Fissures.IfEvent.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.apache.log4j.*;

/**
 * XMLParameter.java
 *
 *
 * Created: Thu Jun 13 11:29:36 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class XMLParameter {

    public static void insert(Element element, String name, Element value){
        Document doc = element.getOwnerDocument();
        element.appendChild(XMLUtil.createTextElement(doc, 
                                                      "name", 
                                                      name));
	Element valueElement = doc.createElement("value");
	valueElement.appendChild(value);
	element.appendChild(valueElement);
    }

    public static void insert(Element element, String name, String value){
        Document doc = element.getOwnerDocument();
        element.appendChild(XMLUtil.createTextElement(doc, 
                                                      "name", 
                                                      name));
        element.appendChild(XMLUtil.createTextElement(doc, 
                                                      "value", 
                                                      value));
    }

}// XMLParameter
