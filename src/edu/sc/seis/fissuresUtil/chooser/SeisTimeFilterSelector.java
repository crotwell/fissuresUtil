
package edu.sc.seis.fissuresUtil.chooser;

import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;
import java.awt.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import java.lang.Integer;

import org.apache.log4j.*;

/**
 * SeisTimeWindowSelector.java
 *
 *
 * Created: Mon Mar 19 09:29:38 2001
 *
 * @author Srinivasa Telukutla.
 * @version
 */





public class SeisTimeFilterSelector  {

    public SeisTimeFilterSelector() {
    }

    public LocalSeismogram[] getFromGivenFilters(DataCenterOperations dataCenter,
                         RequestFilter[] filterseq) throws FissuresException {
    for (int i=0; i<filterseq.length; i++) {
            logger.info("request "+ChannelIdUtil.toString(filterseq[i].channel_id));
            logger.info("   from "+filterseq[i].start_time.date_time);
            logger.info("   to   "+filterseq[i].end_time.date_time);
        }
    LocalSeismogram[] ls = dataCenter.retrieve_seismograms(filterseq);
        logger.info("Got "+ls.length+" seismograms.");
        return ls;


    }


    public RequestFilter[] makeFiltersGivenChannelId(edu.iris.Fissures.IfNetwork.ChannelId channel_id,
                             int year,
                             int jday,
                             edu.iris.Fissures.Dimension pixel_size,
                             edu.iris.Fissures.Time sTime,
                             edu.iris.Fissures.Time eTime) {


    StringBuffer dayBuffer = new StringBuffer();
    DecimalFormat numberFormat = new DecimalFormat();
    numberFormat.setMinimumIntegerDigits(3);
    numberFormat.format((long)jday, dayBuffer, new FieldPosition(0));

    String startTimeStr = new java.lang.Integer(year).toString();

    startTimeStr =  startTimeStr  + dayBuffer + "T00:00:00.000Z";
    edu.iris.Fissures.Time startTime = sTime;
    String endTimeStr = new java.lang.Integer(year).toString();
    endTimeStr = endTimeStr  + dayBuffer + "T23:59:59.999z";
    edu.iris.Fissures.Time endTime = eTime;
    RequestFilter[] filters;
    TimeRange window = new TimeRange(startTime, endTime);
    filters = new RequestFilter[1];
    filters[0] =
        new RequestFilter(channel_id,
                  window.start_time,
                  window.end_time);

    return filters;
    }






    static Category logger = Category.getInstance(SeisTimeFilterSelector.class.getName());

} // SeisTimeFilterSelector
