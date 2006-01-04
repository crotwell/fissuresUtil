package edu.sc.seis.fissuresUtil.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import edu.iris.Fissures.FlinnEngdahlRegion;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.event.EventAttrImpl;
import edu.sc.seis.fissuresUtil.cache.EventUtil;

/**
 * XMLEvent.java
 * 
 * 
 * Created: Wed Jun 12 14:55:38 2002
 * 
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */
public class XMLEvent {

    /**
     * StAX insert
     */
    public static void insert(XMLStreamWriter writer,
                              EventAccessOperations event)
            throws XMLStreamException {
        XMLUtil.writeTextElement(writer, "name", event.get_attributes().name);
        writer.writeStartElement("region");
        XMLFlinnEngdahlRegion.insert(writer, event.get_attributes().region);
        XMLUtil.writeEndElementWithNewLine(writer);
        Origin prefOrigin = EventUtil.extractOrigin(event);
        writer.writeStartElement("preferred_origin");
        XMLOrigin.insert(writer, prefOrigin);
        XMLUtil.writeEndElementWithNewLine(writer);
    }

    /**
     * DOM insert
     */
    public static void insert(Element element, EventAccessOperations event) {
        Document doc = element.getOwnerDocument();
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "name",
                                                      event.get_attributes().name));
        Element feRegion = doc.createElement("region");
        XMLFlinnEngdahlRegion.insert(feRegion, event.get_attributes().region);
        element.appendChild(feRegion);
        Element prefOrigin = doc.createElement("preferred_origin");
        XMLOrigin.insert(prefOrigin, EventUtil.extractOrigin(event));
        element.appendChild(prefOrigin);
    }

    public static EventAttr getEvent(Element base) {
        String name = XMLUtil.getText(XMLUtil.getElement(base, "name"));
        Element region = XMLUtil.getElement(base, "region");
        FlinnEngdahlRegion flinnEngdahlRegion = XMLFlinnEngdahlRegion.getRegion(region);
        return new EventAttrImpl(name, flinnEngdahlRegion);
    }

    public static EventAttr getEvent(XMLStreamReader parser)
            throws XMLStreamException {
        XMLUtil.gotoNextStartElement(parser, "name");
        String name = parser.getElementText();
        FlinnEngdahlRegion flinnEngdahlRegion = XMLFlinnEngdahlRegion.getRegion(parser);
        return new EventAttrImpl(name, flinnEngdahlRegion);
    }

    public static Origin getPreferredOrigin(Element base) {
        Element preferred_originElement = XMLUtil.getElement(base,
                                                             "preferred_origin");
        Origin preferred_origin;
        if(preferred_originElement != null) {
            preferred_origin = XMLOrigin.getOrigin(preferred_originElement);
        } else
            preferred_origin = null;
        return preferred_origin;
    }

    public static Origin getPreferredOrigin(XMLStreamReader parser) throws XMLStreamException {
        XMLUtil.gotoNextStartElement(parser, "preferred_origin");
        if(parser.hasNext()){
            return XMLOrigin.getOrigin(parser);
        } else {
            return null;
        }
    }
}// XMLEvent
