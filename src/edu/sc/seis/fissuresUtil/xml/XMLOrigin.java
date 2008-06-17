package edu.sc.seis.fissuresUtil.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfParameterMgr.ParameterRef;
import edu.iris.Fissures.event.OriginImpl;

/**
 * XMLOrigin.java
 * 
 * 
 * Created: Wed Jun 12 09:50:35 2002
 * 
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */
public class XMLOrigin {

    public static Element translate(Document doc, Origin origin) {
        Element originE = doc.createElement("origin");
        insert(originE, origin);
        return originE;
    }

    /**
     * StAX insert
     */
    public static void insert(XMLStreamWriter writer, Origin origin)
            throws XMLStreamException {
        XMLUtil.writeTextElement(writer, "id", origin.get_id());
        XMLUtil.writeTextElement(writer, "catalog", origin.getCatalog());
        XMLUtil.writeTextElement(writer, "contributor", origin.getContributor());
        writer.writeStartElement("origin_time");
        XMLTime.insert(writer, origin.getOriginTime());
        XMLUtil.writeEndElementWithNewLine(writer);
        writer.writeStartElement("my_location");
        XMLLocation.insert(writer, origin.getLocation());
        XMLUtil.writeEndElementWithNewLine(writer);
        for(int i = 0; i < origin.getMagnitudes().length; i++) {
            writer.writeStartElement("magnitude");
            XMLMagnitude.insert(writer, origin.getMagnitudes()[i]);
            XMLUtil.writeEndElementWithNewLine(writer);
        }
    }

    /**
     * DOM insert
     */
    public static void insert(Element element, Origin origin) {
        Document doc = element.getOwnerDocument();
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "id",
                                                      origin.get_id()));
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "catalog",
                                                      origin.getCatalog()));
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "contributor",
                                                      origin.getContributor()));
        Element originTime = doc.createElement("origin_time");
        XMLTime.insert(originTime, origin.getOriginTime());
        element.appendChild(originTime);
        Element location = doc.createElement("my_location");
        XMLLocation.insert(location, origin.getLocation());
        element.appendChild(location);
        Element magnitude;
        for(int i = 0; i < origin.getMagnitudes().length; i++) {
            magnitude = doc.createElement("magnitude");
            XMLMagnitude.insert(magnitude, origin.getMagnitudes()[i]);
            element.appendChild(magnitude);
        }
    }

    public static OriginImpl getOrigin(Element base) {
        String id = XMLUtil.getText(XMLUtil.getElement(base, "id"));
        String catalog = XMLUtil.getText(XMLUtil.getElement(base, "catalog"));
        String contributor = XMLUtil.getText(XMLUtil.getElement(base,
                                                                "contributor"));
        Element originTimeNode = XMLUtil.getElement(base, "origin_time");
        edu.iris.Fissures.Time origin_time = XMLTime.getFissuresTime(originTimeNode);
        Element locationNode = XMLUtil.getElement(base, "my_location");
        Location location = XMLLocation.getLocation(locationNode);
        Element[] magnitudeList = XMLUtil.getElementArray(base, "magnitude");
        Magnitude[] magnitudes = new Magnitude[magnitudeList.length];
        for(int i = 0; i < magnitudeList.length; i++) {
            magnitudes[i] = XMLMagnitude.getMagnitude(magnitudeList[i]);
        }
        return new OriginImpl(id,
                              catalog,
                              contributor,
                              origin_time,
                              location,
                              magnitudes,
                              new ParameterRef[0]);
    }

    public static OriginImpl getOrigin(XMLStreamReader parser)
            throws XMLStreamException {
        XMLUtil.gotoNextStartElement(parser, "id");
        String id = parser.getElementText();
        XMLUtil.gotoNextStartElement(parser, "catalog");
        String catalog = parser.getElementText();
        XMLUtil.gotoNextStartElement(parser, "contributor");
        String contributor = parser.getElementText();
        XMLUtil.gotoNextStartElement(parser, "origin_time");
        edu.iris.Fissures.Time origin_time = XMLTime.getFissuresTime(parser);
        XMLUtil.gotoNextStartElement(parser, "my_location");
        Location location = XMLLocation.getLocation(parser);
        XMLUtil.getNextStartElement(parser);
        List magnitudeList = new ArrayList();
        while(parser.getLocalName().equals("magnitude")) {
            magnitudeList.add(XMLMagnitude.getMagnitude(parser));
            XMLUtil.getNextStartElement(parser);
        }
        Magnitude[] magnitudes = (Magnitude[])magnitudeList.toArray(new Magnitude[0]);
        return new OriginImpl(id,
                              catalog,
                              contributor,
                              origin_time,
                              location,
                              magnitudes,
                              new ParameterRef[0]);
    }
}// XMLOrigin
