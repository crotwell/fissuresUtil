package edu.sc.seis.fissuresUtil.rt130;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.database.seismogram.PopulationProperties;
import edu.sc.seis.fissuresUtil.database.seismogram.RT130Report;

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
        this.props = props;
        stationLocations = XYReader.read(new BufferedReader(new FileReader(xyFileLoc)));
        Map dataStreamToSampleRate = RT130ToLocalSeismogram.makeDataStreamToSampleRate(props, pp);
        netAttr = PopulationProperties.getNetworkAttr(props);
        rtFileReader = new RT130FileReader();
        NCReader reader = new NCReader(netAttr, stationLocations);
        reader.load(new FileInputStream(pp.getPath(NCFile.NC_FILE_LOC)));
        chanCreator = new DASChannelCreator(netAttr,
                                            new RT130SamplingFinder(rtFileReader),
                                            reader.getSites());
        toSeismogram = new RT130ToLocalSeismogram(chanCreator,
                                                  dataStreamToSampleRate);
        double nominalLengthOfData = Double.parseDouble(pp.getString("nominalLengthOfData"));
        acceptableLengthOfData = (nominalLengthOfData + (nominalLengthOfData * 0.05));
    }

    public boolean scan(String fileLoc, String fileName) throws IOException {
        if(fileLoc.endsWith("00000000")) {
            return read(fileLoc, fileName);
        }
        File file = new File(fileLoc);
        String unitIdNumber = getUnitId(file);
        MicroSecondDate beginTime = getBeginTime(file, unitIdNumber);
        TimeInterval lengthOfData;
        try {
            lengthOfData = FileNameParser.getLengthOfData(file.getName());
        } catch(RT130FormatException e) {
            report.addFileFormatException(fileLoc, e.getMessage());
            logger.error(e.getMessage());
            return false;
        }
        if(lengthOfData.value > acceptableLengthOfData) {
            String toBeReadMessage = fileLoc
                    + " indicates more data than in a regular rt130 file. The file will be read to determine its true length.";
            report.addMalformedFileNameException(fileLoc, toBeReadMessage);
            logger.error(toBeReadMessage);
            return read(fileLoc, file.getName());
        }
        Channel[] channel = chanCreator.create(unitIdNumber, beginTime, fileLoc);
        MicroSecondDate endTime = beginTime.add(lengthOfData);
        for(int i = 0; i < channel.length; i++) {
            addSeismogramToReport(channel[i], beginTime, endTime);
        }
        return true;
    }

    private MicroSecondDate getBeginTime(File file, String unitId) {
        MicroSecondDate begin = FileNameParser.getBeginTime(file.getParentFile()
                                                                    .getParentFile()
                                                                    .getParentFile()
                                                                    .getName(),
                                                            file.getName());
        return LeapSecondApplier.applyLeapSecondCorrection(unitId, begin);
    }

    public boolean read(String fileLoc, String fileName) throws IOException {
        File file = new File(fileLoc);
        String unitIdNumber = getUnitId(file);
        MicroSecondDate beginTime = getBeginTime(file, unitIdNumber);
        Channel[] channel = chanCreator.create(unitIdNumber, beginTime, fileLoc);
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

    private String getUnitId(File file) {
        return file.getParentFile().getParentFile().getName();
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
        LocalSeismogramImpl[] seismogramArray = toSeismogram.convert(seismogramDataPacketArray);
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

    private DASChannelCreator chanCreator;

    private RT130FileReader rtFileReader;

    private RT130ToLocalSeismogram toSeismogram;

    private List flags;

    private NCFile ncFile;

    private RT130Report report = new RT130Report();

    private Properties props;

    private Map stationLocations;

    private static final Logger logger = Logger.getLogger(RT130FileHandler.class);

    private double acceptableLengthOfData;
}
