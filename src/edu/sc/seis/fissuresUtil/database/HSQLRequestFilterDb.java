package edu.sc.seis.fissuresUtil.database;

import edu.sc.seis.fissuresUtil.sac.*;
import edu.sc.seis.fissuresUtil.cache.*;

import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.model.*;

import java.sql.*;
import java.util.*;
import java.io.*;

import org.hsqldb.*;
import org.apache.log4j.*;

/**
 * HSQLRequestFilterDb.java
 *
 *
 * Created: Tue Feb  4 11:30:30 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class HSQLRequestFilterDb extends AbstractDb{
    public HSQLRequestFilterDb (DataCenterOperations router){
	this.dataCenterRouter = router;
	create();
    }
    
    public HSQLRequestFilterDb() {
	create();
    }

    public void create() {
        connection = getConnection();
	try {
	  
	    Statement stmt = connection.createStatement();
	    stmt.executeUpdate(" CREATE TABLE requestFilterDB ( "+
			       " id INTEGER IDENTITY PRIMARY KEY, "+
			       " channel_id VARCHAR_IGNORECASE , "+
			       " begin_time TIMESTAMP, "+
			       " end_time TIMESTAMP, "+
			       " access_time TIMESTAMP, "+
			       " fileid int ) ");
	    stmt.executeUpdate(" CREATE TABLE fileInfoDB( "+
			       " fileid INTEGER, "+
			       " filename VARCHAR_IGNORECASE, "+
			       " filesize BIGINT) " );
	    
	} catch(SQLException sqle) {
	    // sqle.printStackTrace();
	}
	
	try {
	    getTotalSizeStmt = connection.prepareStatement(" SELECT sum(filesize) from fileInfoDB ");
							   

	    rfInsertStmt = connection.prepareStatement(" INSERT INTO requestFilterDB "+
						       " ( channel_id, begin_time, "+
						       " end_time, access_time, "+
						       " fileid ) "+
						       " VALUES(?,?,?,?,?) ");

	    fiInsertStmt = connection.prepareStatement(" INSERT INTO fileInfoDB "+
						      " VALUES(?,?, ?) ");
	    rfGetStmt = connection.prepareStatement(" SELECT fileid FROM requestFilterDB "+
						    " WHERE channel_id = ? AND "+
						    " NOT ((begin_time >= ? AND begin_time >= ? ) "+
						    "     OR (end_time <= ? AND end_time <= ?) "+
						    " ) ");
	    //  " (( begin_time <= ? AND "+
// 						    " end_time >= ? ) "+
// 						    " OR ( begin_time <= ? AND end_time <= ? ) "+
// 						    " OR ( begin_time >= ? AND end_time <= ? ) "+
// 						    " OR (begin_time >=? AND end_time >= ? ))");
						   
	    fiGetStmt = connection.prepareStatement(" SELECT filename FROM fileInfoDB "+
						    " WHERE fileid = ? ");

	    maxFileIDStmt = connection.prepareStatement(" SELECT max(fileid) FROM fileInfoDB ");
						       
	    rfGetFileIdStmt = connection.prepareStatement(" SELECT id FROM requestFilterDB "+
							  " WHERE channel_id = ? AND "+
							  " NOT ((begin_time >= ? AND begin_time >= ? ) "+
							  "     OR (end_time <= ? AND end_time <= ?) "+
							  " ) ");
							  
	    rfGetInfoStmt = connection.prepareStatement(" SELECT channel_id, begin_time, "+
							" end_time FROM requestFilterDB "+
							" WHERE id = ? ");

	    availableDataStmt = connection.prepareStatement(" SELECT begin_time, end_time FROM "+
							    " requestFilterDB "+
							    " WHERE channel_id = ? ORDER BY begin_time ");
	    
	} catch(SQLException sqle) {
	     sqle.printStackTrace();
	}
    }

  
    public RequestFilter[] available_data(RequestFilter[] a_filterseq) {
	ArrayList arrayList = new ArrayList();
	for(int counter = 0; counter < a_filterseq.length; counter++) {
	    
	    RequestFilter[] tempArray = available_data(a_filterseq[counter]);
	    for(int subCounter = 0; subCounter < tempArray.length; subCounter++) {
		arrayList.add(tempArray[subCounter]);
	    }
	}
	RequestFilter[] rtnValues = new RequestFilter[arrayList.size()];
	rtnValues = (RequestFilter[]) arrayList.toArray(rtnValues);
	return rtnValues;

    }

    public RequestFilter[] available_data(RequestFilter a_filterseq) {
	ArrayList arrayList = new ArrayList();
	try {
	    availableDataStmt.setString(1, ChannelIdUtil.toString(a_filterseq.channel_id));
	    ResultSet rs = availableDataStmt.executeQuery();
	    while(rs.next()) {
		MicroSecondDate beginDate = new MicroSecondDate(rs.getTimestamp("begin_time"));
		MicroSecondDate endDate = new MicroSecondDate(rs.getTimestamp("end_time"));
		RequestFilter requestFilter = new RequestFilter(a_filterseq.channel_id,
								beginDate.getFissuresTime(),
								endDate.getFissuresTime());
		arrayList.add(requestFilter);
	    }
	} catch(SQLException sqle) {
	    logger.debug("error occured in available_data method"+sqle);
	}

	RequestFilter[] rtnValues = new RequestFilter[arrayList.size()];
	rtnValues =  (RequestFilter[]) arrayList.toArray(rtnValues);
	return rtnValues;
    }


    public void addSeismogram(LocalSeismogramImpl[] seismos) {
	//if(getSeismograms(requestFilters).length == 0) {
	insertFileInfo(seismos);
	    //}
    }

    public void insertRequestFilterInfo(String channel_id,
				    MicroSecondDate begin_date,
				    MicroSecondDate end_date,
				    int fileid) {
	
	try {
	    rfInsertStmt.setString(1, channel_id);
	    //	    MicroSecondDate date = new MicroSecondDate(begin_time);
	    rfInsertStmt.setTimestamp(2, begin_date.getTimestamp());
	    //date = new MicroSecondDate(end_time);
	    rfInsertStmt.setTimestamp(3, end_date.getTimestamp());
	    rfInsertStmt.setTimestamp(4, new MicroSecondDate().getTimestamp());
	    rfInsertStmt.setInt(5, fileid);
	    rfInsertStmt.executeUpdate();
	} catch(SQLException sqle) {

	}
    }

    /**
     * for the name of the seismogram append begin time and
     * end time along with the channel name.
     */
    public void insertFileInfo(LocalSeismogramImpl[] seismograms) {
	HashMap hashMap = new HashMap();
	
	
	for(int counter = 0; counter < seismograms.length; counter++) {
	    
	    LocalSeismogramImpl seis = (LocalSeismogramImpl) seismograms[counter];
	    try {
		
		SacTimeSeries sac = FissuresToSac.getSAC(seis);
		File directory = new File("_temp_test_GEE_");
		if(!directory.exists()) {
		    directory.mkdirs();
		}
		int id = UniqueNumberGenerator.getUniqueIdentifier();
		File sacDirectory = new File(directory,
					     PREFIX+id);
		sac.write(sacDirectory);
		long fileLength = sacDirectory.length();
		int fileid = getMaxFileID();
		fiInsertStmt.setInt(1, fileid);
		fiInsertStmt.setString(2,
				       "_temp_test_GEE_/"+PREFIX+id );
		fiInsertStmt.setLong(3, fileLength);
		fiInsertStmt.executeUpdate();
		insertRequestFilterInfo(ChannelIdUtil.toString(seis.getChannelID()),
					seis.getBeginTime(),
					seis.getEndTime(),
					fileid
					);
				       
	    } catch(Exception e) {
		e.printStackTrace();
	    }
	    //now build the sac file.
	}//end of for loop
	
    }

    public long getTotalSize() {
	try {
	    ResultSet rs = getTotalSizeStmt.executeQuery();
	    if(rs.next()) {
		return rs.getLong(1);
	    }
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return -1;
    }

    private int getMaxFileID() {
	
	try {
	    ResultSet rs =  maxFileIDStmt.executeQuery();
	    if(rs.next()) {
		return rs.getInt("fileid") + 1;
	    }
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return 0;
    }
	

    public int[] getFileIds(RequestFilter[] requestFilters) {
	ArrayList arrayList = new ArrayList();
	
	for(int counter = 0; counter < requestFilters.length; counter++) {
	    String channel_id = ChannelIdUtil.toString(requestFilters[counter].channel_id);
	    MicroSecondDate beginDate = new MicroSecondDate(requestFilters[counter].start_time);
	    MicroSecondDate endDate = new MicroSecondDate(requestFilters[counter].end_time);
	    int[] ids = getFileIds(channel_id,
				   beginDate,
				   endDate);
	    for(int subCounter = 0; subCounter < ids.length; subCounter++){
		arrayList.add(new Integer(ids[subCounter]));
	    }
	}
	
	
	Integer[] rtnValues = new Integer[arrayList.size()];
	rtnValues = (Integer[]) arrayList.toArray(rtnValues);

	int[] intValues = new int[rtnValues.length];
	for(int counter = 0; counter < rtnValues.length; counter++) {
	    intValues[counter] = rtnValues[counter].intValue();
	}
	//System.out.println("The length of the fileids returned is "+intValues.length);
	//System.out.println("***************************************************************");
	return intValues;
    }

    public int[] getFileIds(String channel_id,
			    MicroSecondDate beginDate,
			    MicroSecondDate endDate) {
	ArrayList arrayList = new ArrayList();
	try {
	    //System.out.println("In hsqlrfdb start_time is "+beginDate);
	    //System.out.println("In hsqlrdb end_time is "+endDate);
	    //System.out.println("begin Timestamp is "+beginDate.getTimestamp());
	    //System.out.println("end Timestamp is "+endDate.getTimestamp());
	    rfGetStmt.setString(1, channel_id);
	    rfGetStmt.setTimestamp(2, beginDate.getTimestamp());
	    rfGetStmt.setTimestamp(3, endDate.getTimestamp());
	    rfGetStmt.setTimestamp(4, beginDate.getTimestamp());
	    rfGetStmt.setTimestamp(5, endDate.getTimestamp());
	    
	   
	  
	    ResultSet rs = rfGetStmt.executeQuery();
	    while(rs.next()) {
		arrayList.add(new Integer(Integer.parseInt(rs.getString("fileid"))));
	    }
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	Integer[] rtnValues = new Integer[arrayList.size()];
	rtnValues = (Integer[]) arrayList.toArray(rtnValues);

	int[] intValues = new int[rtnValues.length];
	for(int counter = 0; counter < rtnValues.length; counter++) {
	    intValues[counter] = rtnValues[counter].intValue();
	}
	return intValues;
    }

    public String[] getFilePaths(int[] fileids) {

	ArrayList arrayList = new ArrayList();
	try {
	    for(int counter = 0; counter < fileids.length; counter++) {
		fiGetStmt.setInt(1, fileids[counter]);
		ResultSet rs = fiGetStmt.executeQuery();
		while(rs.next()) {
		    arrayList.add(rs.getString("filename"));
 		}
	    }
	    
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	String[] rtnValues = new String[arrayList.size()];
	rtnValues = (String[]) arrayList.toArray(rtnValues);
	return rtnValues;
    }

    public LocalSeismogramImpl[] getSeismograms(RequestFilter[] requestFilters) {

	ArrayList arrayList = new ArrayList();
	try {
	    int[] ids = getFileIds(requestFilters);
	    String[] fileNames = getFilePaths(ids);
	    
	    //here use the SAC Processor
	    //to get the seismograms a
	    for(int counter = 0; counter < fileNames.length; counter++) {
		//System.out.println("ALREADY in the database file name is "+fileNames[counter]);
		FileInputStream fis = new FileInputStream(fileNames[counter]);
		DataInputStream dis = new DataInputStream(new BufferedInputStream(fis));
		SacTimeSeries sac = new SacTimeSeries();
		sac.read(dis);
		LocalSeismogramImpl seis;
		seis = SacToFissures.getSeismogram(sac);
		arrayList.add(seis);
	    }

	} catch(Exception sqle) {
	    sqle.printStackTrace();
	}
	LocalSeismogramImpl[] rtnValues = new LocalSeismogramImpl[arrayList.size()];
	rtnValues = (LocalSeismogramImpl[]) arrayList.toArray(rtnValues);
	return rtnValues;
    }

    public String getFileIds(ChannelId channel_id,
			     MicroSecondDate beginDate,
			     MicroSecondDate endDate) {
	try {
	    rfGetFileIdStmt.setString(1, ChannelIdUtil.toString(channel_id));
	    rfGetFileIdStmt.setTimestamp(2, beginDate.getTimestamp());
	    rfGetFileIdStmt.setTimestamp(3, endDate.getTimestamp());
	    rfGetFileIdStmt.setTimestamp(4, beginDate.getTimestamp());
	    rfGetFileIdStmt.setTimestamp(5, endDate.getTimestamp());
	   
	    //System.out.println("The channelids is "+ChannelIdUtil.toString(channel_id));
	    //System.out.println("The begindate is "+beginDate);
	    //System.out.println("The endDateis "+endDate);
	    ResultSet rs = rfGetFileIdStmt.executeQuery();
	    if(rs.next()) {
	        Integer rtn = new Integer(rs.getInt("id"));
		//System.out.println("the file id returned is "+rtn.toString());
		return rtn.toString();
	    }
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	//System.out.println("The file id returned is null ");
	return null;
    }

    public LocalSeismogram getSeismogram(String fileids) {

	int value = Integer.parseInt(fileids);
	try {
	    rfGetInfoStmt.setInt(1, value);
	    ResultSet rs = rfGetInfoStmt.executeQuery();
	    if(rs.next()) {
		String channel_id = rs.getString("channel_id");
		MicroSecondDate beginDate = new MicroSecondDate(rs.getTimestamp("begin_time"));
		MicroSecondDate endDate = new MicroSecondDate(rs.getTimestamp("end_time"));
		int[] ids = getFileIds(channel_id,
				       beginDate,
				       endDate);
		String[] fileNames = getFilePaths(ids);
		if(fileNames.length == 0) return null;
		FileInputStream fis = new FileInputStream(fileNames[0]);
		DataInputStream dis = new DataInputStream(new BufferedInputStream(fis));
		SacTimeSeries sac = new SacTimeSeries();
		sac.read(dis);
		LocalSeismogramImpl seis;
		seis = SacToFissures.getSeismogram(sac);
		return seis;
	    }
	} catch(Exception sqle) {
	    sqle.printStackTrace();
	}
	return null;
    }

    private PreparedStatement getTotalSizeStmt;

    private PreparedStatement rfInsertStmt;

    private PreparedStatement fiInsertStmt;

    private PreparedStatement rfGetStmt;
    
    private PreparedStatement fiGetStmt;

    private PreparedStatement maxFileIDStmt;

    private DataCenterOperations dataCenterRouter;

    private PreparedStatement rfGetFileIdStmt;
    
    private PreparedStatement rfGetInfoStmt;

    private PreparedStatement availableDataStmt;

    private final static String PREFIX = "edu.sc.seis.fissuresUtil.database.seismogram";

    static Category logger =
        Category.getInstance(HSQLRequestFilterDb.class.getName());
    
        
}// HSQLRequestFilterDb

