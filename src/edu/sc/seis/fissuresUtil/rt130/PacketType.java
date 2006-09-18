package edu.sc.seis.fissuresUtil.rt130;

import java.io.DataInput;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.apache.log4j.Logger;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfTimeSeries.EncodedData;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.sc.seis.fissuresUtil.rt130.packetTypes.AuxiliaryDataParameterPacket;
import edu.sc.seis.fissuresUtil.rt130.packetTypes.CalibrationParameterPacket;
import edu.sc.seis.fissuresUtil.rt130.packetTypes.DataPacket;
import edu.sc.seis.fissuresUtil.rt130.packetTypes.DataStreamParameterPacket;
import edu.sc.seis.fissuresUtil.rt130.packetTypes.EventHeaderPacket;
import edu.sc.seis.fissuresUtil.rt130.packetTypes.EventTrailerPacket;
import edu.sc.seis.fissuresUtil.rt130.packetTypes.OperatingModeParameterPacket;
import edu.sc.seis.fissuresUtil.rt130.packetTypes.StateOfHealthPacket;
import edu.sc.seis.fissuresUtil.rt130.packetTypes.StationChannelParameterPacket;

/**
 * @author fenner Created on Jun 14, 2005
 */
public class PacketType {

    public PacketType() {
        encoded_data = new EncodedData[0];
    }

    public PacketType(DataInput in,
                      boolean processData,
                      TimeRange fileTimeWindow) throws IOException,
            RT130FormatException, RT130BadPacketException {
        encoded_data = new EncodedData[0];
        this.readNextPacket(in, processData, fileTimeWindow);
    }

    public PacketType(PacketType original) throws RT130FormatException {
        this.packetType = original.packetType;
        this.unitIdNumber = original.unitIdNumber;
        this.time = original.time;
        this.begin_time_of_seismogram = original.begin_time_of_seismogram;
        this.begin_time_of_first_packet = original.begin_time_of_first_packet;
        this.end_time_of_last_packet = original.end_time_of_last_packet;
        this.experimentNumber = original.experimentNumber;
        this.year = original.year;
        this.byteCount = original.byteCount;
        this.packetSequence = original.packetSequence;
        this.number_of_samples = original.number_of_samples;
        this.sample_rate = original.sample_rate;
        if(original.encoded_data != null) {
            this.encoded_data = new EncodedData[original.encoded_data.length];
            System.arraycopy(original.encoded_data,
                             0,
                             this.encoded_data,
                             0,
                             original.encoded_data.length);
        }
        this.begin_time_from_first_data_file = original.begin_time_from_first_data_file;
        this.latitude_ = original.latitude_;
        this.longitude_ = original.longitude_;
        this.elevation_ = original.elevation_;
        this.number_of_location_readings = original.number_of_location_readings;
        this.channel_number = original.channel_number;
        this.data_stream_number = original.data_stream_number;
        if(original.aDPP != null) {
            this.aDPP = new AuxiliaryDataParameterPacket(original.aDPP);
        }
        if(original.cPP != null) {
            this.cPP = new CalibrationParameterPacket(original.cPP);
        }
        if(original.dSPP != null) {
            this.dSPP = new DataStreamParameterPacket(original.dSPP);
        }
        if(original.dP != null) {
            this.dP = new DataPacket(original.dP);
        }
        if(original.eHP != null) {
            this.eHP = new EventHeaderPacket(original.eHP);
        }
        if(original.eTP != null) {
            this.eTP = new EventTrailerPacket(original.eTP);
        }
        if(original.oMPP != null) {
            this.oMPP = new OperatingModeParameterPacket(original.oMPP);
        }
        if(original.sCPP != null) {
            this.sCPP = new StationChannelParameterPacket(original.sCPP);
        }
        if(original.sOHP != null) {
            this.sOHP = new StateOfHealthPacket(original.sOHP);
        }
    }

    public void readNextPacket(DataInput in,
                               boolean processData,
                               TimeRange fileTimeWindow) throws IOException,
            RT130FormatException, RT130BadPacketException {
        Calendar beginTime = Calendar.getInstance();
        MicroSecondDate beginDate = new MicroSecondDate(fileTimeWindow.start_time);
        beginTime.setTime(beginDate);
        Calendar endTime = Calendar.getInstance();
        MicroSecondDate endDate = new MicroSecondDate(fileTimeWindow.end_time);
        endTime.setTime(endDate);
        // Packet Type
        packetType = new String(this.readBytes(in, 2));
        // logger.debug("Packet Type: " + packetType);
        if(!(packetType.equals("AD") || packetType.equals("CD")
                || packetType.equals("DS") || packetType.equals("DT")
                || packetType.equals("EH") || packetType.equals("ET")
                || packetType.equals("OM") || packetType.equals("SC")
                || packetType.equals("SH") || packetType.equals("FD"))) {
            logger.error("  The first two bytes of the Packet Header were not formatted "
                    + "correctly, and do not refer to a valid Packet Type. \n"
                    + "  First two bytes parse to: " + packetType);
            throw new RT130FormatException("  The first two bytes of the Packet Header were not formatted "
                    + "correctly, and do not refer to a valid Packet Type. \n"
                    + "  First two bytes parse to: " + packetType);
        }
        // Experiment Number
        experimentNumber = BCDRead.toInt(this.readBytes(in, 1));
        // System.out.println("Experiement Number: " + experimentNumber);
        // Year
        year = BCDRead.toInt(this.readBytes(in, 1));
        if((year + 2000) < beginTime.get(Calendar.YEAR)
                || (year + 2000) > endTime.get(Calendar.YEAR) + 1) {
            logger.warn("  The file contained a packet with an invalid year. \n"
                    + "The year parsed is: "
                    + (year + 2000)
                    + "\n The year "
                    + beginTime.get(Calendar.YEAR)
                    + " was parsed from the file structure, and will be used instead.");
            year = beginTime.get(Calendar.YEAR) - 2000;
        }
        // logger.debug("Year: " + year);
        // Unit ID Number
        unitIdNumber = HexRead.toString(this.readBytes(in, 2));
        // System.out.println("Unit ID Number: " + unitIdNumber);
        // Time
        String timeString = BCDRead.toString(this.readBytes(in, 6));
        // logger.debug("Time: " + timeString);
        time = this.stringToMicroSecondDate(timeString, year);
        if(packetType.equals("DT")
                && (time.before(beginDate) || time.after(endDate))) {
            logger.error("  The file contained a Data Packet with an invalid time. "
                    + "\n  The time parsed is: "
                    + time.toString()
                    + "\n  The time should be after: "
                    + beginDate.toString()
                    + "\n  The time should be before: "
                    + endDate.toString()
                    + "\n  The file will not be read.");
            throw new RT130BadPacketException("  The file contained a Data Packet with an invalid time. "
                    + "\n  The time parsed is: "
                    + time.toString()
                    + "\n  The time should be after: "
                    + beginDate.toString()
                    + "\n  The time should be before: "
                    + endDate.toString()
                    + "\n  The file will not be read.");
        }
        begin_time_of_first_packet = time;
        // logger.debug("Micro Second Date Time: " + time.toString());
        // Byte Count
        byteCount = BCDRead.toInt(this.readBytes(in, 2));
        // System.out.println("Byte Count: " + byteCount);
        // Packet Sequence
        packetSequence = BCDRead.toInt(this.readBytes(in, 2));
        // System.out.println("Packet Sequence: " + packetSequence);
        if(packetType.equals("AD")) {
            this.aDPP = new AuxiliaryDataParameterPacket(in);
        } else if(packetType.equals("CD")) {
            this.cPP = new CalibrationParameterPacket(in);
        } else if(packetType.equals("DS")) {
            this.dSPP = new DataStreamParameterPacket(in);
        } else if(packetType.equals("DT")) {
            this.dP = new DataPacket(in, processData);
            if(processData) {
                encoded_data = new EncodedData[1];
                encoded_data[0] = new EncodedData((short)10,
                                                  this.dP.dataFrames,
                                                  this.dP.numberOfSamples,
                                                  false);
            } else {
                encoded_data = new EncodedData[0];
            }
            channel_number = this.dP.channelNumber;
        } else if(packetType.equals("EH")) {
            this.eHP = new EventHeaderPacket(in);
            begin_time_of_seismogram = time;
            end_time_of_last_packet = time;
            String stringSampleRate = this.eHP.sampleRate.trim();
            if(stringSampleRate != null && !stringSampleRate.equals("")) {
                sample_rate = Integer.valueOf(stringSampleRate).intValue();
            }
        } else if(packetType.equals("ET")) {
            this.eTP = new EventTrailerPacket(in);
            sample_rate = Integer.valueOf(this.eTP.sampleRate.trim())
                    .intValue();
        } else if(packetType.equals("OM")) {
            this.oMPP = new OperatingModeParameterPacket(in);
        } else if(packetType.equals("SC")) {
            this.sCPP = new StationChannelParameterPacket(in);
        } else if(packetType.equals("SH")) {
            this.sOHP = new StateOfHealthPacket(in);
        } else if(packetType.equals("FD")) {
            // Just skip FD packet. Using DataStreamParameterPacket is good for
            // that.
            new DataStreamParameterPacket(in);
        } else {
            logger.error("  The first two bytes of the Packet Header were not formatted "
                    + "correctly, and do not refer to a valid Packet Type. \n"
                    + "  First two bytes parse to: " + packetType);
            throw new RT130FormatException("  The first two bytes of the Packet Header were not formatted "
                    + "correctly, and do not refer to a valid Packet Type. \n"
                    + "  First two bytes parse to: " + packetType);
        }
    }

    private byte[] readBytes(DataInput in, int numBytes) throws IOException {
        byte[] seqBytes = new byte[numBytes];
        in.readFully(seqBytes);
        return seqBytes;
    }

    private MicroSecondDate stringToMicroSecondDate(String timeString,
                                                    int yearInt)
            throws RT130FormatException {
        if(yearInt < 10) {
            timeString = "0" + yearInt + timeString;
        } else {
            timeString = yearInt + timeString;
        }
        try {
            Date d;
            synchronized(df) {
                d = df.parse(timeString);
            }
            return new MicroSecondDate(d);
        } catch(ParseException e) {
            throw new RT130FormatException("  Couldn't understand time string "
                    + timeString + ".  " + e.getMessage());
        }
    }

    private static DateFormat df = new SimpleDateFormat("yyDDDHHmmssSSS");
    static {
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    protected String packetType, unitIdNumber;

    protected MicroSecondDate time;

    public MicroSecondDate begin_time_from_first_data_file;

    protected MicroSecondDate begin_time_of_seismogram,
            begin_time_of_first_packet, end_time_of_last_packet;

    protected int experimentNumber, year, byteCount, packetSequence;

    public int sample_rate;

    protected int number_of_samples, channel_number, data_stream_number;

    protected int number_of_location_readings = -1;

    protected float latitude_, longitude_;

    protected double elevation_;

    public EncodedData[] encoded_data;

    public AuxiliaryDataParameterPacket aDPP;

    public CalibrationParameterPacket cPP;

    public DataStreamParameterPacket dSPP;

    public DataPacket dP;

    public EventHeaderPacket eHP;

    public EventTrailerPacket eTP;

    public OperatingModeParameterPacket oMPP;

    public StationChannelParameterPacket sCPP;

    public StateOfHealthPacket sOHP;

    private static final Logger logger = Logger.getLogger(PacketType.class);
}
