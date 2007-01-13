package edu.sc.seis.fissuresUtil.database.problem;

import java.util.Calendar;
import junit.framework.TestCase;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.model.MicroSecondDate;

public class ProblemComponentTimeRangeTest extends TestCase {

    public void testGetTime() {
        Calendar cal = ProblemComponentTimeRange.getCalendarInstance();
        Time time = ProblemComponentTimeRange.getTime("2006:352:23:34:56.123",
                                                      cal);
        MicroSecondDate msd = new MicroSecondDate(time);
        cal.setTime(msd);
        assertEquals(cal.get(Calendar.YEAR), 2006);
        assertEquals(cal.get(Calendar.MONTH), 12 - 1);
        assertEquals(cal.get(Calendar.DAY_OF_MONTH), 18);
        assertEquals(cal.get(Calendar.HOUR_OF_DAY), 23);
        assertEquals(cal.get(Calendar.MINUTE), 34);
        assertEquals(cal.get(Calendar.SECOND), 56);
        assertEquals(cal.get(Calendar.MILLISECOND), 123);
    }
}
