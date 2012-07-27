package edu.sc.seis.fissuresUtil.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.iris.Fissures.Unit;
import edu.iris.Fissures.UnitBase;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;

/**
 * XMLUnit.java
 * 
 * 
 * Created: Mon Jul 1 15:53:29 2002
 * 
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */
public class XMLUnit {

    /**
     * StAX insert
     */
    public static void insert(XMLStreamWriter writer, Unit unit)
            throws XMLStreamException {
        writer.writeStartElement("the_unit_base");
        XMLUnitBase.insert(writer, unit.the_unit_base);
        XMLUtil.writeEndElementWithNewLine(writer);
        for(int i = 0; i < unit.elements.length; i++) {
            writer.writeStartElement("elements");
            XMLUnit.insert(writer, unit.elements[i]);
            XMLUtil.writeEndElementWithNewLine(writer);
        }
        XMLUtil.writeTextElement(writer, "power", "" + unit.power);
        XMLUtil.writeTextElement(writer, "name", unit.name);
        XMLUtil.writeTextElement(writer, "multi_factor", "" + unit.multi_factor);
        XMLUtil.writeTextElement(writer, "exponent", "" + unit.exponent);
    }

    /**
     * DOM insert
     */
    public static void insert(Element element, Unit unit) {
        Document doc = element.getOwnerDocument();
        Element the_unit_base = doc.createElement("the_unit_base");
        XMLUnitBase.insert(the_unit_base, unit.the_unit_base);
        element.appendChild(the_unit_base);
        Element subUnit;
        for(int counter = 0; counter < unit.elements.length; counter++) {
            subUnit = doc.createElement("elements");
            XMLUnit.insert(subUnit, unit.elements[counter]);
            element.appendChild(subUnit);
        }
        element.appendChild(XMLUtil.createTextElement(doc, "power", ""
                + unit.power));
        element.appendChild(XMLUtil.createTextElement(doc, "name", unit.name));
        element.appendChild(XMLUtil.createTextElement(doc, "multi_factor", ""
                + unit.multi_factor));
        element.appendChild(XMLUtil.createTextElement(doc, "exponent", ""
                + unit.exponent));
    }

    public static Unit getUnit(Element base) {
        int unitBaseType = XMLUnitBase.getUnitBaseType(XMLUtil.getElement(base,
                                                                        "the_unit_base"));
        UnitBase unitBase = UnitBase.from_int(unitBaseType);
        NodeList subUnitNodeList = DOMHelper.getElements(base, "elements");
        Unit[] subUnits = new Unit[subUnitNodeList.getLength()];
        for(int i = 0; i < subUnitNodeList.getLength(); i++) {
            Element subUnitElement = (Element)subUnitNodeList.item(i);
            subUnits[i] = XMLUnit.getUnit(subUnitElement);
        }
        int power = Integer.parseInt(XMLUtil.getText(XMLUtil.getElement(base,
                                                                        "power")));
        String name = XMLUtil.getText(XMLUtil.getElement(base, "name"));
        double multi = Double.parseDouble(XMLUtil.getText(XMLUtil.getElement(base,
                                                                             "multi_factor")));
        int exponent = Integer.parseInt(XMLUtil.getText(XMLUtil.getElement(base,
                                                                           "exponent")));
        if(unitBaseType == UnitBase._COMPOSITE) {
            return new UnitImpl(subUnits, power, name, multi, exponent);
        } else {
            return new UnitImpl(unitBase, power, name, multi, exponent);
        }
    }

    public static Unit getUnit(XMLStreamReader parser)
            throws XMLStreamException {
        XMLUtil.gotoNextStartElement(parser, "the_unit_base");
        int unitBaseType = XMLUnitBase.getUnitBaseType(parser);
        UnitBase unitBase = UnitBase.from_int(unitBaseType);
        List subUnits = new ArrayList();
        XMLUtil.getNextStartElement(parser);
        while(parser.getLocalName().equals("elements")) {
            subUnits.add(XMLUnit.getUnit(parser));
            XMLUtil.getNextStartElement(parser);
        }
        int power = Integer.parseInt(parser.getElementText());
        XMLUtil.gotoNextStartElement(parser, "name");
        String name = parser.getElementText();
        XMLUtil.gotoNextStartElement(parser, "multi_factor");
        double multi = Double.parseDouble(parser.getElementText());
        XMLUtil.gotoNextStartElement(parser, "exponent");
        int exponent = Integer.parseInt(parser.getElementText());
        if(unitBaseType == UnitBase._COMPOSITE) {
            return new UnitImpl((Unit[])subUnits.toArray(new Unit[0]),
                                power,
                                name,
                                multi,
                                exponent);
        } else {
            return new UnitImpl(unitBase, power, name, multi, exponent);
        }
    }
}// XMLUnit
