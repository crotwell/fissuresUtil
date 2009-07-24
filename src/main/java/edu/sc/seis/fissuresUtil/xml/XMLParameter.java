package edu.sc.seis.fissuresUtil.xml;

import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfParameterMgr.ParameterRef;
import edu.iris.Fissures.event.OriginImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;

/**
 * XMLParameter.java Created: Thu Jun 13 11:29:36 2002
 * 
 * @author <a href="mailto:">Philip Crotwell </a>
 * @version
 */
public class XMLParameter {

    /**
     * StAX insert
     */
    public static void insert(XMLStreamWriter writer, String name, String value)
            throws XMLStreamException {
        insert(writer,
               name,
               "http://www.w3c.org/1999/xschema/",
               "xsd:string",
               value);
    }

    /**
     * StAX insert
     */
    public static void insert(XMLStreamWriter writer,
                              String name,
                              String typeDef,
                              String typeName,
                              String value) throws XMLStreamException {
        XMLUtil.writeTextElement(writer, "name", name);
        writer.writeStartElement("type");
        XMLUtil.writeTextElement(writer, "definition", typeDef);
        XMLUtil.writeTextElement(writer, "name", typeName);
        XMLUtil.writeEndElementWithNewLine(writer);
        XMLUtil.writeTextElement(writer, "value", value);
    }

    /**
     * StAX insert
     */
    public static void insert(XMLStreamWriter writer, String name, Object value)
            throws XMLStreamException {
        XMLUtil.writeTextElement(writer, "name", name);
        writer.writeStartElement("type");
        if(value instanceof CacheEvent || value instanceof ChannelImpl) {
            XMLUtil.writeTextElement(writer,
                                     "definition",
                                     "http://www.seis.sc.edu/xschema/fissures.xsd");
            if(value instanceof CacheEvent ) {
                XMLUtil.writeTextElement(writer, "name", CacheEvent.class.getName());
            } else if (value instanceof ChannelImpl) {
                XMLUtil.writeTextElement(writer, "name", ChannelImpl.class.getName());
            }
        } else {
            XMLUtil.writeTextElement(writer,
                                     "definition",
                                     "http://www.w3.org/2002/XMLSchema/");
            XMLUtil.writeTextElement(writer, "name", "xsd:string");
        }
        XMLUtil.writeEndElementWithNewLine(writer);
        if(value instanceof CacheEvent || value instanceof ChannelImpl) {
            writer.writeStartElement("value");
            if(value instanceof CacheEvent) {
                writer.writeStartElement("event");
                XMLEvent.insert(writer, (CacheEvent)value);
            } else {
                writer.writeStartElement("channel");
                XMLChannel.insert(writer, (ChannelImpl)value);
            }
            XMLUtil.writeEndElementWithNewLine(writer);
            XMLUtil.writeEndElementWithNewLine(writer);
        } else {
            String valString;
            if(value instanceof ParameterRef) {
                valString = ((ParameterRef)value).creator;
            } else {
                valString = (String)value;
            }
            XMLUtil.writeTextElement(writer, "value", valString);
        }
    }

    public static void insertParameterRef(XMLStreamWriter writer,
                                          String name,
                                          String href,
                                          Object value)
            throws XMLStreamException {
        writer.writeAttribute("name", name);
        writer.writeAttribute("xlink", xlinkNS, "type", "type/xml");
        if(href.startsWith("file:")) {
            href = href.substring(href.indexOf("file:") + 5);
        }
        href = href.replace(' ', '_');
        writer.writeAttribute("xlink", xlinkNS, "href", href);
        if(!(value instanceof String)) {
            writer.writeAttribute("objectType", getObjectType(value));
        }
    }

    /**
     * Inserts a parameter of UNKNOWN type into the dataset element.
     * 
     * @param element
     *            an <code>Element</code> value
     * @param name
     *            a <code>String</code> value
     * @param value
     *            an <code>Element</code> value
     */
    /*
     * public static void insert(Element element, String name, Element value){
     * insert(element, name, "UNKNOWN", "UNKNOWN", value); }
     */
    public static void insert(Element element,
                              String name,
                              String typeDef,
                              String typeName,
                              String value) {
        Document doc = element.getOwnerDocument();
        element.appendChild(XMLUtil.createTextElement(doc, "name", name));
        Element type = doc.createElement("type");
        element.appendChild(type);
        Element typeDefElement = XMLUtil.createTextElement(doc,
                                                           "definition",
                                                           typeDef);
        type.appendChild(typeDefElement);
        Element typeNameElement = XMLUtil.createTextElement(doc,
                                                            "name",
                                                            typeName);
        type.appendChild(typeNameElement);
        Element valueElement = XMLUtil.createTextElement(doc, "value", value);
        element.appendChild(valueElement);
    }

    /*
     * public static void insert(Element element, String name, String typeDef,
     * String typeName, Element value){ insert(element, name, typeDef, typeName,
     * (Node)value); }
     */
    /**
     * DOM insert
     */
    public static void insert(Element element, String name, String value) {
        Document doc = element.getOwnerDocument();
        element.appendChild(XMLUtil.createTextElement(doc, "name", name));
        Element typeName = doc.createElement("type");
        typeName.appendChild(XMLUtil.createTextElement(doc,
                                                       "definition",
                                                       "http://www.w3c.org/1999/xschema/"));
        typeName.appendChild(XMLUtil.createTextElement(doc,
                                                       "name",
                                                       "xsd:string"));
        element.appendChild(XMLUtil.createTextElement(doc, "value", value));
    }

    /**
     * Describe <code>insertParameterRef</code> method here.
     * 
     * @param paramRef
     *            an <code>Element</code> value
     * @param name
     *            a <code>String</code> value
     * @param href
     *            a <code>String</code> value
     * @param value
     *            an <code>Object</code> value
     */
    public static void insertParameterRef(Element paramRef,
                                          String name,
                                          String href,
                                          Object value) {
        // write(href, value);
        // Element paramRef = doc.createElement("parameterRef");
        paramRef.setAttribute("name", name);
        paramRef.setAttributeNS(xlinkNS, "xlink:type", "type/xml");
        if(href.startsWith("file:")) {
            href = href.substring(href.indexOf("file:") + 5);
        }
        href = href.replace(' ', '_');
        paramRef.setAttributeNS(xlinkNS, "xlink:href", href);
        if(!(value instanceof String)) {
            paramRef.setAttribute("objectType", getObjectType(value));
        }
        /*
         * try { DocumentBuilderFactory factory =
         * DocumentBuilderFactory.getInstance(); DocumentBuilder builder =
         * factory.newDocumentBuilder(); } catch(Exception e) {
         * e.printStackTrace(); }
         */
    }

    public static void write(OutputStream out, Object value) {
        try {
            DocumentBuilder builder = XMLDataSet.getDocumentBuilder();
            Document doc = builder.newDocument();
            javax.xml.transform.TransformerFactory tfactory = javax.xml.transform.TransformerFactory.newInstance();
            // This creates a transformer that does a simple identity transform,
            // and thus can be used for all intents and purposes as a
            // serializer.
            javax.xml.transform.Transformer serializer = tfactory.newTransformer();
            java.util.Properties oprops = new java.util.Properties();
            oprops.put("method", "xml");
            oprops.put("indent", "yes");
            // oprops.put("xalan:indent-amount", "4");
            serializer.setOutputProperties(oprops);
            Element element = doc.createElement("parameter");
            insert(element, "notused", value);
            serializer.transform(new javax.xml.transform.dom.DOMSource(element),
                                 new javax.xml.transform.stream.StreamResult(out));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void insert(Element element, String name, Object value) {
        Document doc = element.getOwnerDocument();
        element.appendChild(XMLUtil.createTextElement(doc, "name", name));
        Element typeName = doc.createElement("type");
        Element valueElement = doc.createElement("value");
        if(value instanceof CacheEvent) {
            typeName.appendChild(XMLUtil.createTextElement(doc,
                                                           "definition",
                                                           "http://www.seis.sc.edu/xschema/fissures.xsd"));
            typeName.appendChild(XMLUtil.createTextElement(doc,
                                                           "name",
                                                           CacheEvent.class.getName()));
            element.appendChild(typeName);
            Element event = doc.createElement("event");
            XMLEvent.insert(event, (CacheEvent)value);
            valueElement.appendChild(event);
            element.appendChild(valueElement);
        } else if(value instanceof Channel) {
            typeName.appendChild(XMLUtil.createTextElement(doc,
                                                           "definition",
                                                           "http://www.seis.sc.edu/xschema/fissures.xsd"));
            typeName.appendChild(XMLUtil.createTextElement(doc,
                                                           "name",
                                                           Channel.class.getName()));
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
            element.appendChild(XMLUtil.createTextElement(doc,
                                                          "value",
                                                          ((ParameterRef)value).creator));
        } else {
            typeName.appendChild(XMLUtil.createTextElement(doc,
                                                           "definition",
                                                           "http://www.w3.org/2002/XMLSchema/"));
            typeName.appendChild(XMLUtil.createTextElement(doc,
                                                           "name",
                                                           "xsd:string"));
            element.appendChild(typeName);
            element.appendChild(XMLUtil.createTextElement(doc,
                                                          "value",
                                                          (String)value));
        }
    }

    /**
     * Describe <code>getParameter</code> method here.
     * 
     * @param base
     *            an <code>Element</code> value
     * @return an <code>Object</code> value
     */
    public static Object getParameter(Element base) {
        String name = XMLUtil.getText(XMLUtil.getElement(base, "name"));
        Element type = XMLUtil.getElement(base, "type");
        String className = XMLUtil.getText(XMLUtil.getElement(type, "name"));
        try {
            Class c = Class.forName(className);
            while(!c.equals(Object.class)) {
                if(isRecognizedClass(c)) {
                    return makeForClass(c, base);
                }
                Class[] interfaces = c.getInterfaces();
                for(int i = 0; i < interfaces.length; i++) {
                    if(isRecognizedClass(interfaces[i])) {
                        return makeForClass(interfaces[i], base);
                    }
                }
                c = c.getSuperclass();
            }
        } catch(ClassNotFoundException e) {
            logger.debug("unable to find class of type " + className
                    + " loading parameter as a string",e);
        }
        String value = XMLUtil.getText(XMLUtil.getElement(base, "value"));
        return new ParameterRef(name, value);
    }

    public static Object getParameter(XMLStreamReader parser, String name)
            throws XMLStreamException {
        XMLUtil.gotoNextStartElement(parser, "name");
        String className = parser.getElementText();
        try {
            Class c = Class.forName(className);
            while(!c.equals(Object.class)) {
                if(isRecognizedClass(c)) {
                    return makeForClass(c, parser);
                }
                Class[] interfaces = c.getInterfaces();
                for(int i = 0; i < interfaces.length; i++) {
                    if(isRecognizedClass(interfaces[i])) {
                        return makeForClass(interfaces[i], parser);
                    }
                }
                c = c.getSuperclass();
            }
        } catch(ClassNotFoundException e) {
            logger.debug("unable to find class of type " + className
                    + " loading parameter as a string", e);
        }
        XMLUtil.gotoNextStartElement(parser, "value");
        String value = parser.getElementText();
        return new ParameterRef(name, value);
    }

    public static boolean isRecognizedClass(Class c) {
        return c.equals(CacheEvent.class) || c.equals(Channel.class);
    }

    public static Object makeForClass(Class c, Element base) {
        if(c.equals(CacheEvent.class)) {
            return makeEvent(base);
        } else if(c.equals(Channel.class)) {
            return makeChannel(base);
        }
        return null;
    }

    public static Object makeForClass(Class c, XMLStreamReader parser)
            throws XMLStreamException {
        if(c.equals(CacheEvent.class)) {
            return makeEvent(parser);
        } else if(c.equals(Channel.class)) {
            return makeChannel(parser);
        }
        return null;
    }

    private static Channel makeChannel(Element base) {
        Element channelNode = XMLUtil.getElement(XMLUtil.getElement(base,
                                                                    "value"),
                                                 "channel");
        Channel channel = XMLChannel.getChannel(channelNode);
        return channel;
    }

    private static Channel makeChannel(XMLStreamReader parser)
            throws XMLStreamException {
        XMLUtil.gotoNextStartElement(parser, "channel");
        Channel channel = XMLChannel.getChannel(parser);
        return channel;
    }

    private static CacheEvent makeEvent(Element base) {
        Element event = XMLUtil.getElement(XMLUtil.getElement(base, "value"),
                                           "event");
        EventAttr eventAttr = XMLEvent.getEvent(event);
        OriginImpl preferred_origin = XMLEvent.getPreferredOrigin(event);
        return new CacheEvent(eventAttr, new OriginImpl[0], preferred_origin);
    }

    private static CacheEvent makeEvent(XMLStreamReader parser)
            throws XMLStreamException {
        XMLUtil.gotoNextStartElement(parser, "event");
        EventAttr eventAttr = XMLEvent.getEvent(parser);
        OriginImpl preferred_origin = XMLEvent.getPreferredOrigin(parser);
        return new CacheEvent(eventAttr, new OriginImpl[0], preferred_origin);
    }

    public static String getObjectType(Object object) {
        if(object instanceof Channel)
            return "edu.sc.seis.fissuresUtil.xml.XMLChannel";
        else if(object instanceof CacheEvent)
            return "edu.sc.seis.fissuresUtil.xml.XMLEvent";
        else
            return "String";
    }

    private static final String xlinkNS = "http://www.w3.org/1999/xlink";

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XMLParameter.class);
}// XMLParameter
