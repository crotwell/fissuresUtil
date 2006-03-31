package edu.sc.seis.fissuresUtil.database.seismogram;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;

public class StationCodeWithChannelCodesAndTimeRanges {

    public StationCodeWithChannelCodesAndTimeRanges(String stationCode,
                                                    Map channelsWithTimeRanges) {
        this.stationCode = stationCode;
        this.channelsWithTimeRanges = channelsWithTimeRanges;
    }

    public String getStationCode() {
        return stationCode;
    }

    public Map getChannelsWithTimeRanges() {
        return channelsWithTimeRanges;
    }

    public void print() {
        System.out.println(stationCode);
        Iterator it = channelsWithTimeRanges.keySet().iterator();
        while(it.hasNext()) {
            String channelCode = (String)it.next();
            System.out.println(channelCode);
            Iterator jt = ((List)channelsWithTimeRanges.get(channelCode)).iterator();
            while(jt.hasNext()) {
                MicroSecondTimeRange timeRange = (MicroSecondTimeRange)jt.next();
                System.out.print(timeRange.getBeginTime().toString());
                System.out.print(" - ");
                System.out.println(timeRange.getEndTime().toString());
            }
        }
    }

    private String stationCode;

    private Map channelsWithTimeRanges;
}
