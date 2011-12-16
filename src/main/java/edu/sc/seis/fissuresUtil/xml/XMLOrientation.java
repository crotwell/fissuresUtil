package edu.sc.seis.fissuresUtil.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.iris.Fissures.Orientation;

/**
 * XMLOrientation.java
 * 
 * 
 * Created: Tue Jul 9 14:26:54 2002
 * 
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */
public class XMLOrientation {

    /**
     * StAX insert method
     */
    public static void insert(XMLStreamWriter writer, Orientation orientation)
            throws XMLStreamException {
        XMLUtil.writeTextElement(writer, "azimuth", "" + orientation.azimuth);
        XMLUtil.writeTextElement(writer, "dip", "" + orientation.dip);
    }

    /**
     * DOM insert method
     */
    public static void insert(Element element, Orientation orientation) {
        Document doc = element.getOwnerDocument();
        element.appendChild(XMLUtil.createTextElement(doc, "azimuth", ""
                + orientation.azimuth));
        element.appendChild(XMLUtil.createTextElement(doc, "dip", ""
                + orientation.dip));
    }

    public static Orientation getOrientation(Element base) {
        float azimuth;
        try {
            azimuth = Float.parseFloat(XMLUtil.getText(XMLUtil.getElement(base,
                                                                          "azimuth")));
        } catch(NumberFormatException nfe) {
            azimuth = 0.0f;
        }
        float dip;
        try {
            dip = Float.parseFloat(XMLUtil.getText(XMLUtil.getElement(base,
                                                                      "dip")));
        } catch(NumberFormatException nfe) {
            dip = 0.0f;
        }
        return new Orientation(azimuth, dip);
    }
    
    public static Orientation getOrientation(XMLStreamReader parser) throws XMLStreamException {
        float azimuth;
        try {
            XMLUtil.gotoNextStartElement(parser, "azimuth");
            azimuth = Float.parseFloat(parser.getElementText());
        } catch(NumberFormatException nfe) {
            azimuth = 0.0f;
        }
        float dip;
        try {
            XMLUtil.gotoNextStartElement(parser, "dip");
            dip = Float.parseFloat(parser.getElementText());
        } catch(NumberFormatException nfe) {
            dip = 0.0f;
        }
        return new Orientation(azimuth, dip);
    }
    
}// XMLOrientation
