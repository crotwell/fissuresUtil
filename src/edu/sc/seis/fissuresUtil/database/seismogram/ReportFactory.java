package edu.sc.seis.fissuresUtil.database.seismogram;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import edu.iris.Fissures.IfNetwork.Channel;

public class ReportFactory {

    public ReportFactory(Map channelsWithTimeRanges) {
        stationCodeWithChannelCodesAndTimeRanges = new LinkedList();
        organizeByStationCode(channelsWithTimeRanges);
    }

    public void print() {
        Iterator it = stationCodeWithChannelCodesAndTimeRanges.iterator();
        while(it.hasNext()) {
            ((StationCodeWithChannelCodesAndTimeRanges)it.next()).print();
            System.out.println();
        }
    }

    private void organizeByStationCode(Map channelsWithTimeRanges) {
        Set stationCodes = new HashSet();
        Iterator it = channelsWithTimeRanges.keySet().iterator();
        while(it.hasNext()) {
            stationCodes.add(((Channel)it.next()).my_site.my_station.get_code());
        }
        Iterator jt = stationCodes.iterator();
        while(jt.hasNext()) {
            String setStationCode = (String)jt.next();
            Map channelCodesWithTimeRanges = new HashMap();
            Iterator kt = channelsWithTimeRanges.keySet().iterator();
            while(kt.hasNext()) {
                Channel channelKey = ((Channel)kt.next());
                String stationCode = (channelKey).my_site.my_station.get_code();
                if(stationCode.equals(setStationCode)) {
                    String channelCode = channelKey.get_code();
                    List timeRanges = (List)channelsWithTimeRanges.get(channelKey);
                    channelCodesWithTimeRanges.put(channelCode, timeRanges);
                }
            }
            StationCodeWithChannelCodesAndTimeRanges temp = new StationCodeWithChannelCodesAndTimeRanges(setStationCode,
                                                                                                         channelCodesWithTimeRanges);
            stationCodeWithChannelCodesAndTimeRanges.add(temp);
        }
    }

    private List stationCodeWithChannelCodesAndTimeRanges;
}
