package edu.sc.seis.fissuresUtil.rt130;

import edu.iris.Fissures.IfTimeSeries.EncodedData;
import edu.sc.seis.fissuresUtil.rt130.packetTypes.*;

/**
 * @author fenner Created on Jun 15, 2005
 */
public class AppendLite {

    public static PacketTypeLite appendDataPacket(PacketTypeLite seismogramData,
                                                  PacketTypeLite nextPacket) {
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
        // DO NOT append the sample data frames! That would defeat the purpose
        // of this class.
        return seismogramData;
    }

    public static PacketTypeLite appendEventHeaderPacket(PacketTypeLite seismogramData,
                                                         PacketTypeLite nextPacket) {
        PacketTypeLite clone = new PacketTypeLite(nextPacket);
        return clone;
    }

    public static PacketTypeLite appendEventTrailerPacket(PacketTypeLite seismogramData,
                                                          PacketTypeLite nextPacket) {
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
