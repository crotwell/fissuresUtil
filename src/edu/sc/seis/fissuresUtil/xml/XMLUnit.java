package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.apache.log4j.*;

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
    
}// XMLUnit
