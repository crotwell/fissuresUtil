package edu.sc.seis.fissuresUtil.rt130;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import junit.framework.TestCase;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.rt130.LeapSecondApplier;

public class LeapSecondApplierTest extends TestCase {

    public void setUp() throws IOException, ParseException {
        LeapSecondApplier.addLeapSeconds(leapSecondsFileLoc);
        LeapSecondApplier.addCorrections(correctionsFileLoc);
        format = new SimpleDateFormat("yyyy:DDD:HH:mm:ss:SSS");
        TimeZone timeZone = TimeZone.getTimeZone("GMT");
        format.setTimeZone(timeZone);
        cal = GregorianCalendar.getInstance();
        cal.setTimeZone(timeZone);
    }

    public void testAddLeapSeconds0() throws ParseException {
        List list = LeapSecondApplier.getLeapSecondOccurances();
        MicroSecondDate date0 = new MicroSecondDate(format.parse("2005:365:23:59:59:999"));
        assertTrue(list.contains(date0));
        MicroSecondDate date1 = new MicroSecondDate(format.parse("2006:181:23:59:59:999"));
        assertTrue(list.contains(date1));
    }

    public void testAddCorrections0() throws ParseException {
        String unit1 = "939D";
        String unit2 = "91FC";
        String unit3 = "940B";
        List list = LeapSecondApplier.getPowerUpTimes(unit1);
        MicroSecondDate date0 = new MicroSecondDate(format.parse("2005:365:10:01:47:000"));
        Iterator it = list.iterator();
        while(it.hasNext()) {
            MicroSecondDate obj = (MicroSecondDate)it.next();
            System.out.println(obj+"  "+obj.getMicroSecondTime()+" "+obj.getLeapSecondVersion()+"  "+date0.equals(obj)+"  "+date0+" "+date0.getMicroSecondTime()+" "+date0.getLeapSecondVersion());
        }
        assertTrue(list.contains(date0));
        MicroSecondDate date1 = new MicroSecondDate(format.parse("2006:015:16:50:45:000"));
        assertTrue(list.contains(date1));
        list = LeapSecondApplier.getPowerUpTimes(unit2);
        MicroSecondDate date2 = new MicroSecondDate(format.parse("2006:020:19:07:39:000"));
        assertTrue(list.contains(date2));
        list = LeapSecondApplier.getPowerUpTimes(unit3);
        MicroSecondDate date3 = new MicroSecondDate(format.parse("2005:281:19:29:44:000"));
        assertTrue(list.contains(date3));
    }

    public void testNonApplicationOfLeapSecond_BeforeLeapSecondOccurrence()
            throws ParseException {
        MicroSecondDate date = new MicroSecondDate(format.parse("2005:365:23:59:30:000"));
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
        MicroSecondDate date = new MicroSecondDate(format.parse("2005:365:23:59:59:000"));
        MicroSecondDate newDate = LeapSecondApplier.applyLeapSecondCorrection("939D",
                                                                              date);
        cal.setTime(newDate);
        assertEquals(2005, cal.get(Calendar.YEAR));
        assertEquals(365, cal.get(Calendar.DAY_OF_YEAR));
        assertEquals(23, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(59, cal.get(Calendar.MINUTE));
        assertEquals(59, cal.get(Calendar.SECOND));
    }

    public void testNonApplicationOfLeapSecond_AfterLeapSecondOccurrence_AfterPowerUpTime()
            throws ParseException {
        MicroSecondDate date = new MicroSecondDate(format.parse("2006:015:16:50:46:000"));
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
        MicroSecondDate date = new MicroSecondDate(format.parse("2006:001:00:00:00:000"));
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
        MicroSecondDate date = new MicroSecondDate(format.parse("2006:010:12:30:30:000"));
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
        MicroSecondDate date = new MicroSecondDate(format.parse("2006:015:16:50:45:000"));
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
        MicroSecondDate date = new MicroSecondDate(format.parse("2006:182:12:30:30:000"));
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
        MicroSecondDate date = new MicroSecondDate(format.parse("2006:005:12:30:30:000"));
        MicroSecondDate newDate = LeapSecondApplier.applyLeapSecondCorrection("xxxx",
                                                                              date);
        cal.setTime(newDate);
        assertEquals(2006, cal.get(Calendar.YEAR));
        assertEquals(005, cal.get(Calendar.DAY_OF_YEAR));
        assertEquals( 12, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(30, cal.get(Calendar.SECOND));
    }

    public void testApplicationOfLeapSecond_AfterTwoLeapSecondOccurences_WithUnkownUnitId()
            throws ParseException {
        MicroSecondDate date = new MicroSecondDate(format.parse("2006:182:12:30:30:000"));
        MicroSecondDate newDate = LeapSecondApplier.applyLeapSecondCorrection("xxxx",
                                                                              date);
        cal.setTime(newDate);
        assertEquals(2006, cal.get(Calendar.YEAR));
        assertEquals(182, cal.get(Calendar.DAY_OF_YEAR));
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(30, cal.get(Calendar.SECOND));
    }
    
    public void test940B() throws ParseException {
        MicroSecondDate date = new MicroSecondDate(format.parse("2006:001:12:30:30:000"));
        MicroSecondDate newDate = LeapSecondApplier.applyLeapSecondCorrection("940B",
                                                                              date);
        cal.setTime(newDate);
        assertEquals(2006, cal.get(Calendar.YEAR));
        assertEquals(001, cal.get(Calendar.DAY_OF_YEAR));
        assertEquals( 12, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(30, cal.get(Calendar.SECOND));
    }

    private String leapSecondsFileLoc = "edu/sc/seis/fissuresUtil/rt130/LeapSecondsFile.txt";

    private String correctionsFileLoc = "edu/sc/seis/fissuresUtil/rt130/Corrections.txt";

    private SimpleDateFormat format;

    private Calendar cal;
}
