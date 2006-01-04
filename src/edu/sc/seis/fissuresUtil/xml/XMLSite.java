package edu.sc.seis.fissuresUtil.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.SiteId;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.network.SiteImpl;

/**
 * XMLSite.java
 * 
 * 
 * Created: Tue Jul 9 13:38:47 2002
 * 
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */
public class XMLSite {

    /**
     * StAX insert
     */
    public static void insert(XMLStreamWriter writer, Site site)
            throws XMLStreamException {
        writer.writeStartElement("id");
        XMLSiteId.insert(writer, site.get_id());
        XMLUtil.writeEndElementWithNewLine(writer);
        writer.writeStartElement("my_location");
        XMLLocation.insert(writer, site.my_location);
        XMLUtil.writeEndElementWithNewLine(writer);
        writer.writeStartElement("effective_time");
        XMLTimeRange.insert(writer, site.effective_time);
        XMLUtil.writeEndElementWithNewLine(writer);
        writer.writeStartElement("my_station");
        XMLStation.insert(writer, site.my_station);
        XMLUtil.writeEndElementWithNewLine(writer);
        XMLUtil.writeTextElement(writer, "comment", site.comment);
    }

    /**
     * DOM insert
     */
    public static void insert(Element element, Site site) {
        Document doc = element.getOwnerDocument();
        Element id = doc.createElement("id");
        XMLSiteId.insert(id, site.get_id());
        element.appendChild(id);
        Element my_location = doc.createElement("my_location");
        XMLLocation.insert(my_location, site.my_location);
        element.appendChild(my_location);
        Element effective_time = doc.createElement("effective_time");
        XMLTimeRange.insert(effective_time, site.effective_time);
        element.appendChild(effective_time);
        Element my_station = doc.createElement("my_station");
        XMLStation.insert(my_station, site.my_station);
        element.appendChild(my_station);
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "comment",
                                                      site.comment));
    }

    public static Site getSite(Element base) {
        Element id_node = XMLUtil.getElement(base, "id");
        SiteId id = XMLSiteId.getSiteId(id_node);
        Element my_location_node = XMLUtil.getElement(base, "my_location");
        Location my_location = XMLLocation.getLocation(my_location_node);
        Element effective_time_node = XMLUtil.getElement(base, "effective_time");
        TimeRange effective_time = XMLTimeRange.getTimeRange(effective_time_node);
        Element my_station_node = XMLUtil.getElement(base, "my_station");
        Station my_station = XMLStation.getStation(my_station_node);
        String comment = XMLUtil.getText(XMLUtil.getElement(base, "comment"));
        return new SiteImpl(id,
                            my_location,
                            effective_time,
                            my_station,
                            comment);
    }

    public static Site getSite(XMLStreamReader parser) throws XMLStreamException {
        XMLUtil.gotoNextStartElement(parser, "id");
        SiteId id = XMLSiteId.getSiteId(parser);
        XMLUtil.gotoNextStartElement(parser, "my_location");
        Location my_location = XMLLocation.getLocation(parser);
        XMLUtil.gotoNextStartElement(parser, "effective_time");
        TimeRange effective_time = XMLTimeRange.getTimeRange(parser);
        XMLUtil.gotoNextStartElement(parser, "my_station");
        Station my_station = XMLStation.getStation(parser);
        XMLUtil.gotoNextStartElement(parser, "comment");
        String comment = parser.getElementText();
        return new SiteImpl(id,
                            my_location,
                            effective_time,
                            my_station,
                            comment);
    }
}// XMLSite
