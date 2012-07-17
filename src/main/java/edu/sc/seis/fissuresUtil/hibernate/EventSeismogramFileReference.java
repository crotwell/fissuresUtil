package edu.sc.seis.fissuresUtil.hibernate;

import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.SeismogramAttrImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.xml.SeismogramFileTypes;


public class EventSeismogramFileReference extends AbstractSeismogramFileReference {

    /** just for hibernate */
    protected EventSeismogramFileReference() {}
    
    public EventSeismogramFileReference(CacheEvent event,
                                        ChannelImpl channel,
                                        SeismogramAttrImpl seis,
                                        String fileLocation,
                                        SeismogramFileTypes filetype) {
        super(channel.getId().network_id.network_code, 
              channel.getId().station_code,
              channel.getId().site_code,
              channel.getId().channel_code,
              seis.getBeginTime().getTimestamp(),
              seis.getEndTime().getTimestamp(),
              fileLocation,
              filetype.getIntValue());
        this.event = event;
    }

    protected CacheEvent event;

    
    public CacheEvent getEvent() {
        return event;
    }

    
    protected void setEvent(CacheEvent event) {
        this.event = event;
    }
    
    
}
