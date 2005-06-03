 /*
 * Created on May 6, 2005
 *
 */
package edu.sc.seis.fissuresUtil.database.seismogram;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.database.JDBCTable;
import edu.sc.seis.fissuresUtil.database.JDBCTime;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.network.JDBCChannel;
import edu.sc.seis.fissuresUtil.database.util.TableSetup;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.mseed.MiniSeedRead;
import edu.sc.seis.fissuresUtil.sac.SacTimeSeries;
import edu.sc.seis.fissuresUtil.sac.SacToFissures;
import edu.sc.seis.fissuresUtil.xml.SeismogramFileTypes;
import edu.sc.seis.fissuresUtil.xml.URLDataSetSeismogram;

/**
 * @author fenner
 *
 *Used to enter SQL database information.
 *
 */
public class JDBCSeismogramFiles extends JDBCTable {

	public JDBCSeismogramFiles(Connection conn) throws SQLException {
		super("seisfilereference", conn);
		timeTable = new JDBCTime(conn);
		TableSetup.setup(this, "edu/sc/seis/fissuresUtil/database/seismogram/seisfilereference.vm");
		chanTable = new JDBCChannel(conn);
	}
	
	public void saveSeismogramToDatabase(ChannelId chan, LocalSeismogramImpl seis, String fileLocation, SeismogramFileTypes filetype) throws SQLException{
		//Get absolute file path out of the file path given
	    File sacFile = new File(fileLocation);
	    String absoluteFilePath = sacFile.getPath();
	    
		insert.setInt(1, chanTable.put(chan));
		insert.setInt(2, timeTable.put(seis.getBeginTime().getFissuresTime()));
		insert.setInt(3, timeTable.put(seis.getEndTime().getFissuresTime()));
		insert.setString(4, absoluteFilePath);
        insert.setInt(5, filetype.getIntValue());
		insert.executeUpdate();
	}
	
	public RequestFilter[] findMatchingSeismograms(RequestFilter[] requestArray) throws SQLException{
	    RequestFilter[] matchingSeismogramsResultArray;
	    List matchingSeismogramsResultList = queryDatabaseForSeismograms(requestArray, false);
		return (RequestFilter[])matchingSeismogramsResultList.toArray(new RequestFilter[matchingSeismogramsResultList.size()]);
	}
	
	public LocalSeismogram[] getMatchingSeismograms(RequestFilter[] requestArray) throws SQLException{
		LocalSeismogram[] gettingSeismogramsResultArray;
		List matchingSeismogramsResultList = queryDatabaseForSeismograms(requestArray, true);
		return (LocalSeismogram[])matchingSeismogramsResultList.toArray(new LocalSeismogram[matchingSeismogramsResultList.size()]);
	}
	
	public List queryDatabaseForSeismograms(RequestFilter[] requestArray, boolean returnSeismograms) throws SQLException{
	    List matchingSeismogramsResultList = new ArrayList();
	    
	    //Loop used to compair data in requestArray with the database and save results in a ResultSet.
		for(int i = 0; i < requestArray.length; i++){
			
			//Retrieve channel ID, begin time, and end time from the request
		    //and place the times into a time table while buffering/feathering/widening
		    //the query by one second on each end.
		    int chanId;
		    try{
		        chanId = chanTable.getDBId(requestArray[i].channel_id);
		    }
		    catch (NotFound e){
		        logger.debug("Can not find channel ID in database.");
		        return new ArrayList(0);
		    }
		    MicroSecondDate adjustedBeginTime = new MicroSecondDate(requestArray[i].start_time).subtract(ONE_SECOND);
			MicroSecondDate adjustedEndTime = new MicroSecondDate(requestArray[i].end_time).add(ONE_SECOND);
      
			//Populate databaseResults with all of the matching seismograms from the database.
			select.setInt(1, chanId);
			select.setTimestamp(2, adjustedEndTime.getTimestamp());
			select.setTimestamp(3, adjustedBeginTime.getTimestamp());			
			databaseResults = select.executeQuery();
			
			if(returnSeismograms){
			    try{
				    while(databaseResults.next()){
				        File sacFile = new File(databaseResults.getString(4));
				        SeismogramFileTypes filetype = SeismogramFileTypes.fromInt(databaseResults.getInt("filetype"));
				        URLDataSetSeismogram urlSeis = new URLDataSetSeismogram(sacFile.toURL(), filetype);
				        LocalSeismogramImpl[] result = urlSeis.getSeismograms();
				        for(int j = 0; j < result.length; j++) {
				            matchingSeismogramsResultList.add(result[i]);
				        }
					}
			    }
			    catch (Exception e){
			        GlobalExceptionHandler.handle("Problem occured while returning seismograms from the database.", e);
			    }   
			}
			
			else{
			    try{
				    while(databaseResults.next()){
						RequestFilter resultSetToRequestFilter = new RequestFilter();
					    resultSetToRequestFilter.channel_id = chanTable.getId(databaseResults.getInt(1));
					    resultSetToRequestFilter.start_time = timeTable.get(databaseResults.getInt(2));
					    resultSetToRequestFilter.end_time = timeTable.get(databaseResults.getInt(3));
					    matchingSeismogramsResultList.add(resultSetToRequestFilter);
					}
			    }
			    catch (Exception e){
			        GlobalExceptionHandler.handle("Problem occured while querying the database for seismograms.");
			    }
			}
		}
		return matchingSeismogramsResultList;
	}
	
	private static final TimeInterval ONE_SECOND = new TimeInterval(1, UnitImpl.SECOND);
	
	private static final Logger logger = Logger.getLogger(JDBCSeismogramFiles.class);
	
	private PreparedStatement insert;
	
	private PreparedStatement select;
	
	private ResultSet databaseResults;
	
	private JDBCChannel chanTable;
	
	private JDBCTime timeTable;
	
}

