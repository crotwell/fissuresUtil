package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.apache.log4j.*;

/**
 * XMLTimeRange.java
 *
 *
 * Created: Tue Jul  9 12:30:11 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class XMLTimeRange {
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
	// get the start time
	NodeList start_time_node = XMLUtil.evalNodeList(base, "start_time");
	edu.iris.Fissures.Time start_time = new edu.iris.Fissures.Time();
	if(start_time_node != null && start_time_node.getLength() != 0) {
	    start_time = XMLTime.getFissuresTime((Element)start_time_node.item(0));
	}

	//get the end time
	NodeList end_time_node = XMLUtil.evalNodeList(base, "end_time");
	edu.iris.Fissures.Time end_time = new edu.iris.Fissures.Time();
	if(end_time_node != null && end_time_node.getLength() != 0) {
	    end_time = XMLTime.getFissuresTime((Element)end_time_node.item(0));
	}
	return new TimeRange(start_time,
			     end_time);
    }
}// XMLTimeRange
