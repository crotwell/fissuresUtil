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
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

public class OriginAlignedTimeConfig extends RelativeTimeConfig {

    public MicroSecondTimeRange getInitialTime(DataSetSeismogram seis) {
        DataSet ds = seis.getDataSet();
        Origin seisOrigin;
        EventAccessOperations eao = ds.getEvent();
        if(eao != null) {
            try {
                seisOrigin = eao.get_preferred_origin();
            } catch(NoPreferredOrigin e) {
                if(eao.get_origins().length > 0) {
                    seisOrigin = eao.get_origins()[0];
                } else {
                    return super.getInitialTime(seis);
                }
            }
            MicroSecondDate originTime = new MicroSecondDate(seisOrigin.origin_time);
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
