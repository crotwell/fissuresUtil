package edu.sc.seis.fissuresUtil.display.registrar;

/**
 * RelativeTimeEvent.java
 *
 * @author Created by Charlie Groves
 */

import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

public class RelativeTimeEvent extends TimeEvent{
    public RelativeTimeEvent(DataSetSeismogram[] seismos,
                             MicroSecondTimeRange[] times,
                            MicroSecondTimeRange genericTime){
        super(seismos, times);
        this.seismos = seismos;
        this.times = times;
        this.genericTime = genericTime;
    }

    public MicroSecondTimeRange getTime(){
        if(seismos.length <= 1){
            return super.getTime();
        }else{
            System.out.println(genericTime);
            return genericTime;
        }
    }

    private MicroSecondTimeRange genericTime;

    private DataSetSeismogram[] seismos;

    private MicroSecondTimeRange[] times;
}

