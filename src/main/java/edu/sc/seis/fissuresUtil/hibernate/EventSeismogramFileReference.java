package edu.sc.seis.fissuresUtil.hibernate;

import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.SeismogramAttrImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.xml.SeismogramFileTypes;


public class EventSeismogramFileReference extends SeismogramFileReference {

    public EventSeismogramFileReference(CacheEvent event,
                                        ChannelImpl channel,
                                        SeismogramAttrImpl seis,
                                        String fileLocation,
                                        SeismogramFileTypes filetype) {
        super(channel, seis, fileLocation, filetype);
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
