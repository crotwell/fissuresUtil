package edu.sc.seis.fissuresUtil.rt130;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;

public class LeapSecondApplier {

    private static Map unitIdToCorrections = new HashMap();

    private static List leapSecondOccurrences = new LinkedList();

    private static SimpleDateFormat format = new SimpleDateFormat("yy:DDD:HH:mm:ss:SSS");
    static {
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public static void addLeapSeconds(String correctionFileLoc)
            throws IOException, ParseException {
        BufferedReader in = openReader(correctionFileLoc);
        String nextLine;
        while((nextLine = in.readLine()) != null) {
            MicroSecondDate date = stringToMicroSecondDate(nextLine);
            if(!leapSecondOccurrences.contains(date)) {
                leapSecondOccurrences.add(date);
            }
        }
    }

    public static void addCorrections(String correctionFileLoc)
            throws IOException, ParseException {
        BufferedReader in = openReader(correctionFileLoc);
        String nextLine;
        while((nextLine = in.readLine()) != null) {
            StringTokenizer st = new StringTokenizer(nextLine, ";");
            String unitId = st.nextToken();
            MicroSecondDate date = stringToMicroSecondDate((st.nextToken()));
            if(unitIdToCorrections.containsKey(unitId)) {
                ((List)unitIdToCorrections.get(unitId)).add(date);
            } else {
                unitIdToCorrections.put(unitId, new LinkedList());
                ((List)unitIdToCorrections.get(unitId)).add(date);
            }
        }
    }

    private static BufferedReader openReader(String loc)
            throws FileNotFoundException {
        File f = new File(loc);
        if(f.exists()) {
            return new BufferedReader(new FileReader(loc));
        } else {
            try {
                ClassLoader cl = LeapSecondApplier.class.getClassLoader();
                return new BufferedReader(new InputStreamReader(cl.getResourceAsStream(loc)));
            } catch(Throwable t) {
                throw new FileNotFoundException("Unable to find " + loc
                        + " in filesystem or in classpath");
            }
        }
    }

    public static MicroSecondDate applyLeapSecondCorrection(String unitId,
                                                            MicroSecondDate time) {
        if(unitIdToCorrections.containsKey(unitId)) {
            time = time.subtract(new TimeInterval(howManyLeapSeconds(unitId,
                                                                     time),
                                                  UnitImpl.SECOND));
        } else {
            time = time.subtract(new TimeInterval(howManyLeapSeconds(time),
                                                  UnitImpl.SECOND));
        }
        return time;
    }

    private static int howManyLeapSeconds(String unitId, MicroSecondDate time) {
        int numLeapSeconds = 0;
        List powerOnTimes = (List)unitIdToCorrections.get(unitId);
        MicroSecondTimeRange timeWindow = null;
        for(Iterator i = leapSecondOccurrences.iterator(); i.hasNext();) {
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
        for(Iterator i = leapSecondOccurrences.iterator(); i.hasNext();) {
            MicroSecondDate leapSecondOccurance = (MicroSecondDate)i.next();
            if(time.after(leapSecondOccurance)) {
                numLeapSeconds++;
            }
        }
        return numLeapSeconds;
    }

    private static MicroSecondDate stringToMicroSecondDate(String date)
            throws ParseException {
        return new MicroSecondDate(format.parse(date));
    }

    public static List getLeapSecondOccurances() {
        return leapSecondOccurrences;
    }

    public static List getPowerUpTimes(String unitId) {
        return (List)unitIdToCorrections.get(unitId);
    }

    public static final String LEAP_SECOND_FILE = "leapSecondFile",
            POWER_UP_TIMES = "powerUpTimes";
}
