package edu.sc.seis.fissuresUtil.xml;

import javax.xml.stream.XMLStreamException;
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
 * Created: Tue Jul  9 13:00:49 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class XMLStation {

    /**
     * StAX insert
     */
    public static void insert(XMLStreamWriter writer, Station station)
        throws XMLStreamException{

        writer.writeStartElement("id");
        XMLStationId.insert(writer, station.get_id());
        XMLUtil.writeEndElementWithNewLine(writer);

        XMLUtil.writeTextElement(writer, "name", station.name);

        writer.writeStartElement("my_location");
        XMLLocation.insert(writer, station.my_location);
        XMLUtil.writeEndElementWithNewLine(writer);

        writer.writeStartElement("effective_time");
        XMLTimeRange.insert(writer, station.effective_time);
        XMLUtil.writeEndElementWithNewLine(writer);

        XMLUtil.writeTextElement(writer, "operator", station.operator);
        XMLUtil.writeTextElement(writer, "description", station.description);
        XMLUtil.writeTextElement(writer, "comment", station.comment);

        writer.writeStartElement("my_network");
        XMLNetworkAttr.insert(writer, station.my_network);
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

        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "name",
                                                      station.name));

        Element my_location = doc.createElement("my_location");
        XMLLocation.insert(my_location, station.my_location);
        element.appendChild(my_location);

        Element effective_time = doc.createElement("effective_time");
        XMLTimeRange.insert(effective_time, station.effective_time);
        element.appendChild(effective_time);

        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "operator",
                                                      station.operator));

        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "description",
                                                      station.description));

        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "comment",
                                                      station.comment));

        Element my_network = doc.createElement("my_network");
        XMLNetworkAttr.insert(my_network, station.my_network);
        element.appendChild(my_network);
    }

    public static Station getStation(Element base) {
        //get the station id
        Element id_node = XMLUtil.getElement(base, "id");
        StationId id = null;
        if(id_node != null) {
            id = XMLStationId.getStationId(id_node);
        }

        //get the name
        String name = XMLUtil.getText(XMLUtil.getElement(base, "name"));

        //get my_location
        Element my_location_node = XMLUtil.getElement(base, "my_location");
        Location my_location = null;
        if(my_location_node !=  null) {
            my_location = XMLLocation.getLocation(my_location_node);
        }

        //get effective_time range
        Element effective_time_node = XMLUtil.getElement(base, "effective_time");
        TimeRange effective_time = new TimeRange();
        if(effective_time_node != null) {
            effective_time = XMLTimeRange.getTimeRange(effective_time_node);
        }

        //get the operator
        String operator = XMLUtil.getText(XMLUtil.getElement(base, "operator"));

        //get the description
        String description = XMLUtil.getText(XMLUtil.getElement(base, "description"));

        //get the comment
        String comment = XMLUtil.getText(XMLUtil.getElement(base, "comment"));

        //get the my_network
        Element my_network_node = XMLUtil.getElement(base, "my_network");
        NetworkAttr my_network = null;
        if(my_network_node != null) {

            my_network = XMLNetworkAttr.getNetworkAttr(my_network_node);
        }

        return new StationImpl(id,
                               name,
                               my_location,
                               effective_time,
                               operator,
                               description,
                               comment,
                               my_network);

    }



    //   public static Station getStation(Element base) {
    //  //get the station id
    //  NodeList id_node = XMLUtil.evalNodeList(base, "id");
    //  StationId id = null;
    //  if(id_node != null && id_node.getLength() != 0) {
    //      id = XMLStationId.getStationId((Element)id_node.item(0));
    //  }

    //  //get the name
    //  String name = XMLUtil.evalString(base, "name");

    //  //get my_location
    //  NodeList my_location_node = XMLUtil.evalNodeList(base, "my_location");
    //  Location my_location = null;
    //  if(my_location_node !=  null && my_location_node.getLength() != 0) {
    //      my_location = XMLLocation.getLocation((Element)my_location_node.item(0));
    //  }

    //  //get effective_time range
    //  NodeList effective_time_node = XMLUtil.evalNodeList(base, "effective_time");
    //  TimeRange effective_time = new TimeRange();
    //  if(effective_time_node != null && effective_time_node.getLength() != 0) {
    //      effective_time = XMLTimeRange.getTimeRange((Element)effective_time_node.item(0));
    //  }

    //  //get the operator
    //  String operator = XMLUtil.evalString(base, "operator");

    //  //get the description
    //  String description = XMLUtil.evalString(base, "description");

    //  //get the comment
    //  String comment = XMLUtil.evalString(base, "comment");

    //  //get the my_network
    //  NodeList my_network_node = XMLUtil.evalNodeList(base, "my_network");
    //  NetworkAttr my_network = null;
    //  if(my_network_node != null && my_network_node.getLength() != 0) {

    //      my_network = XMLNetworkAttr.getNetworkAttr((Element)my_network_node.item(0));
    //  }

    //  return new StationImpl(id,
    //                 name,
    //                 my_location,
    //                 effective_time,
    //                 operator,
    //                 description,
    //                 comment,
    //                 my_network);

    //     }
}// XMLStation
