package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.*;
import edu.iris.Fissures.IfEvent.*;
import  edu.sc.seis.fissuresUtil.cache.*;
import edu.iris.Fissures.IfParameterMgr.*;
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

    public static void insert(Element element, String name, Element value){
        Document doc = element.getOwnerDocument();
        element.appendChild(XMLUtil.createTextElement(doc, 
                                                      "name", 
                                                      name));
	Element valueElement = doc.createElement("value");
	valueElement.appendChild((Element)value.cloneNode(true));
	element.appendChild(valueElement);
    }

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

	} else {
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

    public static Object getParameter(Element base) {
	String name = XMLUtil.evalString(base, "name");
	NodeList typeNode = XMLUtil.evalNodeList(base, "type");
	Element type = null;
	if(typeNode != null && typeNode.getLength() != 0) {
		type = (Element) typeNode.item(0);
	}
	String className = XMLUtil.evalString(type, "name");
	//Class objectClass = Class.forName(className);

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
	        NodeList eventNode = XMLUtil.evalNodeList(base, "value/event");
		Element event = null;
		if(eventNode != null && eventNode.getLength() != 0) {
			event = (Element)eventNode.item(0);
		}
		EventAttr eventAttr = XMLEvent.getEvent(event);
		Origin preferred_origin = XMLEvent.getPreferredOrigin(event);	
		return new CacheEvent(eventAttr,
				      new Origin[0],
				      preferred_origin);
	} else {

		String value = XMLUtil.evalString(base, "value");
		return new ParameterRef(name, value);	
	}
    }

}// XMLParameter
