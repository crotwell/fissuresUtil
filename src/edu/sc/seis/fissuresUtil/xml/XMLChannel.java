package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;
import edu.iris.Fissures.IfNetwork.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.apache.log4j.*;

/**
 * XMLChannel.java
 *
 *
 * Created: Tue Jul  9 14:12:13 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class XMLChannel {
    public static void insert(Element element, Channel channel) {

	Document doc = element.getOwnerDocument();
	
	Element id = doc.createElement("id");	
	XMLChannelId.insert(id, channel.get_id());
	element.appendChild(id);

	element.appendChild(XMLUtil.createTextElement(doc,
						      "name",
						      channel.name));
	
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
	
	//get the channel id
	NodeList id_node = XMLUtil.evalNodeList(base, "id");
	ChannelId id = null;
	if(id_node != null && id_node.getLength() != 0) {
	    id = XMLChannelId.getChannelId((Element)id_node.item(0));
	}
	
	//get the name
	String name = XMLUtil.evalString(base, "name");
	
	//get the an_orientation
	NodeList an_orientation_node = XMLUtil.evalNodeList(base, "an_orientation");
	Orientation an_orientation = new Orientation();
	if(an_orientation_node != null && an_orientation_node.getLength() != 0) {
	    an_orientation = XMLOrientation.getOrientation((Element)an_orientation_node.item(0));
	}
	
	//get the sampling_info
	NodeList sampling_info_node = XMLUtil.evalNodeList(base, "sampling_info");
	Sampling sampling_info = null;
	if(sampling_info_node != null && sampling_info_node.getLength() != 0) {
	    sampling_info = XMLSampling.getSampling((Element)sampling_info_node.item(0));
	}
	
	//get effective_time range
	NodeList effective_time_node = XMLUtil.evalNodeList(base, "effective_time");
	TimeRange effective_time = new TimeRange();
	if(effective_time_node != null && effective_time_node.getLength() != 0) {
	    effective_time = XMLTimeRange.getTimeRange((Element)effective_time_node.item(0));
	}

	//get the my_site
	NodeList my_site_node = XMLUtil.evalNodeList(base, "my_site");
	Site my_site = null;
	if(my_site_node != null && my_site_node.getLength() != 0) {
	    my_site = XMLSite.getSite((Element)my_site_node.item(0));
	}
	
	return new ChannelImpl(id,
			       name,
			       an_orientation,
			       sampling_info,
			       effective_time,
			       my_site);
	
    }
}// XMLChannel
