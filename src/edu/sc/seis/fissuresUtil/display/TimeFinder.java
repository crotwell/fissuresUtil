package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;

/**
 * TimeFinder.java
 * 
 *
 * Created: Tue Jul 16 15:34:00 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version 0.1
 */

public interface TimeFinder extends TimeSyncListener{
    
    public MicroSecondDate getBeginTime(DataSetSeismogram seismo);

    public TimeInterval getDisplayInterval(DataSetSeismogram seismo);

}// TimeFinder
