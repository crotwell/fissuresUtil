package edu.sc.seis.fissuresUtil.hibernate;

import java.util.List;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;


public interface PlottableStorage {
    
    public List<PlottableChunk> get(MicroSecondTimeRange requestRange,
                                    ChannelId channel,
                                    int pixelsPerDay);
    

    public void put(List<PlottableChunk> chunks);
    
    public void commit();
    
    public void rollback();
}
