package edu.sc.seis.fissuresUtil.rt130;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.apache.log4j.Logger;
import edu.iris.Fissures.IfTimeSeries.EncodedData;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;

/*
 * @author fenner Created on Jun 13, 2005
 * 
 * This class processes Ref Tek 130 files and returns an array of full or empty
 * PacketTypes depending on processData.
 * 
 * 
 */
public class RT130FileReader {

    public RT130FileReader() {}

    public PacketType[] processRT130Data(String dataFileLoc, boolean processData)
            throws RT130FormatException, IOException {
        this.dataFileLoc = dataFileLoc;
        File file = new File(this.dataFileLoc);
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);
        this.seismogramDataInputStream = dis;
        this.processData = processData;
        lowercaseSOH = false;
        curSOH = false;
        nextSOH = false;
        prevSOH = false;
        PacketType stateOfHealthData = getStateOfHealthData();
        return readEntireDataFile(stateOfHealthData);
    }

    private PacketType getStateOfHealthData() throws RT130FormatException,
            IOException {
        File file = new File(this.dataFileLoc);
        File dataStream = new File(file.getParent());
        File unitId = new File(dataStream.getParent());
        String unitIdDirectory = unitId.getAbsolutePath();
        if(unitIdDirectory.equals(this.unitIdDirectory)) {
            return stateOfHealthData;
        } else {
            this.unitIdDirectory = unitIdDirectory;
            stateOfHealthData = readEntireStateOfHealthFile();
            return stateOfHealthData;
        }
    }

    private String getStateOfHealthFileLoc() throws RT130FormatException {
        String stateOfHealthFileLoc = null;
        if(curSOH == false) {
            File file = new File(this.dataFileLoc);
            File dataStream = new File(file.getParent());
            File unitId = new File(dataStream.getParent());
            if(lowercaseSOH == false) {
                stateOfHealthFileLoc = unitId.getAbsolutePath() + "/0/SOH.RT";
                lowercaseSOH = true;
                return stateOfHealthFileLoc;
            }
            logger.debug("Trying current lowercase SOH file.");
            stateOfHealthFileLoc = unitId.getAbsolutePath() + "/0/soh.rt";
            curSOH = true;
            lowercaseSOH = false;
            return stateOfHealthFileLoc;
        } else if(nextSOH == false) {
            if(lowercaseSOH == false) {
                stateOfHealthFileLoc = getNextStateOfHealthFile();
                lowercaseSOH = true;
                return stateOfHealthFileLoc;
            }
            stateOfHealthFileLoc = getNextStateOfHealthFile();
            nextSOH = true;
            lowercaseSOH = false;
            return stateOfHealthFileLoc;
        } else if(prevSOH == false) {
            if(lowercaseSOH == false) {
                stateOfHealthFileLoc = getPreviousStateOfHealthFile();
                lowercaseSOH = true;
                return stateOfHealthFileLoc;
            }
            stateOfHealthFileLoc = getPreviousStateOfHealthFile();
            prevSOH = true;
            lowercaseSOH = false;
            return stateOfHealthFileLoc;
        } else {
            logger.error("Three different State Of Health files were tried, and none of them were found."
                    + "\n" + "The data file location is: \n" + this.dataFileLoc);
            throw new RT130FormatException("  Three different State Of Health files were tried, and none of them were found.");
        }
    }

    private String getNextStateOfHealthFile() throws RT130FormatException {
        nextSOH = true;
        File dataFile = new File(dataFileLoc);
        File dataStreamDirectory = new File(dataFile.getParent());
        File unitIdDirectory = new File(dataStreamDirectory.getParent());
        File dateDirectory = new File(unitIdDirectory.getParent());
        File baseDirectory = new File(dateDirectory.getParent());
        String baseDirectoryString = baseDirectory.getAbsolutePath();
        MicroSecondDate date = DirectoryNameParser.getTime(dateDirectory);
        MicroSecondDate newDate = date.add(new TimeInterval(1, UnitImpl.DAY));
        String dateString = df.format(newDate);
        String stateOfHealthFileLoc = null;
        if(lowercaseSOH == false) {
            logger.debug("Trying next SOH file.");
            stateOfHealthFileLoc = baseDirectoryString + "/" + dateString + "/"
                    + unitIdDirectory.getName() + "/0/SOH.RT";
        } else {
            logger.debug("Trying next lowercase SOH file.");
            stateOfHealthFileLoc = baseDirectoryString + "/" + dateString + "/"
                    + unitIdDirectory.getName() + "/0/soh.rt";
        }
        return stateOfHealthFileLoc;
    }

    private String getPreviousStateOfHealthFile() throws RT130FormatException {
        prevSOH = true;
        File dataFile = new File(dataFileLoc);
        File dataStreamDirectory = new File(dataFile.getParent());
        File unitIdDirectory = new File(dataStreamDirectory.getParent());
        File dateDirectory = new File(unitIdDirectory.getParent());
        File baseDirectory = new File(dateDirectory.getParent());
        String baseDirectoryString = baseDirectory.getAbsolutePath();
        MicroSecondDate date = DirectoryNameParser.getTime(dateDirectory);
        MicroSecondDate newDate = date.subtract(new TimeInterval(1,
                                                                 UnitImpl.DAY));
        String dateString = df.format(newDate);
        String stateOfHealthFileLoc = null;
        if(lowercaseSOH == false) {
            logger.debug("Trying previous SOH file.");
            stateOfHealthFileLoc = baseDirectoryString + "/" + dateString + "/"
                    + unitIdDirectory.getName() + "/0/SOH.RT";
        } else {
            logger.debug("Trying previous lowercase SOH file.");
            stateOfHealthFileLoc = baseDirectoryString + "/" + dateString + "/"
                    + unitIdDirectory.getName() + "/0/soh.rt";
        }
        return stateOfHealthFileLoc;
    }

    private PacketType readEntireStateOfHealthFile() throws IOException,
            RT130FormatException {
        DataInputStream stateOfHealthDataInputStream = null;
        String stateOfHealthFileLoc = null;
        try {
            stateOfHealthFileLoc = getStateOfHealthFileLoc();
            File file = new File(stateOfHealthFileLoc);
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            stateOfHealthDataInputStream = new DataInputStream(bis);
        } catch(IOException e) {
            return readEntireStateOfHealthFile();
        }
        boolean done = false;
        PacketType nextPacket = new PacketType();
        PacketType stateOfHealthData = new PacketType();
        try {
            nextPacket = new PacketType(stateOfHealthDataInputStream,
                                        this.processData);
        } catch(EOFException e) {
            logger.error("End of file reached before any data processing was done. "
                    + "The file likely contains no data. "
                    + "PacketType creation failed.");
            throw new RT130FormatException("  End of file reached before any data processing was done. "
                    + "The file likely contains no data. "
                    + "PacketType creation failed.");
        }
        while(!done) {
            if(nextPacket.packetType.equals("DT")) {
                logger.error("The given data file contains an unexpected Data Packet. "
                        + "More than likely you are reading a data file.");
                throw new RT130FormatException("  The given data file contains an unexpected Data Packet. "
                        + "More than likely you are reading a data file.");
            } else if(nextPacket.packetType.equals("EH")) {
                logger.error("The given data file contains an unexpected Event Header Packet. "
                        + "More than likely you are reading a data file.");
                throw new RT130FormatException("  The given data file contains an unexpected Event Header Packet. "
                        + "More than likely you are reading a data file.");
            } else if(nextPacket.packetType.equals("ET")) {
                logger.error("The given data file contains an unexpected Event Trailer Packet. "
                        + "More than likely you are reading a data file.");
                throw new RT130FormatException("  The given data file contains an unexpected Event Trailer Packet. "
                        + "More than likely you are reading a data file.");
            } else if(nextPacket.packetType.equals("AD")) {
                stateOfHealthData = Append.appendAuxiliaryDataParameterPacket(stateOfHealthData,
                                                                              nextPacket);
            } else if(nextPacket.packetType.equals("CD")) {
                stateOfHealthData = Append.appendCalibrationParameterPacket(stateOfHealthData,
                                                                            nextPacket);
            } else if(nextPacket.packetType.equals("DS")) {
                stateOfHealthData = Append.appendDataStreamParameterPacket(stateOfHealthData,
                                                                           nextPacket);
            } else if(nextPacket.packetType.equals("OM")) {
                stateOfHealthData = Append.appendOperatingModeParameterPacket(stateOfHealthData,
                                                                              nextPacket);
            } else if(nextPacket.packetType.equals("SH")) {
                stateOfHealthData = Append.appendStateOfHealthPacket(stateOfHealthData,
                                                                     nextPacket);
            } else if(nextPacket.packetType.equals("SC")) {
                stateOfHealthData = Append.appendStationChannelParameterPacket(stateOfHealthData,
                                                                               nextPacket);
            } else if(nextPacket.packetType.equals("FD")) {
                // Do nothing. FD packets are the suck.
            } else {
                logger.error("The first two bytes of the Packet Header were not formatted "
                        + "correctly, and do not refer to a valid Packet Type.");
                throw new RT130FormatException("  The first two bytes of the Packet Header were not formatted "
                        + "correctly, and do not refer to a valid Packet Type.");
            }
            if(!done) {
                try {
                    nextPacket = new PacketType(stateOfHealthDataInputStream,
                                                this.processData);
                } catch(EOFException e) {
                    done = true;
                }
            }
        }
        if(stateOfHealthData.channel_name == null) {
            logger.error("The State Of Health file contained no Auxiliary Data Parameter Packets"
                    + "\n"
                    + "The State Of Health file location is: \n"
                    + stateOfHealthFileLoc
                    + "\n"
                    + "Used \"lowercase\" State Of Health File: "
                    + lowercaseSOH
                    + "\n"
                    + "Used \"current\" State Of Health File: "
                    + curSOH
                    + "\n"
                    + "Used \"next\" State Of Health File: "
                    + nextSOH
                    + "\n"
                    + "Used \"previous\" State Of HealthFile: " + prevSOH);
            return readEntireStateOfHealthFile();
        }
        return stateOfHealthData;
    }

    private PacketType[] readEntireDataFile(PacketType stateOfHealthData)
            throws RT130FormatException, IOException {
        boolean done = false;
        List seismogramList = new ArrayList();
        PacketType nextPacket = new PacketType();
        PacketType header = new PacketType();
        Map seismogramData = new HashMap();
        try {
            nextPacket = new PacketType(this.seismogramDataInputStream,
                                        this.processData);
        } catch(EOFException e) {
            logger.error("End of file reached before any data processing was done. "
                    + "The file likely contains no data. "
                    + "PacketType creation failed.");
            throw new RT130FormatException("  End of file reached before any data processing was done. "
                    + "The file likely contains no data. "
                    + "PacketType creation failed.");
        }
        while(!done) {
            if(nextPacket.packetType.equals("DT")) {
                Integer i = new Integer(nextPacket.dP.channelNumber);
                if(!seismogramData.containsKey(i)) {
                    seismogramData.put(i, new PacketType(header));
                }
                TimeInterval lengthOfData = new TimeInterval(((double)nextPacket.dP.numberOfSamples / (double)((PacketType)seismogramData.get(i)).sample_rate),
                                                             UnitImpl.SECOND);
                nextPacket.end_time_of_last_packet = nextPacket.begin_time_of_first_packet.add(lengthOfData);
                append(seismogramData,
                       i,
                       nextPacket,
                       seismogramList,
                       stateOfHealthData);
            } else if(nextPacket.packetType.equals("EH")) {
                seismogramData.put(new Integer(0),
                                   Append.appendEventHeaderPacket(new PacketType(),
                                                                  nextPacket));
                header = Append.appendEventHeaderPacket(new PacketType(),
                                                        nextPacket);
            } else if(nextPacket.packetType.equals("ET")) {
                for(Integer j = new Integer(0); seismogramData.containsKey(j); j = new Integer(j.intValue() + 1)) {
                    seismogramData.put(j,
                                       Append.appendEventTrailerPacket((PacketType)seismogramData.get(j),
                                                                       nextPacket));
                    seismogramList.add(finalizeSeismogramCreation((PacketType)seismogramData.get(j),
                                                                  stateOfHealthData,
                                                                  false));
                }
                done = true;
            } else if(nextPacket.packetType.equals("AD")) {
                logger.error("The given data file contains an unexpected Auxiliary Data Parameter Packet. "
                        + "More than likely you are reading a State-of-Health file.");
                throw new RT130FormatException("  The given data file contains an unexpected Auxiliary Data Parameter Packet. \n"
                        + "  More than likely you are reading a State-of-Health file.");
            } else if(nextPacket.packetType.equals("CD")) {
                logger.error("The given data file contains an unexpected Calibration Parameter Packet. "
                        + "More than likely you are reading a State-of-Health file.");
                throw new RT130FormatException("  The given data file contains an unexpected Calibration Parameter Packet. \n"
                        + "  More than likely you are reading a State-of-Health file.");
            } else if(nextPacket.packetType.equals("DS")) {
                logger.error("The given data file contains an unexpected Data Stream Parameter Packet. "
                        + "More than likely you are reading a State-of-Health file.");
                throw new RT130FormatException("  The given data file contains an unexpected Data Stream Parameter Packet. \n"
                        + "  More than likely you are reading a State-of-Health file.");
            } else if(nextPacket.packetType.equals("OM")) {
                logger.error("The given data file contains an unexpected Operating Mode Parameter Packet. "
                        + "More than likely you are reading a State-of-Health file.");
                throw new RT130FormatException("  The given data file contains an unexpected Operating Mode Parameter Packet. \n"
                        + "  More than likely you are reading a State-of-Health file.");
            } else if(nextPacket.packetType.equals("SH")) {
                logger.error("The given data file contains an unexpected State-Of-Health Packet. "
                        + "More than likely you are reading a State-of-Health file.");
                throw new RT130FormatException("  The given data file contains an unexpected State-Of-Health Packet. \n"
                        + "  More than likely you are reading a State-of-Health file.");
            } else if(nextPacket.packetType.equals("SC")) {
                logger.error("The given data file contains an unexpected Station/Channel Parameter Packet. "
                        + "More than likely you are reading a State-of-Health file.");
                throw new RT130FormatException("  The given data file contains an unexpected Station/Channel Parameter Packet. \n"
                        + "  More than likely you are reading a State-of-Health file.");
            } else {
                logger.error("The first two bytes of the Packet Header were not formatted "
                        + "correctly, and do not refer to a valid Packet Type.");
                throw new RT130FormatException("  The first two bytes of the Packet Header were not formatted "
                        + "correctly, and do not refer to a valid Packet Type.");
            }
            if(!done) {
                try {
                    nextPacket = new PacketType(this.seismogramDataInputStream,
                                                this.processData);
                } catch(EOFException e) {
                    logger.warn("End of file reached before Event Trailer Packet was read."
                            + " The file likely contains an incomplete seismogram."
                            + " Local seismogram creation was not disturbed.");
                    for(Integer j = new Integer(0); seismogramData.containsKey(j); j = new Integer(j.intValue() + 1)) {
                        seismogramList.add(finalizeSeismogramCreation((PacketType)seismogramData.get(j),
                                                                      stateOfHealthData,
                                                                      false));
                    }
                    done = true;
                }
            }
        }
        return (PacketType[])seismogramList.toArray(new PacketType[0]);
    }

    private boolean seismogramIsContinuos(PacketType seismogramData,
                                          PacketType dataPacket)
            throws RT130FormatException {
        boolean value = false;
        double tolerance = 1.1;
        TimeInterval sampleGapWithTolerance = new TimeInterval(((double)1 / (double)seismogramData.sample_rate)
                                                                       * tolerance,
                                                               UnitImpl.SECOND);
        if(seismogramData.end_time_of_last_packet == null
                && dataPacket.packetType.equals("DT")) {
            seismogramData = new PacketType(dataPacket);
            seismogramData.dP.dataFrames = new byte[0];
            seismogramData.begin_time_of_seismogram = dataPacket.begin_time_of_first_packet;
            seismogramData.end_time_of_last_packet = dataPacket.begin_time_of_first_packet;
            logger.warn("The Event Header Packet for channel "
                    + seismogramData.channel_number + " was missing "
                    + "from the beginning of the data file."
                    + " This rare occurance is handled by using "
                    + "the header information from the first "
                    + "data packet instead."
                    + " The only information unable to be recovered "
                    + "this way is the data sample rate. The sample rate from "
                    + "the Event Trailer Packet will be used.");
        }
        if(seismogramData.end_time_of_last_packet.difference(dataPacket.begin_time_of_first_packet)
                .lessThan(sampleGapWithTolerance)) {
            value = true;
        }
        return value;
    }

    private PacketType finalizeSeismogramCreation(PacketType seismogramData,
                                                  PacketType stateOfHealthData,
                                                  boolean gapInData)
            throws RT130FormatException {
        if(gapInData) {
            logger.warn("The data collecting unit stopped recording data on channel "
                    + seismogramData.channel_number
                    + " for a period of time longer than allowed."
                    + " A new seismogram will be created to hold the rest of the data.");
        }
        if(stateOfHealthData == null) {
            logger.error("The state of health file was not read correctly, and was returned as null.");
            throw new RT130FormatException("  The state of health file was not read correctly, and was returned as null.");
        } else {
            if(stateOfHealthData.begin_time_from_state_of_health_file == null) {
                logger.warn("The begin time for the channel is not present in the state of health data.");
            } else {
                seismogramData.begin_time_from_state_of_health_file = stateOfHealthData.begin_time_from_state_of_health_file;
            }
            if(stateOfHealthData.channel_name == null) {
                logger.error("Sould never happen.");
                throw new RT130FormatException("Should never happen.");
            } else {
                seismogramData.channel_name = stateOfHealthData.channel_name;
            }
        }
        if(seismogramData.sample_rate == 0) {
            logger.warn("The Event Header and Event Trailer Packets for channel "
                    + seismogramData.channel_number
                    + " were missing "
                    + "from the beginning and end of the data file."
                    + " This rare occurance is handled by using "
                    + "the header information from the first "
                    + "data packet instead."
                    + " The only information unable to be recovered "
                    + "is the data sample rate, which will be gathered from the props file."
                    + " The current sample rate is "
                    + seismogramData.sample_rate + " samples per second.");
        }
        return new PacketType(seismogramData);
    }

    private void resetSeismogramData(PacketType seismogramData,
                                     PacketType nextPacket) {
        seismogramData.begin_time_of_seismogram = nextPacket.time;
        seismogramData.end_time_of_last_packet = nextPacket.time;
        seismogramData.number_of_samples = 0;
        seismogramData.encoded_data = new EncodedData[0];
    }

    private void append(Map seismogramData,
                        Integer i,
                        PacketType nextPacket,
                        List seismogramList,
                        PacketType stateOfHealthData)
            throws RT130FormatException {
        if(seismogramIsContinuos((PacketType)seismogramData.get(i), nextPacket)) {
            seismogramData.put(i,
                               Append.appendDataPacket((PacketType)seismogramData.get(i),
                                                       nextPacket,
                                                       this.processData));
        } else {
            seismogramList.add(finalizeSeismogramCreation((PacketType)seismogramData.get(i),
                                                          stateOfHealthData,
                                                          true));
            resetSeismogramData((PacketType)seismogramData.get(i), nextPacket);
            append(seismogramData,
                   i,
                   nextPacket,
                   seismogramList,
                   stateOfHealthData);
        }
    }

    private static DateFormat df = new SimpleDateFormat("yyyyDDD");
    static {
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private boolean processData, nextSOH, prevSOH, curSOH, lowercaseSOH;

    private String unitIdDirectory, dataFileLoc;

    private DataInput seismogramDataInputStream;

    PacketType stateOfHealthData;

    private static final Logger logger = Logger.getLogger(RT130FileReader.class);
}
