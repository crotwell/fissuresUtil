

package edu.sc.seis.fissuresUtil.xml;

import edu.sc.seis.fissuresUtil.sac.*;
import edu.iris.Fissures.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.seismogramDC.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.apache.xpath.*;
import org.apache.xpath.objects.*;
import java.io.*;
import java.net.*;
import org.apache.log4j.*;


public class XMLDataSetAccess implements DataSetAccess, Serializable {

    public XMLDataSetAccess(DocumentBuilder docBuilder, Element config) {
	this.docBuilder = docBuilder;
	this.config = config;
    }

// 	XObject xobj = xpath.eval(context, path);
// 	if (xobj.getType() == XObject.CLASS_NODESET) {
// 	    NodeList nList = xobj.nodelist();
// 	    System.out.println("got "+nList.getLength());
// 	    Node n = nList.item(0); 
// 	    if (n instanceof Element) {
// 		return (Element)n;
// 	    }
// 	}
// 	// not a Element???
// 	return null;

    public String getId() {
	return evalString(config, "@datasetid");
    }

    public String getName() {
	return evalString(config, "name/text()");
    }

    public String[] getParameterRefNames() {
	return getAllAsStrings("parameterRef/text()");
    }

    public Element getParamter(String name) {
	NodeList nList = evalNodeList(config, 
				      "dataset/parameter[name/text()="+
				      dquote+name+dquote+"]");
	if (nList != null && nList.getLength() != 0) {
	    System.out.println("got "+nList.getLength());
	    Node n = nList.item(0); 
	    if (n instanceof Element) {
		return (Element)n;
	    }
	}

	// not a parameter, try parameterRef
	nList = evalNodeList(config, 
			     "dataset/parameterRef[text()="+dquote+name+dquote+"]");
	if (nList != null && nList.getLength() != 0) {
	    System.out.println("got "+nList.getLength());
	    Node n = nList.item(0); 
	    if (n instanceof Element) {
		SimpleXLink sl = new SimpleXLink(docBuilder, (Element)n);
		try {
		    return sl.retrieve();
		} catch (Exception e) {
		    logger.error("can't get paramterRef", e);
		} // end of try-catch
	    }
	}

	//can't find that name???
	return null;
    }

    public String[] getDataSetIds() {
	return getAllAsStrings("dataset/@datasetid");
    }

    public DataSetAccess getDataSet(String id) {
	NodeList nList = 
	    evalNodeList(config, "dataset[@datasetid="+dquote+id+dquote+"]");
	if (nList != null && nList.getLength() != 0) {
	    System.out.println("got "+nList.getLength());
	    Node n = nList.item(0); 
	    if (n instanceof Element) {
		return new XMLDataSetAccess(docBuilder, (Element)n);
	    }
	}

	// not an embedded dataset, try datasetRef
	nList = 
	    evalNodeList(config, "datasetRef[@datasetid="+dquote+id+dquote+"]");
	if (nList != null && nList.getLength() != 0) {
	    System.out.println("got "+nList.getLength());
	    Node n = nList.item(0); 
	    if (n instanceof Element) {
		try {
		    SimpleXLink sl = new SimpleXLink(docBuilder, (Element)n);
		    return new XMLDataSetAccess(docBuilder, sl.retrieve());
		} catch (Exception e) {
		    logger.error("Couldn't get datasetRef", e);
		} // end of try-catch
		
	    }
	}

	// can't find it
	return null;
    }

    public String[] getSeismogramNames() {
	return getAllAsStrings("dataset/SacSeismogram/name/text()");
    }

    public LocalSeismogramImpl getSeismogram(String name) {
	NodeList nList = 
	    evalNodeList(config, "dataset/SacSeismogram[name="+dquote+name+dquote+"]");
	if (nList != null && nList.getLength() != 0) {
	    try {
		System.out.println("got "+nList.getLength());
		Node n = nList.item(0); 
		if (n instanceof Element) {
		    Element e = (Element)n;
		    URL sacURL = 
			new URL(e.getAttribute("xlink:href"));
		    DataInputStream dis = new DataInputStream(new BufferedInputStream(sacURL.openStream())); 
		    SacTimeSeries sac = new SacTimeSeries();
		    sac.read(dis);
		    LocalSeismogramImpl seis = SacToFissures.getSeismogram(sac);
		    
		    NodeList propList = evalNodeList(e, "property");
		    if (propList != null && propList.getLength() != 0) {
			Property[] props = seis.getProperties();
			Property[] newProps = 
			    new Property[1+props.length+nList.getLength()];
			System.arraycopy(props, 0, newProps, 0, props.length);
			for (int i=0; i<propList.getLength(); i++) {
			    Element propElement = (Element)propList.item(i);
			    newProps[props.length+i] = 
				new Property(xpath.eval(propElement, "name/text()").str(),
					     xpath.eval(propElement, "value/text()").str());
			} // end of for
			newProps[newProps.length-1] = new Property("name",
								   name);
			seis.setProperties(newProps);
		    }
		    return seis;
		}
		
	    } catch (Exception e) {
		logger.error("Couldn't get seismogram", e);
	    } // end of try-catch
	    
	}
	return null;
    }

    protected String[] getAllAsStrings(String path) {
	NodeList nodes = evalNodeList(config, path);
	String[] out = new String[nodes.getLength()];
	for (int i=0; i<out.length; i++) {
	    out[i] = nodes.item(i).getNodeValue();
	} // end of for (int i=0; i++; i<out.length)
	return out;
    }

    protected NodeList evalNodeList(Node context, String path) {
	try {
	    XObject xobj = xpath.eval(context, path);
	    if (xobj != null && xobj.getType() == XObject.CLASS_NODESET) {
		return xobj.nodelist();
	    }
 	} catch (javax.xml.transform.TransformerException e) {
	    logger.error("Couldn't get NodeList", e);
 	} // end of try-catch
	return null;
    }

    protected String evalString(Node context, String path) {
	try {
	    return xpath.eval(config, "@datasetid").str();
 	} catch (javax.xml.transform.TransformerException e) {
	    logger.error("Couldn't get String", e);
 	} // end of try-catch
	return null;
    }

    private CachedXPathAPI xpath = new CachedXPathAPI();

    protected Element config;

    protected DocumentBuilder docBuilder;

    private static final String dquote = ""+'"';

    static Category logger = 
	Category.getInstance(XMLDataSetAccess.class.getName());

    public static void main (String[] args) {
	try {
	    BasicConfigurator.configure();

	    System.out.println("Starting..");
	    DocumentBuilderFactory factory
		= DocumentBuilderFactory.newInstance();
	    DocumentBuilder docBuilder = factory.newDocumentBuilder();


	    // just for testing
	    Document doc = docBuilder.parse(args[0]);
	    Element docElement = doc.getDocumentElement();
	    NodeList nList = docElement.getChildNodes();
	    XMLDataSetAccess dataset = null;

	    for (int i=0; i<nList.getLength(); i++) {
// 		Node m = nList.item(i);
// 		NodeList mList = m.getChildNodes();
// 		for (int j=0; j<mList.getLength(); j++) {
// 		    Node n = mList.item(j);
		Node n = nList.item(i);
		    if (n instanceof Element) {
			Element nodeElement = (Element)n;
			System.out.println(nodeElement.getTagName()+" {"+nodeElement.getAttribute("xlink:href")+"}");
			if (nodeElement.getTagName().equals("dataset")) {
			    System.out.println("######dataset yes");
			    dataset = new XMLDataSet(docBuilder, nodeElement);
			    System.out.println(nodeElement.getTagName()+" {"+nodeElement.getAttribute("datasetid")+"}");
			}
			if (nodeElement.getTagName().equals("datasetRef")) {
			    System.out.println("datasetRef yes");
			    SimpleXLink sxlink = new SimpleXLink(docBuilder, nodeElement);
			    Element e = sxlink.retrieve();
			    dataset = new XMLDataSet(docBuilder, e);
			    System.out.println(e.getTagName()+" {"+e.getAttribute("datasetid")+"}");
			} // end of if (nodeElement.getTagName().equals("dataset"))

			if (dataset != null) {
			    String[] names = dataset.getSeismogramNames();
			    for (int num=0; num<names.length; num++) {
				System.out.println("Seismogram name="+names[num]);
				LocalSeismogramImpl seis = dataset.getSeismogram(names[num]);
				System.out.println(seis.getNumPoints()+" "+seis.getMinValue());
			    } // end of for (int num=0; num<names.length; num++)
			    
			} // end of if (dataset != null)
			
		    } // end of if (node instanceof Element)
		    
		    //	} // end of for (int i=0; i<nList.getLength(); i++)
	    }
	} catch (Exception e) {
	    e.printStackTrace();	    
	} // end of try-catch
    } // end of main ()
    
}
