package edu.sc.seis.fissuresUtil.xml;

import edu.sc.seis.fissuresUtil.sac.*;
import edu.iris.Fissures.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.IfParameterMgr.ParameterRef;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.apache.xpath.*;
import org.apache.xpath.objects.*;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import org.apache.log4j.*;

/**
 * Access to a dataset stored as an XML file.
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version $Id: XMLDataSet.java 1922 2002-06-20 18:38:06Z crotwell $
 */
public class XMLDataSet implements DataSet, Serializable {

    /**
     * Creates a new <code>XMLDataSet</code> instance.
     *
     * @param docBuilder a <code>DocumentBuilder</code> value
     * @param datasetURL an <code>URL</code> to a dsml file
     */
    public XMLDataSet(DocumentBuilder docBuilder, URL datasetURL) {
        this.base = datasetURL; 
        this.docBuilder = docBuilder;
        Document doc = docBuilder.newDocument();
        config = doc.createElement("dataset");
        config.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
    }

    /**
     * Loads a xml dataset from a URL.
     *
     * @param datasetURL an <code>URL</code> to an xml dataset
     * @return a <code>XMLDataSet</code> populated form the URL
     */
    public static XMLDataSet load(URL datasetURL) {
        XMLDataSet dataset = null;
        try {
            DocumentBuilderFactory factory
                = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
	    
            Document doc = docBuilder.parse(new BufferedInputStream(datasetURL.openStream()));
            Element docElement = doc.getDocumentElement();

            if (docElement.getTagName().equals("dataset")) {
                dataset = new XMLDataSet(docBuilder, datasetURL, docElement);
            }
        } catch (java.io.IOException e) {
            logger.error("Error loading XMLDataSet",e);
        } catch (org.xml.sax.SAXException e) {
            logger.error("Error loading XMLDataSet",e);
        } catch (javax.xml.parsers.ParserConfigurationException e) {
            logger.error("Error loading XMLDataSet",e);
        } // end of try-catch
        return dataset;
	
    }

    /**
     * Creates an empty dataset.
     * @param docBuilder a <code>DocumentBuilder</code> to use to create the 
     *      document.
     * @param base the <code>URL</code> other urls should be made relative to.
     * @param id a unique id
     * @param name the display name of this dataset
     * @param owner the owner/creator of this dataset
     */
    public XMLDataSet(DocumentBuilder docBuilder, 
                      URL base, 
                      String id, 
                      String name,
                      String owner) {
        this(docBuilder, base);
        Document doc = config.getOwnerDocument();
        Element nameE = doc.createElement("name");
        Text text = doc.createTextNode(name);
        nameE.appendChild(text);
        Element ownerE = doc.createElement("owner");
        text = doc.createTextNode(owner);
        ownerE.appendChild(text);
        config.setAttribute("datasetid", id);
        config.appendChild(nameE);
        config.appendChild(ownerE);
    }

    /**
     * Creates a new <code>XMLDataSet</code> instance.
     *
     * @param docBuilder a <code>DocumentBuilder</code> to use to create the 
     *      document.
     * @param base the <code>URL</code> other urls should be made relative to.
     * @param config the dataset contents as a DOM <code>Element</code>
     */
    public XMLDataSet(DocumentBuilder docBuilder, URL base, Element config) {
        this(docBuilder, base);
        this.config = config;
    }

    /**
     * Gets the dataset Id. This should be unique.
     *
     * @return a <code>String</code> id
     */
    public String getId() {
        return evalString(config, "@datasetid");
    }

    /**
     * Gets the base URL that other URLs in this dataset are relative to.
     *
     * @return the base <code>URL</code>
     */
    public URL getBase() {
        return base;
    }

    /**
     * Sets the base URL that other URLs in this dataset are relative to.
     *
     * @param base an <code>URL</code>
     */
    public void setBase(URL base) {
        this.base = base;
    }

    /**
     * Gets the displayable name.
     *
     * @return a <code>String</code> name
     */
    public String getName() {
        return evalString(config, "name/text()");
    }

    /**
     * Sets the displayable name.
     *
     * @param name a <code>String</code> name
     */
    public void setName(String name) {
        Element nameElement = evalElement(config, "name");
        Text text = config.getOwnerDocument().createTextNode(name);
        nameElement.appendChild(text);
        config.appendChild(nameElement);
    }

    /**
     * Gets the names of all parameters within this dataset.
     *
     * @return an array of names.
     */
    public String[] getParameterNames() {
        String[] params = getAllAsStrings("parameter/name/text()");
        String[] paramRefs = getAllAsStrings("parameterRef/text()");
        String[] all = new String[params.length+paramRefs.length];
        System.arraycopy(params, 0, all, 0, params.length);
        System.arraycopy(paramRefs, 0, all, params.length, paramRefs.length);
        return all;
    }

    /**
     * Gets the parameter with the given name. The returns null if the 
     * parameter cannot be found.
     *
     * @param name a <code>String</code> paramter name
     * @return the parameter with that name
     */
    public Object getParameter(String name) {
        if (parameterCache.containsKey(name)) {
            return parameterCache.get(name);
        } // end of if (parameterCache.containsKey(name))
	
        NodeList nList = evalNodeList(config, 
                                      "parameter[name/text()="+
                                      dquote+name+dquote+"]");
        if (nList != null && nList.getLength() != 0) {
            Node n = nList.item(0); 
            if (n instanceof Element) {
                parameterCache.put(name, n);
                return (Element)n;
            }
        }

        // not a parameter, try parameterRef
        nList = evalNodeList(config, 
                             "parameterRef[text()="+dquote+name+dquote+"]");
        if (nList != null && nList.getLength() != 0) {
            Node n = nList.item(0);
            if (n instanceof Element) {
                SimpleXLink sl = new SimpleXLink(docBuilder, (Element)n);
                try {
                    Element e = sl.retrieve();
                    parameterCache.put(name, e);
                    return e; 
                } catch (Exception e) {
                    logger.error("can't get paramterRef", e);
                } // end of try-catch
            }
        }

        //can't find that name???
        return null;
    }

    /**
     * Adds a new parameter. Currently objects that are not DOM Elements are
     * stored in memory, but cannot be premanantly saved in the xml file.
     *
     * @param name a <code>String</code> name for this parameter
     * @param param the new parameter
     * @param audit the audit related to this paramter 
     */
    public void addParameter(String name, 
                             Object value,
                             AuditInfo[] audit) {
        parameterCache.put(name, value);
        if (value instanceof Element) {
	    Element parameter = 
		config.getOwnerDocument().createElement("parameter");
	    XMLParameter.insert(parameter, name, (Element)value);
            config.appendChild(parameter);
        } else {
            logger.warn("Parameter is only stored in memory.");
        } // end of else
	
    }

    /**
     * Adds a reference to a remote parameter.
     *
     * @param paramURL an <code>URL</code> value
     * @param name a <code>String</code> value
     * @param audit an <code>AuditInfo[]</code> value
     */
    public void addParameterRef(URL paramURL, 
                                String name, 
                                AuditInfo[] audit) {
        String baseStr = base.toString();
        String paramStr = paramURL.toString();
        if (paramStr.startsWith(baseStr)) {
            // use relative URL
            paramStr = paramStr.substring(baseStr.length());
        } // end of if (paramStr.startsWith(baseStr))
	
        Document doc = config.getOwnerDocument();
        Element param = doc.createElement("parameterRef");
        param.setAttributeNS(xlinkNS, "xlink:type", "simple");
        param.setAttributeNS(xlinkNS, "xlink:href", paramStr);
        Text text = doc.createTextNode(name);
        param.appendChild(text);

        config.appendChild(param);
    }

    /**
     * Gets the Ids for all child datasets of this dataset.
     *
     * @return a <code>String[]</code> id
     */
    public String[] getDataSetIds() {
        return getAllAsStrings("*/@datasetid");
    }

    /**
     * Gets the names of all child datasets of this dataset.
     *
     * @return a <code>String[]</code> value
     */
    public String[] getDataSetNames() {
        String[] ids = getDataSetIds();
        String[] names = new String[ids.length];
        for (int i=0; i<ids.length; i++) {
            DataSet ds = getDataSetById(ids[i]);
            names[i] = ds.getName();
        } // end of for (int i=0; i<ids.length; i++)
        return names;
    }

    /**
     * Gets the dataset with the given name.
     *
     * @param name a <code>String</code> dataset name
     * @return a <code>DataSet</code>, or null if it cannot be found
     */
    public DataSet getDataSet(String name) {
        String[] ids = getDataSetIds();
        for (int i=0; i<ids.length; i++) {
            DataSet ds = getDataSetById(ids[i]);
            if (name == ds.getName()) {
                return ds;
            }
        } // end of for (int i=0; i<ids.length; i++)
        return null;
    }

    /**
     * Adds a child dataset.
     *
     * @param dataset a <code>DataSet</code>
     * @param audit the audit info for this dataset addition
     */
    public void addDataSet(DataSet dataset,
                           AuditInfo[] audit) {
        if (dataset instanceof XMLDataSet) {
            XMLDataSet xds = (XMLDataSet)dataset;
            Element element = xds.getElement();
            if (element.getOwnerDocument().equals(config.getOwnerDocument())) {
                config.appendChild(element);
                logger.debug("dataset append "+config.getChildNodes().getLength());
            } else {
                // not from the same document, must clone
                Node copyNode = config.getOwnerDocument().importNode(element, 
                                                                     true);
                config.appendChild(copyNode);
                logger.debug("dataset import "+config.getChildNodes().getLength());
                NodeList nl = config.getChildNodes();
                for (int i=0; i<nl.getLength(); i++) {
                    logger.debug("node "+nl.item(i).getLocalName());
                } // end of for (int i=0; i<nl.getLenght(); i++)
		
            } // end of else
        } else {
            logger.warn("Attempt to add non-XML dataset");
        } // end of else
	
    }

    /**
     * Creates a new Child DataSet.
     *
     * @param id a <code>String</code> id
     * @param name a <code>String</code> name
     * @param owner a <code>String</code> owner/creator
     * @param audit the audit
     * @return a <code>DataSet</code>
     */
    public DataSet createChildDataSet(String id, String name, String owner,
                                      AuditInfo[] audit) {
        XMLDataSet dataset = new XMLDataSet(docBuilder, base, id, name, owner);
        addDataSet(dataset, audit);
        return dataset;
    }

    /**
     * Gets the dataset with the given id.
     *
     * @param id a <code>String</code> id
     * @return a <code>DataSet</code>
     */
    public DataSet getDataSetById(String id) {
        if (dataSetCache.containsKey(id)) {
            return (DataSet)dataSetCache.get(id);
        }

        NodeList nList = 
            evalNodeList(config, "//dataset[@datasetid="+dquote+id+dquote+"]");
        if (nList != null && nList.getLength() != 0) {
            Node n = nList.item(0); 
            if (n instanceof Element) {
                XMLDataSet dataset = new XMLDataSet(docBuilder, 
                                                    base,
                                                    (Element)n);
                dataSetCache.put(id, dataset);
                return dataset;
            }
        }

        // not an embedded dataset, try datasetRef
        nList = 
            evalNodeList(config, "datasetRef[@datasetid="+dquote+id+dquote+"]");
        if (nList != null && nList.getLength() != 0) {
            Node n = nList.item(0); 
            if (n instanceof Element) {
                try {
                    SimpleXLink sl = new SimpleXLink(docBuilder, (Element)n, base);
                    return new XMLDataSet(docBuilder, base, sl.retrieve());
                } catch (Exception e) {
                    logger.error("Couldn't get datasetRef", e);
                } // end of try-catch
		
            }
        }

        // can't find it
        return null;
    }

    /**
     * Gets the names of the seismograms in this dataset.
     *
     * @return the names.
     */
    public String[] getSeismogramNames() {
	String[] names = getAllAsStrings("SacSeismogram/name/text()");
	logger.debug("found "+names.length+" names in xml");
	logger.debug("cache has "+seismogramCache.keySet().size());
	String outNames[] = 
	    new String[names.length+seismogramCache.keySet().size()];
	System.arraycopy(names, 0, outNames, 0, names.length);
	java.util.Iterator it = seismogramCache.keySet().iterator();
	int i=names.length;
	while (it.hasNext()) {
	    outNames[i] = (String)it.next();
	    logger.debug("outNames "+outNames[i]);
			 i++;
	} // end of while (it.hasNext())
	
        return outNames;
    }

    /**
     * Gets the seismogram for the given name, Null if it cannot be found.
     *
     * @param name a <code>String</code> name
     * @return a <code>LocalSeismogramImpl</code>
     */
    public LocalSeismogramImpl getSeismogram(String name) {
        if (seismogramCache.containsKey(name)) {
            return (LocalSeismogramImpl)seismogramCache.get(name);
        } // end of if (seismogramCache.containsKey(name))
	
        String urlString = "NONE";
        NodeList nList = 
            evalNodeList(config, "SacSeismogram[name="+dquote+name+dquote+"]");
        if (nList != null && nList.getLength() != 0) {
            try {
                Node n = nList.item(0); 
                if (n instanceof Element) {
                    Element e = (Element)n;
                    urlString = e.getAttribute("xlink:href");
                    if (urlString == null || urlString == "") {
                        throw new MalformedURLException(name+" does not have an xlink:href attribute");			 
                    } // end of if (urlString == null || urlString == "")
                    URL sacURL = 
                        new URL(base, urlString);
                    DataInputStream dis = new DataInputStream(new BufferedInputStream(sacURL.openStream())); 
                    SacTimeSeries sac = new SacTimeSeries();
                    sac.read(dis);
                    LocalSeismogramImpl seis = SacToFissures.getSeismogram(sac);
		    
                    NodeList propList = evalNodeList(e, "property");
                    int numDSProps = 0;
                    if (propList != null && propList.getLength() != 0) {
                        numDSProps = nList.getLength();
                    } else {
                        // no properties in dataset
                        numDSProps = 0;
                    } // end of else

                    Property[] props = seis.getProperties();
                    Property[] newProps = 
                        new Property[1+props.length+numDSProps];
                    System.arraycopy(props, 0, newProps, 0, props.length);
                    for (int i=0; i<propList.getLength(); i++) {
                        Element propElement = (Element)propList.item(i);
                        newProps[props.length+i] = 
                            new Property(xpath.eval(propElement, "name/text()").str(),
                                         xpath.eval(propElement, "value/text()").str());
                    } // end of for
                    newProps[newProps.length-1] = new Property(seisNameKey,
                                                               name);
                    seis.setProperties(newProps);

                    if (seis != null) {
                        seismogramCache.put(name, seis);
                    } // end of if (seis != null)
		    
                    return seis;
                }
		
            } catch (MalformedURLException e) {
                logger.error("Couldn't get seismogram "+name, e);
                logger.error(urlString);
            } catch (Exception e) {
                logger.error("Couldn't get seismogram "+name, e);
                logger.error(urlString);
            } // end of try-catch
	    
        }
        return null;
    }

    /**
     * Adds a seismogram.
     *
     * @param seis a <code>LocalSeismogramImpl</code> seismogram
     * @param audit the audit for this seismogram
     */
    public void addSeismogram(LocalSeismogramImpl seis,
                              AuditInfo[] audit) {

        // Note this does not set the xlink, as the seis has not been saved anywhere yet.

        Document doc = config.getOwnerDocument();
        Element sac = doc.createElement("SacSeismogram");

        String name =seis.getProperty(seisNameKey);
        if (name == null || name.length() == 0) {
	    name = edu.iris.Fissures.network.ChannelIdUtil.toStringNoDates(seis.channel_id);
	}
	Element nameE = doc.createElement("name");
	nameE.setNodeValue(name);
	sac.appendChild(nameE);
	
        Property[] props = seis.getProperties();
        Element propE, propNameE, propValueE;
        for (int i=0; i<props.length; i++) {
            if (props[i].name != seisNameKey) {
                propE = doc.createElement("property");
                propNameE = doc.createElement("name");
                propNameE.setNodeValue(props[i].name);
                propValueE = doc.createElement("value");
                propValueE.setNodeValue(props[i].value);
                propE.appendChild(propNameE);
                propE.appendChild(propValueE);
                sac.appendChild(propE);
            }
        }
        config.appendChild(sac);

        seismogramCache.put(name, seis);

	logger.debug("added seis now "+getSeismogramNames().length+" seisnogram names.");
xpath = new CachedXPathAPI();
	logger.debug("2 added seis now "+getSeismogramNames().length+" seisnogram names.");
    }

    /**
     * Adds a reference to a remote seismogram.
     *
     * @param seisURL an <code>URL</code> to the seismogram
     * @param name a <code>String</code> name
     * @param props the properties for this seismogram to be stored in the 
     *    dataset
     * @param parm_ids the Parameter References for this seismogram to be 
     *    stored in the dataset
     * @param audit the audit for thie seismogram
     */
    public void addSeismogramRef(URL seisURL, 
                                 String name, 
                                 Property[] props, 
                                 ParameterRef[] parm_ids,
                                 AuditInfo[] audit) {
        String baseStr = base.toString();
        String seisStr = seisURL.toString();
        if (seisStr.startsWith(baseStr)) {
            // use relative URL
            seisStr = seisStr.substring(baseStr.length());
        } // end of if (seisStr.startsWith(baseStr))
	
        Document doc = config.getOwnerDocument();
        Element sac = doc.createElement("SacSeismogram");
        sac.setAttributeNS(xlinkNS, "xlink:type", "simple");
        sac.setAttributeNS(xlinkNS, "xlink:href", seisStr);

        Element nameE = doc.createElement("name");
        Text text = doc.createTextNode(name);
        nameE.appendChild(text);
        sac.appendChild(nameE);

        Element propE, propNameE, propValueE;
        for (int i=0; i<props.length; i++) {
            if (props[i].name != seisNameKey) {
                propE = doc.createElement("property");
                propNameE = doc.createElement("name");
                text = doc.createTextNode(props[i].name);
                propNameE.appendChild(text);

                propValueE = doc.createElement("value");
                text = doc.createTextNode(props[i].value);
                propValueE.appendChild(text);

                propE.appendChild(propNameE);
                propE.appendChild(propValueE);
                sac.appendChild(propE);
            }
        }
        config.appendChild(sac);
    }

    /**
     * Describe <code>toString</code> method here.
     *
     * @return a <code>String</code> value
     */
    public String toString() {
        return getName();
    }

    /**
     * returns a DOM Element that represents this dataset.
     * @return an <code>Element</code> value
     */
    public Element getElement() {
        return config;
    }

    /**
     * Writes the xml version of this dataset to the output stream.
     *
     * @param out an <code>OutputStream</code> value
     * @exception Exception if an error occurs
     */
    public void write(OutputStream out) throws Exception {
        DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
        org.w3c.dom.Document outNode = docBuilder.newDocument();

        javax.xml.transform.TransformerFactory tfactory = 
            javax.xml.transform.TransformerFactory.newInstance(); 
    
        // This creates a transformer that does a simple identity transform, 
        // and thus can be used for all intents and purposes as a serializer.
        javax.xml.transform.Transformer serializer = tfactory.newTransformer();
    
        java.util.Properties oprops = new java.util.Properties();
        oprops.put("method", "xml");
        oprops.put("indent", "yes");
        oprops.put("xalan:indent-amount", "4");
        serializer.setOutputProperties(oprops);
        serializer.transform(new javax.xml.transform.dom.DOMSource(getElement()), 
                             new javax.xml.transform.stream.StreamResult(out));

    }

    /**
     * Describe <code>getAllAsStrings</code> method here.
     *
     * @param path a <code>String</code> value
     * @return a <code>String[]</code> value
     */
    protected String[] getAllAsStrings(String path) {
        NodeList nodes = evalNodeList(config, path);
        String[] out = new String[nodes.getLength()];
        for (int i=0; i<out.length; i++) {
            out[i] = nodes.item(i).getNodeValue();
        } // end of for (int i=0; i++; i<out.length)
        return out;
    }

    /**
     * Describe <code>evalNodeList</code> method here.
     *
     * @param context a <code>Node</code> value
     * @param path a <code>String</code> value
     * @return a <code>NodeList</code> value
     */
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

    /**
     * Describe <code>evalElement</code> method here.
     *
     * @param context a <code>Node</code> value
     * @param path a <code>String</code> value
     * @return an <code>Element</code> value
     */
    protected Element evalElement(Node context, String path) {
        NodeList nList = evalNodeList(context, path);
        if (nList != null && nList.getLength() != 0) {
            return (Element)nList.item(0);
        }
        logger.error("Couldn't get NodeList "+path);
        return null;
    }

    /**
     * Describe <code>evalString</code> method here.
     *
     * @param context a <code>Node</code> value
     * @param path a <code>String</code> value
     * @return a <code>String</code> value
     */
    protected String evalString(Node context, String path) {
        try {
            return xpath.eval(config, path).str();
        } catch (javax.xml.transform.TransformerException e) {
            logger.error("Couldn't get String", e);
        } // end of try-catch
        return null;
    }

    private CachedXPathAPI xpath = new CachedXPathAPI();

    /**
     * Describe variable <code>base</code> here.
     *
     */
    protected URL base;

    /**
     * Describe variable <code>config</code> here.
     *
     */
    protected Element config;

    /**
     * Describe variable <code>docBuilder</code> here.
     *
     */
    protected DocumentBuilder docBuilder;

    /**
     * Describe variable <code>dataSetCache</code> here.
     *
     */
    protected HashMap dataSetCache = new HashMap();

    /**
     * Describe variable <code>seismogramCache</code> here.
     *
     */
    protected HashMap seismogramCache = new HashMap();

    /**
     * Describe variable <code>parameterCache</code> here.
     *
     */
    protected HashMap parameterCache = new HashMap();

    private static final String dquote = ""+'"';
    private static final String xlinkNS = "http://www.w3.org/1999/xlink";
    private static final String seisNameKey = "Name";

    static Category logger = 
        Category.getInstance(XMLDataSet.class.getName());

    static void testDataSet(DataSet dataset, String indent) {
        indent = indent+"  ";
        String[] names = dataset.getSeismogramNames();
        System.out.println(indent+" has "+names.length+" seismograms.");
        for (int num=0; num<names.length; num++) {
            System.out.println(indent+" Seismogram name="+names[num]);
            LocalSeismogramImpl seis = dataset.getSeismogram(names[num]);
            System.out.println(seis.getNumPoints());

        }
        names = dataset.getDataSetNames();
        System.out.println(indent+" has "+names.length+" datasets.");
        for (int num=0; num<names.length; num++) {
            System.out.println(indent+" Dataset name="+names[num]);
            testDataSet(dataset.getDataSet(names[num]), indent);
        }
    }

    /**
     * Describe <code>main</code> method here.
     *
     * @param args a <code>String[]</code> value
     */
    public static void main (String[] args) {
        try {
            BasicConfigurator.configure();

            System.out.println("Starting..");
            File file = new File(args[0]);
            URL base = file.toURL();

            XMLDataSet dataset = load(base);

            String[] names = dataset.getDataSetNames();
            for (int i=0; i<names.length; i++) {
                FileOutputStream out = new FileOutputStream("test_"+names[i]);
                XMLDataSet sub = (XMLDataSet)dataset.getDataSet(names[i]);
                sub.write(out);
                out.flush();
                out.close();
            } // end of for (int i=0; i<names.length; i++)
	    

            // testDataSet(dataset, " ");

        } catch (Exception e) {
            e.printStackTrace();	    
        } // end of try-catch
    } // end of main ()
    
}
