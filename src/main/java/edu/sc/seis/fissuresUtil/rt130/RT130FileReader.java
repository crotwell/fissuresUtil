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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.iris.Fissures.IfTimeSeries.EncodedData;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;

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

    public PacketType[] processRT130Data(String dataFileLoc,
                                         boolean processData,
                                         MicroSecondTimeRange fileTimeWindow)
            throws RT130FormatException, IOException {
        logger.info("reading: "+dataFileLoc);
        this.dataFileLoc = dataFileLoc;
        File file = new File(this.dataFileLoc);
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);
        this.seismogramDataInputStream = dis;
        this.processData = processData;
        return readEntireDataFile(fileTimeWindow);
    }

    private PacketType[] readEntireDataFile(MicroSecondTimeRange fileTimeWindow)
            throws RT130FormatException, IOException {
        boolean done = false;
        List seismogramList = new ArrayList();
        PacketType nextPacket = new PacketType();
        PacketType header = new PacketType();
        Map seismogramData = new HashMap();
        boolean haveFile = false;
        while(!haveFile) {
            try {
                nextPacket = new PacketType(this.seismogramDataInputStream,
                                            this.processData,
                                            fileTimeWindow);
                haveFile = true;
            } catch(EOFException e) {
                logger.error("End of file reached before any data processing was done. "
                        + "The file likely contains no data. "
                        + "PacketType creation failed.");
                throw new RT130FormatException("  End of file reached before any data processing was done. "
                        + "The file likely contains no data. "
                        + "PacketType creation failed.");
            } catch(RT130BadPacketException e) {
                // Skip bad packet.
            }
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
                append(seismogramData, i, nextPacket, seismogramList);
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
                haveFile = false;
                while(!haveFile && !done) {
                    try {
                        nextPacket = new PacketType(this.seismogramDataInputStream,
                                                    this.processData,
                                                    fileTimeWindow);
                        haveFile = true;
                    } catch(EOFException e) {
                        logger.warn("End of file reached before Event Trailer Packet was read."
                                + " The file likely contains an incomplete seismogram."
                                + " Local seismogram creation was not disturbed.");
                        for(Integer j = new Integer(0); seismogramData.containsKey(j); j = new Integer(j.intValue() + 1)) {
                            seismogramList.add(finalizeSeismogramCreation((PacketType)seismogramData.get(j),
                                                                          false));
                        }
                        done = true;
                    } catch(RT130BadPacketException e) {
                        // Skip bad packet.
                    }
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
            seismogramData.setBeginTimeOfSeismogram(dataPacket.begin_time_of_first_packet);
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
                                                  boolean gapInData)
            throws RT130FormatException {
        if(gapInData) {
            logger.warn("The data collecting unit stopped recording data on channel "
                    + seismogramData.channel_number
                    + " for a period of time longer than allowed."
                    + " A new seismogram will be created to hold the rest of the data.");
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
        seismogramData.setBeginTimeOfSeismogram(nextPacket.time);
        seismogramData.end_time_of_last_packet = nextPacket.time;
        seismogramData.number_of_samples = 0;
        seismogramData.encoded_data = new EncodedData[0];
    }

    private void append(Map seismogramData,
                        Integer i,
                        PacketType nextPacket,
                        List seismogramList) throws RT130FormatException {
        if(seismogramIsContinuos((PacketType)seismogramData.get(i), nextPacket)) {
            seismogramData.put(i,
                               Append.appendDataPacket((PacketType)seismogramData.get(i),
                                                       nextPacket,
                                                       this.processData));
        } else {
            seismogramList.add(finalizeSeismogramCreation((PacketType)seismogramData.get(i),
                                                          true));
            resetSeismogramData((PacketType)seismogramData.get(i), nextPacket);
            append(seismogramData, i, nextPacket, seismogramList);
        }
    }

    private static DateFormat df = new SimpleDateFormat("yyyyDDD");
    static {
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private boolean processData;

    private String dataFileLoc;

    private DataInput seismogramDataInputStream;

    PacketType firstDataPacketOfFirstFile;

    private static final Logger logger = LoggerFactory.getLogger(RT130FileReader.class);
}
