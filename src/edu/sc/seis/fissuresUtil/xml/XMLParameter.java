package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.IfNetwork.*;
import  edu.sc.seis.fissuresUtil.cache.*;
import edu.iris.Fissures.IfParameterMgr.*;
import java.util.*;
import java.io.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.apache.log4j.*;

/**
 * XMLParameter.java
 *
 *
 * Created: Thu Jun 13 11:29:36 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class XMLParameter {

    /**
     * Describe <code>insert</code> method here.
     *
     * @param element an <code>Element</code> value
     * @param name a <code>String</code> value
     * @param value an <code>Element</code> value
     */
    public static void insert(Element element, String name, Element value){
        Document doc = element.getOwnerDocument();
        element.appendChild(XMLUtil.createTextElement(doc, 
                                                      "name", 
                                                      name));
	Element valueElement = doc.createElement("value");
	valueElement.appendChild((Element)value.cloneNode(true));
	element.appendChild(valueElement);
    }

    /**
     * Describe <code>insert</code> method here.
     *
     * @param element an <code>Element</code> value
     * @param name a <code>String</code> value
     * @param value a <code>String</code> value
     */
    public static void insert(Element element, String name, String value){
        Document doc = element.getOwnerDocument();
        element.appendChild(XMLUtil.createTextElement(doc, 
                                                      "name", 
                                                      name));
	Element typeName = doc.createElement("type");
	typeName.appendChild(XMLUtil.createTextElement(doc, 
						       "definition",
						       "http://www.w3c.org/1999/xschema/"));
			     
	typeName.appendChild(XMLUtil.createTextElement(doc, 
						       "name",
						       "xsd:string"));
	
	element.appendChild(XMLUtil.createTextElement(doc, 
                                                      "value", 
                                                      value));
   }

    /**
     * Describe <code>insertParameterRef</code> method here.
     *
     * @param paramRef an <code>Element</code> value
     * @param name a <code>String</code> value
     * @param href a <code>String</code> value
     * @param value an <code>Object</code> value
     */
    public static void insertParameterRef(Element paramRef, String name, String href, Object value) {
	//	write(href, value);
	//Element paramRef = doc.createElement("parameterRef");
	paramRef.setAttribute("name", name);
	paramRef.setAttributeNS(xlinkNS,"xlink:type", "type/xml");
	if(href.startsWith("file:")) {
	    href = href.substring(href.indexOf("file:")+5);
	}
	href = href.replace(' ', '_');
	paramRef.setAttributeNS(xlinkNS, "xlink:href", href);
	if(!(value instanceof String)) {
		paramRef.setAttribute("objectType", getObjectType(value));
	}
	
	/*	try {
	    DocumentBuilderFactory factory = DocumentBuilderFactory.getInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
		
	} catch(Exception e) {

	    e.printStackTrace();
	    }*/
    }

    /**
     * Describe <code>write</code> method here.
     *
     * @param out an <code>OutputStream</code> value
     * @param value an <code>Object</code> value
     */
    public static void write(OutputStream out, Object value) {
	System.out.println("IN THE WRITE METHOD OF THE XML PARAMETER");
	try {
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    Document doc = builder.newDocument();
	    javax.xml.transform.TransformerFactory tfactory = 
		javax.xml.transform.TransformerFactory.newInstance(); 
    
	    // This creates a transformer that does a simple identity transform, 
	    // and thus can be used for all intents and purposes as a serializer.
	    javax.xml.transform.Transformer serializer = tfactory.newTransformer();
	    java.util.Properties oprops = new java.util.Properties();
	    oprops.put("method", "xml");
	    oprops.put("indent", "yes");
	    oprops.put("xalan:indent-amount", "4");
	    serializer.setOutputProperties(oprops);
	    Element element = doc.createElement("parameter");
	    insert(element, "notused", value);
	    
 	    serializer.transform(new javax.xml.transform.dom.DOMSource(element),
				 new javax.xml.transform.stream.StreamResult(out));
	     
	     
	} catch(Exception e) {
	    e.printStackTrace();
	}

    }
    
    /**
     * Describe <code>insert</code> method here.
     *
     * @param element an <code>Element</code> value
     * @param name a <code>String</code> value
     * @param value an <code>Object</code> value
     */
    public static void insert(Element element, String name, Object value) {
	Document doc = element.getOwnerDocument();
	element.appendChild(XMLUtil.createTextElement(doc, 
 						      "name",
 						      name));
	Element typeName = doc.createElement("type");
	
	Element valueElement = doc.createElement("value");

	if(value instanceof CacheEvent){
	    typeName.appendChild(XMLUtil.createTextElement(doc,
							   "definition",
							   "http://www.seis.sc.edu/xschema/fissures.xsd"));
	    typeName.appendChild(XMLUtil.createTextElement(doc,
							   "name",
							   value.getClass().getName()));
	    element.appendChild(typeName);
	    Element event = doc.createElement("event");
	    //System.out.println("Writing the event to xml");
	    XMLEvent.insert(event, (CacheEvent)value);
	    valueElement.appendChild(event);
	    element.appendChild(valueElement);

	} else if(value instanceof Channel) {
	    typeName.appendChild(XMLUtil.createTextElement(doc,
							   "definition",
							   "http://www.seis.sc.edu/xschema/fissures.xsd"));
	    typeName.appendChild(XMLUtil.createTextElement(doc,
							   "name",
							   value.getClass().getName()));
	    element.appendChild(typeName);
	    Element channel = doc.createElement("channel");
	    XMLChannel.insert(channel, (Channel)value);
	    valueElement.appendChild(channel);
	    element.appendChild(valueElement);
	} else if(value instanceof ParameterRef) {

	    typeName.appendChild(XMLUtil.createTextElement(doc,
							   "definition",
							   "http://www.w3.org/2002/XMLSchema/"));
	    typeName.appendChild(XMLUtil.createTextElement(doc,
							   "name",
							   "xsd:string"));
	    element.appendChild(typeName);
	    element.appendChild(XMLUtil.createTextElement(doc, "value",
							 ((ParameterRef)value).creator));
								
        }
	else {
	    typeName.appendChild(XMLUtil.createTextElement(doc,
							   "definition",
							   "http://www.w3.org/2002/XMLSchema/"));
	    typeName.appendChild(XMLUtil.createTextElement(doc,
							   "name",
							   "xsd:string"));
	    element.appendChild(typeName);
	    element.appendChild(XMLUtil.createTextElement(doc, "value",
							  (String)value));
	}

    }

    /**
     * Describe <code>getParameter</code> method here.
     *
     * @param base an <code>Element</code> value
     * @return an <code>Object</code> value
     */
    public static Object getParameter(Element base) {
	String name = XMLUtil.getText(XMLUtil.getElement(base, "name"));
	Element typeNode = XMLUtil.getElement(base, "type");
	Element type = null;
	if(typeNode != null) {
		type = typeNode;
	}
	String className = XMLUtil.getText(XMLUtil.getElement(type, "name"));
	System.out.println("The className is *&*&*&*&*&*&*&*&*&*&*&*&*&*&*&*&*&*&*&*&*&*"+className);
	
	//Class objectClass = Class.forName(className);
	//System.out.println("The class name is "+className);
	if(className.equals("edu.sc.seis.fissuresUtil.cache.CacheEvent") ) {
	/*	Class[] constructorArgTypes = new Class[3];
		NodeList eventNode = XMLUtil.evalNodeList(base, "value/event");
		Element event = (Element)eventNode.item(0);
		constuctorArgTypes[0] = XMLEvent.getEvent(event).class;
		constructorArgTypes[1] = new Origin[0].class;
		constructorArgTypes[2] = XMLEvent.getPreferredOrigin(event).class;
		Constructor constructor = objectClass.getConstructor(constructorArgTypes);
		Object[] constructorArgs = new Object[3];
		constructorArgs[0] = XMLEvent.getEvent(event);
		constructorArgs[1] = new Origin[0];
		constructorArgs[2] = XMLEvent.getPreferredOrigin(event);
		Object obj = Constructor.newInstance(constructorArgs);
		return obj;*/
	        Element eventNode = XMLUtil.getElement(XMLUtil.getElement(base, "value"), "event");
		Element event = null;
		if(eventNode != null) {
			event = eventNode;
		}
		EventAttr eventAttr = XMLEvent.getEvent(event);
		Origin preferred_origin = XMLEvent.getPreferredOrigin(event);	
		System.out.println("RETURNING CACHE EVENT");
		return new CacheEvent(eventAttr,
				      new Origin[0],
				      preferred_origin);

	} else if(className.equalsIgnoreCase("edu.iris.fissures.network.ChannelImpl")) {
	    //System.out.println(" HERE WE GOT THE CHANNEL");
	    Element channelNode = XMLUtil.getElement(XMLUtil.getElement(base, "value"), "channel");
	    if(channelNode != null) {
		Date channelStartTime = Calendar.getInstance().getTime();
		Channel channel = XMLChannel.getChannel(channelNode);
		Date channelEndTime = Calendar.getInstance().getTime();
		System.out.println("TIME FOR GETTING CHANNEL IS "+ (channelEndTime.getTime() - channelStartTime.getTime()));
		System.out.println("RETURNING CHANNEL");
		return channel;
	    }
	    return null;
	}
	else {

		String value = XMLUtil.getText(XMLUtil.getElement(base, "value"));
		System.out.println("RETURNING PARAMETER REF");
		return new ParameterRef(name, value);	
	}
    }

    /**
     * Describe <code>getObjectType</code> method here.
     *
     * @param object an <code>Object</code> value
     * @return a <code>String</code> value
     */
    public static String getObjectType(Object object) {
	if(object instanceof Channel) return "edu.sc.seis.fissuresUtil.xml.XMLChannel";
	else if(object instanceof CacheEvent) return "edu.sc.seis.fissuresUtil.xml.XMLEvent";
	else return "String";

    }

  

//  public static Object getParameter(Element base) {
// 	String name = XMLUtil.evalString(base, "name");
// 	NodeList typeNode = XMLUtil.evalNodeList(base, "type");
// 	Element type = null;
// 	if(typeNode != null && typeNode.getLength() != 0) {
// 		type = (Element) typeNode.item(0);
// 	}
// 	String className = XMLUtil.evalString(type, "name");
// 	//Class objectClass = Class.forName(className);
// 	//System.out.println("The class name is "+className);
// 	if(className.equals("edu.sc.seis.fissuresUtil.cache.CacheEvent") ) {
// 	/*	Class[] constructorArgTypes = new Class[3];
// 		NodeList eventNode = XMLUtil.evalNodeList(base, "value/event");
// 		Element event = (Element)eventNode.item(0);
// 		constuctorArgTypes[0] = XMLEvent.getEvent(event).class;
// 		constructorArgTypes[1] = new Origin[0].class;
// 		constructorArgTypes[2] = XMLEvent.getPreferredOrigin(event).class;
// 		Constructor constructor = objectClass.getConstructor(constructorArgTypes);
// 		Object[] constructorArgs = new Object[3];
// 		constructorArgs[0] = XMLEvent.getEvent(event);
// 		constructorArgs[1] = new Origin[0];
// 		constructorArgs[2] = XMLEvent.getPreferredOrigin(event);
// 		Object obj = Constructor.newInstance(constructorArgs);
// 		return obj;*/
// 	        NodeList eventNode = XMLUtil.evalNodeList(base, "value/event");
// 		Element event = null;
// 		if(eventNode != null && eventNode.getLength() != 0) {
// 			event = (Element)eventNode.item(0);
// 		}
// 		EventAttr eventAttr = XMLEvent.getEvent(event);
// 		Origin preferred_origin = XMLEvent.getPreferredOrigin(event);	
// 		return new CacheEvent(eventAttr,
// 				      new Origin[0],
// 				      preferred_origin);

// 	} else if(className.equalsIgnoreCase("edu.iris.fissures.network.ChannelImpl")) {
// 	    //System.out.println(" HERE WE GOT THE CHANNEL");
// 	    NodeList channelNode = XMLUtil.evalNodeList(base, "value/channel");
// 	    if(channelNode != null && channelNode.getLength() != 0) {
// 		Date channelStartTime = Calendar.getInstance().getTime();
// 		Channel channel = XMLChannel.getChannel((Element)channelNode.item(0));
// 		Date channelEndTime = Calendar.getInstance().getTime();
// 		System.out.println("TIME FOR GETTING CHANNEL IS "+ (channelEndTime.getTime() - channelStartTime.getTime()));
// 		return channel;
// 	    }
// 	    return null;
// 	}
// 	else {

// 		String value = XMLUtil.evalString(base, "value");
// 		return new ParameterRef(name, value);	
// 	}
//     }

 private static final String xlinkNS = "http://www.w3.org/1999/xlink";

}// XMLParameter
