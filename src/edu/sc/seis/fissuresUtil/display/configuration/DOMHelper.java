package edu.sc.seis.fissuresUtil.display.configuration;

import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author groves Created on Feb 17, 2005
 */
public class DOMHelper {

    public static Element getElement(Element el, String name) {
        if(hasElement(el, name)) {
            return (Element)el.getElementsByTagName(name).item(0);
        } else {
            return null;
        }
    }

    public static boolean hasElement(Element el, String name) {
        return el.getElementsByTagName(name).getLength() > 0;
    }

    public static String extractText(Element el, String xpath) {
        return extractText(el, xpath, null);
    }

    public static String extractText(Element el,
                                     String xpath,
                                     String defaultValue) {
        xpath = xpath + "/text()";
        try {
            Node n = XPathAPI.selectSingleNode(el, xpath);
            if(n == null) {
                if(defaultValue == null) { throw new RuntimeException("No nodes found matching XPath "
                        + xpath); }
                return defaultValue;
            } else {
                return n.getNodeValue();
            }
        } catch(DOMException e) {
            handle(e);
        } catch(TransformerException e) {
            handle(e, xpath);
        }
        throw new RuntimeException("Should be unreachable");
    }

    public static NodeList extractNodes(Element el, String xpath) {
        try {
            return XPathAPI.selectNodeList(el, xpath);
        } catch(DOMException e) {
            handle(e);
        } catch(TransformerException e) {
            handle(e, xpath);
        }
        throw new RuntimeException("Should be unreachable");
    }

    public static Element extractElement(Element el, String xpath) {
        try {
            return (Element)XPathAPI.selectSingleNode(el, xpath);
        } catch(DOMException e) {
            handle(e);
        } catch(TransformerException e) {
            handle(e, xpath);
        }
        throw new RuntimeException("Should be unreachable");
    }

    public static void handle(DOMException e) {
        throw new RuntimeException("This DOMException seems like some sort of library error.  Don't know what I could do further up the stack, so I just wrapped it in this runtime exception.",
                                   e);
    }

    public static void handle(TransformerException e, String xpath) {
        throw new RuntimeException("Caught a transformation exception!  This probably means the XPath "
                                           + xpath + " is screwed up.",
                                   e);
    }
}