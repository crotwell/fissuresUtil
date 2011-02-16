package edu.sc.seis.fissuresUtil.xml;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Represents a simple XLink. Provides methods for following the link if the
 * protocol is known, ie  URLConnection can be gotten from Java.
 *
 * @author Philip Crotwell
 * @version $Id: SimpleXLink.java 22054 2011-02-16 16:51:38Z crotwell $
*/
public class SimpleXLink {

    /** Creates a SimpleXLink for following the given element. This assumes
    that the link is absolute, since there is no base. */
    SimpleXLink(DocumentBuilder docBuilder, Element element) {
    this(docBuilder, element, null);
    }

    /** Creates a SimpleXLink for following the given element. The
    href in the element is evaluated relative to the given base. */
    SimpleXLink(DocumentBuilder docBuilder, Element element, URL base) {
    this.docBuilder = docBuilder;
    this.element = element;
    this.base = base;
    }

    /** Trys to retrieve as an XML Element referenced by this simple XLink.
    It is assumed that the href attribute is name spaced with xlink.
    @throws IllegalArgumentException if the link is not a simple xlink
    @returns the Element pointed to, or null if it doesn't exist

    */
    public Element retrieve()
    throws java.net.MalformedURLException,
    java.io.IOException,
    org.xml.sax.SAXException,
    javax.xml.transform.TransformerException
    {
    String xlink = element.getAttribute("xlink:href");
    int sharpIndex = xlink.indexOf("#");
    String fragment = "";
    if (sharpIndex != -1) {
        fragment = xlink.substring(sharpIndex+1, xlink.length());
    } // end of if (sharpIndex != -1)

    int index;

    // check for escaped quotes, ASCII 22 (hex) is quote
    while ((index = fragment.indexOf("%22")) != -1) {
        fragment = fragment.substring(0,index)+'"'+ fragment.substring(index+3);
        //System.out.println(fragment);
    } // end of while (fragment.indexOf("%22") != -1)

    URL url = null;
    if (xlink.startsWith("http") || xlink.startsWith("ftp")) {
        // assume absolute
        url = new URL(xlink);
    } else if (base != null) {
        // assume relative
        url = new URL(base, xlink);
    } // end of else

    if (url != null) {
        InputStream conn = url.openStream();
        BufferedInputStream inStream = new BufferedInputStream(conn);
        Document doc = docBuilder.parse(inStream);
        return retrieve(doc, fragment);
    } else {
        // assume it is a relative path, within current document
        return retrieve(element, fragment);
    } // end of else
    }

    public Element retrieve(Node context, String path)
    throws java.net.MalformedURLException,
    java.io.IOException,
    org.xml.sax.SAXException,
    javax.xml.transform.TransformerException
    {
        logger.debug("path="+path);
        if (context instanceof Document &&
            (path == null || path.length() == 0)) {
            // no path so get document element
            return ((Document)context).getDocumentElement();
        } // end of if (path == null || path.length() == 0)

    XObject xobj = XPathAPI.eval(context, path);
    if (xobj.getType() == XObject.CLASS_NODESET) {
        NodeList nList = xobj.nodelist();
        Node n = nList.item(0);
        if (n instanceof Element) {
        return (Element)n;
        }
    }
    // not a Element???
    return null;
    }

    public static void main (String[] args) throws Exception {
        //System.out.println("Starting..");
        DocumentBuilderFactory factory
        = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = factory.newDocumentBuilder();


    // just for testing
    Document doc = docBuilder.parse(args[0]);
    Element docElement = doc.getDocumentElement();
    NodeList nList = docElement.getChildNodes();
    for (int i=0; i<nList.getLength(); i++) {
        Node m = nList.item(i);
    NodeList mList = m.getChildNodes();
    for (int j=0; j<mList.getLength(); j++) {
        Node n = mList.item(j);
        if (n instanceof Element) {
        Element nodeElement = (Element)n;
        //System.out.println(nodeElement.getTagName()+" {"+nodeElement.getAttribute("xlink:href")+"}");
        if (nodeElement.getTagName().equals("datasetRef")) {
            //System.out.println("datasetRef yes");
        SimpleXLink sxlink = new SimpleXLink(docBuilder, nodeElement);
        Element e = sxlink.retrieve();
        //System.out.println(e.getTagName()+" {"+e.getAttribute("datasetid")+"}");
        } // end of if (nodeElement.getTagName().equals("dataset"))
        } // end of if (node instanceof Element)

    } // end of for (int i=0; i<nList.getLength(); i++)
    }

    } // end of main ()


    protected Element element;

    protected DocumentBuilder docBuilder;

    protected URL base;

    private static Logger logger = LoggerFactory.getLogger(SimpleXLink.class.getName());

}
