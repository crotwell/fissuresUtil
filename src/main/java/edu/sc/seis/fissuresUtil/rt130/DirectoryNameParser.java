package edu.sc.seis.fissuresUtil.rt130;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.iris.Fissures.model.MicroSecondDate;

public class DirectoryNameParser {

    /**
     * Parses times out of base dirs created by racs and dump.py and day dirs
     * created by rt130s
     * 
     * @return a microseconddate for recognized formats or null if the directory
     *         is unrecognized
     */
    public static MicroSecondDate getTime(File file) {
        String name = file.getName();
        if (name.startsWith(".")) {
            return null;
        } else if(dumpyDasDir.matcher(name).matches()) {
            return getTimeFromDumppyDirectory(file);
        } else if(dayDir.matcher(name).matches()) {
            return getTimeFromDayDirectory(file);
        } else if(racsDasDir.matcher(name).matches()) {
            return getTimeFromDumppyDirectory(file);
        } else {
            logger.warn("Directory '" + file
                    + "' did not match any known format.");
            return null;
        }
    }

    private static Pattern racsDasDir = Pattern.compile("[A-Z0-9]{4}_\\d{4}_\\d{3}_\\d{2}_\\d{2}\\.\\d"),
            dumpyDasDir = Pattern.compile("[A-Z0-9]{4}_\\d{4}_\\d{3}"),
            dayDir = Pattern.compile("\\d{7}");

    private static MicroSecondDate getTimeFromDumppyDirectory(File file) {
        String yearAndDay = file.getName().substring(5, 13);
        int year = Integer.valueOf(yearAndDay.substring(0, 4)).intValue();
        int dayOfYear = Integer.valueOf(yearAndDay.substring(5, 8)).intValue();
        return makeMicroSecondFromDayAndYear(year, dayOfYear);
    }

    private static MicroSecondDate getTimeFromDayDirectory(File file) {
        String yearAndDay = file.getName();
        int year = Integer.valueOf(yearAndDay.substring(0, 4)).intValue();
        int dayOfYear = Integer.valueOf(yearAndDay.substring(4, 7)).intValue();
        return makeMicroSecondFromDayAndYear(year, dayOfYear);
    }

    private static MicroSecondDate makeMicroSecondFromDayAndYear(int year,
                                                                 int dayOfYear) {
        Date d;
        synchronized(dirParserCal) {
            dirParserCal.set(Calendar.YEAR, year);
            dirParserCal.set(Calendar.DAY_OF_YEAR, dayOfYear);
            d = dirParserCal.getTime();
        }
        return new MicroSecondDate(d);
    }

    private static Calendar dirParserCal = Calendar.getInstance();
    static {
        dirParserCal.setTimeZone(TimeZone.getTimeZone("GMT"));
        dirParserCal.set(Calendar.HOUR_OF_DAY, 0);
        dirParserCal.set(Calendar.MINUTE, 0);
        dirParserCal.set(Calendar.SECOND, 0);
        dirParserCal.set(Calendar.MILLISECOND, 0);
    }

    private static final Logger logger = LoggerFactory.getLogger(DirectoryNameParser.class);
}
