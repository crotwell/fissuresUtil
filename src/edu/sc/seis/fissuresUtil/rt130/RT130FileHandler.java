package edu.sc.seis.fissuresUtil.rt130;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.LocationType;
import edu.iris.Fissures.Orientation;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.SiteId;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.TimeUtils;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.SiteImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.database.ConnectionCreator;
import edu.sc.seis.fissuresUtil.database.JDBCTime;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.network.JDBCChannel;
import edu.sc.seis.fissuresUtil.database.seismogram.RT130Report;
import edu.sc.seis.fissuresUtil.database.seismogram.JDBCSeismogramFiles;
import edu.sc.seis.fissuresUtil.database.seismogram.PopulationProperties;

public class RT130FileHandler {

    public RT130FileHandler(Properties props, List rt130FileHandlerFlags)
            throws SQLException, FileNotFoundException, IOException,
            ParseException {
        flags = rt130FileHandlerFlags;
        checkFlagsForIncompatibleSettings();
        LeapSecondApplier.addLeapSeconds(props.getProperty(LeapSecondApplier.LEAP_SECOND_FILE));
        LeapSecondApplier.addCorrections(props.getProperty(LeapSecondApplier.POWER_UP_TIMES));
        ncFile = new NCFile(props.getProperty(NCFile.NC_FILE_LOC));
        logger.debug("NC file location: " + ncFile.getCanonicalPath());
        String xyFileLoc = props.getProperty(XYReader.XY_FILE_LOC);
        logger.debug("XY file location: " + xyFileLoc);
        if(flags.contains(RT130FileHandlerFlag.SCAN)
                || flags.contains(RT130FileHandlerFlag.FULL)) {} else {
            ConnectionCreator connCreator = new ConnectionCreator(props);
            conn = connCreator.createConnection();
            jdbcSeisFile = new JDBCSeismogramFiles(conn);
            chanTable = new JDBCChannel(conn);
            timeTable = new JDBCTime(conn);
        }
        this.report = new RT130Report();
        this.props = props;
        stationLocations = XYReader.read(new BufferedReader(new FileReader(xyFileLoc)));
    }

    public boolean processSingleRefTekScan(String fileLoc, String fileName)
            throws IOException, SQLException, NotFound, ParseException {
        File file = new File(fileLoc);
        String yearAndDay = file.getParentFile()
                .getParentFile()
                .getParentFile()
                .getName();
        String unitIdNumber = file.getParentFile().getParentFile().getName();
        String datastream = file.getParentFile().getName();
        if(!datastreamToFileData.containsKey(unitIdNumber + datastream)) {
            RT130FileReader rtFileReader = new RT130FileReader(fileLoc, false);
            PacketType[] fileData;
            try {
                fileData = rtFileReader.processRT130Data();
            } catch(RT130FormatException e) {
                report.addProblemFile(fileLoc, fileName
                        + " seems to be an invalid rt130 file." + "\n"
                        + e.getMessage());
                logger.error(fileName + " seems to be an invalid rt130 file."
                        + "\n" + e.getMessage());
                return false;
            }
            datastreamToFileData.put(unitIdNumber + datastream, fileData[0]);
        }
        if(!datastreamToChannel.containsKey(unitIdNumber + datastream)
                && (!datastream.equals("0"))) {
            Channel[] newChannel = createChannels(unitIdNumber, datastream);
            datastreamToChannel.put(unitIdNumber + datastream, newChannel);
        }
        if(fileName.endsWith("00000000")) {
            Channel[] channel = (Channel[])datastreamToChannel.get(unitIdNumber
                    + datastream);
            for(int i = 0; i < channel.length; i++) {
                processSingleRefTekWithKnownChannel(fileLoc,
                                                    fileName,
                                                    channel[i],
                                                    unitIdNumber);
            }
            return true;
        } else {
            try {
                MicroSecondDate beginTime = FileNameParser.getBeginTime(yearAndDay,
                                                                        fileName);
                beginTime = LeapSecondApplier.applyLeapSecondCorrection(unitIdNumber,
                                                                        beginTime);
                TimeInterval lengthOfData = FileNameParser.getLengthOfData(fileName);
                double nominalLengthOfData = Double.valueOf(props.getProperty("nominalLengthOfData"))
                        .doubleValue();
                if(lengthOfData.value > (nominalLengthOfData + (nominalLengthOfData * 0.05))) {
                    Channel[] channel = (Channel[])datastreamToChannel.get(unitIdNumber
                            + datastream);
                    report.addProblemFile(fileLoc,
                                          fileName
                                                  + " seems to be an invalid rt130 file name. The file will be read to determine its true length.");
                    logger.error(fileName
                            + " seems to be an invalid rt130 file name. The file was read to determine its true length.");
                    for(int i = 0; i < channel.length; i++) {
                        processSingleRefTekWithKnownChannel(fileLoc,
                                                            fileName,
                                                            channel[i],
                                                            unitIdNumber);
                    }
                    return true;
                } else {
                    MicroSecondDate endTime = beginTime.add(lengthOfData);
                    Channel[] channel = (Channel[])datastreamToChannel.get(unitIdNumber
                            + datastream);
                    for(int i = 0; i < channel.length; i++) {
                        saveRefTekChannelToDatabase(channel[i],
                                                    beginTime,
                                                    endTime);
                    }
                    return true;
                }
            } catch(RT130FormatException e) {
                report.addProblemFile(fileLoc, fileName
                        + " seems to be an invalid rt130 file." + "\n"
                        + e.getMessage());
                logger.error(fileName + " seems to be an invalid rt130 file."
                        + "\n" + e.getMessage());
                return false;
            }
        }
    }

    public boolean processSingleRefTekFull(String fileLoc, String fileName)
            throws IOException, SQLException, NotFound, ParseException {
        if(ncFile == null) {
            logger.debug("No NC (Network Configuration) file was specified. "
                    + "The channel IDs created will not be correct.");
        }
        RT130FileReader toSeismogramDataPackets = new RT130FileReader(fileLoc,
                                                                      true);
        PacketType[] seismogramDataPacketArray = null;
        try {
            seismogramDataPacketArray = toSeismogramDataPackets.processRT130Data();
        } catch(RT130FormatException e) {
            report.addProblemFile(fileLoc, fileName
                    + " seems to be an invalid rt130 file." + "\n"
                    + e.getMessage());
            logger.error(fileName + " seems to be an invalid rt130 file."
                    + "\n" + e.getMessage());
            return false;
        }
        RT130ToLocalSeismogram toSeismogram = new RT130ToLocalSeismogram(ncFile,
                                                                         props,
                                                                         stationLocations);
        LocalSeismogramImpl[] seismogramArray = toSeismogram.ConvertRT130ToLocalSeismogram(seismogramDataPacketArray);
        Channel[] channel = toSeismogram.getChannels();
        // Check database for channels that match (with lat/long buffer)
        // If channel exists, use it. If not, use new channel.
        for(int i = 0; i < seismogramArray.length; i++) {
            if(flags.contains(RT130FileHandlerFlag.SCAN)
                    || flags.contains(RT130FileHandlerFlag.FULL)) {
                saveRefTekChannelToDatabase(channel[i],
                                            seismogramArray[i].getBeginTime(),
                                            seismogramArray[i].getEndTime());
            } else {
                Channel closeChannel = jdbcSeisFile.findCloseChannel(channel[i],
                                                                     new QuantityImpl(1,
                                                                                      UnitImpl.KILOMETER));
                if(closeChannel == null) {
                    saveRefTekChannelToDatabase(channel[i],
                                                seismogramArray[i].getBeginTime(),
                                                seismogramArray[i].getEndTime());
                } else {
                    jdbcSeisFile.setChannelBeginTimeToEarliest(closeChannel,
                                                               channel[i]);
                    saveRefTekChannelToDatabase(closeChannel,
                                                seismogramArray[i].getBeginTime(),
                                                seismogramArray[i].getEndTime());
                }
            }
        }
        return true;
    }

    private Channel[] createChannels(String unitIdNumber, String datastream) {
        String stationCode = ncFile.getUnitName(((PacketType)(datastreamToFileData.get(unitIdNumber
                                                        + datastream))).begin_time_from_state_of_health_file,
                                                unitIdNumber);
        String networkIdString = props.getProperty(PopulationProperties.NETWORK_REMAP
                + "XX");
        Time networkBeginTime = ncFile.network_begin_time.getFissuresTime();
        Time channelBeginTime = networkBeginTime;
        NetworkId networkId = PopulationProperties.getNetworkAttr(networkIdString,
                                                                  props)
                .get_id();
        networkId.begin_time = networkBeginTime;
        String tempCode = "B";
        if(((PacketType)(datastreamToFileData.get(unitIdNumber + datastream))).sample_rate < 10) {
            tempCode = "L";
        }
        ChannelId[] channelId = {new ChannelId(networkId,
                                               stationCode,
                                               "00",
                                               tempCode + "HZ",
                                               channelBeginTime),
                                 new ChannelId(networkId,
                                               stationCode,
                                               "00",
                                               tempCode + "HN",
                                               channelBeginTime),
                                 new ChannelId(networkId,
                                               stationCode,
                                               "00",
                                               tempCode + "HE",
                                               channelBeginTime)};
        TimeRange effectiveChannelTime = new TimeRange(channelBeginTime,
                                                       TimeUtils.timeUnknown);
        SiteId siteId = new SiteId(networkId,
                                   stationCode,
                                   "00",
                                   channelBeginTime);
        StationId stationId = new StationId(networkId,
                                            stationCode,
                                            channelBeginTime);
        Location location = new Location(0,
                                         0,
                                         new QuantityImpl(0, UnitImpl.METER),
                                         new QuantityImpl(0, UnitImpl.METER),
                                         LocationType.GEOGRAPHIC);
        if(stationLocations.containsKey(stationCode)) {
            location = (Location)stationLocations.get(stationCode);
        } else {
            logger.error("XY file did not contain a location for unit "
                    + stationCode
                    + ".\n"
                    + "The location used for the unit will be the Gulf of Guinea (Atlantic Ocean).");
        }
        NetworkAttrImpl networkAttr = PopulationProperties.getNetworkAttr(networkIdString,
                                                                          props);
        StationImpl station = new StationImpl(stationId,
                                              "",
                                              location,
                                              effectiveChannelTime,
                                              "",
                                              "",
                                              "",
                                              networkAttr);
        SiteImpl site = new SiteImpl(siteId,
                                     location,
                                     effectiveChannelTime,
                                     station,
                                     "");
        SamplingImpl sampling = new SamplingImpl(((PacketType)(datastreamToFileData.get(unitIdNumber
                                                         + datastream))).sample_rate,
                                                 new TimeInterval(1,
                                                                  UnitImpl.SECOND));
        Channel[] newChannel = new ChannelImpl[channelId.length];
        for(int i = 0; i < channelId.length; i++) {
            if(channelId[i].channel_code.endsWith("N")) {
                newChannel[i] = new ChannelImpl(channelId[i],
                                                "",
                                                new Orientation(0, 0),
                                                sampling,
                                                effectiveChannelTime,
                                                site);
            } else if(channelId[i].channel_code.endsWith("E")) {
                newChannel[i] = new ChannelImpl(channelId[i],
                                                "",
                                                new Orientation(90, 0),
                                                sampling,
                                                effectiveChannelTime,
                                                site);
            } else if(channelId[i].channel_code.endsWith("Z")) {
                newChannel[i] = new ChannelImpl(channelId[i],
                                                "",
                                                new Orientation(0, -90),
                                                sampling,
                                                effectiveChannelTime,
                                                site);
            }
        }
        return newChannel;
    }

    private boolean processSingleRefTekWithKnownChannel(String fileLoc,
                                                        String fileName,
                                                        Channel knownChannel,
                                                        String unitIdNumber)
            throws IOException, SQLException, NotFound, ParseException {
        RT130FileReader toSeismogramDataPackets = new RT130FileReader(fileLoc,
                                                                      false);
        PacketType[] seismogramDataPacketArray = null;
        try {
            seismogramDataPacketArray = toSeismogramDataPackets.processRT130Data();
        } catch(RT130FormatException e) {
            report.addProblemFile(fileLoc, fileName
                    + " seems to be an invalid rt130 file." + "\n"
                    + e.getMessage());
            logger.error(fileName + " seems to be an invalid rt130 file."
                    + "\n" + e.getMessage());
            return false;
        }
        RT130ToLocalSeismogram toSeismogram = new RT130ToLocalSeismogram(ncFile,
                                                                         props,
                                                                         stationLocations);
        LocalSeismogramImpl[] seismogramArray = toSeismogram.ConvertRT130ToLocalSeismogram(seismogramDataPacketArray);
        for(int i = 0; i < seismogramArray.length; i++) {
            saveRefTekChannelToDatabase(knownChannel,
                                        LeapSecondApplier.applyLeapSecondCorrection(unitIdNumber,
                                                                                    seismogramArray[i].getBeginTime()),
                                        LeapSecondApplier.applyLeapSecondCorrection(unitIdNumber,
                                                                                    seismogramArray[i].getEndTime()));
        }
        return true;
    }

    private void saveRefTekChannelToDatabase(Channel channel,
                                             MicroSecondDate beginTime,
                                             MicroSecondDate endTime)
            throws SQLException, NotFound {
        report.addRefTekSeismogram(channel, beginTime, endTime);
        if(flags.contains(RT130FileHandlerFlag.POP_DB_WITH_CHANNELS)) {
            putChannelInDb(channel);
            putTimeInDb(beginTime);
            putTimeInDb(endTime);
        }
    }

    private void putTimeInDb(MicroSecondDate date) throws SQLException {
        Integer timeDbId = (Integer)timeToDbId.get(date);
        if(timeDbId == null) {
            timeDbId = new Integer(timeTable.put(date.getFissuresTime()));
            timeToDbId.put(date, timeDbId);
        }
    }

    private void putChannelInDb(Channel channel) throws SQLException, NotFound {
        Integer channelDbId = (Integer)channelToDbId.get(channel);
        if(channelDbId == null) {
            channelDbId = new Integer(chanTable.put(channel));
            channelToDbId.put(channel, channelDbId);
        }
    }

    private void checkFlagsForIncompatibleSettings() {
        if(flags.contains(RT130FileHandlerFlag.SCAN)
                && flags.contains(RT130FileHandlerFlag.FULL)) {
            while(flags.contains(RT130FileHandlerFlag.FULL)) {
                flags.remove(RT130FileHandlerFlag.FULL);
            }
            logger.warn("Both -scan and -full flags were set.");
            logger.warn("Scan processing of RT130 data: ON");
        }
        if(flags.contains(RT130FileHandlerFlag.NO_LOGS)
                && flags.contains(RT130FileHandlerFlag.MAKE_LOGS)) {
            while(flags.contains(RT130FileHandlerFlag.NO_LOGS)) {
                flags.remove(RT130FileHandlerFlag.NO_LOGS);
            }
            logger.warn("Both -nologs and -makelogs flags were set.");
            logger.warn("Log creation: ON");
        }
    }

    public List getFlags() {
        return flags;
    }

    public RT130Report getReport() {
        return report;
    }

    public Properties getProps() {
        return props;
    }

    public JDBCSeismogramFiles getJDBCSeismogramFiles() {
        return jdbcSeisFile;
    }

    private List flags;

    private NCFile ncFile;

    private RT130Report report;

    private Connection conn;

    private Properties props;

    private JDBCSeismogramFiles jdbcSeisFile;

    private JDBCChannel chanTable;

    private JDBCTime timeTable;

    private Map stationLocations;

    private Map timeToDbId = new HashMap();

    private Map channelToDbId = new HashMap();

    private Map datastreamToChannel = new HashMap();

    private Map datastreamToFileData = new HashMap();

    private static final Logger logger = Logger.getLogger(RT130FileHandler.class);
}
