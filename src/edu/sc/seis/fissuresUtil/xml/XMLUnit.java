package edu.sc.seis.fissuresUtil.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import edu.iris.Fissures.Unit;
import edu.iris.Fissures.UnitBase;
import edu.iris.Fissures.model.UnitImpl;

/**
 * XMLUnit.java
 *
 *
 * Created: Mon Jul  1 15:53:29 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class XMLUnit {

    /**
     * StAX insert
     */
    public static void insert(XMLStreamWriter writer, Unit unit)
        throws XMLStreamException{

        writer.writeStartElement("the_unit_base");
        XMLUnitBase.insert(writer, unit.the_unit_base);
        XMLUtil.writeEndElementWithNewLine(writer);

        for (int i = 0; i < unit.elements.length; i++) {
            writer.writeStartElement("elements");
            XMLUnit.insert(writer, unit.elements[i]);
            XMLUtil.writeEndElementWithNewLine(writer);
        }

        XMLUtil.writeTextElement(writer, "power", ""+unit.power);
        XMLUtil.writeTextElement(writer, "name", unit.name);
        XMLUtil.writeTextElement(writer, "multi_factor", ""+unit.multi_factor);
        XMLUtil.writeTextElement(writer, "exponent", ""+unit.exponent);
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

        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "power",
                                                      ""+unit.power));

        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "name",
                                                      unit.name));

        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "multi_factor",
                                                      ""+unit.multi_factor));


        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "exponent",
                                                      ""+unit.exponent));

    }

    public static Unit getUnit(Element base) {

        //Get the UnitBase
        UnitBase the_unit_base = null;
        Element the_unit_base_node = XMLUtil.getElement(base, "the_unit_base");
        if(the_unit_base_node != null) {
            the_unit_base = XMLUnitBase.getUnitBase(the_unit_base_node);
        }

        //Get the subUnit Elements.
        Unit[] elements = new Unit[0];
        Element[] elements_list = XMLUtil.getElementArray(base, "elements");
        if(elements_list != null && elements_list.length != 0) {
            for(int counter = 0; counter < elements_list.length; counter++) {
                elements[counter] = XMLUnit.getUnit(elements_list[counter]);
            }
        }

        //Get the power
        int power = Integer.parseInt(XMLUtil.getText(XMLUtil.getElement(base, "power")));

        //Get the name
        String name = XMLUtil.getText(XMLUtil.getElement(base, "name"));

        //Get the multi_factor
        double multi_factor = (double)Float.parseFloat(XMLUtil.getText(XMLUtil.getElement(base, "multi_factor")));

        //Get the exponent
        int exponent = Integer.parseInt(XMLUtil.getText(XMLUtil.getElement(base, "exponent")));

        /*return new UnitImpl(
         power,
         name,
         multi_factor,
         exponent);*/
        return new UnitImpl(the_unit_base,
                            exponent,
                            power);
    }

    //  public static Unit getUnit(Element base) {

    //  //Get the UnitBase
    //  UnitBase the_unit_base = null;
    //  NodeList the_unit_base_node = XMLUtil.evalNodeList(base, "the_unit_base");
    //  if(the_unit_base_node != null && the_unit_base_node.getLength() != 0) {
    //      the_unit_base = XMLUnitBase.getUnitBase((Element)the_unit_base_node.item(0));
    //  }

    //  //Get the subUnit Elements.
    //  Unit[] elements = new Unit[0];
    //  NodeList elements_list = XMLUtil.evalNodeList(base, "elements");
    //  if(elements_list != null && elements_list.getLength() != 0) {
    //      for(int counter = 0; counter < elements_list.getLength(); counter++) {
    //      elements[counter] = XMLUnit.getUnit((Element)elements_list.item(0));
    //      }
    //  }

    //  //Get the power
    //  int power = Integer.parseInt(XMLUtil.evalString(base, "power"));

    //  //Get the name
    //  String name = XMLUtil.evalString(base, "name");

    //  //Get the multi_factor
    //  double multi_factor = (double)Float.parseFloat(XMLUtil.evalString(base, "multi_factor"));

    //  //Get the exponent
    //  int exponent = Integer.parseInt(XMLUtil.evalString(base, "exponent"));

    //  /*return new UnitImpl(
    //              power,
    //              name,
    //              multi_factor,
    //              exponent);*/
    //  return new UnitImpl(the_unit_base,
    //              exponent,
    //              power);
    //     }

}// XMLUnit
