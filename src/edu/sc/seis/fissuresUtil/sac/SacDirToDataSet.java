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
 * @version $Id: SacDirToDataSet.java 1725 2002-05-29 13:52:11Z crotwell $
 */

public class SacDirToDataSet {
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
	dataset 
	    = new XMLDataSet(docBuilder,
			     base,
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
		URL dirURL = new URL(base, directory.getName()+"/");
		dataset.addParameterRef(new URL(dirURL,
						(String)paramRefs.get(key)),
					key,
					audit);
		
	    } catch (MalformedURLException e) {
		//can't happen?
		e.printStackTrace();
		System.err.println("Caught exception on parameterRef "
				   +key+", continuing...");
	    } // end of try-catch
	    
	} // end of while (it.hasNext())
	
	    
	File[] sacFiles = directory.listFiles();
	//SacTimeSeries sac = new SacTimeSeries();
	for (int i=0; i<sacFiles.length; i++) {
	    if (excludes.contains(sacFiles[i].getName())) {
		continue;
	    } // end of if (excludes.contains(sacFiles[i].getName()))
	    if (paramRefs.containsValue(sacFiles[i].getName())) {
		continue;
	    } // end of if (excludes.contains(sacFiles[i].getName()))

	    try {
		//sac.read(sacFiles[i].getCanonicalPath());
		AuditInfo[] audit = new AuditInfo[1];
		audit[0] = new AuditInfo(userName+" via SacDirToDataSet",
					 "seismogram loaded from sacFiles[i].getCanonicalPath()");
		URL seisURL = new URL(base, sacFiles[i].getName());
		dataset.addSeismogramRef(seisURL, 
					 sacFiles[i].getName(), 
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
