package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

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

    public MicroSecondDate getBeginTime(DataSetSeismogram seismo){
	return ((LocalSeismogramImpl)seismo.getSeismogram()).getBeginTime();
    }
   
    public MicroSecondDate getEndTime(DataSetSeismogram seismo){
	return ((LocalSeismogramImpl)seismo.getSeismogram()).getEndTime();
    }


}// EdgeTimeFinder
