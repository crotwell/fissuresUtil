package edu.sc.seis.fissuresUtil.xml;

import org.w3c.dom.*;
import java.util.ArrayList;
import javax.xml.parsers.*;
import org.apache.xpath.*;
import org.apache.xpath.objects.*;

/**
 * XMLUtil.java
 *
 *
 * Created: Wed Jun 12 10:03:01 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class XMLUtil {

    public static Element createTextElement(Document doc, 
                                            String elementName, 
                                            String value) {
        Element element = doc.createElement(elementName);
        Text text = doc.createTextNode(value);
        element.appendChild(text);
        return element;
    }
    
    /**
     * Describe <code>evalNodeList</code> method here.
     *
     * @param context a <code>Node</code> value
     * @param path a <code>String</code> value
     * @return a <code>NodeList</code> value
     */
    public static NodeList evalNodeList(Node context, String path) {
	try {
	    //xpath = new CachedXPathAPI();
	     XObject xobj = xpath.eval(context, path);
            if (xobj != null && xobj.getType() == XObject.CLASS_NODESET) {
                return xobj.nodelist();
            }
        } catch (javax.xml.transform.TransformerException e) {
            //System.out.println("Couldn't get NodeList"+e);
	    } // end of try-catch
        return null;
    }

    /**
     * Describe <code>evalString</code> method here.
     *
     * @param context a <code>Node</code> value
     * @param path a <code>String</code> value
     * @return a <code>String</code> value
     */
    public static String evalString(Node context, String path) {
        try {
            return xpath.eval(context, path).str();
        } catch (javax.xml.transform.TransformerException e) {
            //System.out.println("Couldn't get String"+ e);
        } // end of try-catch
        return null;
    }


    /** returns the first text child within the node.
     */
    public static String getText(Element config) {
	if(config == null) return new String("");
        NodeList children = config.getChildNodes();
        Node node;
        for (int i=0; i<children.getLength(); i++) {
            node = children.item(i);
            if (node instanceof Text) {
                return node.getNodeValue();
            }
        }
        //nothing found, return null
        return new String("");
    }

    /** returns the element with the given name
     */

    public static Element getElement(Element config, String elementName) {
	
	NodeList children = config.getChildNodes();
	Node node;
	for(int counter = 0; counter < children.getLength(); counter++ ) {
	    node = children.item(counter);
	    if(node instanceof Element ) {
		if(((Element)node).getTagName().equals(elementName)) {
		    return ((Element)node);
		}
	    }
	    
	}
	return null;
    }

    public static Element[] getElementArray(Element config, String elementName) {
	NodeList children = config.getChildNodes();
	Node node;
	ArrayList arrayList = new ArrayList();
	for(int counter = 0; counter < children.getLength(); counter++) {

	    node = children.item(counter);
	    if(node instanceof Element) {
		if(((Element)node).getTagName().equals(elementName)) {
		    arrayList.add((Element)node);
		}
	    }
	}
	Element[] elementArray = new Element[arrayList.size()];
	elementArray = (Element[]) arrayList.toArray(elementArray);
	return elementArray;
    }


    private static CachedXPathAPI xpath = new CachedXPathAPI();
}// XMLUtil
