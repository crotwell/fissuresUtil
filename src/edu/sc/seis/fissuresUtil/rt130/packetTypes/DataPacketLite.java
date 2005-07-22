package edu.sc.seis.fissuresUtil.rt130.packetTypes;

import java.io.DataInput;
import java.io.IOException;
import edu.sc.seis.fissuresUtil.rt130.BCDRead;
import edu.sc.seis.fissuresUtil.rt130.HexRead;
import edu.sc.seis.fissuresUtil.rt130.RT130FormatException;
import edu.sc.seis.fissuresUtil.rt130.PacketType;

/**
 * @author fenner Created on Jul 21, 2005
 */
public class DataPacketLite extends PacketType {

    public DataPacketLite(DataPacketLite original) {
        if(original != null) {
            this.dataFormat = original.dataFormat;
            this.flags = original.flags;
            this.eventNumber = original.eventNumber;
            this.dataStreamNumber = original.dataStreamNumber;
            this.channelNumber = original.channelNumber;
            this.numberOfSamples = original.numberOfSamples;
        }
    }

    public DataPacketLite(DataInput in) throws IOException,
            RT130FormatException {
        this.read(in);
    }

    private void read(DataInput in) throws IOException, RT130FormatException {
        // Event Number
        eventNumber = BCDRead.toInt(this.readBytes(in, 2));
        // System.out.println(" Event Number: " + eventNumber);
        // Data Stream Number
        dataStreamNumber = BCDRead.toInt(this.readBytes(in, 1));
        // System.out.println(" Data Stream Number: " + dataStreamNumber);
        // Channel Number
        channelNumber = BCDRead.toInt(this.readBytes(in, 1));
        // System.out.println(" Channel Number: " + channelNumber);
        // Number Of Samples
        numberOfSamples = BCDRead.toInt(this.readBytes(in, 2));
        // System.out.println(" Number Of Samples: " + numberOfSamples);
        // Flags
        byte[] flagsArray = this.readBytes(in, 1);
        flags = flagsArray[0];
        // System.out.println(" Flags: " + Integer.toBinaryString(flags));
        // Data Format
        dataFormat = HexRead.toString(this.readBytes(in, 1));
        // System.out.println(" Data Format: " + dataFormat);
        if(dataFormat.equals("C0")) {
            // Filler
            in.skipBytes(40);
            // Data Frames 0-14
            in.skipBytes(960);
        } else {
            System.err.println("The data in this packet is not in compressed format. This file reader is not prepaired to read uncompressed seismogram data. Throwing a format exception even though this format is valid.");
            throw new RT130FormatException();
        }
    }

    private byte[] readBytes(DataInput in, int numBytes) throws IOException {
        byte[] seqBytes = new byte[numBytes];
        in.readFully(seqBytes);
        return seqBytes;
    }

    public String dataFormat;

    public byte flags;

    public int eventNumber, dataStreamNumber, channelNumber, numberOfSamples;
}
