package edu.sc.seis.fissuresUtil.database;

import edu.iris.Fissures.IfSeismogramDC.*;

/**
 * SeisDataChangeEvent.java
 *
 *
 * Created: Thu Feb 20 13:43:59 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class SeisDataChangeEvent {
    public SeisDataChangeEvent (LocalSeismogram[] seismos,
				Object source,
				Object initiator){
	this.seismos = seismos;
	this.source = source;
	this.initiator = initiator;
    }

    

    public LocalSeismogram[] seismos;
    
    public Object source;
    
    public Object initiator;
    
}// SeisDataChangeEvent
