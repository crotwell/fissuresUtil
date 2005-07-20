package edu.sc.seis.fissuresUtil.rt130;

import java.io.DataInput;
import java.io.IOException;
import edu.iris.Fissures.IfTimeSeries.EncodedData;
import edu.iris.Fissures.model.ISOTime;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.rt130.packetTypes.*;

/**
 * @author fenner Created on Jun 14, 2005
 */
public class PacketType {

    public PacketType() {
        encoded_data = new EncodedData[0];
    }

    public PacketType(DataInput in) throws IOException,
            RT130FormatException {
        encoded_data = new EncodedData[0];
        this.readNextPacket(in);
    }

    public PacketType(PacketType original) {
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
        this.encoded_data = original.encoded_data;
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

    public void readNextPacket(DataInput in) throws IOException,
            RT130FormatException {
        // Packet Type
        packetType = new String(this.readBytes(in, 2));
        // System.out.println("Packet Type: " + packetType);
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
        time = this.stringToMicroSecondDate(timeString, (year + 2000));
        begin_time_of_first_packet = time;
        //System.out.println("Micro Second Date Time: " + time.toString());
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
            this.dP = new DataPacket(in);
            encoded_data = new EncodedData[1];
            encoded_data[0] = new EncodedData((short)10, this.dP.dataFrames, this.dP.numberOfSamples, false);
            channel_number = this.dP.channelNumber;
        } else if(packetType.equals("EH")) {
            this.eHP = new EventHeaderPacket(in);
            begin_time_of_seismogram = time;
            end_time_of_last_packet = time;
            sample_rate = Integer.valueOf(this.eHP.sampleRate.trim())
                    .intValue();
        } else if(packetType.equals("ET")) {
            this.eTP = new EventTrailerPacket(in);
        } else if(packetType.equals("OM")) {
            this.oMPP = new OperatingModeParameterPacket(in);
        } else if(packetType.equals("SC")) {
            this.sCPP = new StationChannelParameterPacket(in);
        } else if(packetType.equals("SH")) {
            this.sOHP = new StateOfHealthPacket(in);
        } else {
            System.err.println("First two bytes of Packet Header were not formatted correctly, and do not refer to a valid Packet Type.");
            throw new RT130FormatException();
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
        String fractionsOfSecond = "";
        String seconds = "";
        String minutes = "";
        String hours = "";
        String daysOfYearReversed = "";
        if(timeString.length() >= 1) {
            fractionsOfSecond = "" + timeString.charAt(timeString.length() - 3);
            fractionsOfSecond = fractionsOfSecond
                    + timeString.charAt(timeString.length() - 2);
            fractionsOfSecond = fractionsOfSecond
                    + timeString.charAt(timeString.length() - 1);
        }
        if(timeString.length() >= 5) {
            seconds = "" + timeString.charAt(timeString.length() - 5);
            seconds = seconds + timeString.charAt(timeString.length() - 4);
        }
        if(timeString.length() >= 7) {
            minutes = "" + timeString.charAt(timeString.length() - 7);
            minutes = minutes + timeString.charAt(timeString.length() - 6);
        }
        if(timeString.length() >= 9) {
            hours = "" + timeString.charAt(timeString.length() - 9);
            hours = hours + timeString.charAt(timeString.length() - 8);
        }
        if(timeString.length() >= 10) {
            daysOfYearReversed = ""
                    + timeString.charAt(timeString.length() - 10);
        }
        if(timeString.length() >= 11) {
            daysOfYearReversed = daysOfYearReversed
                    + timeString.charAt(timeString.length() - 11);
        }
        if(timeString.length() >= 12) {
            daysOfYearReversed = daysOfYearReversed
                    + timeString.charAt(timeString.length() - 12);
        }
        if(timeString.length() > 12 || timeString.length() < 9) {
            System.err.println("Cannot read time field of Packet Header.");
            throw new RT130FormatException();
        }
        String daysOfYear = "0";
        for(int i = daysOfYearReversed.length() - 1; i >= 0; i--) {
            daysOfYear = daysOfYear.concat("" + daysOfYearReversed.charAt(i));
        }
        int daysOfYearInt = Integer.valueOf(daysOfYear).intValue();
        int hoursInt = Integer.valueOf(hours).intValue();
        int minutesInt = Integer.valueOf(minutes).intValue();
        seconds = seconds.concat(".");
        seconds = seconds.concat(fractionsOfSecond);
        float secondsInt = Float.valueOf(seconds).floatValue();
        ISOTime isoTime = new ISOTime(yearInt,
                                      daysOfYearInt,
                                      hoursInt,
                                      minutesInt,
                                      secondsInt);
        return isoTime.getDate();
    }

    protected String packetType, unitIdNumber;

    protected MicroSecondDate time;

    public MicroSecondDate begin_time_of_seismogram,
            begin_time_of_first_packet, end_time_of_last_packet;

    protected int experimentNumber, year, byteCount, packetSequence;

    public int number_of_samples, sample_rate, channel_number,
            data_stream_number;

    
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