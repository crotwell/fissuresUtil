package edu.sc.seis.fissuresUtil.rt130;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
import edu.iris.Fissures.IfNetwork.NetworkAttr;
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
import edu.iris.Fissures.network.SiteImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.database.seismogram.RT130Report;
import edu.sc.seis.fissuresUtil.database.seismogram.PopulationProperties;

public class RT130FileHandler {

    public RT130FileHandler(Properties props, List rt130FileHandlerFlags)
            throws FileNotFoundException, IOException, ParseException {
        PropParser pp = new PropParser(props);
        flags = rt130FileHandlerFlags;
        checkFlagsForIncompatibleSettings();
        LeapSecondApplier.addLeapSeconds(pp.getPath(LeapSecondApplier.LEAP_SECOND_FILE));
        LeapSecondApplier.addCorrections(pp.getPath(LeapSecondApplier.POWER_UP_TIMES));
        ncFile = new NCFile(pp.getPath(NCFile.NC_FILE_LOC));
        logger.debug("NC file location: " + ncFile.getCanonicalPath());
        String xyFileLoc = pp.getPath(XYReader.XY_FILE_LOC);
        logger.debug("XY file location: " + xyFileLoc);
        this.report = new RT130Report();
        this.propParser = pp;
        this.props = props;
        stationLocations = XYReader.read(new BufferedReader(new FileReader(xyFileLoc)));
        Map dataStreamToSampleRate = new HashMap();
        for(int i = 1; i < 7; i++) {
            if(props.containsKey(RT130ToLocalSeismogram.DATA_STREAM + i)) {
                dataStreamToSampleRate.put(new Integer(i - 1),
                                           new Integer(pp.getInt(RT130ToLocalSeismogram.DATA_STREAM
                                                   + i)));
            }
        }
        netAttr = PopulationProperties.getNetworkAttr(props);
        toSeismogram = new RT130ToLocalSeismogram(ncFile,
                                                  stationLocations,
                                                  dataStreamToSampleRate,
                                                  netAttr);
        rtFileReader = new RT130FileReader();
    }

    public boolean processSingleRefTekScan(String fileLoc, String fileName)
            throws IOException {
        File file = new File(fileLoc);
        String unitIdNumber = file.getParentFile().getParentFile().getName();
        String datastream = file.getParentFile().getName();
        if(!datastreamToFileData.containsKey(unitIdNumber + datastream)) {
            PacketType[] fileData;
            try {
                fileData = rtFileReader.processRT130Data(fileLoc, false);
            } catch(RT130FormatException e) {
                report.addFileFormatException(fileLoc, fileName
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
            Channel[] newChannel = createChannels(unitIdNumber,
                                                  datastream,
                                                  fileLoc);
            datastreamToChannel.put(unitIdNumber + datastream, newChannel);
        }
        if(fileName.endsWith("00000000")) {
            return processSingleRefTekFull(fileLoc, fileName);
        } else {
            String yearAndDay = file.getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getName();
            try {
                MicroSecondDate beginTime = FileNameParser.getBeginTime(yearAndDay,
                                                                        fileName);
                beginTime = LeapSecondApplier.applyLeapSecondCorrection(unitIdNumber,
                                                                        beginTime);
                TimeInterval lengthOfData = FileNameParser.getLengthOfData(fileName);
                double nominalLengthOfData = Double.valueOf(propParser.getString("nominalLengthOfData"))
                        .doubleValue();
                if(lengthOfData.value > (nominalLengthOfData + (nominalLengthOfData * 0.05))) {
                    report.addMalformedFileNameException(fileLoc,
                                                         fileName
                                                                 + " seems to be an invalid rt130 file name. The file will be read to determine its true length.");
                    logger.error(fileName
                            + " seems to be an invalid rt130 file name. The file was read to determine its true length.");
                    return processSingleRefTekFull(fileLoc, fileName);
                } else {
                    MicroSecondDate endTime = beginTime.add(lengthOfData);
                    Channel[] channel = (Channel[])datastreamToChannel.get(unitIdNumber
                            + datastream);
                    for(int i = 0; i < channel.length; i++) {
                        addSeismogramToReport(channel[i], beginTime, endTime);
                    }
                    return true;
                }
            } catch(RT130FormatException e) {
                report.addFileFormatException(fileLoc, fileName
                        + " seems to be an invalid rt130 file." + "\n"
                        + e.getMessage());
                logger.error(fileName + " seems to be an invalid rt130 file."
                        + "\n" + e.getMessage());
                return false;
            }
        }
    }

    public boolean processSingleRefTekFull(String fileLoc, String fileName)
            throws IOException {
        File file = new File(fileLoc);
        String unitIdNumber = file.getParentFile().getParentFile().getName();
        String datastream = file.getParentFile().getName();
        if(!datastreamToFileData.containsKey(unitIdNumber + datastream)) {
            PacketType[] fileData;
            try {
                fileData = rtFileReader.processRT130Data(fileLoc, false);
            } catch(RT130FormatException e) {
                report.addFileFormatException(fileLoc, fileName
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
            Channel[] newChannel = createChannels(unitIdNumber,
                                                  datastream,
                                                  fileLoc);
            datastreamToChannel.put(unitIdNumber + datastream, newChannel);
        }
        Channel[] channel = (Channel[])datastreamToChannel.get(unitIdNumber
                + datastream);
        TimeInterval totalSeismogramTime = new TimeInterval(0,
                                                            UnitImpl.MILLISECOND);
        for(int i = 0; i < channel.length; i++) {
            TimeInterval newSeismogramTime = processSingleRefTekWithKnownChannel(fileLoc,
                                                                                 fileName,
                                                                                 channel[i],
                                                                                 unitIdNumber);
            totalSeismogramTime = totalSeismogramTime.add(newSeismogramTime);
        }
        if(flags.contains(RT130FileHandlerFlag.FULL)) {
            TimeInterval lengthOfDataFromFileName = null;
            try {
                lengthOfDataFromFileName = FileNameParser.getLengthOfData(fileName);
            } catch(RT130FormatException e) {
                report.addFileFormatException(fileLoc, fileName
                        + " seems to be an invalid rt130 file." + "\n"
                        + e.getMessage());
                logger.error(fileName + " seems to be an invalid rt130 file."
                        + "\n" + e.getMessage());
                return false;
            }
            if(lengthOfDataFromFileName.value != (totalSeismogramTime.value / (toSeismogram.getChannels().length * channel.length))) {
                report.addMalformedFileNameException(fileLoc,
                                                     fileName
                                                             + " seems to be an invalid rt130 file name."
                                                             + " The length of data described in the file"
                                                             + " name does not match the length of data in the file.");
                logger.error(fileName
                        + " seems to be an invalid rt130 file name."
                        + " The length of data described in the file"
                        + " name does not match the length of data in the file.");
            }
        }
        return true;
    }

    private Channel[] createChannels(String unitIdNumber,
                                     String datastream,
                                     String fileLoc) {
        String stationCode = ncFile.getUnitName(((PacketType)(datastreamToFileData.get(unitIdNumber
                                                        + datastream))).begin_time_from_first_data_file,
                                                unitIdNumber);
        Time networkBeginTime = ncFile.network_begin_time.getFissuresTime();
        Time channelBeginTime = networkBeginTime;
        NetworkId networkId = netAttr.get_id();
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
                    + "The location used for the unit will be a lat/long of 0/0.");
        }
        StationImpl station = new StationImpl(stationId,
                                              "",
                                              location,
                                              effectiveChannelTime,
                                              "",
                                              "",
                                              "",
                                              netAttr);
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

    private TimeInterval processSingleRefTekWithKnownChannel(String fileLoc,
                                                             String fileName,
                                                             Channel knownChannel,
                                                             String unitIdNumber)
            throws IOException {
        TimeInterval totalSeismogramTime = new TimeInterval(0,
                                                            UnitImpl.MILLISECOND);
        PacketType[] seismogramDataPacketArray = null;
        try {
            seismogramDataPacketArray = rtFileReader.processRT130Data(fileLoc,
                                                                      false);
        } catch(RT130FormatException e) {
            report.addFileFormatException(fileLoc, fileName
                    + " seems to be an invalid rt130 file." + "\n"
                    + e.getMessage());
            logger.error(fileName + " seems to be an invalid rt130 file."
                    + "\n" + e.getMessage());
            return totalSeismogramTime;
        }
        LocalSeismogramImpl[] seismogramArray = toSeismogram.ConvertRT130ToLocalSeismogram(seismogramDataPacketArray);
        for(int i = 0; i < seismogramArray.length; i++) {
            // Add one sample period to end time on purpose.
            addSeismogramToReport(knownChannel,
                                  seismogramArray[i].getBeginTime(),
                                  seismogramArray[i].getEndTime()
                                          .add(seismogramArray[i].getSampling()
                                                  .getPeriod()));
            if(flags.contains(RT130FileHandlerFlag.FULL)) {
                // Add one sample period to end time on purpose.
                TimeInterval seismogramTime = new TimeInterval(seismogramArray[i].getBeginTime(),
                                                               seismogramArray[i].getEndTime()
                                                                       .add(seismogramArray[i].getSampling()
                                                                               .getPeriod()));
                totalSeismogramTime = totalSeismogramTime.add(seismogramTime);
            }
        }
        return totalSeismogramTime;
    }

    private void addSeismogramToReport(Channel channel,
                                       MicroSecondDate beginTime,
                                       MicroSecondDate endTime) {
        report.addRefTekSeismogram(channel, beginTime, endTime);
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

    NetworkAttr netAttr;

    private RT130FileReader rtFileReader;

    private RT130ToLocalSeismogram toSeismogram;

    private List flags;

    private NCFile ncFile;

    private RT130Report report;

    private PropParser propParser;

    private Properties props;

    private Map stationLocations;

    private Map datastreamToChannel = new HashMap();

    private Map datastreamToFileData = new HashMap();

    private static final Logger logger = Logger.getLogger(RT130FileHandler.class);
}
