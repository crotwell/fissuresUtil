package edu.sc.seis.fissuresUtil.database.seismogram;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;

public class StationDataSummary implements Comparable{

    public StationDataSummary(String stationCode, Map channelsWithTimeRanges) {
        this.stationCode = stationCode;
        this.channelCodesWithTimeRanges = channelsWithTimeRanges;
    }

    public String getStationCode() {
        return stationCode;
    }

    public Map getChannelsWithTimeRanges() {
        return channelCodesWithTimeRanges;
    }

    public MicroSecondTimeRange getEncompassingTimeRange() {
        MicroSecondTimeRange result = null;
        Set channelCodeSet = channelCodesWithTimeRanges.keySet();
        Iterator it = channelCodeSet.iterator();
        while(it.hasNext()) {
            Iterator ij = ((List)channelCodesWithTimeRanges.get((String)it.next())).iterator();
            while(ij.hasNext()) {
                MicroSecondTimeRange newTimeRange = (MicroSecondTimeRange)ij.next();
                if(result == null) {
                    result = newTimeRange;
                } else {
                    if(newTimeRange.getBeginTime()
                            .before(result.getBeginTime())) {
                        result = new MicroSecondTimeRange(newTimeRange.getBeginTime(),
                                                          result.getEndTime());
                    }
                    if(newTimeRange.getEndTime().after(result.getEndTime())) {
                        result = new MicroSecondTimeRange(result.getBeginTime(),
                                                          newTimeRange.getEndTime());
                    }
                }
            }
        }
        return result;
    }

    public void print() {
        System.out.println(stationCode);
        Iterator it = channelCodesWithTimeRanges.keySet().iterator();
        while(it.hasNext()) {
            String channelCode = (String)it.next();
            System.out.println(channelCode);
            Iterator jt = ((List)channelCodesWithTimeRanges.get(channelCode)).iterator();
            while(jt.hasNext()) {
                MicroSecondTimeRange timeRange = (MicroSecondTimeRange)jt.next();
                System.out.print(timeRange.getBeginTime().toString());
                System.out.print(" - ");
                System.out.println(timeRange.getEndTime().toString());
            }
        }
    }

    private String stationCode;

    private Map channelCodesWithTimeRanges;

    public int compareTo(Object o) {
        if(o instanceof StationDataSummary){
            return ((StationDataSummary)o).stationCode.compareTo(this.stationCode); 
        }
        return -1;
    }
}
