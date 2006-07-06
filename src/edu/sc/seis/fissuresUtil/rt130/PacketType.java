package edu.sc.seis.fissuresUtil.rt130;

import java.io.DataInput;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import edu.iris.Fissures.IfTimeSeries.EncodedData;
import edu.iris.Fissures.model.MicroSecondDate;
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

    public PacketType(DataInput in, boolean processData) throws IOException,
            RT130FormatException {
        encoded_data = new EncodedData[0];
        this.readNextPacket(in, processData);
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
        this.begin_time_from_state_of_health_file = original.begin_time_from_state_of_health_file;
        this.latitude_ = original.latitude_;
        this.longitude_ = original.longitude_;
        this.elevation_ = original.elevation_;
        this.number_of_location_readings = original.number_of_location_readings;
        if(original.channel_name != null) {
            this.channel_name = new String[original.channel_name.length];
            System.arraycopy(original.channel_name,
                             0,
                             this.channel_name,
                             0,
                             original.channel_name.length);
        }
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

    public void readNextPacket(DataInput in, boolean processData)
            throws IOException, RT130FormatException {
        // Packet Type
        packetType = new String(this.readBytes(in, 2));
        // System.out.println("Packet Type: " + packetType);
        if(!(packetType.equals("AD") || packetType.equals("CD")
                || packetType.equals("DS") || packetType.equals("DT")
                || packetType.equals("EH") || packetType.equals("ET")
                || packetType.equals("OM") || packetType.equals("SC") || packetType.equals("SH"))) {
            throw new RT130FormatException("First two bytes of Packet Header were not formatted correctly, and do not refer to a valid Packet Type.");
        }
        // Experiment Number
        experimentNumber = BCDRead.toInt(this.readBytes(in, 1));
        // System.out.println("Experiement Number: " + experimentNumber);
        // Year
        year = BCDRead.toInt(this.readBytes(in, 1));
        // System.out.println("Year: " + year);
        // Unit ID Number
        unitIdNumber = HexRead.toString(this.readBytes(in, 2));
        // System.out.println("Unit ID Number: " + unitIdNumber);
        // Time
        String timeString = BCDRead.toString(this.readBytes(in, 6));
        // System.out.println("Time: " + timeString);
        time = this.stringToMicroSecondDate(timeString, year);
        begin_time_of_first_packet = time;
        // System.out.println("Micro Second Date Time: " + time.toString());
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
            this.channel_name = this.sCPP.channelName;
        } else if(packetType.equals("SH")) {
            this.sOHP = new StateOfHealthPacket(in);
        } else {
            throw new RT130FormatException("First two bytes of Packet Header were not formatted correctly, and do not refer to a valid Packet Type.");
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
            throw new RT130FormatException("Couldn't understand time string "
                    + timeString + ".  " + e.getMessage());
        }
    }

    private static DateFormat df = new SimpleDateFormat("yyDDDHHmmssSSS");
    static {
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    protected String packetType, unitIdNumber;

    protected MicroSecondDate time;

    public String[] channel_name;

    public MicroSecondDate begin_time_from_state_of_health_file;

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
}
