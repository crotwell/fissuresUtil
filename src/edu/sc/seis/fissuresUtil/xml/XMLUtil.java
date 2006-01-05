package edu.sc.seis.fissuresUtil.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.apache.xpath.CachedXPathAPI;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * XMLUtil.java Created: Wed Jun 12 10:03:01 2002
 * 
 * @author <a href="mailto:">Philip Crotwell </a>
 * @version
 */
public class XMLUtil {

    // ---------------------------------------------------------
    // Begin StAX stuff. DOM stuff is at the bottom.
    // ---------------------------------------------------------
    /**
     * outputs a text element to a StAX writer
     */
    public static void writeTextElement(XMLStreamWriter writer,
                                        String elementName,
                                        String value) throws XMLStreamException {
        writer.writeStartElement(elementName);
        writer.writeCharacters(value);
        XMLUtil.writeEndElementWithNewLine(writer);
    }

    public static void writeEndElementWithNewLine(XMLStreamWriter writer)
            throws XMLStreamException {
        writer.writeEndElement();
        writer.writeCharacters("\n");
    }

    /**
     * Returns a StAXFileWriter without the root element being closed so that it
     * can be appended to. You are responsible for calling the close() method
     * when you are done appending so that the file is written and any open
     * start elements are closed.
     */
    public static StAXFileWriter openXMLFileForAppending(File file)
            throws IOException, XMLStreamException {
        FileReader fileReader = new FileReader(file);
        XMLStreamReader xmlReader = staxInputFactory.createXMLStreamReader(fileReader);
        StAXFileWriter staxWriter = new StAXFileWriter(file);
        QName rootTag = emptyName;
        int i = xmlReader.next();
        while(xmlReader.hasNext()) {
            if(i != XMLStreamConstants.END_ELEMENT
                    || !xmlReader.getName().equals(rootTag)) {
                // grab the real root element QName
                if(i == XMLStreamConstants.START_ELEMENT
                        && rootTag.equals(emptyName)) {
                    rootTag = xmlReader.getName();
                }
                translateAndWrite(xmlReader, staxWriter.getStreamWriter());
            }
            i = xmlReader.next();
        }
        xmlReader.close();
        fileReader.close();
        return staxWriter;
    }

    public static void translateAndWrite(XMLStreamReader reader,
                                         XMLStreamWriter writer)
            throws XMLStreamException {
        int type = reader.getEventType();
        switch(type){
            case XMLStreamConstants.START_DOCUMENT:
                writer.writeStartDocument(reader.getEncoding(),
                                          reader.getVersion());
                break;
            case XMLStreamConstants.END_DOCUMENT:
                writer.writeEndDocument();
                break;
            case XMLStreamConstants.START_ELEMENT:
                String prefix = reader.getPrefix() == null ? ""
                        : reader.getPrefix();
                writer.writeStartElement(prefix,
                                         reader.getLocalName(),
                                         reader.getNamespaceURI());
                for(int i = 0; i < reader.getNamespaceCount(); i++) {
                    writer.writeNamespace(reader.getNamespacePrefix(i),
                                          reader.getNamespaceURI(i));
                }
                for(int i = 0; i < reader.getAttributeCount(); i++) {
                    if(reader.getAttributePrefix(i) != null) {
                        writer.writeAttribute(reader.getAttributePrefix(i),
                                              reader.getAttributeNamespace(i),
                                              reader.getAttributeLocalName(i),
                                              reader.getAttributeValue(i));
                    } else if(reader.getAttributeNamespace(i) != null) {
                        writer.writeAttribute(reader.getAttributeNamespace(i),
                                              reader.getAttributeLocalName(i),
                                              reader.getAttributeValue(i));
                    } else {
                        writer.writeAttribute(reader.getAttributeLocalName(i),
                                              reader.getAttributeValue(i));
                    }
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
                XMLUtil.writeEndElementWithNewLine(writer);
                break;
            case XMLStreamConstants.PROCESSING_INSTRUCTION:
                writer.writeProcessingInstruction(reader.getPITarget(),
                                                  reader.getPIData());
                break;
            case XMLStreamConstants.CHARACTERS:
                if(!reader.getText().equals("\n")) {
                    writer.writeCharacters(reader.getText());
                }
                break;
            case XMLStreamConstants.COMMENT:
                writer.writeComment(reader.getText());
                break;
        }
    }

    public static String readEvent(XMLStreamReader reader) {
        StringBuffer buf = new StringBuffer();
        int type = reader.getEventType();
        switch(type){
            case XMLStreamConstants.START_DOCUMENT:
                break;
            case XMLStreamConstants.END_DOCUMENT:
                break;
            case XMLStreamConstants.START_ELEMENT:
                buf.append('<');
                if(reader.getPrefix() != null) {
                    buf.append(reader.getPrefix() + ':');
                }
                buf.append(reader.getLocalName());
                for(int i = 0; i < reader.getNamespaceCount(); i++) {
                    buf.append(" xmlns:" + reader.getNamespacePrefix(i) + "=\""
                            + reader.getNamespaceURI(i) + '\"');
                }
                for(int i = 0; i < reader.getAttributeCount(); i++) {
                    buf.append(' ');
                    if(reader.getAttributePrefix(i) != null) {
                        buf.append(reader.getAttributePrefix(i) + ':');
                    }
                    buf.append(reader.getAttributeLocalName(i) + "=\""
                            + reader.getAttributeValue(i) + '\"');
                }
                buf.append('>');
                break;
            case XMLStreamConstants.END_ELEMENT:
                buf.append("</");
                if(reader.getPrefix() != null) {
                    buf.append(reader.getPrefix() + ':');
                }
                buf.append(reader.getLocalName() + '>');
                break;
            case XMLStreamConstants.PROCESSING_INSTRUCTION:
                break;
            case XMLStreamConstants.CHARACTERS:
                buf.append(reader.getText());
                break;
            case XMLStreamConstants.COMMENT:
                break;
        }
        return buf.toString();
    }

    // TODO make this method work again. I think newlines broke it.
    public static void mergeDocs(File intoFile,
                                 File fromFile,
                                 QName compareTag,
                                 QName rootTag) throws FileNotFoundException,
            XMLStreamException, IOException {
        // create reader for original File
        FileReader fileReader1 = new FileReader(intoFile);
        XMLEventReader xmlReader1 = staxInputFactory.createXMLEventReader(fileReader1);
        // create reader for file with the new data to be merged in
        FileReader fileReader2 = new FileReader(fromFile);
        XMLEventReader xmlReader2 = staxInputFactory.createXMLEventReader(fileReader2);
        // create writer for merged document
        File tempFile = File.createTempFile("Temp_" + intoFile.getName() + "1",
                                            "xml",
                                            intoFile.getParentFile());
        FileWriter fileWriter = new FileWriter(tempFile);
        XMLEventWriter xmlWriter = staxOutputFactory.createXMLEventWriter(fileWriter);
        XMLUtil.mergeDocs(xmlReader1,
                          xmlReader2,
                          xmlWriter,
                          compareTag,
                          rootTag);
        // close the readers
        xmlReader1.close();
        xmlReader2.close();
        fileReader1.close();
        fileReader2.close();
        // flush and close the writers
        xmlWriter.flush();
        fileWriter.flush();
        xmlWriter.close();
        fileWriter.close();
        if(!tempFile.renameTo(intoFile)) {
            // If unable to rename the tempfile, delete it and try again
            if(intoFile.delete()) {
                tempFile.renameTo(intoFile);
            } else {
                throw new IOException("Unable to move temp file over old file");
            }
        }
    }

    // TODO make this work again. I think newlines broke it
    /**
     * Merges two XML Documents using StAX. The root element of
     * <code>reader1</code> will be used in the new document. Flushing and
     * closing of the readers and the writer are to be done after this method is
     * called.
     */
    public static void mergeDocs(XMLEventReader reader1,
                                 XMLEventReader reader2,
                                 XMLEventWriter writer,
                                 QName compareTag,
                                 QName rootTag) throws XMLStreamException {
        javax.xml.stream.events.XMLEvent event;
        // write prologue of from reader1
        while(reader1.hasNext()
                && !isOfName(compareTag, reader1.peek())
                && !(reader1.peek().isEndElement() && reader1.peek()
                        .asEndElement()
                        .getName()
                        .equals(rootTag))) {
            event = reader1.nextEvent();
            writer.add(event);
        }
        javax.xml.stream.events.XMLEvent curEvent = reader1.nextEvent();
        // write the pertinant information that is being merged
        if(isOfName(compareTag, curEvent)) {
            moveElements(reader1, writer, curEvent, compareTag);
        }
        // skip prologue of reader2
        while(reader2.hasNext() && !isOfName(compareTag, reader2.peek())) {
            event = reader2.nextEvent();
        }
        curEvent = reader2.nextEvent();
        // write more pertinant information
        if(isOfName(compareTag, curEvent)) {
            curEvent = moveElements(reader2, writer, curEvent, compareTag);
        }
        // try to get the rest of reader1. If there isn't anything left,
        // the xml was either malformed or there was a problem parsing
        writer.add(curEvent);
    }

    // TODO make this work again. I think newlines broke it.
    /**
     * Moves all elements of a particular type.
     * 
     * @precondition - reader must be queued up to the first occurrence of the
     *               <code>compareTag</code>
     */
    public static javax.xml.stream.events.XMLEvent moveElements(XMLEventReader reader,
                                                                XMLEventWriter writer,
                                                                javax.xml.stream.events.XMLEvent currentEvent,
                                                                QName compareTag)
            throws XMLStreamException {
        while(isOfName(compareTag, currentEvent)) {
            writer.add(currentEvent);
            currentEvent = reader.nextEvent();
            while(currentEvent.isAttribute()) {
                writer.add(currentEvent);
                currentEvent = reader.nextEvent();
            }
            while(currentEvent.isStartElement()) {
                currentEvent = moveElements(reader,
                                            writer,
                                            currentEvent,
                                            currentEvent.asStartElement()
                                                    .getName());
            }
            writer.add(currentEvent);
            currentEvent = reader.nextEvent();
        }
        return currentEvent;
    }

    public static boolean isOfName(QName tag,
                                   javax.xml.stream.events.XMLEvent event) {
        return (event.isStartElement() && event.asStartElement()
                .getName()
                .equals(tag))
                || (event.isEndElement() && event.asEndElement()
                        .getName()
                        .equals(tag));
    }

    public static XMLOutputFactory staxOutputFactory = XMLOutputFactory.newInstance();

    public static XMLInputFactory staxInputFactory = XMLInputFactory.newInstance();

    public static XMLEventFactory staxEventFactory = XMLEventFactory.newInstance();

    public static QName emptyName = new QName("",
                                              "thisisareallyuglynameforatagthathopefullynoonewilleveruse");

    // ---------------------------------------------------------
    // End of StAX stuff. Everything else is DOM.
    // ---------------------------------------------------------
    public static Element createTextElement(Document doc,
                                            String elementName,
                                            String value) {
        Element element = doc.createElement(elementName);
        Text text = doc.createTextNode(value);
        element.appendChild(text);
        return element;
    }

    /**
     * Describe <code>evalNodeList</code> method here.
     * 
     * @param context
     *            a <code>Node</code> value
     * @param path
     *            a <code>String</code> value
     * @return a <code>NodeList</code> value
     */
    public static NodeList evalNodeList(Node context, String path) {
        try {
            // xpath = new CachedXPathAPI();
            XObject xobj = xpath.eval(context, path);
            if(xobj != null && xobj.getType() == XObject.CLASS_NODESET) {
                return xobj.nodelist();
            }
        } catch(javax.xml.transform.TransformerException e) {
            // System.out.println("Couldn't get NodeList"+e);
        } // end of try-catch
        return null;
    }

    /**
     * Describe <code>evalString</code> method here.
     * 
     * @param context
     *            a <code>Node</code> value
     * @param path
     *            a <code>String</code> value
     * @return a <code>String</code> value
     */
    public static String evalString(Node context, String path) {
        try {
            return xpath.eval(context, path).str();
        } catch(javax.xml.transform.TransformerException e) {
            // System.out.println("Couldn't get String"+ e);
        } // end of try-catch
        return null;
    }

    /**
     * returns the concatenation of all text children within the node. Does not
     * recurse into subelements.
     */
    public static String getText(Element config) {
        if(config == null)
            return new String("");
        NodeList children = config.getChildNodes();
        Node node;
        String out = "";
        for(int i = 0; i < children.getLength(); i++) {
            node = children.item(i);
            if(node instanceof Text) {
                out += node.getNodeValue();
            }
        }
        return out;
    }

    /**
     * returns the element with the given name
     */
    public static Element getElement(Element config, String elementName) {
        NodeList children = config.getChildNodes();
        Node node;
        for(int counter = 0; counter < children.getLength(); counter++) {
            node = children.item(counter);
            if(node instanceof Element) {
                if(((Element)node).getTagName().equals(elementName)) {
                    return ((Element)node);
                }
            }
        }
        return null;
    }

    public static Element[] getElementArray(Element config, String elementName) {
        NodeList children = config.getChildNodes();
        Node node;
        ArrayList arrayList = new ArrayList();
        for(int counter = 0; counter < children.getLength(); counter++) {
            node = children.item(counter);
            if(node instanceof Element) {
                if(((Element)node).getTagName().equals(elementName)) {
                    arrayList.add(node);
                }
            }
        }
        Element[] elementArray = new Element[arrayList.size()];
        elementArray = (Element[])arrayList.toArray(elementArray);
        return elementArray;
    }

    /**
     * Describe <code>getAllAsStrings</code> method here.
     * 
     * @param path
     *            a <code>String</code> value
     * @return a <code>String[]</code> value
     */
    public static String[] getAllAsStrings(Element config, String path) {
        // logger.debug("The path that is passed to GetALLASStrings is "+path);
        NodeList nodes = evalNodeList(config, path);
        if(nodes == null) {
            return new String[0];
        } // end of if (nodes == null)
        String[] out = new String[nodes.getLength()];
        // logger.debug("the length of the nodes is "+nodes.getLength());
        for(int i = 0; i < out.length; i++) {
            out[i] = nodes.item(i).getNodeValue();
        } // end of for (int i=0; i++; i<out.length)
        return out;
    }

    /**
     * Describe <code>getUniqueName</code> method here.
     * 
     * @param nameList
     *            a <code>String[]</code> value
     * @param name
     *            a <code>String</code> value
     * @return a <code>String</code> value
     */
    public static String getUniqueName(String[] nameList, String name) {
        int counter = 0;
        for(int i = 0; i < nameList.length; i++) {
            if(nameList[i].indexOf(name) != -1)
                counter++;
        }
        if(counter == 0)
            return name;
        return name + "_" + (counter + 1);
    }

    /**
     * Describe <code>evalElement</code> method here.
     * 
     * @param context
     *            a <code>Node</code> value
     * @param path
     *            a <code>String</code> value
     * @return an <code>Element</code> value
     */
    public static Element evalElement(Node context, String path) {
        NodeList nList = evalNodeList(context, path);
        if(nList != null && nList.getLength() != 0) {
            return (Element)nList.item(0);
        }
        return null;
    }

    public static void gotoNextStartElement(XMLStreamReader parser, String name)
            throws XMLStreamException {
        while(parser.hasNext()) {
            int event = parser.next();
            if(event == XMLStreamConstants.START_ELEMENT) {
                if(parser.getLocalName().equals(name)) {
                    return;
                }
            }
        }
    }

    public static void getNextStartElement(XMLStreamReader parser)
            throws XMLStreamException {
        while(parser.hasNext()) {
            int event = parser.next();
            if(event == XMLStreamConstants.START_ELEMENT) {
                return;
            }
        }
    }

    private static CachedXPathAPI xpath = new CachedXPathAPI();
}// XMLUtil
