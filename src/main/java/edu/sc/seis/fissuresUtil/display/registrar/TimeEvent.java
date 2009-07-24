package edu.sc.seis.fissuresUtil.display.registrar;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

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
    public TimeEvent(DataSetSeismogram[] seismos, MicroSecondTimeRange[] times,
                     MicroSecondTimeRange genericTime){
        this.seismos = seismos;
        this.times = times;
        this.genericTime = genericTime;
    }

    public boolean contains(DataSetSeismogram seismo){
        if(indexOf(seismo) != -1){
            return true;
        }
        return false;
    }

    public MicroSecondTimeRange getTime(DataSetSeismogram seismo){
        int index = indexOf(seismo);
        if(index == -1){
            return DisplayUtils.ONE_TIME;
        }
        return times[indexOf(seismo)];
    }

    public MicroSecondTimeRange getTime(){
        if(genericTime == null){
            if(times.length > 0){
                genericTime = times[0];
            }else{
                genericTime = DisplayUtils.ONE_TIME;
            }
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
