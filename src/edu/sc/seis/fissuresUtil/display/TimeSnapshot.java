package edu.sc.seis.fissuresUtil.display;

import java.util.HashMap;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;

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
    public TimeSnapshot(HashMap seismoDisplayTime, MicroSecondTimeRange tr){
	this.seismoDisplayTime = seismoDisplayTime;
	this.tr = tr;
    }

    public MicroSecondTimeRange getTimeRange(){ return tr; }

    public MicroSecondTimeRange getTimeRange(LocalSeismogram seis){ return (MicroSecondTimeRange)seismoDisplayTime.get(seis); }
    
    private MicroSecondTimeRange tr;

    private HashMap seismoDisplayTime;
    
}// TimeSnapshot
