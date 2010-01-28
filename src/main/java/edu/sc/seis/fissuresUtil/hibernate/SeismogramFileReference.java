package edu.sc.seis.fissuresUtil.hibernate;


import java.sql.Timestamp;

import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.SeismogramAttrImpl;
import edu.sc.seis.fissuresUtil.xml.SeismogramFileTypes;

public class SeismogramFileReference extends AbstractSeismogramFileReference {

    /** just for hibernate */
    protected SeismogramFileReference() {}
    
    public SeismogramFileReference(ChannelImpl channel,
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
    }

    public SeismogramFileReference(String netCode,
                                   String staCode,
                                   String siteCode,
                                   String chanCode,
                                   Timestamp beginTime,
                                   Timestamp endTime,
                                   String filePath,
                                   int fileType) {
        super(netCode, staCode, siteCode, chanCode, beginTime, endTime, filePath, fileType);
    }
    
}
