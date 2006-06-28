package edu.sc.seis.fissuresUtil.database.seismogram;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;

public class StationDataSummary implements Comparable {

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

    public String getChannelCodeWithMostGaps() {
        Set channelCodesSet = channelCodesWithTimeRanges.keySet();
        Iterator it = channelCodesSet.iterator();
        int num = 0;
        String channelCode = "";
        while(it.hasNext()) {
            String tempString = (String)it.next();
            int temp = ((List)channelCodesWithTimeRanges.get(tempString)).size();
            if(temp > num) {
                num = temp;
                channelCode = tempString;
            }
        }
        return channelCode;
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

    public void printGapDescription(PrintWriter reportStream) {
        Iterator it = channelCodesWithTimeRanges.keySet().iterator();
        while(it.hasNext()) {
            String channelCode = (String)it.next();
            Iterator jt = ((List)channelCodesWithTimeRanges.get(channelCode)).iterator();
            while(jt.hasNext()) {
                MicroSecondTimeRange timeRange = (MicroSecondTimeRange)jt.next();
                reportStream.print("    ");
                reportStream.print(stationCode);
                reportStream.print(" ");
                reportStream.print(channelCode);
                reportStream.print(" ");
                reportStream.print(timeRange.getBeginTime().toString());
                reportStream.print(" - ");
                reportStream.println(timeRange.getEndTime().toString());
            }
        }
    }

    public void printDaysOfCoverage(PrintWriter reportStream) {
        Iterator it = channelCodesWithTimeRanges.keySet().iterator();
        while(it.hasNext()) {
            String channelCode = (String)it.next();
            if(channelCode.equals("BHZ")) {
                TimeInterval total = null;
                Iterator jt = ((List)channelCodesWithTimeRanges.get(channelCode)).iterator();
                while(jt.hasNext()) {
                    MicroSecondTimeRange timeRange = (MicroSecondTimeRange)jt.next();
                    if(total == null) {
                        total = timeRange.getInterval();
                    } else {
                        total = total.add(timeRange.getInterval());
                    }
                }
                DecimalFormat format = new DecimalFormat();
                format.setMaximumFractionDigits(2);
                format.setMinimumFractionDigits(2);
                reportStream.print("    ");
                reportStream.print(stationCode);
                if(total == null) {
                    reportStream.print(" - The BHZ channel for this station recorded no time.");
                } else {
                    reportStream.print(" covers ");
                    double doubleDays = total.getValue(UnitImpl.DAY);
                    String stringDays = format.format(doubleDays);
                    reportStream.print(stringDays);
                    if(new Double(stringDays).doubleValue() > 1) {
                        reportStream.print(" days.");
                    } else {
                        reportStream.print(" day.");
                    }
                }
            }
        }
    }

    public int compareTo(Object o) {
        if(o instanceof StationDataSummary) {
            return (((StationDataSummary)o).stationCode.compareTo(this.stationCode) * (-1));
        }
        return 1;
    }

    private String stationCode;

    private Map channelCodesWithTimeRanges;
}
