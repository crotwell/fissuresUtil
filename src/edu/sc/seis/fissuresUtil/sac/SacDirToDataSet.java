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
 * @version $Id: SacDirToDataSet.java 2093 2002-07-09 20:52:59Z crotwell $
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
	String userName = System.getProperty("user.name");
	URL dirURL = base;
	System.out.println(" dirURL is "+dirURL.toString());
	try {
	    dirURL = new URL(dirURL, directory.getName()+"/");
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
	    AuditInfo[] audit = new AuditInfo[1];
	    audit[0] = new AuditInfo(userName,
				     "Added parameter "+key);
	    try {
		dataset.addParameter(key,new URL(dirURL,
						(String)paramRefs.get(key)).toString(),
				     audit);
		
	    } catch (MalformedURLException e) {
		//can't happen?
		e.printStackTrace();
		System.err.println("Caught exception on parameterRef "
				   +key+", continuing...");
	    } // end of try-catch
	    
	} // end of while (it.hasNext())
	
	    
	File[] sacFiles = directory.listFiles();
	for (int i=0; i<sacFiles.length; i++) {
	    if (excludes.contains(sacFiles[i].getName())) {
		continue;
	    } // end of if (excludes.contains(sacFiles[i].getName()))
	    if (paramRefs.containsValue(sacFiles[i].getName())) {
		continue;
	    } // end of if (excludes.contains(sacFiles[i].getName()))

	    try {
            SacTimeSeries sac = new SacTimeSeries();
		sac.read(sacFiles[i].getCanonicalPath());
		AuditInfo[] audit = new AuditInfo[1];
		audit[0] = new AuditInfo(userName+" via SacDirToDataSet",
					 "seismogram loaded from "+sacFiles[i].getCanonicalPath());
		URL seisURL = new URL(dirURL, sacFiles[i].getName());
        //		System.out.println(" the seisURL is "+seisURL.toString());
        //		DataInputStream dis = new DataInputStream(new BufferedInputStream(seisURL.openStream())); 
        //		SacTimeSeries sac = new SacTimeSeries();
		//sac.read(dis);
		edu.iris.Fissures.seismogramDC.LocalSeismogramImpl seis = SacToFissures.getSeismogram(sac);

        edu.sc.seis.fissuresUtil.cache.CacheEvent event = 
            SacToFissures.getEvent(sac);
        if (event != null && dataset.getParameter(EVENT) == null) {
            // add event
            AuditInfo[] eventAudit = new AuditInfo[1];
            eventAudit[0] = new AuditInfo(System.getProperty("user.name"),
                                          "event loaded from sac file.");
            dataset.addParameter( EVENT, event, eventAudit);
        } // end of if (event != null)
        
        Channel channel = 
            SacToFissures.getChannel(sac);
        String channelParamName = 
            CHANNEL+ChannelIdUtil.toString(seis.channel_id);
        if (channel != null && 
            dataset.getParameter(channelParamName) == null) {
            // add event
            AuditInfo[] chanAudit = new AuditInfo[1];
            chanAudit[0] = new AuditInfo(System.getProperty("user.name"),
                                          "channel loaded from sac file.");
            dataset.addParameter(channelParamName, channel, chanAudit);
        }
        

        String seisName = sacFiles[i].getName();
        if (seisName.endsWith(".SAC")) {
            seisName = seisName.substring(seisName.length()-4);
        } // end of if (seisName.endsWith(".SAC"))
        
		dataset.addSeismogramRef(seis, seisURL, 
					 seisName, 
					 new Property[0], 
					 new ParameterRef[0],
					 audit);

	    } catch (Exception e) {
		e.printStackTrace();
		System.err.println("Caught exception on "
				   +sacFiles[i].getName()+", continuing...");
	    } // end of try-catch
	} // end of for (int i=0; i<sacFiles.length; i++)

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

    URL base;
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
		dsName = args[i+1];
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
		System.err.println("Not a directory: "+args[0]);
	    } // end of else
	} catch (Exception e) {
	    e.printStackTrace();
	} // end of try-catch
	
		
    } // end of main ()
    
}// SacDirToDataSet
