package edu.sc.seis.fissuresUtil.display.borders;

import java.awt.Dimension;
import java.awt.Font;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.display.BasicSeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.registrar.RTTimeRangeConfig;
import edu.sc.seis.fissuresUtil.display.registrar.RelativeTimeConfig;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;

public class TimeBorder extends Border implements TitleProvider {

    public TimeBorder(SeismogramDisplay disp) {
        this(disp, TOP);
    }

    public TimeBorder(SeismogramDisplay disp, int position) {
        super(position, ASCENDING);
        add((TitleProvider)this);
        this.disp = disp;
        setPreferredSize(new Dimension(BasicSeismogramDisplay.PREFERRED_WIDTH,
                                       50));
        axisFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public RelativeTimeConfig getRelative(TimeConfig tc) {
        if(tc instanceof RTTimeRangeConfig) {
            return getRelative(((RTTimeRangeConfig)tc).getInternalConfig());
        } else {
            return (RelativeTimeConfig)tc;
        }
    }

    public String getTitle() {
        MicroSecondTimeRange time = disp.getTimeConfig().getTime();
        if(roundTheEpoch.intersects(time)) {
            return getRelative(disp.getTimeConfig()).getTypeOfRelativity();
        } else {
            Date middleDate = time.getBeginTime()
                    .add(new TimeInterval(time.getInterval().divideBy(2)));
            calendar.setTime(middleDate);
            return axisFormat.format(calendar.getTime());
        }
    }

    public void setTitleFont(Font f) {
        this.titleFont = f;
    }

    public Font getTitleFont() {
        return titleFont;
    }

    protected UnitRangeImpl getRange() {
        return disp.getTimeConfig().getTime().getMillis();
    }

    protected List createFormats() {
        List formats = new ArrayList();
        formats.add(new TimeBorderFormat("mm:ss.SSS",
                                         new TimeInterval(1,
                                                          UnitImpl.MILLISECOND)));
        formats.add(new TimeBorderFormat("mm:ss.SSS",
                                         new TimeInterval(10,
                                                          UnitImpl.MILLISECOND)));
        createSecondFormats(secDivs, formats);
        createMinuteSecondFormats(minSecDivs, formats);
        createMinuteFormats(minDivs, formats);
        createHourFormats(hourDivs, formats);
        return formats;
    }

    //this is used to create formats with the given number of seconds between
    //labelled ticks and 10 ticks between each labelled tick
    private static double[] secDivs = {.01, .02, .05, .1, .25, .5, 1, 2.5};

    //this is used to create foramts with the first number used to determine
    // how
    //many seconds between each major tick, and the second to determine how
    // many
    //ticks for each major tick
    private static double[][] minSecDivs = { {5, 5}, {10, 5}, {20, 4}, {30, 6}};

    //this is used to create foramts with the first number used to determine
    // how
    //many minutes between each major tick, and the second to determine how
    // many
    //ticks for each major tick
    private static double[][] minDivs = { {1, 6},
                                         {2, 6},
                                         {5, 5},
                                         {10, 10},
                                         {20, 10},
                                         {30, 10}};

    //this is used to create foramts with the first number used to determine
    // how
    //many hours between each major tick, and the second to determine how many
    //ticks for each major tick
    private static double[][] hourDivs = { {1, 6},
                                          {2, 4},
                                          {6, 6},
                                          {12, 6},
                                          {24, 4},
                                          {48, 4},
                                          {96, 4},};

    private void createSecondFormats(double[] secondsPerDivision, List recip) {
        for(int i = 0; i < secondsPerDivision.length; i++) {
            createSecondFormat(secondsPerDivision[i], recip);
        }
    }

    private void createSecondFormat(double secondsPerDivision, List recip) {
        TimeInterval inter = new TimeInterval(secondsPerDivision,
                                              UnitImpl.SECOND);
        recip.add(new TimeBorderFormat("mm:ss.SSS", inter));
    }

    private void createMinuteSecondFormats(double[][] divs, List recip) {
        for(int i = 0; i < divs.length; i++) {
            createHourFormat(divs[i][0],
                             (int)divs[i][1],
                             recip,
                             UnitImpl.SECOND);
        }
    }

    private void createMinuteFormats(double[][] divs, List recip) {
        for(int i = 0; i < divs.length; i++) {
            createHourFormat(divs[i][0],
                             (int)divs[i][1],
                             recip,
                             UnitImpl.MINUTE);
        }
    }

    private void createHourFormat(double minutesPerDivision,
                                  int divPerLabel,
                                  List recip,
                                  UnitImpl unit) {
        TimeInterval inter = new TimeInterval(minutesPerDivision, unit);
        recip.add(new TimeBorderFormat("HH:mm:ss", inter, divPerLabel));
    }

    private void createHourFormats(double[][] divs, List recip) {
        for(int i = 0; i < divs.length; i++) {
            createDayFormat(divs[i][0], (int)divs[i][1], recip);
        }
    }

    private void createDayFormat(double hoursPerDivision,
                                 int divPerLabel,
                                 List recip) {
        TimeInterval inter = new TimeInterval(hoursPerDivision, UnitImpl.HOUR);
        recip.add(new TimeBorderFormat("MM/dd HH:mm", inter, divPerLabel));
    }

    public String getMaxLengthFormattedString() {
        return ("MM/dd HH:mm");
    }

    private SeismogramDisplay disp;

    class TimeBorderFormat extends BorderFormat {

        public TimeBorderFormat(String format, TimeInterval labelInterval) {
            this(format, labelInterval, 10);
        }

        public TimeBorderFormat(String format, TimeInterval labelInterval,
                int ticksPerDivision) {
            super(labelInterval.convertTo(UnitImpl.MILLISECOND).getValue(),
                  ticksPerDivision);
            this.format = new SimpleDateFormat(format);
            this.format.setTimeZone(TimeZone.getTimeZone("GMT"));
        }

        public String getLabel(double value) {
            return format.format(new Date((long)value));
        }

        public String getMaxString() {
            return format.format(FULL_DATE);
        }

        private DateFormat format;
    }

    private Font titleFont = DisplayUtils.DEFAULT_FONT;

    private Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

    private SimpleDateFormat axisFormat = new SimpleDateFormat("MM/dd/yyyy (zzz)");

    //Five days before the epoch to 10 after
    public static MicroSecondTimeRange roundTheEpoch = new MicroSecondTimeRange(new MicroSecondDate(0),
                                                                                new TimeInterval(20,
                                                                                                 UnitImpl.DAY));

    //FULL_DATE has 2 digits for hours, minutes and seconds, and 999 millis
    private static Date FULL_DATE;
    static {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("EST"));
        cal.set(1999, 12, 31, 12, 59, 59);
        FULL_DATE = cal.getTime();
        FULL_DATE.setTime(FULL_DATE.getTime() + 999);
    }
}
