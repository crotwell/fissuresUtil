package edu.sc.seis.fissuresUtil.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkId;

/**
 * XMLChannelId.java
 * 
 * 
 * Created: Mon Jul 1 14:52:45 2002
 * 
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */
public class XMLChannelId {

    /**
     * StAX insert method
     */
    public static void insert(XMLStreamWriter writer, ChannelId channelId)
            throws XMLStreamException {
        writer.writeStartElement("network_id");
        XMLNetworkId.insert(writer, channelId.network_id);
        XMLUtil.writeEndElementWithNewLine(writer);
        XMLUtil.writeTextElement(writer, "station_code", channelId.station_code);
        XMLUtil.writeTextElement(writer, "site_code", channelId.site_code);
        XMLUtil.writeTextElement(writer, "channel_code", channelId.channel_code);
        writer.writeStartElement("begin_time");
        XMLTime.insert(writer, channelId.begin_time);
        XMLUtil.writeEndElementWithNewLine(writer);
    }

    /**
     * DOM insert method
     */
    public static void insert(Element element, ChannelId channelId) {
        Document doc = element.getOwnerDocument();
        Element network_id = doc.createElement("network_id");
        XMLNetworkId.insert(network_id, channelId.network_id);
        element.appendChild(network_id);
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "station_code",
                                                      channelId.station_code));
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "site_code",
                                                      channelId.site_code));
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "channel_code",
                                                      channelId.channel_code));
        Element begin_time = doc.createElement("begin_time");
        XMLTime.insert(begin_time, channelId.begin_time);
        element.appendChild(begin_time);
    }

    public static ChannelId getChannelId(Element base) {
        Element network_id_node = XMLUtil.getElement(base, "network_id");
        NetworkId network_id = XMLNetworkId.getNetworkId(network_id_node);
        String station_code = XMLUtil.getText(XMLUtil.getElement(base,
                                                                 "station_code"));
        String site_code = XMLUtil.getText(XMLUtil.getElement(base, "site_code"));
        String channel_code = XMLUtil.getText(XMLUtil.getElement(base,
                                                                 "channel_code"));
        Element begin_time_node = XMLUtil.getElement(base, "begin_time");
        edu.iris.Fissures.Time begin_time = XMLTime.getFissuresTime(begin_time_node);
        return new ChannelId(network_id,
                             station_code,
                             site_code,
                             channel_code,
                             begin_time);
    }

    public static ChannelId getChannelId(XMLStreamReader parser)
            throws XMLStreamException {
        XMLUtil.gotoNextStartElement(parser, "network_id");
        NetworkId network_id = XMLNetworkId.getNetworkId(parser);
        XMLUtil.gotoNextStartElement(parser, "station_code");
        String station_code = parser.getElementText();
        XMLUtil.gotoNextStartElement(parser, "site_code");
        String site_code = parser.getElementText();
        XMLUtil.gotoNextStartElement(parser, "channel_code");
        String channel_code = parser.getElementText();
        XMLUtil.gotoNextStartElement(parser, "begin_time");
        edu.iris.Fissures.Time begin_time = XMLTime.getFissuresTime(parser);
        return new ChannelId(network_id,
                             station_code,
                             site_code,
                             channel_code,
                             begin_time);
    }
}// XMLChannelId
