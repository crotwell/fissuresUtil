package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;
import edu.iris.Fissures.IfNetwork.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.apache.log4j.*;

/**
 * XMLSite.java
 *
 *
 * Created: Tue Jul  9 13:38:47 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class XMLSite {
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
	//get the site id
	NodeList id_node = XMLUtil.evalNodeList(base, "id");
	SiteId id = null;
	if(id_node != null && id_node.getLength() != 0) {
	    id = XMLSiteId.getSiteId((Element)id_node.item(0));
	}

	//get my_location
	NodeList my_location_node = XMLUtil.evalNodeList(base, "my_location");
	Location my_location = null;
	if(my_location_node !=  null && my_location_node.getLength() != 0) {
	    my_location = XMLLocation.getLocation((Element)my_location_node.item(0));
	}

	//get effective_time range
	NodeList effective_time_node = XMLUtil.evalNodeList(base, "effective_time");
	TimeRange effective_time = new TimeRange();
	if(effective_time_node != null && effective_time_node.getLength() != 0) {
	    effective_time = XMLTimeRange.getTimeRange((Element)effective_time_node.item(0));
	}
	
	//get the my_station
	NodeList my_station_node = XMLUtil.evalNodeList(base, "my_station");
	Station my_station = null;
	if(my_station_node != null && my_station_node.getLength() != 0) {

	    my_station = XMLStation.getStation((Element)my_station_node.item(0));
	}

	//get the comment
	String comment = XMLUtil.evalString(base, "comment");
	
	return new SiteImpl(id,
			    my_location,
			    effective_time,
			    my_station,
			    comment);

    }
    
}// XMLSite
