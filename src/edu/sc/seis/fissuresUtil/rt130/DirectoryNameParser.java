package edu.sc.seis.fissuresUtil.rt130;

import java.io.File;
import org.apache.log4j.Logger;
import edu.iris.Fissures.model.ISOTime;
import edu.iris.Fissures.model.MicroSecondDate;

public class DirectoryNameParser {

    public static MicroSecondDate getTime(File file) {
        String name = file.getName();
        if(name.length() == 13 && name.charAt(4) == '_'
                && name.charAt(9) == '_') {
            return getTimeFromUnitIdYearAndDayDirectory(file);
        } else if(name.length() == 7) {
            return getTimeFromYearAndDayDirectory(file);
        } else {
            logger.warn("Directory name did not match any known format.");
            return null;
        }
    }

    private static MicroSecondDate getTimeFromUnitIdYearAndDayDirectory(File file) {
        String yearAndDay = file.getName().substring(5, 13);
        int year = Integer.valueOf(yearAndDay.substring(0, 4)).intValue();
        int dayOfYear = Integer.valueOf(yearAndDay.substring(5, 8)).intValue();
        return new ISOTime(year, dayOfYear, 0, 0, 0).getDate();
    }

    private static MicroSecondDate getTimeFromYearAndDayDirectory(File file) {
        String yearAndDay = file.getName();
        int year = Integer.valueOf(yearAndDay.substring(0, 4)).intValue();
        int dayOfYear = Integer.valueOf(yearAndDay.substring(4, 7)).intValue();
        return new ISOTime(year, dayOfYear, 0, 0, 0).getDate();
    }

    private static final Logger logger = Logger.getLogger(DirectoryNameParser.class);
}
