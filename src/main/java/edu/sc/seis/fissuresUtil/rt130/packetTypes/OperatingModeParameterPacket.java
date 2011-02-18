package edu.sc.seis.fissuresUtil.rt130.packetTypes;

import java.io.DataInput;
import java.io.IOException;

import edu.sc.seis.fissuresUtil.rt130.PacketType;

/**
 * @author fenner Created on Jun 14, 2005
 */
public class OperatingModeParameterPacket extends PacketType {
    
    public OperatingModeParameterPacket(OperatingModeParameterPacket original){
        
    }
    
    public OperatingModeParameterPacket(DataInput in)
            throws IOException {
        this.read(in);
    }

    private void read(DataInput in) throws IOException {
        // Skip rest of packet
        in.skipBytes(1008);
    }
}
