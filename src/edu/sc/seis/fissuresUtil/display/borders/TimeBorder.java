package edu.sc.seis.fissuresUtil.display.borders;
import java.util.*;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.display.BasicSeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import java.awt.Dimension;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class TimeBorder extends Border implements TitleProvider{
    public TimeBorder(SeismogramDisplay disp){ this(disp, TOP); }

    public TimeBorder(SeismogramDisplay disp, int position){
        super(position, ASCENDING);
        add((TitleProvider)this);
        this.disp = disp;
        setPreferredSize(new Dimension(BasicSeismogramDisplay.PREFERRED_WIDTH,
                                       50));
    }

    public String getTitle() {
        MicroSecondTimeRange time = disp.getTimeConfig().getTime();
        if(roundTheEpoch.intersects(time)) return "Relative time";
        else{
            Date middleDate = time.getBeginTime().add(new TimeInterval(time.getInterval().divideBy(2)));
            calendar.setTime(middleDate);
            return axisFormat.format(calendar.getTime()) + " (GMT)";
        }
    }

    protected UnitRangeImpl getRange(){
        return disp.getTimeConfig().getTime().convertTo(UnitImpl.MILLISECOND);
    }

    protected List createFormats(){
        List borderFormats = new ArrayList();
        borderFormats.add(new TimeBorderFormat("mm:ss.SSS",
                                               new TimeInterval(1, UnitImpl.MILLISECOND)));
        borderFormats.add(new TimeBorderFormat("mm:ss.SSS",
                                               new TimeInterval(10, UnitImpl.MILLISECOND)));
        createSecondFormats(secDivs, borderFormats);
        createMinuteFormats(minDivs, borderFormats);
        createHourFormats(hourDivs, borderFormats);
        return borderFormats;
    }

    //this is used to create formats with the given number of seconds between
    //labelled ticks and 10 ticks between each labelled tick
    private static double[] secDivs = { .1, .25, .5, 1, 3, 5, 10, 20, 30 };

    //this is used to create foramts with the first number used to determine how
    //many minutes between each major tick, and the second to determine how many
    //ticks for each major tick
    private static double[][] minDivs = {{1, 6},{2, 6},{5, 5},{10,10},{20,10},{30,10}};

    //this is used to create foramts with the first number used to determine how
    //many hours between each major tick, and the second to determine how many
    //ticks for each major tick
    private static double[][] hourDivs = {{1, 6},{2, 6},{6, 6},{12,6},{24,4},{48,4}};

    private void createSecondFormats(double[] secondsPerDivision, List recip){
        for (int i = 0; i < secondsPerDivision.length; i++) {
            createSecondFormat(secondsPerDivision[i], recip);
        }
    }

    private void createSecondFormat(double secondsPerDivision, List recip){
        TimeInterval inter =  new TimeInterval(secondsPerDivision, UnitImpl.SECOND);
        recip.add(new TimeBorderFormat("mm:ss.SSS", inter));
    }

    private void createMinuteFormats(double[][] minDivs, List recip){
        for (int i = 0; i < minDivs.length; i++) {
            createHourFormat(minDivs[i][0], (int)minDivs[i][1], recip);
        }
    }

    private void createHourFormat(double minutesPerDivision, int divPerLabel, List recip){
        TimeInterval inter = new TimeInterval(minutesPerDivision, UnitImpl.MINUTE);
        recip.add(new TimeBorderFormat("HH:mm:ss", inter, divPerLabel));
    }

    private void createHourFormats(double[][] minDivs, List recip){
        for (int i = 0; i < minDivs.length; i++) {
            createDayFormat(minDivs[i][0], (int)minDivs[i][1], recip);
        }
    }

    private void createDayFormat(double hoursPerDivision, int divPerLabel, List recip){
        TimeInterval inter = new TimeInterval(hoursPerDivision, UnitImpl.HOUR);
        recip.add(new TimeBorderFormat("MM/dd HH:mm", inter, divPerLabel));
    }

    private SeismogramDisplay disp;

     class TimeBorderFormat extends BorderFormat{
        public TimeBorderFormat(String format, TimeInterval labelInterval){
            this(format, labelInterval, 10);
        }

        public TimeBorderFormat(String format,
                                TimeInterval labelInterval,
                                int ticksPerDivision){
            super(labelInterval.convertTo(UnitImpl.MILLISECOND).getValue(),
                  ticksPerDivision);
            this.format = new SimpleDateFormat(format);
            this.format.setTimeZone(TimeZone.getTimeZone("GMT"));
        }

        public String getLabel(double value) {
            return format.format(new Date((long)value));
        }

        public String getMaxString() { return format.format(FULL_DATE); }

        private DateFormat format;
    }

    private Calendar calendar  = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    private SimpleDateFormat axisFormat = new SimpleDateFormat("MM/dd/yyyy");
    //Five days before the epoch to 10 after
    public static MicroSecondTimeRange roundTheEpoch = new MicroSecondTimeRange(new MicroSecondDate(0),
                                                                                new TimeInterval(20, UnitImpl.DAY));

    //FULL_DATE has 2 digits for hours, minutes and seconds, and 999 millis
    private static Date FULL_DATE;
    static{
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("EST"));
        cal.set(1999, 12, 31, 12, 59, 59);
        FULL_DATE = cal.getTime();
        FULL_DATE.setTime(FULL_DATE.getTime() + 999);
    }
}
