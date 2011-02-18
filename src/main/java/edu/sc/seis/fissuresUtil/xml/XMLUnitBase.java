package edu.sc.seis.fissuresUtil.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.iris.Fissures.UnitBase;

/**
 * XMLUnitBase.java
 * 
 * 
 * Created: Mon Jul 1 15:48:00 2002
 * 
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */
public class XMLUnitBase {

    /**
     * StAX insert
     */
    public static void insert(XMLStreamWriter writer, UnitBase unitBase)
            throws XMLStreamException {
        XMLUtil.writeTextElement(writer, "value", "" + unitBase.value());
    }

    /**
     * DOM insert
     */
    public static void insert(Element element, UnitBase unitBase) {
        Document doc = element.getOwnerDocument();
        element.appendChild(XMLUtil.createTextElement(doc, "value", ""
                + unitBase.value()));
    }

    public static int getUnitBaseType(Element base) {
        return Integer.parseInt(XMLUtil.getText(XMLUtil.getElement(base,
                                                                   "value")));
    }

    public static UnitBase getUnitBase(Element base) {
        return UnitBase.from_int(getUnitBaseType(base));
    }

    public static int getUnitBaseType(XMLStreamReader parser)
            throws XMLStreamException {
        XMLUtil.gotoNextStartElement(parser, "value");
        return Integer.parseInt(parser.getElementText());
    }

    public static UnitBase getUnitBase(XMLStreamReader parser)
            throws XMLStreamException {
        return UnitBase.from_int(getUnitBaseType(parser));
    }
}// XMLUnitBase
