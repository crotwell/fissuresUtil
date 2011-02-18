package edu.sc.seis.fissuresUtil.display;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import edu.iris.Fissures.model.MicroSecondDate;

/**
 * AbsTimeScaleMapper.java
 *
 *
 * Created: Tue Oct 19 12:48:26 1999
 *
 * @author Philip Crotwell
 * @version
 */

public class AbsTimeScaleMapper extends TimeScaleMapper {

    public AbsTimeScaleMapper(int totalPixels,
                              int hintPixels,
                              MicroSecondDate beginTime,
                              MicroSecondDate endTime) {
        super(totalPixels, hintPixels, beginTime, endTime);
	dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT,
                                                        DateFormat.FULL);
	dateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	dateFormat = DateFormat.getDateInstance(DateFormat.FULL);
	dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	longTimeFormat = new SimpleDateFormat("HH:MM:ss.S");
	longTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	mediumTimeFormat = new SimpleDateFormat("MM:ss.S");
	mediumTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	shortTimeFormat = new SimpleDateFormat("ss.S");
	shortTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        this.calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calculateTicks();
    }

    public int getPixelLocation(int i) {
        return SimplePlotUtil.getPixel(totalPixels, beginTime, endTime,
				     new MicroSecondDate(Math.round(minTick.getMicroSecondTime() +
								    i * tickInc)));
    }

    public String getLabel(int i) {
        if (isLabelTick(i)) {
            MicroSecondDate date =
                new MicroSecondDate(Math.round(minTick.getMicroSecondTime()
                                               + i * tickInc));
            calendar.setTime(date);
            if(i== firstMajorTick){
		return longTimeFormat.format(calendar.getTime());
	    }
	    else
		return shortTimeFormat.format(calendar.getTime());
	    //            return dateTimeFormat.format(calendar.getTime());
        } else {
            return "";
        }
    }

    public int getNumTicks() { return numTicks;}

    public int getTotalPixels() {
      return totalPixels;
    }

    protected void calculateTicks() {

        if (totalPixels == 0) {
            numTicks = 0;
            return;
        }


        // aim for about hintNumber ticks
        double hintNumber = totalPixels / (double)hintPixels;

        // find power of ten just smaller than width
        long microSecWidth = endTime.getMicroSecondTime() -
            beginTime.getMicroSecondTime();
        tickInc = Math.pow(10,
			   Math.floor(Math.log(microSecWidth) /
				      Math.log(10.0)));
        double goalTickInc = microSecWidth / totalPixels * hintPixels;

        //mostly major ticks are ten x minor ticks, but may be overridden
        majorTickStep = 10;

        StartTickReturn retValue = startTick(goalTickInc, beginTime);

        minTick = retValue.minTick;
        majorTickStep = retValue.majorTickStep;
        tickInc = retValue.tickInc;

        retValue = startTick(tickInc * majorTickStep, beginTime);
        // this sets the minTick to the first major tick time,
        // we then move backwards to find the first minor tick before
        // the begin time
        minTick = retValue.minTick;

        long beginMicros = beginTime.getMicroSecondTime();
        long endMicros = endTime.getMicroSecondTime();
	long minMicros = minTick.getMicroSecondTime();
        firstMajorTick = 0;
        while (minMicros < beginMicros) {
            minMicros += tickInc;
            firstMajorTick--;
        }
        while (minMicros > beginMicros) {
            minMicros -= tickInc;
            firstMajorTick++;
        }
        if (firstMajorTick < 0) {
            firstMajorTick += majorTickStep;
        }
	minTick = new MicroSecondDate(minMicros);
//  	System.out.println("minTick="+minTick+
//                             "  TickInc="+tickInc+
//                             " firstMajor="+firstMajorTick+
//                             " majorStep="+majorTickStep+
//                             " numTicks="+numTicks);
        numTicks = 1;
	while (minMicros + numTicks * tickInc < endMicros ) {
            numTicks++;
        }
    }

    public StartTickReturn startTick(double goalTickInc, Date startTime) {
	calendar.setTime(startTime);
        StartTickReturn retValue = new StartTickReturn();
        retValue.majorTickStep = super.majorTickStep;

	if (goalTickInc < 500000L) {
	// less than .5 second, use decimal seconds
	    retValue.tickInc =  Math.pow(10,Math.floor(Math.log(goalTickInc)*Math.log(10.0)));
	    //if (goalTickInc > retValue.tickInc*4) {
		retValue.tickInc*=2;
		retValue.majorTickStep = 5;
		//}
            calendar.set(Calendar.MILLISECOND, 0);
            int sec = calendar.get(Calendar.SECOND);
	    sec -= 1;
	    calendar.set(Calendar.SECOND, sec);
	} else if (goalTickInc < 2 *1000000L) {
	    // less than 2 seconds, use integer seconds
	    retValue.tickInc = 1000000L;
	    calendar.set(Calendar.MILLISECOND, 0);
	    } else if (goalTickInc < 5 *1000000L) {
	    // less than 5 seconds, use integer 2 seconds
	    retValue.tickInc = 2 * 1000000L;
	    calendar.set(Calendar.MILLISECOND, 0);
	    int sec = calendar.get(Calendar.SECOND);
	    sec -= sec % 2;
	    calendar.set(Calendar.SECOND, sec);
            retValue.majorTickStep = 5; // major tick is 10 seconds
	} else if (goalTickInc < 20 *1000000L) {
	    // less than 20 seconds, use integer 10 seconds
	    retValue.tickInc = 10 * 1000000L;
	    calendar.set(Calendar.MILLISECOND, 0);
	    int sec = calendar.get(Calendar.SECOND);
	    sec -= sec % 10;
	    calendar.set(Calendar.SECOND, sec);
            retValue.majorTickStep = 6; // major tick is minutes
	} else if (goalTickInc < 2*60 *1000000L) {
	    // less than 2 minutes, use integer minutes
	    retValue.tickInc = 60 *1000000L;
	    calendar.set(Calendar.MILLISECOND, 0);
	    calendar.set(Calendar.SECOND, 0);
	} else if (goalTickInc < 20*60 *1000000L) {
	    // less than 20 minutes, use integer 10 minutes
	    retValue.tickInc = 600 * 1000000L;
	    calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.SECOND, 0);
	    int min = calendar.get(Calendar.MINUTE);
	    min -= min % 10;
	    calendar.set(Calendar.MINUTE, min);
            retValue.majorTickStep = 6; // major tick is hours
	} else if (goalTickInc < 2*60*60 *1000000L) {
	    // less than 2 hours, use integer hours
	    retValue.tickInc = 60*60 *1000000L;
	    calendar.set(Calendar.MILLISECOND, 0);
	    calendar.set(Calendar.SECOND, 0);
	    calendar.set(Calendar.MINUTE, 0);
            retValue.majorTickStep = 24;  // major tick is days
	} else if (goalTickInc < 2*86400L *1000000L) {
	    // less than 2 days, use integer days
	    retValue.tickInc = 24*60*60 *1000000L;
	    calendar.set(Calendar.MILLISECOND, 0);
	    calendar.set(Calendar.SECOND, 0);
	    calendar.set(Calendar.MINUTE, 0);
	    calendar.set(Calendar.HOUR, 0);
	} else if (goalTickInc < 20*86400L *1000000L) {
	    // less than 20 days, use integer 10 days
	    calendar.set(Calendar.MILLISECOND, 0);
	    calendar.set(Calendar.SECOND, 0);
	    calendar.set(Calendar.MINUTE, 0);
	    calendar.set(Calendar.HOUR, 0);
	    int jday = calendar.get(Calendar.DAY_OF_YEAR);
	    jday -= jday % 10;
	    calendar.set(Calendar.DAY_OF_YEAR, jday);
	    retValue.tickInc =  10*24*60*60 *1000000L;
	} else  if (goalTickInc < 200*86400L *1000000L) {
	    // less than 200 days, use integer 100 days
	    calendar.set(Calendar.MILLISECOND, 0);
	    calendar.set(Calendar.SECOND, 0);
	    calendar.set(Calendar.MINUTE, 0);
	    calendar.set(Calendar.HOUR, 0);
	    int jday = calendar.get(Calendar.DAY_OF_YEAR);
	    jday -= jday % 100;
	    calendar.set(Calendar.DAY_OF_YEAR, jday);
	    retValue.tickInc = 100*24*60*60 *1000000L;
	}
        retValue.minTick = new MicroSecondDate(calendar.getTime());
        return retValue;
    }

    protected java.text.DateFormat dateFormat;
    protected java.text.DateFormat dateTimeFormat;
    protected java.text.SimpleDateFormat longTimeFormat;
    protected java.text.SimpleDateFormat mediumTimeFormat;
    protected java.text.SimpleDateFormat shortTimeFormat;
    protected Calendar calendar;
    protected int firstMajorTick;

    protected final class StartTickReturn {
        MicroSecondDate minTick;
        double tickInc;
        int majorTickStep;
    }

} // AbsTimeScaleMapper


