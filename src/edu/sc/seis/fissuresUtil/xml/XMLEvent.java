package edu.sc.seis.fissuresUtil.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import edu.iris.Fissures.FlinnEngdahlRegion;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.event.EventAttrImpl;

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
    public static void insert(XMLStreamWriter writer, EventAccessOperations event)
        throws XMLStreamException{

        XMLUtil.writeTextElement(writer, "name", event.get_attributes().name);

        writer.writeStartElement("region");
        XMLFlinnEngdahlRegion.insert(writer, event.get_attributes().region);
        XMLUtil.writeEndElementWithNewLine(writer);

        try{
            Origin prefOrigin = event.get_preferred_origin();
            writer.writeStartElement("preferred_origin");
            XMLOrigin.insert(writer, prefOrigin);
            XMLUtil.writeEndElementWithNewLine(writer);
        }
        catch (NoPreferredOrigin e){}
    }

    /**
     * DOM insert
     */
    public static void insert(Element element,
                              EventAccessOperations event)  {
        Document doc = element.getOwnerDocument();
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "name",
                                                      event.get_attributes().name));
        Element feRegion = doc.createElement("region");
        XMLFlinnEngdahlRegion.insert(feRegion, event.get_attributes().region);
        element.appendChild(feRegion);

        try {
            Element prefOrigin = doc.createElement("preferred_origin");
            XMLOrigin.insert(prefOrigin, event.get_preferred_origin());
            element.appendChild(prefOrigin);
        } catch (NoPreferredOrigin e) {

        } // end of try-catch

    }

    public static EventAttr getEvent(Element base) {
        String name = XMLUtil.getText(XMLUtil.getElement(base, "name"));
        Element regionNode = XMLUtil.getElement(base, "region");
        Element regionElement = null;
        if(regionNode != null) {
            regionElement = regionNode;
        }
        FlinnEngdahlRegion flinnEngdahlRegion =
            XMLFlinnEngdahlRegion.getRegion(regionElement);
        return new EventAttrImpl(name,
                                 flinnEngdahlRegion);
    }

    public static Origin getPreferredOrigin(Element base) {
        Element preferred_originNode = XMLUtil.getElement(base,"preferred_origin");
        Element preferred_originElement = null;
        Origin preferred_origin;
        if(preferred_originNode != null) {
            preferred_originElement = preferred_originNode;
            preferred_origin =
                XMLOrigin.getOrigin(preferred_originElement);
        }  else preferred_origin = null;
        return preferred_origin;
    }

}// XMLEvent
