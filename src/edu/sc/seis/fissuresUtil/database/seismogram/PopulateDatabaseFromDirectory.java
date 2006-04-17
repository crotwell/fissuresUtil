package edu.sc.seis.fissuresUtil.database.seismogram;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import edu.iris.Fissures.FissuresException;
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
import edu.iris.Fissures.seismogramDC.SeismogramAttrImpl;
import edu.sc.seis.fissuresUtil.database.ConnectionCreator;
import edu.sc.seis.fissuresUtil.database.JDBCTime;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.network.JDBCChannel;
import edu.sc.seis.fissuresUtil.mseed.FissuresConvert;
import edu.sc.seis.fissuresUtil.rt130.FileNameParser;
import edu.sc.seis.fissuresUtil.rt130.LeapSecondApplier;
import edu.sc.seis.fissuresUtil.rt130.NCFile;
import edu.sc.seis.fissuresUtil.rt130.PacketType;
import edu.sc.seis.fissuresUtil.rt130.RT130FileReader;
import edu.sc.seis.fissuresUtil.rt130.RT130FormatException;
import edu.sc.seis.fissuresUtil.rt130.RT130ToLocalSeismogram;
import edu.sc.seis.fissuresUtil.rt130.XYReader;
import edu.sc.seis.fissuresUtil.sac.SacToFissures;
import edu.sc.seis.fissuresUtil.simple.Initializer;
import edu.sc.seis.fissuresUtil.xml.SeismogramFileTypes;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.MiniSeedRead;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.sac.SacTimeSeries;

public class PopulateDatabaseFromDirectory {

    public static void main(String[] args) throws FissuresException,
            IOException, SeedFormatException, SQLException, NotFound,
            ParseException {
        BasicConfigurator.configure();
        Properties props = Initializer.loadProperties(args);
        LeapSecondApplier.addLeapSeconds(props.getProperty("leapSecondTimeFileLoc"));
        LeapSecondApplier.addCorrections(props.getProperty("powerUpTimeFileLoc"));
        ConnectionCreator connCreator = new ConnectionCreator(props);
        Connection conn = connCreator.createConnection();
        DatabasePopulationReport report = new DatabasePopulationReport();
        JDBCSeismogramFiles jdbcSeisFile = new JDBCSeismogramFiles(conn);
        JDBCChannel chanTable = new JDBCChannel(conn);
        JDBCTime timeTable = new JDBCTime(conn);
        boolean finished = false;
        boolean batch = false;
        NCFile ncFile = new NCFile(props.getProperty("NCFileLoc"));
        logger.debug("NC file location: " + ncFile.getCanonicalPath());
        String xyFileLoc = props.getProperty("XYFileLoc");
        logger.debug("XY file location: " + xyFileLoc);
        Map stationLocations = XYReader.read(new BufferedReader(new FileReader(xyFileLoc)));
        for(int i = 1; i < args.length - 1; i++) {
            if(args[i].equals("-props")) {
                String propFileLocation = args[i + 1];
                File file = new File(propFileLocation);
                logger.debug("Properties file location: "
                        + file.getCanonicalPath());
            }
        }
        for(int i = 1; i < args.length - 1; i++) {
            if(args[i].equals("-hsql")) {
                String hsqlFileLocation = args[i + 1];
                File file = new File(hsqlFileLocation);
                logger.debug("HSQL properties file location: "
                        + file.getCanonicalPath());
            }
        }
        for(int i = 0; i < args.length; i++) {
            if(args[i].equals("-rt")) {
                batch = true;
                logger.debug("Batch process of RT130 data: ON");
            }
        }
        if(args.length > 0) {
            String fileLoc = args[args.length - 1];
            File file = new File(fileLoc);
            if(file.isDirectory()) {
                finished = readEntireDirectory(file,
                                               conn,
                                               ncFile,
                                               jdbcSeisFile,
                                               report,
                                               chanTable,
                                               timeTable,
                                               props,
                                               batch,
                                               stationLocations);
            } else if(file.isFile()) {
                finished = readSingleFile(fileLoc,
                                          conn,
                                          ncFile,
                                          jdbcSeisFile,
                                          report,
                                          chanTable,
                                          timeTable,
                                          props,
                                          batch,
                                          stationLocations);
            } else {
                logger.error("File: " + file
                        + " is not a file or a directory. This can"
                        + " be caused in Windows when the file path includes"
                        + " a Unix-style reference (soft or hard).");
            }
        } else {
            printHelp();
        }
        if(finished) {
            report.makeReportImage();
            report.printReport();
            System.out.println();
            System.out.println("Database population complete.");
            System.out.println();
        } else {
            printHelp();
        }
    }

    private static boolean readSingleFile(String fileLoc,
                                          Connection conn,
                                          NCFile ncFile,
                                          JDBCSeismogramFiles jdbcSeisFile,
                                          DatabasePopulationReport report,
                                          JDBCChannel chanTable,
                                          JDBCTime timeTable,
                                          Properties props,
                                          boolean batch,
                                          Map stationLocations)
            throws IOException, FissuresException, SeedFormatException,
            SQLException, NotFound, ParseException {
        boolean finished = false;
        StringTokenizer t = new StringTokenizer(fileLoc, "/\\");
        String fileName = "";
        while(t.hasMoreTokens()) {
            fileName = t.nextToken();
        }
        if(fileName.length() == 18 && fileName.charAt(9) == '_') {
            if(batch) {
                finished = processSingleRefTekBatch(jdbcSeisFile,
                                                    report,
                                                    conn,
                                                    fileLoc,
                                                    fileName,
                                                    ncFile,
                                                    chanTable,
                                                    timeTable,
                                                    props,
                                                    stationLocations);
            } else {
                finished = processSingleRefTek(jdbcSeisFile,
                                               report,
                                               conn,
                                               fileLoc,
                                               fileName,
                                               ncFile,
                                               chanTable,
                                               timeTable,
                                               props,
                                               stationLocations);
            }
        } else if(fileName.endsWith(".mseed")) {
            finished = processMSeed(jdbcSeisFile, report, fileLoc, fileName);
        } else if(fileName.endsWith(".sac")) {
            finished = processSac(jdbcSeisFile,
                                  report,
                                  fileLoc,
                                  fileName,
                                  props);
        } else {
            if(fileName.equals("SOH.RT")) {
                logger.debug("Ignoring file: " + fileName);
            } else if(fileName.equals(".DS_Store")) {
                logger.debug("Ignoring Mac OS X file: " + fileName);
            } else {
                report.addProblemFile(fileLoc,
                                      fileName
                                              + " can not be processed because it's file"
                                              + " name is not formatted correctly, and therefore"
                                              + " is assumed to be an invalid file format. If"
                                              + " the data file format is valid (mini seed, sac, rt130)"
                                              + " try renaming the file.");
                logger.debug(fileName
                        + " can not be processed because it's file"
                        + " name is not formatted correctly, and therefore"
                        + " is assumed to be an invalid file format. If"
                        + " the data file format is valid (mini seed, sac, rt130)"
                        + " try renaming the file.");
            }
        }
        return finished;
    }

    private static boolean readEntireDirectory(File baseDirectory,
                                               Connection conn,
                                               NCFile ncFile,
                                               JDBCSeismogramFiles jdbcSeisFile,
                                               DatabasePopulationReport report,
                                               JDBCChannel chanTable,
                                               JDBCTime timeTable,
                                               Properties props,
                                               boolean batch,
                                               Map stationLocations)
            throws FissuresException, IOException, SeedFormatException,
            SQLException, NotFound, ParseException {
        File[] files = baseDirectory.listFiles();
        if(files == null) {
            throw new IOException("Unable to get listing of directory: "
                    + baseDirectory);
        }
        for(int i = 0; i < files.length; i++) {
            if(files[i].isDirectory()) {
                readEntireDirectory(files[i],
                                    conn,
                                    ncFile,
                                    jdbcSeisFile,
                                    report,
                                    chanTable,
                                    timeTable,
                                    props,
                                    batch,
                                    stationLocations);
            } else {
                readSingleFile(files[i].getCanonicalPath(),
                               conn,
                               ncFile,
                               jdbcSeisFile,
                               report,
                               chanTable,
                               timeTable,
                               props,
                               batch,
                               stationLocations);
            }
        }
        return true;
    }

    private static void printHelp() {
        System.out.println();
        System.out.println("    The last argument must be a directory or file location.");
        System.out.println("    The default SOD properties file is server.properties.");
        System.out.println("    The default database properties file is server.properties.");
        System.out.println();
        System.out.println("    -props   | Accepts alternate SOD properties file");
        System.out.println("    -hsql    | Accepts alternate database properties file");
        System.out.println("    -v       | Turn verbose messages on");
        System.out.println("    -rt      | Turn on batch process of RT130 data");
        System.out.println("             |   No other types of data can be processed");
        System.out.println();
        System.out.println();
        System.out.println("Program finished before database population was completed.");
        System.out.println();
    }

    private static boolean processSac(JDBCSeismogramFiles jdbcSeisFile,
                                      DatabasePopulationReport report,
                                      String fileLoc,
                                      String fileName,
                                      Properties props) throws IOException,
            FissuresException, SQLException {
        SacTimeSeries sacTime = new SacTimeSeries();
        try {
            sacTime.readHeader(new DataInputStream(new BufferedInputStream(new FileInputStream(fileLoc))));
        } catch(EOFException e) {
            report.addProblemFile(fileLoc, fileName
                    + " seems to be an invalid sac file.");
            logger.error(fileName + " seems to be an invalid sac file.");
            return false;
        } catch(FileNotFoundException e) {
            report.addProblemFile(fileLoc, "Unable to find file " + fileName);
            logger.error("Unable to find file " + fileName);
            return false;
        }
        SeismogramAttrImpl seis = SacToFissures.getSeismogramAttr(sacTime);
        Channel chan = SacToFissures.getChannel(sacTime);
        chan = PopulationProperties.fix(chan, props);
        saveSacToDatabase(jdbcSeisFile, report, chan, seis, fileLoc);
        return true;
    }

    private static boolean processMSeed(JDBCSeismogramFiles jdbcSeisFile,
                                        DatabasePopulationReport report,
                                        String fileLoc,
                                        String fileName) throws IOException,
            SeedFormatException, FissuresException, SQLException {
        MiniSeedRead mseedRead = null;
        try {
            mseedRead = new MiniSeedRead(new DataInputStream(new BufferedInputStream(new FileInputStream(fileLoc))));
        } catch(EOFException e) {
            report.addProblemFile(fileLoc, fileName
                    + " seems to be an invalid mseed file.");
            logger.error(fileName + " seems to be an invalid mseed file.");
            return false;
        } catch(FileNotFoundException e) {
            report.addProblemFile(fileLoc, "Unable to find file " + fileName);
            logger.error("Unable to find file " + fileName);
            return false;
        }
        LinkedList list = new LinkedList();
        try {
            DataRecord dr = mseedRead.getNextRecord();
            list.add(dr);
        } catch(EOFException e) {
            // must be all
        }
        LocalSeismogramImpl seis = FissuresConvert.toFissures((DataRecord[])list.toArray(new DataRecord[0]));
        saveMSeedToDatabase(jdbcSeisFile, report, seis, fileLoc);
        return true;
    }

    private static boolean processSingleRefTek(JDBCSeismogramFiles jdbcSeisFile,
                                               DatabasePopulationReport report,
                                               Connection conn,
                                               String fileLoc,
                                               String fileName,
                                               NCFile ncFile,
                                               JDBCChannel chanTable,
                                               JDBCTime timeTable,
                                               Properties props,
                                               Map stationLocations)
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
                    + " seems to be an invalid rt130 file.");
            logger.error(fileName + " seems to be an invalid rt130 file.");
            return false;
        }
        RT130ToLocalSeismogram toSeismogram = new RT130ToLocalSeismogram(conn,
                                                                         ncFile,
                                                                         props,
                                                                         stationLocations);
        LocalSeismogramImpl[] seismogramArray = toSeismogram.ConvertRT130ToLocalSeismogram(seismogramDataPacketArray);
        Channel[] channel = toSeismogram.getChannels();
        // Check database for channels that match (with lat/long buffer)
        // If channel exists, use it. If not, use new channel.
        for(int i = 0; i < seismogramArray.length; i++) {
            Channel closeChannel = jdbcSeisFile.findCloseChannel(channel[i],
                                                                 new QuantityImpl(1,
                                                                                  UnitImpl.KILOMETER));
            if(closeChannel == null) {
                saveRefTekToDatabase(jdbcSeisFile,
                                     report,
                                     chanTable,
                                     timeTable,
                                     channel[i],
                                     seismogramArray[i].getBeginTime(),
                                     seismogramArray[i].getEndTime(),
                                     fileLoc);
            } else {
                jdbcSeisFile.setChannelBeginTimeToEarliest(closeChannel,
                                                           channel[i]);
                saveRefTekToDatabase(jdbcSeisFile,
                                     report,
                                     chanTable,
                                     timeTable,
                                     closeChannel,
                                     seismogramArray[i].getBeginTime(),
                                     seismogramArray[i].getEndTime(),
                                     fileLoc);
            }
        }
        return true;
    }

    private static boolean processSingleRefTekWithKnownChannel(JDBCSeismogramFiles jdbcSeisFile,
                                                               DatabasePopulationReport report,
                                                               Connection conn,
                                                               String fileLoc,
                                                               String fileName,
                                                               NCFile ncFile,
                                                               JDBCChannel chanTable,
                                                               JDBCTime timeTable,
                                                               Properties props,
                                                               Channel knownChannel,
                                                               Map stationLocations)
            throws IOException, SQLException, NotFound, ParseException {
        RT130FileReader toSeismogramDataPackets = new RT130FileReader(fileLoc,
                                                                      false);
        PacketType[] seismogramDataPacketArray = null;
        try {
            seismogramDataPacketArray = toSeismogramDataPackets.processRT130Data();
        } catch(RT130FormatException e) {
            report.addProblemFile(fileLoc, fileName
                    + " seems to be an invalid rt130 file.");
            logger.error(fileName + " seems to be an invalid rt130 file.");
            return false;
        }
        RT130ToLocalSeismogram toSeismogram = new RT130ToLocalSeismogram(conn,
                                                                         ncFile,
                                                                         props,
                                                                         stationLocations);
        LocalSeismogramImpl[] seismogramArray = toSeismogram.ConvertRT130ToLocalSeismogram(seismogramDataPacketArray);
        for(int i = 0; i < seismogramArray.length; i++) {
            saveRefTekToDatabase(jdbcSeisFile,
                                 report,
                                 chanTable,
                                 timeTable,
                                 knownChannel,
                                 seismogramArray[i].getBeginTime(),
                                 seismogramArray[i].getEndTime(),
                                 fileLoc);
        }
        return true;
    }

    private static boolean processSingleRefTekBatch(JDBCSeismogramFiles jdbcSeisFile,
                                                    DatabasePopulationReport report,
                                                    Connection conn,
                                                    String fileLoc,
                                                    String fileName,
                                                    NCFile ncFile,
                                                    JDBCChannel chanTable,
                                                    JDBCTime timeTable,
                                                    Properties props,
                                                    Map stationLocations)
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
                        + " seems to be an invalid rt130 file.");
                logger.error(fileName + " seems to be an invalid rt130 file.");
                return false;
            }
            datastreamToFileData.put(unitIdNumber + datastream, fileData[0]);
        }
        if(!datastreamToChannel.containsKey(unitIdNumber + datastream)
                && (!datastream.equals("0"))) {
            Channel[] newChannel = createChannels(ncFile,
                                                  unitIdNumber,
                                                  datastream,
                                                  props,
                                                  stationLocations);
            datastreamToChannel.put(unitIdNumber + datastream, newChannel);
        }
        if(fileName.endsWith("00000000")) {
            Channel[] channel = (Channel[])datastreamToChannel.get(unitIdNumber
                    + datastream);
            for(int i = 0; i < channel.length; i++) {
                processSingleRefTekWithKnownChannel(jdbcSeisFile,
                                                    report,
                                                    conn,
                                                    fileLoc,
                                                    fileName,
                                                    ncFile,
                                                    chanTable,
                                                    timeTable,
                                                    props,
                                                    channel[i],
                                                    stationLocations);
            }
            return true;
        } else {
            try {
                MicroSecondDate beginTime = FileNameParser.getBeginTime(yearAndDay,
                                                                        fileName);
                beginTime = LeapSecondApplier.applyLeapSecondCorrection(unitIdNumber,
                                                                        beginTime);
                TimeInterval lengthOfData = FileNameParser.getLengthOfData(fileName);
                MicroSecondDate endTime = beginTime.add(lengthOfData);
                Channel[] channel = (Channel[])datastreamToChannel.get(unitIdNumber
                        + datastream);
                for(int i = 0; i < channel.length; i++) {
                    saveRefTekToDatabase(jdbcSeisFile,
                                         report,
                                         chanTable,
                                         timeTable,
                                         channel[i],
                                         beginTime,
                                         endTime,
                                         file.getPath());
                }
                return true;
            } catch(RT130FormatException e) {
                report.addProblemFile(fileLoc, fileName
                        + " seems to be an invalid rt130 file.");
                logger.error(fileName + " seems to be an invalid rt130 file.");
                return false;
            }
        }
    }

    private static int getTimeDbId(MicroSecondDate date, JDBCTime timeTable)
            throws SQLException {
        Integer timeDbId = (Integer)timeToDbId.get(date);
        if(timeDbId == null) {
            timeDbId = new Integer(timeTable.put(date.getFissuresTime()));
            timeToDbId.put(date, timeDbId);
        }
        return timeDbId.intValue();
    }

    private static Map timeToDbId = new HashMap();

    private static int getChannelDbId(Channel channel, JDBCChannel chanTable)
            throws SQLException, NotFound {
        Integer channelDbId = (Integer)channelToDbId.get(channel);
        if(channelDbId == null) {
            channelDbId = new Integer(chanTable.put(channel));
            channelToDbId.put(channel, channelDbId);
        }
        return channelDbId.intValue();
    }

    private static Map channelToDbId = new HashMap();

    private static Channel[] createChannels(NCFile ncFile,
                                            String unitIdNumber,
                                            String datastream,
                                            Properties props, Map stationLocations) {
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

    private static void saveSacToDatabase(JDBCSeismogramFiles jdbcSeisFile,
                                          DatabasePopulationReport report,
                                          Channel chan,
                                          SeismogramAttrImpl seis,
                                          String fileLoc) throws SQLException {
        report.addSacSeismogram();
        jdbcSeisFile.saveSeismogramToDatabase(chan,
                                              seis,
                                              fileLoc,
                                              SeismogramFileTypes.SAC);
    }

    private static void saveMSeedToDatabase(JDBCSeismogramFiles jdbcSeisFile,
                                            DatabasePopulationReport report,
                                            SeismogramAttrImpl seis,
                                            String fileLoc) throws SQLException {
        report.addMSeedSeismogram();
        jdbcSeisFile.saveSeismogramToDatabase(seis.channel_id,
                                              seis,
                                              fileLoc,
                                              SeismogramFileTypes.MSEED);
    }

    private static void saveRefTekToDatabase(JDBCSeismogramFiles jdbcSeisFile,
                                             DatabasePopulationReport report,
                                             JDBCChannel chanTable,
                                             JDBCTime timeTable,
                                             Channel channel,
                                             MicroSecondDate beginTime,
                                             MicroSecondDate endTime,
                                             String fileLoc)
            throws SQLException, NotFound {
        report.addRefTekSeismogram(channel, beginTime, endTime);
        jdbcSeisFile.saveSeismogramToDatabase(getChannelDbId(channel, chanTable),
                                              getTimeDbId(beginTime, timeTable),
                                              getTimeDbId(endTime, timeTable),
                                              fileLoc,
                                              SeismogramFileTypes.RT_130);
    }

    private static Map datastreamToChannel = new HashMap();

    private static Map datastreamToFileData = new HashMap();

    private static final Logger logger = Logger.getLogger(PopulateDatabaseFromDirectory.class);
}
