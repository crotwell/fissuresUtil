/**
 * RelativeTimeConfig.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.display.registrar;

import java.util.Date;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

public class RelativeTimeConfig extends BasicTimeConfig{
    public void add(DataSetSeismogram[] seis){
        if(time == null && seis.length > 0){
            if(initialTime == null){
                initialTime = getInitialTime(seis[0]);
            }
            time = new MicroSecondTimeRange(new MicroSecondDate(new Date(5 * 24 * 60 * 60 * 1000)),
                                            initialTime.getInterval());
            initialTime = new MicroSecondTimeRange(time.getBeginTime(),
                                                   initialTime.getInterval());
            time = time.shale(shift, scale);
        }
        super.add(seis);
    }
    
    protected MicroSecondTimeRange getInitialTime(DataSetSeismogram seis){
        MicroSecondTimeRange rfTimeRange = new MicroSecondTimeRange(seis.getRequestFilter());
        TimeInterval interval;
        if(initialTime != null){
            interval = initialTime.getInterval();
        }else{
            interval = rfTimeRange.getInterval();
        }
        MicroSecondTimeRange rfIntervalTime = new MicroSecondTimeRange(rfTimeRange.getBeginTime(),
                                                                       interval);
        return rfIntervalTime.shale(shift, scale);
    }
}
