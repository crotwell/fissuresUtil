package edu.sc.seis.fissuresUtil.xml;

import edu.sc.seis.fissuresUtil.chooser.ChannelProxy; //needed only for testing
import edu.sc.seis.fissuresUtil.database.DataSetCache;
import edu.sc.seis.fissuresUtil.sac.*;
import edu.iris.Fissures.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.IfParameterMgr.ParameterRef;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.apache.xpath.*;
import org.apache.xpath.objects.*;
import java.lang.ref.*;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.*;
import org.apache.log4j.*;

/**
 * Access to a dataset stored as an XML file.
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version $Id: XMLDataSet.java 3489 2003-03-18 19:35:44Z telukutl $
 */
/**
 * Describe class <code>XMLDataSet</code> here.
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
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
        prefixResolver = new org.apache.xml.utils.PrefixResolverDefault(config);
    }

    /**
     * Load a xml dataset from a URL.
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
		//logger.debug(" ***********************************************DATASET IS SET FROM THE URL SO IT IS NOT NULL");
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
    /**
     * Creates a new <code>XMLDataSet</code> instance.
     *
     * @param docBuilder a <code>DocumentBuilder</code> value
     * @param base an <code>URL</code> value
     * @param id a <code>String</code> value
     * @param name a <code>String</code> value
     * @param owner a <code>String</code> value
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
	//logger.debug("In the method getId");
        return evalString(config, "@datasetid");
    }

    /**
     * Gets the base URL that other URLs in this dataset are relative to.
     *
     * @return the base <code>URL</code>
     */
    /**
     * Describe <code>getBase</code> method here.
     *
     * @return an <code>URL</code> value
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
	if (parameterNameCache == null) {
	    parameterNameCache = cacheParameterNames(); 
	} // end of if (parameterNameCache == null) 
	return parameterNameCache; 
    } 
 
    /**
     * Describe <code>cacheParameterNames</code> method here.
     *
     * @return a <code>String[]</code> value
     */
    public String[] cacheParameterNames() { 

        String[] params = getAllAsStrings("parameter/name/text()");
	ArrayList referenceNames = new ArrayList();
	NodeList  paramRefsList = evalNodeList(config, "parameterRef");
	if(paramRefsList != null && paramRefsList.getLength() != 0) {
	    for(int counter = 0; counter < paramRefsList.getLength(); counter++) {

		Node n = paramRefsList.item(counter);
		if(n instanceof Element) {
		    String str = ((Element)n).getAttribute("name");
		    referenceNames.add(str);
		}
	    }
	}
	String[] paramRefs = new String[referenceNames.size()];
	paramRefs = (String[])referenceNames.toArray(paramRefs);
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
	   
	    Object obj = parameterCache.get(name);
	    if(obj instanceof SoftReference) {
		SoftReference softReference  = (SoftReference)obj;
		if(softReference.get() != null) return softReference.get();
		else parameterCache.remove(name);
	    } else return obj;
	} // end of if (parameterCache.containsKey(name))
	
        NodeList nList = evalNodeList(config, 
                                      "parameter[name/text()="+
                                      dquote+name+dquote+"]");
        if (nList != null && nList.getLength() != 0) {
	    //logger.debug("getting the parameter "+name);
            Node n = nList.item(0); 
            if (n instanceof Element) {
		Object r = XMLParameter.getParameter((Element)n);
		parameterCache.put(name, new SoftReference(r));
		return r;
            }
        } else {
	    logger.debug("THE NODE LIST IS NULL for parameter "+name);
	}
	
        // not a parameter, try parameterRef
        nList = evalNodeList(config, 
                             "parameterRef");//[text()="+dquote+name+dquote+"]");
        if (nList != null && nList.getLength() != 0) {
	    for(int counter = 0 ; counter < nList.getLength() ; counter++) {
		Node n = nList.item(counter);
		if (n instanceof Element) {
		    if(!((Element)n).getAttribute("name").equals(name)) continue;
		    SimpleXLink sl = new SimpleXLink(docBuilder, (Element)n, getBase());

		    try {
			Element e = sl.retrieve();
		       	//parameterCache.put(name, e);
			Object obj = XMLParameter.getParameter(e); 
			parameterCache.put(name, new SoftReference(obj));
			return obj;
		    } catch (Exception e) {
			logger.error("can't get paramterRef for "+name, e);
		    } // end of try-catch
		}
	    }
        }
	logger.warn("can't find paramter for "+name);

        //can't find that name???
        return null;
    }

    /**
     * Adds a new parameter. Currently objects that are not DOM Elements are
     * stored in memory, but cannot be premanantly saved in the xml file.
     *
     * @param name a <code>String</code> name for this parameter
     * @param value an <code>Object</code> value
     * @param audit the audit related to this paramter
     */
    public void addParameter(String name, 
                             Object value,
                             AuditInfo[] audit) {
	parameterNameCache = null;
	parameterCache.put(name, value);
        //if (value instanceof Element) {
	    Element parameter = 
		config.getOwnerDocument().createElement("parameter");
	    XMLParameter.insert(parameter, name, value);	   //  Object cacheEvent = XMLParameter.getParameter(parameter);
// 	    if(cacheEvent instanceof edu.sc.seis.fissuresUtil.cache.CacheEvent) {
// 		logger.debug("Instance of CaCHE EVENT -----------");			
// 	    } else {
// 		logger.debug(" NOt an Instance of CACHE EVENT ------");
// 	    }
            config.appendChild(parameter);
        //} else {
        //    logger.warn("Parameter is only stored in memory.");
        //} // end of else
	
    }

    /**
     * Adds a reference to a remote parameter.
     *
     * @param paramURL an <code>URL</code> value
     * @param name a <code>String</code> value
     * @param audit an <code>AuditInfo[]</code> value
     */
    /**
     * Describe <code>addParameterRef</code> method here.
     *
     * @param paramURL an <code>URL</code> value
     * @param name a <code>String</code> value
     * @param object an <code>Object</code> value
     * @param audit an <code>AuditInfo[]</code> value
     */
    public void addParameterRef(URL paramURL, 
                                String name,
				Object object,
                                AuditInfo[] audit) {
	parameterNameCache = null;
	String baseStr = base.toString();

	String paramStr = paramURL.toString();
        if (paramStr.startsWith(baseStr)) {
            // use relative URL
            paramStr = paramStr.substring(baseStr.length());
        } // end of if (paramStr.startsWith(baseStr))
	//logger.debug("inside the addParameterRef");
        Document doc = config.getOwnerDocument();
        Element param = doc.createElement("parameterRef");
/*	param.setAttribute("name", name);
        param.setAttributeNS(xlinkNS, "xlink:type", "simple");
        param.setAttributeNS(xlinkNS, "xlink:href", paramStr);
	param.setAttribute("type","text/xml");
	param.setAttribute("objectType", "object");
	//        Text text = doc.createTextNode(name);
	// param.appendChild(text);

        config.appendChild(param);*/
	XMLParameter.insertParameterRef(param,
					name,
					paramStr,
					object);
	config.appendChild(param);
    }

    /**
     * Gets the Ids for all child datasets of this dataset.
     *
     * @return a <code>String[]</code> id
     */
    public String[] getDataSetIds() {
        if (dataSetIdCache == null) {
            String[] internal = getAllAsStrings("*/@datasetid"); 
            String[] external = getDataSetRefIds();
            for (int i=0; i<external.length; i++) {
                //logger.debug("External Dataset Id cache :'"+external[i]+"'");
            } // end of for (int i=0; i<tmp.length; i++)
            String[] tmp = new String[internal.length+external.length];
            System.arraycopy(internal, 0, 
                             tmp, 0, 
                             internal.length);
            System.arraycopy(external, 0, 
                             tmp, internal.length, 
                             external.length);
            for (int i=0; i<tmp.length; i++) {
                //logger.debug("Dataset Id cache :'"+tmp[i]+"'");
            } // end of for (int i=0; i<tmp.length; i++)
            
            dataSetIdCache = tmp;
        } // end of if (dataSetIdCache == null) 
        return dataSetIdCache;  
    }

    String[] getDataSetRefIds() {
        String[] xlinktmps = getAllAsStrings("datasetRef");
        String xlinkNS= "http://www.w3.org/1999/xlink";
        NodeList nodes = evalNodeList(config, "datasetRef");
        if (nodes == null) {
            return new String[0];
        } // end of if (nodes == null)
	//logger.debug("the length of the nodes is "+nodes.getLength());

        String[] xlinks = new String[nodes.getLength()];
        String[] ids = new String[nodes.getLength()];
        for (int i=0; i<nodes.getLength(); i++) {
            Node n = nodes.item(i);
            NamedNodeMap map = n.getAttributes();
            for (int j=0; j<map.getLength(); j++) {
                //logger.debug("attribute: "+map.item(j).getLocalName());
            } // end of for (int j=0; j<map.getLength(); j++)
            
            try {
                if (n instanceof Element) {
                    Element e = (Element)n;
                    String href = e.getAttribute("xlink:href");
		    SimpleXLink sl = new SimpleXLink(docBuilder, e, getBase());
                    Element referredElement = sl.retrieve();
                    //logger.debug("simpleLink element is"+referredElement.toString());
                    XMLDataSet ds = new XMLDataSet(docBuilder, 
                                                   new URL(getBase(), href),
                                                   referredElement);
                     dataSetCache.put(ds.getId(), ds);
                    ids[i] = ds.getId();
                } else {
                    ids[i] = null;
                } // end of else
                
            } catch (Exception e) {
                logger.error("can't get dataset for "+xlinks[i], e);
                ids[i] = null;
            } // end of try-catch
        } // end of for (int i=0; i<xlinks.length; i++)
        //logger.debug("got "+xlinks.length+" datasetRef ids from "+xlinktmps.length+" datasetRefs");
        return ids;
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
    /**
     * Describe <code>getDataSet</code> method here.
     *
     * @param name a <code>String</code> value
     * @return a <code>DataSet</code> value
     */
    public DataSet getDataSet(String name) {
        String[] ids = getDataSetIds();
        for (int i=0; i<ids.length; i++) {
            DataSet ds = getDataSetById(ids[i]);
	    //logger.debug("++++++++ name is "+name +" the datasetID name is "+ds.getName());
	    //logger.debug("returning as found in CACHE "+((XMLDataSet)ds).getBase().toString());
            if (name.equals(ds.getName())) {
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
    /**
     * Describe <code>addDataSet</code> method here.
     *
     * @param dataset an <code>edu.sc.seis.fissuresUtil.xml.DataSet</code> value
     * @param audit an <code>AuditInfo[]</code> value
     */
    public void addDataSet(edu.sc.seis.fissuresUtil.xml.DataSet dataset,
                           AuditInfo[] audit) {
	dataSetIdCache = null;
	if (dataset instanceof XMLDataSet) {
	    XMLDataSet xds = (XMLDataSet)dataset;
            Element element = xds.getElement();
            if (element.getOwnerDocument().equals(config.getOwnerDocument())) {
                config.appendChild(element);
                //logger.debug("dataset append "+config.getChildNodes().getLength());
            } else {
                // not from the same document, must clone
                Node copyNode = config.getOwnerDocument().importNode(element, 
                                                                     true);
                config.appendChild(copyNode);
                //logger.debug("dataset import "+config.getChildNodes().getLength());
                NodeList nl = config.getChildNodes();
                for (int i=0; i<nl.getLength(); i++) {
                    //logger.debug("node "+nl.item(i).getLocalName());
                } // end of for (int i=0; i<nl.getLenght(); i++)
		
            } // end of else
        } else {
            logger.warn("Attempt to add non-XML dataset");
        } // end of else
	
    }

    /**
     * Describe <code>addDataSetRef</code> method here.
     *
     * @param datasetURL an <code>URL</code> value
     * @param audit an <code>AuditInfo[]</code> value
     */
    public void addDataSetRef(URL datasetURL, 
			      AuditInfo[] audit) {
    	dataSetIdCache = null;

        String baseStr = base.toString();
        String datasetStr = datasetURL.toString();
        if (datasetStr.startsWith(baseStr)) {
            // use relative URL
           datasetStr = datasetStr.substring(baseStr.length());
        } // end of if 

        Document doc = config.getOwnerDocument();
        Element datasetElement = doc.createElement("datasetRef");
        datasetElement.setAttributeNS(xlinkNS, "xlink:type", "simple");
        datasetElement.setAttributeNS(xlinkNS, "xlink:href", datasetStr);
        config.appendChild(datasetElement);
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
    /**
     * Describe <code>createChildDataSet</code> method here.
     *
     * @param id a <code>String</code> value
     * @param name a <code>String</code> value
     * @param owner a <code>String</code> value
     * @param audit an <code>AuditInfo[]</code> value
     * @return a <code>DataSet</code> value
     */
    public DataSet createChildDataSet(String id, String name, String owner,
                                      AuditInfo[] audit) {
	dataSetIdCache = null;
	name = getUniqueName(getDataSetNames(), name);
        XMLDataSet dataset = new XMLDataSet(docBuilder, base, id, name, owner);
        //addDataSet(dataset, audit);
        return dataset;
    }

    /**
     * Gets the dataset with the given id.
     *
     * @param id a <code>String</code> id
     * @return a <code>DataSet</code>
     */
    /**
     * Describe <code>getDataSetById</code> method here.
     *
     * @param id a <code>String</code> value
     * @return a <code>DataSet</code> value
     */
    public DataSet getDataSetById(String id) {
        if (dataSetCache.containsKey(id)) {
	    //logger.debug("returning as found in CACHE "+getBase().toString());
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

	//try to get the dataset from the datasetRefs.
	//added by srinivasa
	NodeList nodes = evalNodeList(config, "datasetRef");
        if (nodes == null) {
	    //logger.debug("returning null as the nodes is null");
            return null;
        } // end of if (nodes == null)

	//logger.debug("*********** Before For the length is  "+nodes.getLength());
             
        for (int i=0; i<nodes.getLength(); i++) {
            Node n = nodes.item(i);
            NamedNodeMap map = n.getAttributes();
	    try {
		//logger.debug("*********** Before Checking for If");
                if (n instanceof Element) {
                    Element e = (Element)n;
                    String href = e.getAttribute("xlink:href");
		    
		    SimpleXLink sl = new SimpleXLink(docBuilder, e, getBase());
                    Element referredElement = sl.retrieve();
                    //logger.debug("simpleLink element is"+referredElement.toString());
                    XMLDataSet ds = new XMLDataSet(docBuilder, 
                                                   new URL(getBase(), href),
                                                   referredElement);
		    if(id.equals(ds.getId())) {
		       	dataSetCache.put(ds.getId(), ds);
			return ds;
		    }
                } else {
                    return null;
                } // end of else
                
            } catch (Exception e) {
		e.printStackTrace();
		return null;
            } // end of try-catch
        } // end of for (int i=0; i<xlinks.length; i++)

        // not an embedded dataset, try datasetRef
        // getIds adds to cache
        String[] ids = getDataSetRefIds();
        if (dataSetCache.containsKey(id)) {
            return (DataSet)dataSetCache.get(id);
        }

        logger.error("Couldn't get datasetRef :"+id);

        // can't find it
        return null;
    }

    /**
     * Gets the names of the seismograms in this dataset.
     *
     * @return the names.
     */
    public String[] getSeismogramNames() {
	if (seismogramNameCache == null) {
	    seismogramNameCache = cacheSeismogramNames();
	} // end of if (seismogramNameCache == null)
         
	return seismogramNameCache;
    }
 
    /**
     * Describe <code>cacheSeismogramNames</code> method here.
     *
     * @return a <code>String[]</code> value
     */
    protected String[] cacheSeismogramNames() {
	String[] names = getAllAsStrings("localSeismogram/seismogramAttr/property[name="+dquote+"Name"+dquote+
					 "]"+"/value/text()");
        //String[] names = getAllAsStrings("SacSeismogram/name/text()");
	//logger.debug("found "+names.length+" names in xml");
	//logger.debug("cache has "+seismogramCache.keySet().size());
	String outNames[] = 
	    new String[names.length];//+seismogramCache.keySet().size()];
	System.arraycopy(names, 0, outNames, 0, names.length);
	/*java.util.Iterator it = seismogramCache.keySet().iterator();
	int i=names.length;
	while (it.hasNext()) {
	    outNames[i] = (String)it.next();
	    logger.debug("outNames "+outNames[i]);
			 i++;
			 } // end of while (it.hasNext())*/
	String[] noNames = getNoNameSeismogramNames();
   	String[] rtnValues = new String[noNames.length + outNames.length];
	//logger.debug("The length of noNames is "+noNames.length);
	//logger.debug("The length of nameed siesmograms is "+outNames.length);
	System.arraycopy(outNames, 0, rtnValues, 0, outNames.length);
	if(outNames.length != 0) {
	    System.arraycopy(noNames, 0, rtnValues, rtnValues.length - 1, noNames.length);
	}
	else {
	    System.arraycopy(noNames, 0, rtnValues, 0, noNames.length);
	}
        return rtnValues;
    }
    
    /**
     * Assigns names to the seismograms without names based on the channelId and returns the array of assigned names
     *
     * @return a <code>String[]</code> value
     */
    /**
     * Describe <code>getNoNameSeismogramNames</code> method here.
     *
     * @return a <code>String[]</code> value
     */
    public String[] getNoNameSeismogramNames() {

	NodeList nList;
	
	nList = evalNodeList(config, "localSeismogram/seismogramAttr/property");
	if(nList == null || (nList != null && nList.getLength() == 0)) {
	    nList = evalNodeList(config, "localSeismogram/seismogramAttr");
	    //logger.debug("ONLY SEIS ATTR EXISTS");
	} else {
	   nList  = evalNodeList(config, "localSeismogram/seismogramAttr/property[name!="+dquote+"Name"+dquote+
					 "]"+"/..");
	   //logger.debug("PROPERTY TAG EXISTS");
	}

	String[] rtn;
	if(nList != null) {
	    rtn = new String[nList.getLength()];
	} else {
	    rtn = new String[0];
	}
	if(nList != null &&  nList.getLength() != 0) {

	    int size = nList.getLength();
	  //   for( int counter = 0; counter < size; counter++) {
		
// 		Node n = nList.item(counter);
// 		if(n instanceof Element) {

// 		    Element subElement = (Element)n;
// 		    String name = getAsString(subElement, "channel_id/network_id/network_code/text()");
// 		    name = name + getAsString(subElement, "channel_id/station_code/text()"); 
// 		    name = name + getAsString(subElement, "channel_id/site_code/text()"); 
// 		    name = name + getAsString(subElement, "channel_id/channel_code/text()"); 
// 		    rtn[counter] = name;
// 		}
// 	    }
	   
	    for(int counter = 0; counter < size; counter++) {

		Node n = nList.item(counter);
		if(n instanceof Element) {
		    NodeList channelIdNode = evalNodeList((Element)n, "channel_id");
		    Element subElement = (Element)channelIdNode.item(0);
		    //   SeismogramAttr seismogramAttr = XMLSeismogramAttr.getSeismogramAttr(subElement);
		    ChannelId channel_id = XMLChannelId.getChannelId(subElement);
		    String name = edu.iris.Fissures.network.ChannelIdUtil.toStringNoDates(channel_id);
		    rtn[counter] = name;
		}
	    }
	    
	}
	//logger.debug("The length of the no Name SeismogramNames is "+rtn.length);
	return rtn;
    }


    /**
     * Describe <code>getSeismogramAttrs</code> method here.
     *
     * @return a <code>SeismogramAttr[]</code> value
     */
    public SeismogramAttr[] getSeismogramAttrs() {

	NodeList nList;
	nList = evalNodeList(config, "localSeismogram/seismogramAttr");
	SeismogramAttr[] seismogramAttrs = new SeismogramAttr[0];
	if(nList != null && nList.getLength() != 0) {
	    seismogramAttrs = new SeismogramAttr[nList.getLength()];
	    for(int counter = 0; counter < nList.getLength(); counter++) {
		seismogramAttrs[counter] = XMLSeismogramAttr.getSeismogramAttr((Element) nList.item(counter));
	    }//end of for.
	}//end of if.
	return seismogramAttrs;
    }

    /**
     * Describe <code>getChannelIds</code> method here.
     *
     * @return a <code>ChannelId[]</code> value
     */
    public ChannelId[] getChannelIds() {
	Date startTime = Calendar.getInstance().getTime();
	String[] paramNames = getParameterNames();
	ArrayList arrayList = new ArrayList();
	
	for(int counter = 0; counter < paramNames.length; counter++) {
	    if(paramNames[counter].startsWith(StdDataSetParamNames.CHANNEL)) {
		Channel channel = (Channel)getParameter(paramNames[counter]);
		arrayList.add(channel.get_id());	    
	    }
	}
	ChannelId[] channelIds = new ChannelId[arrayList.size()];
	channelIds = (ChannelId[]) arrayList.toArray(channelIds);
	Date endTime = Calendar.getInstance().getTime();
	logger.debug("The time Taken for getting the channelIDs is ------------------------------------------------------->>>>"+(endTime.getTime() - startTime.getTime()));
	return channelIds;
	
	/*SeismogramAttr[] seismogramAttrs = getSeismogramAttrs();

	for(int counter = 0; counter < seismogramAttrs.length; counter++) {
	    channelIds[counter] = ((SeismogramAttrImpl)seismogramAttrs[counter]).getChannelID();
	    }*/
		}

    /**
     * Describe <code>getAsString</code> method here.
     *
     * @param base an <code>Element</code> value
     * @param path a <code>String</code> value
     * @return a <code>String</code> value
     */
    public String getAsString(Element base, String path) {
	
	NodeList nodes = evalNodeList(base, path);
	String out = new String();

	if(nodes != null && nodes.getLength() != 0) {
	    out = nodes.item(0).getNodeValue();
	}
	return out;
    }



    /**
     * Gets the seismogram for the given name, Null if it cannot be found.
     *
     * @param name a <code>String</code> name
     * @return a <code>LocalSeismogramImpl</code>
     */
    public LocalSeismogramImpl getSeismogram(String name) {
       //  if (seismogramCache.containsKey(name)) {
// 	    logger.debug("getting the seismogram from the cache");
// 	    Object obj = seismogramCache.get(name);
// 	    if(obj instanceof SoftReference) {
// 		SoftReference softReference = (SoftReference)obj;
// 		LocalSeismogramImpl seis = (LocalSeismogramImpl)softReference.get();
// 		if(seis != null) {
// 		    logger.debug("**********NO NULL WHILE GETTING FROM RHT CACHE");
// 		    return seis;
// 		}
// 		else {
// 		    logger.debug("********** GARBAGE COLLECTED SO SEISMOGRAM NOT IN MEWMOERY");
// 		    seismogramCache.remove(name);
// 		}
		
// 	    } else return (LocalSeismogramImpl)obj;
//         } // end of if (seismogramCache.containsKey(name))
	
	//logger.debug("The name of the data set is "+getName());
	//logger.debug("The name of the seismogram is "+name);
	LocalSeismogramImpl seisd = datasetCache.getSeismogram(name);
	if(seisd != null) return seisd;
	
        String urlString = "NONE";
        NodeList nList = 
            evalNodeList(config, "localSeismogram/seismogramAttr/property[name="+dquote+"Name"+dquote+
			 "]"+"[value="+dquote+name+dquote+"]"+"/../../data");
	if(nList == null || (nList != null && nList.getLength() == 0)) {
	    
	    nList =  getNoNameSeismogram(name);
	}
        if (nList != null && nList.getLength() != 0) {
            try {
                Node n = nList.item(0); 
                if (n instanceof Element) {
                    Element e = (Element)n;
		    //logger.debug("**********************The name of the element is "+e.getTagName());
                    urlString = e.getAttribute("xlink:href");
                    if (urlString == null || urlString == "") {
                        throw new MalformedURLException(name+" does not have an xlink:href attribute");			 
                    } // end of if (urlString == null || urlString == "")
		    //logger.debug("IN GET SEISMOGRAM   The base str is "+base.toString());
		    

		    //get the Seismogram Attributes from the xml .. only the data must 
		    // must be obtained fromt the SAC.
		    NodeList seisAttrNode = XMLUtil.evalNodeList(e, "../seismogramAttr");
		    SeismogramAttr seisAttr = null;
		    if(seisAttrNode != null && seisAttrNode.getLength() != 0) {
			seisAttr = XMLSeismogramAttr.getSeismogramAttr((Element)seisAttrNode.item(0));
		    }
		       
                    NodeList propList = evalNodeList(e, "property");
                    int numDSProps = 0;
                    if (propList != null && propList.getLength() != 0) {
                        numDSProps = nList.getLength();
                    } else {
                        // no properties in dataset
                        numDSProps = 0;
                    } // end of else

                  //   Property[] props = seis.getProperties();
//                     Property[] newProps = 
//                         new Property[1+props.length+numDSProps];
//                     System.arraycopy(props, 0, newProps, 0, props.length);
//                     for (int i=0; i<propList.getLength(); i++) {
//                         Element propElement = (Element)propList.item(i);
//                         newProps[props.length+i] = 
//                             new Property(xpath.eval(propElement, "name/text()").str(),
//                                          xpath.eval(propElement, "value/text()").str());
//                     } // end of for
//                     newProps[newProps.length-1] = new Property(seisNameKey,
//                                                                name);
//                     seis.setProperties(newProps);

		    URL sacURL = new URL(base, urlString);
		    //logger.debug("The sacUrl is "+sacURL.toString());
                    DataInputStream dis = new DataInputStream(new BufferedInputStream(sacURL.openStream())); 
                    SacTimeSeries sac = new SacTimeSeries();
                    sac.read(dis);
                    LocalSeismogramImpl seis;
		    if (seisAttr != null) {
			seis = SacToFissures.getSeismogram(sac, seisAttr);
		    } else {
			seis = SacToFissures.getSeismogram(sac);
		    } // end of else
		    
                    if (seis != null) {
			//seismogramCache.put(name, new SoftReference(seis));
			datasetCache.addSeismogram(seis, name, new AuditInfo[0]);
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
     * Describe <code>getNoNameSeismogram</code> method here.
     *
     * @param name a <code>String</code> value
     * @return a <code>NodeList</code> value
     */
    public NodeList getNoNameSeismogram(String name) {
	/*String[] names = getNoNameSeismogramNames();
	////logger.debug("the Length of the no Name seismograms when actually getting the seeismogram "+names.length);
	
	boolean found = false;
	for(int counter = 0; counter < names.length; counter++) {
	    if(names[counter].equals(name)) { found = true; break;}
	}
	if(found) {
	    //logger.debug("found the equivalent name");
	    */
	    Node n = getNoNameSeismogramNode(name);

	    if( n == null) return null;
	    else {
		if(n instanceof Element) {
		    Element subElement = (Element)n;
		    NodeList nodeList =   evalNodeList(subElement, "../data");
		    return nodeList;
		} else return null;
	    }
	    /*
	} else {
	    return null;
	    }*/
	
    }

    /**
     * Describe <code>getNoNameSeismogramNode</code> method here.
     *
     * @param paramName a <code>String</code> value
     * @return a <code>Node</code> value
     */
    public Node getNoNameSeismogramNode(String paramName) {

	//	NodeList nList = evalNodeList(config, "localSeismogram/seismogramAttr/property[name!="+dquote+"Name"+dquote+
	//		      "]"+"/../");
	NodeList nList;
	
	nList = evalNodeList(config, "localSeismogram/seismogramAttr/property");
	if(nList == null || (nList != null && nList.getLength() == 0)) {
	    nList = evalNodeList(config, "localSeismogram/seismogramAttr");
	} else {
	   nList  = evalNodeList(config, "localSeismogram/seismogramAttr/property[name!="+dquote+"Name"+dquote+
					 "]"+"/../");
	}
		if(nList != null &&  nList.getLength() != 0) {

	    int size = nList.getLength();
	 //    for( int counter = 0; counter < size; counter++) {
		
// 		Node n = nList.item(counter);
// 		if(n instanceof Element) {

// 		    Element subElement = (Element)n;
// 		    String name = getAsString(subElement, "channel_id/network_id/network_code/text()");
// 		    name = name + getAsString(subElement, "channel_id/station_code/text()"); 
// 		    name = name + getAsString(subElement, "channel_id/site_code/text()"); 
// 		    name = name + getAsString(subElement, "channel_id/channel_code/text()"); 
// 		    if(name.equals(paramName)) return n;
// 		}
// 		}
	    /*for(int counter = 0; counter < size; counter++) {

		Node n = nList.item(counter);
		if(n instanceof Element) {
		    Element subElement = (Element) n;
		    SeismogramAttr seismogramAttr = XMLSeismogramAttr.getSeismogramAttr(subElement);
		    ChannelId channel_id = ((SeismogramAttrImpl)seismogramAttr).getChannelID();
		    String name = edu.iris.Fissures.network.ChannelIdUtil.toStringNoDates(channel_id);
		    if(name.equals(paramName)) return n;
		}
		}*/

	     for(int counter = 0; counter < size; counter++) {

		Node n = nList.item(counter);
		if(n instanceof Element) {
		    NodeList channelIdNode = evalNodeList((Element)n, "channel_id");
		    Element subElement = (Element)channelIdNode.item(0);
		    //   SeismogramAttr seismogramAttr = XMLSeismogramAttr.getSeismogramAttr(subElement);
		    ChannelId channel_id = XMLChannelId.getChannelId(subElement);
		    String name = edu.iris.Fissures.network.ChannelIdUtil.toStringNoDates(channel_id);
		    if(name.equals(paramName)) return n;
		}
	    }
	     
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
	
	seismogramNameCache = null;

        // Note this does not set the xlink, as the seis has not been saved anywhere yet.

        Document doc = config.getOwnerDocument();
        Element localSeismogram = doc.createElement("localSeismogram");//doc.createElement("SacSeismogram");
	
        String name =seis.getProperty(seisNameKey);
        if (name == null || name.length() == 0) {
	    name = seis.channel_id.network_id.network_code+"."+
            seis.channel_id.station_code+"."+
            seis.channel_id.channel_code;
        //edu.iris.Fissures.network.ChannelIdUtil.toStringNoDates(seis.channel_id);
	
	}
	name = getUniqueName(getSeismogramNames(), name);
	seis.setName(name);
	Element seismogramAttr = doc.createElement("seismogramAttr");
	XMLSeismogramAttr.insert(seismogramAttr, (LocalSeismogram)seis);
	//localSeismogram.appendChild(seismogramAttr);

// 	Element propertyElement = doc.createElement("property");
// 	propertyElement.appendChild(XMLUtil.createTextElement(doc, "name",
// 							      "Name"));
// 	propertyElement.appendChild(XMLUtil.createTextElement(doc, "value",
// 							      name));
	///seismogramAttr.appendChild(propertyElement);
	localSeismogram.appendChild(seismogramAttr);
	

	
        /*Property[] props = seis.getProperties();
	//logger.debug("the length of the Properties of the seismogram are "+props.length);
        Element propE, propNameE, propValueE;
        for (int i=0; i<props.length; i++) {

            if (props[i] != null && props[i].name != seisNameKey) {
                propE = doc.createElement("property");
                propNameE = doc.createElement("name");
                propNameE.setNodeValue(props[i].name);
                propValueE = doc.createElement("value");
                propValueE.setNodeValue(props[i].value);
                propE.appendChild(propNameE);
                propE.appendChild(propValueE);
                localSeismogram.appendChild(propE);
            }
	    }*/
        config.appendChild(localSeismogram);
	//	seismogramCache.put(name, seis);
	datasetCache.addSeismogram(seis, name, audit);

	//logger.debug("added seis now "+getSeismogramNames().length+" seisnogram names.");
       	seismogramNameCache = null;
    //xpath = new XPathAPI();
	//xpath = new CachedXPathAPI(xpath);
	//logger.debug("2 added seis now "+getSeismogramNames().length+" seisnogram names.");
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
    public void addSeismogramRef(LocalSeismogramImpl seis,
				 URL seisURL, 
                                 String name, 
                                 Property[] props, 
                                 ParameterRef[] parm_ids,
                                 AuditInfo[] audit) {
	seismogramNameCache = null;
        String baseStr = base.toString();
        String seisStr = seisURL.toString();
        if (seisStr.startsWith(baseStr)) {
            // use relative URL
            seisStr = seisStr.substring(baseStr.length());
        } // end of if (seisStr.startsWith(baseStr))
	
        Document doc = config.getOwnerDocument();
	Element localSeismogram = doc.createElement("localSeismogram");
	if(name == null || name.length() == 0) {
	    name =seis.getProperty(seisNameKey);
	}
        if (name == null || name.length() == 0) {
	    name = edu.iris.Fissures.network.ChannelIdUtil.toStringNoDates(seis.channel_id);
	
	}
	name = getUniqueName(getSeismogramNames(), name);
	seis.setName(name);
	Element seismogramAttr = doc.createElement("seismogramAttr");	
	XMLSeismogramAttr.insert(seismogramAttr, (LocalSeismogram)seis);
	//localSeismogram.appendChild(seismogramAttr);
	
// 	Element propertyElement = doc.createElement("property");
// 	propertyElement.appendChild(XMLUtil.createTextElement(doc,
// 							      "name",
// 							      "Name"));
// 	propertyElement.appendChild(XMLUtil.createTextElement(doc,
// 							      "value",
// 							      name));
	//seismogramAttr.appendChild(propertyElement);
	localSeismogram.appendChild(seismogramAttr);	

	Element data = doc.createElement("data");
        data.setAttributeNS(xlinkNS, "xlink:type", "simple");
        data.setAttributeNS(xlinkNS, "xlink:href", seisStr);
	data.setAttribute("seisType", "sac");



	
	//Element nameE = doc.createElement("name");
	// Text text = doc.createTextNode(name);
        //nameE.appendChild(text);
        //localSeismogram.appendChild(nameE);

	localSeismogram.appendChild(data);

	/*  Element propE, propNameE, propValueE;
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
	    }*/
        config.appendChild(localSeismogram);
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
     * Describe <code>getEvent</code> method here.
     *
     * @return an <code>edu.sc.seis.fissuresUtil.cache.CacheEvent</code> value
     */
    public edu.sc.seis.fissuresUtil.cache.CacheEvent getEvent() {
	/*boolean found;
	NodeList paramList = evalNodeList(config, "parameter");
	if(paramList != null && paramList.getLength() != 0) {
	    for(int counter = 0; counter < paramList.getLength(); counter++) {
		Object object = XMLParameter.getParameter((Element)paramList.item(counter));
		if(object instanceof edu.sc.seis.fissuresUtil.cache.CacheEvent){
		    return (edu.sc.seis.fissuresUtil.cache.CacheEvent)object;
		}
	    }
	}
	return null;*/
	return (edu.sc.seis.fissuresUtil.cache.CacheEvent)getParameter(StdDataSetParamNames.EVENT);
    }

    
    /**
     * Describe <code>getChannel</code> method here.
     *
     * @param channelId a <code>ChannelId</code> value
     * @return an <code>edu.iris.Fissures.IfNetwork.Channel</code> value
     */
    public edu.iris.Fissures.IfNetwork.Channel getChannel(ChannelId channelId) {
	//logger.debug("-------- "+StdDataSetParamNames.CHANNEL+ChannelIdUtil.toString(channelId));

	Object obj = getParameter(StdDataSetParamNames.CHANNEL+ChannelIdUtil.toString(channelId));
	
	return (edu.iris.Fissures.IfNetwork.Channel)obj;
    }

    /**
     * Describe <code>getUniqueName</code> method here.
     *
     * @param nameList a <code>String[]</code> value
     * @param name a <code>String</code> value
     * @return a <code>String</code> value
     */
    public String getUniqueName(String[] nameList, String name) {
	int counter = 0;
	for(int i = 0; i < nameList.length; i++) {
		if(nameList[i].indexOf(name) != -1) counter++;
	}
	if(counter == 0) return name;
	return name+"_"+(counter+1);
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
	//        oprops.put("xalan:indent-amount", "4");
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
    /**
     * Describe <code>getAllAsStrings</code> method here.
     *
     * @param path a <code>String</code> value
     * @return a <code>String[]</code> value
     */
    protected String[] getAllAsStrings(String path) {
	//logger.debug("The path that is passed to GetALLASStrings is "+path);
	
        NodeList nodes = evalNodeList(config, path);
        if (nodes == null) {
            return new String[0];
        } // end of if (nodes == null)
        
	String[] out = new String[nodes.getLength()];
	//logger.debug("the length of the nodes is "+nodes.getLength());
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
    /**
     * Describe <code>evalNodeList</code> method here.
     *
     * @param context a <code>Node</code> value
     * @param path a <code>String</code> value
     * @return a <code>NodeList</code> value
     */
    protected NodeList evalNodeList(Node context, String path) {
        try {
            XObject xobj = xpath.eval(context, path, prefixResolver);
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

    public void addDataSetSeismogram(DataSetSeismogram dss, AuditInfo[] audit) {

    }
    
    public DataSetSeismogram getDataSetSeismogram(String name) {
	
	LocalSeismogramImpl seis = getSeismogram(name);
	if(seis != null) {
	    System.out.println("The seismogram is no null");
	} else {
	    System.out.println("The seismogram is not null");

	}
	RequestFilter rf = new RequestFilter(seis.getChannelID(),
					     seis.getBeginTime().getFissuresTime(),
					     seis.getEndTime().getFissuresTime());
	return new DataSetSeismogram(rf, null);
    }

    
    public String[] getDataSetSeismogramNames() {

	return new String[0];
    }
    



    private DataSetCache datasetCache = new DataSetCache();

    private XPathAPI xpath = new XPathAPI();

    private org.apache.xml.utils.PrefixResolver prefixResolver;

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
     * Describe variable <code>dataSetIdCache</code> here.
     *
     */
    protected String[] dataSetIdCache = null;

    /**
     * Describe variable <code>seismogramCache</code> here.
     *
     */
    protected HashMap seismogramCache = new HashMap();

    /**
     * Describe variable <code>seismogramNameCache</code> here.
     *
     */
    protected String[] seismogramNameCache = null;

    /**
     * Describe variable <code>parameterCache</code> here.
     *
     */
    protected HashMap parameterCache = new HashMap();

    /**
     * Describe variable <code>parameterNameCache</code> here.
     *
     */
    protected String[] parameterNameCache = null;

    private static final String dquote = ""+'"';
    private static final String xlinkNS = "http://www.w3.org/1999/xlink";
    private static final String seisNameKey = "Name";

    static Category logger = 
        Category.getInstance(XMLDataSet.class.getName());

    static void testDataSet(DataSet dataset, String indent) {
        indent = indent+"  ";
        String[] names = dataset.getSeismogramNames();
        //logger.debug(indent+" has "+names.length+" seismograms.");
        for (int num=0; num<names.length; num++) {
	    // logger.debug(indent+" Seismogram name="+names[num]);
            LocalSeismogramImpl seis = dataset.getSeismogram(names[num]);
            //logger.debug(seis.getNumPoints());

        }
        names = dataset.getDataSetNames();
        //logger.debug(indent+" has "+names.length+" datasets.");
        for (int num=0; num<names.length; num++) {
	    // logger.debug(indent+" Dataset name="+names[num]);
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

            //logger.debug("Starting..");
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

