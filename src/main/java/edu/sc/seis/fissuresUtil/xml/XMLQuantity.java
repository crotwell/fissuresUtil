package edu.sc.seis.fissuresUtil.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.Unit;
import edu.iris.Fissures.model.QuantityImpl;

/**
 * XMLQuantity.java
 * 
 * 
 * Created: Wed Jun 12 15:32:37 2002
 * 
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */
public class XMLQuantity {

    /**
     * StAX insert
     */
    public static void insert(XMLStreamWriter writer, Quantity quantity)
            throws XMLStreamException {
        XMLUtil.writeTextElement(writer, "value", "" + quantity.value);
        writer.writeStartElement("the_units");
        XMLUnit.insert(writer, quantity.the_units);
        XMLUtil.writeEndElementWithNewLine(writer);
    }

    /**
     * DOM insert
     */
    public static void insert(Element element, Quantity quantity) {
        Document doc = element.getOwnerDocument();
        element.appendChild(XMLUtil.createTextElement(doc, "value", ""
                + quantity.value));
        Element the_units = doc.createElement("the_units");
        XMLUnit.insert(the_units, quantity.the_units);
        element.appendChild(the_units);
    }

    public static QuantityImpl getQuantity(Element base) {
        double value = Double.parseDouble(XMLUtil.getText(XMLUtil.getElement(base,
                                                                             "value")));
        Unit the_units = null;
        Element the_units_node = XMLUtil.getElement(base, "the_units");
        if(the_units_node != null) {
            the_units = XMLUnit.getUnit(the_units_node);
        }
        return new QuantityImpl(value, the_units);
    }

    public static QuantityImpl getQuantity(XMLStreamReader parser)
            throws XMLStreamException {
        XMLUtil.gotoNextStartElement(parser, "value");
        double value = Double.parseDouble(parser.getElementText());
        XMLUtil.gotoNextStartElement(parser, "the_units");
        Unit the_units = XMLUnit.getUnit(parser);
        return new QuantityImpl(value, the_units);
    }
}// XMLQuantity
