package edu.sc.seis.fissuresUtil.display;

import java.util.HashMap;
import java.util.Iterator;

/**
 * TimeSnapshot.java
 *
 *
 * Created: Tue Jul 16 12:14:18 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class TimeSnapshot {
    public TimeSnapshot(HashMap seismos, MicroSecondTimeRange tr){
	update(seismos, tr);
    }

    public void setGeneric(MicroSecondTimeRange tr){ this.tr = tr; }

    public void update(HashMap seismos, MicroSecondTimeRange tr){
	this.tr = tr;
	Iterator e = seismos.keySet().iterator();
	while(e.hasNext()){
	    DataSetSeismogram currentDSS = (DataSetSeismogram)e.next();
	    MicroSecondTimeRange currentTR = (MicroSecondTimeRange)seismos.get(currentDSS);
	    seismoDisplayTime.put(currentDSS, new MicroSecondTimeRange(currentTR.getBeginTime(), currentTR.getEndTime()));
	}
    }

    public MicroSecondTimeRange getTimeRange(){ return tr; }

    public MicroSecondTimeRange getTimeRange(DataSetSeismogram seis){ return (MicroSecondTimeRange)seismoDisplayTime.get(seis); }
    
    private MicroSecondTimeRange tr;

    private HashMap seismoDisplayTime = new HashMap();
    
}// TimeSnapshot
