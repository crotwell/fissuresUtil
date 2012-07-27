package edu.sc.seis.fissuresUtil.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.NetworkId;

/**
 * XMLNetworkId.java
 * 
 * 
 * Created: Mon Jul 1 14:52:45 2002
 * 
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */
public class XMLNetworkId {

    /**
     * StAX insert method
     */
    public static void insert(XMLStreamWriter writer, NetworkId networkId)
            throws XMLStreamException {
        XMLUtil.writeTextElement(writer, "network_code", networkId.network_code);
        writer.writeStartElement("begin_time");
        XMLTime.insert(writer, networkId.begin_time);
        XMLUtil.writeEndElementWithNewLine(writer);
    }

    /**
     * DOM insert method
     */
    public static void insert(Element element, NetworkId networkId) {
        Document doc = element.getOwnerDocument();
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "network_code",
                                                      networkId.network_code));
        Element begin_time = doc.createElement("begin_time");
        XMLTime.insert(begin_time, networkId.begin_time);
        element.appendChild(begin_time);
    }

    public static NetworkId getNetworkId(Element base) {
        String network_code = XMLUtil.getText(XMLUtil.getElement(base,
                                                                 "network_code"));
        Element begin_time_node = XMLUtil.getElement(base, "begin_time");
        edu.iris.Fissures.Time begin_time = XMLTime.getFissuresTime(begin_time_node);
        return new NetworkId(network_code, begin_time);
    }
    
    public static NetworkId getNetworkId(XMLStreamReader parser) throws XMLStreamException {
        XMLUtil.gotoNextStartElement(parser, "network_code");
        String network_code = parser.getElementText();
        XMLUtil.gotoNextStartElement(parser, "begin_time");
        edu.iris.Fissures.Time begin_time = XMLTime.getFissuresTime(parser);
        return new NetworkId(network_code, begin_time);
    }
    
}// XMLNetworkId
