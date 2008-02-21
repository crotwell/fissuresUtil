package edu.sc.seis.fissuresUtil.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import edu.iris.Fissures.Orientation;
import edu.iris.Fissures.Sampling;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.network.ChannelImpl;

/**
 * XMLChannel.java
 * 
 * 
 * Created: Tue Jul 9 14:12:13 2002
 * 
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */
public class XMLChannel {

    /**
     * StAX insert method
     */
    public static void insert(XMLStreamWriter writer, ChannelImpl channel)
            throws XMLStreamException {
        writer.writeStartElement("id");
        XMLChannelId.insert(writer, channel.get_id());
        XMLUtil.writeEndElementWithNewLine(writer);
        XMLUtil.writeTextElement(writer, "name", channel.getName());
        writer.writeStartElement("an_orientation");
        XMLOrientation.insert(writer, channel.getOrientation());
        XMLUtil.writeEndElementWithNewLine(writer);
        writer.writeStartElement("sampling_info");
        XMLSampling.insert(writer, channel.getSamplingInfo());
        XMLUtil.writeEndElementWithNewLine(writer);
        writer.writeStartElement("effective_time");
        XMLTimeRange.insert(writer, channel.getEffectiveTime());
        XMLUtil.writeEndElementWithNewLine(writer);
        writer.writeStartElement("my_site");
        XMLSite.insert(writer, channel.getSite());
        XMLUtil.writeEndElementWithNewLine(writer);
    }

    /**
     * DOM insert method
     */
    public static void insert(Element element, Channel channel) {
        Document doc = element.getOwnerDocument();
        Element id = doc.createElement("id");
        XMLChannelId.insert(id, channel.get_id());
        element.appendChild(id);
        element.appendChild(XMLUtil.createTextElement(doc, "name", channel.name));
        Element an_orientation = doc.createElement("an_orientation");
        XMLOrientation.insert(an_orientation, channel.an_orientation);
        element.appendChild(an_orientation);
        Element sampling_info = doc.createElement("sampling_info");
        XMLSampling.insert(sampling_info, channel.sampling_info);
        element.appendChild(sampling_info);
        Element effective_time = doc.createElement("effective_time");
        XMLTimeRange.insert(effective_time, channel.effective_time);
        element.appendChild(effective_time);
        Element my_site = doc.createElement("my_site");
        XMLSite.insert(my_site, channel.my_site);
        element.appendChild(my_site);
    }

    public static Channel getChannel(Element base) {
        Element id_node = XMLUtil.getElement(base, "id");
        ChannelId id = XMLChannelId.getChannelId(id_node);
        String name = XMLUtil.getText(XMLUtil.getElement(base, "name"));
        Element an_orientation_node = XMLUtil.getElement(base, "an_orientation");
        Orientation an_orientation = XMLOrientation.getOrientation(an_orientation_node);
        Element sampling_info_node = XMLUtil.getElement(base, "sampling_info");
        Sampling sampling_info = XMLSampling.getSampling(sampling_info_node);
        Element effective_time_node = XMLUtil.getElement(base, "effective_time");
        TimeRange effective_time = XMLTimeRange.getTimeRange(effective_time_node);
        Element my_site_node = XMLUtil.getElement(base, "my_site");
        Site my_site = XMLSite.getSite(my_site_node);
        return new ChannelImpl(id,
                               name,
                               an_orientation,
                               sampling_info,
                               effective_time,
                               my_site);
    }

    public static Channel getChannel(XMLStreamReader parser)
            throws XMLStreamException {
        XMLUtil.gotoNextStartElement(parser, "id");
        ChannelId id = XMLChannelId.getChannelId(parser);
        XMLUtil.gotoNextStartElement(parser, "name");
        String name = parser.getElementText();
        XMLUtil.gotoNextStartElement(parser, "an_orientation");
        Orientation an_orientation = XMLOrientation.getOrientation(parser);
        XMLUtil.gotoNextStartElement(parser, "sampling_info");
        Sampling sampling_info = XMLSampling.getSampling(parser);
        XMLUtil.gotoNextStartElement(parser, "effective_time");
        TimeRange effective_time = XMLTimeRange.getTimeRange(parser);
        XMLUtil.gotoNextStartElement(parser, "my_site");
        Site my_site = XMLSite.getSite(parser);
        return new ChannelImpl(id,
                               name,
                               an_orientation,
                               sampling_info,
                               effective_time,
                               my_site);
    }
}// XMLChannel
