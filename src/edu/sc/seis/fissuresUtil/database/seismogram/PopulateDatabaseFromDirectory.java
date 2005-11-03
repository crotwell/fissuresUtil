package edu.sc.seis.fissuresUtil.database.seismogram;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import org.apache.log4j.BasicConfigurator;
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
import edu.sc.seis.fissuresUtil.rt130.NCFile;
import edu.sc.seis.fissuresUtil.rt130.PacketType;
import edu.sc.seis.fissuresUtil.rt130.RT130FileReader;
import edu.sc.seis.fissuresUtil.rt130.RT130FormatException;
import edu.sc.seis.fissuresUtil.rt130.RT130ToLocalSeismogram;
import edu.sc.seis.fissuresUtil.sac.SacToFissures;
import edu.sc.seis.fissuresUtil.simple.Initializer;
import edu.sc.seis.fissuresUtil.xml.SeismogramFileTypes;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.MiniSeedRead;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.sac.SacTimeSeries;

public class PopulateDatabaseFromDirectory {

    public static void main(String[] args) throws FissuresException,
            IOException, SeedFormatException, SQLException, NotFound {
        BasicConfigurator.configure();
        Properties props = Initializer.loadProperties(args);
        ConnectionCreator connCreator = new ConnectionCreator(props);
        Connection conn = connCreator.createConnection();
        JDBCSeismogramFiles jdbcSeisFile = new JDBCSeismogramFiles(conn);
        JDBCChannel chanTable = new JDBCChannel(conn);
        JDBCTime timeTable = new JDBCTime(conn);
        boolean verbose = false;
        boolean finished = false;
        boolean batch = false;
        NCFile ncFile = null;
        for(int i = 1; i < args.length; i++) {
            if(args[i].equals("-v")) {
                verbose = true;
                System.out.println();
                System.out.println("/---------------Database Populator---");
                System.out.println();
                System.out.println("Verbose messages: ON");
            }
        }
        for(int i = 1; i < args.length - 1; i++) {
            if(args[i].equals("-nc")) {
                String ncFileLocation = args[i + 1];
                ncFile = new NCFile(ncFileLocation);
                if(verbose) {
                    File file = new File(ncFileLocation);
                    System.out.println("NC file location: "
                            + file.getCanonicalPath());
                }
            }
        }
        for(int i = 1; i < args.length - 1; i++) {
            if(verbose) {
                if(args[i].equals("-props")) {
                    String propFileLocation = args[i + 1];
                    File file = new File(propFileLocation);
                    System.out.println("Properties file location: "
                            + file.getCanonicalPath());
                }
            }
        }
        for(int i = 1; i < args.length - 1; i++) {
            if(verbose) {
                if(args[i].equals("-hsql")) {
                    String hsqlFileLocation = args[i + 1];
                    File file = new File(hsqlFileLocation);
                    System.out.println("HSQL properties file location: "
                            + file.getCanonicalPath());
                }
            }
        }
        for(int i = 1; i < args.length; i++) {
            if(args[i].equals("-rt")) {
                batch = true;
                if(verbose) {
                    System.out.println("Batch process of RT130 data: ON");
                }
            } else {
                System.out.println("Batch process of RT130 data: OFF");
            }
        }
        if(verbose) {
            System.out.println();
            System.out.println("\\------------------------------------");
            System.out.println();
        }
        if(args.length > 0) {
            String fileLoc = args[args.length - 1];
            File file = new File(fileLoc);
            if(file.isDirectory()) {
                finished = readEntireDirectory(file,
                                               verbose,
                                               conn,
                                               ncFile,
                                               jdbcSeisFile,
                                               chanTable,
                                               timeTable,
                                               props,
                                               batch);
            } else if(file.isFile()) {
                finished = readSingleFile(fileLoc,
                                          verbose,
                                          conn,
                                          ncFile,
                                          jdbcSeisFile,
                                          chanTable,
                                          timeTable,
                                          props,
                                          batch);
            } else {
                System.err.println("File: " + file
                        + " is not a file or a directory.");
            }
        } else {
            printHelp();
        }
        if(finished) {
            System.out.println();
            System.out.println("Database population complete.");
            System.out.println();
        } else {
            printHelp();
        }
    }

    private static boolean readSingleFile(String fileLoc,
                                          boolean verbose,
                                          Connection conn,
                                          NCFile ncFile,
                                          JDBCSeismogramFiles jdbcSeisFile,
                                          JDBCChannel chanTable,
                                          JDBCTime timeTable,
                                          Properties props,
                                          boolean batch) throws IOException,
            FissuresException, SeedFormatException, SQLException, NotFound {
        boolean finished = false;
        StringTokenizer t = new StringTokenizer(fileLoc, "/\\");
        String fileName = "";
        while(t.hasMoreTokens()) {
            fileName = t.nextToken();
        }
        if(fileName.length() == 18 && fileName.charAt(9) == '_') {
            if(batch) {
                finished = processSingleRefTekBatch(jdbcSeisFile,
                                                    conn,
                                                    fileLoc,
                                                    fileName,
                                                    verbose,
                                                    ncFile,
                                                    chanTable,
                                                    timeTable,
                                                    props);
            } else {
                finished = processSingleRefTek(jdbcSeisFile,
                                               conn,
                                               fileLoc,
                                               fileName,
                                               verbose,
                                               ncFile,
                                               chanTable,
                                               timeTable,
                                               props);
            }
        } else if(fileName.endsWith(".mseed")) {
            finished = processMSeed(jdbcSeisFile, fileLoc, fileName, verbose);
        } else if(fileName.endsWith(".sac")) {
            finished = processSac(jdbcSeisFile,
                                  fileLoc,
                                  fileName,
                                  verbose,
                                  props);
        } else if(verbose) {
            if(fileName.equals("SOH.RT")) {
                System.out.println("Ignoring file: " + fileName + ".");
            } else if(fileName.equals(".DS_Store")) {
                System.out.println("Ignoring Mac OS X file: " + fileName + ".");
            } else {
                System.out.println(fileName
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
                                               boolean verbose,
                                               Connection conn,
                                               NCFile ncFile,
                                               JDBCSeismogramFiles jdbcSeisFile,
                                               JDBCChannel chanTable,
                                               JDBCTime timeTable,
                                               Properties props,
                                               boolean batch)
            throws FissuresException, IOException, SeedFormatException,
            SQLException, NotFound {
        File[] files = baseDirectory.listFiles();
        if(files == null) {
            throw new IOException("Unable to get listing of directory: "
                    + baseDirectory);
        }
        for(int i = 0; i < files.length; i++) {
            if(files[i].isDirectory()) {
                readEntireDirectory(files[i],
                                    verbose,
                                    conn,
                                    ncFile,
                                    jdbcSeisFile,
                                    chanTable,
                                    timeTable,
                                    props,
                                    batch);
            } else {
                readSingleFile(files[i].getCanonicalPath(),
                               verbose,
                               conn,
                               ncFile,
                               jdbcSeisFile,
                               chanTable,
                               timeTable,
                               props,
                               batch);
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
        System.out.println("    -nc      | NC file location. Required for RT130 data");
        System.out.println("    -rt      | Turn on batch process of RT130 data");
        System.out.println("             |   No other types of data can be processed");
        System.out.println();
        System.out.println();
        System.out.println("Program finished before database population was completed.");
        System.out.println();
    }

    private static boolean processSac(JDBCSeismogramFiles jdbcSeisFile,
                                      String fileLoc,
                                      String fileName,
                                      boolean verbose,
                                      Properties props) throws IOException,
            FissuresException, SQLException {
        SacTimeSeries sacTime = new SacTimeSeries();
        try {
            sacTime.readHeader(new DataInputStream(new BufferedInputStream(new FileInputStream(fileLoc))));
        } catch(EOFException e) {
            System.err.println(fileName + " seems to be an invalid sac file.");
            return false;
        } catch(FileNotFoundException e) {
            System.err.println("Unable to find file " + fileName);
            return false;
        }
        SeismogramAttrImpl seis = SacToFissures.getSeismogramAttr(sacTime);
        Channel chan = SacToFissures.getChannel(sacTime);
        chan = PopulationProperties.fix(chan, props);
        jdbcSeisFile.saveSeismogramToDatabase(chan,
                                              seis,
                                              fileLoc,
                                              SeismogramFileTypes.SAC);
        if(verbose) {
            System.out.println("SAC file " + fileName
                    + " added to the database.");
        }
        return true;
    }

    private static boolean processMSeed(JDBCSeismogramFiles jdbcSeisFile,
                                        String fileLoc,
                                        String fileName,
                                        boolean verbose) throws IOException,
            SeedFormatException, FissuresException, SQLException {
        MiniSeedRead mseedRead = null;
        try {
            mseedRead = new MiniSeedRead(new DataInputStream(new BufferedInputStream(new FileInputStream(fileLoc))));
        } catch(EOFException e) {
            System.err.println(fileName
                    + " seems to  be an invalid mseed file.");
            return false;
        } catch(FileNotFoundException e) {
            System.err.println("Unable to find file " + fileName);
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
        jdbcSeisFile.saveSeismogramToDatabase(seis.channel_id,
                                              seis,
                                              fileLoc,
                                              SeismogramFileTypes.MSEED);
        if(verbose) {
            System.out.println("MSEED file " + fileName
                    + " added to the database.");
        }
        return true;
    }

    private static boolean processSingleRefTek(JDBCSeismogramFiles jdbcSeisFile,
                                               Connection conn,
                                               String fileLoc,
                                               String fileName,
                                               boolean verbose,
                                               NCFile ncFile,
                                               JDBCChannel chanTable,
                                               JDBCTime timeTable,
                                               Properties props)
            throws IOException, SQLException, NotFound {
        if(ncFile == null || conn == null) {
            if(verbose) {
                System.out.println("No NC (Network Configuration) file was specified.");
                System.out.println("The channel IDs created will not be correct.");
            }
        }
        RT130FileReader toSeismogramDataPackets = new RT130FileReader(fileLoc,
                                                                      true);
        PacketType[] seismogramDataPacketArray = null;
        try {
            seismogramDataPacketArray = toSeismogramDataPackets.processRT130Data();
        } catch(RT130FormatException e) {
            System.err.println(fileName + " seems to be an invalid rt130 file.");
            return false;
        }
        RT130ToLocalSeismogram toSeismogram = new RT130ToLocalSeismogram(conn,
                                                                         ncFile,
                                                                         props);
        LocalSeismogramImpl[] seismogramArray = toSeismogram.ConvertRT130ToLocalSeismogram(seismogramDataPacketArray);
        Channel[] channel = toSeismogram.getChannels();
        // Check database for channels that match (with lat/long buffer)
        // If channel exists, use it. If not, use new channel.
        for(int i = 0; i < seismogramArray.length; i++) {
            Channel closeChannel = jdbcSeisFile.findCloseChannel(channel[i],
                                                                 new QuantityImpl(1,
                                                                                  UnitImpl.KILOMETER));
            if(closeChannel == null) {
                jdbcSeisFile.saveSeismogramToDatabase(getChannelDbId(channel[i],
                                                                     chanTable),
                                                      getTimeDbId(seismogramArray[i].getBeginTime(),
                                                                  timeTable),
                                                      getTimeDbId(seismogramArray[i].getEndTime(),
                                                                  timeTable),
                                                      fileLoc,
                                                      SeismogramFileTypes.RT_130);
            } else {
                jdbcSeisFile.setChannelBeginTimeToEarliest(closeChannel,
                                                           channel[i]);
                jdbcSeisFile.saveSeismogramToDatabase(getChannelDbId(closeChannel,
                                                                     chanTable),
                                                      getTimeDbId(seismogramArray[i].getBeginTime(),
                                                                  timeTable),
                                                      getTimeDbId(seismogramArray[i].getEndTime(),
                                                                  timeTable),
                                                      fileLoc,
                                                      SeismogramFileTypes.RT_130);
            }
        }
        if(verbose) {
            System.out.println("RT130 file " + fileName
                    + " added to the database.");
        }
        return true;
    }

    private static boolean processSingleRefTekWithKnownChannel(JDBCSeismogramFiles jdbcSeisFile,
                                                               Connection conn,
                                                               String fileLoc,
                                                               String fileName,
                                                               boolean verbose,
                                                               NCFile ncFile,
                                                               JDBCChannel chanTable,
                                                               JDBCTime timeTable,
                                                               Properties props,
                                                               Channel knownChannel)
            throws IOException, SQLException, NotFound {
        RT130FileReader toSeismogramDataPackets = new RT130FileReader(fileLoc,
                                                                      false);
        PacketType[] seismogramDataPacketArray = null;
        try {
            seismogramDataPacketArray = toSeismogramDataPackets.processRT130Data();
        } catch(RT130FormatException e) {
            System.err.println(fileName + " seems to be an invalid rt130 file.");
            return false;
        }
        RT130ToLocalSeismogram toSeismogram = new RT130ToLocalSeismogram(conn,
                                                                         ncFile,
                                                                         props);
        LocalSeismogramImpl[] seismogramArray = toSeismogram.ConvertRT130ToLocalSeismogram(seismogramDataPacketArray);
        for(int i = 0; i < seismogramArray.length; i++) {
            jdbcSeisFile.saveSeismogramToDatabase(getChannelDbId(knownChannel,
                                                                 chanTable),
                                                  getTimeDbId(seismogramArray[i].getBeginTime(),
                                                              timeTable),
                                                  getTimeDbId(seismogramArray[i].getEndTime(),
                                                              timeTable),
                                                  fileLoc,
                                                  SeismogramFileTypes.RT_130);
        }
        if(verbose) {
            System.out.println("RT130 file " + fileName
                    + " added to the database.");
        }
        return true;
    }

    private static boolean processSingleRefTekBatch(JDBCSeismogramFiles jdbcSeisFile,
                                                    Connection conn,
                                                    String fileLoc,
                                                    String fileName,
                                                    boolean verbose,
                                                    NCFile ncFile,
                                                    JDBCChannel chanTable,
                                                    JDBCTime timeTable,
                                                    Properties props)
            throws IOException, SQLException, NotFound {
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
                System.err.println(fileName
                        + " seems to be an invalid rt130 file.");
                return false;
            }
            datastreamToFileData.put(unitIdNumber + datastream, fileData[0]);
        }
        if(!datastreamToChannel.containsKey(unitIdNumber + datastream)
                && (!datastream.equals("0"))) {
            Channel[] newChannel = createChannels(ncFile,
                                                  unitIdNumber,
                                                  datastream,
                                                  props);
            datastreamToChannel.put(unitIdNumber + datastream, newChannel);
        }
        if(fileName.endsWith("00000000")) {
            Channel[] channel = (Channel[])datastreamToChannel.get(unitIdNumber
                    + datastream);
            for(int i = 0; i < channel.length; i++) {
                processSingleRefTekWithKnownChannel(jdbcSeisFile,
                                                    conn,
                                                    fileLoc,
                                                    fileName,
                                                    verbose,
                                                    ncFile,
                                                    chanTable,
                                                    timeTable,
                                                    props,
                                                    channel[i]);
            }
            return true;
        } else {
            try {
                MicroSecondDate beginTime = FileNameParser.getBeginTime(yearAndDay,
                                                                        fileName);
                TimeInterval lengthOfData = FileNameParser.getLengthOfData(fileName);
                MicroSecondDate endTime = beginTime.add(lengthOfData);
                Channel[] channel = (Channel[])datastreamToChannel.get(unitIdNumber
                        + datastream);
                for(int i = 0; i < channel.length; i++) {
                    jdbcSeisFile.saveSeismogramToDatabase(getChannelDbId(channel[i],
                                                                         chanTable),
                                                          getTimeDbId(beginTime,
                                                                      timeTable),
                                                          getTimeDbId(endTime,
                                                                      timeTable),
                                                          file.getPath(),
                                                          SeismogramFileTypes.RT_130);
                }
                if(verbose) {
                    System.out.println("RT130 file " + fileName
                            + " added to the database.");
                }
                return true;
            } catch(RT130FormatException e) {
                System.err.println(fileName
                        + " seems to be an invalid rt130 file.");
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
                                            Properties props) {
        String stationCode = ncFile.getUnitName(((PacketType)(datastreamToFileData.get(unitIdNumber
                                                        + datastream))).begin_time_from_state_of_health_file,
                                                unitIdNumber);
        if(stationCode == null) {
            stationCode = unitIdNumber;
            System.err.println("/-------------------------");
            System.err.println("| Unit name for DAS unit number "
                    + unitIdNumber + " was not found in the NC file.");
            System.err.println("| The name \"" + unitIdNumber
                    + "\" will be used instead.");
            System.err.println("| To correct this entry in the database, please run UnitNameUpdater.");
            System.err.println("\\-------------------------");
        }
        String networkIdString = props.getProperty(PopulationProperties.NETWORK_REMAP
                + "XX");
        Time networkBeginTime = ncFile.network_begin_time.getFissuresTime();
        Time channelBeginTime = networkBeginTime;
        NetworkId networkId = new NetworkId(networkIdString, networkBeginTime);
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
        QuantityImpl elevation = new QuantityImpl(0, UnitImpl.METER);
        QuantityImpl depth = elevation;
        Location location = new Location(((PacketType)(datastreamToFileData.get(unitIdNumber
                                                 + datastream))).latitude_,
                                         ((PacketType)(datastreamToFileData.get(unitIdNumber
                                                 + datastream))).longitude_,
                                         elevation,
                                         depth,
                                         LocationType.from_int(0));
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

    private static Map datastreamToChannel = new HashMap();

    private static Map datastreamToFileData = new HashMap();
}
