package edu.sc.seis.fissuresUtil.rt130;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;

public class FileNameParser {

    public static TimeInterval getLengthOfData(String fileName)
            throws RT130FormatException {
        if(fileName.length() != 18) {
            throw new RT130FormatException("The file '" + fileName
                    + "'is not 18 characters in length.");
        }
        String stringLengthOfData = fileName.substring(10, 18);
        long longLengthOfData = Long.parseLong(stringLengthOfData, 16);
        TimeInterval lengthOfData = new TimeInterval(longLengthOfData,
                                                     UnitImpl.MILLISECOND);
        return lengthOfData;
    }

    public static MicroSecondDate getBeginTime(String yearAndDay,
                                               String fileName) {
        Matcher yearDay = YEAR_DAY_DIR.matcher(yearAndDay);
        if(!yearDay.matches()) {
            throw new IllegalArgumentException("The yearAndDay argument must be a four digit year specifier followed by a three digit julian day, not '"
                    + yearAndDay + "'");
        }
        Matcher time = SINGLE_TIME_FILE.matcher(fileName);
        if(!time.matches()) {
            throw new IllegalArgumentException("The fileName argument must be an 18 character RT130 filename, not '"
                    + fileName + "'");
        }
        int year = Integer.valueOf(yearDay.group(1)).intValue();
        int dayOfYear = Integer.valueOf(yearDay.group(2)).intValue();
        int hours = Integer.valueOf(time.group(1)).intValue();
        int minutes = Integer.valueOf(time.group(2)).intValue();
        int seconds = Integer.valueOf(time.group(3)).intValue();
        int millis = Integer.valueOf(time.group(4)).intValue();
        Date d;
        synchronized(beginParserCal) {
            beginParserCal.set(Calendar.YEAR, year);
            beginParserCal.set(Calendar.DAY_OF_YEAR, dayOfYear);
            beginParserCal.set(Calendar.HOUR_OF_DAY, hours);
            beginParserCal.set(Calendar.MINUTE, minutes);
            beginParserCal.set(Calendar.SECOND, seconds);
            beginParserCal.set(Calendar.MILLISECOND, millis);
            d = beginParserCal.getTime();
        }
        return new MicroSecondDate(d);
    }

    public static boolean isYearAndDayDir(String filename) {
        return YEAR_DAY_DIR.matcher(filename).matches();
    }

    public static boolean isRT130File(String filename) {
        return SINGLE_TIME_FILE.matcher(filename).matches();
    }

    private static Calendar beginParserCal = Calendar.getInstance();
    static {
        beginParserCal.setTimeZone(TimeZone.getTimeZone("GMT"));
        beginParserCal.set(Calendar.MILLISECOND, 0);
    }

    private static final Pattern YEAR_DAY_DIR = Pattern.compile("(\\d{4})(\\d{3})");

    private static final Pattern SINGLE_TIME_FILE = Pattern.compile("(\\d{2})(\\d{2})(\\d{2})(\\d{3})_\\w{8}");
}
