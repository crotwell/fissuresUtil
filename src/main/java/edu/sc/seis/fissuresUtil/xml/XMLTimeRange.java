package edu.sc.seis.fissuresUtil.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.iris.Fissures.TimeRange;

/**
 * XMLTimeRange.java
 * 
 * 
 * Created: Tue Jul 9 12:30:11 2002
 * 
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */
public class XMLTimeRange {

    /**
     * StAX insert
     */
    public static void insert(XMLStreamWriter writer, TimeRange timeRange)
            throws XMLStreamException {
        writer.writeStartElement("start_time");
        XMLTime.insert(writer, timeRange.start_time);
        XMLUtil.writeEndElementWithNewLine(writer);
        writer.writeStartElement("end_time");
        XMLTime.insert(writer, timeRange.end_time);
        XMLUtil.writeEndElementWithNewLine(writer);
    }

    /**
     * DOM insert
     */
    public static void insert(Element element, TimeRange timeRange) {
        Document doc = element.getOwnerDocument();
        Element start_time = doc.createElement("start_time");
        XMLTime.insert(start_time, timeRange.start_time);
        element.appendChild(start_time);
        Element end_time = doc.createElement("end_time");
        XMLTime.insert(end_time, timeRange.end_time);
        element.appendChild(end_time);
    }

    public static TimeRange getTimeRange(Element base) {
        Element start_time_node = XMLUtil.getElement(base, "start_time");
        edu.iris.Fissures.Time start_time = XMLTime.getFissuresTime(start_time_node);
        Element end_time_node = XMLUtil.getElement(base, "end_time");
        edu.iris.Fissures.Time end_time = XMLTime.getFissuresTime(end_time_node);
        return new TimeRange(start_time, end_time);
    }
    
    public static TimeRange getTimeRange(XMLStreamReader parser) throws XMLStreamException {
        XMLUtil.gotoNextStartElement(parser, "start_time");
        edu.iris.Fissures.Time start_time = XMLTime.getFissuresTime(parser);
        XMLUtil.gotoNextStartElement(parser, "end_time");
        edu.iris.Fissures.Time end_time = XMLTime.getFissuresTime(parser);
        return new TimeRange(start_time, end_time);
    }
    
}// XMLTimeRange
