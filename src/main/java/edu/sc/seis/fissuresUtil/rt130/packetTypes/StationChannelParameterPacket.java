package edu.sc.seis.fissuresUtil.rt130.packetTypes;

import java.io.DataInput;
import java.io.IOException;

import edu.sc.seis.fissuresUtil.rt130.PacketType;

/**
 * @author fenner Created on Jun 14, 2005
 */
public class StationChannelParameterPacket extends PacketType {

    public StationChannelParameterPacket(StationChannelParameterPacket original) {
        
    }

    public StationChannelParameterPacket(DataInput in) throws IOException {
        this.read(in);
    }

    private void read(DataInput in) throws IOException {
        // Experiment Number
        experimentNumber = new String(this.readBytes(in, 2));
        //System.out.println("Experiment Number: " + experimentNumber);
        // Experiment Name
        experimentName = new String(this.readBytes(in, 24));
        //System.out.println("Experiement Name: " + experimentName);
        // Experiment Comment
        experimentComment = new String(this.readBytes(in, 40));
        //System.out.println("Experiement Comment: " + experimentComment);
        // Station Number
        stationNumber = new String(this.readBytes(in, 4));
        //System.out.println("Station Number: " + stationNumber);
        // Station Name
        stationName = new String(this.readBytes(in, 24));
        //System.out.println("Station Name: " + stationName);
        // Station Comment
        stationComment = new String(this.readBytes(in, 40));
        //System.out.println("Station Comment: " + stationComment);
        // DAS Model
        dasModel = new String(this.readBytes(in, 12));
        //System.out.println("DAD Model: " + dasModel);
        // DAS Serial Number
        dasSerialNumber = new String(this.readBytes(in, 12));
        //System.out.println("DAD Serial Number: " + dasSerialNumber);
        // Experiment Start
        experimentStart = new String(this.readBytes(in, 14));
        //System.out.println("Experiment Start: " + experimentStart);
        // Time Clock Type
        timeClockType = new String(this.readBytes(in, 4));
        //System.out.println("Time Clock Type: " + timeClockType);
        // Time Clock Signal To Noise Ratio
        timeClockSignalToNoiseRatio = new String(this.readBytes(in, 10));
        //System.out.println("Time Clock Signal To Noise Ratio: "
        //        + timeClockSignalToNoiseRatio);
        // Channel Information
        for(int i = 0; i < 5; i++) {
            // Channel Number
            channelNumber[i] = new String(this.readBytes(in, 2));
            // System.out.println("Channel Number: " + channelNumber[i]);
            // Channel Name
            channelName[i] = new String(this.readBytes(in, 10)).trim();
            // System.out.println("Channel Name: " + channelName[i]);
            // Azimuth
            azimuth[i] = new String(this.readBytes(in, 10));
            //System.out.println("Azimuth: " + azimuth[i]);
            // Inclination
            inclination[i] = new String(this.readBytes(in, 10));
            //System.out.println("Inclination: " + inclination[i]);
            // X Coordinate
            xCoordinate[i] = new String(this.readBytes(in, 10));
            //System.out.println("X Coordinate: " + xCoordinate[i]);
            // Y Coordinate
            yCoordinate[i] = new String(this.readBytes(in, 10));
            //System.out.println("Y Coordinate: " + yCoordinate[i]);
            // Z Coordinate
            zCoordinate[i] = new String(this.readBytes(in, 10));
            //System.out.println("Z Coordinate: " + zCoordinate[i]);
            // Unit Type for X, Y
            unitTypeXY[i] = new String(this.readBytes(in, 4));
            //System.out.println("Unit Type for X, Y: " + unitTypeXY[i]);
            // Unit Type for Z
            unitTypeZ[i] = new String(this.readBytes(in, 4));
            //System.out.println("Unit Type for Z: " + unitTypeZ[i]);
            // Preamp Gain
            preampGain[i] = new String(this.readBytes(in, 4));
            //System.out.println("Preamp Gain: " + preampGain[i]);
            // Sensor Model
            sensorModel[i] = new String(this.readBytes(in, 12));
            //System.out.println("Sensor Model: " + sensorModel[i]);
            // Sensor Serial Number
            sensorSerialNumber[i] = new String(this.readBytes(in, 12));
            //System.out.println("Sensor Serial Number: " + sensorSerialNumber[i]);
            // Comments
            comments[i] = new String(this.readBytes(in, 40));
            //System.out.println("Comments: " + comments[i]);
            // Adjusted Nominal Bit Weight
            adjustedNominalBitWeight[i] = new String(this.readBytes(in, 8));
            //System.out.println("Adjusted nominal Bit Weights: "
            //        + adjustedNominalBitWeight[i]);
        }
        // Reserved
        in.skipBytes(92);
    }

    private byte[] readBytes(DataInput in, int numBytes) throws IOException {
        byte[] seqBytes = new byte[numBytes];
        in.readFully(seqBytes);
        return seqBytes;
    }
    
    public String experimentNumber, experimentName, experimentComment,
            stationNumber, stationName, stationComment, dasModel,
            dasSerialNumber, experimentStart, timeClockType,
            timeClockSignalToNoiseRatio, channelNumber[] = new String[5],
            channelName[] = new String[5], azimuth[] = new String[5],
            inclination[] = new String[5], xCoordinate[] = new String[5],
            yCoordinate[] = new String[5], zCoordinate[] = new String[5],
            unitTypeXY[] = new String[5], unitTypeZ[] = new String[5],
            preampGain[] = new String[5], sensorModel[] = new String[5],
            sensorSerialNumber[] = new String[5], comments[] = new String[5],
            adjustedNominalBitWeight[] = new String[5];
}
