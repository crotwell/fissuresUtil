package edu.sc.seis.fissuresUtil.database.plottable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;
import org.apache.log4j.Logger;
import edu.iris.Fissures.Plottable;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.database.JDBCTable;

public class JDBCPlottable extends JDBCTable{
    
    /**
     * constructor
     * 
     * @param conn -
     *            the database connection
     * @param props -
     *            the properties.
     */
    public JDBCPlottable(Connection conn, Properties props) throws SQLException{
        super("plottable",conn);
        try{
            this.conn = conn;
            this.props = props;
            this.subTableName = "plottableSegments";
            
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("CREATE SEQUENCE plottable_cache_seq");
            stmt.executeUpdate("CREATE TABLE "+getTableName()+"  ("+
                               " plottableid int primary key, "+
                               " network_code text,"+
                               " station_code text,"+
                               " site_code text,"+
                               " channel_code text,"+
                               " year int,"+
                               " jday int,"+
                               " xDimension int,"+
                               " yDimension int,"+
                               " timeInMilliSec bigint,"+
                               " status text) "
            );
            
            stmt.executeUpdate("CREATE TABLE "+subTableName+ " ("+
                               " plottableid int, "+
                               " segment_id int, "+
                               " data byte[], "+
                               " beginTime timestamp, "+
                               " endtime timestamp)"
            );
            stmt.executeUpdate("CREATE INDEX "+getTableName()+
                               "__jday_idx ON "+getTableName()+
            " ( jday ) ");
            stmt.executeUpdate("CREATE INDEX "+subTableName+
                               "__id_idx ON "+subTableName+
            " ( plottableid ) ");
            conn.commit();
        }catch(SQLException sqle) {
            logger.error("The Plottable Cache Tables may be already present"+sqle);
        }
        conn.setAutoCommit(false);
        
        putStmt = conn.prepareStatement("INSERT INTO  "+getTableName()+
                                        " (plottableid, "+
                                        " station_code, "+
                                        " site_code,  "+
                                        " channel_code, "+
                                        " network_code, "+
                                        " year, "+
                                        " jday, "+
                                        " xDimension, "+
                                        " yDimension, "+
                                        " timeInMilliSec, "+
                                        " status) "+
                                        " VALUES(?,?,?,?,?,?,?,?,?,?,?)"
        );
        
        putSubEntryStmt = conn.prepareStatement("INSERT INTO "+subTableName+
                                                " (plottableid, "+
                                                " segment_id, "+
                                                " data, "+
                                                " beginTime, "+
                                                " endTime) "+
                                                " VALUES(?,?,?,?) "
        );
        
        
        updateStatusStmt = conn.prepareStatement("UPDATE "+getTableName()+
                                                 " set status = ? "+
                                                 " WHERE plottableid = ?"
        );
        
        getStatusStmt = conn.prepareStatement("SELECT status FROM "+ getTableName()+
                                              " where plottableid = ? "
        );
        
        getDBIdStmt = conn.prepareStatement(" select plottableid, timeInMilliSec, status FROM "+getTableName()+
                                            " WHERE "+
                                            " station_code = ? AND "+
                                            " site_code = ? AND "+
                                            " channel_code = ? AND "+
                                            " network_code = ? AND "+
                                            " year = ? AND "+
        " jday = ?");
        
        getStmt = conn.prepareStatement(" SELECT plottableobj, beginTime, endTime FROM "+subTableName+
        " WHERE plottableid = ?");
        
        
        getTimesStmt = conn.prepareStatement(" SELECT beginTime, endTime FROM "+subTableName+
                                             " WHERE plottableid = ? "+
        " ORDER BY endTime");
        
        
        deleteStmt = conn.prepareStatement(" delete from "+getTableName()+
                                           " WHERE "+
                                           " station_code = ? AND "+
                                           " site_code = ? AND "+
                                           " channel_code = ? AND "+
                                           " network_code = ? AND "+
                                           " year = ? AND "+
        " jday = ?");
        
        deleteSubEntryStmt = conn.prepareStatement(" delete from "+subTableName+
                                                   " WHERE "+
                                                   " plottableid = ? AND "+
                                                   " segmentid = ? "
        );
        
        
        nextIdStmt = conn.prepareStatement(" SELECT NEXTVAL('plottable_cache_seq')");
        
        
    }
    
    /***************************************************************************
     * puts the array of plottables as large objects into the database
     * 
     * @param plottable[] -
     *            an array of edu.iris.Fissures.Plottable[]
     * @param channelid -
     *            edu.iris.Fissures.IfNetwork.ChannelId
     * @param year -
     *            the year
     * @param jday -
     *            julian day
     * @return int - the dbid of the cached entry.
     **************************************************************************/
    
    public int put( edu.iris.Fissures.Plottable plottable[], 
                    edu.iris.Fissures.IfNetwork.ChannelId channel_id,
                    edu.iris.Fissures.Dimension pixel_size, int year, int jday, String status) throws SQLException, IOException{
        int dbid = 0;
        
        
        //first put the entry into the main table plottable cache
        //and store the dbid for that entry.
        
        int index;
        dbid = getDBIdStmt(channel_id, year, jday);
        if( dbid == -1) {
            dbid = nextId();
            putStmt.setInt(1, dbid);
            index = insert(putStmt, 2, channel_id, year, jday);
            index = insertDimension(putStmt, index, pixel_size);
            index = insertTime(putStmt, index);
            getCurrentTime();
            //      if( calendar.get(Calendar.DAY_OF_YEAR) > jday &&
            // calendar.get(Calendar.YEAR) >= year) {
            //      if(getCurrentTime()*1000 > (new
            // MicroSecondDate(seis.getEndTime())).getMicroSecondTime()){
            //  putStmt.setString(index++, "COMPLETE");
            //} else
            putStmt.setString(index++, status);
            
            putStmt.executeUpdate();
            conn.commit();
        }
        else {
            //      getCurrentTime();
            //if( calendar.get(Calendar.DAY_OF_YEAR) > jday &&
            // calendar.get(Calendar.YEAR) >= year)
            //  updateStatusStmt.setString(1, "COMPLETE");
            //else
            updateStatusStmt.setString(1, status);
            
            updateStatusStmt.setInt(2, dbid);
            updateStatusStmt.executeUpdate();
            conn.commit();
            
        }
        
        //Now iterate through the array of plottables and
        //create largeobjectid using large object manager and creat large
        // objects
        //and insert the dbid and oid corressponding into the
        //table plottablecacheentries.
        
        for(int counter = 0; counter < plottable.length; counter++) {
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(baos);
            os.writeObject(plottable[counter]);
            os.flush();
            os.close();
            byte[] data = baos.toByteArray();
            insertSubEntry(putSubEntryStmt, localSeismograms[counter],  1, dbid, data);
            putSubEntryStmt.executeUpdate();
            conn.commit();
            
        }
        return dbid;
        
    }
    
    /***************************************************************************
     * populates the preparedStatement passed with the currenttime.
     * 
     * @param stmt -
     *            the preparedStatement to be populated
     * @param index -
     *            the starting index
     * @return int - the resultant index.
     **************************************************************************/
    
    public int insertTime(PreparedStatement stmt, int index) throws SQLException {
        
        stmt.setLong(index++, getCurrentTime());
        return index;
    }
    
    /***************************************************************************
     * returns the current time in Milliseconds.
     * 
     * @return long - the current time in milliseconds.
     **************************************************************************/
    public long getCurrentTime() {
        return ClockUtil.now().getTime();
    }
    
    public String getStatus(edu.iris.Fissures.IfNetwork.ChannelId channel_id, int year, int jday) throws SQLException {
        int dbid = getDBIdStmt(channel_id, year, jday);
        
        if(dbid == -1) return new String("NOTCOMPLETE");
        getStatusStmt.setInt(1, dbid);
        ResultSet rs = getStatusStmt.executeQuery();
        if( rs != null) {
            if(rs.next()) {
                return rs.getString("status");
                
            }
        }
        return new String("NOTCOMPLETE");
        
    }
    
    /***************************************************************************
     * populates the PreparedStatement.
     * 
     * @param stmt -
     *            the PreparedStatement
     * @param index -
     *            the index.
     * @param channelid -
     *            edu.iris.Fissures.IfNetwork.ChannelId
     * @param year -
     *            the year
     * @param jday -
     *            julian day
     * @return int - the resultantindex.
     **************************************************************************/
    public int insert(PreparedStatement stmt, int index, edu.iris.Fissures.IfNetwork.ChannelId channel_id,int year, int jday) throws SQLException {
        stmt.setString(index++, channel_id.station_code);
        stmt.setString(index++, channel_id.site_code);
        stmt.setString(index++, channel_id.channel_code);
        stmt.setString(index++, channel_id.network_id.network_code);
        stmt.setInt(index++, year);
        stmt.setInt(index++, jday);
        return index;
    }
    
    
    public int insertDimension(PreparedStatement stmt, int index, edu.iris.Fissures.Dimension dimension) throws SQLException {
        stmt.setInt(index++, dimension.width);
        stmt.setInt(index++, dimension.height);
        return index;
    }
    
    /***************************************************************************
     * populates the preparedStatement passed with the currenttime.
     * 
     * @param stmt -
     *            the preparedStatement to be populated
     * @param index -
     *            the starting index
     * @param dbid -
     *            the databaseid
     * @param oid -
     *            the large object id
     * @return int - the resultant index.
     **************************************************************************/
    
    public int insertSubEntry(PreparedStatement stmt, LocalSeismogram localSeismogram, int index,int dbid,byte[] data) throws SQLException {
        stmt.setInt(index++, dbid);
        stmt.setBytes(index++, data);
        stmt.setTimestamp(index++, ((edu.iris.Fissures.seismogramDC.LocalSeismogramImpl)localSeismogram).getBeginTime().getTimestamp());
        stmt.setTimestamp(index++, ((edu.iris.Fissures.seismogramDC.LocalSeismogramImpl)localSeismogram).getEndTime().getTimestamp());
        return index;
    }
    
    
    /***************************************************************************
     * gets the databaseid corresponding to the parameters passed.
     * 
     * @param channelid -
     *            edu.iris.Fissures.IfNetwork.ChannelId
     * @param year -
     *            the year
     * @param jday -
     *            julian day
     * @return int - the databaseid.
     **************************************************************************/
    public int getDBIdStmt(edu.iris.Fissures.IfNetwork.ChannelId channel_id, int year, int jday) throws SQLException {
        insert(getDBIdStmt, 1,  channel_id, year, jday);
        ResultSet rs = getDBIdStmt.executeQuery();
        int dbid;
        
        if( rs != null) {
            if( rs.next() ) {
                dbid =  rs.getInt(1);
                return dbid;
            }
        }
        return -1;
        
    }
    
    /***************************************************************************
     * gets all the LargeObjectids corresponding to a particular databaseid.
     * 
     * @param dbid -
     *            the databaseid.
     * @return Integer[] - and array of objectids.
     **************************************************************************/
    
    public Integer[] getOIds(int dbid) throws SQLException {
        ArrayList ids = new ArrayList();
        getStmt.setInt(1,dbid);
        ResultSet rs = getStmt.executeQuery();
        if(rs != null) {
            while(rs.next()) {
                ids.add(new Integer(rs.getInt(1)));
            }
        }
        Integer lobjIds[] = new Integer[ids.size()];
        lobjIds = (Integer[])ids.toArray(lobjIds);
        return lobjIds;
    }
    
    
    /***************************************************************************
     * gets the array of Plottables corresponding to the parameters passed.
     * 
     * @param channelid -
     *            edu.iris.Fissures.IfNetwork.ChannelId
     * @param year -
     *            the year
     * @param jday -
     *            julian day
     * @return edu.iris.Fissures.Plottable[] - an array of Plottables.
     **************************************************************************/
    
    
    public  Plottable[] get(edu.iris.Fissures.IfNetwork.ChannelId channel_id, int year, int jday) throws SQLException, IOException, ClassNotFoundException           {
        byte bytes[];
        int oid = 0;
        int dbid = 0;
        int counter;
        ArrayList ids = new ArrayList();
        
        //first get the dbid corresponding to this channel_id, year and jday
        //from the table plottablecache.
        dbid = getDBIdStmt(channel_id, year , jday);
        
        //if dbid == -1 then the entry is not the plottable cache
        if(dbid == -1) return new Plottable[0];
        
        getStmt.setInt(1, dbid);
        
        ResultSet rs = getStmt.executeQuery();
        
        //now build the plottable array and return it.
        if(rs != null) {
            counter = 0;
            while(rs.next() ) {
                oid = rs.getInt(1);
                bytes = rs.getBytes(1);
                
                MicroSecondDate msTime = new MicroSecondDate(rs.getTimestamp("beginTime"));
                MicroSecondDate meTime = new MicroSecondDate(rs.getTimestamp("endTime"));
                TimeInterval timeInterval = meTime.difference(msTime);
                timeInterval = TimeInterval.createTimeInterval(timeInterval.convertTo(UnitImpl.SECOND));
                logger.info("THe TIMEINTERVAL :"+timeInterval);
                
                long minimum, maximum;
                try {
                    
                    minimum = Long.parseLong(props.getProperty("plottable_minimum_time", "300"));
                    maximum = Long.parseLong(props.getProperty("plottable_maximum_time", "86400"));
                    
                } catch(Exception e) {
                    logger.info("Assigning the Offset the default value 18000 sec ");
                    minimum = 300;
                    maximum = 86400;
                }
                
                TimeInterval beginToNow = ClockUtil.now().subtract(meTime);
                beginToNow = TimeInterval.createTimeInterval(beginToNow.convertTo(UnitImpl.SECOND));
                
                
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                
                ObjectInputStream ois = new ObjectInputStream(bais);
                
                ids.add(ois.readObject());
                ois.close();
                bais.close();
                counter++;
                
                
            }
            edu.iris.Fissures.Plottable plottables[] = new edu.iris.Fissures.Plottable[ids.size()];
            plottables = (edu.iris.Fissures.Plottable[])ids.toArray(plottables);
            return plottables;
            
        } else {
            return new Plottable[0];
        }
    }
    
    
    public edu.iris.Fissures.TimeRange[] getTimeRanges(edu.iris.Fissures.IfNetwork.ChannelId channel_id, int year, int jday) throws SQLException           {
        int dbid = getDBIdStmt( channel_id, year, jday);
        //  insert(getTimesStmt, 1, channel_id, year, jday);
        getTimesStmt.setInt(1, dbid);
        ResultSet rs = getTimesStmt.executeQuery();
        ArrayList beginList = new ArrayList();
        ArrayList endList = new ArrayList();
        if( rs != null ) {
            edu.iris.Fissures.TimeRange[] timeRanges;
            while(rs.next()) {
                beginList.add( new edu.iris.Fissures.model.MicroSecondDate(rs.getTimestamp("beginTime")).getFissuresTime());
                endList.add(new edu.iris.Fissures.model.MicroSecondDate(rs.getTimestamp("endTime")).getFissuresTime());
                
            }
        }
        logger.info("IN TIME RANGES: the the arrayList size is "+beginList.size());
        edu.iris.Fissures.TimeRange[] timeRanges = new edu.iris.Fissures.TimeRange[beginList.size()];
        for( int counter = 0; counter < beginList.size(); counter++) {
            logger.info("The beginTime is "+((edu.iris.Fissures.Time)(beginList.get(counter))).date_time);
            logger.info("The endTime is "+((edu.iris.Fissures.Time)(endList.get(counter))).date_time);
            timeRanges[counter] = new edu.iris.Fissures.TimeRange((edu.iris.Fissures.Time)beginList.get(counter),
                                                                  (edu.iris.Fissures.Time)endList.get(counter)
            );
        }
        return timeRanges;
        
    }
    /***************************************************************************
     * gets the row from plottablecahe corresponding to the parameters passed.
     * 
     * @param channelid -
     *            edu.iris.Fissures.IfNetwork.ChannelId
     * @param year -
     *            the year
     * @param jday -
     *            julian day
     **************************************************************************/
    
    public  void delete(edu.iris.Fissures.IfNetwork.ChannelId channel_id, int year, int jday) throws SQLException {
        int dbid = getDBIdStmt(channel_id, year, jday);
        Integer lobjIds[] = getOIds(dbid);
        //first delete the entries in the subtable plottablecacheentries.
        for(int counter = 0; counter < lobjIds.length; counter++) {
            deleteSubEntry(lobjIds[counter].intValue(), dbid);
        }
        insert(deleteStmt, 1, channel_id, year, jday);
        deleteStmt.executeUpdate();
        conn.commit();
    }
    
    
    /***************************************************************************
     * deletes all entry having the Largeobjectid passed.
     * 
     * @param oid -
     *            the large object id
     * @param dbid -
     *            the databaseid.
     **************************************************************************/
    
    public void deleteSubEntry(int plottable_id, int segment_id) throws SQLException {
        deleteSubEntryStmt.setInt(1, plottable_id);
        deleteSubEntryStmt.setInt(2, segment_id);
        deleteSubEntryStmt.executeUpdate();
    }
    
    
    /**
     * returns the nextid to be assigned to the dbid of FlinnEngdahlRegion
     * 
     * @param -
     *            the next assignable dbid.
     */
    
    
    public int nextId() throws SQLException {
        ResultSet rs = nextIdStmt.executeQuery();
        if(rs.next()) {
            return rs.getInt("nextval");
        } else {
            throw new SQLException(" Cannot get nextId for jdbcPlottable");
        }
    }
    
    
    private PreparedStatement putStmt;
    
    private PreparedStatement updateStatusStmt;
    
    private PreparedStatement getStatusStmt;
    
    private PreparedStatement getStmt;
    
    private Connection conn;
    
    private PreparedStatement nextIdStmt;
    
    private PreparedStatement putSubEntryStmt;
    
    private PreparedStatement getDBIdStmt;
    
    private PreparedStatement getTimesStmt;
    
    private PreparedStatement deleteStmt;
    
    private PreparedStatement deleteSubEntryStmt;
    
    private Properties props = new Properties();
    
    private String subTableName;
    
    private static Logger logger = Logger.getLogger(JDBCPlottable.class);
    
}// JDBCPlottableCache
