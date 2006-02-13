package edu.sc.seis.fissuresUtil.rt130.leapSecondCorrection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.StringTokenizer;
import java.util.TimeZone;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;

public class LeapSecondApplier {

    private static Map map = new HashMap();

    private static List leapSecondOccurances = new LinkedList();

    public static void addLeapSeconds(String correctionFileLoc)
            throws IOException, ParseException {
        BufferedReader in = new BufferedReader(new FileReader(correctionFileLoc));
        String nextLine = null;
        while((nextLine = in.readLine()) != null) {
            MicroSecondDate date = stringToMicroSecondDate(nextLine);
            if(!leapSecondOccurances.contains(date)) {
                leapSecondOccurances.add(date);
            }
        }
    }

    public static void addCorrections(String correctionFileLoc)
            throws IOException, ParseException {
        BufferedReader in = new BufferedReader(new FileReader(correctionFileLoc));
        String nextLine = null;
        while((nextLine = in.readLine()) != null) {
            StringTokenizer st = new StringTokenizer(nextLine, ";");
            String unitId = st.nextToken();
            MicroSecondDate date = stringToMicroSecondDate((st.nextToken()));
            if(map.containsKey(unitId)) {
                ((List)map.get(unitId)).add(date);
            } else {
                map.put(unitId, new LinkedList());
                ((List)map.get(unitId)).add(date);
            }
        }
    }

    public static MicroSecondDate applyLeapSecondCorrection(String unitId,
                                                            MicroSecondDate time) {
        if(map.containsKey(unitId)) {
            time = time.add(new TimeInterval(howManyLeapSeconds(unitId, time),
                                             UnitImpl.SECOND));
        } else {
            time = time.add(new TimeInterval(howManyLeapSeconds(time),
                                             UnitImpl.SECOND));
        }
        return time;
    }

    private static int howManyLeapSeconds(String unitId, MicroSecondDate time) {
        int numLeapSeconds = 0;
        List powerOnTimes = (List)map.get(unitId);
        MicroSecondTimeRange timeWindow = null;
        for(Iterator i = leapSecondOccurances.iterator(); i.hasNext();) {
            MicroSecondDate leapSecondOccurance = (MicroSecondDate)i.next();
            for(Iterator j = powerOnTimes.iterator(); j.hasNext();) {
                MicroSecondDate powerOnTime = (MicroSecondDate)j.next();
                if(powerOnTime.after(leapSecondOccurance)) {
                    MicroSecondTimeRange temp = new MicroSecondTimeRange(leapSecondOccurance,
                                                                         powerOnTime);
                    if(timeWindow == null) {
                        timeWindow = temp;
                    } else if(temp.getInterval().value < timeWindow.getInterval().value) {
                        timeWindow = temp;
                    }
                }
            }
            if(timeWindow != null && timeWindow.contains(time)) {
                numLeapSeconds++;
            }
            timeWindow = null;
        }
        return numLeapSeconds;
    }

    private static int howManyLeapSeconds(MicroSecondDate time) {
        int numLeapSeconds = 0;
        for(Iterator i = leapSecondOccurances.iterator(); i.hasNext();) {
            MicroSecondDate leapSecondOccurance = (MicroSecondDate)i.next();
            if(time.after(leapSecondOccurance)) {
                numLeapSeconds++;
            }
        }
        return numLeapSeconds;
    }

    private static MicroSecondDate stringToMicroSecondDate(String date)
            throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yy:DDD:HH:mm:ss:SSS");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return new MicroSecondDate(format.parse(date));
    }

    public static List getLeapSecondOccurances() {
        return leapSecondOccurances;
    }

    public static Map getMap() {
        return map;
    }
}
