package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.model.TimeInterval;

/**
 * EdgeTimeFinder.java
 *
 *
 * Created: Tue Jul 16 15:36:05 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class EdgeTimeFinder implements TimeFinder{

    public EdgeTimeFinder(TimeRangeConfig trc){
	this.trc = trc;
	trc.addTimeSyncListener(this);
    }

    public MicroSecondDate getBeginTime(DataSetSeismogram seismo){
	if(beginTime == null)
	    return ((LocalSeismogramImpl)seismo.getSeismogram()).getBeginTime();
	else
	    return beginTime;
    }
   
    public TimeInterval getDisplayInterval(DataSetSeismogram seismo){
	return new TimeInterval(((LocalSeismogramImpl)seismo.getSeismogram()).getBeginTime(), 
				((LocalSeismogramImpl)seismo.getSeismogram()).getEndTime());
    }

    public void updateTimeRange(){
	beginTime = trc.getTimeRange().getBeginTime();
    }

    protected TimeRangeConfig trc;

    protected MicroSecondDate beginTime;

}// EdgeTimeFinder
