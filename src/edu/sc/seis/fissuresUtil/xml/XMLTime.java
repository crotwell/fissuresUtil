package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.*;
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
    
}// XMLTime
