package edu.sc.seis.fissuresUtil.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
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
 * Created: Tue Jul 9 10:21:32 2002
 * 
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */
public class XMLNetworkAttr {

    /**
     * StAX insert
     */
    public static void insert(XMLStreamWriter writer, NetworkAttr networkAttr)
            throws XMLStreamException {
        XMLUtil.writeTextElement(writer, "name", networkAttr.getName());
        XMLUtil.writeTextElement(writer, "description", networkAttr.getDescription());
        XMLUtil.writeTextElement(writer, "owner", networkAttr.getOwner());
        writer.writeStartElement("id");
        XMLNetworkId.insert(writer, networkAttr.get_id());
        XMLUtil.writeEndElementWithNewLine(writer);
        writer.writeStartElement("effective_time");
        XMLTimeRange.insert(writer, networkAttr.getEffectiveTime());
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
        String description = XMLUtil.getText(XMLUtil.getElement(base,
                                                                "description"));
        String owner = XMLUtil.getText(XMLUtil.getElement(base, "owner"));
        Element network_id_node = XMLUtil.getElement(base, "id");
        NetworkId id = XMLNetworkId.getNetworkId(network_id_node);
        Element effective_time_node = XMLUtil.getElement(base, "effective_time");
        TimeRange effective_time = XMLTimeRange.getTimeRange(effective_time_node);
        return new NetworkAttrImpl(id, name, description, owner, effective_time);
    }

    public static NetworkAttr getNetworkAttr(XMLStreamReader parser)
            throws XMLStreamException {
        XMLUtil.gotoNextStartElement(parser, "name");
        String name = parser.getElementText();
        XMLUtil.gotoNextStartElement(parser, "description");
        String description = parser.getElementText();
        XMLUtil.gotoNextStartElement(parser, "owner");
        String owner = parser.getElementText();
        XMLUtil.gotoNextStartElement(parser, "id");
        NetworkId id = XMLNetworkId.getNetworkId(parser);
        XMLUtil.gotoNextStartElement(parser, "effective_time");
        TimeRange effective_time = XMLTimeRange.getTimeRange(parser);
        return new NetworkAttrImpl(id, name, description, owner, effective_time);
    }
}// XMLNetworkAttr
