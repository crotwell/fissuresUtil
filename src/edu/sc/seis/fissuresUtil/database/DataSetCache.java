package edu.sc.seis.fissuresUtil.database;

import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.*;

/**
 * DataSetCache.java
 *
 *
 * Created: Mon Feb 10 11:13:23 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class DataSetCache {
    public DataSetCache (){
	
    }

    public void addSeismogram(LocalSeismogramImpl seis,
			      String name,
			      AuditInfo[] auditInfo) {
	
	String fileids = DBDataCenter.getDataCenter().getFileIds(seis.getChannelID(),
						seis.getBeginTime(),
						seis.getEndTime());
	SeisInfoDb.getSeisInfoDb().insert(name, 
					  fileids);
    }


    public LocalSeismogramImpl getSeismogram(String name) {
	//System.out.println("The name of the seismogram queried is "+name);
	String fileids = SeisInfoDb.getSeisInfoDb().getFileIds(name);
	if(fileids == null) return null;
	System.out.println("The value of the fileids is "+fileids);
	return (LocalSeismogramImpl)DBDataCenter.getDataCenter().getSeismogram(fileids);

    }
    
}// DataSetCache
