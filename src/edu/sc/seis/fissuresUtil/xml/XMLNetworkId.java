package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.*;
import edu.iris.Fissures.IfNetwork.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.apache.log4j.*;

/**
 * XMLNetworkId.java
 *
 *
 * Created: Mon Jul  1 14:52:45 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class XMLNetworkId {
 
    public static void insert(Element element, NetworkId networkId){
	 Document doc = element.getOwnerDocument();
	 element.appendChild(XMLUtil.createTextElement(doc, 
						       "network_code",
						       networkId.network_code));
	 Element begin_time = doc.createElement("begin_time");
	 XMLTime.insert(begin_time, networkId.begin_time);
	 element.appendChild(begin_time);
	 

     }
}// XMLNetworkId
