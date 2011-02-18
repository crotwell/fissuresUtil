package edu.sc.seis.fissuresUtil.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.network.StationImpl;

/**
 * XMLStation.java
 * 
 * 
 * Created: Tue Jul 9 13:00:49 2002
 * 
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */
public class XMLStation {

    /**
     * StAX insert
     */
    public static void insert(XMLStreamWriter writer, Station station)
            throws XMLStreamException {
        writer.writeStartElement("id");
        XMLStationId.insert(writer, station.get_id());
        XMLUtil.writeEndElementWithNewLine(writer);
        XMLUtil.writeTextElement(writer, "name", station.getName());
        writer.writeStartElement("my_location");
        XMLLocation.insert(writer, station.getLocation());
        XMLUtil.writeEndElementWithNewLine(writer);
        writer.writeStartElement("effective_time");
        XMLTimeRange.insert(writer, station.getEffectiveTime());
        XMLUtil.writeEndElementWithNewLine(writer);
        XMLUtil.writeTextElement(writer, "operator", station.getOperator());
        XMLUtil.writeTextElement(writer, "description", station.getDescription());
        XMLUtil.writeTextElement(writer, "comment", station.getComment());
        writer.writeStartElement("my_network");
        XMLNetworkAttr.insert(writer, station.getNetworkAttr());
        XMLUtil.writeEndElementWithNewLine(writer);
    }

    /**
     * DOM insert
     */
    public static void insert(Element element, Station station) {
        Document doc = element.getOwnerDocument();
        Element id = doc.createElement("id");
        XMLStationId.insert(id, station.get_id());
        element.appendChild(id);
        element.appendChild(XMLUtil.createTextElement(doc, "name", station.getName()));
        Element my_location = doc.createElement("my_location");
        XMLLocation.insert(my_location, station.getLocation());
        element.appendChild(my_location);
        Element effective_time = doc.createElement("effective_time");
        XMLTimeRange.insert(effective_time, station.getEffectiveTime());
        element.appendChild(effective_time);
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "operator",
                                                      station.getOperator()));
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "description",
                                                      station.getDescription()));
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "comment",
                                                      station.getComment()));
        Element my_network = doc.createElement("my_network");
        XMLNetworkAttr.insert(my_network, station.getNetworkAttr());
        element.appendChild(my_network);
    }

    public static Station getStation(Element base) {
        Element id_node = XMLUtil.getElement(base, "id");
        StationId id = XMLStationId.getStationId(id_node);
        String name = XMLUtil.getText(XMLUtil.getElement(base, "name"));
        Element my_location_node = XMLUtil.getElement(base, "my_location");
        Location my_location = XMLLocation.getLocation(my_location_node);
        Element effective_time_node = XMLUtil.getElement(base, "effective_time");
        TimeRange effective_time = XMLTimeRange.getTimeRange(effective_time_node);
        String operator = XMLUtil.getText(XMLUtil.getElement(base, "operator"));
        String description = XMLUtil.getText(XMLUtil.getElement(base,
                                                                "description"));
        String comment = XMLUtil.getText(XMLUtil.getElement(base, "comment"));
        Element my_network_node = XMLUtil.getElement(base, "my_network");
        NetworkAttr my_network = XMLNetworkAttr.getNetworkAttr(my_network_node);
        return new StationImpl(id,
                               name,
                               my_location,
                               effective_time,
                               operator,
                               description,
                               comment,
                               my_network);
    }

    public static Station getStation(XMLStreamReader parser) throws XMLStreamException {
        XMLUtil.gotoNextStartElement(parser, "id");
        StationId id = XMLStationId.getStationId(parser);
        XMLUtil.gotoNextStartElement(parser, "name");
        String name = parser.getElementText();
        XMLUtil.gotoNextStartElement(parser, "my_location");
        Location my_location = XMLLocation.getLocation(parser);
        XMLUtil.gotoNextStartElement(parser, "effective_time");
        TimeRange effective_time = XMLTimeRange.getTimeRange(parser);
        XMLUtil.gotoNextStartElement(parser, "operator");
        String operator = parser.getElementText();
        XMLUtil.gotoNextStartElement(parser, "description");
        String description = parser.getElementText();
        XMLUtil.gotoNextStartElement(parser, "comment");
        String comment = parser.getElementText();
        XMLUtil.gotoNextStartElement(parser, "my_network");
        NetworkAttr my_network = XMLNetworkAttr.getNetworkAttr(parser);
        return new StationImpl(id,
                               name,
                               my_location,
                               effective_time,
                               operator,
                               description,
                               comment,
                               my_network);
    }
}// XMLStation
