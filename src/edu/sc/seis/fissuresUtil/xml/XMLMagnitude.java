package edu.sc.seis.fissuresUtil.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.Magnitude;

/**
 * XMLMagnitude.java
 *
 *
 * Created: Wed Jun 12 16:39:51 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class XMLMagnitude {

    /**
     * StAX insert
     */
    public static void insert(XMLStreamWriter writer, Magnitude mag)
        throws XMLStreamException{

        XMLUtil.writeTextElement(writer, "type", mag.type);
        XMLUtil.writeTextElement(writer, "value", ""+mag.value);
        XMLUtil.writeTextElement(writer, "contributor", mag.contributor);
    }

    /**
     * DOM insert
     */
    public static void insert(Element element, Magnitude mag){
        Document doc = element.getOwnerDocument();
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "type",
                                                      mag.type));
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "value",
                                                      ""+mag.value));
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "contributor",
                                                      mag.contributor));
    }


    public static Magnitude getMagnitude(Element base) {
        String type = XMLUtil.getText(XMLUtil.getElement(base, "type"));
        float value = Float.parseFloat(XMLUtil.getText(XMLUtil.getElement(base, "value")));
        String contributor = XMLUtil.getText(XMLUtil.getElement(base, "contributor"));
        return new Magnitude(type,
                             value,
                             contributor);

    }
    
    public static Magnitude getMagnitude(XMLStreamReader parser) throws XMLStreamException {
        XMLUtil.gotoNextStartElement(parser, "type");
        String type = parser.getElementText();
        XMLUtil.gotoNextStartElement(parser, "value");
        float value = Float.parseFloat(parser.getElementText());
        XMLUtil.gotoNextStartElement(parser, "contributor");
        String contributor = parser.getElementText();
        return new Magnitude(type,
                             value,
                             contributor);

    }
    
}//XMLMagnitude
