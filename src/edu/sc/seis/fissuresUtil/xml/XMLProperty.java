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
     * Describe <code>parse</code> method here.
     *
     * @param element an <code>Element</code> value
     * @return a <code>Property</code> value
     */
    public static Property parse(Element element) {
	try {
	    String name = XMLUtil.getText(XMLUtil.getElement(element,"name"));
	    String value = XMLUtil.getText(XMLUtil.getElement(element, "value"));
	    return new Property(name, value);
	} catch (Exception e) {
	    return null;
	} // end of try-catch
	
    }

    /**
     * Describe <code>createElement</code> method here.
     *
     * @param doc a <code>Document</code> value
     * @param prop a <code>Property</code> value
     * @param tagName a <code>String</code> value
     * @return an <code>Element</code> value
     */
    public static Element createElement(Document doc, 
					Property prop, 
					String tagName) {
	Element element = doc.createElement(tagName);

	Text textNode = doc.createTextNode(prop.name);
	Element tempElement = doc.createElement("name");
	tempElement.appendChild(textNode);
	element.appendChild(tempElement);

	textNode = doc.createTextNode(prop.value);
	tempElement = doc.createElement("value");
	tempElement.appendChild(textNode);
	element.appendChild(tempElement);
	
	return element;
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
	System.out.println("While getting property name= "+name+" value= "+value);
	return new Property(name, value);
    }

}
