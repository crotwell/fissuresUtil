package edu.sc.seis.fissuresUtil.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.SiteId;

/**
 * XMLSiteId.java
 *
 *
 * Created: Tue Jul  9 13:33:00 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class XMLSiteId {

    /**
     * StAX insert
     */
    public static void insert(XMLStreamWriter writer, SiteId siteId)
        throws XMLStreamException{

        writer.writeStartElement("network_id");
        XMLNetworkId.insert(writer, siteId.network_id);
        XMLUtil.writeEndElementWithNewLine(writer);

        XMLUtil.writeTextElement(writer, "station_code", siteId.station_code);
        XMLUtil.writeTextElement(writer, "site_code", siteId.site_code);

        writer.writeStartElement("begin_time");
        XMLTime.insert(writer, siteId.begin_time);
        XMLUtil.writeEndElementWithNewLine(writer);
    }

    /**
     * DOM insert
     */
    public static void insert(Element element, SiteId siteId){
        Document doc = element.getOwnerDocument();
        Element network_id = doc.createElement("network_id");
        XMLNetworkId.insert(network_id, siteId.network_id);
        element.appendChild(network_id);

        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "station_code",
                                                      siteId.station_code));
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "site_code",
                                                      siteId.site_code));
        Element begin_time = doc.createElement("begin_time");
        XMLTime.insert(begin_time, siteId.begin_time);
        element.appendChild(begin_time);

    }

    public static SiteId getSiteId(Element base) {

        //get the network_id
        NetworkId network_id = null;
        Element network_id_node = XMLUtil.getElement(base, "network_id");
        if(network_id_node != null) {

            network_id = XMLNetworkId.getNetworkId(network_id_node);
        }
        //if(network_id == null) System.out.println("Network ID is null");
        //else System.out.println("The Network Id is NOT NULL");

        //get the station_code
        String station_code = XMLUtil.getText(XMLUtil.getElement(base, "station_code"));

        //get the site_code
        String site_code = XMLUtil.getText(XMLUtil.getElement(base, "site_code"));

        //get the begin_time
        edu.iris.Fissures.Time begin_time = new edu.iris.Fissures.Time();
        Element begin_time_node = XMLUtil.getElement(base, "begin_time");
        if(begin_time_node != null) {
            begin_time = XMLTime.getFissuresTime(begin_time_node);
        }
        return new SiteId(network_id,
                          station_code,
                          site_code,
                          begin_time);

    }

    //    public static SiteId getSiteId(Element base) {

    //  //get the network_id
    //  NetworkId network_id = null;
    //  NodeList network_id_node = XMLUtil.evalNodeList(base, "network_id");
    //  if(network_id_node != null && network_id_node.getLength() != 0) {

    //      network_id = XMLNetworkId.getNetworkId((Element)network_id_node.item(0));
    //  }
    //  //if(network_id == null) System.out.println("Network ID is null");
    //  //else System.out.println("The Network Id is NOT NULL");

    //  //get the station_code
    //  String station_code = XMLUtil.evalString(base, "station_code");

    //  //get the site_code
    //  String site_code = XMLUtil.evalString(base, "site_code");

    //  //get the begin_time
    //  edu.iris.Fissures.Time begin_time = new edu.iris.Fissures.Time();
    //  NodeList begin_time_node = XMLUtil.evalNodeList(base, "begin_time");
    //  if(begin_time_node != null && begin_time_node.getLength() != 0) {
    //      begin_time = XMLTime.getFissuresTime((Element)begin_time_node.item(0));
    //  }
    //  return new SiteId(network_id,
    //            station_code,
    //            site_code,
    //            begin_time);

    //     }

}// XMLSiteId
