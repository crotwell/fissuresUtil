package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;

/**
 * SeismogramLoadedListener.java
 * Allows a backgroung seismogram loader to notify interested parties that the
 * attributes and preferred origin have been added to the cache for the seismogram.
 *
 * Created: Fri Jan 25 12:22:38 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface SeismogramLoadedListener extends java.util.EventListener {

    public void seismogramLoaded(RequestFilter request, 
				 LocalSeismogram[] seismogram);

    public void seismogramError(RequestFilter request, 
				 FissuresException e);
	    
}// SeismogramLoadedListener
