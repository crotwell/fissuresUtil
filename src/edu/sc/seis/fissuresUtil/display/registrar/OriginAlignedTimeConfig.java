/**
 * OriginAlignedTimeConfig.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.fissuresUtil.display.registrar;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

public class OriginAlignedTimeConfig extends RelativeTimeConfig {

    public MicroSecondTimeRange getInitialTime(DataSetSeismogram seis) {
        DataSet ds = seis.getDataSet();
        EventAccessOperations eao = ds.getEvent();
        if(eao != null) {
            MicroSecondDate originTime = new MicroSecondDate(EventUtil.extractOrigin(eao).origin_time);
            TimeInterval interval;
            if(initialTime != null) {
                interval = initialTime.getInterval();
            } else {
                MicroSecondDate seisEndTime = new MicroSecondDate(seis.getRequestFilter().end_time);
                interval = new TimeInterval(originTime, seisEndTime);
            }
            MicroSecondTimeRange originRange = new MicroSecondTimeRange(originTime,
                                                                        interval);
            return originRange.shale(shift, scale);
        }
        return super.getInitialTime(seis);
    }

    public String getTypeOfRelativity() {
        return "Time since the earthquake";
    }
}
