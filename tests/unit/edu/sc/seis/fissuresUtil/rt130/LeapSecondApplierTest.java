package edu.sc.seis.fissuresUtil.rt130;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import junit.framework.TestCase;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.rt130.LeapSecondApplier;

public class LeapSecondApplierTest extends TestCase {

    public void setUp() throws IOException, ParseException {
        LeapSecondApplier.addLeapSeconds(leapSecondsFileLoc);
        LeapSecondApplier.addCorrections(correctionsFileLoc);
        format = new SimpleDateFormat("yy:DDD:HH:mm:ss:SSS");
        TimeZone timeZone = TimeZone.getTimeZone("GMT");
        format.setTimeZone(timeZone);
        cal = GregorianCalendar.getInstance();
        cal.setTimeZone(timeZone);
    }

    public void testAddLeapSeconds0() throws ParseException {
        List list = LeapSecondApplier.getLeapSecondOccurances();
        MicroSecondDate date0 = new MicroSecondDate(format.parse("05:365:23:59:59:999"));
        assertTrue(list.contains(date0));
        MicroSecondDate date1 = new MicroSecondDate(format.parse("06:181:23:59:59:999"));
        assertTrue(list.contains(date1));
    }

    public void testAddCorrections0() throws ParseException {
        String unit1 = "939D";
        String unit2 = "91FC";
        String unit3 = "940B";
        List list = LeapSecondApplier.getPowerUpTimes(unit1);
        MicroSecondDate date0 = new MicroSecondDate(format.parse("05:365:10:01:47:000"));
        assertTrue(list.contains(date0));
        MicroSecondDate date1 = new MicroSecondDate(format.parse("06:015:16:50:45:000"));
        assertTrue(list.contains(date1));
        list = LeapSecondApplier.getPowerUpTimes(unit2);
        MicroSecondDate date2 = new MicroSecondDate(format.parse("06:020:19:07:39:000"));
        assertTrue(list.contains(date2));
        list = LeapSecondApplier.getPowerUpTimes(unit3);
        MicroSecondDate date3 = new MicroSecondDate(format.parse("05:281:19:29:44:000"));
        assertTrue(list.contains(date3));
    }

    public void testNonApplicationOfLeapSecond_BeforeLeapSecondOccurrence()
            throws ParseException {
        MicroSecondDate date = new MicroSecondDate(format.parse("05:365:23:59:30:000"));
        MicroSecondDate newDate = LeapSecondApplier.applyLeapSecondCorrection("939D",
                                                                              date);
        cal.setTime(newDate);
        assertEquals(cal.get(Calendar.YEAR), 2005);
        assertEquals(cal.get(Calendar.DAY_OF_YEAR), 365);
        assertEquals(cal.get(Calendar.HOUR_OF_DAY), 23);
        assertEquals(cal.get(Calendar.MINUTE), 59);
        assertEquals(cal.get(Calendar.SECOND), 30);
    }

    public void testNonApplicationOfLeapSecond_BeforeLeapSecondOccurrence_EdgeCase()
            throws ParseException {
        MicroSecondDate date = new MicroSecondDate(format.parse("05:365:23:59:59:000"));
        MicroSecondDate newDate = LeapSecondApplier.applyLeapSecondCorrection("939D",
                                                                              date);
        cal.setTime(newDate);
        assertEquals(cal.get(Calendar.YEAR), 2005);
        assertEquals(cal.get(Calendar.DAY_OF_YEAR), 365);
        assertEquals(cal.get(Calendar.HOUR_OF_DAY), 23);
        assertEquals(cal.get(Calendar.MINUTE), 59);
        assertEquals(cal.get(Calendar.SECOND), 59);
    }

    public void testNonApplicationOfLeapSecond_AfterLeapSecondOccurrence_AfterPowerUpTime()
            throws ParseException {
        MicroSecondDate date = new MicroSecondDate(format.parse("06:015:16:50:46:000"));
        MicroSecondDate newDate = LeapSecondApplier.applyLeapSecondCorrection("939D",
                                                                              date);
        cal.setTime(newDate);
        assertEquals(cal.get(Calendar.YEAR), 2006);
        assertEquals(cal.get(Calendar.DAY_OF_YEAR), 15);
        assertEquals(cal.get(Calendar.HOUR_OF_DAY), 16);
        assertEquals(cal.get(Calendar.MINUTE), 50);
        assertEquals(cal.get(Calendar.SECOND), 46);
    }

    public void testApplicationOfLeapSecond_AfterLeapSecondOccurrence_EdgeCase()
            throws ParseException {
        MicroSecondDate date = new MicroSecondDate(format.parse("06:001:00:00:00:000"));
        MicroSecondDate newDate = LeapSecondApplier.applyLeapSecondCorrection("939D",
                                                                              date);
        cal.setTime(newDate);
        assertEquals(cal.get(Calendar.YEAR), 2005);
        assertEquals(cal.get(Calendar.DAY_OF_YEAR), 365);
        assertEquals(cal.get(Calendar.HOUR_OF_DAY), 23);
        assertEquals(cal.get(Calendar.MINUTE), 59);
        assertEquals(cal.get(Calendar.SECOND), 59);
    }

    public void testApplicationOfLeapSecond_AfterLeapSecondOccurrence()
            throws ParseException {
        MicroSecondDate date = new MicroSecondDate(format.parse("06:010:12:30:30:000"));
        MicroSecondDate newDate = LeapSecondApplier.applyLeapSecondCorrection("939D",
                                                                              date);
        cal.setTime(newDate);
        assertEquals(cal.get(Calendar.YEAR), 2006);
        assertEquals(cal.get(Calendar.DAY_OF_YEAR), 10);
        assertEquals(cal.get(Calendar.HOUR_OF_DAY), 12);
        assertEquals(cal.get(Calendar.MINUTE), 30);
        assertEquals(cal.get(Calendar.SECOND), 29);
    }

    public void testApplicationOfLeapSecond_BeforePowerUpTime_EdgeCase()
            throws ParseException {
        MicroSecondDate date = new MicroSecondDate(format.parse("06:015:16:50:45:000"));
        MicroSecondDate newDate = LeapSecondApplier.applyLeapSecondCorrection("939D",
                                                                              date);
        cal.setTime(newDate);
        assertEquals(cal.get(Calendar.YEAR), 2006);
        assertEquals(cal.get(Calendar.DAY_OF_YEAR), 15);
        assertEquals(cal.get(Calendar.HOUR_OF_DAY), 16);
        assertEquals(cal.get(Calendar.MINUTE), 50);
        assertEquals(cal.get(Calendar.SECOND), 44);
    }

    public void testApplicationOfLeapSecond_AfterTwoLeapSecondOccurrencesAndNoPowerUpTimes()
            throws ParseException {
        MicroSecondDate date = new MicroSecondDate(format.parse("06:182:12:30:30:000"));
        MicroSecondDate newDate = LeapSecondApplier.applyLeapSecondCorrection("6991",
                                                                              date);
        cal.setTime(newDate);
        assertEquals(cal.get(Calendar.YEAR), 2006);
        assertEquals(cal.get(Calendar.DAY_OF_YEAR), 182);
        assertEquals(cal.get(Calendar.HOUR_OF_DAY), 12);
        assertEquals(cal.get(Calendar.MINUTE), 30);
        assertEquals(cal.get(Calendar.SECOND), 28);
    }

    public void testApplicationOfLeapSecond_WithUnknownUnitId()
            throws ParseException {
        MicroSecondDate date = new MicroSecondDate(format.parse("06:005:12:30:30:000"));
        MicroSecondDate newDate = LeapSecondApplier.applyLeapSecondCorrection("xxxx",
                                                                              date);
        cal.setTime(newDate);
        assertEquals(cal.get(Calendar.YEAR), 2006);
        assertEquals(cal.get(Calendar.DAY_OF_YEAR), 005);
        assertEquals(cal.get(Calendar.HOUR_OF_DAY), 12);
        assertEquals(cal.get(Calendar.MINUTE), 30);
        assertEquals(cal.get(Calendar.SECOND), 29);
    }

    public void testApplicationOfLeapSecond_AfterTwoLeapSecondOccurences_WithUnkownUnitId()
            throws ParseException {
        MicroSecondDate date = new MicroSecondDate(format.parse("06:182:12:30:30:000"));
        MicroSecondDate newDate = LeapSecondApplier.applyLeapSecondCorrection("xxxx",
                                                                              date);
        cal.setTime(newDate);
        assertEquals(cal.get(Calendar.YEAR), 2006);
        assertEquals(cal.get(Calendar.DAY_OF_YEAR), 182);
        assertEquals(cal.get(Calendar.HOUR_OF_DAY), 12);
        assertEquals(cal.get(Calendar.MINUTE), 30);
        assertEquals(cal.get(Calendar.SECOND), 28);
    }

    private String leapSecondsFileLoc = "edu/sc/seis/fissuresUtil/rt130/LeapSecondsFile.txt";

    private String correctionsFileLoc = "edu/sc/seis/fissuresUtil/rt130/Corrections.txt";

    private SimpleDateFormat format;

    private Calendar cal;
}
