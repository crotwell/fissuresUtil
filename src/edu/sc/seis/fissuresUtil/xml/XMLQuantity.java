package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.*;
import edu.iris.Fissures.model.UnitImpl;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.apache.log4j.*;
import java.lang.reflect.Field;

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

     public static void insert(Element element, Quantity quantity){
         Document doc = element.getOwnerDocument();
        element.appendChild(XMLUtil.createTextElement(doc, 
                                                      "value", 
                                                      ""+quantity.value));
        UnitImpl unitImpl = UnitImpl.METER; // just get an instance
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

        
     }

}// XMLQuantity
