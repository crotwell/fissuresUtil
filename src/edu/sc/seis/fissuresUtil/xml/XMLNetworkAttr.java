package edu.sc.seis.fissuresUtil.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.network.NetworkAttrImpl;

/**
 * XMLNetworkAttr.java
 *
 *
 * Created: Tue Jul  9 10:21:32 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class XMLNetworkAttr {

    /**
     * StAX insert
     */
    public static void insert(XMLStreamWriter writer, NetworkAttr networkAttr)
        throws XMLStreamException{

        XMLUtil.writeTextElement(writer, "name", networkAttr.name);
        XMLUtil.writeTextElement(writer, "description", networkAttr.description);
        XMLUtil.writeTextElement(writer, "owner", networkAttr.owner);

        writer.writeStartElement("id");
        XMLNetworkId.insert(writer, networkAttr.get_id());
        XMLUtil.writeEndElementWithNewLine(writer);

        writer.writeStartElement("effective_time");
        XMLTimeRange.insert(writer, networkAttr.effective_time);
        XMLUtil.writeEndElementWithNewLine(writer);
    }

    /**
     * DOM insert
     */
    public static void insert(Element element, NetworkAttr networkAttr) {

        Document doc = element.getOwnerDocument();

        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "name",
                                                      networkAttr.name));
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "description",
                                                      networkAttr.description));
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "owner",
                                                      networkAttr.owner));
        Element network_id = doc.createElement("id");
        XMLNetworkId.insert(network_id, networkAttr.get_id());
        element.appendChild(network_id);

        Element effective_time_range = doc.createElement("effective_time");
        XMLTimeRange.insert(effective_time_range, networkAttr.effective_time);
        element.appendChild(effective_time_range);
    }

    public static NetworkAttr getNetworkAttr(Element base) {

        String name = XMLUtil.getText(XMLUtil.getElement(base, "name"));
        String description = XMLUtil.getText(XMLUtil.getElement(base, "description"));
        String owner = XMLUtil.getText(XMLUtil.getElement(base, "owner"));
        //get the networkId
        NetworkId id = null;
        Element network_id_node = XMLUtil.getElement(base, "id");
        if(network_id_node != null) {
            id = XMLNetworkId.getNetworkId(network_id_node);
        }

        //get the effective time Range
        Element effective_time_node = XMLUtil.getElement(base, "effective_time");
        TimeRange effective_time = new TimeRange();
        if(effective_time_node != null) {
            effective_time = XMLTimeRange.getTimeRange(effective_time_node);
        }
        return new NetworkAttrImpl(id,
                                   name,
                                   description,
                                   owner,
                                   effective_time);

    }
    //    public static NetworkAttr getNetworkAttr(Element base) {

    //  String name = XMLUtil.evalString(base, "name");
    //  String description = XMLUtil.evalString(base, "description");
    //  String owner = XMLUtil.evalString(base, "owner");
    //  //get the networkId
    //  NetworkId id = null;
    //  NodeList network_id_node = XMLUtil.evalNodeList(base, "id");
    //  if(network_id_node != null && network_id_node.getLength() != 0) {
    //      id = XMLNetworkId.getNetworkId((Element)network_id_node.item(0));
    //  }

    //  //get the effective time Range
    //  NodeList effective_time_node = XMLUtil.evalNodeList(base, "effective_time");
    //  TimeRange effective_time = new TimeRange();
    //  if(effective_time_node != null && effective_time_node.getLength() != 0) {
    //      effective_time = XMLTimeRange.getTimeRange((Element)effective_time_node.item(0));
    //  }
    //  return new NetworkAttrImpl(id,
    //                 name,
    //                 description,
    //                 owner,
    //                 effective_time);

    //     }
}// XMLNetworkAttr
