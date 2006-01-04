package edu.sc.seis.fissuresUtil.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.Sampling;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;

/**
 * XMLSampling.java
 * 
 * 
 * Created: Mon Jul 1 14:33:15 2002
 * 
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */
public class XMLSampling {

    /**
     * StAX insert method
     */
    public static void insert(XMLStreamWriter writer, Sampling sampling)
            throws XMLStreamException {
        XMLUtil.writeTextElement(writer,
                                 "numPoints",
                                 Integer.toString(sampling.numPoints));
        writer.writeStartElement("interval");
        XMLQuantity.insert(writer, sampling.interval);
        XMLUtil.writeEndElementWithNewLine(writer);
    }

    /**
     * DOM insert method
     */
    public static void insert(Element element, Sampling sampling) {
        Document doc = element.getOwnerDocument();
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "numPoints",
                                                      Integer.toString(sampling.numPoints)));
        Element interval = doc.createElement("interval");
        XMLQuantity.insert(interval, sampling.interval);
        element.appendChild(interval);
    }

    public static Sampling getSampling(Element base) {
        int numPoints = Integer.parseInt(XMLUtil.getText(XMLUtil.getElement(base,
                                                                            "numPoints")));
        Element interval_node = XMLUtil.getElement(base, "interval");
        Quantity interval = XMLQuantity.getQuantity(interval_node);
        return new SamplingImpl(numPoints, new TimeInterval(interval));
    }

    public static Sampling getSampling(XMLStreamReader parser)
            throws XMLStreamException {
        XMLUtil.gotoNextStartElement(parser, "numPoints");
        int numPoints = Integer.parseInt(parser.getElementText());
        XMLUtil.gotoNextStartElement(parser, "interval");
        Quantity interval = XMLQuantity.getQuantity(parser);
        return new SamplingImpl(numPoints, new TimeInterval(interval));
    }
}// XMLSampling
