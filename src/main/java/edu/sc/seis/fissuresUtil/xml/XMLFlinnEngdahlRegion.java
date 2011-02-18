package edu.sc.seis.fissuresUtil.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.iris.Fissures.FlinnEngdahlRegion;
import edu.iris.Fissures.FlinnEngdahlType;
import edu.iris.Fissures.model.FlinnEngdahlRegionImpl;

/**
 * XMLFlinnEngdahlRegion.java
 *
 *
 * Created: Thu Jun 13 11:06:22 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class XMLFlinnEngdahlRegion {

    /**
     * StAX insert
     */
    public static void insert(XMLStreamWriter writer, FlinnEngdahlRegion region)
        throws XMLStreamException{

        if (region == null){
            XMLUtil.writeTextElement(writer, "type", "SEISMIC_REGION");
            XMLUtil.writeTextElement(writer, "number", ""+0);
        }
        else {
            if (region.type.equals(FlinnEngdahlType.SEISMIC_REGION)){
                XMLUtil.writeTextElement(writer, "type", "SEISMIC_REGION");
            }
            else{
                XMLUtil.writeTextElement(writer, "type", "GEOGRAPHIC_REGION");
            }
            XMLUtil.writeTextElement(writer, "number", ""+region.number);
        }
    }

    /**
     * DOM insert
     */
    public static void insert(Element element, FlinnEngdahlRegion region){
        Document doc = element.getOwnerDocument();
        if (region == null) {
            element.appendChild(XMLUtil.createTextElement(doc,
                                                          "type",
                                                          "SEISMIC_REGION"));
            element.appendChild(XMLUtil.createTextElement(doc,
                                                          "number",
                                                          ""+0));
        } else {
            if (region.type.equals(FlinnEngdahlType.SEISMIC_REGION)) {
                element.appendChild(XMLUtil.createTextElement(doc,
                                                              "type",
                                                              "SEISMIC_REGION"));
            } else {
                element.appendChild(XMLUtil.createTextElement(doc,
                                                              "type",
                                                              "GEOGRAPHIC_REGION"));
            }
            element.appendChild(XMLUtil.createTextElement(doc,
                                                          "number",
                                                          ""+region.number));
        }
    }

    public static FlinnEngdahlRegion getRegion(Element base) {

        String type = XMLUtil.getText(XMLUtil.getElement(base, "type"));
        int value = Integer.parseInt(XMLUtil.getText(XMLUtil.getElement(base, "number")));
        FlinnEngdahlRegion flinnEngdahlRegion = null;
        if(type.equals("SEISMIC_REGION")) {
            flinnEngdahlRegion = new FlinnEngdahlRegionImpl(FlinnEngdahlType.SEISMIC_REGION,
                                                            value);
        } else if(type.equals("GEOGRAPHIC_REGION")) {
            flinnEngdahlRegion = new FlinnEngdahlRegionImpl(FlinnEngdahlType.GEOGRAPHIC_REGION,
                                                            value);
        }
        return flinnEngdahlRegion;
    }
    
    public static FlinnEngdahlRegion getRegion(XMLStreamReader parser) throws XMLStreamException {
        XMLUtil.gotoNextStartElement(parser, "type");
        String type = parser.getElementText();
        XMLUtil.gotoNextStartElement(parser, "number");
        int number = Integer.parseInt(parser.getElementText());
        FlinnEngdahlRegion flinnEngdahlRegion = null;
        if(type.equals("SEISMIC_REGION")) {
            flinnEngdahlRegion = new FlinnEngdahlRegionImpl(FlinnEngdahlType.SEISMIC_REGION,
                                                            number);
        } else if(type.equals("GEOGRAPHIC_REGION")) {
            flinnEngdahlRegion = new FlinnEngdahlRegionImpl(FlinnEngdahlType.GEOGRAPHIC_REGION,
                                                            number);
        }
        return flinnEngdahlRegion;
    }

}// XMLFlinnEngdahlRegion
