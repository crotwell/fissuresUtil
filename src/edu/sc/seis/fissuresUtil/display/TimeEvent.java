package edu.sc.seis.fissuresUtil.display;

/**
 * TimeEvent.java
 *
 *
 * Created: Sun Sep 15 19:58:40 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class TimeEvent {
   public TimeEvent(DataSetSeismogram[] seismos, MicroSecondTimeRange[] times){
       this.seismos = seismos;
       this.times = times;
   }
    
    public MicroSecondTimeRange getTime(DataSetSeismogram seismo){
	return times[indexOf(seismo)];
    }

    public MicroSecondTimeRange getTime(){
	if(genericTime == null){
	    genericTime = times[0];
	}
	return genericTime;
    }

    private int indexOf(DataSetSeismogram seismo){
	for(int i = 0; i < seismos.length; i++){
	    if(seismos[i] == seismo){
		return i;
	    }
	}
	return -1;
    }

    private DataSetSeismogram[] seismos;
    private MicroSecondTimeRange[] times;
    private MicroSecondTimeRange genericTime;    
}// TimeEvent
