package edu.sc.seis.fissuresUtil.sac;

import java.io.*;
import java.net.*;
import java.util.*;
import edu.iris.Fissures.*;
import edu.sc.seis.fissuresUtil.xml.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.IfParameterMgr.*;
import javax.xml.parsers.*;

/**
 * SacDirToDataSet.java
 *
 *
 * Created: Tue Feb 26 11:43:08 2002
 *
 * @author <a href="mailto:crotwell@pooh">Philip Crotwell</a>
 * @version $Id: SacDirToDataSet.java 2566 2002-09-03 19:15:10Z telukutl $
 */

public class SacDirToDataSet implements StdDataSetParamNames {
    public SacDirToDataSet (URL base,
			    File directory, 
			    String dsName, 
			    List excludes, 
			    Map paramRefs){
	this.base = base;
	this.directory = directory;	
	this.dsName = dsName;
	this.excludes = excludes;
	this.paramRefs = paramRefs;
    }

    void process() throws ParserConfigurationException {
	DocumentBuilderFactory factory
	    = DocumentBuilderFactory.newInstance();
	DocumentBuilder docBuilder = factory.newDocumentBuilder();
	dirURL = base;
	System.out.println(" dirURL is "+dirURL.toString());
	System.out.println(" directory name is "+directory.getName());
	try {
	    dirURL = new URL(dirURL.toString()+"/"+directory.getName()+"/");
	    System.out.println("updated dirURL is "+dirURL.toString());
	} catch (MalformedURLException e) {
	    e.printStackTrace();
	    return;	    
	} // end of try-catch
	
	dataset 
	    = new XMLDataSet(docBuilder,
			     dirURL,
			    "genid"+Math.round(Math.random()*Integer.MAX_VALUE),
			     dsName, 
			     userName); 

	Iterator it = paramRefs.keySet().iterator();
	while (it.hasNext()) {
	    String key = (String)it.next();
	    //loadParameterRef(key, (String)paramRefs.get(key));
	    
	} // end of while (it.hasNext())
	
	    
	File[] files = directory.listFiles();
	for (int i=0; i<files.length; i++) {
	    try {
            String filename = files[i].getName();
            // maybe an image?
            if (filename.endsWith(".gif") ||
                filename.endsWith(".GIF") ||
                filename.endsWith(".png") ||
                filename.endsWith(".PNG") ||
                filename.endsWith(".jpeg") ||
                filename.endsWith(".JPEG") ||
                filename.endsWith(".jpg") ||
                filename.endsWith(".JPG") ) {
                String name = filename.substring(0, filename.lastIndexOf('.'));
                loadParameterRef(filename, filename);
            } else {
                // try as a sac file
                loadSacFile(files[i]);
            } // end of else

	    } catch (Exception e) {
		e.printStackTrace();
		System.err.println("Caught exception on "
				   +files[i].getName()+", continuing...");
	    } // end of try-catch
	} // end of for (int i=0; i<sacFiles.length; i++)

    }

    void loadParameterRef(String paramName, String paramFile) {
	    AuditInfo[] audit = new AuditInfo[1];
	    audit[0] = new AuditInfo(userName,
				     "Added parameter "+paramName+" for "+paramFile);
	    try {
		if(dataset.getParameter(paramName) != null) return;
		dataset.addParameterRef(new URL("file:"+paramName),
					paramName,
					dataset.getParameter(paramName),
					audit);
				// 	new URL(dirURL,
// 						paramFile).toString(),
// 					audit);
		
	    } catch (MalformedURLException e) {
		//can't happen?
		e.printStackTrace();
		System.err.println("Caught exception on parameterRef "
				   +paramName+", continuing...");
	    } // end of try-catch
    }

    void loadSacFile(File sacFile) throws IOException, FissuresException {
	    if (excludes.contains(sacFile.getName())) {
		return;
	    } // end of if (excludes.contains(sacFile.getName()))
	    if (paramRefs.containsValue(sacFile.getName())) {
		return;
	    } // end of if (excludes.contains(sacFile.getName()))

            SacTimeSeries sac = new SacTimeSeries();
		sac.read(sacFile.getCanonicalPath());
		AuditInfo[] audit = new AuditInfo[1];
		audit[0] = new AuditInfo(userName+" via SacDirToDataSet",
					 "seismogram loaded from "+sacFile.getCanonicalPath());
		URL seisURL = new URL(dirURL, sacFile.getName());
        //		System.out.println(" the seisURL is "+seisURL.toString());
        //		DataInputStream dis = new DataInputStream(new BufferedInputStream(seisURL.openStream())); 
        //		SacTimeSeries sac = new SacTimeSeries();
		//sac.read(dis);
		edu.iris.Fissures.seismogramDC.LocalSeismogramImpl seis = SacToFissures.getSeismogram(sac);
		
		
		System.out.println("The PATH is "+sacFile.getParent());
		
		edu.sc.seis.fissuresUtil.cache.CacheEvent event = 
		    SacToFissures.getEvent(sac);

		  
		    if (event != null && dataset.getParameter(EVENT) == null) {
			String eventName = event.get_attributes().name;
				
			String eName = eventName.replace(' ', '_');
			// add event
			File outFile = new File(sacFile.getParent(), eName);
			OutputStream fos = new BufferedOutputStream(new FileOutputStream(outFile));
		    
			AuditInfo[] eventAudit = new AuditInfo[1];
			eventAudit[0] = new AuditInfo(System.getProperty("user.name"),
						      "event loaded from sac file.");
			XMLParameter.write(fos, event);
			fos.close();
			dataset.addParameterRef( new URL("file:"+eName), EVENT, event,eventAudit);
			edu.sc.seis.fissuresUtil.cache.CacheEvent cacheEvent = (edu.sc.seis.fissuresUtil.cache.CacheEvent)((XMLDataSet)dataset).getParameter(EVENT);
			if(cacheEvent == null){
			    System.out.println("CACHE EVENT IS NULL");
			    System.exit(0);
			}
			else System.out.println("CACHE EVENT IS NOT NULL");
		    } // end of if (event != null)
        
        Channel channel = 
            SacToFissures.getChannel(sac);
	String channelParamName = 
            CHANNEL+ChannelIdUtil.toString(seis.channel_id);
	

        if (channel != null && 
            dataset.getParameter(channelParamName) == null) {


	    File outFile = new File(sacFile.getParent(), ChannelIdUtil.toString(seis.channel_id));
	    BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(outFile));

            // add event
            AuditInfo[] chanAudit = new AuditInfo[1];
            chanAudit[0] = new AuditInfo(System.getProperty("user.name"),
                                          "channel loaded from sac file.");
	    XMLParameter.write(fos, channel);
	    fos.close();
            dataset.addParameterRef(new URL("file:"+ChannelIdUtil.toString(seis.channel_id)), 
				    channelParamName, 
				    channel,
				    chanAudit);

	}
        

        String seisName = sacFile.getName();
        if (seisName.endsWith(".SAC")) {
	    seisName = seisName.substring(0,seisName.length()-4);
        } // end of if (seisName.endsWith(".SAC"))
        seis.setName(seisName);
        
		dataset.addSeismogramRef(seis, seisURL, 
					 seisName, 
					 new Property[0], 
					 new ParameterRef[0],
					 audit);

    }

    void save() {
	try {
	 
	  
	    File outFile = new File(directory, dsName+".dsml");
	    OutputStream fos = new BufferedOutputStream(
			       new FileOutputStream(outFile));
	    dataset.write(fos);
	    fos.close();
	} catch(Exception ex) {

	    System.out.println("EXCEPTION CAUGHT WHILE trying to save dataset"
			       +ex.toString());
	    ex.printStackTrace();
	}
    }

	String userName = System.getProperty("user.name");
    URL base;
	URL dirURL;
    File directory;
    String dsName;
    XMLDataSet dataset;
    List excludes;
    Map paramRefs;

    public static void main (String[] args) {
	if (args.length < 4) {
	    System.err.println("Usage: java edu.sc.seis.fissuresUtil.sac.SacDirToDataSet -base url -dir directoryPath -name datasetname [-exclude file] [-paramRef name file]");
	    return;
	} // end of if (args.length != 2)
	String dirName = null;
	URL base = null;
	String baseStr = "";
	String dsName = "default dataset name";
	LinkedList excludes = new LinkedList();
	HashMap params = new HashMap();
	int i=0;
	while (i<args.length) {
	    System.out.println(i+" "+args[i]);
	    if (args[i].equals("-dir")) {
		dirName = args[i+1];
		i+=2;
	    } else  if (args[i].equals("-name")) {
		String tmp = args[i+1];
        if (tmp.endsWith("/") || 
            tmp.endsWith("\\") || 
            tmp.endsWith(":") || 
            tmp.endsWith(".") ) {
            tmp = tmp.substring(0, tmp.length()-1);
        } // end of if (dsName.endsWith('/'))
        tmp = tmp.replace(' ','_');
        if (tmp.length() > 0) {
            dsName = tmp;
        } // end of if (tmp.length() > 0)
		i+=2;
	    } else  if (args[i].equals("-base")) {
		baseStr = args[i+1];
		System.out.println("The baseStr is "+baseStr);
		i+=2;
	    } else  if (args[i].equals("-exclude")) {
		excludes.add(args[i+1]);
		i+=2;
	    } else  if (args[i].equals("-paramRef")) {
		params.put(args[i+1], args[i+2]);
		i+=3;
	    } else {
		System.out.println("Don't understand "+args[i++]);
	    }	    
	} // end of for (int i=0; i<args.length; i++)
	

	try {
	    base = new URL(baseStr);
	    System.out.println("base is "+base.toString());
	    File f = new File(dirName);
	    if (dirName != null && f.isDirectory()) {
		SacDirToDataSet sdir = new SacDirToDataSet(base, f, dsName, excludes, params);
		sdir.process();
		sdir.save();
	    } else {
		System.err.println("Not a directory: "+args[1]);
	    } // end of else
	} catch (Exception e) {
	    e.printStackTrace();
	} // end of try-catch
	
		
    } // end of main ()
    
}// SacDirToDataSet
