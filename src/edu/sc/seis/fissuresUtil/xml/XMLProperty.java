package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.seismogramDC.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.apache.xpath.*;
import org.apache.xpath.objects.*;
import java.io.*;
import java.net.*;

/**
 * Describe class <code>XMLProperty</code> here.
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class XMLProperty {

    /**
     * Describe <code>createElement</code> method here.
     *
     * @param doc a <code>Document</code> value
     * @param prop a <code>Property</code> value
     * @param tagName a <code>String</code> value
     * @return an <code>Element</code> value
     */
    public static void insert(Element element,
                              Property prop) {
        insert(element, prop.name, prop.value);
    }

    public static void insert(Element element,
                              String name,
                              String value) {

        Document doc = element.getOwnerDocument();
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "name",
                                                      name));
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "value",
                                                      value));
    }

    /**
     * Describe <code>getProperty</code> method here.
     *
     * @param base an <code>Element</code> value
     * @return a <code>Property</code> value
     */
    public static Property getProperty(Element base) {
    String name = XMLUtil.getText(XMLUtil.getElement(base,"name"));
    String value = XMLUtil.getText(XMLUtil.getElement(base, "value"));
    return new Property(name, value);
    }

}
