/**
 * XMLRequestFilter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.Time;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLRequestFilter {
    public static void insert(Element element, RequestFilter filter) {

        Document doc = element.getOwnerDocument();

        Element id = doc.createElement("channelId");
        XMLChannelId.insert(id, filter.channel_id);
        element.appendChild(id);

        Element start_time = doc.createElement("start_time");
        XMLTime.insert(start_time, filter.start_time);
        element.appendChild(start_time);
        Element end_time = doc.createElement("end_time");
        XMLTime.insert(end_time, filter.end_time);
        element.appendChild(end_time);
    }

    public static RequestFilter getRequestFilter(Element base) {

        //get the channel id
        Element id_node = XMLUtil.getElement(base, "channelId");
        ChannelId id = null;
        if(id_node != null) {
            id = XMLChannelId.getChannelId(id_node);
        }


        //get effective_time range
        Element start_time_node = XMLUtil.getElement(base, "start_time");
        Time start_time = new Time();
        if(start_time_node != null) {
            start_time = XMLTime.getFissuresTime(start_time_node);
        }

        //get effective_time range
        Element end_time_node = XMLUtil.getElement(base, "end_time");
        Time end_time = new Time();
        if(end_time_node != null) {
            end_time = XMLTime.getFissuresTime(end_time_node);
        }

        return new RequestFilter(id,
                                 start_time,
                                 end_time);

    }
}

