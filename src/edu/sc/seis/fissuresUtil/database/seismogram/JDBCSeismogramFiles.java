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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.seismogramDC.SeismogramAttrImpl;
import edu.sc.seis.fissuresUtil.bag.DistAz;
import edu.sc.seis.fissuresUtil.bag.EncodedCut;
import edu.sc.seis.fissuresUtil.database.JDBCTable;
import edu.sc.seis.fissuresUtil.database.JDBCTime;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.network.JDBCChannel;
import edu.sc.seis.fissuresUtil.database.util.TableSetup;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.rt130.PacketType;
import edu.sc.seis.fissuresUtil.rt130.RT130FileReader;
import edu.sc.seis.fissuresUtil.rt130.RT130FormatException;
import edu.sc.seis.fissuresUtil.rt130.RT130ToLocalSeismogram;
import edu.sc.seis.fissuresUtil.time.ReduceTool;
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

    public void saveSeismogramToDatabase(Channel channel,
                                         SeismogramAttrImpl seis,
                                         String fileLocation,
                                         SeismogramFileTypes filetype)
            throws SQLException {
        chanTable.put(channel);
        saveSeismogramToDatabase(channel.get_id(), seis, fileLocation, filetype);
    }

    public void saveSeismogramToDatabase(ChannelId channelId,
                                         SeismogramAttrImpl seis,
                                         String fileLocation,
                                         SeismogramFileTypes filetype)
            throws SQLException {
        saveSeismogramToDatabase(chanTable.put(channelId),
                                 timeTable.put(seis.getBeginTime()
                                         .getFissuresTime()),
                                 timeTable.put(seis.getEndTime()
                                         .getFissuresTime()),
                                 fileLocation,
                                 filetype);
    }

    public void saveSeismogramToDatabase(int channelDbId,
                                         int beginTimeId,
                                         int endTimeId,
                                         String fileLocation,
                                         SeismogramFileTypes filetype)
            throws SQLException {
        String filePath = getLocation(fileLocation);
        int fileTypeInt = filetype.getIntValue();
        selectSeismogram.setInt(1, channelDbId);
        selectSeismogram.setString(2, filePath);
        ResultSet results = selectSeismogram.executeQuery();
        if(results.next()) {
            // Do nothing.
        } else {
            insert.setInt(1, channelDbId);
            insert.setInt(2, beginTimeId);
            insert.setInt(3, endTimeId);
            insert.setString(4, filePath);
            insert.setInt(5, fileTypeInt);
            insert.executeUpdate();
        }
    }

    private String getLocation(String fileLocation) {
        // Get absolute file path out of the file path given
        File seismogramFile = new File(fileLocation);
        return seismogramFile.getPath();
    }

    public int removeSeismogramFromDatabase(Channel channel, String seisFile)
            throws SQLException {
        remove.setInt(1, chanTable.put(channel.get_id()));
        remove.setString(2, getLocation(seisFile));
        return remove.executeUpdate();
    }

    public RequestFilter[] findMatchingSeismograms(RequestFilter[] requestArray,
                                                   boolean ignoreNetworkTimes)
            throws SQLException {
        List results = queryDatabaseForSeismograms(requestArray,
                                                   false,
                                                   ignoreNetworkTimes);
        RequestFilter[] request = (RequestFilter[])results.toArray(new RequestFilter[results.size()]);
        RequestFilter[] reduced = ReduceTool.merge(request);
        logger.debug("Reduced " + request.length + " to " + reduced.length);
        return reduced;
    }

    public LocalSeismogram[] getMatchingSeismograms(RequestFilter[] requestArray,
                                                    boolean ignoreNetworkTimes)
            throws SQLException {
        List results = queryDatabaseForSeismograms(requestArray,
                                                   true,
                                                   ignoreNetworkTimes);
        LocalSeismogramImpl[] seis = (LocalSeismogramImpl[])results.toArray(new LocalSeismogramImpl[results.size()]);
        LocalSeismogramImpl[] reduced = ReduceTool.merge(seis);
        logger.debug("Reduced " + seis.length + " to " + reduced.length);
        return reduced;
    }

    public List queryDatabaseForSeismograms(RequestFilter[] request,
                                            boolean returnSeismograms,
                                            boolean ignoreNetworkTimes)
            throws SQLException {
        RequestFilter[] minimalRequest = ReduceTool.merge(request);
        List resultCollector = new ArrayList();
        for(int i = 0; i < minimalRequest.length; i++) {
            queryDatabaseForSeismogram(resultCollector,
                                       minimalRequest[i],
                                       returnSeismograms,
                                       ignoreNetworkTimes);
        }
        return resultCollector;
    }

    public Channel findCloseChannel(Channel newChannel, QuantityImpl distance)
            throws SQLException, NotFound {
        ChannelId[] channelId;
        try {
            channelId = chanTable.getIdsByCode(newChannel.get_id().network_id,
                                               newChannel.get_id().station_code,
                                               newChannel.get_id().site_code,
                                               newChannel.get_code());
        } catch(NotFound e) {
            return null;
        }
        ChannelId newChannelId = newChannel.get_id();
        for(int i = 0; i < channelId.length; i++) {
            if(ChannelIdUtil.areEqualExceptForBeginTime(newChannelId,
                                                        channelId[i])) {
                Channel closeChannel = getChannel(channelId[i]);
                Location locationFromDatabase = closeChannel.my_site.my_location;
                DistAz da = new DistAz(locationFromDatabase,
                                       newChannel.my_site.my_location);
                QuantityImpl siteDistance = new QuantityImpl(DistAz.degreesToKilometers(da.getDelta()),
                                                             UnitImpl.KILOMETER);
                if(siteDistance.lessThan(distance)) {
                    return closeChannel;
                }
            }
        }
        return null;
    }

    private Channel getChannel(ChannelId channelId) throws SQLException,
            NotFound {
        String chanIdString = ChannelIdUtil.toString(channelId);
        Channel closeChannel = (Channel)chanIdToChannel.get(chanIdString);
        if(closeChannel == null) {
            closeChannel = chanTable.get(channelId);
            chanIdToChannel.put(chanIdString, closeChannel);
        }
        return closeChannel;
    }

    private Map chanIdToChannel = new HashMap();

    public void setChannelBeginTimeToEarliest(Channel channelFromDatabase,
                                              Channel newChannel)
            throws SQLException, NotFound {
        MicroSecondDate newChannelBeginTime = new MicroSecondDate(newChannel.get_id().begin_time);
        MicroSecondDate newChannelEndTime = new MicroSecondDate(newChannel.effective_time.end_time);
        MicroSecondDate channelFromDatabaseBeginTime = new MicroSecondDate(channelFromDatabase.get_id().begin_time);
        MicroSecondDate channelFromDatabaseEndTime = new MicroSecondDate(channelFromDatabase.effective_time.end_time);
        if(newChannelBeginTime.before(channelFromDatabaseBeginTime)
                && newChannelEndTime.equals(channelFromDatabaseEndTime)) {
            updateChannelBeginTime.setInt(1,
                                          timeTable.put(newChannel.get_id().begin_time));
            updateChannelBeginTime.setInt(2,
                                          chanTable.getDBId(channelFromDatabase.get_id()));
            updateChannelBeginTime.executeUpdate();
            // Set the begin time of the channel ID from the database
            // to the earlier begin time, so when the channel is put
            // into the database, it matches the updated channel, and
            // does not create a new channel.
            channelFromDatabase.get_id().begin_time = newChannel.get_id().begin_time;
        }
    }

    private void queryDatabaseForSeismogram(List resultCollector,
                                            RequestFilter request,
                                            boolean returnSeismograms,
                                            boolean ignoreNetworkTimes)
            throws SQLException {
        // Retrieve channel ID, begin time, and end time from the request
        // and place the times into a time table while
        // buffering the query by one second on each end.
        int[] chanId;
        EncodedCut cutter = new EncodedCut(request);
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
            logger.debug("Making query " + select);
            databaseResults = select.executeQuery();
            if(returnSeismograms) {
                try {
                    while(databaseResults.next()) {
                        File seismogramFile = new File(databaseResults.getString(4));
                        SeismogramFileTypes filetype = SeismogramFileTypes.fromInt(databaseResults.getInt("filetype"));
                        LocalSeismogramImpl[] curSeis;
                        if(filetype.equals(SeismogramFileTypes.RT_130)) {
                            List refTekSeis = getMatchingSeismogramsFromRefTek(seismogramFile.getCanonicalPath(),
                                                                               request.channel_id,
                                                                               adjustedBeginTime,
                                                                               adjustedEndTime);
                            curSeis = (LocalSeismogramImpl[])refTekSeis.toArray(new LocalSeismogramImpl[refTekSeis.size()]);
                        } else {
                            URLDataSetSeismogram urlSeis = new URLDataSetSeismogram(seismogramFile.toURL(),
                                                                                    filetype);
                            curSeis = urlSeis.getSeismograms();
                        }
                        for(int j = 0; j < curSeis.length; j++) {
                            LocalSeismogram seis = cutter.apply(curSeis[j]);
                            if(seis != null) {
                                resultCollector.add(seis);
                            }
                        }
                        logger.debug("After adding " + seismogramFile
                                + " there are " + resultCollector.size()
                                + " items");
                    }
                } catch(Exception e) {
                    GlobalExceptionHandler.handle("Problem occured while returning seismograms from the database."
                                                          + "\n"
                                                          + "The problem file is located at "
                                                          + databaseResults.getString(4),
                                                  e);
                }
            } else {
                try {
                    while(databaseResults.next()) {
                        RequestFilter req = new RequestFilter(chanTable.getId(databaseResults.getInt(1)),
                                                              timeTable.get(databaseResults.getInt(2)),
                                                              timeTable.get(databaseResults.getInt(3)));
                        req = cutter.apply(req);
                        if(req != null) {
                            resultCollector.add(req);
                        }
                    }
                } catch(Exception e) {
                    GlobalExceptionHandler.handle("Problem occured while querying the database for seismograms.",
                                                  e);
                }
            }
        }
    }

    private List getMatchingSeismogramsFromRefTek(String seismogramFile,
                                                  ChannelId chanId,
                                                  MicroSecondDate beginTime,
                                                  MicroSecondDate endTime)
            throws IOException, RT130FormatException {
        RT130FileReader toSeismogramDataPacket = new RT130FileReader(seismogramFile,
                                                                     true);
        PacketType[] seismogramDataPacketArray = toSeismogramDataPacket.processRT130Data();
        RT130ToLocalSeismogram toSeismogram = new RT130ToLocalSeismogram();
        LocalSeismogramImpl[] seis = toSeismogram.ConvertRT130ToLocalSeismogram(seismogramDataPacketArray);
        logger.debug("Got " + seis.length + " seismograms out of "
                + seismogramFile);
        List matchingSeis = new ArrayList();
        for(int i = 0; i < seis.length; i++) {
            // Check to make sure the seismograms returned fall within the
            // requested time window, and compare the channel code of the
            // channel requested with the channel code of the dummy channel
            // created during the above file reading. If the codes are equal,
            // set the dummy channel equal to the channel requested (real
            // channel).
            if(seis[i].channel_id.channel_code.equals(chanId.channel_code)
                    && seis[i].getBeginTime().before(endTime)
                    && seis[i].getEndTime().after(beginTime)) {
                seis[i].channel_id = chanId;
                matchingSeis.add(seis[i]);
            }
        }
        logger.debug(matchingSeis.size() + " were of the right channel code");
        return matchingSeis;
    }

    public void updateStationCode(String oldName, String newName)
            throws SQLException {
        updateUnitName.setString(1, newName);
        updateUnitName.setString(2, oldName);
        updateUnitName.executeUpdate();
    }

    public void populateStationName() throws SQLException {
        populateStationName.executeUpdate();
    }

    private static final TimeInterval ONE_SECOND = new TimeInterval(1,
                                                                    UnitImpl.SECOND);

    private static final Logger logger = Logger.getLogger(JDBCSeismogramFiles.class);

    private PreparedStatement insert, select, updateChannelBeginTime,
            selectSeismogram, updateUnitName, populateStationName, remove;

    private ResultSet databaseResults;

    private JDBCChannel chanTable;

    private JDBCTime timeTable;
}
