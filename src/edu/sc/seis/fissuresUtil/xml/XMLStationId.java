package edu.sc.seis.fissuresUtil.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.StationId;

/**
 * XMLStationId.java
 *
 *
 * Created: Tue Jul  9 12:48:58 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class XMLStationId {

    /**
     * StAX insert
     */
    public static void insert(XMLStreamWriter writer, StationId stationId)
        throws XMLStreamException{

        writer.writeStartElement("network_id");
        XMLNetworkId.insert(writer, stationId.network_id);
        XMLUtil.writeEndElementWithNewLine(writer);

        XMLUtil.writeTextElement(writer, "station_code", stationId.station_code);

        writer.writeStartElement("begin_time");
        XMLTime.insert(writer, stationId.begin_time);
        XMLUtil.writeEndElementWithNewLine(writer);
    }

    /**
     * DOM insert
     */
    public static void insert(Element element, StationId stationId) {

        Document doc = element.getOwnerDocument();
        Element network_id = doc.createElement("network_id");
        XMLNetworkId.insert(network_id, stationId.network_id);
        element.appendChild(network_id);

        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "station_code",
                                                      stationId.station_code));
        Element begin_time = doc.createElement("begin_time");
        XMLTime.insert(begin_time, stationId.begin_time);
        element.appendChild(begin_time);
    }

    public static StationId getStationId(Element base) {
        //get the network_id
        Element network_id_node = XMLUtil.getElement(base, "network_id");
        NetworkId network_id = null;
        if(network_id_node != null) {
            network_id = XMLNetworkId.getNetworkId(network_id_node);
        }

        //get the station_code
        String station_code = XMLUtil.getText(XMLUtil.getElement(base, "station_code"));

        //get the begin_time
        Element begin_time_node = XMLUtil.getElement(base, "begin_time");
        edu.iris.Fissures.Time begin_time = new edu.iris.Fissures.Time();
        if(begin_time_node != null) {
            begin_time = XMLTime.getFissuresTime(begin_time_node);
        }

        return new StationId(network_id,
                             station_code,
                             begin_time);
    }

    //  public static StationId getStationId(Element base) {
    //  //get the network_id
    //  NodeList network_id_node = XMLUtil.evalNodeList(base, "network_id");
    //  NetworkId network_id = null;
    //  if(network_id_node != null && network_id_node.getLength() != 0) {
    //      network_id = XMLNetworkId.getNetworkId((Element)network_id_node.item(0));
    //  }

    //  //get the station_code
    //  String station_code = XMLUtil.evalString(base, "station_code");

    //  //get the begin_time
    //  NodeList begin_time_node = XMLUtil.evalNodeList(base, "begin_time");
    //  edu.iris.Fissures.Time begin_time = new edu.iris.Fissures.Time();
    //  if(begin_time_node != null && begin_time_node.getLength() != 0) {
    //      begin_time = XMLTime.getFissuresTime((Element)begin_time_node.item(0));
    //  }

    //  return new StationId(network_id,
    //               station_code,
    //               begin_time);
    //     }

}// XMLStationId
