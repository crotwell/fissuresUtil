package edu.sc.seis.fissuresUtil.rt130;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
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
        int year = Integer.valueOf(yearAndDay.substring(0, 4)).intValue();
        int dayOfYear = Integer.valueOf(yearAndDay.substring(4, 7)).intValue();
        int hours = Integer.valueOf(fileName.substring(0, 2)).intValue();
        int minutes = Integer.valueOf(fileName.substring(2, 4)).intValue();
        int seconds = Integer.valueOf(fileName.substring(4, 6)).intValue();
        int millis = Integer.valueOf(fileName.substring(6,9)).intValue();
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

    private static Calendar beginParserCal = Calendar.getInstance();
    static {
        beginParserCal.setTimeZone(TimeZone.getTimeZone("GMT"));
        beginParserCal.set(Calendar.MILLISECOND, 0);
    }
}
