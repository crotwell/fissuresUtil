package edu.sc.seis.fissuresUtil.rt130;

import edu.iris.Fissures.model.ISOTime;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;

public class FileNameParser {

    public static TimeInterval getLengthOfData(String fileName)
            throws RT130FormatException {
        if(fileName.length() != 18) {
            System.err.println("The file is not 18 characters in length.");
            throw new RT130FormatException();
        } else {
            String[] hexLengthOfData = {fileName.substring(17, 18),
                                        fileName.substring(16, 17),
                                        fileName.substring(15, 16),
                                        fileName.substring(14, 15),
                                        fileName.substring(13, 14),
                                        fileName.substring(12, 13),
                                        fileName.substring(11, 12),
                                        fileName.substring(10, 11)};
            double milliseconds = 0;
            for(int i = 0; i < hexLengthOfData.length; i++) {
                milliseconds = milliseconds
                        + (convertHexToDecimal(hexLengthOfData[i]) * (Math.pow(16.0,
                                                                               i)));
            }
            TimeInterval lengthOfData = new TimeInterval(milliseconds,
                                                         UnitImpl.MILLISECOND);
            return lengthOfData;
        }
    }

    private static int convertHexToDecimal(String a) {
        if(a.equals("F")) {
            return 15;
        } else if(a.equals("E")) {
            return 14;
        } else if(a.equals("D")) {
            return 13;
        } else if(a.equals("C")) {
            return 12;
        } else if(a.equals("B")) {
            return 11;
        } else if(a.equals("A")) {
            return 10;
        } else {
            return Integer.valueOf(a).intValue();
        }
    }

    public static MicroSecondDate getBeginTime(String yearAndDay,
                                               String fileName) {
        int year = Integer.valueOf(yearAndDay.substring(0, 4)).intValue();
        int dayOfYear = Integer.valueOf(yearAndDay.substring(4, 7)).intValue();
        int hours = Integer.valueOf(fileName.substring(0, 2)).intValue();
        int minutes = Integer.valueOf(fileName.substring(2, 4)).intValue();
        int seconds = Integer.valueOf(fileName.substring(4, 6)).intValue();
        return new ISOTime(year, dayOfYear, hours, minutes, seconds).getDate();
    }
}
