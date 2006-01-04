package edu.sc.seis.fissuresUtil.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfSeismogramDC.Property;

/**
 * Describe class <code>XMLProperty</code> here.
 * 
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class XMLProperty {

    /**
     * StAX insert
     */
    public static void insert(XMLStreamWriter writer, Property prop)
            throws XMLStreamException {
        insert(writer, prop.name, prop.value);
    }

    /**
     * StAX insert
     */
    public static void insert(XMLStreamWriter writer, String name, String value)
            throws XMLStreamException {
        XMLUtil.writeTextElement(writer, "name", name);
        XMLUtil.writeTextElement(writer, "value", value);
    }

    /**
     * DOM insert
     */
    public static void insert(Element element, Property prop) {
        insert(element, prop.name, prop.value);
    }

    /**
     * DOM insert
     */
    public static void insert(Element element, String name, String value) {
        Document doc = element.getOwnerDocument();
        element.appendChild(XMLUtil.createTextElement(doc, "name", name));
        element.appendChild(XMLUtil.createTextElement(doc, "value", value));
    }

    /**
     * Describe <code>getProperty</code> method here.
     * 
     * @param base
     *            an <code>Element</code> value
     * @return a <code>Property</code> value
     */
    public static Property getProperty(Element base) {
        String name = XMLUtil.getText(XMLUtil.getElement(base, "name"));
        String value = XMLUtil.getText(XMLUtil.getElement(base, "value"));
        return new Property(name, value);
    }

    public static Property getProperty(XMLStreamReader parser)
            throws XMLStreamException {
        XMLUtil.gotoNextStartElement(parser, "name");
        String name = parser.getElementText();
        XMLUtil.gotoNextStartElement(parser, "value");
        String value = parser.getElementText();
        return new Property(name, value);
    }
}
