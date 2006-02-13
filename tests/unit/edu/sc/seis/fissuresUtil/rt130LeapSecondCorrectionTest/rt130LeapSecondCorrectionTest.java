package edu.sc.seis.fissuresUtil.rt130LeapSecondCorrectionTest;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import junit.framework.TestCase;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.rt130.leapSecondCorrection.LeapSecondApplier;

public class rt130LeapSecondCorrectionTest extends TestCase {

    public void setUp() throws IOException, ParseException {
        leapSecondsFileLoc = "C:\\Documents and Settings\\fenner.POKEY\\Desktop\\src\\seis\\fissuresUtil\\tests\\unit\\edu\\sc\\seis\\fissuresUtil\\rt130LeapSecondCorrectionTest\\LeapSecondsFile.txt";
        correctionsFileLoc = "C:\\Documents and Settings\\fenner.POKEY\\Desktop\\src\\seis\\fissuresUtil\\tests\\unit\\edu\\sc\\seis\\fissuresUtil\\rt130LeapSecondCorrectionTest\\Corrections.txt";
        /*--------------
         * 
         * LeapSecondsFile.txt:
         * 05:365:23:59:59:999
         * 06:181:23:59:59:999
         * 
         * Corrections.txt:
         * 939D;05:365:10:01:47:000
         * 939D;06:015:16:50:45:000
         * 91FC;06:020:19:07:39:000
         * 940B;05:281:19:29:44:000
         * 6991;06:195:12:30:30:000
         * 
         --------------*/
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
        Map map = LeapSecondApplier.getMap();
        MicroSecondDate date0 = new MicroSecondDate(format.parse("05:365:10:01:47:000"));
        assertTrue(((List)map.get("939D")).contains(date0));
        MicroSecondDate date1 = new MicroSecondDate(format.parse("06:015:16:50:45:000"));
        assertTrue(((List)map.get("939D")).contains(date1));
        MicroSecondDate date2 = new MicroSecondDate(format.parse("06:020:19:07:39:000"));
        assertTrue(((List)map.get("91FC")).contains(date2));
        MicroSecondDate date3 = new MicroSecondDate(format.parse("05:281:19:29:44:000"));
        assertTrue(((List)map.get("940B")).contains(date3));
    }

    public void testNonApplicationOfLeapSecond0() throws ParseException {
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

    public void testNonApplicationOfLeapSecond1() throws ParseException {
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

    public void testNonApplicationOfLeapSecond2() throws ParseException {
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

    public void testApplicationOfLeapSecond0() throws ParseException {
        MicroSecondDate date = new MicroSecondDate(format.parse("06:001:00:00:00:000"));
        MicroSecondDate newDate = LeapSecondApplier.applyLeapSecondCorrection("939D",
                                                                              date);
        cal.setTime(newDate);
        assertEquals(cal.get(Calendar.YEAR), 2006);
        assertEquals(cal.get(Calendar.DAY_OF_YEAR), 1);
        assertEquals(cal.get(Calendar.HOUR_OF_DAY), 0);
        assertEquals(cal.get(Calendar.MINUTE), 0);
        assertEquals(cal.get(Calendar.SECOND), 1);
    }

    public void testApplicationOfLeapSecond1() throws ParseException {
        MicroSecondDate date = new MicroSecondDate(format.parse("06:010:12:30:30:000"));
        MicroSecondDate newDate = LeapSecondApplier.applyLeapSecondCorrection("939D",
                                                                              date);
        cal.setTime(newDate);
        assertEquals(cal.get(Calendar.YEAR), 2006);
        assertEquals(cal.get(Calendar.DAY_OF_YEAR), 10);
        assertEquals(cal.get(Calendar.HOUR_OF_DAY), 12);
        assertEquals(cal.get(Calendar.MINUTE), 30);
        assertEquals(cal.get(Calendar.SECOND), 31);
    }

    public void testApplicationOfLeapSecond2() throws ParseException {
        MicroSecondDate date = new MicroSecondDate(format.parse("06:015:16:50:44:000"));
        MicroSecondDate newDate = LeapSecondApplier.applyLeapSecondCorrection("939D",
                                                                              date);
        cal.setTime(newDate);
        assertEquals(cal.get(Calendar.YEAR), 2006);
        assertEquals(cal.get(Calendar.DAY_OF_YEAR), 15);
        assertEquals(cal.get(Calendar.HOUR_OF_DAY), 16);
        assertEquals(cal.get(Calendar.MINUTE), 50);
        assertEquals(cal.get(Calendar.SECOND), 45);
    }

    public void testApplicationOfLeapSecond3() throws ParseException {
        MicroSecondDate date = new MicroSecondDate(format.parse("06:015:16:50:45:000"));
        MicroSecondDate newDate = LeapSecondApplier.applyLeapSecondCorrection("939D",
                                                                              date);
        cal.setTime(newDate);
        assertEquals(cal.get(Calendar.YEAR), 2006);
        assertEquals(cal.get(Calendar.DAY_OF_YEAR), 15);
        assertEquals(cal.get(Calendar.HOUR_OF_DAY), 16);
        assertEquals(cal.get(Calendar.MINUTE), 50);
        assertEquals(cal.get(Calendar.SECOND), 46);
    }

    public void testApplicationOfLeapSecond4() throws ParseException {
        MicroSecondDate date = new MicroSecondDate(format.parse("06:182:12:30:30:000"));
        MicroSecondDate newDate = LeapSecondApplier.applyLeapSecondCorrection("6991",
                                                                              date);
        cal.setTime(newDate);
        assertEquals(cal.get(Calendar.YEAR), 2006);
        assertEquals(cal.get(Calendar.DAY_OF_YEAR), 182);
        assertEquals(cal.get(Calendar.HOUR_OF_DAY), 12);
        assertEquals(cal.get(Calendar.MINUTE), 30);
        assertEquals(cal.get(Calendar.SECOND), 32);
    }

    public void testApplicationOfLeapSecond5() throws ParseException {
        MicroSecondDate date = new MicroSecondDate(format.parse("06:005:12:30:30:000"));
        MicroSecondDate newDate = LeapSecondApplier.applyLeapSecondCorrection("xxxx",
                                                                              date);
        cal.setTime(newDate);
        assertEquals(cal.get(Calendar.YEAR), 2006);
        assertEquals(cal.get(Calendar.DAY_OF_YEAR), 005);
        assertEquals(cal.get(Calendar.HOUR_OF_DAY), 12);
        assertEquals(cal.get(Calendar.MINUTE), 30);
        assertEquals(cal.get(Calendar.SECOND), 31);
    }

    public void testApplicationOfLeapSecond6() throws ParseException {
        MicroSecondDate date = new MicroSecondDate(format.parse("06:182:12:30:30:000"));
        MicroSecondDate newDate = LeapSecondApplier.applyLeapSecondCorrection("xxxx",
                                                                              date);
        cal.setTime(newDate);
        assertEquals(cal.get(Calendar.YEAR), 2006);
        assertEquals(cal.get(Calendar.DAY_OF_YEAR), 182);
        assertEquals(cal.get(Calendar.HOUR_OF_DAY), 12);
        assertEquals(cal.get(Calendar.MINUTE), 30);
        assertEquals(cal.get(Calendar.SECOND), 32);
    }

    private String leapSecondsFileLoc, correctionsFileLoc;

    private SimpleDateFormat format;

    private Calendar cal;
}
