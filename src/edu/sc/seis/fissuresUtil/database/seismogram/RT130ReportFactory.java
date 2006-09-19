package edu.sc.seis.fissuresUtil.database.seismogram;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import edu.iris.Fissures.IfNetwork.ChannelId;

public class RT130ReportFactory {

    public RT130ReportFactory(Map channelIdWithTimeRanges,
                              Map channelIdStringToChannelId) {
        stationDataSummaryList = new LinkedList();
        organizeByStationCode(channelIdWithTimeRanges,
                              channelIdStringToChannelId);
    }

    public void printGapDescription(PrintWriter reportStream) {
        Collections.sort(stationDataSummaryList);
        Iterator it = stationDataSummaryList.iterator();
        while(it.hasNext()) {
            ((StationDataSummary)it.next()).printGapDescription(reportStream);
            reportStream.println();
        }
    }

    public void printDaysOfCoverage(PrintWriter reportStream) {
        Collections.sort(stationDataSummaryList);
        Iterator it = stationDataSummaryList.iterator();
        while(it.hasNext()) {
            ((StationDataSummary)it.next()).printDaysOfCoverage(reportStream);
            reportStream.println();
        }
    }

    public List getStationCodes() {
        List stationCodes = new LinkedList();
        Iterator it = stationDataSummaryList.iterator();
        while(it.hasNext()) {
            stationCodes.add(((StationDataSummary)it.next()).getStationCode());
        }
        return stationCodes;
    }

    public List getStationDataSummaryList() {
        return stationDataSummaryList;
    }

    public List getSortedStationDataSummaryList() {
        Collections.sort(stationDataSummaryList);
        return stationDataSummaryList;
    }

    private void organizeByStationCode(Map channelIdWithTimeRanges,
                                       Map channelIdToChannel) {
        Set stationCodes = new HashSet();
        Iterator it = channelIdWithTimeRanges.keySet().iterator();
        while(it.hasNext()) {
            stationCodes.add(((ChannelId)channelIdToChannel.get((String)it.next())).station_code);
        }
        Iterator jt = stationCodes.iterator();
        while(jt.hasNext()) {
            String setStationCode = (String)jt.next();
            Map channelCodesWithTimeRanges = new HashMap();
            Iterator kt = channelIdWithTimeRanges.keySet().iterator();
            while(kt.hasNext()) {
                String channelIdKey = ((String)kt.next());
                String stationCode = ((ChannelId)channelIdToChannel.get(channelIdKey)).station_code;
                if(stationCode.equals(setStationCode)) {
                    String channelCode = ((ChannelId)channelIdToChannel.get(channelIdKey)).channel_code;
                    List timeRanges = (List)channelIdWithTimeRanges.get(channelIdKey);
                    if(channelCodesWithTimeRanges.containsKey(channelCode)) {
                        List current = (List)channelCodesWithTimeRanges.get(channelCode);
                        current.addAll(timeRanges);
                    } else {
                        channelCodesWithTimeRanges.put(channelCode, timeRanges);
                    }
                }
            }
            StationDataSummary temp = new StationDataSummary(setStationCode,
                                                             channelCodesWithTimeRanges);
            stationDataSummaryList.add(temp);
        }
    }

    private List stationDataSummaryList;
}
