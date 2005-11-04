package edu.sc.seis.fissuresUtil.rt130;

import edu.iris.Fissures.IfTimeSeries.EncodedData;

/**
 * @author fenner Created on Jun 15, 2005
 */
public class Append {

    public static PacketType appendDataPacket(PacketType seismogramData,
                                              PacketType nextPacket,
                                              boolean processData) {
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
        // Update unitIdNumber
        //
        // This is needed in case the event header is not present.
        seismogramData.unitIdNumber = nextPacket.unitIdNumber;
        // Keep at total of the number of samples in a seismogram.
        seismogramData.number_of_samples = seismogramData.number_of_samples
                + nextPacket.dP.numberOfSamples;
        // Update begin_time_of_first_packet.
        seismogramData.begin_time_of_first_packet = nextPacket.begin_time_of_first_packet;
        // Update end_time_of_last_packet
        seismogramData.end_time_of_last_packet = nextPacket.end_time_of_last_packet;
        if(processData) {
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
        }
        return seismogramData;
    }

    public static PacketType appendEventHeaderPacket(PacketType seismogramData,
                                                     PacketType nextPacket) throws RT130FormatException {
        PacketType clone = new PacketType(nextPacket);
        return clone;
    }

    public static PacketType appendEventTrailerPacket(PacketType seismogramData,
                                                      PacketType nextPacket) {
        if(seismogramData.sample_rate == 0){
            seismogramData.sample_rate = nextPacket.sample_rate;
        }
        return seismogramData;
    }

    public static PacketType appendAuxiliaryDataParameterPacket(PacketType stateOfHealthData,
                                                                PacketType nextPacket) {
        if(stateOfHealthData.begin_time_from_state_of_health_file == null) {
            stateOfHealthData.begin_time_from_state_of_health_file = nextPacket.time;
        }
        return stateOfHealthData;
    }

    public static PacketType appendCalibrationParameterPacket(PacketType stateOfHealthData,
                                                              PacketType nextPacket) {
        if(stateOfHealthData.begin_time_from_state_of_health_file == null) {
            stateOfHealthData.begin_time_from_state_of_health_file = nextPacket.time;
        }
        return stateOfHealthData;
    }

    public static PacketType appendDataStreamParameterPacket(PacketType stateOfHealthData,
                                                             PacketType nextPacket) {
        if(stateOfHealthData.begin_time_from_state_of_health_file == null) {
            stateOfHealthData.begin_time_from_state_of_health_file = nextPacket.time;
        }
        return stateOfHealthData;
    }

    public static PacketType appendOperatingModeParameterPacket(PacketType stateOfHealthData,
                                                                PacketType nextPacket) {
        if(stateOfHealthData.begin_time_from_state_of_health_file == null) {
            stateOfHealthData.begin_time_from_state_of_health_file = nextPacket.time;
        }
        return stateOfHealthData;
    }

    public static PacketType appendStateOfHealthPacket(PacketType stateOfHealthData,
                                                       PacketType nextPacket) {
        if(stateOfHealthData.begin_time_from_state_of_health_file == null) {
            stateOfHealthData.begin_time_from_state_of_health_file = nextPacket.time;
        }
        if(stateOfHealthData.number_of_location_readings < 0) {
            // Skips first GPS entry in SOH file.
        }else {
            if(nextPacket.sOHP.latitude == 0 && nextPacket.sOHP.longitude == 0 && nextPacket.sOHP.elevation == 0){
                // Reduce count if 0's are added.
                stateOfHealthData.number_of_location_readings--;
            }
            stateOfHealthData.latitude_ =  stateOfHealthData.latitude_ + nextPacket.sOHP.latitude;
            stateOfHealthData.longitude_ = stateOfHealthData.longitude_ + nextPacket.sOHP.longitude;
            stateOfHealthData.elevation_ = stateOfHealthData.elevation_ + nextPacket.sOHP.elevation;
        }
        stateOfHealthData.number_of_location_readings++;
        return stateOfHealthData;
    }

    public static PacketType appendStationChannelParameterPacket(PacketType stateOfHealthData,
                                                                 PacketType nextPacket) {
        if(stateOfHealthData.begin_time_from_state_of_health_file == null) {
            stateOfHealthData.begin_time_from_state_of_health_file = nextPacket.time;
        }
        if(stateOfHealthData.channel_name == null){
            stateOfHealthData.channel_name = nextPacket.channel_name;
        }
        return stateOfHealthData;
    }
}
