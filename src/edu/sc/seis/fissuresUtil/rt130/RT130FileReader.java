package edu.sc.seis.fissuresUtil.rt130;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import edu.iris.Fissures.IfTimeSeries.EncodedData;
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

    public RT130FileReader(String dataFileLoc, boolean processData)
            throws FileNotFoundException {
        File file = new File(dataFileLoc);
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);
        this.seismogramDataInputStream = dis;
        File dataStream = new File(file.getParent());
        File unitId = new File(dataStream.getParent());
        String stateOfHealthFileLoc = unitId.getAbsolutePath() + "/0/SOH.RT";
        try {
            file = new File(stateOfHealthFileLoc);
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            dis = new DataInputStream(bis);
            this.stateOfHealthDataInputStream = dis;
        } catch(FileNotFoundException e) {
            stateOfHealthFileLoc = tryAlternateStateOfHealthFile(dataFileLoc);
            try {
                file = new File(stateOfHealthFileLoc);
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                dis = new DataInputStream(bis);
                this.stateOfHealthDataInputStream = dis;
            } catch(FileNotFoundException f) {
                System.err.println("Missing Sate of Health File.");
                e.printStackTrace();
                f.printStackTrace();
            }
        }
        this.stateOfHealthDataInputStream = dis;
        this.processData = processData;
    }

    private String tryAlternateStateOfHealthFile(String dataFileLoc) {
        File dataFile = new File(dataFileLoc);
        File dataStreamDirectory = new File(dataFile.getParent());
        File unitIdDirectory = new File(dataStreamDirectory.getParent());
        File dateDirectory = new File(unitIdDirectory.getParent());
        String dateDirectoryLoc = dateDirectory.getAbsolutePath();
        char[] dateDirectoryLocChars = dateDirectoryLoc.toCharArray();
        char[] yearChars = new char[4];
        char[] dayChars = new char[3];
        for(int i = 0; i < yearChars.length; i++) {
            yearChars[i] = dateDirectoryLocChars[dateDirectoryLocChars.length
                    - (yearChars.length + dayChars.length) + i];
        }
        for(int i = 0; i < dayChars.length; i++) {
            dayChars[i] = dateDirectoryLocChars[dateDirectoryLocChars.length
                    - dayChars.length + i];
        }
        String yearString = new String(yearChars);
        String dayString = new String(dayChars);
        int yearInt = Integer.parseInt(yearString);
        int dayInt = Integer.parseInt(dayString);
        dayInt++;
        if(dayInt > 365) {
            yearInt++;
            dayString = "001";
        } else {
            dayString = dayInt + "";
        }
        yearString = yearInt + "";
        if(dayString.length() == 1) {
            dayString = "00" + dayString;
        }
        if(dayString.length() == 2) {
            dayString = "0" + dayString;
        }
        String dateString = yearString + dayString;
        char[] dateChars = dateString.toCharArray();
        for(int i = 0; i < dateChars.length; i++) {
            dateDirectoryLocChars[dateDirectoryLocChars.length
                    - dateChars.length + i] = dateChars[i];
        }
        String stateOfHealthFileLoc = new String(dateDirectoryLocChars) + "/"
                + unitIdDirectory.getName() + "/0/SOH.RT";
        return stateOfHealthFileLoc;
    }

    public void close() {
        this.seismogramDataInputStream = null;
        this.stateOfHealthDataInputStream = null;
    }

    public PacketType[] processRT130Data() throws RT130FormatException,
            IOException {
        return readEntireDataFile(readEntireStateOfHealthFile());
    }

    private PacketType readEntireStateOfHealthFile() throws IOException,
            RT130FormatException {
        boolean done = false;
        PacketType nextPacket = new PacketType();
        PacketType stateOfHealthData = new PacketType();
        try {
            nextPacket = new PacketType(this.stateOfHealthDataInputStream,
                                        this.processData);
        } catch(EOFException e) {
            System.err.println("End of file reached before any data processing was done.");
            System.err.println("The file likely contains no data.");
            System.err.println("PacketType creation failed.");
            done = true;
        }
        while(!done) {
            if(nextPacket.packetType.equals("DT")) {
                System.err.println("The given data file contains an unexpected Data Packet.");
                System.err.println("More than likely you are reading a data file.");
                return null;
            } else if(nextPacket.packetType.equals("EH")) {
                System.err.println("The given data file contains an unexpected Event Header Packet.");
                System.err.println("More than likely you are reading a data file.");
                return null;
            } else if(nextPacket.packetType.equals("ET")) {
                System.err.println("The given data file contains an unexpected Event Trailer Packet.");
                System.err.println("More than likely you are reading a data file.");
                return null;
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
            } else {
                System.err.println("The first two bytes of the Packet Header were not formatted");
                System.err.println("correctly, and do not refer to a valid Packet Type.");
                throw new RT130FormatException();
            }
            if(!done) {
                try {
                    nextPacket = new PacketType(this.stateOfHealthDataInputStream,
                                                this.processData);
                } catch(EOFException e) {
                    done = true;
                }
            }
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
            System.err.println("End of file reached before any data processing was done.");
            System.err.println("The file likely contains no data.");
            System.err.println("PacketType creation failed.");
            done = true;
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
                    resetSeismogramData((PacketType)seismogramData.get(j),
                                        nextPacket);
                }
                done = true;
            } else if(nextPacket.packetType.equals("AD")) {
                System.err.println("The given data file contains an unexpected Auxiliary Data Parameter Packet.");
                System.err.println("More than likely you are reading the State-of-Health file.");
                return null;
            } else if(nextPacket.packetType.equals("CD")) {
                System.err.println("The given data file contains an unexpected Calibration Parameter Packet.");
                System.err.println("More than likely you are reading the State-of-Health file.");
                return null;
            } else if(nextPacket.packetType.equals("DS")) {
                System.err.println("The given data file contains an unexpected Data Stream Parameter Packet.");
                System.err.println("More than likely you are reading the State-of-Health file.");
                return null;
            } else if(nextPacket.packetType.equals("OM")) {
                System.err.println("The given data file contains an unexpected Operating Mode Parameter Packet.");
                System.err.println("More than likely you are reading the State-of-Health file.");
                return null;
            } else if(nextPacket.packetType.equals("SH")) {
                System.err.println("The given data file contains an unexpected State-Of-Health Packet.");
                System.err.println("More than likely you are reading the State-of-Health file.");
                return null;
            } else if(nextPacket.packetType.equals("SC")) {
                System.err.println("The given data file contains an unexpected Station/Channel Parameter Packet.");
                System.err.println("More than likely you are reading the State-of-Health file.");
                return null;
            } else {
                System.err.println("The first two bytes of the Packet Header were not formatted");
                System.err.println("correctly, and do not refer to a valid Packet Type.");
                throw new RT130FormatException();
            }
            if(!done) {
                try {
                    nextPacket = new PacketType(this.seismogramDataInputStream,
                                                this.processData);
                } catch(EOFException e) {
                    System.err.println("End of file reached before Event Trailer Packet was read.");
                    System.err.println("The file likely contains an incomplete seismogram.");
                    System.err.println("Local seismogram creation was not disturbed.");
                    done = true;
                }
            }
        }
        return (PacketType[])seismogramList.toArray(new PacketType[seismogramList.size()]);
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
            seismogramData.sample_rate = 40;
            System.out.println();
            System.out.println("The Event Header Packet for channel "
                    + seismogramData.channel_number + " was missing "
                    + "from the beginning of the data file.");
            System.out.println("This rare occurance is handled by using "
                    + "the header information from the first "
                    + "data packet instead.");
            System.out.println("The only information unable to be recovered "
                    + "is the data sample rate. A sample rate of "
                    + seismogramData.sample_rate
                    + " samples per second will be assumed.");
            System.out.println();
        }
        if(seismogramData.end_time_of_last_packet.difference(dataPacket.begin_time_of_first_packet)
                .lessThan(sampleGapWithTolerance)) {
            value = true;
        }
        return value;
    }

    private PacketType finalizeSeismogramCreation(PacketType seismogramData,
                                                  PacketType stateOfHealthData,
                                                  boolean gapInData) {
        if(gapInData) {
            System.out.println("The data collecting unit stopped recording data on channel "
                    + seismogramData.channel_number);
            System.out.println("for a period of time longer than allowed.");
            System.out.println("A new seismogram will be created to hold the rest of the data.");
        }
        if(stateOfHealthData == null
                || stateOfHealthData.begin_time_from_state_of_health_file == null) {
            System.err.println("The begin time for the channel is not present in the state of health data.");
        } else {
            seismogramData.begin_time_from_state_of_health_file = stateOfHealthData.begin_time_from_state_of_health_file;
        }
        if(stateOfHealthData == null || stateOfHealthData.latitude_ == 0) {
            System.err.println("The latitude for the channel is not present in the state of health data.");
        } else {
            seismogramData.latitude_ = stateOfHealthData.latitude_;
        }
        if(stateOfHealthData == null || stateOfHealthData.longitude_ == 0) {
            System.err.println("The longitude for the channel is not present in the state of health data.");
        } else {
            seismogramData.longitude_ = stateOfHealthData.longitude_;
        }
        if(stateOfHealthData == null || stateOfHealthData.channel_name == null) {
            System.err.println("The channel name for the channel is not present in the state of health data.");
        } else {
            seismogramData.channel_name = stateOfHealthData.channel_name;
        }
        return seismogramData;
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

    private boolean processData;

    private DataInput seismogramDataInputStream, stateOfHealthDataInputStream;
}