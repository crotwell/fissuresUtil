package edu.sc.seis.fissuresUtil.rt130.packetTypes;

import java.io.DataInput;
import java.io.IOException;
import edu.sc.seis.fissuresUtil.rt130.BCDRead;
import edu.sc.seis.fissuresUtil.rt130.HexRead;
import edu.sc.seis.fissuresUtil.rt130.RT130FormatException;
import edu.sc.seis.fissuresUtil.rt130.PacketType;


/**
 * @author fenner Created on Jun 14, 2005
 */
public class EventHeaderPacket extends PacketType{
    
    public EventHeaderPacket(EventHeaderPacket original){
        if(original != null){
            this.dataFormat = original.dataFormat;
            this.triggerTimeMessage = original.triggerTimeMessage;
            this.timeSource = original.timeSource;
            this.timeQuality = original.timeQuality;
            this.stationNameExt = original.timeQuality;
            this.stationName = original.timeQuality;
            this.streamName = original.timeQuality;
            this.sampleRate = original.timeQuality;
            this.triggerType = original.timeQuality;
            this.triggerTime = original.triggerTime;
            this.firstSampleTime = original.firstSampleTime;
            this.detriggerTime = original.firstSampleTime;
            this.lastSampleTime = original.lastSampleTime;
            this.channelAdjustedNominalBitWeights = original.channelAdjustedNominalBitWeights;
            this.channelTreeBitWeights = original.channelTreeBitWeights;
            this.channelGain = original.channelTreeBitWeights;
            this.channelADResolution = original.channelTreeBitWeights;
            this.channelFSA = original.channelTreeBitWeights;
            this.channelCode = original.channelTreeBitWeights;
            this.channelSensorFSA = original.channelTreeBitWeights;
            this.channelSensorVPU = original.channelTreeBitWeights;
            this.channelSensorUnits = original.channelTreeBitWeights;
            this.stationComment = original.stationComment;
            this.filterList = original.filterList;
            this.position = original.filterList;
            
            this.eventNumber = original.eventNumber;
            this.dataStreamNumber = original.dataStreamNumber;
        }
    }
    
    public EventHeaderPacket(DataInput in)
            throws IOException, RT130FormatException{

        this.read(in);
    }
    
    private void read(DataInput in) throws IOException, RT130FormatException{
        
        // Event Number
        eventNumber = BCDRead.toInt(this.readBytes(in, 2)); 
        //System.out.println("    Event Number: " + eventNumber);
        
        //Data Stream Number
        dataStreamNumber = BCDRead.toInt(this.readBytes(in, 1));
        //System.out.println("    Data Stream Number: " + dataStreamNumber);
        
        // Reserved (Binary: 0)
        // Reserved bytes are used as filler as far as I [Ben] can tell.
        in.skipBytes(4);
        
        // Data Format
        dataFormat = HexRead.toString(this.readBytes(in, 1));
        System.out.println("    Data Format: " + dataFormat);
        
        if(dataFormat.equals("C0")){
            // Trigger Time Message
            triggerTimeMessage = new String(this.readBytes(in, 33));
            //System.out.println("    Trigger Time Message: " + triggerTimeMessage);
            
            // Time Source
            timeSource = new String(this.readBytes(in, 1));
            //System.out.println("    Time Source: " + timeSource);
            
            // Time Quality
            timeQuality = new String(this.readBytes(in, 1));
            //System.out.println("    Time Quality: " + timeQuality);
            
            // Station Name Ext
            stationNameExt = new String(this.readBytes(in, 1));
            //System.out.println("    Station Name Ext: " + stationNameExt);
            
            // Station Name
            stationName = new String(this.readBytes(in, 4));
            //System.out.println("    Station Name: " + stationName);
            
            // Stream Name
            streamName = new String(this.readBytes(in, 16));
            //System.out.println("    Stream Name: " + streamName);
            
            // Reserved (ASCII space)
            // Reserved bytes are used as filler as far as I [Ben] can tell.
            in.skipBytes(8);
            
            // Sample Rate
            sampleRate = new String(this.readBytes(in, 4));
            System.out.println("    Sample Rate: " + sampleRate);
            
            // Trigger Type
            triggerType = new String(this.readBytes(in, 4));
            //System.out.println("    Trigger Type: " + triggerType);
            
            // Trigger Time
            triggerTime = new String(this.readBytes(in, 16));
            //System.out.println("    Trigger Time: " + triggerTime);
            
            // First Sample Time
            firstSampleTime = new String (this.readBytes(in, 16));
            //System.out.println("    First Sample Time: " + firstSampleTime);
            
            // Detrigger Time
            // The file format lists ASCII spaces for these bytes.
            detriggerTime = new String (this.readBytes(in, 16));
            //System.out.println("    Detrigger Time: " + detriggerTime);
            
            // Last Sample Time
            // The file format lists ASCII spaces for these bytes.
            lastSampleTime = new String (this.readBytes(in, 16));
            //System.out.println("    Last Sample Time: " + lastSampleTime);
            
            // Channel Adjusted Nominal Bit Weights
            channelAdjustedNominalBitWeights = new String (this.readBytes(in, 128));
            //System.out.println("    Channel Adjusted Nominal Bit Weights: " + channelAdjustedNominalBitWeights);
            
            // Channel Tree Bit Weights
            channelTreeBitWeights = new String (this.readBytes(in, 128));
            //System.out.println("    Channel Tree Bit Weights: " + channelTreeBitWeights);
            
            // Channel Gain
            channelGain = new String (this.readBytes(in, 16));
            System.out.println("    Channel Gain: " + channelGain);
            
            // Channel A/D Resolution
            channelADResolution = new String (this.readBytes(in, 16));
            //System.out.println("    Channel A/D Resolution: " + channelADResolution);
            
            // Channel FSA
            channelFSA = new String (this.readBytes(in, 16));
            //System.out.println("    Channel FSA: " + channelFSA);
            
            // Channel Code
            channelCode = new String (this.readBytes(in, 64));
            //System.out.println("    Channel Code: " + channelCode);
            
            // Channel Sensor FSA
            channelSensorFSA = new String (this.readBytes(in, 16));
            //System.out.println("    Channel Sensor FSA: " + channelSensorFSA);
            
            // Channel Sensor FSA
            channelSensorVPU = new String (this.readBytes(in, 96));
            //System.out.println("    Channel Sensor VPU: " + channelSensorVPU);
            
            // Channel Sensor Units
            channelSensorUnits = new String (this.readBytes(in, 16));
            //System.out.println("    Channel Sensor Units: " + channelSensorUnits);
            
            // Reserved (ASCII space)
            // Reserved bytes are used as filler as far as I [Ben] can tell.
            in.skipBytes(206);;
            
            // Station Comment
            stationComment = new String (this.readBytes(in, 40));
            //System.out.println("    Station Comment: " + stationComment);
            
            // Filter List
            filterList = new String (this.readBytes(in, 16));
            //System.out.println("    Filter List: " + filterList);
            
            // Position
            position = new String (this.readBytes(in, 26));
            //System.out.println("    Position: " + position);
            
            // Unavailable (Reserved for Ref Tek 120)
            in.skipBytes(80);
        } else{
            System.err.println("The data in this packet is not in compressed format.");
            System.err.println("This file reader is not prepaired to read uncompressed seismogram data.");
            System.err.println("Throwing a format exception even though this format is valid.");
            throw new RT130FormatException();
        }
    }
    
    private byte[] readBytes(DataInput in, int numBytes) throws IOException {
        byte[] seqBytes = new byte[numBytes];
        in.readFully(seqBytes);
        return seqBytes;
    }
   
    public String dataFormat, triggerTimeMessage, timeSource, timeQuality,
            stationNameExt, stationName, streamName, sampleRate, triggerType,
            triggerTime, firstSampleTime, detriggerTime, lastSampleTime,
            channelAdjustedNominalBitWeights, channelTreeBitWeights, channelGain,
            channelADResolution, channelFSA, channelCode, channelSensorFSA,
            channelSensorVPU, channelSensorUnits, stationComment, filterList,
            position;
    
    public int eventNumber, dataStreamNumber;
}
