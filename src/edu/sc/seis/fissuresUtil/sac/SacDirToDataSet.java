package edu.sc.seis.fissuresUtil.sac;

import java.io.*;
import java.net.*;
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
 * @version $Id: SacDirToDataSet.java 1711 2002-05-28 13:33:10Z crotwell $
 */

public class SacDirToDataSet {
    public SacDirToDataSet (File directory, String dsName){
	this.directory = directory;	
	this.dsName = dsName;
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

    public static void main (String[] args) {
	if (args.length != 2) {
	    System.err.println("Usage: java edu.sc.seis.fissuresUtil.sac.SacDirToDataSet directoryPath datasetname");
	    return;
	} // end of if (args.length != 2)
	
	try {
	    File f = new File(args[0]);
	    if (f.isDirectory()) {
		SacDirToDataSet sdir = new SacDirToDataSet(f, args[1]);
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
