package edu.sc.seis.fissuresUtil.database;

import java.sql.SQLException;
import edu.iris.Fissures.AuditInfo;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

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
    public DataSetCache (String directoryName, String databaseName){
        this.directoryName = directoryName;
        this.databaseName = databaseName;
    }

    public void addSeismogram(LocalSeismogramImpl seis,
                  String name,
                  AuditInfo[] auditInfo) throws SQLException {

    String fileids = DBDataCenter.getDataCenter(directoryName, databaseName).getFileIds(seis.getChannelID(),
                        seis.getBeginTime(),
                        seis.getEndTime());
    SeisInfoDb.getSeisInfoDb(directoryName, databaseName).insert(name,
                      fileids);
    }


    public LocalSeismogramImpl getSeismogram(String name)
        throws SQLException, java.io.IOException, edu.iris.Fissures.FissuresException
 {
    //System.out.println("The name of the seismogram queried is "+name);
    String fileids = SeisInfoDb.getSeisInfoDb(directoryName, databaseName).getFileIds(name);
    if(fileids == null) return null;
    return (LocalSeismogramImpl)DBDataCenter.getDataCenter(directoryName, databaseName).getSeismogram(fileids);

    }

    String directoryName;

    String databaseName;

}// DataSetCache
