package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.*;
import java.util.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.apache.log4j.*;

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
		    System.out.println(" THE VALUE is "+XMLUtil.getText(elem));
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
