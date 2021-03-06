package edu.sc.seis.fissuresUtil.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.LocationType;
import edu.iris.Fissures.Quantity;

/**
 * XMLLocation.java
 * 
 * 
 * Created: Wed Jun 12 15:22:08 2002
 * 
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */
public class XMLLocation {

    /**
     * StAX insert
     */
    public static void insert(XMLStreamWriter writer, Location loc)
            throws XMLStreamException {
        XMLUtil.writeTextElement(writer, "latitude", "" + loc.latitude);
        XMLUtil.writeTextElement(writer, "longitude", "" + loc.longitude);
        writer.writeStartElement("elevation");
        XMLQuantity.insert(writer, loc.elevation);
        XMLUtil.writeEndElementWithNewLine(writer);
        writer.writeStartElement("depth");
        XMLQuantity.insert(writer, loc.depth);
        XMLUtil.writeEndElementWithNewLine(writer);
        if(loc.type.equals(LocationType.GEOGRAPHIC)) {
            XMLUtil.writeTextElement(writer, "type", "GEOGRAPHIC");
        } else {
            XMLUtil.writeTextElement(writer, "type", "GEOCENTRIC");
        }
    }

    /**
     * DOM insert
     */
    public static void insert(Element element, Location loc) {
        Document doc = element.getOwnerDocument();
        element.appendChild(XMLUtil.createTextElement(doc, "latitude", ""
                + loc.latitude));
        element.appendChild(XMLUtil.createTextElement(doc, "longitude", ""
                + loc.longitude));
        Element elevation = doc.createElement("elevation");
        XMLQuantity.insert(elevation, loc.elevation);
        element.appendChild(elevation);
        Element depth = doc.createElement("depth");
        XMLQuantity.insert(depth, loc.depth);
        element.appendChild(depth);
        if(loc.type.equals(LocationType.GEOGRAPHIC)) {
            element.appendChild(XMLUtil.createTextElement(doc,
                                                          "type",
                                                          "GEOGRAPHIC"));
        } else {
            element.appendChild(XMLUtil.createTextElement(doc,
                                                          "type",
                                                          "GEOCENTRIC"));
        } // end of else
    }

    public static Location getLocation(Element base) {
        float latitude = Float.parseFloat(XMLUtil.getText(XMLUtil.getElement(base,
                                                                             "latitude")));
        float longitude = Float.parseFloat(XMLUtil.getText(XMLUtil.getElement(base,
                                                                              "longitude")));
        Element elevationNode = XMLUtil.getElement(base, "elevation");
        Quantity elevation = XMLQuantity.getQuantity(elevationNode);
        Element depthNode = XMLUtil.getElement(base, "depth");
        Quantity depth = XMLQuantity.getQuantity(depthNode);
        LocationType locationType;
        String type = XMLUtil.getText(XMLUtil.getElement(base, "type"));
        if(type.equals("GEOGRAPHIC")) {
            locationType = LocationType.GEOGRAPHIC;
        } else {
            locationType = LocationType.GEOCENTRIC;
        }
        return new Location(latitude, longitude, elevation, depth, locationType);
    }

    public static Location getLocation(XMLStreamReader parser)
            throws XMLStreamException {
        XMLUtil.gotoNextStartElement(parser, "latitude");
        float latitude = Float.parseFloat(parser.getElementText());
        XMLUtil.gotoNextStartElement(parser, "longitude");
        float longitude = Float.parseFloat(parser.getElementText());
        XMLUtil.gotoNextStartElement(parser, "elevation");
        Quantity elevation = XMLQuantity.getQuantity(parser);
        XMLUtil.gotoNextStartElement(parser, "depth");
        Quantity depth = XMLQuantity.getQuantity(parser);
        LocationType locationType;
        XMLUtil.gotoNextStartElement(parser, "type");
        String type = parser.getElementText();
        if(type.equals("GEOGRAPHIC")) {
            locationType = LocationType.GEOGRAPHIC;
        } else {
            locationType = LocationType.GEOCENTRIC;
        }
        return new Location(latitude, longitude, elevation, depth, locationType);
    }
}// XMLLocation
