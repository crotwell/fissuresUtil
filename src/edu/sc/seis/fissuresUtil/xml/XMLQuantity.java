package edu.sc.seis.fissuresUtil.xml;

import javax.xml.stream.XMLStreamException;
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
        throws XMLStreamException{

        XMLUtil.writeTextElement(writer, "value", ""+quantity.value);

        writer.writeStartElement("the_units");
        XMLUnit.insert(writer, quantity.the_units);
        XMLUtil.writeEndElementWithNewLine(writer);
    }

    /**
     * DOM insert
     */
    public static void insert(Element element, Quantity quantity){
        Document doc = element.getOwnerDocument();
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "value",
                                                      ""+quantity.value));
        /*UnitImpl unitImpl = UnitImpl.METER; // just get an instance
         Field[] knownUnits = UnitImpl.class.getDeclaredFields();
         for (int i=0; i<knownUnits.length; i++) {
         try {
         if (quantity.the_units.equals(knownUnits[i].get(unitImpl))) {
         // units match, just save name
         element.appendChild(XMLUtil.createTextElement(doc,
         "unit",
         knownUnits[i].getName()));
         return;
         } // end of if (quantity.the_units.equals(knownUnits[i]))
         } catch (IllegalAccessException e) {
         // should never happen for legit Unit, so keep going
         }

         } // end of for (int i=0; i<knownUnits.length; i++)

         // didn't find as a known unit, save whole thing...
         // implement this later...
         element.appendChild(XMLUtil.createTextElement(doc,
         "unit",
         "UNKNOWN UNIT TYPE "+quantity.the_units.toString()));

         */
        Element the_units = doc.createElement("the_units");
        XMLUnit.insert(the_units, quantity.the_units);
        element.appendChild(the_units);
    }

    public static QuantityImpl getQuantity(Element base) {

        //get the value
        double value = Double.parseDouble(XMLUtil.getText(XMLUtil.getElement(base, "value")));

        //get the_units
        Unit the_units = null;
        Element the_units_node = XMLUtil.getElement(base, "the_units");
        if(the_units_node != null) {

            the_units = XMLUnit.getUnit(the_units_node);
        }
        return new QuantityImpl(value, the_units);
    }

    //  public static Quantity getQuantity(Element base) {

    //  //get the value
    //  double value = Double.parseDouble(XMLUtil.evalString(base, "value"));

    //  //get the_units
    //  Unit the_units = null;
    //  NodeList the_units_node = XMLUtil.evalNodeList(base, "the_units");
    //  if(the_units_node != null && the_units_node.getLength() != 0) {

    //      the_units = XMLUnit.getUnit((Element)the_units_node.item(0));
    //  }
    //  return new QuantityImpl(value, the_units);
    //     }

}// XMLQuantity
