package edu.sc.seis.fissuresUtil.xml;

import java.util.Calendar;
import java.util.Date;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.Time;

/**
 * XMLTime.java
 *
 *
 * Created: Wed Jun 12 12:17:15 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class XMLTime {

    public static void insert(XMLStreamWriter writer, Time time) throws XMLStreamException {
        XMLUtil.writeTextElement(writer, "date_time", time.date_time);
        XMLUtil.writeTextElement(writer,
                                 "leap_seconds_version",
                                 ""+time.leap_seconds_version);
    }

    public static void insert(Element element, Time time){
        Document doc = element.getOwnerDocument();
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "date_time",
                                                      time.date_time));
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "leap_seconds_version",
                                                      ""+time.leap_seconds_version));
    }

    public static edu.iris.Fissures.Time  getFissuresTime(Element element) {
        Date startTime = Calendar.getInstance().getTime();
        NodeList  nodeList = element.getChildNodes();
        String date_time = "";
        int leap_seconds_version = 0;
        for(int counter = 0; counter < nodeList.getLength(); counter++) {
            if(nodeList.item(counter) instanceof Element) {

                Element elem = (Element)nodeList.item(counter);
                if(elem.getNodeName().equals("date_time")) {
                    date_time = XMLUtil.getText(elem);
                } else if(elem.getNodeName().equals("leap_seconds_version")) {

                    leap_seconds_version = Integer.parseInt(XMLUtil.getText(elem));
                }
            }
            //String date_time = XMLUtil.evalString(element, "date_time");
            //int leap_seconds_version = Integer.parseInt(XMLUtil.evalString(element,"leap_seconds_version"));
        }
        Date endTime = Calendar.getInstance().getTime();
        return new edu.iris.Fissures.Time(date_time, leap_seconds_version);
    }



}// XMLTime
