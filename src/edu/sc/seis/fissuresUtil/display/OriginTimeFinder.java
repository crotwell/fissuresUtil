package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.IfEvent.*;
import edu.sc.seis.fissuresUtil.xml.XMLDataSet;


/**
 * OriginTimeFinder.java
 *
 *
 * Created: Wed Jul 17 17:16:53 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class OriginTimeFinder implements TimeFinder{
    public OriginTimeFinder(TimeRangeConfig trc){
	this.trc = trc;
	trc.addTimeSyncListener(this);
	MicroSecondDate zeroTime = new MicroSecondDate(0l);
	trc.set(new MicroSecondDate(zeroTime), new TimeInterval(zeroTime, zeroTime));
    }
   
    public MicroSecondDate getBeginTime(DataSetSeismogram seismo){
	MicroSecondDate beginTime;
	try{
	    beginTime = new MicroSecondDate(((XMLDataSet)seismo.getDataSet()).getEvent().get_preferred_origin().origin_time);
	}catch(NoPreferredOrigin e){
	    try{
		Origin[] origins = ((XMLDataSet)seismo.getDataSet()).getEvent().get_origins();
		beginTime = new MicroSecondDate(origins[0].origin_time);
	    }catch(Exception f){
		beginTime = ((LocalSeismogramImpl)seismo.getSeismogram()).getBeginTime();
	    }
	}
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
}// OriginTimeFinder
