package edu.sc.seis.fissuresUtil.database.seismogram;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.time.ReduceTool;

public class StationDataSummary implements Comparable {

    public StationDataSummary(String stationCode, Map timeRangesByChannelCode) {
        this.stationCode = stationCode;
        this.timeRangesByChannelCode = timeRangesByChannelCode;
    }

    public String getStationCode() {
        return stationCode;
    }

    public MicroSecondTimeRange[] getRecordedTimes() {
        Iterator it = timeRangesByChannelCode.keySet().iterator();
        List allTimeRanges = new ArrayList();
        while(it.hasNext()) {
            allTimeRanges.addAll((List)timeRangesByChannelCode.get(it.next()));
        }
        return ReduceTool.merge((MicroSecondTimeRange[])allTimeRanges.toArray(new MicroSecondTimeRange[0]));
    }

    public MicroSecondTimeRange getEncompassingTimeRange() {
        MicroSecondTimeRange result = null;
        Set channelCodeSet = timeRangesByChannelCode.keySet();
        Iterator it = channelCodeSet.iterator();
        while(it.hasNext()) {
            Iterator ij = ((List)timeRangesByChannelCode.get(it.next())).iterator();
            while(ij.hasNext()) {
                MicroSecondTimeRange newTimeRange = (MicroSecondTimeRange)ij.next();
                if(result == null) {
                    result = newTimeRange;
                } else {
                    result = new MicroSecondTimeRange(newTimeRange, result);
                }
            }
        }
        return result;
    }

    public void printGapDescription(PrintWriter reportStream) {
        Iterator it = timeRangesByChannelCode.keySet().iterator();
        while(it.hasNext()) {
            String channelCode = (String)it.next();
            Iterator jt = ((List)timeRangesByChannelCode.get(channelCode)).iterator();
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
        Iterator it = timeRangesByChannelCode.keySet().iterator();
        while(it.hasNext()) {
            String channelCode = (String)it.next();
            if(channelCode.equals("BHZ")) {
                TimeInterval total = null;
                Iterator jt = ((List)timeRangesByChannelCode.get(channelCode)).iterator();
                while(jt.hasNext()) {
                    MicroSecondTimeRange timeRange = (MicroSecondTimeRange)jt.next();
                    if(total == null) {
                        total = timeRange.getInterval();
                    } else {
                        total = total.add(timeRange.getInterval());
                    }
                }
                DecimalFormat decFormat = new DecimalFormat();
                decFormat.setMaximumFractionDigits(2);
                decFormat.setMinimumFractionDigits(2);
                reportStream.print("    ");
                reportStream.print(stationCode);
                if(total == null) {
                    reportStream.print(" - The BHZ channel for this station recorded no time.");
                } else {
                    reportStream.print(" covers ");
                    double doubleDays = total.getValue(UnitImpl.DAY);
                    String stringDays = decFormat.format(doubleDays);
                    reportStream.print(stringDays);
                    if(doubleDays > 1.0049) {
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
            // Sort in alphabetical station code order
            return (((StationDataSummary)o).stationCode.compareTo(stationCode) * (-1));
        }
        return 1;
    }

    private String stationCode;

    private Map timeRangesByChannelCode;
}
