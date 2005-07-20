package edu.sc.seis.fissuresUtil.rt130;

import edu.iris.Fissures.IfTimeSeries.EncodedData;
import edu.sc.seis.fissuresUtil.rt130.packetTypes.*;

/**
 * @author fenner Created on Jun 15, 2005
 */
public class Append {

    public static PacketType appendDataPacket(PacketType seismogramData,
                                              PacketType nextPacket) {
        // Update channel_number
        //
        // There is no need for this to happen every time,
        // but I am too lazy to figure out a different way
        // right now.
        seismogramData.channel_number = nextPacket.dP.channelNumber;
        // Update data_stream_number
        //
        // There is no need for this to happen every time,
        // but I am too lazy to figure out a different way
        // right now.
        seismogramData.data_stream_number = nextPacket.dP.dataStreamNumber;
        // Keep at total of the number of samples in a seismogram.
        seismogramData.number_of_samples = seismogramData.number_of_samples
                + nextPacket.dP.numberOfSamples;
        // Update begin_time_of_first_packet.
        seismogramData.begin_time_of_first_packet = nextPacket.begin_time_of_first_packet;
        // Update end_time_of_last_packet
        seismogramData.end_time_of_last_packet = nextPacket.end_time_of_last_packet;
        // Append the sample data frames.
        int newArraySize = seismogramData.encoded_data.length
                + nextPacket.encoded_data.length;
        EncodedData[] data = new EncodedData[newArraySize];
        int i = 0;
        for(; i < seismogramData.encoded_data.length; i++) {
            data[i] = seismogramData.encoded_data[i];
        }
        for(; i < newArraySize; i++) {
            data[i] = nextPacket.encoded_data[i
                    - seismogramData.encoded_data.length];
        }
        seismogramData.encoded_data = data;
        return seismogramData;
    }

    public static PacketType appendEventHeaderPacket(PacketType seismogramData,
                                                     PacketType nextPacket) {
        PacketType clone = new PacketType(nextPacket);
        return clone;
    }

    public static PacketType appendEventTrailerPacket(PacketType seismogramData,
                                                      PacketType nextPacket) {
        return seismogramData;
    }

    public static PacketType appendAuxiliaryDataParameterPacket(PacketType seismogramData,
                                                                AuxiliaryDataParameterPacket nextPacket) {
        return seismogramData;
    }

    public static PacketType appendCalibrationParameterPacket(PacketType seismogramData,
                                                              CalibrationParameterPacket nextPacket) {
        return seismogramData;
    }

    public static PacketType appendDataStreamParameterPacket(PacketType seismogramData,
                                                             DataStreamParameterPacket nextPacket) {
        return seismogramData;
    }

    public static PacketType appendOperatingModeParameterPacket(PacketType seismogramData,
                                                                OperatingModeParameterPacket nextPacket) {
        return seismogramData;
    }

    public static PacketType appendStateOfHealthPacket(PacketType seismogramData,
                                                       StateOfHealthPacket nextPacket) {
        return seismogramData;
    }

    public static PacketType appendStationChannelParameterPacket(PacketType seismogramData,
                                                                 StationChannelParameterPacket nextPacket) {
        return seismogramData;
    }
}
