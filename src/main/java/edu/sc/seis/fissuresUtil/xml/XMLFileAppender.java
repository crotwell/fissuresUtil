package edu.sc.seis.fissuresUtil.xml;

import java.io.File;
import java.io.IOException;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * Opens an XML file for appending. Can be queued up to a particular position.
 */
public class XMLFileAppender {

    public XMLFileAppender(File file) throws IOException, XMLStreamException {
        this(file, false);
    }

    public XMLFileAppender(File file, boolean writeNewlines)
            throws IOException, XMLStreamException {
        logger.debug("creating XMLFileAppender for file " + file);
        writer = new StAXFileWriter(file);
        reader = XMLUtil.getXMLStreamReader(file);
        this.writeNewLines = writeNewlines;
        beforeNextStartElement();
        rootElementLocalName = reader.getLocalName();
    }

    public void beforeNextStartElement() throws XMLStreamException {
        beforeNextStartElement(null);
    }

    public void beforeNextStartElement(String localName)
            throws XMLStreamException {
        translateUntilFound(XMLStreamConstants.START_ELEMENT,
                            localName,
                            false,
                            false);
    }

    public void afterNextStartElement() throws XMLStreamException {
        afterNextStartElement(null);
    }

    public void afterNextStartElement(String localName)
            throws XMLStreamException {
        translateUntilFound(XMLStreamConstants.START_ELEMENT,
                            localName,
                            false,
                            true);
    }

    public void beforeNextEndElement() throws XMLStreamException {
        beforeNextStartElement(null);
    }

    public void beforeNextEndElement(String localName)
            throws XMLStreamException {
        translateUntilFound(XMLStreamConstants.END_ELEMENT,
                            localName,
                            false,
                            false);
    }

    public void afterNextEndElement() throws XMLStreamException {
        afterNextStartElement(null);
    }

    public void afterNextEndElement(String localName) throws XMLStreamException {
        translateUntilFound(XMLStreamConstants.END_ELEMENT,
                            localName,
                            false,
                            true);
    }

    public void finishTranslating() throws XMLStreamException {
        afterNextEndElement(rootElementLocalName);
    }

    protected void translateUntilFound(int xmlStreamConstant,
                                       String localName,
                                       boolean includeCurrentEvent,
                                       boolean includeDesiredEvent)
            throws XMLStreamException {
        logger.debug("translateUntilFound(" + xmlStreamConstant + ", "
                + localName + ", " + includeCurrentEvent + ", "
                + includeDesiredEvent + ")");
        int event = -1;
        while(reader.hasNext()) {
            if(!holdOnToCurrentReaderEvent()) {
                event = reader.next();
            } else {
                event = getCurrentReaderEvent();
            }
            boolean found = (event == xmlStreamConstant && (localName == null || reader.getLocalName()
                    .equals(localName)));
            if(found && !includeDesiredEvent) {
                break;
            }
            XMLUtil.translateAndWrite(reader,
                                      writer.getStreamWriter(),
                                      writeNewLines);
            if(found && includeDesiredEvent) {
                break;
            }
        }
        holdOn = !includeDesiredEvent;
    }

    private boolean holdOnToCurrentReaderEvent() {
        boolean curHoldOnValue = holdOn;
        if(curHoldOnValue) {
            holdOn = false;
        }
        return curHoldOnValue;
    }

    public int getCurrentReaderEvent() {
        return reader.getEventType();
    }

    public XMLStreamWriter getWriter() {
        return writer.getStreamWriter();
    }

    public void abort() throws XMLStreamException, IOException {
        writer.abort();
    }

    public void close() throws XMLStreamException, IOException {
        close(true);
    }

    public void close(boolean finishTranslating) throws XMLStreamException,
            IOException {
        if(finishTranslating) {
            finishTranslating();
        }
        writer.close();
    }

    private String rootElementLocalName = null;

    private boolean holdOn = false;

    private boolean writeNewLines;

    private StAXFileWriter writer;

    private XMLStreamReader reader;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(XMLFileAppender.class);
}
