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

public class XMLProperty {

    public static Property parse(Element element) {
	try {
	    String name = xpath.eval(element, "name/text()").str();
	    String value = xpath.eval(element, "value/text()").str();
	    return new Property(name, value);
	} catch (Exception e) {
	    return null;
	} // end of try-catch
	
    }

    public static Element createElement(Document doc, 
					Property prop, 
					String tagName) {
	//System.out.println("The name of the property is "+prop.name);
	//System.out.println("The value of the property is "+prop.value);
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

    public static Property getProperty(Element base) {
	String name = XMLUtil.getText(XMLUtil.getElement(base,"name"));
	String value = XMLUtil.getText(XMLUtil.getElement(base, "value"));
	return new Property(name, value);
    }

//  public static Property getProperty(Element base) {
// 	String name = XMLUtil.evalString(base,"name");
// 	String value = XMLUtil.evalString(base, "value");
// 	return new Property(name, value);
//     }
    private static CachedXPathAPI xpath = new CachedXPathAPI();
}
