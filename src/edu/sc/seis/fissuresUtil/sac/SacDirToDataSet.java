package edu.sc.seis.fissuresUtil.sac;

import java.io.*;
import edu.iris.Fissures.*;
import edu.iris.Fissures.IfDataSetMgr.*;
import edu.iris.Fissures.dataSetMgr.*;

/**
 * SacDirToDataSet.java
 *
 *
 * Created: Tue Feb 26 11:43:08 2002
 *
 * @author <a href="mailto:crotwell@pooh">Philip Crotwell</a>
 * @version $Id: SacDirToDataSet.java 1709 2002-05-27 21:00:47Z crotwell $
 */

public class SacDirToDataSet {
    public SacDirToDataSet (File directory, String dsName){
	this.directory = directory;	
	this.dsName = dsName;

	org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init();
	any = orb.create_any();

    }

    void process() {
	String userName = System.getProperty("user.name");
	DataSetAttr attr = new DataSetAttr("no id", 
					   dsName, 
					   userName, 
					   new edu.iris.Fissures.IfParameterMgr.ParameterRef[0]);
	dataset = new LocalDataSetImpl(attr);
	File[] sacFiles = directory.listFiles();
	for (int i=0; i<sacFiles.length; i++) {
	    try {
		SacTimeSeries sac = new SacTimeSeries();
		sac.read(sacFiles[i].getCanonicalPath());
		AuditInfo[] audit = new AuditInfo[1];
		audit[0] = new AuditInfo(userName+" via SacDirToDataSet",
					 "seismogram loaded from sacFiles[i].getCanonicalPath()");
		dataset.addLocalSeismogram(SacToFissures.getSeismogram(sac), 
					   audit);
	    } catch (Exception e) {
		System.err.println("Caught exception on "
				   +sacFiles[i].getName()+", continuing...");
	    } // end of try-catch
	} // end of for (int i=0; i<sacFiles.length; i++)
    }

    void save() {
	try {
	    LocalDataSetHelper.insert(any, dataset);
	    FileOutputStream fos = new FileOutputStream(dsName+".ds");
	    ObjectOutputStream oos = new ObjectOutputStream(fos);
	    oos.writeObject(any.extract_Value());
	    oos.close();
	    fos.close();
	} catch(Exception ex) {

	    System.out.println("EXCEPTION CAUGHT WHILE trying to save dataset"
			       +ex.toString());
	    ex.printStackTrace();
	}
    }

    LocalDataSetImpl dataset;
    File directory;
    String dsName;

    public static void main (String[] args) {
	if (args.length != 2) {
	    System.err.println("Usage: java edu.sc.seis.fissuresUtil.sac.SacDirToDataSet directoryPath datasetname");
	    return;
	} // end of if (args.length != 2)
	
	File f = new File(args[0]);
	if (f.isDirectory()) {
	    SacDirToDataSet sdir = new SacDirToDataSet(f, args[1]);
	    sdir.process();
	    sdir.save();
	} else {
	    System.err.println("Not a directory: "+args[0]);
	} // end of else
		
    } // end of main ()
    
        org.omg.CORBA.Any any;

}// SacDirToDataSet
