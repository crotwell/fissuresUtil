package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.*;
import edu.iris.Fissures.IfNetwork.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.apache.log4j.*;

/**
 * XMLChannelId.java
 *
 *
 * Created: Mon Jul  1 14:52:45 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class XMLChannelId {
 
    public static void insert(Element element, ChannelId channelId){
	 Document doc = element.getOwnerDocument();
	 Element network_id = doc.createElement("nework_id");
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
}// XMLChannelId
