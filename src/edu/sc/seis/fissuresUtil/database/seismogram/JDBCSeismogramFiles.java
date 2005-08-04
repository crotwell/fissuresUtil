/*
 * Created on May 6, 2005
 * 
 */
package edu.sc.seis.fissuresUtil.database.seismogram;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.TimeUtils;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.DistAz;
import edu.sc.seis.fissuresUtil.database.JDBCTable;
import edu.sc.seis.fissuresUtil.database.JDBCTime;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.network.JDBCChannel;
import edu.sc.seis.fissuresUtil.database.util.TableSetup;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.rt130.Append;
import edu.sc.seis.fissuresUtil.rt130.PacketType;
import edu.sc.seis.fissuresUtil.rt130.RT130FormatException;
import edu.sc.seis.fissuresUtil.rt130.RT130FileReader;
import edu.sc.seis.fissuresUtil.rt130.RT130ToLocalSeismogram;
import edu.sc.seis.fissuresUtil.xml.SeismogramFileTypes;
import edu.sc.seis.fissuresUtil.xml.URLDataSetSeismogram;

/**
 * @author fenner
 */
public class JDBCSeismogramFiles extends JDBCTable {

    public JDBCSeismogramFiles(Connection conn) throws SQLException {
        super("seisfilereference", conn);
        timeTable = new JDBCTime(conn);
        chanTable = new JDBCChannel(conn);
        TableSetup.setup(this,
                         "edu/sc/seis/fissuresUtil/database/seismogram/seisfilereference.vm");
    }

    public void saveSeismogramToDatabase(ChannelId channelId,
                                         LocalSeismogramImpl seis,
                                         String fileLocation,
                                         SeismogramFileTypes filetype)
            throws SQLException {
        // Get absolute file path out of the file path given
        File seismogramFile = new File(fileLocation);
        String absoluteFilePath = seismogramFile.getPath();
        insert.setInt(1, chanTable.put(channelId));
        insert.setInt(2, timeTable.put(seis.getBeginTime().getFissuresTime()));
        insert.setInt(3, timeTable.put(seis.getEndTime().getFissuresTime()));
        insert.setString(4, absoluteFilePath);
        insert.setInt(5, filetype.getIntValue());
        insert.executeUpdate();
    }

    public void saveSeismogramToDatabase(Channel channel,
                                         LocalSeismogramImpl seis,
                                         String fileLocation,
                                         SeismogramFileTypes filetype)
            throws SQLException {
        // Get absolute file path out of the file path given
        File seismogramFile = new File(fileLocation);
        String absoluteFilePath = seismogramFile.getPath();
        insert.setInt(1, chanTable.put(channel));
        insert.setInt(2, timeTable.put(seis.getBeginTime().getFissuresTime()));
        insert.setInt(3, timeTable.put(seis.getEndTime().getFissuresTime()));
        insert.setString(4, absoluteFilePath);
        insert.setInt(5, filetype.getIntValue());
        insert.executeUpdate();
    }

    public RequestFilter[] findMatchingSeismograms(RequestFilter[] requestArray,
                                                   boolean ignoreNetworkTimes)
            throws SQLException {
        List matchingSeismogramsResultList = queryDatabaseForSeismograms(requestArray,
                                                                         false,
                                                                         ignoreNetworkTimes);
        return (RequestFilter[])matchingSeismogramsResultList.toArray(new RequestFilter[matchingSeismogramsResultList.size()]);
    }

    public LocalSeismogram[] getMatchingSeismograms(RequestFilter[] requestArray,
                                                    boolean ignoreNetworkTimes)
            throws SQLException {
        List matchingSeismogramsResultList = queryDatabaseForSeismograms(requestArray,
                                                                         true,
                                                                         ignoreNetworkTimes);
        return (LocalSeismogram[])matchingSeismogramsResultList.toArray(new LocalSeismogram[matchingSeismogramsResultList.size()]);
    }

    public List queryDatabaseForSeismograms(RequestFilter[] requestArray,
                                            boolean returnSeismograms,
                                            boolean ignoreNetworkTimes)
            throws SQLException {
        List matchingSeismogramsResultList = new ArrayList();
        // Loop used to compair data in requestArray with the database and save
        // results in a ResultSet.
        for(int i = 0; i < requestArray.length; i++) {
            queryDatabaseForSeismogram(matchingSeismogramsResultList,
                                       requestArray[i],
                                       returnSeismograms,
                                       ignoreNetworkTimes);
        }
        return matchingSeismogramsResultList;
    }

    public Channel findCloseChannel(Channel newChannel, QuantityImpl distance)
            throws SQLException, NotFound {
        Channel[] channel = chanTable.getAllChannels();
        for(int i = 0; i < channel.length; i++) {
            Location locationFromDatabase = channel[i].my_site.my_location;
            DistAz da = new DistAz(locationFromDatabase,
                                   newChannel.my_site.my_location);
            QuantityImpl siteDistance = new QuantityImpl(DistAz.degreesToKilometers(da.getDelta()),
                                                         UnitImpl.KILOMETER);            
            if(ChannelIdUtil.areEqualExceptForBeginTime(newChannel.get_id(), channel[i].get_id()) && siteDistance.lessThan(distance)) {
                return channel[i];
            }
        }
        return null;
    }

    public void setChannelBeginTimeToEarliest(Channel channelFromDatabase,
                                              Channel newChannel)
            throws SQLException, NotFound{
        MicroSecondDate newChannelBeginTime = new MicroSecondDate(newChannel.get_id().begin_time);
        MicroSecondDate channelFromDatabaseBeginTime = new MicroSecondDate(channelFromDatabase.get_id().begin_time);
        if(newChannelBeginTime.before(channelFromDatabaseBeginTime)){
            updateChannelBeginTime.setInt(1, timeTable.put(newChannel.get_id().begin_time));
            updateChannelBeginTime.setInt(2, chanTable.getDBId(channelFromDatabase.get_id()));
            updateChannelBeginTime.executeUpdate();
            // Set the begin time of the channel ID from the database
            // to the earlier begin time, so when the channel is put 
            // into the database, it matches the updated channel, and 
            // does not create a new channel.
            channelFromDatabase.get_id().begin_time = newChannel.get_id().begin_time;
        }
    }
    
    private void queryDatabaseForSeismogram(List matchingSeismogramsResultList,
                                            RequestFilter request,
                                            boolean returnSeismograms,
                                            boolean ignoreNetworkTimes)
            throws SQLException {
        // Retrieve channel ID, begin time, and end time from the request
        // and place the times into a time table while
        // buffering the query by one second on each end.
        int[] chanId;
        try {
            if(ignoreNetworkTimes) {
                chanId = chanTable.getDBIdIgnoringNetworkId(request.channel_id.network_id.network_code,
                                                            request.channel_id.station_code,
                                                            request.channel_id.site_code,
                                                            request.channel_id.channel_code);
            } else {
                chanId = new int[] {chanTable.getDBId(request.channel_id)};
            }
        } catch(NotFound e) {
            logger.debug("Can not find channel ID in database.");
            return;
        }
        MicroSecondDate adjustedBeginTime = new MicroSecondDate(request.start_time).subtract(ONE_SECOND);
        MicroSecondDate adjustedEndTime = new MicroSecondDate(request.end_time).add(ONE_SECOND);
        for(int i = 0; i < chanId.length; i++) {
            // Populate databaseResults with all of the matching seismograms
            // from the database.
            select.setInt(1, chanId[i]);
            select.setTimestamp(2, adjustedEndTime.getTimestamp());
            select.setTimestamp(3, adjustedBeginTime.getTimestamp());
            databaseResults = select.executeQuery();
            if(returnSeismograms) {
                try {
                    while(databaseResults.next()) {
                        File seismogramFile = new File(databaseResults.getString(4));
                        SeismogramFileTypes filetype = SeismogramFileTypes.fromInt(databaseResults.getInt("filetype"));
                        if(filetype.equals(SeismogramFileTypes.RT_130)) {
                            List refTekSeismogramsList = getMatchingSeismogramsFromRefTek(seismogramFile.getAbsolutePath(),
                                                                                          request.channel_id,
                                                                                          adjustedBeginTime,
                                                                                          adjustedEndTime);
                            LocalSeismogramImpl[] refTekSeismogramsArray = (LocalSeismogramImpl[])refTekSeismogramsList.toArray(new LocalSeismogramImpl[refTekSeismogramsList.size()]);
                            for(int j = 0; j < refTekSeismogramsArray.length; j++) {
                                matchingSeismogramsResultList.add(refTekSeismogramsArray[j]);
                            }
                        } else {
                            URLDataSetSeismogram urlSeis = new URLDataSetSeismogram(seismogramFile.toURL(),
                                                                                    filetype);
                            LocalSeismogramImpl[] result = urlSeis.getSeismograms();
                            for(int j = 0; j < result.length; j++) {
                                matchingSeismogramsResultList.add(result[j]);
                            }
                        }
                    }
                } catch(Exception e) {
                    GlobalExceptionHandler.handle("Problem occured while returning seismograms from the database.",
                                                  e);
                }
            } else {
                try {
                    while(databaseResults.next()) {
                        RequestFilter resultSetToRequestFilter = new RequestFilter();
                        resultSetToRequestFilter.channel_id = chanTable.getId(databaseResults.getInt(1));
                        resultSetToRequestFilter.start_time = timeTable.get(databaseResults.getInt(2));
                        resultSetToRequestFilter.end_time = timeTable.get(databaseResults.getInt(3));
                        matchingSeismogramsResultList.add(resultSetToRequestFilter);
                    }
                } catch(Exception e) {
                    GlobalExceptionHandler.handle("Problem occured while querying the database for seismograms.");
                }
            }
        }
    }

    private List getMatchingSeismogramsFromRefTek(String seismogramFile,
                                                  ChannelId chanId,
                                                  MicroSecondDate beginTime,
                                                  MicroSecondDate endTime)
            throws IOException {
        RT130FileReader toSeismogramDataPacket = new RT130FileReader(seismogramFile,
                                                                     true);
        PacketType[] seismogramDataPacketArray;
        LocalSeismogramImpl[] seismogramArray;
        try {
            seismogramDataPacketArray = toSeismogramDataPacket.processRT130Data();
            RT130ToLocalSeismogram toSeismogram = new RT130ToLocalSeismogram();
            seismogramArray = toSeismogram.ConvertRT130ToLocalSeismogram(seismogramDataPacketArray);
        } catch(RT130FormatException e) {
            logger.debug("Problem occured while returning rt130 seismograms from the file listed in the database.");
            return new ArrayList(0);
        }
        List matchingSeismogramsResultList = new ArrayList(0);
        for(int i = 0; i < seismogramArray.length; i++) {
            // Check to make sure the seismograms returned fall within the
            // requested time window, and compair the channel code of the
            // channel requested with the channel code of the dummy channel
            // created during the above file reading. If the codes are equal,
            // set the dummy channel equal to the channel requested (real
            // channel).
            if(seismogramArray[i].channel_id.channel_code.equals(chanId.channel_code)
                    && seismogramArray[i].getBeginTime().before(endTime)
                    && seismogramArray[i].getEndTime().after(beginTime)) {
                seismogramArray[i].channel_id = chanId;
                matchingSeismogramsResultList.add(seismogramArray[i]);
            }
        }
        return matchingSeismogramsResultList;
    }

    private static final TimeInterval ONE_SECOND = new TimeInterval(1,
                                                                    UnitImpl.SECOND);

    private static final Logger logger = Logger.getLogger(JDBCSeismogramFiles.class);

    private PreparedStatement insert, select, updateChannelBeginTime;

    private ResultSet databaseResults;

    private JDBCChannel chanTable;

    private JDBCTime timeTable;
}
