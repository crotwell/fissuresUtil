package edu.sc.seis.fissuresUtil.xml;

import org.w3c.dom.*;
import javax.xml.parsers.*;

/**
 * XMLUtil.java
 *
 *
 * Created: Wed Jun 12 10:03:01 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class XMLUtil {

    public static Element createTextElement(Document doc, 
                                            String elementName, 
                                            String value) {
        Element element = doc.createElement(elementName);
        Text text = doc.createTextNode(value);
        element.appendChild(text);
        return element;
    }
    
}// XMLUtil
