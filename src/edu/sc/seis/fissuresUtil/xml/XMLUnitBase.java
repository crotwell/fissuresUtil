package edu.sc.seis.fissuresUtil.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import edu.iris.Fissures.UnitBase;


/**
 * XMLUnitBase.java
 *
 *
 * Created: Mon Jul  1 15:48:00 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class XMLUnitBase {

    /**
     * StAX insert
     */
    public static void insert(XMLStreamWriter writer, UnitBase unitBase)
        throws XMLStreamException{

        XMLUtil.writeTextElement(writer, "value", ""+unitBase.value());
    }

    /**
     * DOM insert
     */
    public static void insert(Element element, UnitBase unitBase) {

        Document doc = element.getOwnerDocument();
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "value",
                                                      ""+unitBase.value()));
    }

    public static UnitBase getUnitBase(Element base) {

        int value = Integer.parseInt(XMLUtil.getText(XMLUtil.getElement(base,"value")));
        //Integer.parseInt(XMLUtil.evalString(base, "value"));
        return UnitBase.from_int(value);
    }

}// XMLUnitBase
