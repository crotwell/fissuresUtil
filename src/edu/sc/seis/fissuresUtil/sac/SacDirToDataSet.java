package edu.sc.seis.fissuresUtil.sac;

import java.io.*;
import java.net.*;
import java.util.*;
import edu.iris.Fissures.*;
import edu.sc.seis.fissuresUtil.xml.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.IfParameterMgr.*;
import javax.xml.parsers.*;

/**
 * SacDirToDataSet.java
 *
 *
 * Created: Tue Feb 26 11:43:08 2002
 *
 * @author <a href="mailto:crotwell@pooh">Philip Crotwell</a>
 * @version $Id: SacDirToDataSet.java 1712 2002-05-28 15:00:57Z crotwell $
 */

public class SacDirToDataSet {
    public SacDirToDataSet (File directory, 
			    String dsName, 
			    List excludes, 
			    Map paramRefs){
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
	XMLDataSet dataset 
	    = new XMLDataSet(docBuilder,
			    "genid"+Math.rint(Math.random()*Integer.MAX_VALUE),
			     dsName, 
			     userName); 
	File[] sacFiles = directory.listFiles();
	//SacTimeSeries sac = new SacTimeSeries();
	for (int i=0; i<sacFiles.length; i++) {
	    if (excludes.contains(sacFiles[i].getName())) {
		continue;
	    } // end of if (excludes.contains(sacFiles[i].getName()))
	    if (paramRefs.containsValue(sacFiles[i].getName())) {
		continue;
	    } // end of if (excludes.contains(sacFiles[i].getName()))

	    Iterator it = paramRefs.keySet().iterator();
	    while (it.hasNext()) {
		String key = (String)it.next();
		AuditInfo[] audit = new AuditInfo[1];
		audit[0] = new AuditInfo(userName,
					 "Added parameter "+key);
		try {
		    dataset.addParameterRef(new URL((String)paramRefs.get(key)),
					    key,
					    audit);
		     
		} catch (MalformedURLException e) {
		    //can't happen?
		    System.err.println("Caught exception on parameterRef "
				       +key+", continuing...");
		} // end of try-catch
		
	    } // end of while (it.hasNext())
	    
	    
	    try {
		//sac.read(sacFiles[i].getCanonicalPath());
		AuditInfo[] audit = new AuditInfo[1];
		audit[0] = new AuditInfo(userName+" via SacDirToDataSet",
					 "seismogram loaded from sacFiles[i].getCanonicalPath()");
		URL seisURL = new URL(sacFiles[i].getName());
		dataset.addSeismogramRef(seisURL, 
					 sacFiles[i].getName(), 
					 new Property[0], 
					 new ParameterRef[0],
					 audit);

	    } catch (Exception e) {
		System.err.println("Caught exception on "
				   +sacFiles[i].getName()+", continuing...");
	    } // end of try-catch
	} // end of for (int i=0; i<sacFiles.length; i++)

    }

    void save() {
	try {
	    OutputStream fos = new BufferedOutputStream(
			       new FileOutputStream(dsName+".dsml"));
	    root.write(fos);
	    fos.close();
	} catch(Exception ex) {

	    System.out.println("EXCEPTION CAUGHT WHILE trying to save dataset"
			       +ex.toString());
	    ex.printStackTrace();
	}
    }

    File directory;
    String dsName;
    XMLDataSet root;
    List excludes;
    Map paramRefs;

    public static void main (String[] args) {
	if (args.length < 4) {
	    System.err.println("Usage: java edu.sc.seis.fissuresUtil.sac.SacDirToDataSet -dir directoryPath -name datasetname [-exclude file] [-paramRef name file]");
	    return;
	} // end of if (args.length != 2)
	String dirName = null;
	String dsName = "default dataset name";
	LinkedList excludes = new LinkedList();
	HashMap params = new HashMap();
	int i=0;
	while (i<args.length) {
	    if (args[i].equals("-dir")) {
		dirName = args[i+1];
		i+=2;
	    } else  if (args[i].equals("-name")) {
		dsName = args[i+1];
		i+=2;
	    } else  if (args[i].equals("-exclude")) {
		excludes.add(args[i+1]);
		i+=2;
	    } else  if (args[i].equals("-paramRef")) {
		params.put(args[i+1], args[i+2]);
		i+=3;
	    }	    
	} // end of for (int i=0; i<args.length; i++)
	

	try {
	    File f = new File(dirName);
	    if (dirName != null && f.isDirectory()) {
		SacDirToDataSet sdir = new SacDirToDataSet(f, dsName, excludes, params);
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
